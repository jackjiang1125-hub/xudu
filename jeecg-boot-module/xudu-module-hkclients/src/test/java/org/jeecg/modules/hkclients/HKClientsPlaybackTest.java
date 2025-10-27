package org.jeecg.modules.hkclients;

import org.jeecg.modules.hkclients.dto.HkConn;
import org.jeecg.modules.hkclients.http.HikPooledClientManager;
import org.jeecg.modules.hkclients.model.search.CMSearchResult;
import org.jeecg.modules.hkclients.util.RtspUriUtils;
import org.junit.jupiter.api.*;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HKClientsPlaybackTest {

    // ====== 改成你的设备参数 ======
    static final String HOST = "192.168.1.168";
    static final int    PORT = 80;           // HTTP 端口（不是 RTSP）
    static final String USER = "admin";
    static final String PASS = "Zkteco@124";
    static final int    CH   = 1;            // 通道号
    static final int    STREAM_TYPE = 1;     // 1=主 2=子 3=第三
    static final int    RTSP_PORT = 554;     // 如你改过，填你自己的
    // =================================

    static HikPooledClientManager manager;
    static HKClients hk;
    static HkConn conn;

    @BeforeAll static void init() {
        manager = new HikPooledClientManager(); manager.start();
        hk = new HKClients(manager);
        conn = HkConn.builder()
                .host(HOST).port(PORT)
                .username(USER).password(PASS)
                .connectTimeoutMs(5000).readTimeoutMs(15000)
                .build();
    }

    @AfterAll static void shutdown() { if (manager != null) manager.shutdown(); }

    @Test @Order(1)
    void testBuildPlaybackUrl() {
        int trackId = CH * 100 + STREAM_TYPE;

        // 看 1 分钟（举例：今天 10:00~10:01）——你自己改下时间即可
        LocalDateTime start = LocalDateTime.now().withMinute(0).withSecond(0);
        LocalDateTime end   = start.plusMinutes(1);

        String playable = RtspUriUtils.buildPlaybackRtsp(conn, trackId, start, end,554);


        System.out.println("playable (for VLC)   : " + playable);
        // 把 playable 粘到 VLC 或 ZLM 的拉流处即可测试
    }

    @Test @Order(2)
    void testDownloadByFileName() throws Exception {

        CMSearchResult r = hk.searchRecordings(conn, Arrays.asList(CH), STREAM_TYPE,
                "2025-10-25T21:22:00Z", "2025-10-25T21:31:00Z", 50);



        // 这个 name 建议从你刚才 search 的结果里取（mediaSegmentDescriptor.name）
        String fileName = "00000000115000000";

        Path saveTo = Path.of("target", fileName + ".mp4"); // 后缀随意，设备实际多为 PS/MP4

        Files.createDirectories(saveTo.getParent());
        try (FileOutputStream out = new FileOutputStream(saveTo.toFile())) {
            hk.downloadByFileName(conn, fileName, out);
        }
        System.out.println("保存到：" + saveTo.toAbsolutePath());
    }



}
