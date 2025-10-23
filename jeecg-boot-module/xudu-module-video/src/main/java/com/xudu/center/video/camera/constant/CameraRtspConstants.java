package com.xudu.center.video.camera.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

/**
 * Defines default RTSP patterns for different camera vendors.
 */
public final class CameraRtspConstants {

    public static final String VENDOR_ZHENSHI = "zhenshi";
    public static final String VENDOR_SHANGJI = "shangji";
    public static final String VENDOR_HUAWEI = "huawei";
    public static final String VENDOR_HIKVISION = "hikvision";

    private static final VendorProfile DEFAULT_PROFILE = new VendorProfile("554", "/h264");

    private static final Map<String, VendorProfile> PROFILES;

    static {
        Map<String, VendorProfile> profiles = new HashMap<>();

        VendorProfile zhenshi = new VendorProfile("8557", "/h264");
        VendorProfile shangji = new VendorProfile("554", "/ch01");
        VendorProfile huawei = new VendorProfile("554", "/LiveMedia/ch1/Media1");
        VendorProfile hikvision = new VendorProfile("554", "/Streaming/Channels/101");

        profiles.put(VENDOR_ZHENSHI, zhenshi);
        profiles.put("臻识", zhenshi);

        profiles.put(VENDOR_SHANGJI, shangji);
        profiles.put("熵基", shangji);

        profiles.put(VENDOR_HUAWEI, huawei);
        profiles.put("华为", huawei);

        profiles.put(VENDOR_HIKVISION, hikvision);
        profiles.put("海康", hikvision);
        profiles.put("haikang", hikvision);
        PROFILES = Collections.unmodifiableMap(profiles);
    }

    private CameraRtspConstants() {
    }

    /**
     * Builds an RTSP URL using vendor defaults. Returns empty string when IP is missing.
     */
    public static String buildRtspUrl(String vendor, String ip, String port) {
        if (!StringUtils.hasText(ip)) {
            return "";
        }
        VendorProfile profile = getProfileOrDefault(vendor);

        String resolvedPort = StringUtils.hasText(port) ? port : profile.defaultPort();
        StringBuilder sb = new StringBuilder("rtsp://");

        sb.append(ip);
        if (StringUtils.hasText(resolvedPort)) {
            sb.append(':').append(resolvedPort);
        }
        String path = profile.path();
        if (StringUtils.hasText(path)) {
            if (path.startsWith("/")) {
                sb.append(path);
            } else {
                sb.append('/').append(path);
            }
        }

        return sb.toString();
    }

    /**
     * Returns the vendor default port when the provided one is blank.
     */
    public static String resolvePort(String vendor, String port) {
        if (StringUtils.hasText(port)) {
            return port;
        }
        return getProfileOrDefault(vendor).defaultPort();
    }

    private static VendorProfile getProfileOrDefault(String vendor) {
        if (!StringUtils.hasText(vendor)) {
            return DEFAULT_PROFILE;
        }
        VendorProfile profile = PROFILES.get(vendor.trim().toLowerCase());
        return profile != null ? profile : DEFAULT_PROFILE;
    }

    private record VendorProfile(String defaultPort, String path) {
    }
}
