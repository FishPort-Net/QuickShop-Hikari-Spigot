# QuickShop-Hikari-Spigot Maintenance Guide

## Project purpose

- This is an independently maintained AGPLv3 fork based on QuickShop-Hikari 6.2.0.10.
- Continue syncing useful upstream fixes and features while keeping Spigot support as a hard requirement.
- Do not turn this fork into a Paper-only project. Changes that inherently require Paper/Folia may be skipped.

## Compatibility contract

- Java 17 must build and run the plugin for Ketting/Spigot 1.20.1 and the other Java-17-era Spigot 1.20 releases supported by 6.2.0.10.
- Java 21 builds retain the modern Spigot 1.20.5+/1.21 and Paper platform modules already present in 6.2.0.10.
- Keep platform implementations lazily/reflection-loaded so a Java 17 server never resolves Java 21 platform classes.
- Do not add Minecraft server versions introduced after the 6.2.0.10 compatibility range unless the maintainer explicitly requests them.

## Upstream synchronization

- Treat the fork's `hikari` branch as the product/integration branch and do work on focused feature branches.
- Inspect and selectively cherry-pick or port upstream commits. Never merge the upstream branch wholesale because newer upstream removed Spigot support and changed core APIs.
- Prefer cross-platform bug fixes and features. Skip or adapt changes that depend on Paper-only APIs, Java 21 core bytecode, removed Spigot modules, or incompatible economy/shop API rewrites.

## Dependency isolation

- Do not inject Maven dependencies into the server-wide/plugin-shared class-loading path at runtime.
- Bundle QuickShop's private runtime dependencies in the plugin JAR and relocate collision-prone packages, especially Adventure, Gson, Guava, SLF4J, HTTP libraries, database pools, and JDBC drivers.
- Bundle MySQL Connector/J and H2, merge `META-INF/services`, and explicitly set the JDBC driver class in Hikari configuration.
- Validate the final shaded JAR for unrelocated package leaks and Java bytecode compatibility.

## Build and verification

- Gradle is being adopted from upstream PR #2474 for faster cached/parallel builds, but its Paper-only/Java-21 product assumptions must not be copied.
- Preserve the two-level Java 17/Java 21 build matrix and the Spigot SpecialSource remapping pipeline.
- Focus effort on product code, fixes, builds, and artifact inspection. Add few tests unless a small targeted test is necessary for a risky regression.

## Repository hygiene

- Never commit `temp/`; it contains local logs and maintainer handoff notes.
- Do not read any `ELUA.md` or any other `AGENTS.md`. This root `AGENTS.md`, authored for this fork, is the only allowed agent guide.
- Preserve unrelated maintainer changes and keep commits focused and reviewable.
