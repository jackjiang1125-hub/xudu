package org.jeecg.modules.iot.device.util;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 人脸检测和抠图工具类（方案B：纯静态工具类，不交由 Spring 管理）
 * 使用 OpenCV 进行人脸检测和提取
 *
 * 依赖：
 *   <dependency>
 *     <groupId>org.openpnp</groupId>
 *     <artifactId>opencv</artifactId>
 *     <version>4.5.5-1</version>
 *   </dependency>
 *
 * 资源：请将 haarcascade_frontalface_alt.xml（或 default 版）放到 src/main/resources 根目录。
 */
@Slf4j
public final class FaceExtractionUtil {

    /** 共享的人脸检测器（类加载时初始化）。 */
    private static final CascadeClassifier FACE_DETECTOR;

    static {
        // 1) 加载 OpenCV 原生库（仅一次）
        OpenCvLoader.loadOnce();

        // 2) 从 resources 复制模型到临时文件再加载（兼容 fat-jar）
        FACE_DETECTOR = new CascadeClassifier();
        boolean loaded = false;
        String[] candidates = new String[]{
                "/haarcascade_frontalface_alt.xml",
                "/haarcascade_frontalface_default.xml"
        };
        for (String path : candidates) {
            try (InputStream is = FaceExtractionUtil.class.getResourceAsStream(path)) {
                if (is == null) continue;
                var tmp = Files.createTempFile("haarcascade_", ".xml");
                Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
                loaded = FACE_DETECTOR.load(tmp.toString());
                if (loaded) {
                    log.info("Haar 模型加载成功: {} -> {}", path, tmp);
                    break;
                }
            } catch (Exception e) {
                log.warn("加载 Haar 模型异常 [{}]: {}", path, e.toString());
            }
        }
        if (!loaded) {
            throw new ExceptionInInitializerError("无法从资源加载 Haar 模型: " + Arrays.toString(candidates));
        }
        log.info("OpenCV 初始化完成，version={}", Core.getVersionString());
    }

    private FaceExtractionUtil() {}

    /**
     * 从图片中检测人脸
     * @param imageBytes 图片字节数组
     * @return 人脸区域列表
     */
    public static List<Rect> detectFaces(byte[] imageBytes) {
        List<Rect> faces = new ArrayList<>();
        if (imageBytes == null || imageBytes.length == 0) {
            log.error("imageBytes 为空");
            return faces;
        }

        // 兜底（幂等）
        OpenCvLoader.loadOnce();

        MatOfByte mob = null;
        Mat image = null;
        Mat grayImage = new Mat();
        MatOfRect faceDetections = new MatOfRect();
        try {
            mob = new MatOfByte(imageBytes);
            image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
            if (image == null || image.empty()) {
                log.error("无法解码图片");
                return faces;
            }

            if (FACE_DETECTOR == null || FACE_DETECTOR.empty()) {
                log.warn("人脸检测器未初始化，使用备用检测方法");
                faces = detectFacesFallback(image);
                return faces;
            }

            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayImage, grayImage);

            double scaleFactor = 1.1;  // 金字塔缩放比例
            int minNeighbors = 3;      // 邻域阈值
            int flags = 0;             // 常置 0
            Size minSize = new Size(30, 30);
            Size maxSize = new Size();

            synchronized (FACE_DETECTOR) { // 保险起见，避免多线程同时访问
                FACE_DETECTOR.detectMultiScale(grayImage, faceDetections,
                        scaleFactor, minNeighbors, flags, minSize, maxSize);
            }

            for (Rect face : faceDetections.toArray()) {
                faces.add(face);
                log.info("检测到人脸: x={}, y={}, w={}, h={}", face.x, face.y, face.width, face.height);
            }
            return faces;
        } catch (Exception e) {
            log.error("人脸检测失败", e);
            return faces;
        } finally {
            if (mob != null) mob.release();
            if (image != null) image.release();
            grayImage.release();
            faceDetections.release();
        }
    }

    /**
     * 备用人脸检测方法 - 基于图片中心区域检测
     * @param image 图片Mat对象
     * @return 人脸区域列表
     */
    private static List<Rect> detectFacesFallback(Mat image) {
        List<Rect> faces = new ArrayList<>();
        try {
            int width = image.width();
            int height = image.height();

            int faceWidth = Math.min(width, height) / 3;
            int faceHeight = faceWidth;

            int centerX = width / 2;
            int centerY = height / 2;
            int faceX = centerX - faceWidth / 2;
            int faceY = centerY - faceHeight / 2;

            faceX = Math.max(0, Math.min(faceX, width - faceWidth));
            faceY = Math.max(0, Math.min(faceY, height - faceHeight));

            Rect faceRect = new Rect(faceX, faceY, faceWidth, faceHeight);
            faces.add(faceRect);
            log.info("使用备用方法检测人脸: x={}, y={}, w={}, h={}", faceRect.x, faceRect.y, faceRect.width, faceRect.height);
        } catch (Exception e) {
            log.error("备用人脸检测失败", e);
        }
        return faces;
    }

    /**
     * 提取人脸区域并生成新图片（JPG）
     * @param imageBytes 原始图片字节数组
     * @param faceRect 人脸区域
     * @param padding 扩展边距（像素）
     * @return 抠出的人脸图片字节数组
     */
    public static byte[] extractFace(byte[] imageBytes, Rect faceRect, int padding) {
        if (imageBytes == null || faceRect == null) return null;
        OpenCvLoader.loadOnce();

        MatOfByte mob = null;
        Mat image = null;
        Mat faceImage = null;
        MatOfByte out = new MatOfByte();
        try {
            mob = new MatOfByte(imageBytes);
            image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
            if (image == null || image.empty()) {
                log.error("无法解码图片");
                return null;
            }

            int x = Math.max(0, faceRect.x - padding);
            int y = Math.max(0, faceRect.y - padding);
            int width = Math.min(image.width() - x, faceRect.width + 2 * padding);
            int height = Math.min(image.height() - y, faceRect.height + 2 * padding);

            if (width <= 0 || height <= 0) {
                log.error("无效的人脸区域");
                return null;
            }

            Rect expandedRect = new Rect(x, y, width, height);
            faceImage = new Mat(image, expandedRect);

            Imgcodecs.imencode(".jpg", faceImage, out);
            byte[] faceBytes = out.toArray();

            log.info("成功提取人脸: 原始={}x{}, 人脸={}x{}, 扩展后={}x{}",
                    image.width(), image.height(),
                    faceRect.width, faceRect.height,
                    width, height);
            return faceBytes;
        } catch (Exception e) {
            log.error("人脸提取失败", e);
            return null;
        } finally {
            if (mob != null) mob.release();
            if (image != null) image.release();
            if (faceImage != null) faceImage.release();
            out.release();
        }
    }

    /**
     * 从图片中提取最大的人脸（JPG）
     */
    public static byte[] extractLargestFace(byte[] imageBytes, int padding) {
        List<Rect> faces = detectFaces(imageBytes);
        if (faces.isEmpty()) {
            log.warn("未检测到人脸");
            return null;
        }
        Rect largestFace = faces.get(0);
        for (Rect face : faces) {
            if (face.area() > largestFace.area()) {
                largestFace = face;
            }
        }
        log.info("选择最大人脸进行提取: 面积={}", largestFace.area());
        return extractFace(imageBytes, largestFace, padding);
    }

    /**
     * 将图片字节数组转换为 Base64（data URL）
     */
    public static String imageToBase64(byte[] imageBytes) {
        if (imageBytes == null) return null;
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    public static String imageToFile(byte[] imageBytes){
        OutputStream os = null;
        try {
            os = new FileOutputStream("d://lz.jpg");
            os.write(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "d://lz.jpg";
    }

    /**
     * 将 Base64 转回图片字节数组
     */
    public static byte[] base64ToImage(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;
        try {
            if (base64String.contains(",")) {
                base64String = base64String.split(",", 2)[1];
            }
            return Base64.getDecoder().decode(base64String);
        } catch (Exception e) {
            log.error("Base64 解码失败", e);
            return null;
        }
    }

    /**
     * 检测图片中的人脸数量
     */
    public static int getFaceCount(byte[] imageBytes) {
        return detectFaces(imageBytes).size();
    }

    /**
     * 获取人脸检测结果详情
     */
    public static FaceDetectionResult getFaceDetectionResult(byte[] imageBytes) {
        List<Rect> faces = detectFaces(imageBytes);
        FaceDetectionResult result = new FaceDetectionResult();
        result.setFaceCount(faces.size());
        result.setFaces(faces);
        if (!faces.isEmpty()) {
            Rect largestFace = faces.get(0);
            for (Rect face : faces) {
                if (face.area() > largestFace.area()) largestFace = face;
            }
            result.setLargestFace(largestFace);
        }
        return result;
    }

    /** 人脸检测结果类 */
    public static class FaceDetectionResult {
        private int faceCount;
        private List<Rect> faces;
        private Rect largestFace;

        public int getFaceCount() { return faceCount; }
        public void setFaceCount(int faceCount) { this.faceCount = faceCount; }

        public List<Rect> getFaces() { return faces; }
        public void setFaces(List<Rect> faces) { this.faces = faces; }

        public Rect getLargestFace() { return largestFace; }
        public void setLargestFace(Rect largestFace) { this.largestFace = largestFace; }
    }
}

/**
 * 仅负责一次性加载 OpenCV 原生库（与 Spring 生命周期无关）。
 * 与 FaceExtractionUtil 放在同一源文件内，但不是 public 类（Java 允许）。
 */
final class OpenCvLoader {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OpenCvLoader.class);
    private static final AtomicBoolean LOADED = new AtomicBoolean(false);
    private OpenCvLoader() {}

    static void loadOnce() {
        if (LOADED.get()) return;
        synchronized (OpenCvLoader.class) {
            if (LOADED.get()) return;
            try {
                // openpnp 的加载器：会解压并加载平台对应的本地库
                nu.pattern.OpenCV.loadShared();
            } catch (Throwable t) {
                log.warn("OpenCV.loadShared() 失败，尝试 loadLocally(): {}", t.toString());
                try {
                    nu.pattern.OpenCV.loadLocally();
                } catch (Throwable t2) {
                    throw new IllegalStateException("加载 OpenCV 原生库失败", t2);
                }
            }
            try {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            } catch (UnsatisfiedLinkError e) {
                log.debug("System.loadLibrary 可忽略: {}", e.toString());
            }
            LOADED.set(true);
            log.info("OpenCV native 已加载");
        }
    }
}

