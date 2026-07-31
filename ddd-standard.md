# DDD 开发范式

不同于传统 MVC 模式，本工程采用 DDD（领域驱动设计）四层架构，强调**业务逻辑内聚在领域层**、**依赖单向向下**。本文档为统一开发规范，各微服务模块（如 henry-user、henry-file）必须遵守。

## 1. 四层职责

| 层 | 职责 | 允许依赖 | 典型内容 |
| --- | --- | --- | --- |
| adapter（适配层） | 接收外部请求/事件并组装响应，不承载业务逻辑 | application、domain、common | Controller、Listener/Consumer、Job、WebSocket 端点、DTO 参数校验 |
| application（应用层） | 编排用例：组织领域对象完成一次业务，管理事务、权限、安全 | domain、common | `*ApplicationService`、输入/输出 DTO |
| domain（领域层） | 业务规则与领域模型，是最核心、最稳定的层 | common（纯共享库） | 实体、值对象、聚合、`*Repository` 接口、领域服务 |
| infrastructure（基础设施层） | 技术实现：数据库、外部服务、中间件、文件系统 | 其他三层均可 | `*PO`（持久化模型）、`*Mapper`、`*RepositoryImpl`、`*Assembler`、`*Client`、配置 |

## 2. 依赖规则（单向）

- **adapter → application → domain**：上层只能依赖下层，禁止反向（如 domain 依赖 infrastructure）。
- **infrastructure 可依赖所有层**：它是技术实现细节，通过实现 domain 的 `*Repository` 接口反向适配领域，但**接口定义在 domain**。
- **domain 不依赖任何层**：领域对象为纯净 Java（无 Spring/MyBatis 注解），只可依赖 `common` 这类**纯共享库**（无框架依赖、跨切面通用能力，如统一返回、异常、工具类）。`common` 不属于四层中的任何一层。
- **适配手段**：infrastructure 通过 `*Assembler` 在领域模型与持久化模型之间转换，而不是把持久化注解带进 domain。

```
adapter  ──▶ application ──▶ domain ◀── infrastructure（实现 domain 接口）
   ▲                           │
   └────────── common ◀────────┘ （共享纯库，各层均可依赖）
```

## 3. 标准目录结构

一个业务模块（微服务）的标准包结构：

```
com.henry.<module>/
├── adapter/
│   ├── controller/           # 对外 HTTP 接口
│   └── listener/             # 消息监听、定时任务等
├── application/
│   ├── dto/                  # 出入参 DTO：*Request / *Response / *DTO
│   └── XxxApplicationService.java
├── domain/
│   ├── model/                # 实体、值对象、聚合
│   ├── repository/           # 仓储接口（仅接口，无实现）
│   └── service/              # 领域服务（跨实体的业务规则）
└── infrastructure/
    ├── persistence/          # 持久化模型 *PO（MyBatis-Plus 实体）
    ├── mapper/               # MyBatis Mapper（BaseMapper<PO>）
    ├── repository/           # *RepositoryImpl（实现 domain 接口）
    ├── assembler/            # 领域模型 <-> PO 转换器
    ├── client/               # 外部服务/中间件调用
    └── config/               # 本模块配置类
```

## 4. 命名规范

| 层 | 类型 | 命名 |
| --- | --- | --- |
| adapter | 控制器 | `XxxController` |
| adapter | 监听器/任务 | `XxxListener`、`XxxConsumer`、`XxxJob` |
| application | 应用服务 | `XxxApplicationService` |
| application | 入参 DTO | `XxxRequest` |
| application | 出参 DTO | `XxxResponse`、`XxxDTO` |
| domain | 实体/值对象 | 领域业务名（如 `User`、`StoredFile`） |
| domain | 仓储接口 | `XxxRepository`（接口，无 Impl 后缀） |
| domain | 领域服务 | `XxxDomainService` |
| infrastructure | 持久化模型 | `XxxPO` |
| infrastructure | Mapper | `XxxMapper` |
| infrastructure | 仓储实现 | `XxxRepositoryImpl` |
| infrastructure | 转换器 | `XxxAssembler`（或 `XxxConverter`） |

## 5. 调用链路示例

以「用户登录」为例，调用方向严格自上而下：

```java
// adapter：只做参数接收与响应包装
@PostMapping("/login")
public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
    return Result.success(userApplicationService.login(request));
}

// application：编排用例，校验密码、禁用状态，签发 token
public LoginResponse login(LoginRequest request) {
    User user = userRepository.findByName(request.getUsername())
            .orElseThrow(() -> new BusinessException("用户名或密码错误"));
    if (!user.matchesPassword(request.getPassword())) {
        throw new BusinessException("用户名或密码错误");
    }
    if (user.isDisabled()) {
        throw new BusinessException("账号已被禁用");
    }
    return new LoginResponse(jwtUtils.createToken(...), user.getId(), user.getUsername(), ...);
}

// domain：接口定义在领域层，行为内聚在实体上
public interface UserRepository {
    Optional<User> findByName(String username);
    User save(User user);
}

// infrastructure：实现领域接口，持久化模型与领域模型相互转换
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper userMapper;
    private final UserAssembler userAssembler;

    @Override
    public Optional<User> findByName(String username) {
        return Optional.ofNullable(userMapper.selectOne(
                        new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, username)))
                .map(userAssembler::toDomain);
    }
}
```

## 6. 关键约定

- **事务**：`@Transactional` 放在 application 层（`*ApplicationService`），domain/infrastructure 不直接加事务注解。
- **对外输出**：不把 domain 对象直接序列化返回前端，一律转成 `*DTO` / `*Response`（如隐藏密码字段）。
- **模型分离**：domain 实体纯净（无持久化注解）；`*PO` 对应数据库表；`*Assembler` 负责双向转换。DB 表字段变更只影响 infrastructure。
- **校验分层**：参数格式校验（`@NotBlank` 等）放 DTO + `@Valid`；业务规则校验（唯一性、状态等）放 domain 或 application。
- **common 边界**：`common` 只放跨切面、纯能力（统一返回/异常、JWT、工具类、BaseEntity），不允许放具体业务；不参与四层，但各层可依赖。
- **领域逻辑内聚**：实体自带的业务行为（如 `matchesPassword`、`isDisabled`）写在实体方法里，不要写在 service 里。

## 7. 常见反模式（禁止）

| 反模式 | 问题 |
| --- | --- |
| Controller 直接调用 Mapper / RepositoryImpl | 绕过 application 编排，业务逻辑散落 |
| domain 出现 `@TableName`、`@Service`、Spring 依赖 | 领域层被框架污染，无法独立测试 |
| 领域实体当 DTO 直接返回前端 | 暴露内部字段（如密码），耦合视图与模型 |
| application 中堆积大量 if/else 业务规则 | 业务规则应下沉到 domain 实体/领域服务 |
| 每层都用 `ServiceImpl` 一刀切命名 | 应用层用 `*ApplicationService`，仓储实现用 `*RepositoryImpl` |
| 事务注解加在 Mapper/Controller | 事务边界应在 application 用例层 |
