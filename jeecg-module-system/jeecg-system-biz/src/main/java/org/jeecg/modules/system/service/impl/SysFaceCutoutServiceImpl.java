package org.jeecg.modules.system.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.service.ISysFaceCutoutService;
import org.jeecg.modules.system.util.FaceCutoutUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class SysFaceCutoutServiceImpl implements ISysFaceCutoutService {

    @Value("${jeecg.path.upload}")
    private String uploadPath;

    private static final String BIZ_SUB_DIR = "user/face_cutout";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Override
    public String generateFaceCutoutFromAvatar(String avatar) {
        try {
            byte[] avatarBytes = readAvatarBytes(avatar);
            if (avatarBytes == null || avatarBytes.length == 0) {
                log.warn("头像字节为空，跳过抠图");
                return null;
            }
            byte[] faceBytes = FaceCutoutUtil.extractLargestFace(avatarBytes, 10);
            if (faceBytes == null || faceBytes.length == 0) {
                log.warn("未能提取到人脸，跳过保存");
                return null;
            }
            return saveFaceCutout(faceBytes);
        } catch (Throwable t) {
            log.warn("生成人脸抠图失败，忽略: {}", t.getMessage());
            return null;
        }
    }

    private byte[] readAvatarBytes(String avatar) {
        if (avatar == null || avatar.trim().isEmpty()) return null;
        try {
            String v = avatar.trim();
            if (v.startsWith("http://") || v.startsWith("https://")) {
                try (InputStream is = new URL(v).openStream()) {
                    return toByteArray(is);
                }
            }
            if (v.startsWith("data:image")) {
                String[] parts = v.split(",", 2);
                String b64 = parts.length == 2 ? parts[1] : v;
                return Base64.getDecoder().decode(b64);
            }
            // 相对路径：拼接 uploadPath
            String cleaned = v.replace("\\", "/");
            if (cleaned.startsWith("/")) cleaned = cleaned.substring(1);
            File file = new File(uploadPath, cleaned);
            try (InputStream is = new FileInputStream(file)) {
                return toByteArray(is);
            }
        } catch (Exception e) {
            log.warn("读取头像失败: {}", e.toString());
            return null;
        }
    }

    private String saveFaceCutout(byte[] faceBytes) throws Exception {
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String name = LocalDateTime.now().format(TS) + "_" + UUID.randomUUID().toString().replace("-", "") + ".jpg";
        Path dir = new File(uploadPath, BIZ_SUB_DIR + "/" + dateFolder).toPath();
        Files.createDirectories(dir);
        Path file = dir.resolve(name);
        Files.write(file, faceBytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        String dbPath = BIZ_SUB_DIR + "/" + dateFolder + "/" + name;
        return dbPath.replace("\\", "/");
    }

    private byte[] toByteArray(InputStream is) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        while ((len = is.read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        return bos.toByteArray();
    }
}