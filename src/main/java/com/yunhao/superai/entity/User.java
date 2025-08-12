package com.yunhao.superai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore  // 密码不应该序列化到JSON
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "fitness_level")
    private FitnessLevel fitnessLevel;

    // 使用JSON字段存储用户详细信息
    @Column(columnDefinition = "TEXT")
    private String profileJson;  // 简化处理，存储JSON字符串

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 健身等级枚举
    public enum FitnessLevel {
        BEGINNER("初级"),
        INTERMEDIATE("中级"),
        ADVANCED("高级");

        private final String displayName;

        FitnessLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 用户资料内部类（用于JSON序列化）
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private Integer age;
        private String gender;
        private Integer height; // cm
        private Integer weight; // kg
        private List<String> goals;
        private String bio;
        private List<String> medicalConditions;
        private String activityLevel;
    }
}