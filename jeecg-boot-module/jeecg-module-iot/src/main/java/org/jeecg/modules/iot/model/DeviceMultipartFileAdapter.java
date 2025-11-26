package org.jeecg.modules.iot.model;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * 将 Netty 解析出的 DeviceFilePart 适配成 Spring 的 MultipartFile，
 * 方便复用 CommonUtils.upload 等已有上传逻辑。
 */
public class DeviceMultipartFileAdapter implements MultipartFile {

    private final DeviceFilePart part;

    public DeviceMultipartFileAdapter(DeviceFilePart part) {
        this.part = part;
    }

    @Override
    public String getName() {
        return part.getName();
    }

    @Override
    public String getOriginalFilename() {
        return part.getFilename();
    }

    @Override
    public String getContentType() {
        return part.getContentType();
    }

    @Override
    public boolean isEmpty() {
        return part.getBytes() == null || part.getBytes().length == 0;
    }

    @Override
    public long getSize() {
        return part.getSize();
    }

    @Override
    public byte[] getBytes() {
        return part.getBytes();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(part.getBytes());
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (OutputStream os = new FileOutputStream(dest)) {
            os.write(part.getBytes());
        }
    }
}
