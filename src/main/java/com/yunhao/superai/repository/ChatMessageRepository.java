package com.yunhao.superai.repository;

import com.yunhao.superai.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * 分页查询会话的消息，按创建时间正序
     */
    Page<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId, Pageable pageable);
    
    /**
     * 查询会话的所有消息
     */
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    /**
     * 查询会话的最新N条消息（用于聊天记忆）
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId ORDER BY m.createdAt DESC")
    List<ChatMessage> findLatestMessagesBySessionId(@Param("sessionId") String sessionId, Pageable pageable);
    
    /**
     * 删除指定会话的所有消息
     */
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 统计会话的消息数量
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 查找指定时间范围内的消息
     */
    List<ChatMessage> findBySessionIdAndCreatedAtBetween(
            String sessionId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找用户消息（不包括系统消息）
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.messageType != 'SYSTEM' ORDER BY m.createdAt ASC")
    List<ChatMessage> findUserAndAssistantMessages(@Param("sessionId") String sessionId);
    
    /**
     * 统计Token使用量
     */
    @Query("SELECT SUM(m.tokensUsed) FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.tokensUsed IS NOT NULL")
    Long getTotalTokensBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 查找处理时间较长的消息（性能监控）
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.processingTimeMs > :threshold")
    List<ChatMessage> findSlowMessages(@Param("threshold") Integer threshold);
}