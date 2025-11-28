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
import org.jeecg.modules.wec.entity.WecFloor;
import org.jeecg.modules.wec.service.IWecFloorService;
import org.jeecg.modules.wec.service.IWecRoomService;
import org.jeecg.modules.wec.entity.WecRoom;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(WecStructureConstants.FLOOR_BASE)
@RequiredArgsConstructor
@Slf4j
public class WecFloorController extends JeecgController<WecFloor, IWecFloorService> {

    private final IWecFloorService floorService;
    private final IWecRoomService roomService;

    @GetMapping("/list")
    @AutoLog("楼层-列表")
    public Result<?> list(WecFloor floor,
                          @RequestParam(name = "pageNo", required = false) Integer pageNo,
                          @RequestParam(name = "pageSize", required = false) Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<WecFloor> qw = QueryGenerator.initQueryWrapper(floor, req.getParameterMap());
        boolean hasPage = req.getParameter("pageNo") != null || req.getParameter("pageSize") != null;
        if (hasPage) {
            Page<WecFloor> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
            IPage<WecFloor> pageList = floorService.page(page, qw);
            pageList.getRecords().forEach(r -> {
                r.setFloorName(clean(r.getFloorName()));
                r.setFloorCode(clean(r.getFloorCode()));
            });
            return Result.OK(pageList);
        } else {
            return Result.OK(floorService.list(qw).stream().map(r -> {
                r.setFloorName(clean(r.getFloorName()));
                r.setFloorCode(clean(r.getFloorCode()));
                return r;
            }).toList());
        }
    }

    @PostMapping("/add")
    @AutoLog("楼层-添加")
    public Result<String> add(@RequestBody WecFloor entity) {
        entity.setFloorName(clean(entity.getFloorName()));
        entity.setFloorCode(clean(entity.getFloorCode()));
        floorService.save(entity);
        return Result.OK("添加成功");
    }

    @PutMapping("/edit")
    @AutoLog("楼层-编辑")
    public Result<String> edit(@RequestBody WecFloor entity) {
        entity.setFloorName(clean(entity.getFloorName()));
        entity.setFloorCode(clean(entity.getFloorCode()));
        boolean ok = floorService.updateById(entity);
        return ok ? Result.OK("编辑成功") : Result.error("记录不存在");
    }

    @DeleteMapping("/delete")
    @AutoLog("楼层-删除")
    @Transactional
    public Result<String> delete(@RequestParam String id) {
        roomService.remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WecRoom>().eq("floor_id", id));
        floorService.removeById(id);
        return Result.OK("删除成功");
    }

    @DeleteMapping("/deleteBatch")
    @AutoLog("楼层-批量删除")
    @Transactional
    public Result<String> deleteBatch(@RequestParam String ids) {
        java.util.List<String> idList = java.util.Arrays.asList(ids.split(","));
        roomService.remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WecRoom>().in("floor_id", idList));
        floorService.removeByIds(idList);
        return Result.OK("批量删除成功");
    }

    @PostMapping("/batchAdd")
    @AutoLog("楼层-批量添加")
    public Result<String> batchAdd(@RequestBody java.util.Map<String, Object> body) {
        String buildingId = String.valueOf(body.get("buildingId"));
        int startNo = Integer.parseInt(String.valueOf(body.get("startNo")));
        int endNo = Integer.parseInt(String.valueOf(body.get("endNo")));
        java.util.List<WecFloor> list = new java.util.ArrayList<>();
        for (int i = startNo; i <= endNo; i++) {
            WecFloor f = new WecFloor();
            f.setBuildingId(buildingId);
            f.setFloorName("第" + i + "层");
            f.setFloorCode("F" + i);
            list.add(f);
        }
        floorService.saveBatch(list);
        return Result.OK("批量添加成功");
    }

    private String clean(String s) { return s == null ? null : s.replace("*", ""); }
}
