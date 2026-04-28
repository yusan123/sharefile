# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

sharefile 是一个基于 Spring Boot + Thymeleaf 的简易文件共享盘应用，支持上传、下载、删除、批量打包下载、Excel 导出文件列表等功能。配置驱动的功能开关设计，可单独开启/关闭各项功能。

## 常用命令

```shell
# 编译打包
mvn clean package

# 运行（Linux）
java -Dfile.encoding=utf-8 -jar target/sharefile-0.0.1-SNAPSHOT.jar

# 使用脚本构建和启动
sh script/build.sh
sh script/start.sh
sh script/shutdown.sh
```

## 架构

- **后端**: Spring Boot 2.2.9 + Sa-Token 权限认证
- **前端**: Thymeleaf 模板，两套 UI 并存 (`index` 和 `new_index`)
- **存储**: 本地文件系统，路径和空间大小通过 `application.yml` 配置
- **Excel 导出**: Alibaba EasyExcel

### 关键路径

| 文件 | 作用 |
|------|------|
| `src/main/resources/application.yml` | 存储路径、最大空间、功能开关(sa-token 账号密码也在此) |
| `src/main/java/com/yu/controller/FileController.java` | 核心业务逻辑(上传/下载/删除/打包) |
| `src/main/java/com/yu/util/FileToZip.java` | 打包下载 ZIP 工具类 |
| `src/main/java/com/yu/interceptor/OperationInterceptor.java` | 操作日志拦截器 |
| `src/main/resources/templates/new_index.html` | 新版前端页面 |

### 配置项说明

```yaml
file:
  path: D:\root                    # 文件存储目录
  maxSpace: 1024                   # 最大可用空间(MB)
  isOpenUpload: true               # 上传功能开关
  isOpenDownload: true            # 下载功能开关
  isOpenDelete: true               # 删除功能开关
  isOpenDeleteAll: false           # 删除所有功能开关
  isOpenDownloadAll: true          # 打包下载开关
  isOpenComplexChar: true         # 中文/特殊字符文件名支持

sa:
  name: root                       # Sa-Token 管理员账号
  pwd: 1234                        # Sa-Token 管理员密码
```

## 前端设计

存在 `frontend-design` 技能用于优化页面样式和交互，遵循该技能的审美指南避免 AI 通用样式。

## TODO 待办

参见 `TODO.md`，重要项包括：
- 多线程上传(当前实现有问题，剩余空间判断在并发下不准确)
- 登录认证功能(考虑 Spring Security)
- 文件信息对齐优化
