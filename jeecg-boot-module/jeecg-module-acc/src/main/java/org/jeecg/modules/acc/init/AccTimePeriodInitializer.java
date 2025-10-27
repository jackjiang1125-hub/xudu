package org.jeecg.modules.acc.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.acc.entity.AccTimePeriod;
import org.jeecg.modules.acc.service.IAccTimePeriodService;
import org.jeecg.modules.acc.vo.TimeIntervalVO;
import org.jeecg.modules.acc.vo.TimePeriodDetailVO;
import org.jeecg.modules.acc.vo.TimePeriodVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * 项目启动初始化：自动检查并创建“24小时通行”的门禁时间段配置
 */
@Slf4j
@Component
@Order(10)
public class AccTimePeriodInitializer implements ApplicationRunner {

    private static final String DEFAULT_NAME = "24小时通行";
    private static final String DEFAULT_REMARK = "系统初始化创建（自动生成）";

    // day_key 与显示标签
    private static final String[] DAY_KEYS = {
        "mon", "tue", "wed", "thu", "fri", "sat", "sun",
        "holiday1", "holiday2", "holiday3"
    };
    private static final String[] DAY_LABELS = {
        "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日",
        "假日类型1", "假日类型2", "假日类型3"
    };

    private static final Pattern TIME_FMT = Pattern.compile("^(?:[01]\\d|2[0-3]):[0-5]\\d$");

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final ReentrantLock INIT_LOCK = new ReentrantLock();

    @Autowired
    private IAccTimePeriodService timePeriodService;

    @Override
    public void run(ApplicationArguments args) {
        // 防止同一 JVM 内多次执行
        if (!INITIALIZED.compareAndSet(false, true)) {
            log.info("[AccTimePeriodInitializer] 已执行过，跳过本次初始化。");
            return;
        }

        INIT_LOCK.lock();
        try {
            // 1) 检查是否已存在
            AccTimePeriod existing = timePeriodService.getOne(
                new LambdaQueryWrapper<AccTimePeriod>().eq(AccTimePeriod::getName, DEFAULT_NAME)
            );
            if (existing != null) {
                log.info("[AccTimePeriodInitializer] 已存在时间段 '{}', id={}，无需初始化。", DEFAULT_NAME, existing.getId());
                return;
            }

            // 2) 构建 VO 并校验
            TimePeriodVO vo = buildAllDayVO();
            String err = validate(vo);
            if (err != null) {
                log.error("[AccTimePeriodInitializer] 校验失败: {}", err);
                return;
            }

            // 3) 保存（幂等：name 唯一约束）
            try {
                TimePeriodVO saved = timePeriodService.saveVO(vo, "system");
                log.info("[AccTimePeriodInitializer] 已创建默认时间段 '{}', 新ID={}", DEFAULT_NAME, saved.getId());
            } catch (Exception e) {
                // 可能因并发导致唯一约束冲突
                log.warn("[AccTimePeriodInitializer] 创建时间段时出现异常（可能为并发导致重复）：{}", e.getMessage());
            }
        } finally {
            INIT_LOCK.unlock();
        }
    }

    private TimePeriodVO buildAllDayVO() {
        TimePeriodVO vo = new TimePeriodVO();
        vo.setName(DEFAULT_NAME);
        vo.setRemark(DEFAULT_REMARK);
        List<TimePeriodDetailVO> details = new ArrayList<>(DAY_KEYS.length);
        for (int i = 0; i < DAY_KEYS.length; i++) {
            details.add(buildDayDetail(DAY_KEYS[i], DAY_LABELS[i]));
        }
        vo.setDetail(details);
        return vo;
    }

    private TimePeriodDetailVO buildDayDetail(String key, String label) {
        TimePeriodDetailVO d = new TimePeriodDetailVO();
        d.setKey(key);
        d.setLabel(label);
        List<TimeIntervalVO> segs = new ArrayList<>(3);
        // 区间1：00:00-23:59；区间2、3：默认关闭 00:00-00:00
        segs.add(buildSeg("00:00", "23:59"));
        segs.add(buildSeg("00:00", "00:00"));
        segs.add(buildSeg("00:00", "00:00"));
        d.setSegments(segs);
        return d;
    }

    private TimeIntervalVO buildSeg(String start, String end) {
        TimeIntervalVO seg = new TimeIntervalVO();
        seg.setStart(start);
        seg.setEnd(end);
        return seg;
    }

    /**
     * 简单校验：
     * - 名称与详情不能为空
     * - day_key 必须在允许集合内
     * - 每个时间段字符串必须为 HH:mm
     */
    private String validate(TimePeriodVO vo) {
        if (vo == null) return "VO 为空";
        if (vo.getName() == null || vo.getName().isBlank()) return "名称为空";
        if (vo.getDetail() == null || vo.getDetail().isEmpty()) return "详情为空";
        List<String> allowed = Arrays.asList(DAY_KEYS);
        for (TimePeriodDetailVO d : vo.getDetail()) {
            if (!allowed.contains(d.getKey())) return "非法 day_key: " + d.getKey();
            if (d.getSegments() == null || d.getSegments().size() != 3) return "day_key=" + d.getKey() + " 段数量不为3";
            for (TimeIntervalVO seg : d.getSegments()) {
                if (!isValidTime(seg.getStart()) || !isValidTime(seg.getEnd())) {
                    return "非法时间段格式: " + seg.getStart() + "-" + seg.getEnd();
                }
            }
        }
        return null;
    }

    private boolean isValidTime(String s) {
        return s != null && TIME_FMT.matcher(s).matches();
    }
}