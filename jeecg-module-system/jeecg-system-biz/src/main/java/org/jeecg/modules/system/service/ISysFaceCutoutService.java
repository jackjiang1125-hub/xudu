package org.jeecg.modules.system.service;

/**
 * 用户头像抠图服务接口
 */
public interface ISysFaceCutoutService {

    /**
     * 根据头像来源（相对路径/HTTP/BASE64）生成抠图并保存到本地静态目录，返回数据库相对路径
     * @param avatar 头像来源字符串
     * @return 相对路径（如 user/face_cutout/xxx.jpg），失败返回 null
     */
    String generateFaceCutoutFromAvatar(String avatar);
}