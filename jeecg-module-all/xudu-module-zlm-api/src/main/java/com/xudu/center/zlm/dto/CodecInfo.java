package com.xudu.center.zlm.dto;

import com.xudu.center.zlm.params.Audio;
import com.xudu.center.zlm.params.Video;

// === 小 DTO ===
    // 放在 ZlmService 内部或单独文件都行
    public record CodecInfo(
        String videoRaw,   // 原始视频编码名（如 "H264" / "CodecH264"）
        String audioRaw,   // 原始音频编码名（如 "mpeg4-generic" / "PCMA"）
        Video videoKind,  // 归一化后的视频类型枚举
        Audio audioKind,  // 归一化后的音频类型枚举
        Integer width,
        Integer height,
        Double  fps,
        Integer sampleRate,
        Integer channels
    ) {}