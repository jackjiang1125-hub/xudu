package org.jeecg.modules.iot.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.iot.device.protocol.WaterDeviceSessionManager;
import org.jeecg.modules.iot.model.DeviceMessage;
import org.jeecg.modules.iot.model.DeviceResponse;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;

import java.net.InetSocketAddress;

@Slf4j
public class WaterNettyHandler extends ChannelInboundHandlerAdapter {

    private final DeviceMessageProcessor processor;
    private final WaterDeviceSessionManager sessionManager;

    public WaterNettyHandler(DeviceMessageProcessor processor, WaterDeviceSessionManager sessionManager) {
        this.processor = processor;
        this.sessionManager = sessionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String ip = getClientIp(ctx);
        log.info("Water Device Connected: {}", ip);
        sessionManager.addSession(ip, ctx.channel());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        if (ctx.channel().isActive()) {
            String ip = getClientIp(ctx);
            // Ensure session is registered if handler is added after connection established
            sessionManager.addSession(ip, ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        String ip = getClientIp(ctx);
        log.info("Water Device Disconnected: {}", ip);
        sessionManager.removeSession(ip);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                int length = buf.readableBytes();
                if (length > 0) {
                    byte[] data = new byte[length];
                    buf.readBytes(data);

                    DeviceMessage message = DeviceMessage.builder()
                            .uri("/iot/water/tcp")
                            .path("/iot/water/tcp")
                            .method("TCP")
                            .clientIp(getClientIp(ctx))
                            .rawBody(data)
                            .build();

                    if (processor.supports(message)) {
                        DeviceResponse response = processor.process(message);
                        if (response != null && response.getRawBody() != null && response.getRawBody().length > 0) {
                            ctx.writeAndFlush(Unpooled.copiedBuffer(response.getRawBody()));
                        }
                    }
                }
            } finally {
                buf.release();
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Water Netty Handler Error", cause);
        ctx.close();
    }

    private String getClientIp(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress socketAddress) {
            return socketAddress.getAddress().getHostAddress();
        }
        return ctx.channel().id().asLongText();
    }
}
