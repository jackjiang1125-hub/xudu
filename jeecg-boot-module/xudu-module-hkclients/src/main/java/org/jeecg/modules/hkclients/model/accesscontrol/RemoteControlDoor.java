package org.jeecg.modules.hkclients.model.accesscontrol;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

/**
 * 对应请求体:
 * <RemoteControlDoor xmlns="http://www.isapi.org/ver20/XMLSchema" version="2.0">
 *     <cmd>open</cmd>
 * </RemoteControlDoor>
 */
@Data
@JacksonXmlRootElement(localName = "RemoteControlDoor",
        namespace = "http://www.isapi.org/ver20/XMLSchema")
public class RemoteControlDoor {

    /** 作为属性写在根节点上 version="2.0" */
    @JacksonXmlProperty(isAttribute = true, localName = "version")
    private String version = "2.0";

    /** 命令：open / close / alwaysOpen / alwaysClose */
    @JacksonXmlProperty(localName = "cmd",
            namespace = "http://www.isapi.org/ver20/XMLSchema")
    private Cmd cmd;

    public enum Cmd {
        open,
        close,
        alwaysOpen,
        alwaysClose
    }
}
