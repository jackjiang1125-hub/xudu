package org.jeecg.modules.system.util;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 用户模块的人脸检测与抠图工具（静态工具类，不依赖 Spring 注入）
 * 说明：尽量与 IoT 模块的实现保持一致，但不强制要求 Haar 资源存在，无法加载模型时走兜底逻辑。
 */
@Slf4j
public final class FaceCutoutUtil {

    private static final CascadeClassifier FACE_DETECTOR;

    static {
        OpenCvLoader.loadOnce();

        FACE_DETECTOR = new CascadeClassifier();
        boolean loaded = false;
        String[] candidates = new String[]{
                "/haarcascade_frontalface_alt.xml",
                "/haarcascade_frontalface_default.xml"
        };
        for (String path : candidates) {
            try (InputStream is = FaceCutoutUtil.class.getResourceAsStream(path)) {
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
            // 不抛异常，允许后续走兜底检测逻辑
            log.warn("未能加载 Haar 模型，将使用兜底中心裁剪方法");
        }
        log.info("OpenCV 初始化完成，version={}", Core.getVersionString());
    }

    private FaceCutoutUtil() {}

    public static List<Rect> detectFaces(byte[] imageBytes) {
        List<Rect> faces = new ArrayList<>();
        if (imageBytes == null || imageBytes.length == 0) {
            log.error("imageBytes 为空");
            return faces;
        }
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
                return detectFacesFallback(image);
            }

            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayImage, grayImage);

            double scaleFactor = 1.1;
            int minNeighbors = 3;
            int flags = 0;
            Size minSize = new Size(30, 30);
            Size maxSize = new Size();

            synchronized (FACE_DETECTOR) {
                FACE_DETECTOR.detectMultiScale(grayImage, faceDetections,
                        scaleFactor, minNeighbors, flags, minSize, maxSize);
            }

            for (Rect face : faceDetections.toArray()) {
                faces.add(face);
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

    private static List<Rect> detectFacesFallback(Mat image) {
        List<Rect> faces = new ArrayList<>();
        try {
            int width = image.width();
            int height = image.height();
            int faceWidth = Math.min(width, height) / 3;
            int faceHeight = faceWidth;
            int centerX = width / 2;
            int centerY = height / 2;
            int faceX = Math.max(0, Math.min(centerX - faceWidth / 2, width - faceWidth));
            int faceY = Math.max(0, Math.min(centerY - faceHeight / 2, height - faceHeight));
            faces.add(new Rect(faceX, faceY, faceWidth, faceHeight));
        } catch (Exception e) {
            log.error("备用人脸检测失败", e);
        }
        return faces;
    }

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
            if (width <= 0 || height <= 0) return null;

            Rect expandedRect = new Rect(x, y, width, height);
            faceImage = new Mat(image, expandedRect);
            Imgcodecs.imencode(".jpg", faceImage, out);
            return out.toArray();
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

    public static byte[] extractLargestFace(byte[] imageBytes, int padding) {
        List<Rect> faces = detectFaces(imageBytes);
        if (faces.isEmpty()) return null;
        Rect largestFace = faces.get(0);
        for (Rect face : faces) {
            if (face.area() > largestFace.area()) largestFace = face;
        }
        return extractFace(imageBytes, largestFace, padding);
    }

    public static String imageToBase64(byte[] imageBytes) {
        if (imageBytes == null) return null;
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
}

/**
 * OpenCV 原生库加载器（一次性加载）
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
                nu.pattern.OpenCV.loadShared();
            } catch (Throwable t) {
                log.warn("OpenCV.loadShared() 失败，尝试 loadLocally(): {}", t.toString());
                try {
                    nu.pattern.OpenCV.loadLocally();
                } catch (Throwable t2) {
                    log.warn("OpenCV.loadLocally() 失败: {}", t2.toString());
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