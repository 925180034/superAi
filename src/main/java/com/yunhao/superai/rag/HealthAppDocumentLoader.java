package com.yunhao.superai.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 健康养生应用文档加载器
 */
@Component
@Slf4j
public class HealthAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public HealthAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载健康养生相关的 Markdown 文档
     * @return 文档列表
     */
    public List<Document> loadHealthMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 从 health-docs 目录加载文档
            Resource[] resources = resourcePatternResolver.getResources("classpath:health-docs/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null) {
                    // 根据文件名提取健康分类标签
                    String category = extractHealthCategory(filename);

                    MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                            .withHorizontalRuleCreateDocument(true)
                            .withIncludeCodeBlock(false)
                            .withIncludeBlockquote(false)
                            .withAdditionalMetadata("filename", filename)
                            .withAdditionalMetadata("category", category)
                            .withAdditionalMetadata("type", "health")
                            .build();

                    MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                    List<Document> documents = markdownDocumentReader.get();
                    allDocuments.addAll(documents);
                    log.info("成功加载健康文档: {}, 分类: {}, 文档数: {}", filename, category, documents.size());
                }
            }
            log.info("总共加载健康文档数量: {}", allDocuments.size());
        } catch (IOException e) {
            log.error("健康文档加载失败", e);
        }
        return allDocuments;
    }

    /**
     * 从文件名提取健康分类
     * @param filename 文件名
     * @return 健康分类
     */
    private String extractHealthCategory(String filename) {
        if (filename.contains("fitness") || filename.contains("exercise")) {
            return "运动健身";
        } else if (filename.contains("nutrition") || filename.contains("diet")) {
            return "饮食营养";
        } else if (filename.contains("sleep") || filename.contains("rest")) {
            return "作息调理";
        } else if (filename.contains("mental")) {
            return "心理健康";
        } else {
            return "综合健康";
        }
    }
}