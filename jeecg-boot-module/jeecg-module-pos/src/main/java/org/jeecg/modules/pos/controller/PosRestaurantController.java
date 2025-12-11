package org.jeecg.modules.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.pos.service.IPosRestaurantService;
import org.jeecg.modules.pos.vo.PosRestaurantVO;
import org.jeecg.modules.pos.request.PosRestaurantQuery;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * 餐厅信息管理控制器
 */
@Tag(name = "POS-餐厅信息管理")
@RestController
@RequestMapping("/pos/restaurant")
@Slf4j
public class PosRestaurantController{

    @Autowired
    private IPosRestaurantService restaurantService;

    /**
     * 分页查询餐厅信息
     */
    @AutoLog(value = "餐厅信息-分页列表查询")
    @GetMapping("/list")
    @Operation(summary = "分页查询餐厅信息")
    public Result<PageResult<PosRestaurantVO>> list(PosRestaurantQuery query,
                                                    PageRequest pageRequest,
                                                    HttpServletRequest req) {
        try {
            PageResult<PosRestaurantVO> page = restaurantService.list(query, pageRequest, req.getParameterMap());
            return Result.OK(page);
        } catch (Exception e) {
            log.error("查询餐厅列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询详情
     */
    @AutoLog(value = "餐厅信息-查询详情")
    @GetMapping("/detail")
    @Operation(summary = "查询餐厅详情")
    public Result<PosRestaurantVO> detail(@RequestParam String id) {
        try {
            PosRestaurantVO vo = restaurantService.getDetailById(id);
            if (vo == null) {
                return Result.error("餐厅不存在");
            }
            return Result.OK(vo);
        } catch (Exception e) {
            log.error("查询餐厅详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增餐厅
     */
    @AutoLog(value = "餐厅信息-新增")
    @PostMapping("/add")
    @Operation(summary = "新增餐厅")
    public Result<PosRestaurantVO> add(@RequestBody PosRestaurantVO vo) {
        try {
            String operator = getOperator();
            PosRestaurantVO saved = restaurantService.saveVO(vo, operator);
            return Result.OK(saved);
        } catch (RuntimeException e) {
            log.warn("新增餐厅失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("新增餐厅失败", e);
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    /**
     * 编辑餐厅信息
     */
    @AutoLog(value = "餐厅信息-编辑")
    @PutMapping("/edit")
    @Operation(summary = "编辑餐厅信息")
    public Result<PosRestaurantVO> edit(@RequestBody PosRestaurantVO vo) {
        try {
            String operator = getOperator();
            PosRestaurantVO updated = restaurantService.updateVO(vo, operator);
            return Result.OK(updated);
        } catch (RuntimeException e) {
            log.warn("编辑餐厅失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("编辑餐厅失败", e);
            return Result.error("编辑失败: " + e.getMessage());
        }
    }

    /**
     * 删除餐厅
     */
    @AutoLog(value = "餐厅信息-删除")
    @DeleteMapping("/delete")
    @Operation(summary = "删除餐厅")
    public Result<String> delete(@RequestParam String id) {
        try {
            boolean ok = restaurantService.deleteById(id);
            return ok ? Result.OK("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除餐厅失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除餐厅
     */
    @AutoLog(value = "餐厅信息-批量删除")
    @DeleteMapping("/deleteBatch")
    @Operation(summary = "批量删除餐厅")
    public Result<?> deleteBatch(@RequestParam String ids) {
        try {
            String[] idArray = ids.split(",");
            boolean ok = restaurantService.deleteBatchByIds(idArray);
            return ok ? Result.OK("批量删除成功") : Result.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除餐厅失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 检查餐厅编码是否重复
     */
    @AutoLog(value = "餐厅信息-检查餐厅编码")
    @GetMapping("/checkCode")
    @Operation(summary = "检查餐厅编码是否重复")
    public Result<Boolean> checkCode(@RequestParam String restaurantCode, @RequestParam(required = false) String excludeId) {
        try {
            boolean isDuplicate = restaurantService.checkRestaurantCodeDuplicate(restaurantCode, excludeId);
            return Result.OK(!isDuplicate); // 返回true表示可以使用，false表示重复
        } catch (Exception e) {
            log.error("检查餐厅编码失败", e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有餐厅列表
     */
    @AutoLog(value = "餐厅信息-获取所有餐厅列表")
    @GetMapping("/getAllList")
    @Operation(summary = "获取所有餐厅列表")
    public Result<List<PosRestaurantVO>> getAllList() {
        try {
            List<PosRestaurantVO> list = restaurantService.getAllRestaurantList();
            return Result.OK(list);
        } catch (Exception e) {
            log.error("获取餐厅列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前操作用户
     */
    private String getOperator() {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        return sysUser != null ? sysUser.getUsername() : "system";
    }
}
