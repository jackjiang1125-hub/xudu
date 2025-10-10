package org.jeecg.modules.iot.utils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * 图片压缩测试工具类
 */
@Component
public class ImageCompressionTest {
    
    /**
     * 测试图片压缩功能
     * @param originalBase64 原始图片的base64编码
     * @param targetSizeKB 目标大小（KB）
     * @return 压缩结果信息
     */
    public String testCompression(String originalBase64, int targetSizeKB) {
        try {
            // 解码base64
            byte[] originalBytes = Base64.getDecoder().decode(originalBase64);
            int originalSizeKB = originalBytes.length / 1024;
            
            // 压缩图片
            byte[] compressedBytes = compressImageToTargetSize(originalBytes, targetSizeKB * 1024);
            int compressedSizeKB = compressedBytes.length / 1024;
            
            // 计算压缩率
            double compressionRatio = (1.0 - (double) compressedBytes.length / originalBytes.length) * 100;
            
            return String.format(
                "压缩测试结果:\n" +
                "原始大小: %d KB\n" +
                "压缩后大小: %d KB\n" +
                "目标大小: %d KB\n" +
                "压缩率: %.2f%%\n" +
                "是否达到目标: %s",
                originalSizeKB,
                compressedSizeKB,
                targetSizeKB,
                compressionRatio,
                compressedBytes.length <= targetSizeKB * 1024 ? "是" : "否"
            );
            
        } catch (Exception e) {
            return "压缩测试失败: " + e.getMessage();
        }
    }
    
    /**
     * 压缩图片到指定大小以下
     */
    private byte[] compressImageToTargetSize(byte[] originalBytes, int targetSizeBytes) throws Exception {
        // 如果原始图片已经小于目标大小，直接返回
        if (originalBytes.length <= targetSizeBytes) {
            return originalBytes;
        }
        
        // 读取原始图片
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (originalImage == null) {
            throw new Exception("无法读取图片数据");
        }
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 计算压缩比例，从0.8开始尝试
        double scale = 0.8;
        byte[] compressedBytes = null;
        int attempts = 0;
        final int maxAttempts = 10;
        
        while (attempts < maxAttempts) {
            try {
                int newWidth = (int) (originalWidth * scale);
                int newHeight = (int) (originalHeight * scale);
                
                // 确保最小尺寸
                if (newWidth < 50) newWidth = 50;
                if (newHeight < 50) newHeight = 50;
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                Thumbnails.of(originalImage)
                    .size(newWidth, newHeight)
                    .antialiasing(Antialiasing.ON)
                    .outputQuality(0.8) // JPEG质量
                    .outputFormat("jpg") // 统一输出为JPEG格式
                    .toOutputStream(baos);
                
                compressedBytes = baos.toByteArray();
                
                // 如果压缩后大小符合要求，返回结果
                if (compressedBytes.length <= targetSizeBytes) {
                    return compressedBytes;
                }
                
                // 如果还是太大，减小压缩比例
                scale *= 0.8;
                attempts++;
                
            } catch (Exception e) {
                attempts++;
                scale *= 0.8;
            }
        }
        
        // 如果所有尝试都失败，返回最后一次的结果
        if (compressedBytes != null) {
            return compressedBytes;
        }
        
        // 如果完全失败，返回原始图片
        return originalBytes;
    }
}
