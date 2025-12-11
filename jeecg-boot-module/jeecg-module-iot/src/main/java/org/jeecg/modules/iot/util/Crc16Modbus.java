package org.jeecg.modules.iot.util;

public class Crc16Modbus {
    public static byte[] appendCrc(byte[] payload) {
        int crc = compute(payload);
        byte lo = (byte) (crc & 0xFF);
        byte hi = (byte) ((crc >> 8) & 0xFF);
        byte[] out = new byte[payload.length + 2];
        System.arraycopy(payload, 0, out, 0, payload.length);
        out[out.length - 2] = lo;
        out[out.length - 1] = hi;
        return out;
    }

    public static byte[] appendCrc(byte[] payload, boolean highFirst) {
        int crc = compute(payload);
        byte lo = (byte) (crc & 0xFF);
        byte hi = (byte) ((crc >> 8) & 0xFF);
        byte[] out = new byte[payload.length + 2];
        System.arraycopy(payload, 0, out, 0, payload.length);
        if (highFirst) {
            out[out.length - 2] = hi;
            out[out.length - 1] = lo;
        } else {
            out[out.length - 2] = lo;
            out[out.length - 1] = hi;
        }
        return out;
    }

    public static int computeRange(byte[] data, int startIndex, boolean skipLenBytes) {
        int crc = 0xFFFF;
        for (int i = Math.max(0, startIndex); i < data.length; i++) {
            if (skipLenBytes && (i == 4 || i == 5)) continue;
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >>> 1) ^ 0xA001;
                } else {
                    crc = (crc >>> 1);
                }
            }
        }
        return crc & 0xFFFF;
    }

    public static byte[] appendCrcCustom(byte[] payload, int startIndex, boolean skipLenBytes, boolean highFirst) {
        return appendCrcCustom(payload, startIndex, skipLenBytes, highFirst, "modbus");
    }

    public static int compute(byte[] data) {
        int crc = 0xFFFF;
        for (byte b : data) {
            crc ^= (b & 0xFF);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >>> 1) ^ 0xA001;
                } else {
                    crc = (crc >>> 1);
                }
            }
        }
        return crc & 0xFFFF;
    }

    private static int computeCcittRange(byte[] data, int startIndex, boolean skipLenBytes, int init, int xorOut) {
        int crc = init & 0xFFFF;
        for (int i = Math.max(0, startIndex); i < data.length; i++) {
            if (skipLenBytes && (i == 4 || i == 5)) continue;
            crc ^= (data[i] & 0xFF) << 8;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = ((crc << 1) ^ 0x1021) & 0xFFFF;
                } else {
                    crc = (crc << 1) & 0xFFFF;
                }
            }
        }
        crc ^= xorOut;
        return crc & 0xFFFF;
    }

    public static int computeGeneric(byte[] data, int startIndex, boolean skipLenBytes, String type) {
        String t = type == null ? "" : type.toLowerCase();
        switch (t) {
            case "ccitt_false":
                return computeCcittRange(data, startIndex, skipLenBytes, 0xFFFF, 0x0000);
            case "xmodem":
                return computeCcittRange(data, startIndex, skipLenBytes, 0x0000, 0x0000);
            case "x25":
                return computeCcittReflectedRange(data, startIndex, skipLenBytes, 0xFFFF, 0xFFFF);
            case "ibm":
            case "modbus":
            default:
                return computeRange(data, startIndex, skipLenBytes);
        }
    }

    public static byte[] appendCrcCustom(byte[] payload, int startIndex, boolean skipLenBytes, boolean highFirst, String type) {
        int crc = computeGeneric(payload, startIndex, skipLenBytes, type);
        byte lo = (byte) (crc & 0xFF);
        byte hi = (byte) ((crc >> 8) & 0xFF);
        byte[] out = new byte[payload.length + 2];
        System.arraycopy(payload, 0, out, 0, payload.length);
        if (highFirst) {
            out[out.length - 2] = hi;
            out[out.length - 1] = lo;
        } else {
            out[out.length - 2] = lo;
            out[out.length - 1] = hi;
        }
        return out;
    }

    private static int computeCcittReflectedRange(byte[] data, int startIndex, boolean skipLenBytes, int init, int xorOut) {
        int crc = init & 0xFFFF;
        for (int i = Math.max(0, startIndex); i < data.length; i++) {
            if (skipLenBytes && (i == 4 || i == 5)) continue;
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >>> 1) ^ 0x8408;
                } else {
                    crc = (crc >>> 1);
                }
            }
        }
        crc ^= xorOut;
        return crc & 0xFFFF;
    }
}
