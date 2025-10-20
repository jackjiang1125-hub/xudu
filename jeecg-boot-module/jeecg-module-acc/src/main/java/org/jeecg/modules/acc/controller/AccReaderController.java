package org.jeecg.modules.acc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.acc.entity.AccReader;
import org.jeecg.modules.acc.mapstruct.AccReaderMapstruct;
import org.jeecg.modules.acc.service.IAccReaderService;
import org.jeecg.modules.acc.vo.AccReaderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description: 读头管理
 * @Author: jeecg-boot
 * @Date: 2025-01-26
 * @Version: V1.0
 */
@Tag(name = "读头管理", description = "读头管理")
@RestController
@RequestMapping("/acc/reader")
@Slf4j
public class AccReaderController extends JeecgController<AccReader, IAccReaderService> {

    @Autowired
    private IAccReaderService accReaderService;

    /**
     * 分页列表查询
     */
    @AutoLog(value = "读头管理-分页列表查询")
    @Operation(summary = "读头管理-分页列表查询", description = "读头管理-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AccReaderVO>> queryPageList(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "readerId", required = false) String readerId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "doorName", required = false) String doorName,
            HttpServletRequest req) {
        
        Page<AccReader> page = new Page<>(pageNo, pageSize);
        IPage<AccReader> pageList = accReaderService.queryPageList(page, readerId, name, doorName);
        
        // 转换为VO
        IPage<AccReaderVO> result = pageList.convert(AccReaderMapstruct.INSTANCE::toVO);
        
        return Result.OK(result);
    }

    /**
     * 通过id查询
     */
    @AutoLog(value = "读头管理-通过id查询")
    @Operation(summary = "读头管理-通过id查询", description = "读头管理-通过id查询")
    @GetMapping(value = "/detail")
    public Result<AccReaderVO> queryById(@RequestParam(name = "id") String id) {
        AccReader accReader = accReaderService.getById(id);
        if (accReader == null) {
            return Result.error("未找到对应数据");
        }
        AccReaderVO vo = AccReaderMapstruct.INSTANCE.toVO(accReader);
        return Result.OK(vo);
    }

    /**
     * 添加
     */
    @AutoLog(value = "读头管理-添加")
    @Operation(summary = "读头管理-添加", description = "读头管理-添加")
    @RequiresPermissions("acc:reader:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody @Validated AccReaderVO accReaderVO) {
        boolean success = accReaderService.saveFromVO(accReaderVO);
        if (success) {
            return Result.OK("添加成功！");
        } else {
            return Result.error("添加失败！");
        }
    }

    /**
     * 编辑
     */
    @AutoLog(value = "读头管理-编辑")
    @Operation(summary = "读头管理-编辑", description = "读头管理-编辑")
    @RequiresPermissions("acc:reader:edit")
    @PutMapping(value = "/edit")
    public Result<String> edit(@RequestBody @Validated AccReaderVO accReaderVO) {
        boolean success = accReaderService.saveFromVO(accReaderVO);
        if (success) {
            return Result.OK("编辑成功！");
        } else {
            return Result.error("编辑失败！");
        }
    }

    /**
     * 通过id删除
     */
    @AutoLog(value = "读头管理-通过id删除")
    @Operation(summary = "读头管理-通过id删除", description = "读头管理-通过id删除")
    @RequiresPermissions("acc:reader:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        boolean success = accReaderService.removeById(id);
        if (success) {
            return Result.OK("删除成功!");
        } else {
            return Result.error("删除失败!");
        }
    }

    /**
     * 批量删除
     */
    @AutoLog(value = "读头管理-批量删除")
    @Operation(summary = "读头管理-批量删除", description = "读头管理-批量删除")
    @RequiresPermissions("acc:reader:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        boolean success = accReaderService.removeByIds(Arrays.asList(ids.split(",")));
        if (success) {
            return Result.OK("批量删除成功!");
        } else {
            return Result.error("批量删除失败!");
        }
    }
}