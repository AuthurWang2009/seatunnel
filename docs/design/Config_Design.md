# 配置系统重构计划

## 目标描述
本次重构的目标是改进 SeaTunnel 的配置系统，解决以下问题：
1.  **解耦**: 移除公共 API 中对 `com.typesafe.config` 的强依赖，对配置逻辑进行抽象。
2.  **多格式支持**: 除 HOCON 和 SQL 外，增加对 YAML, JSON, 和 Properties 格式的支持。
3.  **整合**: 集中管理目前分散在 `seatunnel-api`, `seatunnel-common`, 和 `seatunnel-config` 中的配置工具和解析逻辑。
4.  **构建器 API**: 提供流畅的 API，允许用户通过代码从零开始构建 `Config` 对象。

## 需要用户确收
> [!IMPORTANT]
> 此重构引入了一个新的 `Config` 接口，该接口最终将取代 `ReadonlyConfig`。在一段时间内将保持向后兼容性，但会添加弃用提示（Deprecation notices）。

## 建议变更

### 逻辑抽象 (`seatunnel-api`)

#### [新增] `org.apache.seatunnel.api.configuration.Config` (接口)
定义独立于底层实现的配置访问契约。
- `getString(String key)`
- `getInt(String key)`
- `getBoolean(String key)`
- `getConfig(String key)`: 返回嵌套的 `Config`。
- `get<T>(Option<T> option)`
- `toMap()`
- `withValue(String key, Object value)`: 返回一个新的 `Config` 实例，包含更新后的值（Copy-on-Write）。
- `merge(Config other)`: 返回一个新的 `Config` 实例，合并了其他配置的覆盖值。

> [!NOTE]
> 所有的 get/withValue 方法中的 `key` 参数均需支持 "dotted-path" (点号分隔路径) 访问方式 (如 "a.b.c")。
> 特别地，对于 `withValue` 等修改操作，若路径中的中间节点不存在，则需自动创建对应的嵌套 Map 结构。

#### [新增] `org.apache.seatunnel.api.configuration.AbstractConfig`
`Config` 的基础实现，处理通用逻辑（如 `Option` 解析）。
不要在AbstractConfig暴露任何 Typesafe 相关 protected API，确保未来可以彻底删除 typesafe 而不影响继承结构

#### [修改] `org.apache.seatunnel.api.configuration.ReadonlyConfig`
继承自AbstractConfig用于取代旧的ReadonlyConfig，重构以实现新的 `Config` 接口，内部可能暂时仍使用 `Map`，但对外暴露新的 API。

### 格式支持与加载 (`seatunnel-config`)

#### [新增] `org.apache.seatunnel.api.configuration.ConfigLoader`
**Facade (门面)**：这是用户获取配置的唯一建议入口。它向下屏蔽了具体的解析实现细节。
- 内部通过 `ServiceLoader` 机制自动发现并加载注册的 `ConfigParser` 实现。
- `load(Path path)`: 自动根据文件后缀匹配合适的 Parser。
- `load(String content, ConfigType type)`: 指定类型加载。

#### [新增] `org.apache.seatunnel.common.config.ConfigType` (枚举)
支持的配置类型枚举：`HOCON`, `JSON`, `YAML`, `PROPERTIES`。
支持基于文件后缀的模式自动识别 (如 .conf/.json/.yaml/.properties)。

#### [新增] `org.apache.seatunnel.common.parser.ConfigParser` (SPI 接口)
供开发者扩展新格式的接口（用户不可见）。解析器无需关注最终的 `Config` 对象实现，只需返回标准的 Java `Map` 结构即可，从而彻底解耦具体的解析库（如 Jackson/SnakeYAML）与上层 API。
- `ConfigType getConfigType()`
- `Map<String, Object> parse(Path path)`
- `Map<String, Object> parse(String content)`

实现类：
- `HoconConfigParser` (适配 Typesafe Config)
- `YamlConfigParser` (使用 Jackson 或 SnakeYAML)
- `JsonConfigParser` (使用 Jackson)
- `PropertiesConfigParser`

### 修改机制 (Modification Mechanism)

为保证 Config 的不可变性 (Immutability)，所有修改操作应采用 Copy-on-Write 策略或通过 Builder 进行。

#### Copy-on-Write 策略
接口本身不提供 `set` 方法，而是提供生成新实例的方法。
- `withValue(String key, Object value)`: 适用于少量的、局部的覆盖修改。
- `merge(Config other)`: 适用于整体配置的合并（如 defaults + overrides）。执行深度合并 (Deep Merge)，若键冲突则 `other` 覆盖 `this`。

#### Builder 修改策略
- `ConfigBuilder.config(Config config)`: 允许将现有 Config 加载到 Builder 中，作为修改的基础。修改完成后再次 `build()` 生成新的不可变实例。

### 构建器 API (`seatunnel-api`)

#### [新增] `org.apache.seatunnel.api.configuration.ConfigBuilder`
用于构建配置的流式 API。
- `ConfigBuilder.create()`
- `config(Config config)`: 基于现有 Config 初始化。
- `put(String key, Object value)`
- `put(String key, Object value)`
- `build()` -> 返回 `Config`

#### [新增] `org.apache.seatunnel.api.configuration.JobConfigBuilder`
业务层面的高阶构建器，用于快速构建完整的 SeaTunnel 作业配置。

- `JobConfigBuilder.create()`
- `env(Config envConfig)`: 设置环境变量。
- `source(String pluginName, Consumer<ConfigBuilder> sourceConfig)`: 添加 Source 插件。
- `transform(String pluginName, Consumer<ConfigBuilder> transformConfig)`: 添加 Transform 插件。
- `sink(String pluginName, Consumer<ConfigBuilder> sinkConfig)`: 添加 Sink 插件。
- `build()` -> 返回 `Config` (包含完整的 env, source, transform, sink 结构)

### 工具类整合

#### [修改] `org.apache.seatunnel.api.configuration.util.ConfigUtil`
吸收 `TypesafeConfigUtils` 中的有用方法（即通用的 Map 处理），并标记 `seatunnel-common` 中的 `TypesafeConfigUtils` 为过时（Deprecated）。

## 迁移路径 (Migration Path)

为了确保系统的稳定性并平滑过渡，迁移将分为四个阶段进行：

### 第一阶段：API 定义与共存 (Definition & Coexistence)
- **目标**: 引入新 API，但不破坏现有代码。
- **动作**:
    1.  创建 `Config` 接口, `AbstractConfig`, `ConfigType` 枚举。
    2.  创建 `ConfigBuilder` 和 `JobConfigBuilder`。
    3.  此时新旧 API 共存，新功能（如 Builder）立即可用。

### 第二阶段：适配与实现 (Adapter & Implementation)
- **目标**: 让核心类 `ReadonlyConfig` 兼容新接口，并引入 SPI 实现。
- **动作**:
    1.  重构 `ReadonlyConfig`，使其实现 `Config` 接口。保留原有的 `toConfig()` 方法但标记为 `@Deprecated`。
    2.  在 `seatunnel-config` 模块中实现 `ConfigLoader` 和 `ConfigParser` SPI (HOCON, JSON, YAML, Properties)。
    3.  实现基于 Map 的标准 `ImmutableConfig`。

### 第三阶段：内部集成 (Internal Integration)
- **目标**: 系统内部组件切换到新机制，用户侧无感知。
- **动作**:
    1.  修改 `seatunnel-core` / `seatunnel-starter` 的启动逻辑，使用 `ConfigLoader.load()` 替代 `ConfigFactory.load()`。这使得 SeaTunnel 立刻获得多格式支持。
    2.  更新 `seatunnel-config-sql`，使其内部使用 `JobConfigBuilder` 或适配到新的 `Config` 结构。

### 第四阶段：API 演进 (API Evolution)
- **目标**: 逐步清理旧有的 Typesafe 依赖暴露。
- **动作**:
    1.  标记所有接收或返回 `com.typesafe.config.Config` 的公共方法为 `@Deprecated`。
    2.  在未来的大版本中，仅保留 `org.apache.seatunnel.api.configuration.Config` 接口。

## 验证计划

### 自动化测试
- `ConfigBuilder` 的单元测试。
- `ConfigLoader` 的单元测试，涵盖所有支持格式（JSON, YAML, Properties, HOCON）的样本文件。
- `ReadonlyConfig` 的回归测试，确保现有行为保持不变。

## 后续优化 (Future Work)
- **命名规范统一**: 考虑将 `Option` 重命名为 `ConfigOption`，将 `Options` 重命名为 `ConfigOptions`，以对齐 Flink/Spark 的命名风格并减少歧义。此项变更涉及范围较广，暂不包含在本次架构重构中。