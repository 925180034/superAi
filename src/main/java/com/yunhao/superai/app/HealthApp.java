package com.yunhao.superai.app;

import com.yunhao.superai.advisor.MyLoggerAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class HealthApp {

    private final ChatClient chatClient;

    private static final String HEALTH_SYSTEM_PROMPT = """
        你是一位专业的健康咨询助手，具备丰富的医疗知识和健康指导经验。
        
        请遵循以下原则：
        1. 提供专业、准确的健康建议
        2. 强调预防和健康生活方式的重要性  
        3. 对于严重症状，建议及时就医
        4. 不能替代专业医生的诊断和治疗
        5. 回答要温馨、专业、易懂
        
        你的目标是帮助用户获得可靠的健康指导和养生建议。
        """;

    /**
     * 初始化 ChatClient - 完全参考 LoveApp 的实现方式
     */
    public HealthApp(ChatModel dashscopeChatModel) {
        // 简单的方式创建 ChatClient，先不使用复杂的内存管理
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(HEALTH_SYSTEM_PROMPT)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
    }

    /**
     * 健康咨询 - 同步方法
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(new MyLoggerAdvisor())
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("健康咨询回复: {}", content);
        return content;
    }

    /**
     * 健康咨询 - 流式方法
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .stream()
                .content();
    }
}