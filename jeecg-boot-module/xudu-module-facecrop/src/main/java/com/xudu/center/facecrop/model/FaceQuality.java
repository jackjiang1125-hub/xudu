package com.xudu.center.facecrop.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FaceQuality {
    public String imageName;
    public int faceIndex;
    public int imgW, imgH;
    public int x, y, w, h;
    public float faceWidthRatio;
    public float mean, std;
    public float underRatio, overRatio;
    public double varLap;
    public float symDiff, illumDiff;
    public boolean accepted;
    public List<String> failedReasons = new ArrayList<>();

    public static String csvHeader(){
        return String.join(",",
                "image","faceIndex","imgW","imgH","x","y","w","h",
                "faceWidthRatio","mean","std","underRatio","overRatio",
                "varLap","symDiff","illumDiff","accepted","failedReasons");
    }

    public String toCsvLine() {
        return csv(imageName) + "," + faceIndex + "," +
                imgW + "," + imgH + "," + x + "," + y + "," + w + "," + h + "," +
                fmt(faceWidthRatio) + "," + fmt(mean) + "," + fmt(std) + "," +
                fmt(underRatio) + "," + fmt(overRatio) + "," + fmt(varLap) + "," +
                fmt(symDiff) + "," + fmt(illumDiff) + "," + accepted + "," +
                // 注意：Java 的正确写法是 String.join(";", list)
                csv(String.join(";", failedReasons));
    }

    private static String fmt(double v){
        return String.format(Locale.ROOT, "%.6f", v);
    }

    /** 简单 CSV 转义：如果包含逗号/引号/换行，就用双引号包起来并把内部双引号翻倍 */
    private static String csv(String s){
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
