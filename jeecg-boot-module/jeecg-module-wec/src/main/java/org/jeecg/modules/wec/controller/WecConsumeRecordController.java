package org.jeecg.modules.wec.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.wec.entity.WecConsumeRecord;
import org.jeecg.modules.wec.service.IWecConsumeRecordService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 消费记录
 * @Author: jeecg-boot
 * @Date:   2025-12-07
 * @Version: V1.0
 */
@RestController
@RequestMapping("/wec/consumeRecord")
@Slf4j
public class WecConsumeRecordController extends JeecgController<WecConsumeRecord, IWecConsumeRecordService> {
	@Autowired
	private IWecConsumeRecordService wecConsumeRecordService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wecConsumeRecord
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "消费记录-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(WecConsumeRecord wecConsumeRecord,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<WecConsumeRecord> queryWrapper = QueryGenerator.initQueryWrapper(wecConsumeRecord, req.getParameterMap());
		queryWrapper.orderByDesc("consume_time");
		Page<WecConsumeRecord> page = new Page<WecConsumeRecord>(pageNo, pageSize);
		IPage<WecConsumeRecord> pageList = wecConsumeRecordService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wecConsumeRecord
	 * @return
	 */
	@AutoLog(value = "消费记录-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody WecConsumeRecord wecConsumeRecord) {
		wecConsumeRecordService.save(wecConsumeRecord);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wecConsumeRecord
	 * @return
	 */
	@AutoLog(value = "消费记录-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody WecConsumeRecord wecConsumeRecord) {
		wecConsumeRecordService.updateById(wecConsumeRecord);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "消费记录-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		wecConsumeRecordService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "消费记录-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wecConsumeRecordService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "消费记录-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		WecConsumeRecord wecConsumeRecord = wecConsumeRecordService.getById(id);
		if(wecConsumeRecord==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(wecConsumeRecord);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wecConsumeRecord
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WecConsumeRecord wecConsumeRecord) {
        return super.exportXls(request, wecConsumeRecord, WecConsumeRecord.class, "消费记录");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, WecConsumeRecord.class);
    }

}
