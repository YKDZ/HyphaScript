# HyphaScript

English | [简体中文](README.zh-CN.md)

## Introduction

A lightweight JavaScript-like scripting language running on the JVM, originally designed to add dynamic functionality to `.yml` configuration files in Minecraft Paper plugins.

Its Abstract Syntax Tree (AST) is built using a custom implementation of the Pratt Parser.

## Features

- Native support for Adventure API `Component` template strings and `+` operations
- Uses `BigDecimal` for numeric storage
- Supports array and string slicing syntax
- Supports configuration-friendly function calls like `func{arg1="1", arg2="2"}`
- Simple lexical scoping mechanism
- Allows registering native Java functions via `ScriptObject` and `MethodHandle`
- Includes built-in utility functions for interacting with the Minecraft environment, such as `message(msg)` and `tp_to_player(player_name)`

## Use Cases

- [Deserializing specific objects from configuration](https://github.com/YKDZ/HyphaShop/blob/8408c229aa77dcb00f3aeecff179406be44f9876/plugin/src/main/java/cn/encmys/ykdz/forest/hyphashop/utils/ConfigUtils.java#L449)
- [Allowing users to configure how predefined functions are executed](https://github.com/YKDZ/HyphaShop/blob/8408c229aa77dcb00f3aeecff179406be44f9876/plugin/src/main/java/cn/encmys/ykdz/forest/hyphashop/script/pack/HyphaShopActionObject.java#L48)