package com.yunhao.superai.repository;

import com.yunhao.superai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 查找指定时间之后登录的活跃用户
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.lastLoginAt > :since")
    List<User> findActiveUsersAfter(@Param("since") LocalDateTime since);
    
    /**
     * 根据健身等级查找用户
     */
    List<User> findByFitnessLevel(User.FitnessLevel fitnessLevel);
    
    /**
     * 查找活跃用户
     */
    List<User> findByIsActiveTrue();
    
    /**
     * 根据姓名模糊查询（用于管理功能）
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.isActive = true")
    List<User> findByNameContaining(@Param("name") String name);
}