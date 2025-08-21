package com.yunhao.superai.controller;

import com.yunhao.superai.agent.YunManus;
import com.yunhao.superai.app.HealthApp;
import com.yunhao.superai.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/sentevent")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
        // 获取 Flux 数据流并直接订阅
        loveApp.doChatByStream(message, chatId)
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 处理完成
                        emitter::complete
                );
        // 返回emitter
        return emitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        YunManus yunManus = new YunManus(allTools, dashscopeChatModel);
        return yunManus.runStream(message);
    }

    /**
     * 流式调用 Manus 超级智能体 - 修正为支持POST方法
     */
    @PostMapping("/manus/chat")  // 改为 PostMapping
    public SseEmitter doChatWithManus(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String chatId = request.get("chatId");  // 可选参数

        YunManus yunManus = new YunManus(allTools, dashscopeChatModel);
        return yunManus.runStream(message);
    }

    @Resource
    private HealthApp healthApp;

    /**
     * 健康咨询 - 同步接口
     */
    @GetMapping("/health/chat/sync")
    public String doChatWithHealthSync(String message, String chatId) {
        return healthApp.doChat(message, chatId);
    }

    /**
     * 健康咨询 - SSE流式接口
     */
    @GetMapping(value = "/health/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithHealthSSE(String message, String chatId) {
        return healthApp.doChatByStream(message, chatId);
    }

    /**
     * 健康咨询 - SseEmitter方式 (推荐使用这个)
     */
    @GetMapping("/health/chat/sse/emitter")
    public SseEmitter doChatWithHealthSseEmitter(String message, String chatId) {
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时

        healthApp.doChatByStream(message, chatId)
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );
        return emitter;
    }

}
