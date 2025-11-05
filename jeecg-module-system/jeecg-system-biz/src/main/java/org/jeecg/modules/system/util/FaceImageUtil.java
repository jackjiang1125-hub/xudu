package org.jeecg.modules.system.util;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * 图片路径解析与压缩处理工具
 * - 解析 avatar（data:image、http/https、本地路径）为本地临时文件
 * - 将图片压缩到目标范围（默认 30-70KB）以降低抠图开销
 * - 构建抠图输出目标路径（物理路径与DB相对路径）
 */
public final class FaceImageUtil {

    private FaceImageUtil() {}

    private static final Logger log = LoggerFactory.getLogger(FaceImageUtil.class);

    /**
     * 解析上传根路径（保持与配置一致，不做盘符猜测）
     * - 返回原值或去除首尾空白；后续由 Paths.get(uploadRoot, ...) 负责拼接与创建目录
     * - 若配置为 Unix 风格（如 "/opt/upFiles"）且运行在 Windows，建议在配置层改为绝对盘符路径
     */
    public static String resolveUploadRoot(String configured) {
        if (configured == null) return null;
        String root = configured.trim();
        return root.isEmpty() ? configured : root;
    }

    public static class FaceTargetPath {
        public final String physicalPath;
        public final String dbPath;
        public FaceTargetPath(String physicalPath, String dbPath) {
            this.physicalPath = physicalPath;
            this.dbPath = dbPath;
        }
    }

    /**
     * 解析并压缩 avatar 得到本地临时文件路径
     * @param avatar 原始头像字符串（data:image/…，http/https URL，或本地路径）
     * @param uploadRoot 上传根路径（如 jeecg.path.upload）
     * @param maxInputBytes 输入图片最大字节（防护，建议 5MB）
     * @param minKB 压缩目标下限（KB）
     * @param maxKB 压缩目标上限（KB）
     * @return 本地临时文件路径（jpg），解析失败返回 null
     */
    public static String resolveAvatarToCompressedTempPath(String avatar, String uploadRoot, long maxInputBytes, int minKB, int maxKB) {
        try {
            log.info("[FaceImageUtil] resolveAvatarToCompressedTempPath start, target=[{}KB,{}KB], uploadRoot={}", minKB, maxKB, uploadRoot);
            if (avatar == null || avatar.isBlank()) return null;

            byte[] raw;
            String ext = "jpg"; // 输出统一为 jpg 以获得较好压缩比

            if (avatar.startsWith("data:image")) {
                int idx = avatar.indexOf(',');
                if (idx < 0) return null;
                String base64 = avatar.substring(idx + 1);
                raw = Base64.getDecoder().decode(base64);
                if (raw.length > maxInputBytes) return null;
                log.info("[FaceImageUtil] input type=dataURI, size={}KB", (int)Math.ceil(raw.length / 1024.0));
            } else if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                long total = 0;
                try (InputStream is = new URL(avatar).openStream()) {
                    int read;
                    while ((read = is.read(buf)) != -1) {
                        total += read;
                        if (total > maxInputBytes) return null;
                        baos.write(buf, 0, read);
                    }
                }
                raw = baos.toByteArray();
                log.info("[FaceImageUtil] input type=url, size={}KB, url={}", (int)Math.ceil(raw.length / 1024.0), avatar);
            } else {
                Path p = Paths.get(avatar);
                Path local = p.isAbsolute() ? p : Paths.get(uploadRoot, avatar);
                if (!Files.exists(local) || !Files.isRegularFile(local)) return null;
                long size = Files.size(local);
                if (size > maxInputBytes) return null;
                raw = Files.readAllBytes(local);
                log.info("[FaceImageUtil] input type=file, size={}KB, path={}", (int)Math.ceil(raw.length / 1024.0), local.toString());
            }

            byte[] compressed = compressToRange(raw, minKB, maxKB);
            String tmpDir = Paths.get(uploadRoot, "tmp", "facecrop").toString();
            Files.createDirectories(Paths.get(tmpDir));
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path out = Paths.get(tmpDir, fileName);
            Files.write(out, compressed);
            log.info("[FaceImageUtil] compressed result: {}KB -> {}KB, out={}", (int)Math.ceil(raw.length / 1024.0), (int)Math.ceil(compressed.length / 1024.0), out.toString());
            return out.toString();
        } catch (Throwable e) {
            log.warn("[FaceImageUtil] resolveAvatarToCompressedTempPath error: {}", e.toString());
            return null;
        }
    }

    /**
     * 构建抠图输出目标路径（物理路径 + DB相对路径）
     */
    public static FaceTargetPath buildFacecropTargetPath(String uploadRoot, String username) {
        String ym = new SimpleDateFormat("yyyyMM").format(new Date());
        String rid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String safeUser = (username == null || username.isBlank()) ? "user" : username.replaceAll("[^a-zA-Z0-9_-]", "_");
        String fileName = safeUser + "_" + rid + ".png"; // 裁剪结果统一存 png
        String relative = Paths.get("user", "face_crop", ym, fileName).toString().replace('\\', '/');
        String physical = Paths.get(uploadRoot, relative).toString();
        Path parent = Paths.get(physical).getParent();
        try { if (parent != null) Files.createDirectories(parent); } catch (IOException ignored) {}
        return new FaceTargetPath(physical, relative);
    }

    /**
     * 将数据库中保存的相对路径转换为本地物理路径（若已是绝对路径则直接返回）
     */
    public static String resolveDbPathToPhysical(String uploadRoot, String dbPath) {
        if (dbPath == null || dbPath.isBlank()) return null;
        Path p = Paths.get(dbPath);
        if (p.isAbsolute()) return p.toString();
        return Paths.get(uploadRoot, dbPath).toString();
    }

    /**
     * 解析 avatar 到本地物理路径（不做压缩），支持绝对路径或基于 uploadRoot 的相对路径
     * 返回存在的本地文件路径，不存在或异常时返回 null
     */
    public static String resolveAvatarToLocalPath(String uploadRoot, String avatar) {
        try {
            if (avatar == null || avatar.isBlank()) {
                return null;
            }
            Path p = Paths.get(avatar);
            Path local = p.isAbsolute() ? p : Paths.get(uploadRoot, avatar);
            if (Files.exists(local) && Files.isRegularFile(local)) {
                return local.toString();
            }
            log.warn("[FaceImageUtil] resolveAvatarToLocalPath not found: avatar={}, resolved={}", avatar, local.toString());
            return null;
        } catch (Exception e) {
            log.warn("[FaceImageUtil] resolveAvatarToLocalPath error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 构建抠图输出路径（与 buildFacecropTargetPath 相同），但文件后缀统一改为 .jpg
     */
    public static FaceTargetPath buildFacecropTargetPathAsJpg(String uploadRoot, String username) {
        FaceTargetPath tp = buildFacecropTargetPath(uploadRoot, username);
        String phys = tp.physicalPath.replaceFirst("(?i)\\.png$", ".jpg");
        String db = tp.dbPath.replaceFirst("(?i)\\.png$", ".jpg");
        return new FaceTargetPath(phys, db);
    }

    /**
     * 压缩头像并保存到正式目录，返回DB相对路径
     * @param avatar 原始头像字符串（data:image/…，http/https URL，或本地路径）
     * @param uploadRoot 上传根路径（如 jeecg.path.upload）
     * @param username 用户名（用于构建文件名）
     * @param maxInputBytes 输入图片最大字节（防护，建议 5MB）
     * @param minKB 压缩目标下限（KB）
     * @param maxKB 压缩目标上限（KB）
     * @return DB相对路径，失败返回 null
     */
    public static String compressAndSaveAvatar(String avatar, String uploadRoot, String username, long maxInputBytes, int minKB, int maxKB) {
        try {
            log.info("[FaceImageUtil] compressAndSaveAvatar start, user={}, target=[{}KB,{}KB]", username, minKB, maxKB);
            if (avatar == null || avatar.isBlank()) return null;

            byte[] raw;
            String ext = "jpg"; // 输出统一为 jpg 以获得较好压缩比

            if (avatar.startsWith("data:image")) {
                int idx = avatar.indexOf(',');
                if (idx < 0) return null;
                String base64 = avatar.substring(idx + 1);
                raw = Base64.getDecoder().decode(base64);
                if (raw.length > maxInputBytes) return null;
                log.info("[FaceImageUtil] input type=dataURI, size={}KB", (int)Math.ceil(raw.length / 1024.0));
            } else if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                long total = 0;
                try (InputStream is = new URL(avatar).openStream()) {
                    int read;
                    while ((read = is.read(buf)) != -1) {
                        total += read;
                        if (total > maxInputBytes) return null;
                        baos.write(buf, 0, read);
                    }
                }
                raw = baos.toByteArray();
                log.info("[FaceImageUtil] input type=url, size={}KB, url={}", (int)Math.ceil(raw.length / 1024.0), avatar);
            } else {
                Path p = Paths.get(avatar);
                Path local = p.isAbsolute() ? p : Paths.get(uploadRoot, avatar);
                if (!Files.exists(local) || !Files.isRegularFile(local)) return null;
                long size = Files.size(local);
                if (size > maxInputBytes) return null;
                raw = Files.readAllBytes(local);
                log.info("[FaceImageUtil] input type=file, size={}KB, path={}", (int)Math.ceil(raw.length / 1024.0), local.toString());
            }

            // 压缩图片
            byte[] compressed = compressToRange(raw, minKB, maxKB);
            
            // 构建保存路径（正式目录）
            String ym = new SimpleDateFormat("yyyyMM").format(new Date());
            String rid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String safeUser = (username == null || username.isBlank()) ? "user" : username.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = safeUser + "_avatar_" + rid + "." + ext;
            String relative = Paths.get("user", "avatar", ym, fileName).toString().replace('\\', '/');
            String physical = Paths.get(uploadRoot, relative).toString();
            
            // 确保目录存在
            Path parent = Paths.get(physical).getParent();
            if (parent != null) Files.createDirectories(parent);
            
            // 保存压缩后的图片
            Files.write(Paths.get(physical), compressed);
            
            log.info("[FaceImageUtil] avatar compressed and saved: {}KB -> {}KB, dbPath={}", 
                    (int)Math.ceil(raw.length / 1024.0), 
                    (int)Math.ceil(compressed.length / 1024.0), 
                    relative);
            
            return relative;
        } catch (Throwable e) {
            log.warn("[FaceImageUtil] compressAndSaveAvatar error: {}", e.toString());
            return null;
        }
    }

    /**
     * 将图片压缩到目标范围（KB）内，统一输出为 JPEG 格式
     * 新策略：对质量做二分搜索，必要时逐步缩放，优先保证不超过 maxKB
     */
    public static byte[] compressToRange(byte[] originalBytes, int minKB, int maxKB) throws Exception {
        if (originalBytes == null) throw new Exception("empty image");
        int min = Math.max(1, minKB) * 1024;
        // 修正：max 必须以字节比较，先将 maxKB 转换为字节再与 (min+1) 比较
        int max = Math.max(min + 1, maxKB * 1024);

        BufferedImage original = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (original == null) throw new Exception("invalid image");

        int ow = original.getWidth();
        int oh = original.getHeight();
        log.info("[FaceImageUtil] compressToRange start: input={}KB, target=[{}KB,{}KB], dimension={}x{}", (int)Math.ceil(originalBytes.length / 1024.0), minKB, maxKB, ow, oh);

        // 候选结果：优先返回 [min,max] 内，其次返回 <=max 的最优
        byte[] candidateInRange = null;
        byte[] candidateUnderMax = null;

        double scale = 1.0; // 初始按原图
        for (int s = 0; s < 8; s++) { // 最多 8 次缩放尝试
            // 对 quality 做二分搜索
            float qLow = 0.2f;
            float qHigh = 0.95f;
            for (int q = 0; q < 8; q++) { // 最多 8 次质量搜索
                float quality = (qLow + qHigh) / 2.0f;
                int w = Math.max(50, (int) (ow * scale));
                int h = Math.max(50, (int) (oh * scale));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Thumbnails.of(original)
                        .size(w, h)
                        .antialiasing(Antialiasing.ON)
                        .outputQuality(quality)
                        .outputFormat("jpg")
                        .toOutputStream(baos);
                byte[] out = baos.toByteArray();
                int sz = out.length;
                if (log.isDebugEnabled()) {
                    log.debug("[FaceImageUtil] try scale={} quality={} -> {}KB", String.format("%.3f", scale), String.format("%.3f", quality), (int)Math.ceil(sz / 1024.0));
                }

                if (sz > max) {
                    // 太大，降低质量
                    qHigh = quality;
                } else {
                    // 不超过 max，记录候选
                    candidateUnderMax = out;
                    qLow = quality;
                    if (sz >= min) {
                        candidateInRange = out;
                        log.info("[FaceImageUtil] pick in-range: {}KB (quality={} scale={})", (int)Math.ceil(sz / 1024.0), String.format("%.3f", quality), String.format("%.3f", scale));
                        return candidateInRange; // 已在目标区间
                    }
                }
            }
            // 质量搜索后仍没有进入区间，若存在不超过 max 的候选则直接返回
            if (candidateUnderMax != null) {
                log.info("[FaceImageUtil] pick under-max candidate: {}KB (scale={})", (int)Math.ceil(candidateUnderMax.length / 1024.0), String.format("%.3f", scale));
                return candidateUnderMax;
            }
            // 继续缩小尺寸再尝试
            scale *= 0.8; // 逐步缩小
            log.info("[FaceImageUtil] shrink and retry, new scale={}", String.format("%.3f", scale));
            if (scale < 0.15) break; // 防止过度缩小
        }

        // 仍未找到 <=max 的结果，进行一次更激进的压缩尝试，确保不超过 max
        double aggressiveScale = Math.min(scale, 0.5);
        float aggressiveQuality = 0.25f;
        byte[] last = originalBytes;
        log.warn("[FaceImageUtil] aggressive fallback start: scale={} quality={}", String.format("%.3f", aggressiveScale), String.format("%.3f", aggressiveQuality));
        for (int i = 0; i < 6; i++) {
            int w = Math.max(50, (int) (ow * aggressiveScale));
            int h = Math.max(50, (int) (oh * aggressiveScale));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(original)
                    .size(w, h)
                    .antialiasing(Antialiasing.ON)
                    .outputQuality(aggressiveQuality)
                    .outputFormat("jpg")
                    .toOutputStream(baos);
            byte[] out = baos.toByteArray();
            last = out;
            if (log.isDebugEnabled()) {
                log.debug("[FaceImageUtil] fallback try scale={} quality={} -> {}KB", String.format("%.3f", aggressiveScale), String.format("%.3f", aggressiveQuality), (int)Math.ceil(out.length / 1024.0));
            }
            if (out.length <= max) {
                log.warn("[FaceImageUtil] fallback success: {}KB", (int)Math.ceil(out.length / 1024.0));
                return out; // 保证不超过 max
            }
            aggressiveQuality = Math.max(0.18f, aggressiveQuality - 0.03f);
            aggressiveScale *= 0.8;
            if (aggressiveScale < 0.12) break;
        }

        // 兜底返回最后一次结果（极端情况下可能仍略超出 max）
        log.warn("[FaceImageUtil] return last (may exceed max): {}KB", (int)Math.ceil(last.length / 1024.0));
        return last;
    }
}