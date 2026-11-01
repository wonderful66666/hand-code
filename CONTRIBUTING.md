# 协作规范

规则保持简单，服务于每天动手写代码。

## 分工与分支

- `WithYourHandQBH` 和 `WithYourHandYmy` 分别是两人的独立 Java 项目，不复制对方的业务代码，也不互相引用模块。
- `frontend` 与 `WithYourHandRust` 为共享项目，修改前先沟通涉及的接口。
- 使用三个固定分支：`ymy` 供 Ymy 长期开发，`qbh` 供 QBH 长期开发，`main` 汇总双方确认后的成果。日常功能和每日练习都在自己的固定分支上继续提交。
- 一个提交尽量解决一件事。提交示例：`feat(qbh): 添加用户查询接口`、`fix(rust): 处理空数组`、`docs(day-001): 记录第一次接口联调`。
- 日常开发通过 `ymy → main` 或 `qbh → main` 的 Pull Request 合并，CI 通过后请伙伴看一眼。仓库中提供检查工作流；分支保护规则需要在 GitHub 仓库设置中另行开启。

## 第一次切换到个人分支

固定分支已发布到远端。克隆仓库后，在项目根目录执行：

```powershell
git fetch origin
git switch ymy
```

QBH 将第二行换成 `git switch qbh`。本地还没有该分支时，Git 会根据对应的 `origin/ymy` 或 `origin/qbh` 创建跟踪分支。朋友需要先获得仓库写入权限，才能推送自己的分支。

## 每天开发与提交

开始前先提交手头已有的改动，再把最新的 `main` 合入自己的分支。以下以 Ymy 为例，QBH 将 `ymy` 替换为 `qbh`：

```powershell
git switch ymy
git fetch origin
git merge origin/main
```

这一步可以拿到伙伴已经合入 `main` 的代码。完成今天的功能后，先运行项目校验：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify.ps1
```

校验通过后，检查并提交需要上传的文件：

```powershell
git status
git add .
git commit -m "feat(ymy): 完成今天的功能"
git push
```

## 合并成果与继续开发

1. 在 GitHub 创建 Pull Request：`base` 选择 `main`，`compare` 选择自己的 `ymy` 或 `qbh`。
2. 等待 CI 通过，请伙伴检查本次改动。
3. 选择 **Create a merge commit**，再点击 **Merge pull request → Confirm merge**。固定分支反复合并时，统一使用普通合并来保留分支间的合并关系。
4. 合并后保留 `ymy` 和 `qbh` 分支，不删除个人分支。
5. 下一次开发前，在自己的分支上再次执行 `git fetch origin`、`git merge origin/main`，然后继续提交。再次产生需要汇总的内容时，新建一个 Pull Request，继续使用同一个个人分支。

如果同步 `main` 时出现冲突，先在 VS Code 中逐个解决冲突文件，保留双方需要的代码并移除冲突标记，再执行：

```powershell
git add .
git commit -m "merge: 解决与 main 的合并冲突"
git push
```

只有 `git merge` 明确提示冲突时，才需要上述冲突处理步骤。各自推送自己的固定分支，不强制覆盖远端历史。

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
