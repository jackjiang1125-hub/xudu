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
import org.jeecg.modules.wec.constants.WecRateTemplateConstants;
import org.jeecg.modules.wec.entity.WecRateTemplate;
import org.jeecg.modules.wec.service.IWecRateTemplateService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(WecRateTemplateConstants.RATE_TEMPLATE_BASE)
@RequiredArgsConstructor
@Slf4j
public class WecRateTemplateController extends JeecgController<WecRateTemplate, IWecRateTemplateService> {

    private final IWecRateTemplateService rateTemplateService;

    @GetMapping("/list")
    @AutoLog("费率模板-列表")
    public Result<?> list(WecRateTemplate tpl,
                          @RequestParam(name = "pageNo", required = false) Integer pageNo,
                          @RequestParam(name = "pageSize", required = false) Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<WecRateTemplate> qw = QueryGenerator.initQueryWrapper(tpl, req.getParameterMap());
        boolean hasPage = req.getParameter("pageNo") != null || req.getParameter("pageSize") != null;
        if (hasPage) {
            Page<WecRateTemplate> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
            IPage<WecRateTemplate> pageList = rateTemplateService.page(page, qw);
            return Result.OK(rateTemplateService.toVOPage(pageList));
        } else {
            return Result.OK(rateTemplateService.toVOList(rateTemplateService.list(qw)));
        }
    }

    @PostMapping("/add")
    @AutoLog("费率模板-添加")
    public Result<String> add(@RequestBody WecRateTemplate entity) {
        entity.setTemplateName(clean(entity.getTemplateName()));
        entity.setType(clean(entity.getType()));
        entity.setWorkMode(clean(entity.getWorkMode()));
        entity.setDeductionMethod(clean(entity.getDeductionMethod()));
        rateTemplateService.save(entity);
        return Result.OK("添加成功");
    }

    @PutMapping("/edit")
    @AutoLog("费率模板-编辑")
    public Result<String> edit(@RequestBody WecRateTemplate entity) {
        entity.setTemplateName(clean(entity.getTemplateName()));
        entity.setType(clean(entity.getType()));
        entity.setWorkMode(clean(entity.getWorkMode()));
        entity.setDeductionMethod(clean(entity.getDeductionMethod()));
        boolean ok = rateTemplateService.updateById(entity);
        return ok ? Result.OK("编辑成功") : Result.error("记录不存在");
    }

    @DeleteMapping("/delete")
    @AutoLog("费率模板-删除")
    public Result<String> delete(@RequestParam String id) {
        rateTemplateService.removeById(id);
        return Result.OK("删除成功");
    }

    private String clean(String s) { return s == null ? null : s.replace("*", "").trim(); }
}
