package org.jeecg.modules.wec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.wec.entity.WecBuilding;
import org.jeecg.modules.wec.vo.WecBuildingVO;

import java.util.List;

public interface IWecBuildingService extends JeecgService<WecBuilding> {
    List<WecBuildingVO> toVOList(List<WecBuilding> list);
    IPage<WecBuildingVO> toVOPage(IPage<WecBuilding> page);
}
