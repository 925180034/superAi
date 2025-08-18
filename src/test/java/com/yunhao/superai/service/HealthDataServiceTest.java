package com.yunhao.superai.service;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Slf4j
class HealthDataServiceTest {

    @Resource
    private HealthDataService healthDataService;

    @Test
    void testCreateSampleHealthData() {
        List<HealthDataService.HealthMetrics> sampleData = healthDataService.createSampleHealthData();

        // 验证示例数据
        Assertions.assertNotNull(sampleData);
        Assertions.assertFalse(sampleData.isEmpty());
        Assertions.assertTrue(sampleData.size() >= 7, "应该至少有7天的示例数据");

        // 验证数据完整性
        for (HealthDataService.HealthMetrics data : sampleData) {
            Assertions.assertNotNull(data.date(), "日期不能为空");
            Assertions.assertNotNull(data.weight(), "体重不能为空");
            Assertions.assertNotNull(data.steps(), "步数不能为空");
            Assertions.assertNotNull(data.sleepHours(), "睡眠时长不能为空");

            // 验证数据合理性
            Assertions.assertTrue(data.weight() > 0 && data.weight() < 200,
                    "体重应在合理范围内: " + data.weight());
            Assertions.assertTrue(data.steps() >= 0 && data.steps() <= 50000,
                    "步数应在合理范围内: " + data.steps());
            Assertions.assertTrue(data.sleepHours() >= 0 && data.sleepHours() <= 24,
                    "睡眠时长应在0-24小时内: " + data.sleepHours());
        }

        log.info("示例数据验证成功，共{}天数据", sampleData.size());
        for (HealthDataService.HealthMetrics data : sampleData) {
            log.info("日期: {}, 体重: {}kg, 步数: {}, 睡眠: {}h, 心情: {}",
                    data.date(), data.weight(), data.steps(), data.sleepHours(), data.mood());
        }
    }

    @Test
    void testAnalyzeHealthTrends() {
        String chatId = UUID.randomUUID().toString();

        // 使用示例数据进行分析
        List<HealthDataService.HealthMetrics> healthData = healthDataService.createSampleHealthData();
        String analysis = healthDataService.analyzeHealthTrends(healthData, chatId);

        // 验证分析结果
        Assertions.assertNotNull(analysis);
        Assertions.assertTrue(analysis.length() > 100, "分析内容应该足够详细: " + analysis.length() + "字符");

        // 检查分析是否包含关键词
        String analysisLower = analysis.toLowerCase();
        boolean hasAnalysisKeywords = analysisLower.contains("趋势") ||
                analysisLower.contains("数据") ||
                analysisLower.contains("建议") ||
                analysisLower.contains("改善") ||
                analysisLower.contains("健康");
        Assertions.assertTrue(hasAnalysisKeywords, "分析应包含相关健康关键词");

        log.info("健康趋势分析长度: {} 字符", analysis.length());
        log.info("分析内容: {}", analysis);
    }

    @Test
    void testGenerateWeeklyHealthReport() {
        String chatId = UUID.randomUUID().toString();

        // 使用示例数据生成周报
        List<HealthDataService.HealthMetrics> weeklyData = healthDataService.createSampleHealthData();
        String report = healthDataService.generateWeeklyHealthReport(weeklyData, chatId);

        // 验证周报内容
        Assertions.assertNotNull(report);
        Assertions.assertTrue(report.length() > 150, "周报内容应该足够详细: " + report.length() + "字符");

        // 检查周报是否包含关键要素
        String reportLower = report.toLowerCase();
        boolean hasReportKeywords = reportLower.contains("周") ||
                reportLower.contains("概览") ||
                reportLower.contains("总结") ||
                reportLower.contains("目标") ||
                reportLower.contains("建议");
        Assertions.assertTrue(hasReportKeywords, "周报应包含相关关键词");

        log.info("健康周报长度: {} 字符", report.length());
        log.info("周报内容: {}", report);
    }

    @Test
    void testAnalyzeGoalProgress() {
        String chatId = UUID.randomUUID().toString();

        // 测试不同的健康目标
        List<HealthDataService.HealthMetrics> recentData = healthDataService.createSampleHealthData();
        String[] healthGoals = {
                "每天步数达到8000步",
                "体重减少2公斤",
                "每天睡眠8小时",
                "每周运动3次"
        };

        for (String healthGoal : healthGoals) {
            HealthDataService.GoalProgressAnalysis analysis =
                    healthDataService.analyzeGoalProgress(recentData, healthGoal, chatId);

            // 验证分析结果
            Assertions.assertNotNull(analysis);
            Assertions.assertNotNull(analysis.goalDescription());
            Assertions.assertTrue(analysis.progressPercentage() >= 0 && analysis.progressPercentage() <= 100,
                    "进度百分比应在0-100之间: " + analysis.progressPercentage());
            Assertions.assertNotNull(analysis.achievements());
            Assertions.assertNotNull(analysis.challenges());
            Assertions.assertNotNull(analysis.nextSteps());
            Assertions.assertNotNull(analysis.overallAssessment());

            log.info("健康目标: {}", healthGoal);
            log.info("目标描述: {}", analysis.goalDescription());
            log.info("完成进度: {}%", analysis.progressPercentage());
            log.info("已达成: {}", analysis.achievements());
            log.info("挑战: {}", analysis.challenges());
            log.info("下一步: {}", analysis.nextSteps());
            log.info("整体评估: {}", analysis.overallAssessment());
            log.info("---");
        }
    }

    @Test
    void testAnalyzeWithDifferentDataSizes() {
        String chatId = UUID.randomUUID().toString();

        // 测试不同大小的数据集
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        // 1. 空数据
        List<HealthDataService.HealthMetrics> emptyData = new ArrayList<>();
        String emptyAnalysis = healthDataService.analyzeHealthTrends(emptyData, chatId);
        Assertions.assertNotNull(emptyAnalysis);
        log.info("空数据分析: {}", emptyAnalysis);

        // 2. 单天数据
        List<HealthDataService.HealthMetrics> singleDayData = List.of(
                new HealthDataService.HealthMetrics(today.format(formatter), 70.0, 8000, 7, "良好", List.of("跑步"))
        );
        String singleDayAnalysis = healthDataService.analyzeHealthTrends(singleDayData, chatId);
        Assertions.assertNotNull(singleDayAnalysis);
        log.info("单天数据分析: {}", singleDayAnalysis);

        // 3. 少量数据（3天）
        List<HealthDataService.HealthMetrics> fewDaysData = List.of(
                new HealthDataService.HealthMetrics(today.minusDays(2).format(formatter), 70.5, 6000, 6, "一般", List.of("散步")),
                new HealthDataService.HealthMetrics(today.minusDays(1).format(formatter), 70.2, 7500, 7, "良好", List.of("跑步")),
                new HealthDataService.HealthMetrics(today.format(formatter), 70.0, 8500, 8, "很好", List.of("健身"))
        );
        String fewDaysAnalysis = healthDataService.analyzeHealthTrends(fewDaysData, chatId);
        Assertions.assertNotNull(fewDaysAnalysis);
        log.info("少量数据分析: {}", fewDaysAnalysis);

        // 4. 完整一周数据
        List<HealthDataService.HealthMetrics> weekData = healthDataService.createSampleHealthData();
        String weekAnalysis = healthDataService.analyzeHealthTrends(weekData, chatId);
        Assertions.assertNotNull(weekAnalysis);
        log.info("一周数据分析: {}", weekAnalysis);
    }

    @Test
    void testWeeklyReportWithInsufficientData() {
        String chatId = UUID.randomUUID().toString();

        // 测试数据不足的情况
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        // 只有2天数据
        List<HealthDataService.HealthMetrics> insufficientData = List.of(
                new HealthDataService.HealthMetrics(today.minusDays(1).format(formatter), 70.0, 8000, 7, "良好", List.of("跑步")),
                new HealthDataService.HealthMetrics(today.format(formatter), 69.8, 9000, 8, "很好", List.of("健身"))
        );

        String report = healthDataService.generateWeeklyHealthReport(insufficientData, chatId);

        Assertions.assertNotNull(report);
        // 数据不足时应该有相应的提示
        log.info("数据不足时的周报: {}", report);
    }

    @Test
    void testDataServiceEdgeCases() {
        String chatId = UUID.randomUUID().toString();

        // 测试异常数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        List<HealthDataService.HealthMetrics> abnormalData = List.of(
                // 极端值数据
                new HealthDataService.HealthMetrics(today.format(formatter), 0.0, 0, 0, null, null),
                new HealthDataService.HealthMetrics(today.minusDays(1).format(formatter), 150.0, 100000, 24, "极度疲惫", List.of())
        );

        try {
            String analysis = healthDataService.analyzeHealthTrends(abnormalData, chatId);
            Assertions.assertNotNull(analysis);
            log.info("异常数据分析成功: {}", analysis);
        } catch (Exception e) {
            log.warn("异常数据分析失败: {}", e.getMessage());
        }
    }
}