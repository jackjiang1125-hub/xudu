package org.jeecg.modules.wec.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.wec.entity.WecBuilding;
import org.jeecg.modules.wec.mapper.WecBuildingMapper;
import org.jeecg.modules.wec.service.IWecBuildingService;
import org.jeecg.modules.wec.vo.WecBuildingVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WecBuildingServiceImpl extends JeecgServiceImpl<WecBuildingMapper, WecBuilding> implements IWecBuildingService {

    @Override
    public List<WecBuildingVO> toVOList(List<WecBuilding> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        List<WecBuildingVO> result = new ArrayList<>(list.size());
        for (WecBuilding e : list) {
            WecBuildingVO vo = new WecBuildingVO();
            vo.setId(e.getId());
            vo.setBuildingName(clean(e.getBuildingName()));
            vo.setBuildingCode(clean(e.getBuildingCode()));
            vo.setAreaId(e.getAreaId());
            vo.setAreaName(null);
            result.add(vo);
        }
        return result;
    }

    @Override
    public IPage<WecBuildingVO> toVOPage(IPage<WecBuilding> page) {
        List<WecBuildingVO> records = toVOList(page.getRecords());
        Page<WecBuildingVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    private String clean(String s) {
        if (s == null) return null;
        return s.replace("*", "");
    }
}
