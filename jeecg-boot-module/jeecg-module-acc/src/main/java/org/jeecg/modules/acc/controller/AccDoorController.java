package org.jeecg.modules.acc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.acc.entity.AccDoor;
import org.jeecg.modules.acc.mapstruct.AccDoorMapstruct;
import org.jeecg.modules.acc.service.IAccDoorService;
import org.jeecg.modules.acc.service.IAccGroupDeviceService;
import org.jeecg.modules.acc.vo.AccDoorVO;
import org.jeecg.modules.acc.mapper.AccDeviceMapper;
import org.jeecg.modules.acc.entity.AccDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "ACC-门列表管理")
@RestController
@RequestMapping("/acc/door")
@Slf4j
public class AccDoorController {

    @Autowired
    private IAccDoorService accDoorService;

    @Autowired
    private AccDoorMapstruct accDoorMapstruct;

    @Autowired
    private IAccGroupDeviceService accGroupDeviceService;

    @Autowired
    private AccDeviceMapper accDeviceMapper;

    /**
     * 分页查询门列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询门列表")
    public Result<IPage<AccDoorVO>> list(@RequestParam(name = "deviceName", required = false) String deviceName,
                                         @RequestParam(name = "doorName", required = false) String doorName,
                                         @RequestParam(name = "ipAddress", required = false) String ipAddress,
                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                         HttpServletRequest req) {
        IPage<AccDoor> page = accDoorService.pageDoors(deviceName, doorName, ipAddress, pageNo, pageSize);
        List<AccDoorVO> voList = accDoorMapstruct.toVOList(page.getRecords());
        Page<AccDoorVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return Result.OK(voPage);
    }

    /**
     * 根据权限组ID查询门列表
     */
    @GetMapping("/listByGroup")
    @Operation(summary = "根据权限组ID查询门列表")
    public Result<IPage<AccDoorVO>> listByGroup(@RequestParam String groupId,
                                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        List<String> deviceIds = accGroupDeviceService.listDeviceIdsByGroupId(groupId);
        if (deviceIds == null || deviceIds.isEmpty()) {
            Page<AccDoorVO> empty = new Page<>(pageNo, pageSize, 0);
            empty.setRecords(java.util.Collections.emptyList());
            return Result.OK(empty);
        }

        List<AccDevice> devices = accDeviceMapper.selectBatchIds(deviceIds);
        List<String> sns = devices.stream()
                .map(AccDevice::getSn)
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .collect(java.util.stream.Collectors.toList());
        if (sns.isEmpty()) {
            Page<AccDoorVO> empty = new Page<>(pageNo, pageSize, 0);
            empty.setRecords(java.util.Collections.emptyList());
            return Result.OK(empty);
        }

        LambdaQueryWrapper<AccDoor> qw = new LambdaQueryWrapper<>();
        qw.in(AccDoor::getDeviceSn, sns);
        Page<AccDoor> doorPage = new Page<>(pageNo, pageSize);
        IPage<AccDoor> page = accDoorService.page(doorPage, qw);

        List<AccDoorVO> voList = accDoorMapstruct.toVOList(page.getRecords());
        Page<AccDoorVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return Result.OK(voPage);
    }

    /**
     * 根据ID查询门详情
     */
    @GetMapping("/detail")
    @Operation(summary = "根据ID查询门详情")
    public Result<AccDoorVO> getById(@RequestParam String id) {
        AccDoor entity = accDoorService.getById(id);
        if (entity == null) {
            return Result.error("门不存在");
        }
        return Result.OK(accDoorMapstruct.toVO(entity));
    }

    /**
     * 新增门
     */
    @PostMapping("/add")
    @Operation(summary = "新增门")
    public Result<AccDoorVO> add(@RequestBody AccDoorVO vo) {
        try {
            AccDoor saved = accDoorService.saveFromVO(vo);
            return Result.OK(accDoorMapstruct.toVO(saved));
        } catch (Exception e) {
            log.error("新增门失败", e);
            return Result.error("新增门失败: " + e.getMessage());
        }
    }

    /**
     * 更新门
     */
    @PutMapping("/edit")
    @Operation(summary = "更新门")
    public Result<AccDoorVO> edit(@RequestBody AccDoorVO vo) {
        try {
            if (vo.getId() == null) {
                return Result.error("ID不能为空");
            }
            AccDoor saved = accDoorService.saveFromVO(vo);
            return Result.OK(accDoorMapstruct.toVO(saved));
        } catch (Exception e) {
            log.error("更新门失败", e);
            return Result.error("更新门失败: " + e.getMessage());
        }
    }

    /**
     * 删除门
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除门")
    public Result<Void> delete(@RequestParam String id) {
        try {
            boolean ok = accDoorService.removeById(id);
            return ok ? Result.OK() : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除门失败", e);
            return Result.error("删除门失败: " + e.getMessage());
        }
    }
}