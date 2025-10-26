# jeecg-module-hkclients (Hikvision NVR ISAPI client)

- RestTemplate + Apache HttpClient (pooling, keep-alive)
- 仅 JAXB 处理 XML（所有模型显式使用 http://www.hikvision.com/ver20/XMLSchema 命名空间字段）
- 支持：设备信息、通道列表、网络接口、录像日历、录像搜索、**每路码流(主/子/三)编解码参数**、并从 **status/channels** 兜底合并 codecType。

## 快速使用
- 直接 `@Autowired HKClients`，传入 `HkConn` 调用。
- 示例：见 `src/test/java/.../HKClientsSimpleTest.java`（IDEA 里右键运行，顶部常量改成你的设备参数）。
