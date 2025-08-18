package com.yunhao.superai.app;

import com.yunhao.superai.advisor.MyLoggerAdvisor;
import com.yunhao.superai.rag.QueryRewriter;
import com.yunhao.superai.service.HealthAssessmentService;
import com.yunhao.superai.service.HealthDataService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    // 健康养生专家系统提示词
    private static final String SYSTEM_PROMPT = "扮演深耕健康养生领域的专家。开场向用户表明身份，告知用户可咨询健康问题。" +
            "围绕运动健身、饮食营养、作息调理三个方面提问：运动方面询问健身计划及运动习惯的困扰；" +
            "饮食方面询问营养搭配、饮食习惯的问题；作息方面询问睡眠质量与生活节奏的调整需求。" +
            "引导用户详述生活现状、健康目标及遇到的障碍，以便给出专属健康方案。";

    /**
     * 初始化 ChatClient
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于内存的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        // 构建ChatClient
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    // === 基础聊天功能 ===

    /**
     * AI基础多轮对话
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 流式对话
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    // === 数据模型定义 ===

    /**
     * 健康报告（新增分类字段）
     */
    public record HealthReport(String title, List<String> suggestions, String category) {}

    /**
     * 兼容性报告（保持原有结构）
     */
    public record LoveReport(String title, List<String> suggestions) {}

    // === 报告生成功能 ===

    /**
     * 生成健康报告（带分类）
     */
    public HealthReport doChatWithHealthReport(String message, String chatId) {
        HealthReport healthReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成健康咨询结果，标题为{用户名}的健康报告，" +
                        "内容包含建议列表和健康分类（运动健身/饮食营养/作息调理）")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(HealthReport.class);
        log.info("healthReport: {}", healthReport);
        return healthReport;
    }

    /**
     * 生成兼容性报告
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成健康咨询结果，标题为{用户名}的健康报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("healthReport: {}", loveReport);
        return loveReport;
    }

    // === RAG 知识库功能 ===

    @Resource
    @Qualifier("loveAppVectorStore")
    private VectorStore loveAppVectorStore;

    @Resource
    @Qualifier("healthAppVectorStore")
    private VectorStore healthAppVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 和原有知识库进行对话
     */
    public String doChatWithRag(String message, String chatId) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 和健康养生知识库进行对话
     */
    public String doChatWithHealthRag(String message, String chatId) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .advisors(new QuestionAnswerAdvisor(healthAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("healthRagContent: {}", content);
        return content;
    }

    // === 工具调用功能 ===

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * 支持工具调用的健康咨询
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 调用 MCP 服务的健康咨询
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    // === 健康服务集成 ===

    @Resource
    private HealthAssessmentService healthAssessmentService;

    @Resource
    private HealthDataService healthDataService;

    // === 健康评估功能 ===

    /**
     * 健康状况评估 - 委托给专门的服务
     */
    public HealthAssessmentService.HealthAssessment doHealthAssessment(String healthDescription, String chatId) {
        log.info("开始健康评估 - chatId: {}", chatId);
        return healthAssessmentService.doHealthAssessment(healthDescription, chatId);
    }

    /**
     * 生成个性化健康计划 - 委托给专门的服务
     */
    public String generateHealthPlan(String userProfile, String healthGoals, String chatId) {
        log.info("生成健康计划 - chatId: {}", chatId);
        return healthAssessmentService.generateHealthPlan(userProfile, healthGoals, chatId);
    }

    /**
     * 获取快速健康建议
     */
    public List<String> getQuickHealthTips(String healthConcern, String chatId) {
        log.info("获取健康建议 - 问题: {}", healthConcern);
        return healthAssessmentService.getQuickHealthTips(healthConcern, chatId);
    }

    // === 健康数据分析功能 ===

    /**
     * 健康数据趋势分析 - 委托给专门的服务
     */
    public String analyzeHealthTrends(List<HealthDataService.HealthMetrics> healthData, String chatId) {
        log.info("分析健康趋势 - 数据点数: {}", healthData != null ? healthData.size() : 0);
        return healthDataService.analyzeHealthTrends(healthData, chatId);
    }

    /**
     * 生成健康周报
     */
    public String generateWeeklyHealthReport(List<HealthDataService.HealthMetrics> weeklyData, String chatId) {
        log.info("生成健康周报 - 数据天数: {}", weeklyData != null ? weeklyData.size() : 0);
        return healthDataService.generateWeeklyHealthReport(weeklyData, chatId);
    }

    /**
     * 健康目标进度分析
     */
    public HealthDataService.GoalProgressAnalysis analyzeGoalProgress(
            List<HealthDataService.HealthMetrics> recentData,
            String healthGoal,
            String chatId) {
        log.info("分析目标进度 - 目标: {}", healthGoal);
        return healthDataService.analyzeGoalProgress(recentData, healthGoal, chatId);
    }

    /**
     * 获取示例健康数据（用于测试）
     */
    public List<HealthDataService.HealthMetrics> getSampleHealthData() {
        return healthDataService.createSampleHealthData();
    }

    // === 内置简化版健康功能（备用方案） ===
    // 如果服务类暂未创建，可以使用以下内置方法

    /**
     * 内置版健康评估（简化版）
     */
    public HealthAssessment doSimpleHealthAssessment(String healthDescription, String chatId) {
        HealthAssessment assessment = chatClient
                .prompt()
                .system(SYSTEM_PROMPT +
                        "基于用户描述进行健康评估，给出1-100分的健身、营养、睡眠评分，" +
                        "风险等级分为：低风险、中等风险、高风险，并提供改善建议")
                .user(healthDescription)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(HealthAssessment.class);
        log.info("healthAssessment: {}", assessment);
        return assessment;
    }

    /**
     * 内置版健康计划生成（简化版）
     */
    public String generateSimpleHealthPlan(String userProfile, String healthGoals, String chatId) {
        String planPrompt = String.format(
                "用户档案：%s\n健康目标：%s\n请生成详细的个性化健康改善计划，" +
                        "包含运动计划、饮食建议、作息调整和阶段性目标",
                userProfile, healthGoals
        );

        String plan = chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(planPrompt)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();

        log.info("健康计划生成完成 - 计划长度: {} 字符", plan.length());
        return plan;
    }

    /**
     * 内置版健康数据分析（简化版）
     */
    public String analyzeSimpleHealthTrends(List<HealthMetrics> healthData, String chatId) {
        if (healthData == null || healthData.isEmpty()) {
            return "没有足够的健康数据进行分析，建议先记录一段时间的健康数据。";
        }

        // 简单的数据格式转换
        StringBuilder dataDescription = new StringBuilder();
        dataDescription.append("健康数据记录：\n");
        for (HealthMetrics data : healthData) {
            dataDescription.append(String.format(
                    "日期: %s, 体重: %.1fkg, 步数: %d, 睡眠: %d小时, 心情: %s\n",
                    data.date(),
                    data.weight() != null ? data.weight() : 0.0,
                    data.steps() != null ? data.steps() : 0,
                    data.sleepHours() != null ? data.sleepHours() : 0,
                    data.mood() != null ? data.mood() : "未记录"
            ));
        }

        String analysis = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "分析用户的健康数据趋势，识别改善空间和潜在问题")
                .user("请分析以下健康数据：\n" + dataDescription.toString())
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();

        log.info("健康数据分析完成 - 数据点数: {}", healthData.size());
        return analysis;
    }

    // === 内置数据模型 ===

    /**
     * 健康评估结果（内置版）
     */
    public record HealthAssessment(
            String userId,
            int fitnessScore,      // 健身评分 1-100
            int nutritionScore,    // 营养评分 1-100
            int sleepScore,        // 睡眠评分 1-100
            List<String> recommendations,  // 改善建议
            String riskLevel       // 风险等级
    ) {}

    /**
     * 健康数据记录（内置版）
     */
    public record HealthMetrics(
            String date,           // 日期
            Double weight,         // 体重(kg)
            Integer steps,         // 步数
            Integer sleepHours,    // 睡眠时长(小时)
            String mood,           // 心情状态
            List<String> activities // 当日活动
    ) {}
}