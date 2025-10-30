package org.jeecg.modules.iot.utils.zkteco;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * AccessCommandFactory
 *
 * 目的：生成 BioTime/BioAccess 协议（第12章 DATA 子命令）所需的下发/删除命令串。
 *
 * 适配要点（依据协议原文）：
 *  - 下发用户信息：DATA UPDATE user（见文档“下发用户信息”）
 *  - 下发用户门禁权限：DATA UPDATE userauthorize（见文档“下发用户门禁权限”）
 *  - 下发用户照片：DATA UPDATE userpic（见文档“下发用户照片”）
 *  - 下发比对照片：DATA UPDATE biophoto（见文档“下发比对照片”）
 *  - 删除相关信息：DATA DELETE user / userauthorize / templatev10 / biodata / biophoto / userpic ...
 *
 * 使用方式：
 *  1) 直接调用 buildXXX(...) 生成单条命令。
 *  2) 调用 buildAddUserBundle(...) 一键生成 “用户 + 门禁权限 + 用户照片/比对照片” 的新增命令序列。
 *  3) 调用 buildReplaceUserBundle(...) 生成“删除后重建”的更新序列（先删再增）。
 */
public class AccessCommandFactory {
    // 协议分隔符
    private static final String SP = " ";
    private static final String HT = "\t"; // Horizontal Tab
    private static final String LF = "\n";  // Line Feed

    /**
     * 命令号生成器（可选）。也可由业务传入固定 CmdID。
     */
    public static class CmdSeq {
        private final AtomicInteger seq;
        public CmdSeq(int start) { this.seq = new AtomicInteger(start); }
        public int next() { return seq.getAndIncrement(); }
    }

    /* ========================= 数据模型 ========================= */
    public static class CmdUser {
        public String uid;         // 可空
        public String cardno;      // 可空（十六进制如 [15CD5B07] 或十进制字符串）
        public String pin;         // 必填：工号
        public String password;    // 可空，<=6 位
        public String group;       // 可空：门禁组
        public String starttime;   // 可空：按设备配置，YYYYMMDD 或 纪元秒
        public String endtime;     // 可空
        public String name;        // 可空（中文 GB2312，其他 UTF-8）
        public String privilege;   // 可空
        public String disable;     // 可空：黑名单 0/1
        public String verify;      // 可空：验证方式位图

        public CmdUser(String pin) { this.pin = pin; }
    }

    public static class CmdUserAuthorize {
        public String pin;                   // 必填
        public Integer authorizeTimezoneId;  // 必填
        public Integer authorizeDoorId;      // 必填（按 1..15 位图编码）
        public Integer devId;                // 可选/建议填
        public String startTime;             // 可选（纪元秒）
        public String endTime;               // 可选（纪元秒）

        public CmdUserAuthorize(String pin, int tzId, int doorId) {
            this.pin = pin;
            this.authorizeTimezoneId = tzId;
            this.authorizeDoorId = doorId;
        }
    }

    /** 下发用户照片（userpic） */
    public static class CmdUserPic {
        public String pin;       // 必填
        public Integer format;   // 0: base64, 1: url
        public String url;       // format=1 时必填
        public String content;   // format=0 时必填（Base64）
        public Integer size;     // Base64 长度

        public static CmdUserPic fromBase64(String pin, String base64) {
            CmdUserPic p = new CmdUserPic();
            p.pin = pin;
            p.format = 0;
            p.content = base64;
            p.size = base64Length(base64);
            return p;
        }
        public static CmdUserPic fromUrl(String pin, String url) {
            CmdUserPic p = new CmdUserPic();
            p.pin = pin;
            p.format = 1;
            p.url = url;
            p.size = null; // 协议允许 URL 方式不必给 size
            return p;
        }
    }

    /** 下发比对照片（biophoto） */
    public static class CmdBioPhoto {
        public String pin;       // 必填
        public Integer type;     // 常见 9: 可见光人脸
        public Integer format;   // 0: base64, 1: url
        public String url;       // format=1 时必填
        public String content;   // format=0 时必填（Base64）
        public Integer size = 0;     // Base64 长度
        public Integer no;     // 默认 0
        public Integer index;

        public static CmdBioPhoto fromBase64(String pin, String base64) {
            CmdBioPhoto p = new CmdBioPhoto();
            p.pin = pin;
            p.type = 9;
            p.format = 0;
            p.content = base64;
            p.size = base64Length(base64);
            return p;
        }
        public static CmdBioPhoto fromUrl(String pin, String url) {
            CmdBioPhoto p = new CmdBioPhoto();
            p.pin = pin;
            p.type = 9;
            p.format = 1;
            p.url = url;
            p.size = null;
            return p;
        }
    }

    /* ========================= 构建 UPDATE 子命令（略） ========================= */

    /** UPDATE user —— 新增/下发用户信息 */
    public static String buildUpdateUser(int cmdId, CmdUser u) {
        Objects.requireNonNull(u); Objects.requireNonNull(u.pin, "pin required");
        List<String> kv = new ArrayList<>();
        put(kv, "uid", u.uid);
        put(kv, "cardno", u.cardno);
        put(kv, "Pin", u.pin);
        put(kv, "password", u.password);
        put(kv, "Group", u.group);
        put(kv, "StarTtime", u.starttime);
        put(kv, "EndTime", u.endtime);
        put(kv, "Name", u.name);
        put(kv, "Privilege", u.privilege);
        put(kv, "disable", u.disable);
        put(kv, "verify", u.verify);
        return prefix(cmdId) + "DATA" + SP + "UPDATE" + SP + "user" + SP + String.join(HT, kv);
    }

    /** UPDATE userauthorize —— 门禁权限 */
    public static String buildUpdateUserAuthorize(int cmdId, CmdUserAuthorize a) {
        Objects.requireNonNull(a); Objects.requireNonNull(a.pin); Objects.requireNonNull(a.authorizeTimezoneId); Objects.requireNonNull(a.authorizeDoorId);
        List<String> kv = new ArrayList<>();
        put(kv, "Pin", a.pin);
        put(kv, "AuthorizeTimezoneId", a.authorizeTimezoneId);
        put(kv, "AuthorizeDoorId", a.authorizeDoorId);
   //     put(kv, "DevID", a.devId);
        put(kv, "StartTime", a.startTime);
        put(kv, "EndTime", a.endTime);
        return prefix(cmdId) + "DATA" + SP + "UPDATE" + SP + "userauthorize" + SP + String.join(HT, kv);
    }

    /** UPDATE userpic —— 用户照片（头像/考勤照） */
    public static String buildUpdateUserPic(int cmdId, CmdUserPic p) {
        Objects.requireNonNull(p); Objects.requireNonNull(p.pin); Objects.requireNonNull(p.format);
        List<String> kv = new ArrayList<>();
        put(kv, "pin", p.pin);
        putIfNotNull(kv, "size", p.size);
        put(kv, "format", p.format);
        put(kv, "url", p.url);
        put(kv, "content", p.content);
        return prefix(cmdId) + "DATA" + SP + "UPDATE" + SP + "userpic" + SP + String.join(HT, kv);
    }

    /** UPDATE biophoto —— 比对照片（可见光人脸等） */
    public static String buildUpdateBioPhoto(int cmdId, CmdBioPhoto p) {
        Objects.requireNonNull(p); Objects.requireNonNull(p.pin); Objects.requireNonNull(p.format);
        List<String> kv = new ArrayList<>();
        put(kv, "PIN", p.pin);
        put(kv, "Type", p.type);
        put(kv, "No", p.no);
        put(kv, "Index", p.index);
        putIfNotNull(kv, "Size", p.size);
        put(kv, "Content", p.content);
        put(kv, "Format", p.format);
        put(kv, "Url", p.url);
      //  put(kv, "PostBackTmpFlag", p.postBackTmpFlag);
        return prefix(cmdId) + "DATA" + SP + "UPDATE" + SP + "biophoto" + SP + String.join(HT, kv);
    }

    /** UPDATE InputIOSetting —— 输入控制设置 */
    public static String buildUpdateInputIOSetting(int cmdId, Map<String, ?> params) {
        Objects.requireNonNull(params, "InputIOSetting params required");
        List<String> kv = new ArrayList<>();
        params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .forEach(e -> put(kv, e.getKey(), e.getValue()));
        return prefix(cmdId) + "DATA" + SP + "UPDATE" + SP + "InputIOSetting" + SP + String.join(HT, kv);
    }

    /** UPDATE timezone —— 时区设置 */
    public static String buildUpdateTimezone(int cmdId, Map<String, ?> params) {
        Objects.requireNonNull(params, "timezone params required");
        List<String> kv = new ArrayList<>();
        params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .forEach(e -> put(kv, e.getKey(), e.getValue()));
        return prefix(cmdId) + "DATA" + SP + "UPDATE" + SP + "timezone" + SP + String.join(HT, kv);
    }

    /* ========================= 构建 DELETE 子命令 ========================= */
    public static String buildDeleteUserAuthorize(int cmdId, String pin) {
        return buildSimpleDelete(cmdId, "userauthorize", mapOf("Pin", pin));
    }
    public static String buildDeleteUser(int cmdId, String pin) {
        return buildSimpleDelete(cmdId, "user", mapOf("Pin", pin));
    }
    public static String buildDeleteTemplateV10(int cmdId, String pin, Integer fingerId /*可空*/) {
        Map<String, Object> cond = new LinkedHashMap<>();
        cond.put("Pin", pin);
        if (fingerId != null) cond.put("FingerID", fingerId);
        return buildSimpleDelete(cmdId, "templatev10", cond);
    }
    public static String buildDeleteBioData(int cmdId, int type, String pin, Integer no /*可空*/) {
        Map<String, Object> cond = new LinkedHashMap<>();
        cond.put("Type", type);
        if (pin != null) cond.put("Pin", pin);
        if (no != null) cond.put("No", no);
        return buildSimpleDelete(cmdId, "biodata", cond);
    }
    public static String buildDeleteBioPhoto(int cmdId, String pin, Integer type /*常用 9:可见光人脸*/) {
        Map<String, Object> cond = new LinkedHashMap<>();
        cond.put("PIN", pin);
        if (type != null) cond.put("Type", type);
        return buildSimpleDelete(cmdId, "biophoto", cond);
    }
    public static String buildDeleteUserPic(int cmdId, String pin) {
        return buildSimpleDelete(cmdId, "userpic", mapOf("pin", pin));
    }
    public static String buildDeleteExtUser(int cmdId, String pin) {
        return buildSimpleDelete(cmdId, "extuser", mapOf("Pin", pin));
    }
    public static String buildDeleteMulCardUser(int cmdId, String pin) {
        return buildSimpleDelete(cmdId, "mulcarduser", mapOf("Pin", pin));
    }

    /**
     * DELETE timezone —— 删除指定的时间段（按 TimeZoneId）
     * 生成：C:<cmdId>:DATA DELETE timezone TimeZoneId=<timezoneId>
     */
    public static String buildDeleteTimezoneById(int cmdId, int timezoneId) {
        Map<String, Object> cond = new LinkedHashMap<>();
        cond.put("TimezoneId", timezoneId);
        return buildSimpleDelete(cmdId, "timezone", cond);
    }

    /**
     * 删除指定表的全部数据：DATA DELETE <table> *
     */
    public static String buildDeleteAllRows(int cmdId, String table) {
        return prefix(cmdId) + "DATA" + SP + "DELETE" + SP + table + SP + "*";
    }

    private static String buildSimpleDelete(int cmdId, String table, Map<String, ?> cond) {
        String tail = cond.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(HT));
        return prefix(cmdId) + "DATA" + SP + "DELETE" + SP + table + SP + tail;
    }

    /* ========================= 组合：新增/替换 ========================= */

    /**
     * 一键新增：用户 + 门禁权限(可多条) + 用户照片(可选) + 比对照片(可选)
     * 返回顺序：user → userauthorize(多条) → userpic → biophoto
     */
    public static List<String> buildAddUserBundle(int startCmdId, CmdUser user,
                                                 List<CmdUserAuthorize> authList /*可空*/, CmdUserPic userPic /*可空*/, CmdBioPhoto bioPhoto /*可空*/) {
        List<String> cmds = new ArrayList<>();
        int id = startCmdId;
        cmds.add(buildUpdateUser(id++, user));
        if (authList != null) {
            for (CmdUserAuthorize a : authList) cmds.add(buildUpdateUserAuthorize(id++, a));
        }
        if (userPic != null) cmds.add(buildUpdateUserPic(id++, userPic));
        if (bioPhoto != null) cmds.add(buildUpdateBioPhoto(id++, bioPhoto));
        return cmds;
    }

    /**
     * “伪更新”：先删除后新增（更安全更一致）。
     * 默认删除内容：门禁权限、指纹模板(templatev10 全部)、一体化(biodata：按 type 可选)、比对照片(type=9)、用户照片、扩展用户、
     * 最后删除 user 本体。随后重新下发。
     */
    public static List<String> buildReplaceUserBundle(int startCmdId, String pin, boolean alsoDeleteBiodataType9,
                                                      CmdUser user, List<CmdUserAuthorize> authList /*可空*/, CmdUserPic userPic /*可空*/, CmdBioPhoto bioPhoto /*可空*/) {
        List<String> cmds = new ArrayList<>();
        int id = startCmdId;
        // 1) 删除阶段（先删依赖，最后删 user）
        cmds.add(buildDeleteUserAuthorize(id++, pin));
        cmds.add(buildDeleteTemplateV10(id++, pin, null)); // 全部手指模板
        if (alsoDeleteBiodataType9) cmds.add(buildDeleteBioData(id++, 9, pin, null)); // 一体化可见光
        cmds.add(buildDeleteBioPhoto(id++, pin, 9));       // 比对照片（可见光人脸）
        cmds.add(buildDeleteUserPic(id++, pin));           // 用户照片
        cmds.add(buildDeleteExtUser(id++, pin));           // 扩展用户（有则删）
        cmds.add(buildDeleteMulCardUser(id++, pin));       // 一人多卡（有则删）
        cmds.add(buildDeleteUser(id++, pin));              // 最后删 user

        // 2) 重建阶段
        cmds.addAll(buildAddUserBundle(id, user, authList, userPic, bioPhoto));
        return cmds;
    }

    /* ========================= 辅助 ========================= */

    private static String prefix(int cmdId) { return "C:" + cmdId + ":"; }

    private static void put(List<String> kv, String k, Object v) {
        if (v == null) return;
        kv.add(k + "=" + v);
    }
    private static void putIfNotNull(List<String> kv, String k, Object v) { put(kv, k, v); }

    private static Map<String, String> mapOf(String k, String v) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(k, v); return m;
    }

    /** 计算 Base64 字符串长度（按协议要求：编码后的长度） */
    private static int base64Length(String base64) {
        return base64 != null ? base64.getBytes(StandardCharsets.US_ASCII).length : 0;
    }

    /* ========================= 用法示例 ========================= */
    public static void main(String[] args) {
        CmdSeq seq = new CmdSeq(300);

        // 业务：新增一个用户 + 门禁权限 + 头像 + 比对照
        CmdUser user = new CmdUser("1001");
        user.name = "张三";
        user.group = "1";
        user.privilege = "0";
        user.disable = "0";
        user.verify = "0";

        List<CmdUserAuthorize> auths = Arrays.asList(
                new CmdUserAuthorize("1001", 1, 7) {{ devId = 1; }},  // LOCK1~3
                new CmdUserAuthorize("1001", 2, 15) {{ devId = 1; }}  // ALL DOORS
        );

        CmdUserPic up = CmdUserPic.fromBase64("1001", "<BASE64_USERPIC>");
        CmdBioPhoto bp = CmdBioPhoto.fromBase64("1001", "<BASE64_BIOPHOTO>");

        List<String> addCmds = buildAddUserBundle(seq.next(), user, auths, up, bp);
        addCmds.forEach(System.out::println);

        // 业务：伪更新（删除后重建）
        List<String> replaceCmds = buildReplaceUserBundle(seq.next(), "1001", true, user, auths, up, bp);
        replaceCmds.forEach(System.out::println);
    }
}