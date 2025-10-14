package org.jeecg.modules.acc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.acc.entity.AccGroup;
import org.jeecg.modules.acc.service.IAccGroupService;
import org.jeecg.modules.acc.vo.AccGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "ACC-门禁权限组管理")
@RestController
@RequestMapping("/acc/accgroup")
@Slf4j
public class AccGroupController extends JeecgController<AccGroup, IAccGroupService> {

    @Autowired
    private IAccGroupService accGroupService;

    /**
     * 分页查询权限组
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询权限组")
    public Result<IPage<AccGroupVO>> list(@RequestParam(name = "groupName", required = false) String groupName,
                                          @RequestParam(name = "memberCount", required = false) Integer memberCount,
                                          @RequestParam(name = "deviceCount", required = false) Integer deviceCount,
                                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                          HttpServletRequest req) {
        IPage<AccGroupVO> page = accGroupService.pageList(groupName, memberCount, deviceCount, pageNo, pageSize);
        return Result.OK(page);
    }

    /**
     * 查询详情（含成员与设备ID）
     */
    @GetMapping("/detail")
    @Operation(summary = "查询权限组详情")
    public Result<AccGroupVO> detail(@RequestParam String id) {
        AccGroupVO vo = accGroupService.getDetailById(id);
        return vo == null ? Result.error("权限组不存在") : Result.OK(vo);
    }

    /**
     * 新增权限组
     */
    @PostMapping("/add")
    @Operation(summary = "新增权限组")
    public Result<AccGroupVO> add(@RequestBody AccGroupVO vo) {
        String operator = getOperator();
        AccGroupVO saved = accGroupService.saveVO(vo, operator);
        return Result.OK(saved);
    }

    /**
     * 编辑权限组
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑权限组")
    public Result<AccGroupVO> edit(@RequestBody AccGroupVO vo) {
        String operator = getOperator();
        AccGroupVO updated = accGroupService.updateVO(vo, operator);
        return Result.OK(updated);
    }

    /**
     * 删除权限组
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除权限组")
    public Result<String> delete(@RequestParam String id) {
        boolean ok = accGroupService.deleteWithRelations(id);
        return ok ? Result.OK("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除权限组
     */
    @DeleteMapping("/deleteBatch")
    @Operation(summary = "批量删除权限组")
    public Result<String> deleteBatch(@RequestParam String ids) {
        String[] idArr = ids.split(",");
        boolean ok = accGroupService.deleteBatchWithRelations(idArr);
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