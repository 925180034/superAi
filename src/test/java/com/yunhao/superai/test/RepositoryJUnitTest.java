package com.yunhao.superai.test;

import com.yunhao.superai.entity.ChatMessage;
import com.yunhao.superai.entity.ChatSession;
import com.yunhao.superai.entity.User;
import com.yunhao.superai.repository.ChatMessageRepository;
import com.yunhao.superai.repository.ChatSessionRepository;
import com.yunhao.superai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository标准JUnit测试类
 * 在test目录中创建，使用 @SpringBootTest 注解
 */
@SpringBootTest
//@ActiveProfiles("test")  // 使用测试配置
@Transactional  // 每个测试方法后回滚数据
public class RepositoryJUnitTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    public void testUserRepository() {
        System.out.println("=== 测试用户Repository ===");
        
        // 创建测试用户
        User testUser = User.builder()
                .email("junit_test@example.com")
                .password("test_password")
                .name("JUnit测试用户")
                .fitnessLevel(User.FitnessLevel.BEGINNER)
                .profileJson("{\"age\": 25, \"gender\": \"male\"}")
                .build();
        
        // 保存用户
        User savedUser = userRepository.save(testUser);
        assertNotNull(savedUser.getId());
        System.out.println("✓ 用户保存成功，ID: " + savedUser.getId());
        
        // 根据邮箱查找用户
        Optional<User> foundUser = userRepository.findByEmail(savedUser.getEmail());
        assertTrue(foundUser.isPresent());
        assertEquals("JUnit测试用户", foundUser.get().getName());
        System.out.println("✓ 根据邮箱查找用户成功: " + foundUser.get().getName());
        
        // 检查邮箱是否存在
        assertTrue(userRepository.existsByEmail(savedUser.getEmail()));
        System.out.println("✓ 邮箱存在性检查通过");
        
        // 测试健身等级查询
        var beginnerUsers = userRepository.findByFitnessLevel(User.FitnessLevel.BEGINNER);
        assertFalse(beginnerUsers.isEmpty());
        System.out.println("✓ 健身等级查询成功，找到 " + beginnerUsers.size() + " 个初级用户");
    }

    @Test
    public void testChatSessionRepository() {
        System.out.println("\n=== 测试会话Repository ===");
        
        // 先创建一个用户
        User testUser = User.builder()
                .email("session_test@example.com")
                .password("test_password")
                .name("会话测试用户")
                .fitnessLevel(User.FitnessLevel.BEGINNER)
                .build();
        User savedUser = userRepository.save(testUser);
        
        // 创建测试会话
        String sessionId = "test_session_" + UUID.randomUUID().toString().substring(0, 8);
        ChatSession testSession = ChatSession.builder()
                .id(sessionId)
                .userId(savedUser.getId())
                .title("JUnit测试会话")
                .appType(ChatSession.AppType.FITNESS)
                .build();
        
        // 保存会话
        ChatSession savedSession = chatSessionRepository.save(testSession);
        assertEquals(sessionId, savedSession.getId());
        System.out.println("✓ 会话保存成功，ID: " + savedSession.getId());
        
        // 根据用户ID和会话ID查找
        Optional<ChatSession> foundSession = chatSessionRepository.findByIdAndUserId(
                sessionId, savedUser.getId());
        assertTrue(foundSession.isPresent());
        System.out.println("✓ 根据用户ID和会话ID查找成功");
        
        // 检查会话是否属于用户
        assertTrue(chatSessionRepository.existsByIdAndUserId(sessionId, savedUser.getId()));
        System.out.println("✓ 会话归属检查通过");
        
        // 统计用户活跃会话数
        long sessionCount = chatSessionRepository.countActiveSessionsByUserId(savedUser.getId());
        assertTrue(sessionCount > 0);
        System.out.println("✓ 用户活跃会话数: " + sessionCount);
    }

    @Test
    public void testChatMessageRepository() {
        System.out.println("\n=== 测试消息Repository ===");
        
        // 创建测试数据链：User -> Session -> Message
        User testUser = userRepository.save(User.builder()
                .email("message_test@example.com")
                .password("test_password")
                .name("消息测试用户")
                .fitnessLevel(User.FitnessLevel.BEGINNER)
                .build());
        
        String sessionId = "msg_test_session_" + UUID.randomUUID().toString().substring(0, 8);
        ChatSession testSession = chatSessionRepository.save(ChatSession.builder()
                .id(sessionId)
                .userId(testUser.getId())
                .title("消息测试会话")
                .appType(ChatSession.AppType.FITNESS)
                .build());
        
        // 创建测试消息
        ChatMessage testMessage = ChatMessage.builder()
                .sessionId(testSession.getId())
                .messageType(ChatMessage.MessageType.USER)
                .content("这是JUnit测试消息")
                .tokensUsed(10)
                .processingTimeMs(100)
                .build();
        
        // 保存消息
        ChatMessage savedMessage = chatMessageRepository.save(testMessage);
        assertNotNull(savedMessage.getId());
        System.out.println("✓ 消息保存成功，ID: " + savedMessage.getId());
        
        // 统计会话消息数
        long messageCount = chatMessageRepository.countBySessionId(testSession.getId());
        assertEquals(1, messageCount);
        System.out.println("✓ 会话消息数统计正确: " + messageCount);
        
        // 查询会话的所有消息
        var messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(testSession.getId());
        assertEquals(1, messages.size());
        assertEquals("这是JUnit测试消息", messages.get(0).getContent());
        System.out.println("✓ 消息查询成功，内容: " + messages.get(0).getContent());
        
        // 测试Token统计
        Long totalTokens = chatMessageRepository.getTotalTokensBySessionId(testSession.getId());
        assertEquals(10L, totalTokens);
        System.out.println("✓ Token统计正确: " + totalTokens);
    }

    @Test
    public void testCompleteWorkflow() {
        System.out.println("\n=== 测试完整工作流 ===");
        
        // 1. 创建用户
        User user = userRepository.save(User.builder()
                .email("workflow_test@example.com")
                .password("test_password")
                .name("工作流测试用户")
                .fitnessLevel(User.FitnessLevel.INTERMEDIATE)
                .build());
        
        // 2. 创建会话
        String sessionId = "workflow_session_" + UUID.randomUUID().toString().substring(0, 8);
        ChatSession session = chatSessionRepository.save(ChatSession.builder()
                .id(sessionId)
                .userId(user.getId())
                .title("工作流测试会话")
                .build());
        
        // 3. 创建多条消息
        ChatMessage userMsg = chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .messageType(ChatMessage.MessageType.USER)
                .content("用户提问")
                .tokensUsed(5)
                .build());
        
        ChatMessage assistantMsg = chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .messageType(ChatMessage.MessageType.ASSISTANT)
                .content("助手回复")
                .tokensUsed(15)
                .processingTimeMs(200)
                .build());
        
        // 4. 验证完整数据链
        assertTrue(userRepository.existsByEmail("workflow_test@example.com"));
        assertTrue(chatSessionRepository.existsByIdAndUserId(sessionId, user.getId()));
        assertEquals(2, chatMessageRepository.countBySessionId(sessionId));
        
        // 5. 验证Token统计
        Long totalTokens = chatMessageRepository.getTotalTokensBySessionId(sessionId);
        assertEquals(20L, totalTokens);
        
        System.out.println("✓ 完整工作流测试通过");
        System.out.println("  - 用户: " + user.getName());
        System.out.println("  - 会话: " + session.getTitle());
        System.out.println("  - 消息数: 2");
        System.out.println("  - 总Token: " + totalTokens);
    }
}