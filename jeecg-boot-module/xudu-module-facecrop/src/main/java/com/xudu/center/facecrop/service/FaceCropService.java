package com.xudu.center.facecrop.service;

import com.xudu.center.facecrop.config.FaceProcessingProperties;
import com.xudu.center.facecrop.model.FaceQuality;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;
import org.springframework.stereotype.Component;
import com.xudu.center.facecrop.model.FaceCropResponse;
import com.xudu.center.facecrop.model.FaceCropStatus;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class FaceCropService {

    private final FaceProcessingProperties props;

    // ThreadLocal 示例：每个线程一个检测器实例，避免共享可变状态
    private final ThreadLocal<FaceDetector<DetectedFace, FImage>> detectorTL;
    private final ThreadLocal<FaceDetector<DetectedFace, FImage>> eyeDetectorTL;

    public FaceCropService(FaceProcessingProperties props) {
        this.props = props;
        this.detectorTL = ThreadLocal.withInitial(() -> new HaarCascadeDetector(props.getMinSizePx()));
        // 眼睛级联（OpenIMAJ 内置的 OpenCV haarcascade_eye.xml）
        this.eyeDetectorTL = ThreadLocal.withInitial(() ->
                new HaarCascadeDetector(HaarCascadeDetector.BuiltInCascade.eye.classFile())
        );
    }

    public static boolean isImage(Path p) {
        String s = p.getFileName().toString().toLowerCase(Locale.ROOT);
        return s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".png") || s.endsWith(".bmp");
    }

    public List<FaceQuality> processAll() throws IOException, ExecutionException, InterruptedException {
        Path inDir = props.getInputDir();
        Path outDir = props.getOutputDir();
        if (inDir == null || outDir == null) {
            throw new IllegalArgumentException("配置缺失：face.inputDir / face.outputDir");
        }
        Files.createDirectories(outDir);
        Path cropsDir = outDir.resolve("crops");
        Files.createDirectories(cropsDir);
        Path reportCsv = outDir.resolve("report.csv");

        List<Path> images = Files.walk(inDir)
                .filter(p -> !Files.isDirectory(p))
                .filter(FaceCropService::isImage)
                .sorted()
                .collect(Collectors.toList());

        ExecutorService pool = Executors.newFixedThreadPool(props.getThreads());
        List<Future<List<FaceQuality>>> futures = new ArrayList<>();

        for (Path img : images) {
            futures.add(pool.submit(() -> processOneImage(img, cropsDir, detectorTL.get(), props)));
        }

        List<FaceQuality> all = new ArrayList<>();
        try (BufferedWriter bw = Files.newBufferedWriter(reportCsv)) {
            bw.write(FaceQuality.csvHeader()); bw.newLine();
            for (Future<List<FaceQuality>> f : futures) {
                List<FaceQuality> rows = f.get();
                for (FaceQuality r : rows) {
                    bw.write(r.toCsvLine()); bw.newLine();
                    all.add(r);
                }
            }
        } finally {
            pool.shutdown();
        }
        return all;
    }

    /**
     * Public single-image crop method for integration.
     * @param sourcePath original image absolute path
     * @param targetPath destination image absolute path (parent must be writable)
     * @return standardized response including status and output info
     */
    public FaceCropResponse cropSingle(String sourcePath, String targetPath) {
        long start = System.currentTimeMillis();
        try {
            if (sourcePath == null || sourcePath.isBlank()) {
                return FaceCropResponse.error(FaceCropStatus.SRC_NOT_FOUND, "SRC_NOT_FOUND", "sourcePath is empty");
            }
            Path src = Paths.get(sourcePath);
            if (!Files.exists(src) || !Files.isRegularFile(src)) {
                return FaceCropResponse.error(FaceCropStatus.SRC_NOT_FOUND, "SRC_NOT_FOUND", "source file not found");
            }
            if (!isImage(src)) {
                return FaceCropResponse.error(FaceCropStatus.INVALID_FORMAT, "INVALID_FORMAT", "unsupported image format");
            }

            Path dst = Paths.get(targetPath);
            Path parent = dst.getParent();
            if (parent != null) Files.createDirectories(parent);

            MBFImage color = ImageUtilities.readMBF(src.toFile());
            FImage gray = Transforms.calculateIntensity(color);

            // detect faces
            var detector = detectorTL.get();
            List<DetectedFace> faces = detector.detectFaces(gray);
            if (faces == null || faces.isEmpty()) {
                return FaceCropResponse.error(FaceCropStatus.NO_FACE_DETECTED, "NO_FACE", "no face detected");
            }
            faces.sort(Comparator.comparingDouble(f -> -f.getBounds().calculateArea()));
            DetectedFace f = faces.get(0);
            Rectangle r = clamp(color.getWidth(), color.getHeight(), f.getBounds(), props.getQuality().getMarginRatio());

            MBFImage cropColor = color.extractROI((int) r.x, (int) r.y, (int) r.width, (int) r.height);
            FImage cropGray = Transforms.calculateIntensity(cropColor);
            boolean eyesOk = verifyByEyes(cropGray);
            if (!eyesOk) {
                return FaceCropResponse.error(FaceCropStatus.EYE_VERIFY_FAILED, "EYE_VERIFY_FAILED", "eye verification failed");
            }

            // 根据目标路径后缀选择 PNG/JPG 写出，并在模块内完成压缩
            writeToTarget(cropColor, dst);
            long dur = System.currentTimeMillis() - start;
            return FaceCropResponse.ok(dst.toString(), dur);
        } catch (Exception e) {
            return FaceCropResponse.error(FaceCropStatus.PROCESSING_ERROR, "PROCESSING_ERROR", e.getMessage());
        }
    }

    // FaceCropService.java —— 用这个方法覆盖原有的 processOneImage
    private  List<FaceQuality> processOneImage(
            Path imgPath, Path cropsDir,
            FaceDetector<DetectedFace, FImage> detector,
            FaceProcessingProperties props) {

        List<FaceQuality> rows = new ArrayList<>();
        String base = stripExt(imgPath.getFileName().toString());

        try {
            MBFImage color = ImageUtilities.readMBF(imgPath.toFile());
            FImage gray = Transforms.calculateIntensity(color);

            // 1) 仅做人脸检测
            List<DetectedFace> faces = detector.detectFaces(gray);
            if (faces == null || faces.isEmpty()) {
                FaceQuality r = new FaceQuality();
                r.imageName = imgPath.getFileName().toString();
                r.faceIndex = -1;
                r.imgW = color.getWidth();
                r.imgH = color.getHeight();
                r.accepted = false;
                r.failedReasons.add("no_face_detected");
                rows.add(r);
                return rows;
            }

            // 2) 排序 & 只保留最大/最多N个
            faces.sort(Comparator.comparingDouble(f -> -f.getBounds().calculateArea()));
            if (props.isOnlyKeepLargestFace()) {
                faces = faces.subList(0, 1);
            } else {
                int maxN = Math.max(1, props.getMaxFacesPerImage());
                if (faces.size() > maxN) faces = faces.subList(0, maxN);
            }

            // 3) 对每个候选做人眼校验，只有通过者才裁剪输出
            int idx = 0;
            for (DetectedFace f : faces) {
                Rectangle r = clamp(color.getWidth(), color.getHeight(),
                        f.getBounds(), props.getQuality().getMarginRatio());

                FaceQuality q = new FaceQuality();
                q.imageName = imgPath.getFileName().toString();
                q.faceIndex = idx;
                q.imgW = color.getWidth(); q.imgH = color.getHeight();
                q.x = (int) r.x; q.y = (int) r.y; q.w = (int) r.width; q.h = (int) r.height;
                q.faceWidthRatio = q.w / (float) q.imgW;

                MBFImage cropColor = color.extractROI(q.x, q.y, q.w, q.h);
                FImage cropGray = Transforms.calculateIntensity(cropColor);

                // ==== 关键：眼睛校验 ====
                boolean eyesOk = verifyByEyes(cropGray);
                if (!eyesOk) {
                    q.accepted = false;
                    q.failedReasons.add("eye_verify_failed");
                    rows.add(q);
                    idx++;
                    continue; // 不输出这张
                }

                // 通过：直接裁剪输出
                String outName = base + String.format("_face_%02d.png", idx);
                // 离线批处理也根据后缀选择格式并压缩
                writeToTarget(cropColor, cropsDir.resolve(outName));
                q.accepted = true;
                rows.add(q);
                idx++;
            }

        } catch (Exception e) {
            FaceQuality r = new FaceQuality();
            r.imageName = imgPath.getFileName().toString();
            r.faceIndex = -2;
            r.accepted = false; r.failedReasons.add("error:" + e.getClass().getSimpleName());
            rows.add(r);
        }
        return rows;
    }

    // 在候选 ROI 内跑一次“眼睛”级联，要求至少两只眼，且几何关系合理
    private boolean verifyByEyes(FImage faceGray) {
        FaceDetector<DetectedFace, FImage> eyeDet = eyeDetectorTL.get();
        List<DetectedFace> eyes = eyeDet.detectFaces(faceGray);
        if (eyes == null || eyes.size() < 2) return false;

        // 选前两只最大的“眼睛框”
        eyes.sort(Comparator.comparingDouble(e -> -e.getBounds().calculateArea()));
        Rectangle e1 = eyes.get(0).getBounds();
        Rectangle e2 = eyes.get(1).getBounds();

        // 计算几何约束（全部是经验阈值，可按需要微调）
        float w = faceGray.getWidth();
        float h = faceGray.getHeight();

        // 眼睛中心
        float e1x = (float)(e1.x + e1.width/2.0), e1y = (float)(e1.y + e1.height/2.0);
        float e2x = (float)(e2.x + e2.width/2.0), e2y = (float)(e2.y + e2.height/2.0);

        // 1) 两眼水平位置应在上半张脸
        if (Math.max(e1y, e2y) > 0.65f * h) return false;

        // 2) 两眼纵向差不要太大
        float dy = Math.abs(e1y - e2y) / h;
        if (dy > 0.15f) return false;

        // 3) 两眼间距（按脸宽归一化）在合理区间
        float dx = Math.abs(e1x - e2x) / w;
        if (dx < 0.25f || dx > 0.8f) return false;

        // 4) 每只“眼睛框”不应太小
        float minEyeW = 0.04f * w;
        if (e1.width < minEyeW || e2.width < minEyeW) return false;

        // 5) 候选脸的宽高比也做个粗筛（避免极端扁/窄的误检）
        float ar = w / h;
        if (ar < 0.7f || ar > 1.7f) return false;

        return true;
    }




    private static void evalQuality(FImage g, FaceQuality q, FaceProcessingProperties props) {
        FaceProcessingProperties.Quality c = props.getQuality();
        final float UNDER_T = 0.05f, OVER_T = 0.95f;

        int w = g.getWidth(), h = g.getHeight();
        int total = w * h;

        double sum = 0, sum2 = 0; int under = 0, over = 0;
        for (int y = 0; y < h; y++) {
            float[] row = g.pixels[y];
            for (int x = 0; x < w; x++) {
                float v = clamp01(row[x]);
                sum += v; sum2 += v * v;
                if (v <= UNDER_T) under++;
                if (v >= OVER_T) over++;
            }
        }
        q.mean = (float)(sum / total);
        q.underRatio = under / (float) total;
        q.overRatio  = over  / (float) total;

        double var = (sum2 / total) - q.mean * q.mean;
        q.std = (float)Math.sqrt(Math.max(0, var));

        q.varLap = varianceOfLaplacian(g);
        q.symDiff = mirrorDifference(g);
        q.illumDiff = leftRightMeanDiff(g);

        if (q.mean < c.getMinMean() || q.mean > c.getMaxMean()) q.failedReasons.add("mean_out_of_range");
        if (q.underRatio > c.getMaxUnderExpRatio())            q.failedReasons.add("under_exposed");
        if (q.overRatio  > c.getMaxOverExpRatio())             q.failedReasons.add("over_exposed");
        if (q.std < c.getMinStd())                             q.failedReasons.add("low_contrast");
        if (q.varLap < c.getMinVarLap())                       q.failedReasons.add("blurry");
        if (q.symDiff > c.getMaxSymDiff())                     q.failedReasons.add("asymmetry/side_pose");
        if (q.illumDiff > c.getMaxIllumDiff())                 q.failedReasons.add("illumination_unbalanced");
    }

    private static double varianceOfLaplacian(FImage g) {
        int w = g.getWidth(), h = g.getHeight();
        double sum = 0, sum2 = 0; int count = 0;
        for (int y = 1; y < h - 1; y++) {
            float[] r0 = g.pixels[y - 1];
            float[] r1 = g.pixels[y];
            float[] r2 = g.pixels[y + 1];
            for (int x = 1; x < w - 1; x++) {
                double lap = (r0[x] + r2[x] + r1[x - 1] + r1[x + 1]) - 4.0 * r1[x];
                sum += lap; sum2 += lap * lap; count++;
            }
        }
        if (count == 0) return 0;
        double mean = sum / count;
        return (sum2 / count) - mean * mean;
    }

    private static float mirrorDifference(FImage g) {
        int w = g.getWidth(), h = g.getHeight();
        int half = w / 2;
        double acc = 0; int cnt = 0;
        for (int y = 0; y < h; y++) {
            float[] row = g.pixels[y];
            for (int x = 0; x < half; x++) {
                float left = clamp01(row[x]);
                float right = clamp01(row[w - 1 - x]);
                acc += Math.abs(left - right); cnt++;
            }
        }
        return (cnt == 0) ? 1f : (float)(acc / cnt);
    }

    private static float leftRightMeanDiff(FImage g) {
        int w = g.getWidth(), h = g.getHeight();
        int half = w / 2;
        double lsum = 0, rsum = 0; int lcnt = 0, rcnt = 0;
        for (int y = 0; y < h; y++) {
            float[] row = g.pixels[y];
            for (int x = 0; x < half; x++) { lsum += clamp01(row[x]); lcnt++; }
            for (int x = half; x < w; x++) { rsum += clamp01(row[x]); rcnt++; }
        }
        float lmean = lcnt == 0 ? 0 : (float)(lsum / lcnt);
        float rmean = rcnt == 0 ? 0 : (float)(rsum / rcnt);
        return Math.abs(lmean - rmean);
    }

    private static Rectangle clamp(int imgW, int imgH, Rectangle r, float marginRatio) {
        float x = Math.max(0, r.x);
        float y = Math.max(0, r.y);
        float w = Math.min(imgW - x, r.width);
        float h = Math.min(imgH - y, r.height);

        float m = Math.max(w, h) * marginRatio;
        float nx = Math.max(0, x - m);
        float ny = Math.max(0, y - m);
        float nw = Math.min(imgW - nx, w + 2 * m);
        float nh = Math.min(imgH - ny, h + 2 * m);
        return new Rectangle(nx, ny, nw, nh);
    }

    private static float clamp01(float v){ return Math.max(0f, Math.min(1f, v)); }
    private static String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(0, i) : name;
    }

    private static void writeToTarget(MBFImage img, Path dst) throws IOException {
        String lower = dst.getFileName().toString().toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            writeCompressedJpeg(img, dst);
        } else {
            writeCompressedPng(img, dst);
        }
    }

    /**
     * 将 MBFImage 写为压缩后的 PNG：
     * 1) 限制最长边不超过 MAX_SIDE 像素以控制像素数量；
     * 2) 转为 8-bit 索引调色板（RGB332）以显著降低文件体积；
     * 说明：保持 PNG 扩展名与调用方兼容，常见人脸区域在 256~320 像素下配合索引色可降至 ~50–120KB。
     */
    private static void writeCompressedPng(MBFImage img, Path dst) throws IOException {
        final int MAX_SIDE = 320; // 初始最长边限制
        final int TARGET_MAX_KB = 70; // 目标上限，尽量不超过 70KB

        // 转为可显示的 BufferedImage（RGB，无透明）
        BufferedImage src = ImageUtilities.createBufferedImageForDisplay(img, null);

        // 步骤1：按最长边限制尺寸
        int sw = src.getWidth();
        int sh = src.getHeight();
        int maxSide = Math.max(sw, sh);
        BufferedImage scaled = src;
        if (maxSide > MAX_SIDE) {
            double scale = MAX_SIDE / (double) maxSide;
            int tw = Math.max(1, (int) Math.round(sw * scale));
            int th = Math.max(1, (int) Math.round(sh * scale));
            BufferedImage tmp = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = tmp.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, tw, th, null);
            g.dispose();
            scaled = tmp;
        }

        // 步骤2：转为 8-bit 索引调色板（RGB332），256 色
        int w = scaled.getWidth();
        int h = scaled.getHeight();
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        // 构建 RGB332 调色板：R/G 各 8 级，B 4 级
        for (int ri = 0; ri < 8; ri++) {
            int rv = (int) Math.round(ri * 255.0 / 7.0);
            for (int gi = 0; gi < 8; gi++) {
                int gv = (int) Math.round(gi * 255.0 / 7.0);
                for (int bi = 0; bi < 4; bi++) {
                    int bv = (int) Math.round(bi * 255.0 / 3.0);
                    int idx = (ri << 5) | (gi << 2) | bi;
                    r[idx] = (byte) rv;
                    g[idx] = (byte) gv;
                    b[idx] = (byte) bv;
                }
            }
        }
        IndexColorModel icm = new IndexColorModel(8, 256, r, g, b);

        // 写 PNG（索引色），如超出目标大小则逐步缩小再试
        double scaleFactor = 1.0;
        byte[] best = null;
        for (int tries = 0; tries < 6; tries++) {
            int tw = (int) Math.max(1, Math.round(w * scaleFactor));
            int th = (int) Math.max(1, Math.round(h * scaleFactor));
            BufferedImage working = scaled;
            if (tw != w || th != h) {
                BufferedImage tmp = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(scaled, 0, 0, tw, th, null);
                g2.dispose();
                working = tmp;
            }

            BufferedImage indexed = new BufferedImage(working.getWidth(), working.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, icm);
            int ww = working.getWidth();
            int hh = working.getHeight();
            int[] line = new int[ww];
            for (int y = 0; y < hh; y++) {
                working.getRGB(0, y, ww, 1, line, 0, ww);
                for (int x = 0; x < ww; x++) {
                    int argb = line[x];
                    int rr = (argb >> 16) & 0xFF;
                    int gg = (argb >> 8) & 0xFF;
                    int bb = (argb) & 0xFF;
                    int ri = (rr * 7) / 255; // 0..7
                    int gi = (gg * 7) / 255; // 0..7
                    int bi = (bb * 3) / 255; // 0..3
                    int idx = (ri << 5) | (gi << 2) | bi; // 0..255
                    indexed.getRaster().setSample(x, y, 0, idx);
                }
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream(64 * 1024);
            ImageIO.write(indexed, "png", baos);
            byte[] out = baos.toByteArray();
            best = out;
            if (out.length <= TARGET_MAX_KB * 1024) break;
            // 继续缩小尺寸
            scaleFactor *= 0.85;
            if (scaleFactor < 0.30) break;
        }

        Files.createDirectories(dst.getParent());
        java.nio.file.Files.write(dst, best);
    }

    /**
     * 将 MBFImage 写为压缩后的 JPG（有损压缩）：
     * 1) 限制最长边不超过 MAX_SIDE；
     * 2) 优先尝试质量 0.75，如超出目标大小则降低质量与适度缩小尺寸；
     */
    private static void writeCompressedJpeg(MBFImage img, Path dst) throws IOException {
        final int MAX_SIDE = 320;
        final int TARGET_MAX_KB = 70;

        BufferedImage src = ImageUtilities.createBufferedImageForDisplay(img, null);
        int sw = src.getWidth();
        int sh = src.getHeight();
        int maxSide = Math.max(sw, sh);

        BufferedImage base = src;
        if (maxSide > MAX_SIDE) {
            double scale = MAX_SIDE / (double) maxSide;
            int tw = Math.max(1, (int) Math.round(sw * scale));
            int th = Math.max(1, (int) Math.round(sh * scale));
            BufferedImage tmp = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = tmp.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, tw, th, null);
            g.dispose();
            base = tmp;
        }

        double[] qualities = new double[] {0.75, 0.65, 0.55, 0.45, 0.35};
        double scaleFactor = 1.0;
        byte[] best = null;
        for (int tries = 0; tries < 6; tries++) {
            int tw = (int) Math.max(1, Math.round(base.getWidth() * scaleFactor));
            int th = (int) Math.max(1, Math.round(base.getHeight() * scaleFactor));
            BufferedImage working = base;
            if (tw != base.getWidth() || th != base.getHeight()) {
                BufferedImage tmp = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(base, 0, 0, tw, th, null);
                g2.dispose();
                working = tmp;
            }

            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            java.io.ByteArrayOutputStream baosBest = null;
            for (double q : qualities) {
                param.setCompressionQuality((float) q);
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream(64 * 1024);
                javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                writer.setOutput(ios);
                writer.write(null, new IIOImage(working, null, null), param);
                ios.close();
                byte[] out = baos.toByteArray();
                baosBest = baos;
                best = out;
                if (out.length <= TARGET_MAX_KB * 1024) break;
            }
            writer.dispose();
            if (best != null && best.length <= TARGET_MAX_KB * 1024) break;
            scaleFactor *= 0.85;
            if (scaleFactor < 0.30) break;
        }

        Files.createDirectories(dst.getParent());
        java.nio.file.Files.write(dst, best);
    }
}
