package org.jeecg.modules.wec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.wec.entity.WecLocation;
import org.jeecg.modules.wec.vo.WecLocationVO;

import java.util.List;

public interface IWecLocationService extends JeecgService<WecLocation> {
    List<WecLocationVO> toVOList(List<WecLocation> list);
    IPage<WecLocationVO> toVOPage(IPage<WecLocation> page);
}

