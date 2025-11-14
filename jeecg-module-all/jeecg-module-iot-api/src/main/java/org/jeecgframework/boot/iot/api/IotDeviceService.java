package org.jeecgframework.boot.iot.api;

import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.jeecgframework.boot.iot.query.IotDeviceQuery;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;

import java.util.Map;
import java.util.Date;
import java.util.List;

public interface IotDeviceService  {

    PageResult<IotDeviceVO> list(IotDeviceQuery iotDeviceQuery,PageRequest pageRequest, Map<String,String[]> queryParam);

    /**
     * 根据设备序列号查询iot设备的完整详情
     * @param sn 设备序列号
     * @return 设备详情（不存在返回null）
     */
    IotDeviceVO getBySn(String sn);

    /**
     * 为设备授权
     * @param deviceSn 设备序列号
     * @param registryCode 注册码
     * @param remark 备注
     * @param operator 操作人
     */
    void authorizeDevice(String deviceSn,
            String registryCode,
            String remark,
            String operator);

    /**
     * 根据设备序列号删除设备
     * @param sn 设备序列号
     */
    void deleteByDeviceSn(String sn);
    /**
     * 同步时区
     * @param sn 设备序列号
     * @param tz 时区
     */
    void syncTimezone(String sn, String tz);

    /**
     * 同步时间
     * @param sn 设备序列号
     * @param epochSeconds 时间戳（秒）
     */
    void syncTime(String sn, Long epochSeconds);

    /**
     * 删除一体化模板，type=8是掌静脉
     * @param sn 设备序列号
     */
    void deleteAllTemplatesType8(String sn);

    /**
     * 删除一体化模板，type=9是可见光面部
     * @param sn 设备序列号
     */
    void deleteAllTemplatesType9(String sn);

    /**
     * 删除所有用户权限
     * @param sn 设备序列号
     */
    void deleteAllUserAuthorize(String sn);

    /**
     * 删除所有用户
     * @param sn 设备序列号
     */
    void deleteAllUsers(String sn);

    /**
     * 删除全部联动详细信息数据
     * @param sn 设备序列号
     */
    void deleteAllInoutfun(String sn);

    /**
     * 删除全部首卡开门数据
     * 对应命令：DATA DELETE firstcard *
     */
    void deleteAllFirstcard(String sn);

    /**
     * 删除全部不同时段的验证方式数据
     * 对应命令：DATA DELETE DiffTimezoneVS *
     */
    void deleteAllDiffTimezoneVS(String sn);

    /**
     * 删除全部门不同时段的验证方式数据
     * 对应命令：DATA DELETE DoorVSTimezone *
     */
    void deleteAllDoorVSTimezone(String sn);

    /**
     * 删除全部人不同时段的验证方式数据
     * 对应命令：DATA DELETE PersonalVSTimezone *
     */
    void deleteAllPersonalVSTimezone(String sn);

    /**
     * 删除全部输入控制（受时间段限制）数据
     * 对应命令：DATA DELETE InputIOSetting *
     */
    void deleteAllInputIOSetting(String sn);

    /**
     * 删除全部时间组
     * 对应命令：DATA DELETE timezone *
     */
    void deleteAllTimezone(String sn);

    /**
     * 删除指定时间段
     * 对应命令：DATA DELETE timezone TimeZoneId=<id>
     * @param sn 设备序列号
     * @param timezoneId 时间段编号（TimeZoneId）
     */
    void deleteTimezoneById(String sn, int timezoneId);

    /**
     * 删除全部节假日
     * 对应命令：DATA DELETE holiday *
     */
    void deleteAllHoliday(String sn);

    /**
     * 删除全部多卡开门数据
     * 对应命令：DATA DELETE multimcard *
     */
    void deleteAllMultimcard(String sn);

    /**
     * 设置互锁模式
     * @param sn 设备序列号
     * @param code 互锁模式代码
     */
    void setInterlock(String sn, Integer code);
    /**
     * 设置反潜模式
     * @param sn 设备序列号
     * @param code 反潜模式代码
     */
    void setBacktracking(String sn, Integer code);

    
    /**
     * 设置首卡开门配置（非法组合）
     * @param sn 设备序列号
     * @param params 键值参数，例如 Door1FirstCardOpenDoor=0/1/2
     */
    void setDoorFirstCardOpenDoor(String sn, Map<String, Integer> params);

    /**
     * 设置门的相关参数
     * @param sn 设备序列号
     * @param params 键值参数集合（整数或为空），如 Door1CloseAndLock、WiegandIDIn 等
     */
    void setDoorRelatedParameter(String sn, Map<String, Integer> params);

    /**
     * 设置读头离线后是否正常通行
     * @param sn 设备序列号
     * @param params 键值参数，例如 Reader1OfflineRefuse、Reader2OfflineRefuse、AutoServerMode
     */
    void setReaderOfflineAccess(String sn, Map<String, Integer> params);

    /**
     * 设置门锁定标志
     * @param sn 设备序列号
     * @param params 键值参数，例如 Door1MaskFlag=1/2
     */
    void setDoorMaskFlag(String sn, Map<String, Integer> params);

    /**
     * 设置是否多卡开门
     * @param sn 设备序列号
     * @param params 键值参数，例如 Door1MultiCardOpenDoor=0/1
     */
    void setMultiCardOpenDoor(String sn, Map<String, Integer> params);

    /**
     * 更新门的出入类型的输入控制（受时间段限制）
     * @param sn 设备序列号
     * @param params 键值参数，例如 Number、InType、TimeZoneId
     */
    void updateInputIOSetting(String sn, Map<String, Integer> params);

    /**
     * 更新门禁时间段规则
     * @param sn 设备序列号
     * @param params 键值参数，例如 TimeZoneId、StartTime、EndTime 或各种时间段参数
     */
    void updateTimezone(String sn, Map<String, ?> params);

    /**
     * 下发人员新增（含权限）命令序列；按协议下发 4 条基础命令：
     * 1) DATA UPDATE user（人员信息）
     * 2) DATA UPDATE userauthorize（门禁权限）
     * 3) DATA UPDATE userpic（人员照片，默认占位 URL）
     * 4) DATA UPDATE biophoto（比对照片，默认占位 URL）
     * @param sn 设备序列号
     * @param pin 工号（唯一标识）
     * @param name 姓名
     * @param authorizeTimezoneId 授权时区编号（通常与业务时间段的排序号一致）
     * @param authorizeDoorId 授权门位图（1..15，15表示所有门）
     * @param devId 设备ID（一般取1）
     */
    void addUserWithAuthorize(String sn, String pin, String name, Integer authorizeTimezoneId, Integer authorizeDoorId, Integer devId);

    /**
     * 远程开门（CONTROL DEVICE），由实现内部调用控制命令分发器。
     */
    void remoteOpenDoor(String sn, Integer doorId, Integer pulseSeconds, String operator);

    /**
     * 远程关门/锁定（SET OPTIONS DoorXMaskFlag=2），由实现内部调用控制命令分发器。
     */
    void remoteCloseDoor(String sn, Integer doorId, String operator);

    /**
     * 取消报警（CONTROL DEVICE 02010000）。
     */
    void remoteCancelAlarm(String sn, Integer doorId, String operator);

    /**
     * 远程常开（CONTROL DEVICE 010101ff）。
     */
    void remoteHoldOpen(String sn, Integer doorId, String operator);

    /**
     * 远程锁定（CONTROL DEVICE 06010100）。
     */
    void remoteLockDoor(String sn, Integer doorId, String operator);

    /**
     * 远程解锁（CONTROL DEVICE 06010000）。
     */
    void remoteUnlockDoor(String sn, Integer doorId, String operator);

    /**
     * 启动当天常开时间段（CONTROL DEVICE 04010100）。
     */
    void enableTodayAlwaysOpen(String sn, Integer doorId, String operator);

    /**
     * 禁用当天常开时间段（CONTROL DEVICE 04010000）。
     */
    void disableTodayAlwaysOpen(String sn, Integer doorId, String operator);

    /**
     * 入队 DATA COUNT 命令（返回命令ID）
     */
    String enqueueDataCountUser(String sn, String operator);
    String enqueueDataCountBioPhoto(String sn, String operator);
    String enqueueDataCountBiodata(String sn, Integer type, String operator);
    List<String> getCommandReportsRaw(String sn, Long sinceMillis, List<String> ids);

    /**
     * 下发人员新增（含权限）命令序列（增强版）。固定 4 条基础命令：
     * 1) DATA UPDATE user（人员信息）
     * 2) DATA UPDATE userauthorize（门禁权限）
     * 3) DATA UPDATE userpic（人员照片，可为空）
     * 4) DATA UPDATE biophoto（比对照片，可为空）
     * 当图片参数为空时，以空 URL 方式占位，设备可忽略。
     * @param sn 设备序列号
     * @param pin 工号（唯一标识）
     * @param name 姓名
     * @param authorizeTimezoneId 授权时区编号（通常与业务时间段的排序号一致）
     * @param authorizeDoorId 授权门位图（1..15，15表示所有门）
     * @param devId 设备ID（一般取1）
     * @param userPic 人员图片 URL 或 Base64（可为空）
     * @param bioPhoto 抠图/比对图片 URL 或 Base64（可为空）
     */
    void addUserWithAuthorize(String sn, String pin, String name,
                              Integer authorizeTimezoneId, Integer authorizeDoorId, Integer devId,
                              String userPic, String bioPhoto);

    /**
     * 下发人员新增（含权限）命令序列（增强版-带卡号与管理密码）。固定 4 条基础命令：
     * 1) DATA UPDATE user（人员信息，包含 cardno/password）
     * 2) DATA UPDATE userauthorize（门禁权限）
     * 3) DATA UPDATE userpic（人员照片，可为空）
     * 4) DATA UPDATE biophoto（比对照片，可为空）
     * @param sn 设备序列号
     * @param pin 工号（唯一标识）
     * @param name 姓名
     * @param authorizeTimezoneId 授权时区编号
     * @param authorizeDoorId 授权门位图（1..15，15表示所有门）
     * @param devId 设备ID（一般取1）
     * @param userPic 人员图片 URL 或 Base64（可为空）
     * @param bioPhoto 抠图/比对图片 URL 或 Base64（可为空）
     * @param cardNumber 人员卡号（十进制或协议要求格式，可为空）
     * @param verifyPassword 人员管理密码（<=6 位，可为空）
     */
    void addUserWithAuthorize(String sn, String pin, String name,
                              Integer authorizeTimezoneId, Integer authorizeDoorId, Integer devId,
                              String userPic, String bioPhoto,
                              String cardNumber, String verifyPassword);

    /**
     * 下发人员新增（含权限）命令序列（增强版-带业务字段）。固定 4 条基础命令：
     * 1) DATA UPDATE user（人员信息，包含 cardno/password/privilege/有效期）
     * 2) DATA UPDATE userauthorize（门禁权限，支持有效期）
     * 3) DATA UPDATE userpic（人员照片，可为空）
     * 4) DATA UPDATE biophoto（比对照片，可为空）
     * @param sn 设备序列号
     * @param pin 工号（唯一标识）
     * @param name 姓名
     * @param authorizeTimezoneId 授权时区编号
     * @param authorizeDoorId 授权门位图（1..15，15表示所有门）
     * @param devId 设备ID（一般取1）
     * @param userPic 人员图片 URL 或 Base64（可为空）
     * @param bioPhoto 抠图/比对图片 URL 或 Base64（可为空）
     * @param cardNumber 人员卡号（十进制或协议要求格式，可为空）
     * @param verifyPassword 人员管理密码（<=6 位，可为空）
     * @param superUser 超级用户(0/1)
     * @param deviceOpPerm 设备操作权限（预留映射）
     * @param extendAccess 扩展权限开关（预留映射）
     * @param validTimeEnabled 启用有效期
     * @param validStartTime 有效期开始
     * @param validEndTime 有效期结束
     */
    void addUserWithAuthorize(String sn, String pin, String name,
                              Integer authorizeTimezoneId, Integer authorizeDoorId, Integer devId,
                              String userPic, String bioPhoto,
                              String cardNumber, String verifyPassword,
                              Integer superUser, Integer deviceOpPerm,
                              Boolean extendAccess, Boolean prohibitedRoster, Boolean validTimeEnabled,
                              Date validStartTime, Date validEndTime);

    void addUserAuthorize(String sn, String pin, Integer authorizeTimezoneId, Integer authorizeDoorId, Integer devId);

    /**
     * 下发人员删除（含权限）命令序列；按协议下发 4 条基础命令：
     * 1) DATA DELETE userauthorize
     * 2) DATA DELETE biophoto
     * 3) DATA DELETE userpic
     * 4) DATA DELETE user
     * @param sn 设备序列号
     * @param pin 工号（唯一标识）
     */
    void removeUserAndAuthorize(String sn, String pin);

    /**
     * 下发删除人员时间规则
     * @param sn
     * @param pin
     */
    void removeAuthorize(String sn, String pin);

    /**
     * 下发人员删除照片和抠图权限：
     * 2) DATA DELETE biophoto
     * 3) DATA DELETE userpic
     * @param sn 设备序列号
     * @param pin 工号（唯一标识）
     */
    void removeUserPicAndBioPhoto(String sn, String pin);

}
