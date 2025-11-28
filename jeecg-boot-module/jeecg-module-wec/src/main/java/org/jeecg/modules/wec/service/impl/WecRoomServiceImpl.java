package org.jeecg.modules.wec.service.impl;

import lombok.RequiredArgsConstructor;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.wec.entity.WecRoom;
import org.jeecg.modules.wec.mapper.WecRoomMapper;
import org.jeecg.modules.wec.service.IWecRoomService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WecRoomServiceImpl extends JeecgServiceImpl<WecRoomMapper, WecRoom> implements IWecRoomService {}
