package org.jeecg.modules.wec.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.wec.entity.WecRateTemplate;
import org.jeecg.modules.wec.mapper.WecRateTemplateMapper;
import org.jeecg.modules.wec.service.IWecRateTemplateService;
import org.jeecg.modules.wec.vo.WecRateTemplateVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WecRateTemplateServiceImpl extends JeecgServiceImpl<WecRateTemplateMapper, WecRateTemplate> implements IWecRateTemplateService {

    @Override
    public List<WecRateTemplateVO> toVOList(List<WecRateTemplate> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        List<WecRateTemplateVO> result = new ArrayList<>(list.size());
        for (WecRateTemplate e : list) {
            WecRateTemplateVO vo = new WecRateTemplateVO();
            vo.setId(e.getId());
            vo.setTemplateName(clean(e.getTemplateName()));
            vo.setType(clean(e.getType()));
            vo.setFreeSeconds(e.getFreeSeconds());
            vo.setWorkMode(clean(e.getWorkMode()));
            vo.setDeductionMethod(clean(e.getDeductionMethod()));
            vo.setRealTimeAmount(e.getRealTimeAmount());
            vo.setRealTimeDuration(e.getRealTimeDuration());
            vo.setPreDeductTime(e.getPreDeductTime());
            vo.setPreDeductRate(e.getPreDeductRate());
            vo.setPreDeductAmount(e.getPreDeductAmount());
            vo.setPerTimeDuration(e.getPerTimeDuration());
            result.add(vo);
        }
        return result;
    }

    @Override
    public IPage<WecRateTemplateVO> toVOPage(IPage<WecRateTemplate> page) {
        List<WecRateTemplateVO> records = toVOList(page.getRecords());
        Page<WecRateTemplateVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    private String clean(String s) {
        if (s == null) return null;
        return s.replace("*", "").trim();
    }
}
