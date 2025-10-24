package org.jeecg.modules.iot.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecg.modules.iot.device.entity.IotDeviceCommand;
import org.jeecg.modules.iot.device.service.IotDeviceCommandService;
import org.jeecg.modules.iot.device.service.IotDeviceInnerService;
import org.jeecg.modules.iot.utils.zkteco.AccessCommandFactory;
import org.jeecg.modules.iot.utils.ImageCompressionTest;
import org.jeecg.modules.iot.device.util.FaceExtractionUtil;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;

import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.jeecgframework.boot.iot.api.IotDeviceService;
import org.jeecgframework.boot.iot.query.IotDeviceQuery;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;
import org.opencv.core.Rect;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.jeecg.modules.iot.device.seq.CommandSeqService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * REST endpoints for managing access control devices.
 */
@Tag(name = "IOT-ACC:设备管理")
@RestController
@RequestMapping("/iot/acc/device")
@RequiredArgsConstructor
@Slf4j
public class IotDeviceController extends JeecgController<IotDevice, IotDeviceInnerService> {

    private final CommandSeqService commandSeqService;


    private final IotDeviceInnerService iotDeviceInnerService;
    private final IotDeviceCommandService iotDeviceCommandService;
    private final ImageCompressionTest imageCompressionTest;


    private final IotDeviceService iotDeviceService;

    @GetMapping("/list")
    @Operation(summary = "分页查询门禁设备")
    public Result<PageResult<IotDeviceVO>> list(IotDeviceQuery iotDeviceQuery,
                                               PageRequest pageRequest,
                                         HttpServletRequest req) {
//        QueryWrapper<IotDevice> queryWrapper = QueryGenerator.initQueryWrapper(accDevice, req.getParameterMap());
//        String authorizedParam = req.getParameter("authorized");
//        if (StringUtils.isNotBlank(authorizedParam)) {
//            boolean authorized = "1".equalsIgnoreCase(authorizedParam)
//                    || Boolean.parseBoolean(authorizedParam);
//            queryWrapper.eq("authorized", authorized);
//        }
//        Page<IotDevice> page = new Page<>(pageNo, pageSize);
//        IPage<IotDevice> pageList = iotDeviceInnerService.page(page, queryWrapper);
        PageResult<IotDeviceVO> list = iotDeviceService.list(iotDeviceQuery, pageRequest, req.getParameterMap());
        return Result.OK(list);
    }

    @PostMapping("/authorize")
    @Operation(summary = "手动授权设备")
    public Result<IotDevice> authorize(@RequestBody AuthorizeRequest request) {
        if (request == null || StringUtils.isBlank(request.sn)) {
            return Result.error("设备SN不能为空");
        }
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String operator = loginUser != null ? loginUser.getUsername() : null;
        return iotDeviceInnerService.authorizeDevice(request.sn, request.registryCode, request.remark, operator)
                .map(Result::OK)
                .orElseGet(() -> Result.error("未找到设备:" + request.sn));
    }

    @PostMapping("/commands")
    @Operation(summary = "为设备新增下发命令")
    public Result<List<IotDeviceCommand>> enqueueCommands(@RequestBody CommandBatchRequest request) {
        if (request == null || StringUtils.isBlank(request.getSn())) {
            return Result.error("设备SN不能为空");
        }
        List<String> commands = new ArrayList<>();
        if (request.getCommands() != null && !request.getCommands().isEmpty()) {
            request.getCommands().stream()
                    .map(line -> StringUtils.trimToEmpty(line))
                    .filter(StringUtils::isNotBlank)
                    .forEach(commands::add);
        }
        if (StringUtils.isNotBlank(request.getCommandsText())) {
            Arrays.stream(request.getCommandsText().split("\\r?\\n"))
                    .map(StringUtils::trimToEmpty)
                    .filter(StringUtils::isNotBlank)
                    .forEach(commands::add);
        }
        if (commands.isEmpty()) {
            return Result.error("命令内容不能为空");
        }
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String operator = loginUser != null ? loginUser.getUsername() : null;
        List<IotDeviceCommand> saved = iotDeviceCommandService.enqueueCommands(request.getSn(), commands, operator);
        return Result.OK(saved);
    }

    @PostMapping("/personnel")
    @Operation(summary = "人员信息下发")
    public Result<List<IotDeviceCommand>> dispatchPersonnel(@RequestBody PersonnelDispatchRequest request) {
        if (request == null || StringUtils.isBlank(request.getSn())) {
            return Result.error("设备SN不能为空");
        }
        if (StringUtils.isBlank(request.getPin())) {
            return Result.error("工号不能为空");
        }
        
        try {
            // 构建用户信息
            AccessCommandFactory.CmdUser user = new AccessCommandFactory.CmdUser(request.getPin());
            user.name = request.getName();
            user.cardno = request.getCardno();
            user.password = request.getPassword();
            user.group = request.getGroup();
            user.privilege = request.getPrivilege();
            user.starttime = request.getStarttime();
            user.endtime = request.getEndtime();
            user.disable = "0"; // 默认启用
            user.verify = "0";  // 默认验证方式

            // 构建门禁权限
            List<AccessCommandFactory.CmdUserAuthorize> authList = new ArrayList<>();
            if (request.getAuthorizeTimezoneId() != null && request.getAuthorizeDoorId() != null) {
                AccessCommandFactory.CmdUserAuthorize auth = new AccessCommandFactory.CmdUserAuthorize(
                    request.getPin(), 
                    request.getAuthorizeTimezoneId(), 
                    request.getAuthorizeDoorId()
                );
                auth.devId = request.getDevId();
                authList.add(auth);
            }

            // 构建用户照片
            AccessCommandFactory.CmdUserPic userPic = null;
            if (request.getUserPic() != null && StringUtils.isNotBlank(request.getUserPic().getContent())) {
                String content = request.getUserPic().getContent();
                // 判断是URL还是base64
                if (content.startsWith("http://") || content.startsWith("https://")) {
                    // URL方式，需要下载并转换为base64
                    try {
                        String base64Content = downloadImageAsBase64(content);
                        userPic = AccessCommandFactory.CmdUserPic.fromBase64(request.getPin(), base64Content);
                    } catch (Exception e) {
                        log.error("下载用户照片失败: {}", content, e);
                        return Result.error("下载用户照片失败: " + e.getMessage());
                    }
                } else {
                    // 直接是base64
                    userPic = AccessCommandFactory.CmdUserPic.fromBase64(request.getPin(), content);
                }
            }

            // 构建比对照片
            AccessCommandFactory.CmdBioPhoto bioPhoto = null;
            if (request.getBioPhoto() != null && StringUtils.isNotBlank(request.getBioPhoto().getContent())) {
                String content = request.getBioPhoto().getContent();
                log.info("BioPhoto原始content: {}", content);
                log.info("BioPhoto content长度: {}", content.length());
                
                // 验证content是否有效
                if (content.length() < 10) {
                    log.error("BioPhoto content长度太短，可能数据有问题: {}", content);
                    return Result.error("比对照片数据无效，长度太短: " + content);
                }
                
                // 判断是URL还是base64
                if (content.startsWith("http://") || content.startsWith("https://")) {
                    // URL方式，需要下载并转换为base64
                    try {
                        log.info("开始下载比对照片: {}", content);
                        String base64Content = downloadImageAsBase64(content);
                        log.info("下载完成，base64长度: {}", base64Content.length());
                        
                        // 验证base64数据是否有效
                        if (base64Content.length() < 100) {
                            log.error("下载的base64数据长度太短: {}", base64Content);
                            return Result.error("下载的图片数据无效，长度太短");
                        }
                        
                        bioPhoto = AccessCommandFactory.CmdBioPhoto.fromBase64(request.getPin(), base64Content);
                        log.info("BioPhoto对象创建成功，content长度: {}", bioPhoto.content.length());
                    } catch (Exception e) {
                        log.error("下载比对照片失败: {}", content, e);
                        return Result.error("下载比对照片失败: " + e.getMessage());
                    }
                } else {
                    // 直接是base64
                    log.info("直接使用base64数据，长度: {}", content.length());
                    
                    // 验证base64数据是否有效
                    if (content.length() < 100) {
                        log.error("base64数据长度太短: {}", content);
                        return Result.error("比对照片base64数据无效，长度太短");
                    }
                    
                    bioPhoto = AccessCommandFactory.CmdBioPhoto.fromBase64(request.getPin(), content);
                    log.info("BioPhoto对象创建成功，content长度: {}", bioPhoto.content.length());
                }
            }

            // 生成命令序列
            long startCmdId = commandSeqService.nextSeq(request.getSn());
            List<String> commands = AccessCommandFactory.buildAddUserBundle((int) startCmdId, user, authList, userPic, bioPhoto);




            // 下发命令
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String operator = loginUser != null ? loginUser.getUsername() : null;
            List<IotDeviceCommand> saved = iotDeviceCommandService.enqueueCommands(request.getSn(), commands, operator);
            
            return Result.OK(saved);
        } catch (Exception e) {
            return Result.error("人员信息下发失败：" + e.getMessage());
        }
    }

    @PostMapping("/test-compression")
    @Operation(summary = "测试图片压缩功能")
    public Result<String> testImageCompression(@RequestBody CompressionTestRequest request) {
        try {
            if (StringUtils.isBlank(request.getImageBase64())) {
                return Result.error("图片base64数据不能为空");
            }
            
            int targetSizeKB = request.getTargetSizeKB() != null ? request.getTargetSizeKB() : 200;
            String result = imageCompressionTest.testCompression(request.getImageBase64(), targetSizeKB);
            
            return Result.OK(result);
        } catch (Exception e) {
            log.error("图片压缩测试失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/extract-face")
    @Operation(summary = "人脸抠图功能")
    public Result<FaceExtractionResult> extractFace(@RequestBody FaceExtractionRequest request) {
        try {
            log.info("开始人脸抠图处理");
            
            byte[] imageBytes;
            
            // 判断是URL还是base64
            if (StringUtils.isNotBlank(request.getImageUrl())) {
                // URL方式，后端下载图片
                log.info("从URL下载图片: {}", request.getImageUrl());
                imageBytes = downloadImageAsBytes(request.getImageUrl());
                log.info("图片下载完成，大小: {} bytes", imageBytes.length);
            } else if (StringUtils.isNotBlank(request.getImageBase64())) {
                // base64方式
                imageBytes = Base64.getDecoder().decode(request.getImageBase64());
                log.info("原始图片大小: {} bytes", imageBytes.length);
            } else {
                return Result.error("图片URL或base64数据不能为空");
            }
            
            // 使用人脸抠图工具
          //  FaceExtractionUtil faceUtil = new FaceExtractionUtil();
            
            // 检测人脸
            List<Rect> faces = FaceExtractionUtil.detectFaces(imageBytes);
            log.info("检测到 {} 个人脸", faces.size());
            
            if (faces.isEmpty()) {
                return Result.error("未检测到人脸");
            }
            
            // 提取最大的人脸
            int padding = request.getPadding() != null ? request.getPadding() : 20;
            byte[] faceImageBytes = FaceExtractionUtil.extractLargestFace(imageBytes, padding);
            if (faceImageBytes == null) {
                return Result.error("人脸提取失败");
            }
            
            // 转换为base64
            String faceBase64 = FaceExtractionUtil.imageToBase64(faceImageBytes);
            FaceExtractionUtil.imageToFile(faceImageBytes);
            // 构建返回结果
            FaceExtractionResult result = new FaceExtractionResult();
            result.setOriginalSize(Long.valueOf(imageBytes.length));
            result.setFaceCount(faces.size());
            result.setFaceImageBase64(faceBase64);
            result.setFaceImageSize(Long.valueOf(faceImageBytes.length));
            result.setLargestFace(faces.get(0)); // 假设第一个是最大的
            
            log.info("人脸抠图完成: 原始大小={} bytes, 人脸图片大小={} bytes", 
                imageBytes.length, faceImageBytes.length);
            
            return Result.OK(result);
            
        } catch (Exception e) {
            log.error("人脸抠图失败", e);
            return Result.error("人脸抠图失败: " + e.getMessage());
        }
    }

    public record AuthorizeRequest(String sn, String registryCode, String remark) {
    }

    public static class CommandBatchRequest {
        private String sn;
        private String commandsText;
        private List<String> commands;

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getCommandsText() {
            return commandsText;
        }

        public void setCommandsText(String commandsText) {
            this.commandsText = commandsText;
        }

        public List<String> getCommands() {
            return commands;
        }

        public void setCommands(List<String> commands) {
            this.commands = commands;
        }
    }

    public static class PersonnelDispatchRequest {
        private String sn;
        private String pin;
        private String name;
        private String cardno;
        private String password;
        private String group;
        private String privilege;
        private String starttime;
        private String endtime;
        private Integer authorizeTimezoneId;
        private Integer authorizeDoorId;
        private Integer devId;
        private PhotoInfo userPic;
        private PhotoInfo bioPhoto;

        // Getters and Setters
        public String getSn() { return sn; }
        public void setSn(String sn) { this.sn = sn; }
        
        public String getPin() { return pin; }
        public void setPin(String pin) { this.pin = pin; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCardno() { return cardno; }
        public void setCardno(String cardno) { this.cardno = cardno; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
        
        public String getPrivilege() { return privilege; }
        public void setPrivilege(String privilege) { this.privilege = privilege; }
        
        public String getStarttime() { return starttime; }
        public void setStarttime(String starttime) { this.starttime = starttime; }
        
        public String getEndtime() { return endtime; }
        public void setEndtime(String endtime) { this.endtime = endtime; }
        
        public Integer getAuthorizeTimezoneId() { return authorizeTimezoneId; }
        public void setAuthorizeTimezoneId(Integer authorizeTimezoneId) { this.authorizeTimezoneId = authorizeTimezoneId; }
        
        public Integer getAuthorizeDoorId() { return authorizeDoorId; }
        public void setAuthorizeDoorId(Integer authorizeDoorId) { this.authorizeDoorId = authorizeDoorId; }
        
        public Integer getDevId() { return devId; }
        public void setDevId(Integer devId) { this.devId = devId; }
        
        public PhotoInfo getUserPic() { return userPic; }
        public void setUserPic(PhotoInfo userPic) { this.userPic = userPic; }
        
        public PhotoInfo getBioPhoto() { return bioPhoto; }
        public void setBioPhoto(PhotoInfo bioPhoto) { this.bioPhoto = bioPhoto; }
    }

    public static class PhotoInfo {
        private String content;
        private String name;
        private String type;
        private Long size;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }
    }

    public static class CompressionTestRequest {
        private String imageBase64;
        private Integer targetSizeKB;

        public String getImageBase64() { return imageBase64; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
        
        public Integer getTargetSizeKB() { return targetSizeKB; }
        public void setTargetSizeKB(Integer targetSizeKB) { this.targetSizeKB = targetSizeKB; }
    }
    
    public static class FaceExtractionRequest {
        private String imageUrl;
        private String imageBase64;
        private Integer padding;

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getImageBase64() { return imageBase64; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
        
        public Integer getPadding() { return padding; }
        public void setPadding(Integer padding) { this.padding = padding; }
    }
    
    public static class FaceExtractionResult {
        private Long originalSize;
        private Integer faceCount;
        private String faceImageBase64;
        private Long faceImageSize;
        private Rect largestFace;

        public Long getOriginalSize() { return originalSize; }
        public void setOriginalSize(Long originalSize) { this.originalSize = originalSize; }
        
        public Integer getFaceCount() { return faceCount; }
        public void setFaceCount(Integer faceCount) { this.faceCount = faceCount; }
        
        public String getFaceImageBase64() { return faceImageBase64; }
        public void setFaceImageBase64(String faceImageBase64) { this.faceImageBase64 = faceImageBase64; }
        
        public Long getFaceImageSize() { return faceImageSize; }
        public void setFaceImageSize(Long faceImageSize) { this.faceImageSize = faceImageSize; }
        
        public Rect getLargestFace() { return largestFace; }
        public void setLargestFace(Rect largestFace) { this.largestFace = largestFace; }
    }

    /**
     * 下载图片并返回字节数组
     */
    private byte[] downloadImageAsBytes(String imageUrl) throws Exception {
        log.info("开始下载图片: {}", imageUrl);
        URL url = new URL(imageUrl);
        try (InputStream inputStream = url.openStream()) {
            byte[] imageBytes = inputStream.readAllBytes();
            log.info("图片下载完成，原始字节数: {}", imageBytes.length);
            return imageBytes;
        }
    }
    
    /**
     * 下载图片并转换为base64
     */
    private String downloadImageAsBase64(String imageUrl) throws Exception {
        log.info("开始下载图片: {}", imageUrl);
        URL url = new URL(imageUrl);
        try (InputStream inputStream = url.openStream()) {
            byte[] imageBytes = inputStream.readAllBytes();
            log.info("图片下载完成，原始字节数: {}", imageBytes.length);
            
            // 压缩图片到200K以下
            byte[] compressedBytes = compressImageToTargetSize(imageBytes, 200 * 1024); // 200KB
            log.info("图片压缩完成，压缩后字节数: {}", compressedBytes.length);
            log.info("压缩率: {:.2f}%", (1.0 - (double) compressedBytes.length / imageBytes.length) * 100);
            
            String base64 = Base64.getEncoder().encodeToString(compressedBytes);
            log.info("base64转换完成，长度: {}", base64.length());
            log.info("base64前100字符: {}", base64.substring(0, Math.min(100, base64.length())));
            return base64;
        }
    }
    
    /**
     * 压缩图片到指定大小以下
     * @param originalBytes 原始图片字节数组
     * @param targetSizeBytes 目标大小（字节）
     * @return 压缩后的图片字节数组
     */
    private byte[] compressImageToTargetSize(byte[] originalBytes, int targetSizeBytes) throws Exception {
        log.info("开始压缩图片，原始大小: {} bytes, 目标大小: {} bytes", originalBytes.length, targetSizeBytes);
        
        // 如果原始图片已经小于目标大小，直接返回
        if (originalBytes.length <= targetSizeBytes) {
            log.info("图片已小于目标大小，无需压缩");
            return originalBytes;
        }
        
        // 读取原始图片
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (originalImage == null) {
            throw new Exception("无法读取图片数据");
        }
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        log.info("原始图片尺寸: {}x{}", originalWidth, originalHeight);
        
        // 计算压缩比例，从0.8开始尝试
        double scale = 0.8;
        byte[] compressedBytes = null;
        int attempts = 0;
        final int maxAttempts = 10;
        
        while (attempts < maxAttempts) {
            try {
                int newWidth = (int) (originalWidth * scale);
                int newHeight = (int) (originalHeight * scale);
                
                // 确保最小尺寸
                if (newWidth < 50) newWidth = 50;
                if (newHeight < 50) newHeight = 50;
                
                log.info("尝试压缩比例: {:.2f}, 新尺寸: {}x{}", scale, newWidth, newHeight);
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                Thumbnails.of(originalImage)
                    .size(newWidth, newHeight)
                    .antialiasing(Antialiasing.ON)
                    .outputQuality(0.8) // JPEG质量
                    .outputFormat("jpg") // 统一输出为JPEG格式以获得更好的压缩效果
                    .toOutputStream(baos);
                
                compressedBytes = baos.toByteArray();
                log.info("压缩后大小: {} bytes", compressedBytes.length);
                
                // 如果压缩后大小符合要求，返回结果
                if (compressedBytes.length <= targetSizeBytes) {
                    log.info("压缩成功，最终大小: {} bytes", compressedBytes.length);
                    return compressedBytes;
                }
                
                // 如果还是太大，减小压缩比例
                scale *= 0.8;
                attempts++;
                
            } catch (Exception e) {
                log.error("压缩失败，尝试次数: {}", attempts, e);
                attempts++;
                scale *= 0.8;
            }
        }
        
        // 如果所有尝试都失败，返回最后一次的结果
        if (compressedBytes != null) {
            log.warn("无法压缩到目标大小，返回最后一次压缩结果: {} bytes", compressedBytes.length);
            return compressedBytes;
        }
        
        // 如果完全失败，返回原始图片
        log.warn("压缩完全失败，返回原始图片");
        return originalBytes;
    }
}
