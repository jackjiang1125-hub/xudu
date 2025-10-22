package com.xudu.center.video.camera.service;

import java.util.List;

import org.jeecg.common.system.base.service.JeecgService;
import com.xudu.center.video.camera.entity.CameraDevice;
import com.xudu.center.video.camera.vo.CameraVO;

/**
 * Business service for {@link CameraDevice}, wrapping common CRUD logic.
 */
public interface CameraService extends JeecgService<CameraDevice> {

    void addAll2ZLM();

    /** Persist a brand-new camera configuration. */
    CameraDevice create(CameraVO vo);

    /** Persist a brand-new camera configuration optionally skipping ZLM registration. */
    CameraDevice create(CameraVO vo, boolean pushToZlm);

    /** Discover via ONVIF and create one or many camera records. */
    List<CameraDevice> register(CameraVO vo);

    /** Update an existing camera record using the latest payload. */
    CameraDevice update(String id, CameraVO vo);

    /** Remove a camera record by its identifier. */
    boolean delete(String id);

    /** List root-level devices (no parent). */
    List<CameraDevice> listTopLevel();

    /** List all child devices of the supplied parent. */
    List<CameraDevice> listChildren(String parentId);

    /** List top-level devices whose type is NVR. */
    List<CameraDevice> listTopLevelNvr();

    /** Fetch camera details by id; returns null when not found. */
    CameraDevice getDetail(String id);
}
