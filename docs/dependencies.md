# 依赖说明

前端与 Rust 分别提交 npm / Cargo 锁文件，Java 依赖由 Spring Boot 4.1.2 管理。升级时同时运行项目校验，避免只改版本号。

## minimatch 临时约束

ESLint 10.14.0 的依赖范围仍使用 minimatch 10。项目通过 npm `overrides` 固定到 minimatch **11.0.1**，处理 [GHSA-g3hv-75wg-m249](https://github.com/isaacs/minimatch/security/advisories/GHSA-g3hv-75wg-m249)。这是开发工具依赖，升级后需验证 ESLint 和前端构建。

后续 ESLint 的依赖范围包含修复版本时，可以删除此约束并重新生成锁文件，再运行 `npm audit` 与项目校验。

环境兼容要求参考 [Spring Boot 系统要求](https://docs.spring.io/spring-boot/system-requirements.html) 与 [Vite 环境要求](https://vite.dev/guide/)。
