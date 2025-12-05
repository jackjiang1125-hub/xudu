package org.jeecg.modules.iot.device.protocol;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WaterDeviceSessionManager {

    // Key: IP (or DeviceID), Value: Channel
    private final Map<String, Channel> sessions = new ConcurrentHashMap<>();
    
    // Key: IP (or DeviceID), Value: Future waiting for response
    private final Map<String, CompletableFuture<byte[]>> pendingRequests = new ConcurrentHashMap<>();

    public void addSession(String key, Channel channel) {
        sessions.put(key, channel);
        log.info("Water device session added: {}", key);
    }

    public void removeSession(String key) {
        sessions.remove(key);
        pendingRequests.remove(key); // Clean up pending
        log.info("Water device session removed: {}", key);
    }

    public Channel getSession(String key) {
        return sessions.get(key);
    }
    
    public void sendTo(String key, byte[] data) {
        if (key == null) return;
        Channel ch = sessions.get(key);
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(io.netty.buffer.Unpooled.copiedBuffer(data));
        } else {
            log.warn("Device session not found or inactive: {}", key);
            log.warn("Available sessions keys: {}", sessions.keySet());
        }
    }

    public byte[] sendSync(String key, byte[] data, long timeoutMs) {
        Channel ch = sessions.get(key);
        if (ch == null || !ch.isActive()) {
            throw new RuntimeException("Device not connected: " + key);
        }
        
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        pendingRequests.put(key, future);
        
        try {
            ch.writeAndFlush(io.netty.buffer.Unpooled.copiedBuffer(data));
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Request failed or timed out", e);
        } finally {
            pendingRequests.remove(key);
        }
    }
    
    public void onResponseReceived(String key, byte[] data) {
        CompletableFuture<byte[]> future = pendingRequests.get(key);
        if (future != null) {
            future.complete(data);
        }
    }
}
