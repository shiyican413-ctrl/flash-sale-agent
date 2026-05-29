# 秒杀系统项目文档

项目名称：`FlashSaleX`

这是一个面向 Java 后端面试的秒杀系统项目文档集。项目重点不是简单完成 CRUD，而是围绕高并发、库存一致性、缓存、异步削峰、接口安全、可观测性和 AI Agent 辅助运营来设计。

## 文档目录

- `01-项目定位与简历亮点.md`：项目目标、技术栈、简历写法、面试亮点。
- `02-功能清单.md`：用户端、管理端、秒杀链路、风控、Agent 功能。
- `03-系统架构设计.md`：整体架构、核心链路、缓存、消息队列、限流和一致性设计。
- `04-数据库设计.md`：核心表结构、索引建议、状态字段设计。
- `05-接口设计.md`：主要 REST API、请求响应、鉴权和错误码。
- `06-开发计划.md`：阶段拆分、里程碑、优先级。
- `07-Agent元素设计.md`：AI Agent 在秒杀系统里的落地场景和模块设计。
- `08-面试讲解稿.md`：面试时如何介绍这个项目。

## 推荐技术栈

- Java 17
- Spring Boot 3
- Spring Security + JWT
- MyBatis-Plus
- MySQL 8
- Redis 7
- RabbitMQ 或 RocketMQ
- Redisson
- Sentinel 或 Bucket4j
- Knife4j / Swagger
- JUnit 5
- Docker Compose

## 项目核心关键词

高并发、缓存预热、Redis 原子扣减、Lua 脚本、消息队列削峰、异步下单、防超卖、防重复下单、接口限流、库存一致性、订单状态机、幂等性、可观测性、AI Agent 运营助手。
