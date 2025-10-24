package com.xudu.center.zlm.model;

import lombok.Data;
import java.util.List;

@Data
public class MediaListData {
    private List<MediaItem> data; // 有些构建 data 在外层，你也可写自适应解析
}

@Data
class MediaItem {
    private String app;
    private String stream;
    private String schema; // rtsp/rtmp/hls...
    private String vhost;
    private List<Track> tracks;
}

@Data
class Track {
    private String codec_id_name; // CodecH264, CodecH265, CodecAAC, G711A...
    private Integer width;
    private Integer height;
    private Integer fps;
    private Integer sample_rate;
    private Integer channels;
}
