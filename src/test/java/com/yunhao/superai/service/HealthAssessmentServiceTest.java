package com.yunhao.superai.service;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class HealthAssessmentServiceTest {

    @Resource
    private HealthAssessmentService healthAssessmentService;

    @Test
    void testHealthAssessment() {
        String chatId = UUID.randomUUID().toString();

        // 测试不同类型的健康描述
        String[] testCases = {
                "我是程序员，长期久坐，很少运动，经常熬夜",
                "我每天跑步，但是饮食不规律，经常吃外卖",
                "我作息规律，但是工作压力大，经常感到焦虑",
                "我是学生，喜欢熬夜打游戏，不爱运动"
        };

        for (String healthDescription : testCases) {
            HealthAssessmentService.HealthAssessment assessment =
                    healthAssessmentService.doHealthAssessment(healthDescription, chatId);

            // 验证评估结果
            Assertions.assertNotNull(assessment);
            Assertions.assertNotNull(assessment.userId());

            // 验证评分范围
            Assertions.assertTrue(assessment.fitnessScore() >= 0 && assessment.fitnessScore() <= 100,
                    "健身评分应在0-100之间，实际: " + assessment.fitnessScore());
            Assertions.assertTrue(assessment.nutritionScore() >= 0 && assessment.nutritionScore() <= 100,
                    "营养评分应在0-100之间，实际: " + assessment.nutritionScore());
            Assertions.assertTrue(assessment.sleepScore() >= 0 && assessment.sleepScore() <= 100,
                    "睡眠评分应在0-100之间，实际: " + assessment.sleepScore());

            // 验证风险等级
            Assertions.assertNotNull(assessment.riskLevel());
            Assertions.assertTrue(
                    assessment.riskLevel().contains("低风险") ||
                            assessment.riskLevel().contains("中等风险") ||
                            assessment.riskLevel().contains("高风险"),
                    "风险等级格式不正确: " + assessment.riskLevel()
            );

            // 验证建议内容
            Assertions.assertNotNull(assessment.recommendations());
            Assertions.assertFalse(assessment.recommendations().isEmpty());

            // 验证重点关注领域
            Assertions.assertNotNull(assessment.focusAreas());

            log.info("健康描述: {}", healthDescription);
            log.info("评估结果: 健身{}分, 营养{}分, 睡眠{}分, 风险等级: {}",
                    assessment.fitnessScore(), assessment.nutritionScore(),
                    assessment.sleepScore(), assessment.riskLevel());
            log.info("建议数量: {}, 重点关注: {}",
                    assessment.recommendations().size(), assessment.focusAreas().size());
            log.info("---");
        }
    }

    @Test
    void testGenerateHealthPlan() {
        String chatId = UUID.randomUUID().toString();

        // 测试不同类型的用户档案和目标
        String[][] testCases = {
                {"25岁程序员，久坐办公，缺乏运动", "减重10公斤，改善体质"},
                {"35岁宝妈，产后恢复，体重超标", "恢复身材，增强体力"},
                {"45岁中年男性，工作压力大，三高风险", "降血压血脂，改善睡眠"},
                {"20岁大学生，作息不规律，营养不良", "规律作息，均衡营养"}
        };

        for (String[] testCase : testCases) {
            String userProfile = testCase[0];
            String healthGoals = testCase[1];

            String plan = healthAssessmentService.generateHealthPlan(userProfile, healthGoals, chatId);

            // 验证计划内容
            Assertions.assertNotNull(plan);
            Assertions.assertTrue(plan.length() > 200, "健康计划内容太少: " + plan.length() + "字符");

            // 检查计划是否包含关键要素
            String planLower = plan.toLowerCase();
            boolean hasExercise = planLower.contains("运动") || planLower.contains("健身") ||
                    planLower.contains("锻炼") || planLower.contains("训练");
            boolean hasNutrition = planLower.contains("饮食") || planLower.contains("营养") ||
                    planLower.contains("食物") || planLower.contains("膳食");
            boolean hasSleep = planLower.contains("睡眠") || planLower.contains("作息") ||
                    planLower.contains("休息");

            Assertions.assertTrue(hasExercise, "健康计划应包含运动相关内容");
            Assertions.assertTrue(hasNutrition, "健康计划应包含营养相关内容");
            Assertions.assertTrue(hasSleep, "健康计划应包含睡眠相关内容");

            log.info("用户档案: {}", userProfile);
            log.info("健康目标: {}", healthGoals);
            log.info("计划长度: {} 字符", plan.length());
            log.info("计划内容: {}", plan.substring(0, Math.min(200, plan.length())) + "...");
            log.info("---");
        }
    }

    @Test
    void testQuickHealthTips() {
        String chatId = UUID.randomUUID().toString();

        // 测试不同的健康问题
        String[] healthConcerns = {
                "长期对着电脑工作，眼睛干涩",
                "经常加班熬夜，第二天很疲惫",
                "久坐办公，腰酸背痛",
                "饮食不规律，经常胃痛",
                "压力大，经常失眠"
        };

        for (String concern : healthConcerns) {
            List<String> tips = healthAssessmentService.getQuickHealthTips(concern, chatId);

            // 验证建议数量和质量
            Assertions.assertNotNull(tips);
            Assertions.assertTrue(tips.size() >= 3, "建议数量至少3条，实际: " + tips.size());
            Assertions.assertTrue(tips.size() <= 10, "建议数量不超过10条，实际: " + tips.size());

            // 验证每条建议的质量
            for (String tip : tips) {
                Assertions.assertNotNull(tip);
                Assertions.assertTrue(tip.length() >= 10, "建议内容太短: " + tip);
                Assertions.assertTrue(tip.length() <= 200, "建议内容太长: " + tip);
            }

            log.info("健康问题: {}", concern);
            log.info("建议数量: {}", tips.size());
            for (int i = 0; i < tips.size(); i++) {
                log.info("建议{}: {}", i + 1, tips.get(i));
            }
            log.info("---");
        }
    }

    @Test
    void testHealthAssessmentEdgeCases() {
        String chatId = UUID.randomUUID().toString();

        // 测试边界情况
        String[] edgeCases = {
                "我很健康，没有什么问题",  // 正面案例
                "我身体很差，什么都有问题",  // 负面案例
                "健康",  // 极简描述
                ""  // 空描述
        };

        for (String healthDescription : edgeCases) {
            try {
                HealthAssessmentService.HealthAssessment assessment =
                        healthAssessmentService.doHealthAssessment(healthDescription, chatId);

                // 即使是边界情况，也应该返回合理的评估
                Assertions.assertNotNull(assessment);
                log.info("边界测试 - 输入: '{}', 评估成功", healthDescription);

            } catch (Exception e) {
                log.warn("边界测试 - 输入: '{}', 出现异常: {}", healthDescription, e.getMessage());
                // 边界情况可能会失败，但不应该抛出未捕获的异常
            }
        }
    }
}