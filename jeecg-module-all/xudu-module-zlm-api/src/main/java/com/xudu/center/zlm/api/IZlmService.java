package com.xudu.center.zlm.api;

import com.xudu.center.zlm.dto.CodecInfo;
import com.xudu.center.zlm.dto.NormalizeReq;
import com.xudu.center.zlm.dto.NormalizeResult;
import com.xudu.center.zlm.dto.ProxyAndNormalizeReq;
import com.xudu.center.zlm.model.ZlmResponse;

public interface IZlmService {

    ZlmResponse<Object> addProxy(String schema, String app, String stream, String url, Integer rtpType, Boolean closeWhenNoConsumer);

    CodecInfo probeCodec(String app, String stream);

    boolean isBrowserFriendly(CodecInfo c, String playMode);

    /** 规范化入口改为按 playMode 判断 */
    NormalizeResult normalizeForBrowser(NormalizeReq req);

    // -------- 一步到位：addProxy + normalize --------
    NormalizeResult addProxyAndNormalize(ProxyAndNormalizeReq req);
}
