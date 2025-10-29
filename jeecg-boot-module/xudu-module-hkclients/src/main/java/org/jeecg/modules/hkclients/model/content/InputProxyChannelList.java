package org.jeecg.modules.hkclients.model.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

/**
 * Jackson-XML 版 InputProxyChannelList
 * 适配：/ISAPI/ContentMgmt/InputProxy/channels
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "InputProxyChannelList")
public class InputProxyChannelList {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "InputProxyChannel")
    private List<InputProxyChannel> channels;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InputProxyChannel {
        @JacksonXmlProperty(localName = "id")
        private Integer id;

        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "online")
        private Boolean online;

        @JacksonXmlProperty(localName = "sourceInputPortDescriptor")
        private SourceInputPortDescriptor sourceInputPortDescriptor;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceInputPortDescriptor {
        @JacksonXmlProperty(localName = "ipAddress")
        private String ipAddress;

        @JacksonXmlProperty(localName = "manufacturer")
        private String manufacturer;

        @JacksonXmlProperty(localName = "model")
        private String model;

        // 海康私有协议（SDK/管理端口，常见 8000）
        @JacksonXmlProperty(localName = "managePortNo")
        private Integer managePortNo;

        @JacksonXmlProperty(localName = "serialNumber")
        private String serialNumber;

        @JacksonXmlProperty(localName = "userName")
        private String userName;
    }
}