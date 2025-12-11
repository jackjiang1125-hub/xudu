package org.jeecg.modules.iot.util;

public class HexUtil {
    public static byte[] fromHex(String hex) {
        String s = hex.replace(" ", "").replace("\n", "").replace("\t", "");
        if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
        if (s.length() % 2 != 0) throw new IllegalArgumentException("hex length must be even");
        byte[] out = new byte[s.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(s.charAt(i * 2), 16);
            int lo = Character.digit(s.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) throw new IllegalArgumentException("invalid hex");
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }

    public static String toHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b)).append(' ');
        }
        return sb.toString().trim();
    }

    public static byte[] ascii(String s) {
        return s.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    }
}
