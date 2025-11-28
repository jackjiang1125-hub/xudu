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
import org.jeecg.modules.wec.constants.WecLocationConstants;
import org.jeecg.modules.wec.entity.WecLocation;
import org.jeecg.modules.wec.service.IWecLocationService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(WecLocationConstants.LOCATION_BASE)
@RequiredArgsConstructor
@Slf4j
public class WecLocationController extends JeecgController<WecLocation, IWecLocationService> {

    private final IWecLocationService locationService;

    @GetMapping("/list")
    @AutoLog("安装位置-列表")
    public Result<?> list(WecLocation location,
                          @RequestParam(name = "pageNo", required = false) Integer pageNo,
                          @RequestParam(name = "pageSize", required = false) Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<WecLocation> qw = QueryGenerator.initQueryWrapper(location, req.getParameterMap());
        boolean hasPage = req.getParameter("pageNo") != null || req.getParameter("pageSize") != null;
        if (hasPage) {
            Page<WecLocation> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
            IPage<WecLocation> pageList = locationService.page(page, qw);
            return Result.OK(locationService.toVOPage(pageList));
        } else {
            return Result.OK(locationService.toVOList(locationService.list(qw)));
        }
    }

    @PostMapping("/add")
    @AutoLog("安装位置-添加")
    public Result<String> add(@RequestBody WecLocation entity) {
        entity.setLocationName(clean(entity.getLocationName()));
        entity.setRemark(clean(entity.getRemark()));
        locationService.save(entity);
        return Result.OK("添加成功");
    }

    @PutMapping("/edit")
    @AutoLog("安装位置-编辑")
    public Result<String> edit(@RequestBody WecLocation entity) {
        entity.setLocationName(clean(entity.getLocationName()));
        entity.setRemark(clean(entity.getRemark()));
        boolean ok = locationService.updateById(entity);
        return ok ? Result.OK("编辑成功") : Result.error("记录不存在");
    }

    @DeleteMapping("/delete")
    @AutoLog("安装位置-删除")
    public Result<String> delete(@RequestParam String id) {
        locationService.removeById(id);
        return Result.OK("删除成功");
    }

    private String clean(String s) { return s == null ? null : s.replace("*", ""); }
}

