# HyphaScript

[English](README.md) | 简体中文

## 介绍

一个运行在 JVM 平台的，语法类似 JS 的轻量级脚本语言，最初被设计用于为 Minecraft Paper 插件的 `.yml` 配置添加动态特性。

其语法树（AST）由一个自行实现的 `Pratt Parser` 构建。

## 特性

- 原生支持 Adventure API 中 Component 的模板字符串和 `+` 运算
- 利用 `BigDecimal` 储存数字
- 支持数组和字符串切片语法
- 支持 `func{arg1="1", arg2="2"}` 的更利于在配置中使用的函数调用方式
- 简易的词法作用域机制
- 支持通过 `ScriptObject` 和 `MethodHandle` 注册原生 Java 函数
- 自带常用的与 Minecraft 环境交互的相关函数，如 `message(msg)` 和 `tp_to_player(player_name)`

## 用例

- [用于从配置反序列化特定对象](https://github.com/YKDZ/HyphaShop/blob/8408c229aa77dcb00f3aeecff179406be44f9876/plugin/src/main/java/cn/encmys/ykdz/forest/hyphashop/utils/ConfigUtils.java#L449)
- [让用户配置以某些方式执行预定义的函数](https://github.com/YKDZ/HyphaShop/blob/8408c229aa77dcb00f3aeecff179406be44f9876/plugin/src/main/java/cn/encmys/ykdz/forest/hyphashop/script/pack/HyphaShopActionObject.java#L48)