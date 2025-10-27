package org.jeecg.modules.hkclients;

import org.jeecg.modules.hkclients.dto.HkConn;
import org.jeecg.modules.hkclients.dto.NvrDeviceOverview;
import org.jeecg.modules.hkclients.http.HikPooledClientManager;
import org.jeecg.modules.hkclients.model.content.InputProxyChannelList;
import org.jeecg.modules.hkclients.model.record.TrackDailyDistribution;
import org.jeecg.modules.hkclients.model.search.CMSearchResult;
import org.jeecg.modules.hkclients.model.system.DeviceInfo;
import org.jeecg.modules.hkclients.model.network.NetworkInterface;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HKClientsSimpleTest {

    // ====== 修改这里为你的 NVR 参数 ======
    static final String HOST = "192.168.1.168";
    static final int    PORT = 80;
    static final String USER = "admin";
    static final String PASS = "Zkteco@124";
    static final int    CH   = 1;
    static final int    STREAM_TYPE = 1;
    // ===================================

    static HikPooledClientManager manager;
    static HKClients hk;
    static HkConn conn;

    @BeforeAll static void init() {
        manager = new HikPooledClientManager(); manager.start();
        hk = new HKClients(manager);
        conn = HkConn.builder().host(HOST).port(PORT).username(USER).password(PASS)
                .connectTimeoutMs(5000).readTimeoutMs(10000).build();
    }
    @AfterAll static void shutdown() { if (manager != null) manager.shutdown(); }

    @Test @Order(1) void testDeviceInfo() {
        DeviceInfo info = hk.getDeviceInfo(conn);
        assertNotNull(info);
        System.out.println("[deviceInfo] name=" + info.getDeviceName() + ", model=" + info.getModel() + ", sn=" + info.getSerialNumber());
    }

    @Test @Order(2) void testChannelsAndCodecs() {
        NvrDeviceOverview ov = hk.buildOverviewWithRtsp(conn);
        assertNotNull(ov);
        System.out.println("[overview] channels=" + ov.getChannelCount());
        ov.getChannels().forEach(c -> {
            System.out.println("CH" + c.getId() + " name=" + c.getName() + " online=" + c.getOnline());
            c.getStreams().forEach(s -> {
                System.out.print(s);
            });
        });
    }

    @Test @Order(3) void testDailyDistributionAndSearch() {
        TrackDailyDistribution cal = hk.getDailyDistribution(conn, CH, STREAM_TYPE, 2025, 10);
        assertNotNull(cal);
        CMSearchResult r = hk.searchRecordings(conn, Arrays.asList(CH), STREAM_TYPE,
                "2025-10-25T21:32:00Z", "2025-10-25T21:33:00Z", 50);
        assertNotNull(r);
        System.out.println("[search] matches=" + r.getNumOfMatches());
    }

    @Test @Order(4) void testNetworkGetOnly() {
        NetworkInterface ni = hk.getNetworkInterface(conn, 1);
        assertNotNull(ni);
        System.out.println("[net] ip=" + ni.getIpAddress().getIpAddress());
    }

    @Test @Order(5)
    void testRaw(){
        String url = "/ISAPI/ContentMgmt/download/capabilities";
        String string = hk.getRaw(conn, url);
        System.out.println(string);
    }

}
