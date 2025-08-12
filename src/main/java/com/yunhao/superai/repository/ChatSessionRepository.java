package com.yunhao.superai.repository;

import com.yunhao.superai.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    
    /**
     * 查找用户的未归档会话，按更新时间倒序
     */
    Page<ChatSession> findByUserIdAndIsArchivedFalseOrderByUpdatedAtDesc(
            Long userId, Pageable pageable);
    
    /**
     * 根据会话ID和用户ID查找会话（安全检查）
     */
    Optional<ChatSession> findByIdAndUserId(String id, Long userId);
    
    /**
     * 检查会话是否属于指定用户
     */
    boolean existsByIdAndUserId(String id, Long userId);
    
    /**
     * 统计用户的活跃会话数量
     */
    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.userId = :userId AND s.isArchived = false")
    long countActiveSessionsByUserId(@Param("userId") Long userId);
    
    /**
     * 查找用户的所有会话（包括已归档）
     */
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);
    
    /**
     * 查找指定时间范围内的会话
     */
    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.createdAt BETWEEN :startTime AND :endTime")
    List<ChatSession> findByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找长时间未使用的会话（用于清理）
     */
    @Query("SELECT s FROM ChatSession s WHERE s.lastMessageAt < :threshold OR s.lastMessageAt IS NULL")
    List<ChatSession> findInactiveSessions(@Param("threshold") LocalDateTime threshold);
}