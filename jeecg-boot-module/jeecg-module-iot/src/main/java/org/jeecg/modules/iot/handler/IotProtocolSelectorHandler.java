package org.jeecg.modules.iot.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.jeecg.modules.iot.device.protocol.WaterDeviceSessionManager;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;

import java.util.concurrent.TimeUnit;

public class IotProtocolSelectorHandler extends ChannelInboundHandlerAdapter {

    private final DeviceMessageProcessor messageProcessor;
    private final WaterDeviceSessionManager sessionManager;

    public IotProtocolSelectorHandler(DeviceMessageProcessor messageProcessor, WaterDeviceSessionManager sessionManager) {
        this.messageProcessor = messageProcessor;
        this.sessionManager = sessionManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf in = (ByteBuf) msg;
            // Need at least 1 byte to determine protocol
            if (in.readableBytes() < 1) {
                // Wait for more data? 
                // For simplicity, if we get an empty buffer (rare), we just forward it (next handler might not exist or complain)
                // But usually TCP packets have payload.
                return;
            }

            int magic = in.getUnsignedByte(in.readerIndex());

            // Water Protocol starts with 0xFE
            if (magic == 0xFE) {
                ctx.pipeline().addLast(new WaterNettyHandler(messageProcessor, sessionManager));
            } else {
                // Assume HTTP for everything else
                ctx.pipeline().addLast(new HttpServerCodec());
                ctx.pipeline().addLast(new HttpObjectAggregator(1024 * 1024 * 10));
                ctx.pipeline().addLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));
                ctx.pipeline().addLast(new DeviceMessageHandler(messageProcessor));
            }

            // Remove this selector handler
            ctx.pipeline().remove(this);
            
            // Forward the message to the newly added handler
            // The current message 'in' is NOT consumed yet, so the next handler will see it.
            ctx.fireChannelRead(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
