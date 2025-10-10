package org.jeecg.modules.iot.protocol;

/**
 * @Description: 协议适配器接口
 * @Author: jeecg-boot
 * @Date: 2024-01-01
 * @Version: V1.0
 */
public interface ProtocolAdapter {

    /**
     * 获取适配器名称
     * @return 适配器名称
     */
    String name();

    /**
     * 是否支持指定协议
     * @param protocol 协议类型
     * @return 是否支持
     */
    boolean supports(String protocol);

    /**
     * 启动适配器
     * @throws Exception 启动异常
     */
    void start() throws Exception;

    /**
     * 停止适配器
     * @throws Exception 停止异常
     */
    void stop() throws Exception;

    /**
     * 发送下行数据
     * @param deviceSn 设备序列号
     * @param payload 下行数据
     * @return 是否成功
     */
    boolean sendDownlink(String deviceSn, byte[] payload);

    /**
     * 测试设备连接
     * @param deviceSn 设备序列号
     * @return 是否成功
     */
    boolean testConnect(String deviceSn);
}
