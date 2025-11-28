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
import org.jeecg.modules.wec.entity.WecRoom;
import org.jeecg.modules.wec.service.IWecRoomService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(WecStructureConstants.ROOM_BASE)
@RequiredArgsConstructor
@Slf4j
public class WecRoomController extends JeecgController<WecRoom, IWecRoomService> {

    private final IWecRoomService roomService;

    @GetMapping("/list")
    @AutoLog("房间-列表")
    public Result<?> list(WecRoom room,
                          @RequestParam(name = "pageNo", required = false) Integer pageNo,
                          @RequestParam(name = "pageSize", required = false) Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<WecRoom> qw = QueryGenerator.initQueryWrapper(room, req.getParameterMap());
        boolean hasPage = req.getParameter("pageNo") != null || req.getParameter("pageSize") != null;
        if (hasPage) {
            Page<WecRoom> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
            IPage<WecRoom> pageList = roomService.page(page, qw);
            pageList.getRecords().forEach(r -> {
                r.setRoomName(clean(r.getRoomName()));
                r.setRoomCode(clean(r.getRoomCode()));
            });
            return Result.OK(pageList);
        } else {
            return Result.OK(roomService.list(qw).stream().map(r -> {
                r.setRoomName(clean(r.getRoomName()));
                r.setRoomCode(clean(r.getRoomCode()));
                return r;
            }).toList());
        }
    }

    @PostMapping("/add")
    @AutoLog("房间-添加")
    public Result<String> add(@RequestBody WecRoom entity) {
        entity.setRoomName(clean(entity.getRoomName()));
        entity.setRoomCode(clean(entity.getRoomCode()));
        roomService.save(entity);
        return Result.OK("添加成功");
    }

    @PutMapping("/edit")
    @AutoLog("房间-编辑")
    public Result<String> edit(@RequestBody WecRoom entity) {
        entity.setRoomName(clean(entity.getRoomName()));
        entity.setRoomCode(clean(entity.getRoomCode()));
        boolean ok = roomService.updateById(entity);
        return ok ? Result.OK("编辑成功") : Result.error("记录不存在");
    }

    @DeleteMapping("/delete")
    @AutoLog("房间-删除")
    public Result<String> delete(@RequestParam String id) {
        roomService.removeById(id);
        return Result.OK("删除成功");
    }

    @DeleteMapping("/deleteBatch")
    @AutoLog("房间-批量删除")
    public Result<String> deleteBatch(@RequestParam String ids) {
        java.util.List<String> idList = java.util.Arrays.asList(ids.split(","));
        roomService.removeByIds(idList);
        return Result.OK("批量删除成功");
    }

    @PostMapping("/batchAdd")
    @AutoLog("房间-批量添加")
    public Result<String> batchAdd(@RequestBody java.util.Map<String, Object> body) {
        String floorId = String.valueOf(body.get("floorId"));
        int startNo = Integer.parseInt(String.valueOf(body.get("startNo")));
        int endNo = Integer.parseInt(String.valueOf(body.get("endNo")));
        java.util.List<WecRoom> list = new java.util.ArrayList<>();
        for (int i = startNo; i <= endNo; i++) {
            WecRoom r = new WecRoom();
            r.setFloorId(floorId);
            r.setRoomName("房间" + i);
            r.setRoomCode("R" + i);
            list.add(r);
        }
        roomService.saveBatch(list);
        return Result.OK("批量添加成功");
    }

    private String clean(String s) { return s == null ? null : s.replace("*", ""); }
}
