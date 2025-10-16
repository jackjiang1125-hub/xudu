package org.jeecg.modules.acc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.acc.entity.AccGroupMember;
import org.jeecg.modules.acc.service.IAccGroupMemberService;
import org.jeecg.modules.acc.vo.AccMemberVO;
import org.jeecg.modules.acc.vo.BindIdsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "ACC-权限组成员管理")
@RestController
@RequestMapping("/acc/accgroupmember")
@Slf4j
public class AccGroupMemberController extends JeecgController<AccGroupMember, IAccGroupMemberService> {

    @Autowired
    private IAccGroupMemberService accGroupMemberService;

    /**
     * 根据权限组ID查询成员列表
     */
    @GetMapping("/listByGroup")
    @Operation(summary = "根据权限组ID查询成员列表")
    public Result<IPage<AccMemberVO>> listByGroup(@RequestParam String groupId,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  HttpServletRequest req) {
        IPage<AccMemberVO> page = accGroupMemberService.listMembersByGroupId(groupId, pageNo, pageSize);
        return Result.OK(page);
    }

    /**
     * 根据权限组ID查询所有成员ID列表
     */
    @GetMapping("/listIdsByGroup")
    @Operation(summary = "根据权限组ID查询所有成员ID列表")
    public Result<java.util.List<String>> listIdsByGroup(@RequestParam String groupId) {
        java.util.List<String> memberIds = accGroupMemberService.listMemberIdsByGroupId(groupId);
        return Result.OK(memberIds);
    }

    /**
     * 批量添加成员到权限组
     */
    @PostMapping("/addMembers")
    @Operation(summary = "批量添加成员到权限组")
    public Result<String> addMembers(@RequestBody BindIdsRequest request) {
        accGroupMemberService.addMembers(request.getGroupId(), request.getIds());
        return Result.OK("添加成功");
    }

    /**
     * 批量从权限组移除成员
     */
    @PostMapping("/removeMembers")
    @Operation(summary = "批量从权限组移除成员")
    public Result<String> removeMembers(@RequestBody BindIdsRequest request) {
        accGroupMemberService.removeMembers(request.getGroupId(), request.getIds());
        return Result.OK("移除成功");
    }
}