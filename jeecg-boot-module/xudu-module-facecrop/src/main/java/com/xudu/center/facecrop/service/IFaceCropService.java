package com.xudu.center.facecrop.service;

import com.xudu.center.facecrop.model.FaceCropResponse;

/**
 * Independent service interface for face crop.
 * Receives original image path and destination save path, returns standardized response.
 */
public interface IFaceCropService {
    /**
     * Crop face from source image and save to target path.
     * @param sourcePath original image file path
     * @param targetPath destination image file path
     * @return standardized response with processing status and result info
     */
    FaceCropResponse cropImage(String sourcePath, String targetPath);
}