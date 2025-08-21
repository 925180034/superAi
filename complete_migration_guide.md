# AI运动助手完整迁移实施方案

## 📋 项目现状分析

### 当前项目结构
```
superAi/
├── src/main/java/com/yunhao/superai/
│   ├── app/
│   │   └── LoveApp.java                    # 需要改造为 FitnessApp
│   ├── advisor/
│   │   ├── MyLoggerAdvisor.java           # 保持不变
│   │   └── ReReadingAdvisor.java          # 保持不变
│   ├── chatmemory/
│   │   └── FileBasedChatMemory.java       # 保持不变
│   ├── rag/
│   │   ├── LoveAppRagCustomAdvisorFactory.java  # 需要重命名
│   │   ├── LoveAppVectorStoreConfig.java       # 需要重命名
│   │   └── LoveAppDocumentLoader.java          # 需要重命名
│   ├── agent/
│   │   └── ToolCallAgent.java             # 可能需要适配
│   └── tools/                             # 需要完全替换
└── src/main/resources/
    ├── document/                          # 需要替换为运动相关文档
    └── application.yml                    # 需要更新配置
```

### 迁移优先级评估
1. **🔴 高优先级（核心功能）**：System Prompt、RAG文档、基础API
2. **🟡 中优先级（业务逻辑）**：工具类、数据库设计、认证系统
3. **🟢 低优先级（增强功能）**：可视化接口、高级工具、性能优化

---

## 🗺️ 详细迁移路线图

### Phase 1: 基础架构迁移 (2-3天)

#### 1.1 项目结构重命名和重构

**第一步：重命名核心类**
```bash
# 在项目根目录执行重命名
git mv src/main/java/com/yunhao/superai/app/LoveApp.java \
       src/main/java/com/yunhao/superai/app/FitnessApp.java

git mv src/main/java/com/yunhao/superai/rag/LoveAppRagCustomAdvisorFactory.java \
       src/main/java/com/yunhao/superai/rag/FitnessAppRagCustomAdvisorFactory.java

git mv src/main/java/com/yunhao/superai/rag/LoveAppVectorStoreConfig.java \
       src/main/java/com/yunhao/superai/rag/FitnessAppVectorStoreConfig.java

git mv src/main/java/com/yunhao/superai/rag/LoveAppDocumentLoader.java \
       src/main/java/com/yunhao/superai/rag/FitnessAppDocumentLoader.java
```

**第二步：更新包结构**
```bash
mkdir -p src/main/java/com/yunhao/superai/controller
mkdir -p src/main/java/com/yunhao/superai/service
mkdir -p src/main/java/com/yunhao/superai/repository
mkdir -p src/main/java/com/yunhao/superai/entity
mkdir -p src/main/java/com/yunhao/superai/dto
mkdir -p src/main/java/com/yunhao/superai/config
mkdir -p src/main/java/com/yunhao/superai/security
mkdir -p src/main/java/com/yunhao/superai/util
mkdir -p src/main/java/com/yunhao/superai/exception
mkdir -p src/main/java/com/yunhao/superai/fitness/tools
```

#### 1.2 更新 pom.xml 依赖

**添加必要依赖**
```xml
<dependencies>
    <!-- 现有依赖保持不变 -->
    
    <!-- 新增Web和安全依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JWT支持 -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- 数据库相关 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- Redis支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- 参数验证 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- 监控相关 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- 工具类 -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.8.23</version>
    </dependency>
</dependencies>
```

#### 1.3 更新配置文件

**修改 application.yml**
```yaml
server:
  port: 8123

spring:
  application:
    name: ai-fitness-assistant
    
  # 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/fitness_ai?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  # JPA配置
  jpa:
    hibernate:
      ddl-auto: update  # 开发阶段使用，生产环境改为validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        
  # Redis配置
  redis:
    host: localhost
    port: 6379
    timeout: 3000ms
    
  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

# JWT配置
jwt:
  secret: ${JWT_SECRET:fitnessAISecretKey2024ForTokenGeneration}
  expiration: 86400 # 24小时（秒）

# AI配置 - 保持你现有的配置
spring.ai:
  dashscope:
    chat:
      options:
        model: qwen-plus
        temperature: 0.7
      api-key: ${DASHSCOPE_API_KEY:your-api-key}

# 自定义配置
fitness:
  chat:
    max-history: 50
    response-timeout: 30000
  upload:
    path: ./uploads
    max-size: 10485760 # 10MB
    allowed-types: pdf,doc,docx,txt,jpg,jpeg,png

# 日志配置
logging:
  level:
    com.yunhao.superai: DEBUG
    org.springframework.ai: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

---

### Phase 2: 核心业务改造 (3-4天)

#### 2.1 改造 FitnessApp 核心类

**创建 src/main/java/com/yunhao/superai/app/FitnessApp.java**
```java
@Component
@Slf4j
public class FitnessApp {

    private final ChatClient chatClient;

    // 运动助手的系统提示词
    private static final String SYSTEM_PROMPT = 
        "你是一位专业的AI运动健身助手，名叫'AI健身教练'。你具备以下专业能力：\n" +
        
        "**专业领域:**\n" +
        "1. 运动科学和训练理论\n" +
        "2. 营养学和饮食规划\n" +
        "3. 伤病预防和康复指导\n" +
        "4. 健身心理学和动机维持\n" +
        "5. 各类运动项目的技术指导\n" +
        
        "**服务原则:**\n" +
        "1. 基于科学证据提供建议，避免伪科学\n" +
        "2. 充分考虑用户的个体差异和身体状况\n" +
        "3. 优先考虑安全性，预防运动伤害\n" +
        "4. 语言友好、鼓励性，避免过多专业术语\n" +
        "5. 必要时建议用户咨询专业医生或教练\n" +
        
        "**禁止行为:**\n" +
        "- 不提供医疗诊断或治疗建议\n" +
        "- 不推荐未经科学验证的补剂或方法\n" +
        "- 不给出极端或危险的训练建议\n" +
        "- 不忽视用户的身体限制或健康问题\n" +
        
        "请始终以用户的健康和安全为第一位，帮助他们建立科学、持续的运动习惯。";

    public FitnessApp(ChatModel dashscopeChatModel, VectorStore vectorStore, ToolCallback[] fitnessTools) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/fitness-chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()),
                        new MyLoggerAdvisor(),
                        new ReReadingAdvisor()
                )
                .defaultFunctions(fitnessTools) // 使用健身相关工具
                .build();
    }

    /**
     * 基础聊天功能
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();
        log.info("Fitness AI response length: {}", content.length());
        return content;
    }

    /**
     * 带SSE的聊天功能（新增）
     */
    public String doChatWithSSE(String message, String chatId, SseEmitter emitter, 
                               Object userContext) throws IOException {
        try {
            // 1. 发送思考状态
            sendSSEEvent(emitter, "thinking", Map.of(
                "content", "正在分析您的运动需求和身体状况...",
                "stage", "analyzing"
            ));
            
            Thread.sleep(1000); // 模拟思考时间

            // 2. 发送工具准备状态
            sendSSEEvent(emitter, "tool_start", Map.of(
                "tool", Map.of(
                    "id", "fitness_analyzer",
                    "name", "健身分析器",
                    "description", "分析用户健身档案和目标"
                )
            ));

            // 3. 调用ChatClient获取响应
            ChatResponse response = chatClient
                    .prompt()
                    .user(enhanceUserMessage(message, userContext))
                    .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                    .call()
                    .chatResponse();

            String content = response.getResult().getOutput().getText();

            // 4. 发送工具完成状态
            sendSSEEvent(emitter, "tool_complete", Map.of(
                "toolId", "fitness_analyzer",
                "result", "用户档案分析完成，已生成个性化建议"
            ));

            // 5. 发送MCP服务状态（模拟）
            sendSSEEvent(emitter, "mcp_status", Map.of(
                "servers", Arrays.asList(
                    Map.of(
                        "name", "fitness-exercise-library",
                        "displayName", "运动库服务",
                        "type", "exercise_database",
                        "status", "connected",
                        "connectedAt", new Date()
                    ),
                    Map.of(
                        "name", "nutrition-calculator",
                        "displayName", "营养计算器",
                        "type", "nutrition_analysis",
                        "status", "connected",
                        "connectedAt", new Date()
                    )
                )
            ));

            return content;

        } catch (Exception e) {
            log.error("处理SSE聊天时出错", e);
            sendSSEEvent(emitter, "error", Map.of("error", e.getMessage()));
            throw e;
        }
    }

    /**
     * 增强用户消息（添加上下文）
     */
    private String enhanceUserMessage(String message, Object userContext) {
        if (userContext == null) {
            return message;
        }
        
        StringBuilder enhanced = new StringBuilder();
        enhanced.append("用户背景信息：\n");
        // 这里可以添加用户的健身水平、目标等信息
        enhanced.append("用户问题：").append(message);
        return enhanced.toString();
    }

    /**
     * 发送SSE事件
     */
    private void sendSSEEvent(SseEmitter emitter, String eventType, Map<String, Object> data) 
            throws IOException {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", eventType);
        eventData.putAll(data);
        
        emitter.send(SseEmitter.event()
                .name(eventType)
                .data(eventData));
    }

    /**
     * 生成训练计划（结构化输出）
     */
    public FitnessReport generateTrainingPlan(String userGoals, String chatId) {
        FitnessReport report = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "\n请根据用户目标生成详细的训练计划，包括标题和具体的训练建议列表。")
                .user(userGoals)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(FitnessReport.class);

        log.info("生成训练计划: {}", report.title());
        return report;
    }

    // 内部记录类
    public record FitnessReport(String title, List<String> suggestions) {}
}
```

#### 2.2 创建实体类

**用户实体 - src/main/java/com/yunhao/superai/entity/User.java**
```java
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private FitnessLevel fitnessLevel;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private UserProfile profile;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum FitnessLevel {
        BEGINNER("初级"),
        INTERMEDIATE("中级"),
        ADVANCED("高级");

        private final String displayName;

        FitnessLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private Integer age;
        private String gender;
        private Integer height; // cm
        private Integer weight; // kg
        private List<String> goals;
        private String bio;
        private List<String> medicalConditions;
        private String activityLevel;
    }
}
```

**聊天会话实体 - src/main/java/com/yunhao/superai/entity/ChatSession.java**
```java
@Entity
@Table(name = "chat_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    @Id
    private String id;

    @Column(nullable = false)
    private Long userId;

    private String title;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppType appType = AppType.FITNESS;

    @Builder.Default
    private Integer messageCount = 0;

    private LocalDateTime lastMessageAt;

    @Builder.Default
    private Boolean isArchived = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AppType {
        FITNESS, MANUS
    }
}
```

**聊天消息实体 - src/main/java/com/yunhao/superai/entity/ChatMessage.java**
```java
@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Lob
    private String content;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Object attachments;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Object metadata;

    private Integer tokensUsed;

    private Integer processingTimeMs;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum MessageType {
        USER, ASSISTANT, SYSTEM
    }
}
```

#### 2.3 创建Repository接口

**用户Repository - src/main/java/com/yunhao/superai/repository/UserRepository.java**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.lastLoginAt > :since")
    List<User> findActiveUsersAfter(@Param("since") LocalDateTime since);
}
```

**聊天会话Repository - src/main/java/com/yunhao/superai/repository/ChatSessionRepository.java**
```java
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    Page<ChatSession> findByUserIdAndIsArchivedFalseOrderByUpdatedAtDesc(
            Long userId, Pageable pageable);
    
    Optional<ChatSession> findByIdAndUserId(String id, Long userId);
    
    boolean existsByIdAndUserId(String id, Long userId);
    
    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.userId = :userId AND s.isArchived = false")
    long countActiveSessionsByUserId(@Param("userId") Long userId);
}
```

**聊天消息Repository - src/main/java/com/yunhao/superai/repository/ChatMessageRepository.java**
```java
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId, Pageable pageable);
    
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);
}
```

---

### Phase 3: 运动工具类开发 (2-3天)

#### 3.1 创建健身工具配置

**工具配置类 - src/main/java/com/yunhao/superai/config/FitnessToolsConfig.java**
```java
@Configuration
public class FitnessToolsConfig {

    @Bean
    public ToolCallback[] fitnessTools() {
        return new ToolCallback[]{
            new ExerciseLibraryTool(),
            new TrainingPlanTool(),
            new NutritionCalculatorTool(),
            new WorkoutTrackerTool(),
            new InjuryPreventionTool()
        };
    }
}
```

#### 3.2 实现核心工具类

**运动库搜索工具 - src/main/java/com/yunhao/superai/fitness/tools/ExerciseLibraryTool.java**
```java
@Component
@Slf4j
public class ExerciseLibraryTool {

    @Tool(description = "搜索运动动作库，根据肌肉群、器械类型、难度等条件查找合适的运动")
    public String searchExerciseLibrary(
        @ToolParam(description = "目标肌肉群，如：胸部、背部、腿部、肩部、手臂、核心") String muscleGroup,
        @ToolParam(description = "器械类型，如：哑铃、杠铃、自重、器械、弹力带") String equipmentType,
        @ToolParam(description = "难度级别：beginner/intermediate/advanced") String difficulty,
        @ToolParam(description = "运动类型：strength/cardio/flexibility/functional") String exerciseType) {
        
        try {
            log.info("搜索运动库: 肌肉群={}, 器械={}, 难度={}, 类型={}", 
                    muscleGroup, equipmentType, difficulty, exerciseType);
            
            // 模拟运动库数据
            List<Exercise> exercises = getExercisesByParameters(muscleGroup, equipmentType, difficulty, exerciseType);
            
            if (exercises.isEmpty()) {
                return "未找到符合条件的运动。建议：\n" +
                       "1. 尝试更宽泛的搜索条件\n" +
                       "2. 降低难度等级\n" +
                       "3. 选择其他器械类型\n" +
                       "4. 考虑自重训练动作";
            }
            
            StringBuilder result = new StringBuilder();
            result.append(String.format("为您找到 %d 个适合的运动：\n\n", exercises.size()));
            
            exercises.stream().limit(5).forEach(exercise -> {
                result.append(String.format("**%s**\n", exercise.getName()));
                result.append(String.format("- 目标肌肉: %s\n", exercise.getTargetMuscles()));
                result.append(String.format("- 器械需求: %s\n", exercise.getEquipment()));
                result.append(String.format("- 难度等级: %s\n", exercise.getDifficulty()));
                result.append(String.format("- 动作要点: %s\n", exercise.getInstructions()));
                result.append(String.format("- 安全提醒: %s\n\n", exercise.getSafetyTips()));
            });
            
            if (exercises.size() > 5) {
                result.append(String.format("还有 %d 个运动可供选择，请告诉我您的具体需求以获得更精准的推荐。", 
                                          exercises.size() - 5));
            }
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("搜索运动库失败", e);
            return "运动库搜索失败，请稍后重试或联系技术支持";
        }
    }

    private List<Exercise> getExercisesByParameters(String muscleGroup, String equipmentType, 
                                                   String difficulty, String exerciseType) {
        // 这里应该连接真实的运动数据库
        // 现在使用模拟数据
        List<Exercise> allExercises = createMockExerciseDatabase();
        
        return allExercises.stream()
                .filter(ex -> muscleGroup == null || ex.getTargetMuscles().contains(muscleGroup))
                .filter(ex -> equipmentType == null || ex.getEquipment().equalsIgnoreCase(equipmentType))
                .filter(ex -> difficulty == null || ex.getDifficulty().equalsIgnoreCase(difficulty))
                .filter(ex -> exerciseType == null || ex.getExerciseType().equalsIgnoreCase(exerciseType))
                .collect(Collectors.toList());
    }

    private List<Exercise> createMockExerciseDatabase() {
        return Arrays.asList(
            Exercise.builder()
                .name("俯卧撑")
                .targetMuscles("胸部、肩部、三头肌")
                .equipment("自重")
                .difficulty("beginner")
                .exerciseType("strength")
                .instructions("身体保持一条直线，手臂与肩同宽，控制下降和推起速度")
                .safetyTips("膝盖着地降低难度，注意核心收紧")
                .build(),
            Exercise.builder()
                .name("哑铃卧推")
                .targetMuscles("胸部、前束三角肌、三头肌")
                .equipment("哑铃")
                .difficulty("intermediate")
                .exerciseType("strength")
                .instructions("平躺在卧推凳上，哑铃从胸部推至顶点，控制重量下降")
                .safetyTips("选择适当重量，确保有安全保护")
                .build(),
            Exercise.builder()
                .name("深蹲")
                .targetMuscles("大腿前侧、臀部、大腿后侧")
                .equipment("自重")
                .difficulty("beginner")
                .exerciseType("strength")
                .instructions("双脚与肩同宽，蹲至大腿与地面平行，保持膝盖与脚尖方向一致")
                .safetyTips("保持上半身挺直，膝盖不要内扣")
                .build()
        );
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Exercise {
        private String name;
        private String targetMuscles;
        private String equipment;
        private String difficulty;
        private String exerciseType;
        private String instructions;
        private String safetyTips;
    }
}
```

**训练计划生成工具 - src/main/java/com/yunhao/superai/fitness/tools/TrainingPlanTool.java**
```java
@Component
@Slf4j
public class TrainingPlanTool {

    @Tool(description = "根据用户目标、健身水平和时间安排生成个性化训练计划")
    public String generateTrainingPlan(
        @ToolParam(description = "健身目标：muscle_gain/weight_loss/strength/endurance/general_fitness") String fitnessGoal,
        @ToolParam(description = "健身水平：beginner/intermediate/advanced") String fitnessLevel,
        @ToolParam(description = "每周可训练天数：1-7") int daysPerWeek,
        @ToolParam(description = "每次训练时长(分钟)：30-120") int sessionDuration,
        @ToolParam(description = "可用器械：gym/home/minimal/bodyweight") String equipmentAccess) {
        
        try {
            log.info("生成训练计划: 目标={}, 水平={}, 天数={}, 时长={}, 器械={}", 
                    fitnessGoal, fitnessLevel, daysPerWeek, sessionDuration, equipmentAccess);

            // 参数验证
            if (daysPerWeek < 1 || daysPerWeek > 7) {
                return "训练天数应在1-7天之间，请重新输入";
            }
            if (sessionDuration < 30 || sessionDuration > 120) {
                return "训练时长应在30-120分钟之间，请重新输入";
            }

            TrainingPlan plan = createCustomTrainingPlan(fitnessGoal, fitnessLevel, 
                                                        daysPerWeek, sessionDuration, equipmentAccess);
            
            StringBuilder result = new StringBuilder();
            result.append(String.format("**%s**\n\n", plan.getPlanName()));
            result.append(String.format("📋 **计划概览**\n"));
            result.append(String.format("- 训练周期：%d周\n", plan.getDurationWeeks()));
            result.append(String.format("- 训练频率：每周%d次\n", daysPerWeek));
            result.append(String.format("- 单次时长：%d分钟\n", sessionDuration));
            result.append(String.format("- 适用水平：%s\n", getLevelDisplayName(fitnessLevel)));
            result.append(String.format("- 主要目标：%s\n\n", getGoalDisplayName(fitnessGoal)));
            
            result.append("📅 **周训练安排**\n");
            plan.getWeeklySchedule().forEach((day, workout) -> {
                result.append(String.format("**%s - %s**\n", day, workout.getName()));
                result.append(String.format("🎯 重点：%s\n", workout.getFocus()));
                workout.getExercises().forEach(exercise -> {
                    result.append(String.format("• %s - %s\n", exercise.getName(), exercise.getSetsReps()));
                });
                result.append("\n");
            });
            
            result.append("⚠️ **重要提醒**\n");
            plan.getImportantNotes().forEach(note -> 
                result.append(String.format("• %s\n", note)));
            
            result.append("\n💡 **进阶建议**\n");
            plan.getProgressionTips().forEach(tip -> 
                result.append(String.format("• %s\n", tip)));
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("生成训练计划失败", e);
            return "训练计划生成失败，请检查输入参数或稍后重试";
        }
    }

    private TrainingPlan createCustomTrainingPlan(String goal, String level, int days, 
                                                 int duration, String equipment) {
        // 这里应该是复杂的计划生成算法
        // 现在返回模拟数据
        
        Map<String, Workout> schedule = new LinkedHashMap<>();
        
        if (days >= 3) {
            schedule.put("周一", Workout.builder()
                .name("上肢力量训练")
                .focus("胸部、肩部、手臂")
                .exercises(Arrays.asList(
                    WorkoutExercise.builder().name("俯卧撑").setsReps("3组 × 8-12次").build(),
                    WorkoutExercise.builder().name("哑铃推举").setsReps("3组 × 10-15次").build(),
                    WorkoutExercise.builder().name("三头肌屈伸").setsReps("3组 × 12-15次").build()
                ))
                .build());
                
            schedule.put("周三", Workout.builder()
                .name("下肢力量训练")
                .focus("大腿、臀部、小腿")
                .exercises(Arrays.asList(
                    WorkoutExercise.builder().name("深蹲").setsReps("3组 × 12-15次").build(),
                    WorkoutExercise.builder().name("弓步蹲").setsReps("3组 × 每腿10次").build(),
                    WorkoutExercise.builder().name("臀桥").setsReps("3组 × 15-20次").build()
                ))
                .build());
                
            schedule.put("周五", Workout.builder()
                .name("全身综合训练")
                .focus("心肺耐力、协调性")
                .exercises(Arrays.asList(
                    WorkoutExercise.builder().name("波比跳").setsReps("3组 × 5-10次").build(),
                    WorkoutExercise.builder().name("山地爬行").setsReps("3组 × 20秒").build(),
                    WorkoutExercise.builder().name("平板支撑").setsReps("3组 × 30-60秒").build()
                ))
                .build());
        }
        
        return TrainingPlan.builder()
            .planName(String.format("%s训练计划 - %s", getGoalDisplayName(goal), getLevelDisplayName(level)))
            .durationWeeks(8)
            .weeklySchedule(schedule)
            .importantNotes(Arrays.asList(
                "训练前务必进行5-10分钟热身",
                "每组之间休息30-90秒，动作间休息1-2分钟",
                "感到疼痛或不适时立即停止训练",
                "保证充足睡眠和营养摄入",
                "循序渐进，避免急于求成"
            ))
            .progressionTips(Arrays.asList(
                "第1-2周适应动作，重点掌握正确姿势",
                "第3-4周逐渐增加训练强度",
                "第5-6周可以增加训练重量或次数",
                "第7-8周挑战更高难度的动作变式"
            ))
            .build();
    }

    private String getGoalDisplayName(String goal) {
        Map<String, String> goalNames = Map.of(
            "muscle_gain", "增肌塑形",
            "weight_loss", "减脂瘦身",
            "strength", "力量提升",
            "endurance", "耐力增强",
            "general_fitness", "综合健身"
        );
        return goalNames.getOrDefault(goal, "综合健身");
    }

    private String getLevelDisplayName(String level) {
        Map<String, String> levelNames = Map.of(
            "beginner", "初级",
            "intermediate", "中级",
            "advanced", "高级"
        );
        return levelNames.getOrDefault(level, "适中");
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrainingPlan {
        private String planName;
        private Integer durationWeeks;
        private Map<String, Workout> weeklySchedule;
        private List<String> importantNotes;
        private List<String> progressionTips;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Workout {
        private String name;
        private String focus;
        private List<WorkoutExercise> exercises;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkoutExercise {
        private String name;
        private String setsReps;
    }
}
```

---

### Phase 4: Web接口开发 (3-4天)

#### 4.1 创建JWT安全配置

**JWT工具类 - src/main/java/com/yunhao/superai/security/JwtTokenUtil.java**
```java
@Component
public class JwtTokenUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
    
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}
```

**安全配置 - src/main/java/com/yunhao/superai/config/SecurityConfig.java**
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        // CORS配置
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

#### 4.2 创建控制器

**认证控制器 - src/main/java/com/yunhao/superai/controller/AuthController.java**
```java
@RestController
@RequestMapping("/api/auth")
@Validated
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody @Valid RegisterRequest request) {
        
        log.info("用户注册请求: email={}", request.getEmail());
        
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        
        log.info("用户登录请求: email={}", request.getEmail());
        
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponse user = authService.getCurrentUser(email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        
        TokenResponse response = authService.refreshToken(request.getToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }
}
```

**健身聊天控制器 - src/main/java/com/yunhao/superai/controller/FitnessController.java**
```java
@RestController
@RequestMapping("/api/fitness")
@Validated
@Slf4j
public class FitnessController {

    @Autowired
    private FitnessApp fitnessApp;

    @Autowired
    private ChatService chatService;

    /**
     * 基础聊天接口
     */
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @RequestBody @Valid ChatRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("健身聊天请求: user={}, message={}", userEmail, request.getMessage());
        
        // 获取或创建会话
        String sessionId = chatService.getOrCreateSession(request.getChatId(), userEmail, "fitness");
        
        // 保存用户消息
        chatService.saveUserMessage(sessionId, request.getMessage(), request.getAttachments());
        
        // 调用AI
        String aiResponse = fitnessApp.doChat(request.getMessage(), sessionId);
        
        // 保存AI回复
        Long messageId = chatService.saveAssistantMessage(sessionId, aiResponse);
        
        ChatResponse response = ChatResponse.builder()
            .message(MessageResponse.builder()
                .id(messageId)
                .type("assistant")
                .content(aiResponse)
                .timestamp(LocalDateTime.now())
                .status("received")
                .build())
            .chatId(sessionId)
            .processingInfo(ProcessingInfo.builder()
                .thinkingSteps(Arrays.asList("分析用户需求", "检索知识库", "生成建议"))
                .toolsUsed(Arrays.asList("fitness_planner", "exercise_database"))
                .processingTime(2500L)
                .build())
            .build();
            
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * SSE流式聊天接口
     */
    @GetMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter chatStream(
            @RequestParam String message,
            @RequestParam(required = false) String chatId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("SSE聊天请求: user={}, message={}", userEmail, message);
        
        SseEmitter emitter = new SseEmitter(30000L);
        
        CompletableFuture.runAsync(() -> {
            try {
                // 获取或创建会话
                String sessionId = chatService.getOrCreateSession(chatId, userEmail, "fitness");
                
                // 保存用户消息
                chatService.saveUserMessage(sessionId, message, null);
                
                // 调用AI处理 - 带SSE支持
                String aiResponse = fitnessApp.doChatWithSSE(message, sessionId, emitter, null);
                
                // 保存AI回复
                Long messageId = chatService.saveAssistantMessage(sessionId, aiResponse);
                
                // 发送完成事件
                emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(Map.of(
                        "type", "complete",
                        "messageId", messageId,
                        "chatId", sessionId,
                        "content", aiResponse
                    )));
                
                emitter.complete();
                
            } catch (Exception e) {
                log.error("SSE聊天处理失败", e);
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("type", "error", "error", "处理请求时发生错误")));
                } catch (IOException ex) {
                    log.error("发送错误事件失败", ex);
                }
                emitter.completeWithError(e);
            }
        });
        
        // 设置超时处理
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });
        
        return emitter;
    }

    /**
     * 获取聊天历史
     */
    @GetMapping("/chat/{chatId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChatHistoryResponse>> getChatHistory(
            @PathVariable String chatId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        ChatHistoryResponse history = chatService.getChatHistory(chatId, userEmail);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * 获取最近聊天列表
     */
    @GetMapping("/chat/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RecentChatsResponse>> getRecentChats(
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        RecentChatsResponse recentChats = chatService.getRecentChats(userEmail, limit);
        return ResponseEntity.ok(ApiResponse.success(recentChats));
    }

    /**
     * 删除聊天
     */
    @DeleteMapping("/chat/{chatId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteChat(
            @PathVariable String chatId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        chatService.deleteChat(chatId, userEmail);
        return ResponseEntity.ok(ApiResponse.success("聊天记录已删除", null));
    }

    /**
     * 生成训练计划（结构化输出）
     */
    @PostMapping("/training-plan")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FitnessApp.FitnessReport>> generateTrainingPlan(
            @RequestBody @Valid TrainingPlanRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("生成训练计划请求: user={}, goals={}", userEmail, request.getGoals());
        
        // 获取或创建会话
        String sessionId = chatService.getOrCreateSession(null, userEmail, "fitness");
        
        // 调用AI生成训练计划
        FitnessApp.FitnessReport report = fitnessApp.generateTrainingPlan(request.getGoals(), sessionId);
        
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
```

#### 4.3 创建DTO类

**请求DTO类 - src/main/java/com/yunhao/superai/dto/request/RegisterRequest.java**
```java
@Data
@Validated
public class RegisterRequest {
    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100字符")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20位之间")
    private String password;

    @NotNull(message = "健身水平不能为空")
    private String fitnessLevel; // "BEGINNER", "INTERMEDIATE", "ADVANCED"

    private User.UserProfile profile;
}

@Data
@Validated
public class LoginRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;
}

@Data
@Validated
public class ChatRequest {
    @NotBlank(message = "消息内容不能为空")
    private String message;

    private List<AttachmentRequest> attachments;

    private String chatId; // 可选，续接对话

    @NotBlank(message = "应用类型不能为空")
    private String app; // "fitness" 或 "manus"
}

@Data
public class AttachmentRequest {
    private String id;
    private String name;
    private String type;
    private String url;
    private Long size;
}
```

**响应DTO类 - src/main/java/com/yunhao/superai/dto/response/AuthResponse.java**
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private UserInfo user;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String fitnessLevel;
        private User.UserProfile profile;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private MessageResponse message;
    private String chatId;
    private ProcessingInfo processingInfo;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private Long id;
    private String type;
    private String content;
    private LocalDateTime timestamp;
    private String status;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingInfo {
    private List<String> thinkingSteps;
    private List<String> toolsUsed;
    private Long processingTime;
}
```

---

### Phase 5: 服务层实现 (2-3天)

#### 5.1 认证服务

**认证服务实现 - src/main/java/com/yunhao/superai/service/impl/AuthServiceImpl.java**
```java
@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建新用户
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .fitnessLevel(User.FitnessLevel.valueOf(request.getFitnessLevel()))
            .profile(request.getProfile())
            .isActive(true)
            .build();

        User savedUser = userRepository.save(user);
        log.info("新用户注册成功: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        // 生成JWT Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtTokenUtil.generateToken(userDetails);

        return AuthResponse.builder()
            .token(token)
            .user(AuthResponse.UserInfo.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role("user")
                .fitnessLevel(savedUser.getFitnessLevel().name())
                .profile(savedUser.getProfile())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build())
            .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!user.getIsActive()) {
            throw new BusinessException("账户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("用户登录成功: id={}, email={}", user.getId(), user.getEmail());

        // 生成JWT Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenUtil.generateToken(userDetails);

        return AuthResponse.builder()
            .token(token)
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role("user")
                .fitnessLevel(user.getFitnessLevel().name())
                .profile(user.getProfile())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build())
            .build();
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role("user")
            .fitnessLevel(user.getFitnessLevel().name())
            .profile(user.getProfile())
            .build();
    }

    @Override
    public TokenResponse refreshToken(String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (jwtTokenUtil.validateToken(token, userDetails)) {
                String newToken = jwtTokenUtil.generateToken(userDetails);
                return TokenResponse.builder().token(newToken).build();
            } else {
                throw new BusinessException("Token无效");
            }
        } catch (Exception e) {
            throw new BusinessException("Token刷新失败");
        }
    }

    @Override
    public void logout(String email) {
        // 可以在这里实现Token黑名单机制
        log.info("用户登出: email={}", email);
    }
}
```

#### 5.2 聊天服务

**聊天服务实现 - src/main/java/com/yunhao/superai/service/impl/ChatServiceImpl.java**
```java
@Service
@Transactional
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatSessionRepository sessionRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public String getOrCreateSession(String chatId, String userEmail, String appType) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        if (StringUtils.hasText(chatId)) {
            // 验证会话是否属于当前用户
            ChatSession session = sessionRepository.findByIdAndUserId(chatId, user.getId())
                .orElseThrow(() -> new BusinessException("会话不存在或无权限访问"));
            return session.getId();
        }

        // 创建新会话
        String newSessionId = generateSessionId();
        ChatSession session = ChatSession.builder()
            .id(newSessionId)
            .userId(user.getId())
            .title("新的运动咨询")
            .appType(ChatSession.AppType.valueOf(appType.toUpperCase()))
            .messageCount(0)
            .build();

        sessionRepository.save(session);
        log.info("创建新会话: sessionId={}, userId={}", newSessionId, user.getId());

        return newSessionId;
    }

    @Override
    public void saveUserMessage(String sessionId, String content, List<AttachmentRequest> attachments) {
        ChatMessage message = ChatMessage.builder()
            .sessionId(sessionId)
            .messageType(ChatMessage.MessageType.USER)
            .content(content)
            .attachments(attachments)
            .build();

        messageRepository.save(message);
        updateSessionAfterMessage(sessionId, content);
        log.debug("保存用户消息: sessionId={}, contentLength={}", sessionId, content.length());
    }

    @Override
    public Long saveAssistantMessage(String sessionId, String content) {
        ChatMessage message = ChatMessage.builder()
            .sessionId(sessionId)
            .messageType(ChatMessage.MessageType.ASSISTANT)
            .content(content)
            .build();

        ChatMessage savedMessage = messageRepository.save(message);
        updateSessionAfterMessage(sessionId, content);
        log.debug("保存AI消息: sessionId={}, messageId={}", sessionId, savedMessage.getId());

        return savedMessage.getId();
    }

    @Override
    public ChatHistoryResponse getChatHistory(String chatId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        ChatSession session = sessionRepository.findByIdAndUserId(chatId, user.getId())
            .orElseThrow(() -> new BusinessException("会话不存在或无权限访问"));

        Page<ChatMessage> messagesPage = messageRepository.findBySessionIdOrderByCreatedAtAsc(
            chatId, PageRequest.of(0, 100));

        List<MessageResponse> messages = messagesPage.getContent().stream()
            .map(this::convertToMessageResponse)
            .collect(Collectors.toList());

        return ChatHistoryResponse.builder()
            .id(session.getId())
            .title(session.getTitle())
            .app(session.getAppType().name().toLowerCase())
            .messages(messages)
            .createdAt(session.getCreatedAt())
            .updatedAt(session.getUpdatedAt())
            .build();
    }

    @Override
    public RecentChatsResponse getRecentChats(String userEmail, int limit) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        Page<ChatSession> sessionsPage = sessionRepository
            .findByUserIdAndIsArchivedFalseOrderByUpdatedAtDesc(
                user.getId(), PageRequest.of(0, limit));

        List<ChatSummary> chats = sessionsPage.getContent().stream()
            .map(this::convertToChatSummary)
            .collect(Collectors.toList());

        return RecentChatsResponse.builder()
            .chats(chats)
            .total(sessionsPage.getTotalElements())
            .hasMore(sessionsPage.hasNext())
            .build();
    }

    @Override
    public void deleteChat(String chatId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        ChatSession session = sessionRepository.findByIdAndUserId(chatId, user.getId())
            .orElseThrow(() -> new BusinessException("会话不存在或无权限访问"));

        // 删除所有消息
        messageRepository.deleteBySessionId(chatId);
        
        // 删除会话
        sessionRepository.delete(session);
        
        log.info("删除聊天记录: sessionId={}, userId={}", chatId, user.getId());
    }

    private void updateSessionAfterMessage(String sessionId, String content) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException("会话不存在"));

        session.setMessageCount(session.getMessageCount() + 1);
        session.setLastMessageAt(LocalDateTime.now());

        // 如果是第一条消息，更新会话标题
        if (session.getMessageCount() == 1) {
            String title = content.length() > 20 ? content.substring(0, 20) + "..." : content;
            session.setTitle(title);
        }

        sessionRepository.save(session);
    }

    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + 
               UUID.randomUUID().toString().substring(0, 8);
    }

    private MessageResponse convertToMessageResponse(ChatMessage message) {
        return MessageResponse.builder()
            .id(message.getId())
            .type(message.getMessageType().name().toLowerCase())
            .content(message.getContent())
            .timestamp(message.getCreatedAt())
            .status("sent")
            .build();
    }

    private ChatSummary convertToChatSummary(ChatSession session) {
        // 获取最后一条消息
        String lastMessage = messageRepository
            .findBySessionIdOrderByCreatedAtAsc(session.getId(), PageRequest.of(0, 1))
            .getContent()
            .stream()
            .findFirst()
            .map(ChatMessage::getContent)
            .orElse("");

        return ChatSummary.builder()
            .id(session.getId())
            .title(session.getTitle())
            .app(session.getAppType().name().toLowerCase())
            .lastMessage(lastMessage.length() > 50 ? lastMessage.substring(0, 50) + "..." : lastMessage)
            .updatedAt(session.getUpdatedAt())
            .messageCount(session.getMessageCount())
            .build();
    }
}
```

---

## 🎯 详细实施步骤

### 第1天：基础架构搭建
```bash
# 1. 项目重命名和依赖更新
- 重命名核心类文件
- 更新pom.xml依赖
- 修改application.yml配置

# 2. 数据库准备
- 创建MySQL数据库 fitness_ai
- 运行DDL脚本创建表结构
- 验证JPA连接

# 3. 基础测试
- 启动应用验证配置正确
- 测试数据库连接
- 验证Spring AI组件加载
```

### 第2天：实体和Repository开发
```bash
# 1. 创建实体类
- User.java（用户实体）
- ChatSession.java（会话实体）
- ChatMessage.java（消息实体）

# 2. 创建Repository接口
- UserRepository.java
- ChatSessionRepository.java  
- ChatMessageRepository.java

# 3. 测试数据访问层
- 编写单元测试验证Repository功能
```

### 第3天：核心业务改造
```bash
# 1. 改造FitnessApp类
- 更新系统提示词
- 修改文档加载逻辑
- 集成SSE支持

# 2. 创建健身工具类
- ExerciseLibraryTool.java
- TrainingPlanTool.java
- NutritionCalculatorTool.java

# 3. 测试AI功能
- 验证聊天功能正常
- 测试工具调用
- 验证SSE流式输出
```

### 第4天：安全和认证
```bash
# 1. 实现JWT安全机制
- JwtTokenUtil.java
- SecurityConfig.java
- UserDetailsService实现

# 2. 创建认证服务
- AuthService接口和实现
- 用户注册、登录逻辑

# 3. 测试认证功能
- 注册新用户
- 登录获取Token
- 验证Token有效性
```

### 第5天：Web接口开发
```bash
# 1. 创建控制器
- AuthController.java（认证接口）
- FitnessController.java（聊天接口）

# 2. 创建DTO类
- 请求和响应DTO
- 参数验证注解

# 3. 测试API接口
- 使用Postman测试所有接口
- 验证SSE连接
```

### 第6天：服务层完善
```bash
# 1. 实现业务服务
- AuthServiceImpl.java
- ChatServiceImpl.java

# 2. 异常处理
- 全局异常处理器
- 自定义业务异常

# 3. 完整功能测试
- 端到端流程测试
- 性能测试
```

### 第7天：文档和部署
```bash
# 1. 更新RAG知识库
- 替换运动相关文档
- 重新构建向量索引

# 2. 集成测试
- 前后端联调
- API文档更新

# 3. 部署准备
- Docker配置
- 环境变量设置
- 生产环境部署
```

---

## ⚡ 关键注意事项

### 1. 数据迁移策略
```sql
-- 如果有现有数据需要迁移
-- 创建数据备份
mysqldump -u root -p love_ai > backup_love_ai.sql

-- 创建新数据库
CREATE DATABASE fitness_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 迁移用户数据（如果适用）
INSERT INTO fitness_ai.users (email, password, name, fitness_level, created_at)
SELECT email, password, name, 'BEGINNER', created_at 
FROM love_ai.users;
```

### 2. 配置管理
```yaml
# 开发环境配置
spring:
  profiles:
    active: dev
    
# 生产环境配置
spring:
  profiles:
    active: prod
  jpa:
    hibernate:
      ddl-auto: validate  # 生产环境不能是update
    show-sql: false
```

### 3. 性能优化
```java
// 数据库连接池配置
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000

// 缓存配置
@Cacheable(value = "exercises", key = "#muscleGroup + '_' + #difficulty")
public List<Exercise> searchExercises(String muscleGroup, String difficulty) {
    // 实现逻辑
}
```

### 4. 监控和日志
```java
// 添加监控端点
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized

// 日志配置
logging:
  level:
    com.yunhao.superai: INFO
    org.springframework.ai: WARN
    org.springframework.security: WARN
  pattern:
    file: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/fitness-ai.log
```

这个完整的迁移方案提供了详细的步骤指导和代码实现，你可以按照这个计划逐步完成从恋爱助手到运动助手的转换。整个过程大约需要7天时间，每天都有明确的目标和可验证的成果。