package com.xudu.center.zlm.api;

import com.xudu.center.zlm.constants.PlayTarget;
import com.xudu.center.zlm.dto.NormalizeResult;

public interface IOnDemandPlayService {
    NormalizeResult prepareByCamera(String cameraId, PlayTarget target, boolean preferNvenc);


}
