package org.jeecg.modules.pos.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.pos.entity.PosProductCategory;
import org.jeecg.modules.pos.service.IPosProductCategoryService;
import org.jeecg.modules.pos.vo.PosProductCategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 商品分类Controller
 */
@Tag(name = "POS-商品分类管理")
@RestController
@RequestMapping("/pos/productCategory")
@Slf4j
public class PosProductCategoryController extends JeecgController<PosProductCategory, IPosProductCategoryService> {

    @Autowired
    private IPosProductCategoryService productCategoryService;

    /**
     * 分页查询商品分类
     */
    @AutoLog(value = "商品分类-分页列表查询")
    @GetMapping("/list")
    @Operation(summary = "分页查询商品分类")
    public Result<IPage<PosProductCategoryVO>> list(@RequestParam(name = "categoryName", required = false) String categoryName,
                                                  @RequestParam(name = "categoryCode", required = false) String categoryCode,
                                                  @RequestParam(name = "status", required = false) String status,
                                                  @RequestParam(name = "createTimeStart", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date createTimeStart,
                                                  @RequestParam(name = "createTimeEnd", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date createTimeEnd,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            IPage<PosProductCategoryVO> page = productCategoryService.pageList(categoryName, categoryCode, status,
                    createTimeStart, createTimeEnd, pageNo, pageSize);
            return Result.OK(page);
        } catch (Exception e) {
            log.error("查询商品分类列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询详情
     */
    @AutoLog(value = "商品分类-查询详情")
    @GetMapping("/detail")
    @Operation(summary = "查询商品分类详情")
    public Result<PosProductCategoryVO> detail(@RequestParam String id) {
        try {
            PosProductCategoryVO vo = productCategoryService.getDetailById(id);
            if (vo == null) {
                return Result.error("商品分类不存在");
            }
            return Result.OK(vo);
        } catch (Exception e) {
            log.error("查询商品分类详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增商品分类
     */
    @AutoLog(value = "商品分类-新增")
    @PostMapping("/add")
    @Operation(summary = "新增商品分类")
    public Result<PosProductCategoryVO> add(@RequestBody PosProductCategoryVO vo) {
        try {
            String operator = getOperator();
            PosProductCategoryVO saved = productCategoryService.saveVO(vo, operator);
            return Result.OK(saved);
        } catch (RuntimeException e) {
            log.warn("新增商品分类失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("新增商品分类失败", e);
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    /**
     * 编辑商品分类
     */
    @AutoLog(value = "商品分类-编辑")
    @PutMapping("/edit")
    @Operation(summary = "编辑商品分类")
    public Result<PosProductCategoryVO> edit(@RequestBody PosProductCategoryVO vo) {
        try {
            String operator = getOperator();
            PosProductCategoryVO updated = productCategoryService.updateVO(vo, operator);
            return Result.OK(updated);
        } catch (RuntimeException e) {
            log.warn("编辑商品分类失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("编辑商品分类失败", e);
            return Result.error("编辑失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品分类
     */
    @AutoLog(value = "商品分类-删除")
    @DeleteMapping("/delete")
    @Operation(summary = "删除商品分类")
    public Result<String> delete(@RequestParam String id) {
        try {
            boolean ok = productCategoryService.deleteById(id);
            return ok ? Result.OK("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除商品分类失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除商品分类
     */
    @AutoLog(value = "商品分类-批量删除")
    @DeleteMapping("/deleteBatch")
    @Operation(summary = "批量删除商品分类")
    public Result<String> deleteBatch(@RequestParam String ids) {
        try {
            String[] idArr = ids.split(",");
            boolean ok = productCategoryService.deleteBatchByIds(idArr);
            return ok ? Result.OK("批量删除成功") : Result.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除商品分类失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 检查分类编号是否重复
     */
    @AutoLog(value = "商品分类-检查分类编号")
    @GetMapping("/checkCode")
    @Operation(summary = "检查分类编号是否重复")
    public Result<Boolean> checkCode(@RequestParam String categoryCode, @RequestParam(required = false) String excludeId) {
        try {
            boolean isDuplicate = productCategoryService.checkCategoryCodeDuplicate(categoryCode, excludeId);
            return Result.OK(!isDuplicate); // 返回true表示可以使用，false表示重复
        } catch (Exception e) {
            log.error("检查分类编号失败", e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的商品分类列表（用于下拉选择）
     */
    @AutoLog(value = "商品分类-获取启用列表")
    @GetMapping("/getEnabledList")
    @Operation(summary = "获取启用的商品分类列表")
    public Result<List<PosProductCategoryVO>> getEnabledList() {
        try {
            List<PosProductCategoryVO> list = productCategoryService.getEnabledCategoryList();
            return Result.OK(list);
        } catch (Exception e) {
            log.error("获取启用商品分类列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前操作人
     */
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