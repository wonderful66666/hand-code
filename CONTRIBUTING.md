# 协作规范

规则保持简单，服务于每天动手写代码。

## 分工与分支

- `WithYourHandQBH` 和 `WithYourHandYmy` 分别是两人的独立 Java 项目，不复制对方的业务代码，也不互相引用模块。
- `frontend` 与 `WithYourHandRust` 为共享项目，修改前先沟通涉及的接口。
- 从 `main` 创建分支：`qbh/day-001-topic`、`ymy/day-001-topic`，共享维护可用 `chore/topic`。
- 一个提交尽量解决一件事。提交示例：`feat(qbh): 添加用户查询接口`、`fix(rust): 处理空数组`、`docs(day-001): 记录第一次接口联调`。
- 通过 Pull Request 合并，CI 通过后请伙伴看一眼。仓库中提供检查工作流；分支保护规则需要在 GitHub 仓库设置中另行开启。

## 代码与接口

- Java 使用 4 空格，React / JSON / YAML 使用 2 空格，Rust 使用 `cargo fmt`。
- Java 按功能逐步组织包；确实有业务逻辑时再抽 `service`，需要存储时再加 `repository`。不要先堆空目录和空接口。
- API 分别使用 `/api/qbh`、`/api/ymy`、`/api/rust` 前缀；JSON 属性使用 `camelCase`。
- 返回正确的 HTTP 状态码；参数错误说明原因，服务内部错误不向客户端暴露堆栈。
- 新行为附带能验证边界或实际业务的测试，修复问题时优先增加能复现它的用例。
- Rust 的优势通过测量证明：给出输入规模、运行条件和耗时，不凭语言名称认定接口更快。

## 依赖、配置与记录

- 通过 `scripts` 中的脚本安装和运行，所有下载与缓存保留在项目目录内。
- 提交 `frontend/package-lock.json` 和 `WithYourHandRust/Cargo.lock`，升级依赖时一并更新。
- 密钥、令牌、数据库密码放在环境变量或被忽略的本地文件中，不提交 `.env`、SSH 文件、缓存和构建产物。
- 每天只建实际写过的记录，不预先生成 100 份空文件，也不把“框架初始化”等同于完成某一天。
- 两个人同一天分别记录为 `days/day-001-qbh.md`、`days/day-001-ymy.md`。
