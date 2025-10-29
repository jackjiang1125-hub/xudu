package com.xudu.center.facecrop.service.impl;

import com.xudu.center.facecrop.service.FaceCropService;
import lombok.extern.slf4j.Slf4j;
import com.xudu.center.facecrop.model.FaceCropResponse;
import com.xudu.center.facecrop.model.FaceCropStatus;
import com.xudu.center.facecrop.service.IFaceCropService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class FaceCropServiceImpl implements IFaceCropService {

    @Resource
    private FaceCropService faceCropCore;

    @Override
    public FaceCropResponse cropImage(String sourcePath, String targetPath) {
        try {
            log.info("[facecrop] request: src={}, dst={}", sourcePath, targetPath);
            // Pre-validate to guard against path traversal and existence
            if (sourcePath == null || sourcePath.isBlank()) {
                return FaceCropResponse.error(FaceCropStatus.SRC_NOT_FOUND, "SRC_NOT_FOUND", "sourcePath is empty");
            }
            Path src = Paths.get(sourcePath).normalize();
            if (!Files.exists(src) || !Files.isRegularFile(src)) {
                return FaceCropResponse.error(FaceCropStatus.SRC_NOT_FOUND, "SRC_NOT_FOUND", "source file not found");
            }
            Path dst = Paths.get(targetPath).normalize();
            // Delegate to core service (xudu-module-facecrop)
            FaceCropResponse resp = faceCropCore.cropSingle(src.toString(), dst.toString());
            if (resp != null && resp.isSuccess()) {
                log.info("[facecrop] success: output={}, durationMs={}", resp.getOutputPath(), resp.getDurationMs());
            } else {
                log.warn("[facecrop] failed: status={}, code={}, msg={}",
                        (resp == null ? null : resp.getStatus()),
                        (resp == null ? null : resp.getCode()),
                        (resp == null ? null : resp.getMessage()));
            }
            return resp;
        } catch (Exception e) {
            log.error("[facecrop] processing error", e);
            return FaceCropResponse.error(FaceCropStatus.PROCESSING_ERROR, "PROCESSING_ERROR", e.getMessage());
        }
    }
}