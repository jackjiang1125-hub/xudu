package org.jeecg.modules.wec.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.wec.entity.WecDevice;
import org.jeecg.modules.wec.service.IWecDeviceService;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeecg.common.util.RedisUtil;

@Tag(name = "水控设备管理")
@RestController
@RequestMapping("/wec/device")
@Slf4j
public class WecDeviceController extends JeecgController<WecDevice, IWecDeviceService> {

    @Operation(summary = "批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        wecDeviceService.removeDevices(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "远程控制")
    @PostMapping(value = "/control/{cmd}")
    public Result<?> control(@PathVariable("cmd") String cmd, @RequestBody Map<String, Object> params) {
        Object snsObj = params.get("sns");
        String sns = "";
        if (snsObj instanceof String) {
            sns = (String) snsObj;
        } else if (snsObj instanceof List) {
            List<String> list = (List<String>) snsObj;
            sns = String.join(",", list);
        }
        
        if (sns == null || sns.isEmpty()) return Result.error("请选择设备");

        wecDeviceService.executeBatchControl(cmd, sns);
        return Result.OK("指令已下发");
    }

    @Autowired
    private IWecDeviceService wecDeviceService;

    @Autowired
    private RedisUtil redisUtil;

    private static final String REDIS_KEY_PREFIX_HEARTBEAT = "iot:water:heartbeat:";

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<WecDevice>> queryPageList(WecDevice wecDevice,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  HttpServletRequest req) {
        QueryWrapper<WecDevice> queryWrapper = QueryGenerator.initQueryWrapper(wecDevice, req.getParameterMap());
        Page<WecDevice> page = new Page<>(pageNo, pageSize);
        IPage<WecDevice> pageList = wecDeviceService.page(page, queryWrapper);

        // Populate heartbeat from Redis
        if (pageList.getRecords() != null) {
            for (WecDevice device : pageList.getRecords()) {
                if (device.getSn() != null) {
                    Object val = redisUtil.get(REDIS_KEY_PREFIX_HEARTBEAT + device.getSn());
                    if (val != null) {
                        try {
                            long ts = Long.parseLong(val.toString());
                            device.setLastHeartbeatTime(new java.util.Date(ts));
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }

        return Result.OK(pageList);
    }

    @Operation(summary = "搜索待添加设备")
    @GetMapping(value = "/searchPending")
    public Result<List<IotDeviceVO>> searchPending(@RequestParam(name = "keyword", required = false) String keyword) {
        List<IotDeviceVO> list = wecDeviceService.searchPendingDevices(keyword);
        return Result.OK(list);
    }

    @Operation(summary = "添加/绑定设备")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody Map<String, Object> params) {
        // 支持传入 resetData 参数
        WecDevice wecDevice = new WecDevice();
        wecDevice.setSn((String) params.get("sn"));
        wecDevice.setDeviceName(clean((String) params.get("deviceName")));
        wecDevice.setIpAddress((String) params.get("ipAddress"));
        wecDevice.setDeviceType((String) params.get("deviceType"));
        wecDevice.setStatus((String) params.get("status"));

        // 其他字段处理（如果有）
        wecDevice.setRateTemplateId((String) params.get("rateTemplateId"));

        boolean resetData = Boolean.TRUE.equals(params.get("resetData"));

        wecDeviceService.save(wecDevice, resetData);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody WecDevice wecDevice) {
        wecDevice.setDeviceName(clean(wecDevice.getDeviceName()));
        // 如果有其他需要clean的字段，也在这里处理
        wecDeviceService.updateById(wecDevice);
        return Result.OK("编辑成功!");
    }

    private String clean(String s) {
        if (s == null) return null;
        return s.replace("*", "");
    }

    @Operation(summary = "通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        wecDeviceService.removeDevice(id);
        return Result.OK("删除成功!");
    }
}


