package com.yunhao.superai.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 健康养生向量数据库配置
 */
@Configuration
public class HealthAppVectorStoreConfig {

    @Resource
    private HealthAppDocumentLoader healthAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeyWordEnricher myKeyWordEnricher;

    @Bean("healthAppVectorStore")
    VectorStore healthAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();

        // 加载健康养生文档
        List<Document> documentList = healthAppDocumentLoader.loadHealthMarkdowns();

        // 自动补充关键词元信息
        List<Document> enrichDocuments = myKeyWordEnricher.enrichDocuments(documentList);
        simpleVectorStore.add(enrichDocuments);

        return simpleVectorStore;
    }
}