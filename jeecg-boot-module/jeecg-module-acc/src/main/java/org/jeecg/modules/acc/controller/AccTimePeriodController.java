package org.jeecg.modules.acc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.acc.entity.AccTimePeriod;
import org.jeecg.modules.acc.service.IAccTimePeriodService;
import org.jeecg.modules.acc.vo.TimePeriodVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "ACC-门禁时间段管理")
@RestController
@RequestMapping("/acc/timeperiod")
@Slf4j
public class AccTimePeriodController extends JeecgController<AccTimePeriod, IAccTimePeriodService> {

    @Autowired
    private IAccTimePeriodService timePeriodService;

    /**
     * 分页查询时间段
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询时间段")
    public Result<IPage<TimePeriodVO>> list(@RequestParam(name = "name", required = false) String name,
                                            @RequestParam(name = "creator", required = false) String creator,
                                            @RequestParam(name = "updatedAt_begin", required = false) String updatedBegin,
                                            @RequestParam(name = "updatedAt_end", required = false) String updatedEnd,
                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                            HttpServletRequest req) {
        IPage<TimePeriodVO> page = timePeriodService.pageList(name, creator, updatedBegin, updatedEnd, pageNo, pageSize);
        return Result.OK(page);
    }

    /**
     * 查询详情（含每日时间段）
     */
    @GetMapping("/detail")
    @Operation(summary = "查询时间段详情")
    public Result<TimePeriodVO> detail(@RequestParam String id) {
        TimePeriodVO vo = timePeriodService.getDetailById(id);
        return vo == null ? Result.error("时间段不存在") : Result.OK(vo);
    }

    /**
     * 新增时间段
     */
    @PostMapping("/add")
    @Operation(summary = "新增时间段")
    public Result<TimePeriodVO> add(@RequestBody TimePeriodVO vo) {
        String operator = getOperator();
        TimePeriodVO saved = timePeriodService.saveVO(vo, operator);
        return Result.OK(saved);
    }

    /**
     * 编辑时间段
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑时间段")
    public Result<TimePeriodVO> edit(@RequestBody TimePeriodVO vo) {
        String operator = getOperator();
        TimePeriodVO updated = timePeriodService.updateVO(vo, operator);
        return Result.OK(updated);
    }

    /**
     * 删除时间段
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除时间段")
    public Result<String> delete(@RequestParam String id) {
        boolean ok = timePeriodService.deleteWithDetails(id);
        return ok ? Result.OK("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除时间段
     */
    @DeleteMapping("/deleteBatch")
    @Operation(summary = "批量删除时间段")
    public Result<String> deleteBatch(@RequestParam String ids) {
        String[] idArr = ids.split(",");
        boolean ok = timePeriodService.deleteBatchWithDetails(idArr);
        return ok ? Result.OK("批量删除成功") : Result.error("批量删除失败");
    }

    private String getOperator() {
        try {
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            return loginUser != null ? loginUser.getUsername() : "system";
        } catch (Exception e) {
            log.warn("获取当前登录用户失败，使用默认操作人", e);
            return "system";
        }
    }
}