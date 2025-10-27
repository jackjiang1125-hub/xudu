package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.acc.entity.AccTimePeriod;
import org.jeecg.modules.acc.entity.AccTimePeriodDetail;
import org.jeecg.modules.acc.mapper.AccTimePeriodMapper;
import org.jeecg.modules.acc.mapper.AccTimePeriodDetailMapper;
import org.jeecg.modules.acc.service.IAccTimePeriodService;
import org.jeecg.modules.acc.vo.TimeIntervalVO;
import org.jeecg.modules.acc.vo.TimePeriodDetailVO;
import org.jeecg.modules.acc.vo.TimePeriodVO;
import org.jeecg.modules.acc.entity.AccDevice;
import org.jeecg.modules.acc.mapper.AccDeviceMapper;
import org.jeecgframework.boot.iot.api.IotDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccTimePeriodServiceImpl extends JeecgServiceImpl<AccTimePeriodMapper, AccTimePeriod>
        implements IAccTimePeriodService {

    @Autowired
    private AccTimePeriodDetailMapper detailMapper;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private AccDeviceMapper accDeviceMapper;


    private static final SimpleDateFormat TS_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final SimpleDateFormat FULL_TS_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public IPage<TimePeriodVO> pageList(String name, String creator, String updatedBegin, String updatedEnd,
                                        Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<AccTimePeriod> qw = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            qw.like(AccTimePeriod::getName, name);
        }
        if (creator != null && !creator.isBlank()) {
            // 前端展示“创建人/更新人”，此处以更新人为准
            qw.like(AccTimePeriod::getUpdateBy, creator).or().like(AccTimePeriod::getCreateBy, creator);
        }
        if (updatedBegin != null && !updatedBegin.isBlank()) {
            try {
                Date begin = FULL_TS_FMT.parse(updatedBegin + " 00:00:00");
                qw.ge(AccTimePeriod::getUpdateTime, begin);
            } catch (ParseException e) {
                log.warn("解析开始时间失败: {}", updatedBegin, e);
            }
        }
        if (updatedEnd != null && !updatedEnd.isBlank()) {
            try {
                Date end = FULL_TS_FMT.parse(updatedEnd + " 23:59:59");
                qw.le(AccTimePeriod::getUpdateTime, end);
            } catch (ParseException e) {
                log.warn("解析结束时间失败: {}", updatedEnd, e);
            }
        }
        // 先按排序字段升序，再按更新时间降序
        qw.orderByAsc(AccTimePeriod::getSortOrder).orderByDesc(AccTimePeriod::getUpdateTime);

        Page<AccTimePeriod> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
        IPage<AccTimePeriod> entityPage = this.page(page, qw);

        List<TimePeriodVO> voRecords = entityPage.getRecords().stream().map(this::toVOWithoutDetail)
                .collect(Collectors.toList());
        Page<TimePeriodVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voRecords);
        return voPage;
    }

    @Override
    public TimePeriodVO getDetailById(String id) {
        AccTimePeriod period = this.getById(id);
        if (period == null) return null;
        List<AccTimePeriodDetail> details = detailMapper.selectList(new LambdaQueryWrapper<AccTimePeriodDetail>()
                .eq(AccTimePeriodDetail::getPeriodId, id));
        return toVOWithDetail(period, details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TimePeriodVO saveVO(TimePeriodVO vo, String operator) {
        Objects.requireNonNull(vo, "vo");
        AccTimePeriod entity = new AccTimePeriod();
        entity.setName(vo.getName());
        entity.setRemark(vo.getRemark());
        // 排序：从1开始，按当前总数递增
        long total = this.count() + 1;
        try {
            entity.setSortOrder((int) total);
        } catch (Exception e) {
            log.warn("计算排序值失败，使用默认1", e);
            entity.setSortOrder(1);
        }
        entity.setCreateBy(operator);
        entity.setCreateTime(new Date());
        entity.setUpdateBy(operator);
        entity.setUpdateTime(new Date());
        this.save(entity);

        // 用新生成的ID保存详情
        String periodId = entity.getId();
        saveDetails(periodId, vo.getDetail());
        pushTimezoneToAllDevices((int) total);
        return getDetailById(periodId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TimePeriodVO updateVO(TimePeriodVO vo, String operator) {
        Objects.requireNonNull(vo, "vo");
        if (vo.getId() == null || vo.getId().isBlank()) {
            throw new IllegalArgumentException("缺少ID");
        }
        AccTimePeriod entity = this.getById(vo.getId());
        if (entity == null) {
            throw new IllegalArgumentException("时间段不存在");
        }
        entity.setName(vo.getName());
        entity.setRemark(vo.getRemark());
        entity.setUpdateBy(operator);
        entity.setUpdateTime(new Date());
        this.updateById(entity);

        // 先删后增详情
        detailMapper.delete(new LambdaQueryWrapper<AccTimePeriodDetail>().eq(AccTimePeriodDetail::getPeriodId, entity.getId()));
        saveDetails(entity.getId(), vo.getDetail());
        pushTimezoneToAllDevices(entity.getSortOrder());
        return getDetailById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithDetails(String id) {
        if (id == null || id.isBlank()) return false;
        detailMapper.delete(new LambdaQueryWrapper<AccTimePeriodDetail>().eq(AccTimePeriodDetail::getPeriodId, id));
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatchWithDetails(String[] ids) {
        if (ids == null || ids.length == 0) return false;
        for (String id : ids) {
            detailMapper.delete(new LambdaQueryWrapper<AccTimePeriodDetail>().eq(AccTimePeriodDetail::getPeriodId, id));
        }
        return this.removeByIds(List.of(ids));
    }

    private void saveDetails(String periodId, List<TimePeriodDetailVO> detailVOs) {
        if (detailVOs == null) return;
        List<AccTimePeriodDetail> toSave = new ArrayList<>();
        for (TimePeriodDetailVO dvo : detailVOs) {
            AccTimePeriodDetail d = new AccTimePeriodDetail();
            d.setPeriodId(periodId);
            d.setDayKey(dvo.getKey());
            d.setLabel(dvo.getLabel());
            List<TimeIntervalVO> segs = dvo.getSegments();
            if (segs != null && segs.size() >= 1) {
                d.setSegment1Start(segs.get(0).getStart());
                d.setSegment1End(segs.get(0).getEnd());
            }
            if (segs != null && segs.size() >= 2) {
                d.setSegment2Start(segs.get(1).getStart());
                d.setSegment2End(segs.get(1).getEnd());
            }
            if (segs != null && segs.size() >= 3) {
                d.setSegment3Start(segs.get(2).getStart());
                d.setSegment3End(segs.get(2).getEnd());
            }
            toSave.add(d);
        }
        for (AccTimePeriodDetail d : toSave) {
            detailMapper.insert(d);
        }
    }

    private TimePeriodVO toVOWithoutDetail(AccTimePeriod entity) {
        TimePeriodVO vo = new TimePeriodVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setRemark(entity.getRemark());
        vo.setSortOrder(entity.getSortOrder());
        // 更新时间优先，其次创建时间
        Date ts = entity.getUpdateTime() != null ? entity.getUpdateTime() : entity.getCreateTime();
        vo.setUpdatedAt(ts == null ? null : TS_FMT.format(ts));
        vo.setCreator(entity.getUpdateBy() != null ? entity.getUpdateBy() : entity.getCreateBy());
        return vo;
    }

    private TimePeriodVO toVOWithDetail(AccTimePeriod entity, List<AccTimePeriodDetail> details) {
        TimePeriodVO vo = toVOWithoutDetail(entity);
        List<TimePeriodDetailVO> list = new ArrayList<>();
        for (AccTimePeriodDetail d : details) {
            TimePeriodDetailVO dvo = new TimePeriodDetailVO();
            dvo.setKey(d.getDayKey());
            dvo.setLabel(d.getLabel());
            List<TimeIntervalVO> segs = new ArrayList<>(3);
            segs.add(buildSeg(d.getSegment1Start(), d.getSegment1End()));
            segs.add(buildSeg(d.getSegment2Start(), d.getSegment2End()));
            segs.add(buildSeg(d.getSegment3Start(), d.getSegment3End()));
            dvo.setSegments(segs);
            list.add(dvo);
        }
        vo.setDetail(list);
        return vo;
    }

    private TimeIntervalVO buildSeg(String start, String end) {
        TimeIntervalVO seg = new TimeIntervalVO();
        seg.setStart(start == null ? "00:00" : start);
        seg.setEnd(end == null ? "00:00" : end);
        return seg;
    }

    /**
     * 将 HH:mm 文本转为设备时区整数（HHmm）。例如 "23:59" -> 2359, "00:00" -> 0。
     */
    private int toHHmmInt(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return 0;
        String s = hhmm.trim();
        if ("00:00".equals(s)) return 0;
        try {
            String digits = s.replace(":", "");
            // 去掉可能的前导0，例如 "0830" -> 830
            return Integer.parseInt(digits);
        } catch (Exception e) {
            log.warn("解析时区时间失败: {}", hhmm, e);
            return 0;
        }
    }

    /**
     * 根据时间段序号（sortOrder）构建设备时区参数
     */
    @Override
    public LinkedHashMap<String, Object> buildTimezoneParamsByOrder(int order) {
        // 查找对应的时间段记录
        AccTimePeriod period = this.getOne(new LambdaQueryWrapper<AccTimePeriod>()
                .eq(AccTimePeriod::getSortOrder, order)
                .last("limit 1"), false);
        if (period == null) {
            throw new IllegalArgumentException("未找到序号对应的时间段: " + order);
        }
        List<AccTimePeriodDetail> details = detailMapper.selectList(new LambdaQueryWrapper<AccTimePeriodDetail>()
                .eq(AccTimePeriodDetail::getPeriodId, period.getId()));

        // 将 details 便于按 dayKey 索引
        Map<String, AccTimePeriodDetail> byKey = new HashMap<>();
        for (AccTimePeriodDetail d : details) {
            byKey.put(d.getDayKey(), d);
        }

        LinkedHashMap<String, Object> tzBasic = new LinkedHashMap<>();
        tzBasic.put("TimeZoneId", order);

        // 映射每周与节假日键
        fillDay(tzBasic, "Sun", byKey.get("sun"));
        fillDay(tzBasic, "Mon", byKey.get("mon"));
        fillDay(tzBasic, "Tue", byKey.get("tue"));
        fillDay(tzBasic, "Wed", byKey.get("wed"));
        fillDay(tzBasic, "Thu", byKey.get("thu"));
        fillDay(tzBasic, "Fri", byKey.get("fri"));
        fillDay(tzBasic, "Sat", byKey.get("sat"));
        fillDay(tzBasic, "Hol1", byKey.get("holiday1"));
        fillDay(tzBasic, "Hol2", byKey.get("holiday2"));
        fillDay(tzBasic, "Hol3", byKey.get("holiday3"));

        return tzBasic;
    }

    /**
     * 将某天的三个时间段填充到参数中
     */
    private void fillDay(java.util.Map<String, Object> tzBasic, String prefix, AccTimePeriodDetail d) {
        String k1 = prefix + "Time1";
        String k2 = prefix + "Time2";
        String k3 = prefix + "Time3";
        if (d == null) {
            tzBasic.put(k1, 0);
            tzBasic.put(k2, 0);
            tzBasic.put(k3, 0);
            return;
        }
        tzBasic.put(k1, toHHmmInt(d.getSegment1End()));
        tzBasic.put(k2, toHHmmInt(d.getSegment2End()));
        tzBasic.put(k3, toHHmmInt(d.getSegment3End()));
    }

    /**
     * 构建并下发设备时区设置
     */
    @Override
    public void pushTimezoneByOrder(String sn, int order) {
        if (sn == null || sn.isBlank()) {
            log.warn("下发设备时区失败：SN为空");
            return;
        }
        LinkedHashMap<String, Object> params;
        try {
            params = buildTimezoneParamsByOrder(order);
        } catch (Exception e) {
            log.error("构建设备时区参数失败，order={}", order, e);
            return;
        }
        iotDeviceService.updateTimezone(sn, params);
        log.info("已下发设备时区：sn={}, order={}", sn, order);
    }

    // 新增一个异步方法，获取所有设备列表，依次下发最新时间段信息
    @Async
    public void pushTimezoneToAllDevices(int order) {
        List<AccDevice> devices = accDeviceMapper.selectList(new LambdaQueryWrapper<AccDevice>());
        for (AccDevice device : devices) {
            if (device != null && device.getSn() != null && !device.getSn().isBlank()) {
                pushTimezoneByOrder(device.getSn(), order);
            }
        }
    }

}