# WithYourHand · 手写代码 100 天

两个伙伴，用 React、Java 和 Rust，把想法写成代码。

不限定每天的题目：一个 API、一个页面、一个算法、一点性能优化，都可以是今天的内容。先让代码跑起来，再慢慢打磨。

## 技术与目录

| 项目 | 技术 | 默认地址 | 用途 |
| --- | --- | --- | --- |
| `frontend` | React 19 + JavaScript + Vite 8 | http://127.0.0.1:5173 | 共享前端、服务状态与接口试用 |
| `WithYourHandQBH` | Java 25 + Maven + Spring Boot 4.1.2 | http://127.0.0.1:8081 | QBH 的独立后端 |
| `WithYourHandYmy` | Java 25 + Maven + Spring Boot 4.1.2 | http://127.0.0.1:8082 | Ymy 的独立后端 |
| `WithYourHandRust` | Rust 1.93.0 + Axum + Tokio | http://127.0.0.1:8083 | 计算接口和性能实验 |
| `scripts` | PowerShell | — | 本地环境、启动与校验 |
| `days` | Markdown | — | 100 天的手写记录 |
| `docs` | Markdown | — | 接口说明、协作规范 |

两个 Java 项目各有启动类、配置、接口与测试，互不依赖。根目录的 Maven 聚合工程只负责统一构建。暂不接入数据库、缓存或消息队列，需要时再添加。

## 本地环境

沿用项目建立时的本机版本：

- JDK **25.0.1**，`JAVA_HOME` 与 `PATH` 都指向同一套 JDK。
- Maven **3.9.10**（也兼容更新的 Maven 3.9.x）。
- Rust / Cargo **1.93.0**，Windows 使用现有的 MSVC 工具链。
- Node.js **22.14.0**、npm **11.6.1**。

脚本复用已经安装的工具，不下载 JDK、Node 或 Rust 工具链。`Cargo.toml` 声明 Rust 1.93.0 最低版本，CI 固定使用 1.93.0；本地不自动安装或切换工具链。

所有本地下载、依赖、缓存和临时文件都保存在仓库目录内：npm / Maven / Cargo 下载位于 `.cache`，前端依赖位于 `frontend/node_modules`，Java 构建输出位于各模块 `target`，Rust 构建输出位于 `.cache/rust-target`。这些内容不会提交到 Git。请通过以下脚本下载依赖与启动项目，使缓存路径始终生效。

## 第一次运行

在项目根目录打开 PowerShell：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup.ps1
```

再打开四个终端，分别执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1 qbh
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1 ymy
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1 rust
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1 frontend
```

打开 http://127.0.0.1:5173 。按 `Ctrl+C` 停止当前终端的服务。也可以只启动自己正在使用的项目，其余服务显示“未连接”是正常状态。

前端开发服务器通过 Vite 代理访问三个后端，不需要配置宽泛的 CORS。后端默认仅监听本机。端口与接口见 [接口说明](docs/api.md)。前端 `build` 生成的静态文件需要由正式服务器配置相同的 `/api/*` 反向代理；`preview` 仅检查静态构建，不提供开发代理。

## 开始写今天的代码

1. 从 `main` 创建分支，例如 `qbh/day-001-hello` 或 `ymy/day-001-hello`。
2. 在自己的 Java 项目或共享前端 / Rust 项目中实现一个小功能。
3. 按 [每日模板](days/TEMPLATE.md) 记录做了什么、怎么验证、学到了什么。
4. 运行校验并提交，通过 Pull Request 合并。

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify.ps1
```

校验内容包括两个 Java 服务的 HTTP 集成测试、Rust 格式 / Clippy / 测试，以及前端 ESLint / 生产构建。GitHub Actions 会执行同样的检查。

更多约定见 [协作规范](CONTRIBUTING.md)。
