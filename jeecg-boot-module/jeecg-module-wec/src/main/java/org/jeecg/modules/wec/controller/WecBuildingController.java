package org.jeecg.modules.wec.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.wec.constants.WecStructureConstants;
import org.jeecg.modules.wec.entity.WecBuilding;
import org.jeecg.modules.wec.entity.WecFloor;
import org.jeecg.modules.wec.entity.WecRoom;
import org.jeecg.modules.wec.service.IWecBuildingService;
import org.jeecg.modules.wec.service.IWecFloorService;
import org.jeecg.modules.wec.service.IWecRoomService;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(WecStructureConstants.BUILDING_BASE)
@RequiredArgsConstructor
@Slf4j
public class WecBuildingController extends JeecgController<WecBuilding, IWecBuildingService> {

    private final IWecBuildingService buildingService;
    private final IWecFloorService floorService;
    private final IWecRoomService roomService;

    @GetMapping("/list")
    @AutoLog("楼栋-列表")
    public Result<?> list(WecBuilding building,
                          @RequestParam(name = "pageNo", required = false) Integer pageNo,
                          @RequestParam(name = "pageSize", required = false) Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<WecBuilding> qw = QueryGenerator.initQueryWrapper(building, req.getParameterMap());
        boolean hasPage = req.getParameter("pageNo") != null || req.getParameter("pageSize") != null;
        if (hasPage) {
            Page<WecBuilding> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
            IPage<WecBuilding> pageList = buildingService.page(page, qw);
            return Result.OK(buildingService.toVOPage(pageList));
        } else {
            return Result.OK(buildingService.toVOList(buildingService.list(qw)));
        }
    }

    @PostMapping("/add")
    @AutoLog("楼栋-添加")
    public Result<String> add(@RequestBody WecBuilding entity) {
        entity.setBuildingName(clean(entity.getBuildingName()));
        entity.setBuildingCode(clean(entity.getBuildingCode()));
        buildingService.save(entity);
        return Result.OK("添加成功");
    }

    @PutMapping("/edit")
    @AutoLog("楼栋-编辑")
    public Result<String> edit(@RequestBody WecBuilding entity) {
        entity.setBuildingName(clean(entity.getBuildingName()));
        entity.setBuildingCode(clean(entity.getBuildingCode()));
        boolean ok = buildingService.updateById(entity);
        return ok ? Result.OK("编辑成功") : Result.error("记录不存在");
    }

    @DeleteMapping("/delete")
    @AutoLog("楼栋-删除")
    @Transactional
    public Result<String> delete(@RequestParam String id) {
        java.util.List<WecFloor> floors = floorService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WecFloor>().eq("building_id", id));
        java.util.List<String> floorIds = floors.stream().map(WecFloor::getId).toList();
        if (!floorIds.isEmpty()) {
            roomService.remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WecRoom>().in("floor_id", floorIds));
            floorService.remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WecFloor>().in("id", floorIds));
        }
        buildingService.removeById(id);
        return Result.OK("删除成功");
    }

    @DeleteMapping("/deleteBatch")
    @AutoLog("楼栋-批量删除")
    @Transactional
    public Result<String> deleteBatch(@RequestParam String ids) {
        java.util.List<String> idList = java.util.Arrays.asList(ids.split(","));
        java.util.List<WecFloor> floors = floorService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WecFloor>().in("building_id", idList));
        java.util.List<String> floorIds = floors.stream().map(WecFloor::getId).toList();
        if (!floorIds.isEmpty()) {
            roomService.remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WecRoom>().in("floor_id", floorIds));
            floorService.remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WecFloor>().in("id", floorIds));
        }
        buildingService.removeByIds(idList);
        return Result.OK("批量删除成功");
    }

    private String clean(String s) { return s == null ? null : s.replace("*", ""); }
}
