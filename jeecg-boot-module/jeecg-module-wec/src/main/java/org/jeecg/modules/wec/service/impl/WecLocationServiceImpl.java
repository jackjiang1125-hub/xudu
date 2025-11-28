package org.jeecg.modules.wec.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.wec.entity.WecLocation;
import org.jeecg.modules.wec.mapper.WecLocationMapper;
import org.jeecg.modules.wec.service.IWecLocationService;
import org.jeecg.modules.wec.vo.WecLocationVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WecLocationServiceImpl extends JeecgServiceImpl<WecLocationMapper, WecLocation> implements IWecLocationService {

    @Override
    public List<WecLocationVO> toVOList(List<WecLocation> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        List<WecLocationVO> result = new ArrayList<>(list.size());
        for (WecLocation e : list) {
            WecLocationVO vo = new WecLocationVO();
            vo.setId(e.getId());
            vo.setLocationName(clean(e.getLocationName()));
            vo.setRemark(clean(e.getRemark()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public IPage<WecLocationVO> toVOPage(IPage<WecLocation> page) {
        List<WecLocationVO> records = toVOList(page.getRecords());
        Page<WecLocationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    private String clean(String s) {
        if (s == null) return null;
        return s.replace("*", "");
    }
}
