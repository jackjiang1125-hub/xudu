package org.jeecg.modules.iot.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.iot.model.DeviceFilePart;
import org.jeecg.modules.iot.model.DeviceMessage;
import org.jeecg.modules.iot.model.DeviceResponse;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DeviceMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final DeviceMessageProcessor messageProcessor;

    public DeviceMessageHandler(DeviceMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());

        Map<String, String> queryParams = decoder.parameters().entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get(0)
                ));

        String path = decoder.path();
        String method = msg.method().name();
        String contentType = msg.headers().get(HttpHeaderNames.CONTENT_TYPE);

        String payload = null;
        Map<String, String> formParams = Collections.emptyMap();
        Map<String, DeviceFilePart> fileParams = Collections.emptyMap();

        // 解析 multipart/form-data：主要是海康 /event/record 这种
        if (contentType != null && contentType.startsWith(HttpHeaderValues.MULTIPART_FORM_DATA.toString())) {
            log.debug("Handling multipart/form-data request, uri={}", msg.uri());
            DefaultHttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
            HttpPostRequestDecoder postDecoder = null;
            try {
                postDecoder = new HttpPostRequestDecoder(factory, msg);
                formParams = new HashMap<>();
                fileParams = new HashMap<>();

                for (InterfaceHttpData data : postDecoder.getBodyHttpDatas()) {
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) data;
                        formParams.put(attribute.getName(), attribute.getValue());
                    } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        FileUpload fu = (FileUpload) data;
                        DeviceFilePart part = DeviceFilePart.builder()
                                .name(fu.getName())
                                .filename(fu.getFilename())
                                .contentType(fu.getContentType())
                                .bytes(fu.get())            // 全部读入内存
                                .size(fu.length())
                                .build();
                        fileParams.put(fu.getName(), part);
                    }
                }

                // 对于海康，你 Spring MVC 里 event_log 就是一个表单字段
                // 为了保持兼容，可以把 payload 默认设为 event_log
                payload = formParams.getOrDefault("event_log", "");
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException e) {
                log.error("Failed to decode multipart request", e);
                payload = msg.content().toString(CharsetUtil.UTF_8);
            } finally {
                if (postDecoder != null) {
                    postDecoder.destroy();
                }
            }
        } else {
            // 非 multipart 走老逻辑：直接当文本 body
            payload = msg.content().toString(CharsetUtil.UTF_8);
        }

        DeviceMessage message = DeviceMessage.builder()
                .uri(msg.uri())
                .method(method)
                .headers(extractHeaders(msg))
                .payload(payload)
                .path(path)
                .queryParameters(queryParams)
                .clientIp(resolveClientIp(ctx))
                .contentType(contentType)
                .formParameters(formParams)
                .fileParameters(fileParams)
                .build();

        DeviceResponse response = messageProcessor.process(message);
        FullHttpResponse httpResponse = toHttpResponse(response);

        boolean keepAlive = HttpUtil.isKeepAlive(msg);
        if (keepAlive) {
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(httpResponse);
        } else {
            ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Unexpected error while handling device message", cause);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.copiedBuffer("Internal Server Error", CharsetUtil.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Map<String, String> extractHeaders(FullHttpRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.headers().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
        return headers;
    }

    private FullHttpResponse toHttpResponse(DeviceResponse response) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(response.getStatusCode()),
                Unpooled.copiedBuffer(response.getBody(), response.getCharset())
        );
        response.getHeaders().forEach(httpResponse.headers()::set);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, response.getContentType());
        httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        return httpResponse;
    }

    private String resolveClientIp(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress socketAddress) {
            return socketAddress.getAddress().getHostAddress();
        }
        return "";
    }
}
