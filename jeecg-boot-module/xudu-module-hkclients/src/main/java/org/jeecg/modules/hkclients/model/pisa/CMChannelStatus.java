package org.jeecg.modules.hkclients.model.pisa;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 映射 PSIA: urn:psialliance-org
 * <CMChannelStatus>
 *   <channelStatusList>
 *     <channelStatus>
 *       <channelID>101</channelID>
 *       <channelType>
 *         <contentType>mixed|video|audio</contentType>
 *         <codecType>MPEG4-MP|H.264|MJPEG|...</codecType>
 *       </channelType>
 *     </channelStatus>
 *     ...
 *   </channelStatusList>
 * </CMChannelStatus>
 */
@Data @NoArgsConstructor
@XmlRootElement(name = "CMChannelStatus", namespace = CMChannelStatus.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class CMChannelStatus {
    public static final String NS = "urn:psialliance-org";

    @XmlElementWrapper(name = "channelStatusList", namespace = NS)
    @XmlElement(name = "channelStatus", namespace = NS)
    private List<ChannelStatus> channelStatusList = new ArrayList<>();

    @Data @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ChannelStatus {
        @XmlElement(name = "channelID", namespace = NS)
        private Integer channelID;

        @XmlElement(name = "channelURI", namespace = NS)
        private String channelURI;

        @XmlElement(name = "channelState", namespace = NS)
        private String channelState;

        @XmlElement(name = "channelType", namespace = NS)
        private ChannelType channelType;
    }

    @Data @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ChannelType {
        @XmlElement(name = "channelInputType", namespace = NS)
        private String channelInputType;  // stream / ...
        @XmlElement(name = "contentType", namespace = NS)
        private String contentType;       // video / audio / mixed
        @XmlElement(name = "codecType", namespace = NS)
        private String codecType;         // H.264/H.265/MJPEG/MPEG4-MP...
    }
}
