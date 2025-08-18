package com.yunhao.superai.service;

import com.yunhao.superai.advisor.MyLoggerAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HealthDataService {

    private final ChatClient chatClient;

    private static final String DATA_ANALYSIS_PROMPT =
            "你是专业的健康数据分析师。专门分析用户的健康数据趋势，" +
                    "识别数据中的模式、异常和改善空间，提供基于数据的科学健康建议。" +
                    "分析要客观、准确，建议要实用、可操作。";

    public HealthDataService(ChatModel dashscopeChatModel) {
        // 为数据分析服务创建专门的聊天记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(15) // 数据分析可能需要更多上下文
                .build();

        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(DATA_ANALYSIS_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * 健康数据趋势分析
     */
    public String analyzeHealthTrends(List<HealthMetrics> healthData, String chatId) {
        if (healthData == null || healthData.isEmpty()) {
            return "没有足够的健康数据进行分析，建议先记录一段时间的健康数据。";
        }

        String dataJson = convertHealthDataToAnalysisFormat(healthData);

        String analysisPrompt = String.format(
                "请分析以下健康数据趋势：\n\n%s\n\n" +
                        "分析要求：\n" +
                        "1. 识别数据中的趋势和模式\n" +
                        "2. 指出异常或值得关注的变化\n" +
                        "3. 评估整体健康状况的改善或恶化\n" +
                        "4. 提供基于数据的具体改善建议\n" +
                        "5. 建议下一步的健康监测重点",
                dataJson
        );

        try {
            String analysis = chatClient
                    .prompt()
                    .user(analysisPrompt)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .content();

            log.info("健康数据分析完成 - 数据点数: {}, 分析长度: {} 字符",
                    healthData.size(), analysis.length());

            return analysis;

        } catch (Exception e) {
            log.warn("健康数据分析失败，返回默认分析: {}", e.getMessage());
            return generateDefaultAnalysis(healthData);
        }
    }

    /**
     * 生成健康周报
     */
    public String generateWeeklyHealthReport(List<HealthMetrics> weeklyData, String chatId) {
        if (weeklyData == null || weeklyData.size() < 3) {
            return "一周内的健康数据不足，无法生成周报。建议至少记录3天以上的数据。";
        }

        String dataJson = convertHealthDataToAnalysisFormat(weeklyData);

        String reportPrompt = String.format(
                "基于以下一周的健康数据，生成详细的健康周报：\n\n%s\n\n" +
                        "周报要求：\n" +
                        "1. 本周健康数据概览\n" +
                        "2. 各项指标的变化趋势\n" +
                        "3. 本周的健康亮点和问题\n" +
                        "4. 与标准健康指标的对比\n" +
                        "5. 下周的健康目标建议\n" +
                        "6. 需要重点关注的健康指标",
                dataJson
        );

        try {
            String report = chatClient
                    .prompt()
                    .user(reportPrompt)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .content();

            log.info("健康周报生成完成 - 数据天数: {}", weeklyData.size());
            return report;

        } catch (Exception e) {
            log.warn("健康周报生成失败，返回默认周报: {}", e.getMessage());
            return generateDefaultWeeklyReport(weeklyData);
        }
    }

    /**
     * 健康目标达成分析 - 修复版本
     */
    public GoalProgressAnalysis analyzeGoalProgress(List<HealthMetrics> recentData, String healthGoal, String chatId) {
        String dataJson = convertHealthDataToAnalysisFormat(recentData);

        String progressPrompt = String.format(
                "分析用户健康目标的达成情况：\n\n" +
                        "健康目标：%s\n" +
                        "近期健康数据：\n%s\n\n" +
                        "请分析目标达成进度并提供建议。\n\n" +
                        "重要要求：\n" +
                        "1. 进度百分比必须在0-100之间，不能超过100\n" +
                        "2. 如果数据显示已超额完成，进度百分比最多设为100\n" +
                        "3. 客观评估实际完成情况\n" +
                        "4. 提供具体的改善建议",
                healthGoal, dataJson
        );

        try {
            GoalProgressAnalysis rawAnalysis = chatClient
                    .prompt()
                    .user(progressPrompt)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .entity(GoalProgressAnalysis.class);

            // 验证和修正分析结果
            GoalProgressAnalysis validatedAnalysis = validateAndFixGoalProgress(rawAnalysis, healthGoal);

            log.info("健康目标分析完成 - 目标: {}, 修正后进度: {}%",
                    healthGoal, validatedAnalysis.progressPercentage());

            return validatedAnalysis;

        } catch (Exception e) {
            log.warn("健康目标分析失败，返回默认分析: {}", e.getMessage());
            return createDefaultGoalProgress(healthGoal, recentData);
        }
    }

    /**
     * 验证和修正目标进度分析结果
     */
    private GoalProgressAnalysis validateAndFixGoalProgress(GoalProgressAnalysis analysis, String healthGoal) {
        // 修正进度百分比到合理范围
        int validatedProgress = Math.max(0, Math.min(100, analysis.progressPercentage()));

        // 确保各个字段不为空
        String goalDescription = analysis.goalDescription() != null ?
                analysis.goalDescription() : healthGoal;

        List<String> achievements = analysis.achievements() != null && !analysis.achievements().isEmpty() ?
                analysis.achievements() : List.of("正在努力达成目标");

        List<String> challenges = analysis.challenges() != null && !analysis.challenges().isEmpty() ?
                analysis.challenges() : List.of("需要更多时间和努力");

        List<String> nextSteps = analysis.nextSteps() != null && !analysis.nextSteps().isEmpty() ?
                analysis.nextSteps() : List.of("继续坚持当前计划", "定期监测进度");

        String overallAssessment = analysis.overallAssessment() != null ?
                analysis.overallAssessment() : "整体进展正常，需要持续努力";

        // 根据修正后的进度调整评估
        if (validatedProgress != analysis.progressPercentage()) {
            if (analysis.progressPercentage() > 100) {
                overallAssessment = String.format("目标完成度超预期（原%d%%，已达100%%），表现优秀！",
                        analysis.progressPercentage());
                achievements = List.of("超额完成预期目标", "保持良好的执行力");
            }

            log.info("进度百分比已修正：{}% -> {}%", analysis.progressPercentage(), validatedProgress);
        }

        return new GoalProgressAnalysis(
                goalDescription,
                validatedProgress,
                achievements,
                challenges,
                nextSteps,
                overallAssessment
        );
    }

    /**
     * 创建默认目标进度分析
     */
    private GoalProgressAnalysis createDefaultGoalProgress(String healthGoal, List<HealthMetrics> recentData) {
        // 基于数据简单估算进度
        int estimatedProgress = 50; // 默认50%

        if (recentData != null && !recentData.isEmpty()) {
            // 根据数据量和一些基本指标估算
            if (recentData.size() >= 7) {
                estimatedProgress = 60; // 有一周数据，稍好一些
            }

            // 检查数据质量
            long validDataCount = recentData.stream()
                    .filter(data -> data.steps() != null && data.steps() > 1000)
                    .count();

            if (validDataCount >= recentData.size() * 0.8) {
                estimatedProgress = Math.min(75, estimatedProgress + 15);
            }
        }

        return new GoalProgressAnalysis(
                healthGoal,
                estimatedProgress,
                List.of("开始记录健康数据", "建立了基本的健康意识"),
                List.of("需要更长时间的数据积累", "目标达成需要持续努力"),
                List.of("继续记录每日健康数据", "保持良好的生活习惯", "定期回顾和调整目标"),
                String.format("目前进度约%d%%，继续努力可以达成目标", estimatedProgress)
        );
    }

    /**
     * 生成默认数据分析
     */
    private String generateDefaultAnalysis(List<HealthMetrics> healthData) {
        if (healthData == null || healthData.isEmpty()) {
            return "暂无健康数据可供分析，建议开始记录日常健康指标。";
        }

        StringBuilder analysis = new StringBuilder();
        analysis.append("健康数据趋势分析\n\n");

        // 计算一些基本统计
        double avgWeight = healthData.stream()
                .filter(d -> d.weight() != null)
                .mapToDouble(HealthMetrics::weight)
                .average().orElse(0.0);

        double avgSteps = healthData.stream()
                .filter(d -> d.steps() != null)
                .mapToInt(HealthMetrics::steps)
                .average().orElse(0.0);

        double avgSleep = healthData.stream()
                .filter(d -> d.sleepHours() != null)
                .mapToInt(HealthMetrics::sleepHours)
                .average().orElse(0.0);

        analysis.append(String.format("数据概览（%d天记录）：\n", healthData.size()));
        analysis.append(String.format("• 平均体重：%.1f公斤\n", avgWeight));
        analysis.append(String.format("• 平均步数：%.0f步/天\n", avgSteps));
        analysis.append(String.format("• 平均睡眠：%.1f小时/天\n\n", avgSleep));

        // 简单的趋势分析
        analysis.append("趋势分析：\n");
        if (avgSteps >= 8000) {
            analysis.append("• 运动量较充足，继续保持\n");
        } else {
            analysis.append("• 建议增加日常运动量，目标每天8000步\n");
        }

        if (avgSleep >= 7) {
            analysis.append("• 睡眠时间基本充足\n");
        } else {
            analysis.append("• 建议改善睡眠质量，保证每天7-8小时睡眠\n");
        }

        analysis.append("\n改善建议：\n");
        analysis.append("• 继续坚持记录健康数据\n");
        analysis.append("• 制定可行的运动和饮食计划\n");
        analysis.append("• 关注数据变化趋势，及时调整\n");

        return analysis.toString();
    }

    /**
     * 生成默认周报
     */
    private String generateDefaultWeeklyReport(List<HealthMetrics> weeklyData) {
        StringBuilder report = new StringBuilder();
        report.append("本周健康数据报告\n\n");

        if (weeklyData != null && !weeklyData.isEmpty()) {
            report.append(String.format("数据记录天数：%d天\n\n", weeklyData.size()));

            // 基本统计
            double totalSteps = weeklyData.stream()
                    .filter(d -> d.steps() != null)
                    .mapToInt(HealthMetrics::steps)
                    .sum();

            report.append(String.format("本周总步数：%.0f步\n", totalSteps));
            report.append(String.format("日均步数：%.0f步\n\n", totalSteps / weeklyData.size()));

            report.append("本周亮点：\n");
            report.append("• 坚持记录健康数据\n");
            if (totalSteps / weeklyData.size() >= 6000) {
                report.append("• 保持了较好的运动习惯\n");
            }

            report.append("\n下周目标：\n");
            report.append("• 继续保持数据记录习惯\n");
            report.append("• 适当增加运动量\n");
            report.append("• 关注睡眠质量\n");
        } else {
            report.append("本周缺少健康数据记录，建议下周开始规律记录。\n");
        }

        return report.toString();
    }

    /**
     * 将健康数据转换为分析格式
     */
    private String convertHealthDataToAnalysisFormat(List<HealthMetrics> healthData) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("健康数据记录：\n");
        analysis.append("日期 | 体重(kg) | 步数 | 睡眠(小时) | 心情 | 活动\n");
        analysis.append("---|---|---|---|---|---\n");

        for (HealthMetrics data : healthData) {
            String activities = data.activities() != null ?
                    String.join(", ", data.activities()) : "无";

            analysis.append(String.format(
                    "%s | %.1f | %d | %d | %s | %s\n",
                    data.date(),
                    data.weight() != null ? data.weight() : 0.0,
                    data.steps() != null ? data.steps() : 0,
                    data.sleepHours() != null ? data.sleepHours() : 0,
                    data.mood() != null ? data.mood() : "未记录",
                    activities
            ));
        }

        return analysis.toString();
    }

    /**
     * 创建示例健康数据（用于测试）
     */
    public List<HealthDataService.HealthMetrics> createSampleHealthData() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return List.of(
                new HealthMetrics(today.minusDays(6).format(formatter), 70.5, 8500, 7, "良好", List.of("跑步30分钟")),
                new HealthMetrics(today.minusDays(5).format(formatter), 70.3, 6200, 6, "一般", List.of("力量训练")),
                new HealthMetrics(today.minusDays(4).format(formatter), 70.1, 9800, 8, "很好", List.of("瑜伽", "散步")),
                new HealthMetrics(today.minusDays(3).format(formatter), 70.0, 7500, 7, "良好", List.of("游泳45分钟")),
                new HealthMetrics(today.minusDays(2).format(formatter), 69.8, 5800, 6, "疲惫", List.of("休息日")),
                new HealthMetrics(today.minusDays(1).format(formatter), 69.9, 8200, 7, "良好", List.of("跑步25分钟")),
                new HealthMetrics(today.format(formatter), 69.7, 9100, 8, "很好", List.of("力量训练", "拉伸"))
        );
    }

    // === 数据模型定义 ===

    /**
     * 健康数据记录
     */
    public record HealthMetrics(
            String date,           // 日期 YYYY-MM-DD
            Double weight,         // 体重(kg)
            Integer steps,         // 步数
            Integer sleepHours,    // 睡眠时长(小时)
            String mood,           // 心情状态
            List<String> activities // 当日活动
    ) {}

    /**
     * 目标进度分析
     */
    public record GoalProgressAnalysis(
            String goalDescription,     // 目标描述
            int progressPercentage,     // 完成百分比 (0-100)
            List<String> achievements,  // 已达成的成果
            List<String> challenges,    // 面临的挑战
            List<String> nextSteps,     // 下一步建议
            String overallAssessment    // 整体评估
    ) {}
}