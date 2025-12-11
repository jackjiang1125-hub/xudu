package org.jeecg.modules.wec.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.wec.entity.WecUser;
import org.jeecg.modules.wec.service.IWecUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wec/user")
@Slf4j
public class WecUserController extends JeecgController<WecUser, IWecUserService> {
    @Autowired
    private IWecUserService wecUserService;

    /**
     * 分页列表查询
     *
     * @param wecUser
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "水控用户-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(WecUser wecUser,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<WecUser> queryWrapper = QueryGenerator.initQueryWrapper(wecUser, req.getParameterMap());
        Page<WecUser> page = new Page<>(pageNo, pageSize);
        IPage<WecUser> pageList = wecUserService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param wecUser
     * @return
     */
    @AutoLog(value = "水控用户-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody WecUser wecUser) {
        wecUserService.save(wecUser);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param wecUser
     * @return
     */
    @AutoLog(value = "水控用户-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody WecUser wecUser) {
        wecUserService.updateById(wecUser);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "水控用户-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        wecUserService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "水控用户-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.wecUserService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 获取统计数据
     */
    @GetMapping(value = "/statistics")
    public Result<?> getStatistics(HttpServletRequest req) {
        String sysOrgCode = ""; // Temporarily query all data
        Map<String, Object> stats = wecUserService.getStatistics(sysOrgCode);
        return Result.OK(stats);
    }

    /**
     * 从系统用户添加
     */
    @PostMapping(value = "/addFromSystem")
    public Result<?> addFromSystem(@RequestBody Map<String, Object> params) {
        List<String> userIds = (List<String>) params.get("userIds");
        String userType = (String) params.get("userType");
        wecUserService.addSystemUsers(userIds, userType);
        return Result.OK("添加成功");
    }
}
