package com.xudu.center.facecrop.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "face")
public class FaceProcessingProperties {
    private Path inputDir;
    private Path outputDir;
    private int minSizePx = 60;
    private int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    private boolean onlyKeepLargestFace = false;
    private int maxFacesPerImage = 3;
    private Quality quality = new Quality();

    public static class Quality {
        private float minFaceWidthRatio = 0.12f;
        private float minMean = 0.20f;
        private float maxMean = 0.85f;
        private float maxUnderExpRatio = 0.25f;
        private float maxOverExpRatio = 0.10f;
        private float minStd = 0.07f;
        private double minVarLap = 60.0;
        private float maxSymDiff = 0.10f;
        private float maxIllumDiff = 0.12f;
        private float marginRatio = 0.10f;

        public float getMinFaceWidthRatio() { return minFaceWidthRatio; }
        public void setMinFaceWidthRatio(float v) { this.minFaceWidthRatio = v; }
        public float getMinMean() { return minMean; }
        public void setMinMean(float v) { this.minMean = v; }
        public float getMaxMean() { return maxMean; }
        public void setMaxMean(float v) { this.maxMean = v; }
        public float getMaxUnderExpRatio() { return maxUnderExpRatio; }
        public void setMaxUnderExpRatio(float v) { this.maxUnderExpRatio = v; }
        public float getMaxOverExpRatio() { return maxOverExpRatio; }
        public void setMaxOverExpRatio(float v) { this.maxOverExpRatio = v; }
        public float getMinStd() { return minStd; }
        public void setMinStd(float v) { this.minStd = v; }
        public double getMinVarLap() { return minVarLap; }
        public void setMinVarLap(double v) { this.minVarLap = v; }
        public float getMaxSymDiff() { return maxSymDiff; }
        public void setMaxSymDiff(float v) { this.maxSymDiff = v; }
        public float getMaxIllumDiff() { return maxIllumDiff; }
        public void setMaxIllumDiff(float v) { this.maxIllumDiff = v; }
        public float getMarginRatio() { return marginRatio; }
        public void setMarginRatio(float v) { this.marginRatio = v; }
    }

    public Path getInputDir() { return inputDir; }
    public void setInputDir(Path p) { this.inputDir = p; }

    public Path getOutputDir() { return outputDir; }
    public void setOutputDir(Path p) { this.outputDir = p; }

    public int getMinSizePx() { return minSizePx; }
    public void setMinSizePx(int v) { this.minSizePx = v; }

    public int getThreads() { return threads; }
    public void setThreads(int v) { this.threads = v; }

    public boolean isOnlyKeepLargestFace() { return onlyKeepLargestFace; }
    public void setOnlyKeepLargestFace(boolean v) { this.onlyKeepLargestFace = v; }

    public int getMaxFacesPerImage() { return maxFacesPerImage; }
    public void setMaxFacesPerImage(int v) { this.maxFacesPerImage = v; }

    public Quality getQuality() { return quality; }
    public void setQuality(Quality q) { this.quality = q; }
}
