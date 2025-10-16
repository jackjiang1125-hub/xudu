package org.jeecg.modules.acc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.acc.entity.AccGroupDevice;
import org.jeecg.modules.acc.service.IAccGroupDeviceService;
import org.jeecg.modules.acc.vo.AccDeviceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "ACC-权限组设备管理")
@RestController
@RequestMapping("/acc/accgroupdevice")
@Slf4j
public class AccGroupDeviceController extends JeecgController<AccGroupDevice, IAccGroupDeviceService> {

    @Autowired
    private IAccGroupDeviceService accGroupDeviceService;

    /**
     * 根据权限组ID查询设备列表
     */
    @GetMapping("/listByGroup")
    @Operation(summary = "根据权限组ID查询设备列表")
    public Result<IPage<AccDeviceVO>> listByGroup(@RequestParam String groupId,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                   HttpServletRequest req) {
        IPage<AccDeviceVO> page = accGroupDeviceService.listDevicesByGroupId(groupId, pageNo, pageSize);
        return Result.OK(page);
    }

    /**
     * 根据权限组ID查询所有设备ID列表
     */
    @GetMapping("/listIdsByGroup")
    @Operation(summary = "根据权限组ID查询所有设备ID列表")
    public Result<java.util.List<String>> listIdsByGroup(@RequestParam String groupId) {
        java.util.List<String> deviceIds = accGroupDeviceService.listDeviceIdsByGroupId(groupId);
        return Result.OK(deviceIds);
    }
}