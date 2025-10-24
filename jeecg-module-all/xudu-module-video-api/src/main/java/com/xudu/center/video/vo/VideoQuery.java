package com.xudu.center.video.vo;


import lombok.Data;

/**
 * Video query parameters
 */
@Data

public class VideoQuery {
    

    private String name;
    

    private String ip;
    

    private String port;
    

    private String manufacturer;
    

    private String model;
    

    private String type;
    

    private String status;
    

    private String stream;

    private String app;
}
