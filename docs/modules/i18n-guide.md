# Atlas SRM 多语言 i18n 国际化方案

## 一、架构设计

```
请求 Accept-Language: en-US
        │
        ▼
┌─────────────────┐
│  LocaleResolver │  从 Accept-Language 请求头解析 Locale
└────────┬────────┘
         │
         ▼
┌─────────────────┐    未命中    ┌─────────┐    未命中
│ Caffeine L1 缓存 │ ──────────► │ MySQL   │ ──────────► 返回 key 或
│ (本地, 5000条)  │ ◄────────── │ L2 存储 │             defaultMessage
│ TTL=30min       │   回填缓存   └─────────┘
└─────────────────┘
         │ 命中
         ▼
   翻译文本 + MessageFormat 参数化
```

三层防护：
- **L1 Caffeine**：本地内存缓存，启动预热，命中率 >95%
- **L2 MySQL**：持久化存储，支持运行时动态增删改
- **L3 Fallback**：兜底返回 message key 或 defaultMessage

## 二、数据库表

### i18n_language — 语言表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| code | VARCHAR(10) UNIQUE | 语言代码：zh-CN / en-US / ja-JP |
| name | VARCHAR(50) | 语言名称 |
| native_name | VARCHAR(50) | 本地名称 |
| enabled | TINYINT | 1启用 0禁用 |
| sort_order | INT | 排序 |

### i18n_message — 翻译消息表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| message_key | VARCHAR(200) | 消息键 |
| language_code | VARCHAR(10) | 语言代码 |
| message_value | TEXT | 翻译文本（支持 `{0}` `{1}` 占位符） |
| module | VARCHAR(64) | 所属模块 |
| description | VARCHAR(200) | 键描述 |

唯一约束：`(message_key, language_code)`

## 三、预置消息键清单

迁移脚本 `V94__i18n_tables.sql` 预置了 **120+ 条** 中英文消息，覆盖 9 个模块：

| 模块 | 键前缀 | 消息数 | 典型键 |
|------|--------|--------|--------|
| common | `common.*` | 16组 | `common.success/error/unauthorized/not_found/param_invalid` |
| user | `user.*` | 8组 | `user.not_exist/disabled/login_success/password_error` |
| supplier | `supplier.*` | 8组 | `supplier.not_exist/frozen/qualification_expired` |
| contract | `contract.*` | 6组 | `contract.not_exist/already_approved/amount_exceed` |
| purchase | `purchase.*` | 7组 | `purchase.order_not_exist/cannot_modify/bid_started` |
| inventory | `inventory.*` | 5组 | `inventory.stock_insufficient/sku_not_exist/transfer_success` |
| receipt | `receipt.*` | 5组 | `receipt.not_exist/duplicate/confirmed/inspected` |
| workflow | `workflow.*` | 6组 | `workflow.not_exist/task_not_found/process_started` |
| open | `open.*` | 5组 | `open.auth_failed/rate_limited/connectivity_ok` |

## 四、核心 API

### I18nService — 翻译服务

```java
// 注入使用
@Autowired
private I18nService i18nService;

// 1. 简单翻译
String msg = i18nService.translate("common.success", locale);
// zh-CN → "操作成功"
// en-US → "Operation successful"

// 2. 带参数翻译
String msg = i18nService.translate("common.param_missing", locale, "orderId");
// zh-CN → "缺少必要参数: orderId"
// en-US → "Missing required parameter: orderId"

// 3. 带默认值
String msg = i18nService.translate("custom.key", locale, "默认文本");

// 4. 批量导出模块翻译
Map<String, String> all = i18nService.translateAll("supplier", locale);
```

### @I18nTranslate — Controller 返回体自动翻译

```java
@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    // 方式一：自动扫描 @I18nField
    @I18nTranslate
    @GetMapping("/{id}")
    public Result<SupplierVO> getSupplier(@PathVariable Long id) { ... }
}

// VO 定义
public class SupplierVO {
    private Long id;
    private String name;

    @I18nField   // ← 标记可翻译字段，切面自动翻译
    private String statusText;  // "FROZEN" → "已冻结" (zh-CN) / "Frozen" (en-US)
}
```

```java
// 方式二：指定字段名列表
@I18nTranslate(fields = {"statusText", "levelText"})
@GetMapping("/list")
public Result<List<SupplierVO>> list() { ... }
```

### I18nMessageSource — Spring MessageSource 桥接

```java
// 兼容 @Valid 校验消息、Thymeleaf 等 Spring 国际化场景
@Autowired
private MessageSource messageSource;  // 自动路由到 I18nMessageSource

String msg = messageSource.getMessage("common.success", null, locale);
```

## 五、管理接口

所有接口前缀：`/api/i18n`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/languages` | 获取支持的语言列表 |
| GET | `/messages?module=supplier&lang=en-US` | 按模块导出翻译 |
| GET | `/messages/{key}?lang=zh-CN` | 查询单条翻译 |
| POST | `/messages` | 新增/更新单条翻译 |
| POST | `/messages/batch` | 批量导入翻译 |
| DELETE | `/messages/{key}` | 删除某 key 的所有语言翻译 |
| POST | `/messages/refresh` | 刷新所有缓存（加翻译后调用） |

### 新增翻译示例

```http
POST /api/i18n/messages
Content-Type: application/json

{
    "messageKey": "order.created",
    "languageCode": "zh-CN",
    "messageValue": "订单 {0} 已创建",
    "module": "purchase",
    "description": "订单创建成功消息"
}
```

```http
POST /api/i18n/messages/batch
Content-Type: application/json

[
    {
        "messageKey": "order.created",
        "languageCode": "zh-CN",
        "messageValue": "订单 {0} 已创建"
    },
    {
        "messageKey": "order.created",
        "languageCode": "en-US",
        "messageValue": "Order {0} has been created"
    }
]
```

## 六、多语言切换

### 后端
- 默认语言：`zh-CN`（`I18nConfig.localeResolver().setDefaultLocale(Locale.SIMPLIFIED_CHINESE)`）
- 前端通过 `Accept-Language` 请求头控制：
  ```
  Accept-Language: en-US   → 返回英文
  Accept-Language: zh-CN   → 返回中文
  Accept-Language: ja-JP   → 返回日文（需先添加日文翻译数据）
  ```
- 也可通过 `LocaleContextHolder` 在代码中动态设置：
  ```java
  LocaleContextHolder.setLocale(Locale.US);
  ```

### 前端
```javascript
// Axios 拦截器统一设置请求头
axios.interceptors.request.use(config => {
  config.headers['Accept-Language'] = i18n.locale; // 如 'en-US'
  return config;
});

// Vue i18n
this.$i18n.locale = 'en-US';

// React i18next
i18n.changeLanguage('en-US');
```

## 七、集成全局异常处理

`GlobalExceptionHandler` 集成 I18nService 后（示例改造）：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private I18nService i18nService;

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e, HttpServletRequest request) {
        Locale locale = request.getLocale();
        String msg = i18nService.translate("error." + e.getErrorCode(), locale, e.getMessage());
        return Result.fail(e.getCode(), msg);
    }
}
```

## 八、性能说明

| 指标 | 值 |
|------|-----|
| Caffeine 缓存容量 | 5000 条 |
| 缓存过期策略 | 写入后 30 分钟 |
| 缓存预热 | 启动时自动加载 zh-CN + en-US |
| 预期命中率 | >95%（常规场景下未修改的翻译） |
| 缓存击穿防护 | 双重检查锁（synchronized + re-check） |
| 参数化渲染 | `java.text.MessageFormat`（O(1) 字符串替换） |

## 九、模块结构

```
atlas-common/src/main/java/com/atlas/common/i18n/
├── annotation/
│   ├── I18nField.java         # 字段级翻译标记
│   └── I18nTranslate.java     # 方法级自动翻译注解
├── aspect/
│   └── I18nAspect.java        # AOP 切面：自动扫描并翻译 Result.data
├── config/
│   ├── I18nConfig.java        # LocaleResolver + I18nMessageSource 注册
│   └── I18nMessageSource.java # Spring MessageSource 桥接
├── controller/
│   └── I18nController.java    # 管理接口：CRUD + 缓存刷新
├── entity/
│   ├── I18nLanguage.java      # 语言实体
│   └── I18nMessage.java       # 翻译消息实体
├── mapper/
│   ├── I18nLanguageMapper.java
│   └── I18nMessageMapper.java
└── service/
    └── I18nService.java       # 核心翻译服务（Caffeine + double-check）
```

迁移文件：
```
atlas-common/src/main/resources/db/migration/V94__i18n_tables.sql
sql/V94__i18n_tables.sql
```
