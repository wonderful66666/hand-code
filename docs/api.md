# 初始接口

服务均默认监听 `127.0.0.1`。本地开发由 Vite 将三个 `/api` 前缀转发到对应服务。

| 服务 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| QBH | GET | `/actuator/health` | 健康检查，`{"status":"UP"}` |
| QBH | GET | `/api/qbh/info` | 服务名、负责人、语言和实际 JVM 版本 |
| QBH | GET | `/api/qbh/hello?name=QBH` | 问候；不传名字时使用“朋友” |
| Ymy | GET | `/actuator/health` | 健康检查 |
| Ymy | GET | `/api/ymy/info` | 服务信息 |
| Ymy | GET | `/api/ymy/hello?name=Ymy` | 问候 |
| Rust | GET | `/health` | 健康检查 |
| Rust | GET | `/api/rust/info` | 服务信息；`version` 是应用版本 |
| Rust | POST | `/api/rust/stats` | 一次遍历计算整数统计量 |

## Java 示例

```powershell
Invoke-RestMethod 'http://127.0.0.1:8081/api/qbh/hello?name=QBH'
Invoke-RestMethod 'http://127.0.0.1:8082/api/ymy/hello?name=Ymy'
```

```json
{
  "service": "WithYourHandQBH",
  "message": "你好，QBH！今天也动手写点代码。"
}
```

名字最多 50 个字符，不能全部为空白。省略或传空字符串时使用默认名称。非法名字返回 HTTP 400：

```json
{
  "code": "INVALID_ARGUMENT",
  "message": "name 必须为 1–50 个字符，且不能全部为空白。"
}
```

## Rust 示例

```powershell
Invoke-RestMethod -Method Post `
  -Uri 'http://127.0.0.1:8083/api/rust/stats' `
  -ContentType 'application/json' `
  -Body '{"numbers":[1,2,3,4,5]}'
```

```json
{
  "count": 5,
  "sum": 15,
  "min": 1,
  "max": 5,
  "mean": 3.0
}
```

输入为 1–100000 个 32 位有符号整数；总和使用 64 位整数，均值为浮点数，可能有浮点舍入。请求体最大 1 MiB。空数组或数量超限返回 400，JSON 语法错误返回 400，字段类型错误返回 422，请求体过大返回 413，Content-Type 非 JSON 返回 415。该示例提供计算接口扩展起点，尚未做性能基准。

## 修改监听地址与端口

- Java：`SERVER_ADDRESS`（默认 `127.0.0.1`）；`QBH_PORT`（默认 `8081`）、`YMY_PORT`（默认 `8082`）。
- Rust：`RUST_ADDRESS`（默认 `127.0.0.1:8083`）。
- React：`frontend/vite.config.js` 中配置开发端口和代理。修改后端端口后同步修改代理地址。

这套框架面向本地练习，发布到公网前应按功能补充身份校验、存储、HTTPS、请求限制和部署配置。
