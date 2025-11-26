package org.jeecg.modules.iot.service;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.iot.model.DeviceMessage;
import org.jeecg.modules.iot.model.DeviceResponse;

import java.util.List;

@Slf4j
public class CompositeDeviceMessageProcessor implements DeviceMessageProcessor {

    private final List<DeviceMessageProcessor> delegates;

    public CompositeDeviceMessageProcessor(List<DeviceMessageProcessor> delegates) {
        this.delegates = delegates;
    }

    @Override
    public DeviceResponse process(DeviceMessage message) {
        for (DeviceMessageProcessor processor : delegates) {
            if (!processor.supports(message)) {
                continue;
            }
            log.debug("Use processor {} for path={}, uri={}",
                    processor.getClass().getSimpleName(),
                    message.getPath(),
                    message.getUri());
            return processor.process(message);
        }
        return DeviceResponse.text(404, "NOT FOUND");
    }
}
