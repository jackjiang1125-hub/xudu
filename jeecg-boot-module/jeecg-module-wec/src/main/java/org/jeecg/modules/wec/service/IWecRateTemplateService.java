package org.jeecg.modules.wec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.wec.entity.WecRateTemplate;
import org.jeecg.modules.wec.vo.WecRateTemplateVO;

import java.util.List;

public interface IWecRateTemplateService extends JeecgService<WecRateTemplate> {
    List<WecRateTemplateVO> toVOList(List<WecRateTemplate> list);
    IPage<WecRateTemplateVO> toVOPage(IPage<WecRateTemplate> page);
}
