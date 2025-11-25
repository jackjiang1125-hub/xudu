package org.jeecg.modules.hkclients;

import org.jeecg.modules.hkclients.clients.HkAccessControlClient;
import org.jeecg.modules.hkclients.dto.HkConn;
import org.jeecg.modules.hkclients.exception.HKClientException;
import org.jeecg.modules.hkclients.exception.HkErrorResponse;
import org.jeecg.modules.hkclients.http.HikPooledClientManager;
import org.jeecg.modules.hkclients.model.accesscontrol.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

/**
 * 一键冒烟：建“07:00–09:00”周计划 -> 建模板 -> 新增人员并赋模板
 * 然后查询：设备信息 / 能力集 / 周计划 / 模板 / 人员
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HikAccessControlQuickTest {


    // ====== 修改这里为你的 NVR 参数 ======
    static final String HOST = "192.168.51.226";
    static final int    PORT = 80;
    static final String USER = "admin";
    static final String PASS = "Zkteco@124";
    static final int    CH   = 1;
    static final int    STREAM_TYPE = 1;
    // ===================================

    static HikPooledClientManager manager;
    static HkAccessControlClient client;
    static HkConn conn;

    @BeforeAll
    static void init() {
        manager = new HikPooledClientManager(); manager.start();
        client = new HkAccessControlClient(manager);
        conn = HkConn.builder().host(HOST).port(PORT).username(USER).password(PASS)
                .connectTimeoutMs(5000).readTimeoutMs(10000).build();
    }
    @AfterAll
    static void shutdown() { if (manager != null) manager.shutdown(); }



    @Test
    @Order(2)
    public void test2(){
        String deviceInfo = client.getDeviceInfoRaw(conn);
        System.out.println(deviceInfo);
    }




    // ---------- 小工具：构造 Day/RightPlan/Face/图片Base64 ----------

    private static UserRightWeekPlanCfg.Day day(String week, String begin, String end) {
        var d = new UserRightWeekPlanCfg.Day();
        d.setWeek(week);
        d.setId(1);
        d.setEnable(true);
        var ts = new UserRightWeekPlanCfg.TimeSegment();
        ts.setBeginTime(begin);
        ts.setEndTime(end);
        d.setTimeSegment(ts);
        return d;
    }





    @Test
    public void testWeekPlan(){
        var week = new UserRightWeekPlanCfg();
        week.setWeekPlanCfg(List.of(
                day("Monday", "07:00:00", "09:00:00"),
                day("Tuesday", "07:00:00", "20:00:00"),
                day("Wednesday", "07:00:00", "20:00:00"),
                day("Thursday", "07:00:00", "20:00:00"),
                day("Friday", "07:00:00", "21:00:00"),
                day("Saturday", "07:00:00", "09:00:00"),
                day("Sunday", "07:00:00", "09:00:00")
        ));
        week.setEnable(true);
        System.out.println(week);
       boolean okWeek = client.putWeekPlan(conn, 2, week);
        //System.out.println("putUserRightWeekPlan(1) => " + okWeek);
    }

    @Test
    public void control(){
        boolean b = client.remoteControlDoor(conn, 1, RemoteControlDoor.Cmd.open);
        System.out.println(b);
    }




    @Test
    public void  testUserRightTemplate(){
        UserRightPlanTemplate userRightPlanTemplate = new UserRightPlanTemplate();
        userRightPlanTemplate.setTemplateNo(2);
        userRightPlanTemplate.setWeekPlanNo(2);
        userRightPlanTemplate.setEnable(true);
        userRightPlanTemplate.setTemplateName("jwz_test");

        boolean b = client.putUserRightPlanTemplate(conn, 2, userRightPlanTemplate);
        System.out.println(b);

    }







    @Test
    public void testFindWeekPlan2(){
        UserRightWeekPlanCfg userRightWeekPlan = client.getUserRightWeekPlan(conn, 3);
        System.out.println(userRightWeekPlan);
    }




    @Test
    public void testFindUserRightPlan(){
        UserRightPlanTemplate userRightPlanTemplate = client.getUserRightPlanTemplate(conn, 2);
        System.out.println(userRightPlanTemplate);
    }







    @Test
    public void  testUser(){
        var u = new UserInfo();
        u.setEmployeeNo("4");
        u.setName("李智");
        u.setUserType("normal");  // ★ 必带
        u.setDoorRight("1");      // ★ 必带

        var rp = new UserInfo.RightPlan();
        rp.setDoorNo(1);
        rp.setPlanTemplateNo("2");  // ★ 用字符串 "2"
        u.setRightPlan(List.of(rp));

        var valid = new UserInfo.Valid();
        valid.setBeginTime("2025-01-01T00:00:00");
        valid.setEndTime("2035-12-31T23:59:59");
        valid.setTimeType("local"); // ★ 建议
        u.setValid(valid);
        try{
            boolean okUser = client.postUser(conn, u);
        }catch (HKClientException e){
            HkErrorResponse hkError = e.getHkError();
            System.out.println(hkError);
        }

       // System.out.println("postUser => " + okUser);
    }

    @Test
    public void modifyUser(){
        var u = new UserInfo();
        u.setEmployeeNo("1");
        u.setName("jackjiang");
        u.setUserType("normal");  // ★ 必带
        u.setDoorRight("1");      // ★ 必带

        var rp = new UserInfo.RightPlan();
        rp.setDoorNo(1);
        rp.setPlanTemplateNo("2");  // ★ 用字符串 "2"
        u.setRightPlan(List.of(rp));

        var valid = new UserInfo.Valid();
        valid.setBeginTime("2025-01-01T00:00:00");
        valid.setEndTime("2035-12-31T23:59:59");
        valid.setTimeType("local"); // ★ 建议
        u.setValid(valid);

        boolean okUser = client.modifyUser(conn, u);
        System.out.println("postUser => " + okUser);
    }

    @Test
    public void testCard(){
        var card = new CardInfo();
        card.setCardNo("12345670");
        card.setEmployeeNo("4");
        boolean b = client.postCard(conn, card);
        System.out.println(b);
    }

    @Test
    public void testFace() throws Exception {
        byte[] jpg = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("d:/genzi.jpg"));
        boolean ok1 = client.postFaceFDLibMultipart(conn, "4", jpg, "biaozhi");
        System.out.println("multipart => " + ok1);
    }

    @Test
    public void modifyFace() throws IOException {
        byte[] jpg = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("d:/hehe.jpg"));
        client.modifyFaceFDLibMultipart(conn,"4",jpg,"fuck");
    }


    @Test
    public void getrtsp(){
        String doorMainRtsp = client.getDoorMainRtsp(conn);
        System.out.println(doorMainRtsp);
    }



    @Test
    public void configureHttpHost(){
        boolean b = client.configHttpEventCallback(conn, "192.168.51.225", 8080, "test");
        System.out.println(b);
    }







}


