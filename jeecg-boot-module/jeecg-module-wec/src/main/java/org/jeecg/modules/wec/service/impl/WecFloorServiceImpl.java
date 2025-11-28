package org.jeecg.modules.wec.service.impl;

import lombok.RequiredArgsConstructor;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.wec.entity.WecFloor;
import org.jeecg.modules.wec.mapper.WecFloorMapper;
import org.jeecg.modules.wec.service.IWecFloorService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WecFloorServiceImpl extends JeecgServiceImpl<WecFloorMapper, WecFloor> implements IWecFloorService {}
