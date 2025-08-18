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

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class HealthAssessmentService {

    private final ChatClient chatClient;

    private static final String HEALTH_ASSESSMENT_PROMPT =
            "你是专业的健康评估专家。基于用户描述进行全面健康评估，" +
                    "从运动健身、饮食营养、作息调理三个维度分析，" +
                    "给出1-100分的健身、营养、睡眠评分，风险等级分为：低风险、中等风险、高风险，" +
                    "并提供针对性的改善建议。评估要客观、科学、实用。";

    public HealthAssessmentService(ChatModel dashscopeChatModel) {
        // 为健康评估服务创建专门的聊天记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10) // 评估服务使用较短的记忆
                .build();

        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(HEALTH_ASSESSMENT_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * 健康状况全面评估
     */
    public HealthAssessment doHealthAssessment(String healthDescription, String chatId) {
        String assessmentPrompt = String.format(
                "请对以下用户健康状况进行专业评估：%s\n\n" +
                        "要求：\n" +
                        "1. 分别给出健身、营养、睡眠三个维度的评分（1-100分）\n" +
                        "2. 判断整体风险等级（低风险/中等风险/高风险）\n" +
                        "3. 提供具体的改善建议\n" +
                        "4. 指出需要重点关注的健康问题",
                healthDescription
        );

        try {
            HealthAssessment assessment = chatClient
                    .prompt()
                    .user(assessmentPrompt)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .entity(HealthAssessment.class);

            log.info("健康评估完成 - 用户ID: {}, 综合评分: {}, 风险等级: {}",
                    assessment.userId(),
                    (assessment.fitnessScore() + assessment.nutritionScore() + assessment.sleepScore()) / 3,
                    assessment.riskLevel());

            return assessment;

        } catch (Exception e) {
            log.warn("健康评估失败，返回默认评估: {}", e.getMessage());
            return createDefaultAssessment(healthDescription);
        }
    }

    /**
     * 个性化健康计划生成
     */
    public String generateHealthPlan(String userProfile, String healthGoals, String chatId) {
        String planPrompt = String.format(
                "基于以下信息生成详细的个性化健康改善计划：\n\n" +
                        "用户档案：%s\n" +
                        "健康目标：%s\n\n" +
                        "请生成包含以下内容的计划：\n" +
                        "1. 运动计划（具体运动类型、频率、强度）\n" +
                        "2. 饮食建议（营养搭配、用餐时间、注意事项）\n" +
                        "3. 作息调整（睡眠时间、作息规律建议）\n" +
                        "4. 阶段性目标（短期、中期、长期目标）\n" +
                        "5. 监测指标（需要关注的健康数据）\n" +
                        "6. 注意事项和风险提醒",
                userProfile, healthGoals
        );

        try {
            String plan = chatClient
                    .prompt()
                    .user(planPrompt)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .content();

            log.info("健康计划生成完成 - 计划长度: {} 字符", plan.length());
            return plan;

        } catch (Exception e) {
            log.warn("健康计划生成失败，返回默认计划: {}", e.getMessage());
            return generateDefaultHealthPlan(userProfile, healthGoals);
        }
    }

    /**
     * 快速健康建议 - 修复版本
     */
    public List<String> getQuickHealthTips(String healthConcern, String chatId) {
        String tipsPrompt = String.format(
                "针对以下健康问题，提供5-7条简洁实用的健康建议：%s\n" +
                        "要求：\n" +
                        "1. 每条建议简洁明了，可立即实施\n" +
                        "2. 直接返回建议列表，每行一条建议\n" +
                        "3. 不要包含序号，不要包含其他解释文字\n" +
                        "4. 每条建议控制在30字以内",
                healthConcern
        );

        try {
            String response = chatClient
                    .prompt()
                    .user(tipsPrompt)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .content();

            // 解析响应内容
            List<String> tips = parseHealthTips(response);

            if (tips.isEmpty()) {
                // 如果解析失败，返回默认建议
                tips = getDefaultHealthTips(healthConcern);
            }

            log.info("快速健康建议获取成功 - 建议数量: {}", tips.size());
            return tips;

        } catch (Exception e) {
            log.warn("获取健康建议失败，返回默认建议: {}", e.getMessage());
            return getDefaultHealthTips(healthConcern);
        }
    }

    /**
     * 解析健康建议响应
     */
    private List<String> parseHealthTips(String response) {
        List<String> tips = new ArrayList<>();

        try {
            // 清理响应内容
            String cleanResponse = response.trim();

            // 移除markdown代码块标记
            if (cleanResponse.contains("```")) {
                cleanResponse = cleanResponse.replaceAll("```[a-zA-Z]*\\n?", "").replaceAll("```", "");
            }

            // 尝试解析JSON数组格式
            if (cleanResponse.startsWith("[") && cleanResponse.endsWith("]")) {
                String content = cleanResponse.substring(1, cleanResponse.length() - 1);
                String[] items = content.split("\",\\s*\"");

                for (String item : items) {
                    String tip = item.replace("\"", "").trim();
                    if (!tip.isEmpty() && tip.length() > 5) {
                        tips.add(tip);
                    }
                }
            } else {
                // 按行分割处理
                String[] lines = cleanResponse.split("\n");
                for (String line : lines) {
                    String tip = line.trim();

                    // 移除序号和特殊字符
                    tip = tip.replaceAll("^[0-9]+\\.\\s*", "")  // 移除数字序号
                            .replaceAll("^[\\-\\*•]\\s*", "")   // 移除列表符号
                            .replaceAll("^[一二三四五六七八九十]+[、．]\\s*", "") // 移除中文序号
                            .trim();

                    // 过滤有效建议
                    if (!tip.isEmpty() && tip.length() > 8 && tip.length() < 100) {
                        tips.add(tip);
                    }
                }
            }

        } catch (Exception e) {
            log.warn("解析健康建议失败: {}", e.getMessage());
        }

        return tips;
    }

    /**
     * 默认健康建议
     */
    private List<String> getDefaultHealthTips(String healthConcern) {
        List<String> defaultTips = new ArrayList<>();

        if (healthConcern.contains("眼睛") || healthConcern.contains("视力")) {
            defaultTips.addAll(List.of(
                    "每20分钟远眺20秒，缓解眼部疲劳",
                    "调整屏幕亮度，避免过亮或过暗",
                    "使用人工泪液，保持眼部湿润",
                    "眨眼操，每小时做10次完整眨眼",
                    "保持屏幕距离60厘米以上"
            ));
        } else if (healthConcern.contains("颈椎") || healthConcern.contains("脖子")) {
            defaultTips.addAll(List.of(
                    "保持正确坐姿，头部与脊椎成一条直线",
                    "每小时起身活动颈部，左右转动",
                    "使用合适高度的枕头，侧睡时支撑颈部",
                    "颈部热敷，促进血液循环",
                    "避免长时间低头看手机"
            ));
        } else if (healthConcern.contains("失眠") || healthConcern.contains("睡眠")) {
            defaultTips.addAll(List.of(
                    "固定睡眠时间，培养生物钟",
                    "睡前2小时避免使用电子设备",
                    "保持卧室温度18-22度，安静黑暗",
                    "睡前做轻度拉伸或冥想",
                    "避免睡前喝咖啡或大量液体"
            ));
        } else {
            defaultTips.addAll(List.of(
                    "保持规律的作息时间，每天同一时间睡觉和起床",
                    "适量运动，每天至少30分钟的有氧运动",
                    "均衡饮食，多吃蔬菜水果，减少加工食品",
                    "保持充足的水分摄入，每天8杯水",
                    "学会压力管理，尝试冥想或深呼吸练习",
                    "定期体检，及时发现和处理健康问题"
            ));
        }

        return defaultTips;
    }

    /**
     * 创建默认健康评估
     */
    private HealthAssessment createDefaultAssessment(String healthDescription) {
        return new HealthAssessment(
                "默认用户",
                60, // 默认健身评分
                65, // 默认营养评分
                55, // 默认睡眠评分
                getDefaultHealthTips(healthDescription),
                "中等风险",
                List.of("作息调理", "运动健身", "饮食营养")
        );
    }

    /**
     * 生成默认健康计划
     */
    private String generateDefaultHealthPlan(String userProfile, String healthGoals) {
        return String.format(
                "个性化健康改善计划\n\n" +
                        "用户档案：%s\n" +
                        "健康目标：%s\n\n" +
                        "一、运动计划\n" +
                        "• 每周3-4次有氧运动，每次30-45分钟\n" +
                        "• 每周2次力量训练，增强肌肉力量\n" +
                        "• 每天10分钟拉伸运动，提高柔韧性\n\n" +
                        "二、饮食建议\n" +
                        "• 三餐定时，早餐丰富，晚餐清淡\n" +
                        "• 增加蔬菜水果摄入，每天5种以上\n" +
                        "• 控制油盐糖摄入，选择健康烹饪方式\n\n" +
                        "三、作息调整\n" +
                        "• 每晚11点前入睡，保证7-8小时睡眠\n" +
                        "• 规律作息，周末也要保持\n" +
                        "• 创造良好的睡眠环境\n\n" +
                        "四、阶段性目标\n" +
                        "• 第1月：建立运动习惯，调整作息\n" +
                        "• 第2-3月：提高运动强度，优化饮食\n" +
                        "• 第4-6月：巩固健康习惯，达成目标\n\n" +
                        "五、监测指标\n" +
                        "• 每周测量体重、体脂率\n" +
                        "• 记录运动时长和心率\n" +
                        "• 监测睡眠质量和精神状态\n\n" +
                        "注意事项：循序渐进，避免过度运动，如有不适及时调整。",
                userProfile, healthGoals
        );
    }

    // === 数据模型定义 ===

    /**
     * 健康评估结果
     */
    public record HealthAssessment(
            String userId,
            int fitnessScore,      // 健身评分 1-100
            int nutritionScore,    // 营养评分 1-100
            int sleepScore,        // 睡眠评分 1-100
            List<String> recommendations,  // 改善建议
            String riskLevel,      // 风险等级：低风险/中等风险/高风险
            List<String> focusAreas // 重点关注领域
    ) {}

    /**
     * 快速建议响应（保留以备后用）
     */
    public record QuickTipsResponse(
            List<String> tips
    ) {}
}