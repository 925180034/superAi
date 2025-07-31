package com.yunhao.superai.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据库连接测试
 */
@SpringBootTest
@ActiveProfiles("local")
public class DatabaseConnectionTest {

    @Resource
    private DataSource dataSource;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDatabaseConnection() {
        assertNotNull(dataSource);
        assertNotNull(jdbcTemplate);
        
        try {
            // 测试数据库连接
            String result = jdbcTemplate.queryForObject("SELECT 'Connection Successful' as status", String.class);
            assertEquals("Connection Successful", result);
            System.out.println("✅ 数据库连接成功: " + result);
            
            // 测试数据库信息
            String dbName = jdbcTemplate.queryForObject("SELECT DATABASE() as db_name", String.class);
            System.out.println("📊 当前数据库: " + dbName);
            
            // 测试MySQL版本
            String version = jdbcTemplate.queryForObject("SELECT VERSION() as version", String.class);
            System.out.println("🔢 MySQL版本: " + version);
            
        } catch (Exception e) {
            fail("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateTable() {
        try {
            // 测试创建一个简单的表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_connection (" +
                                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                "name VARCHAR(255), " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")");
            
            // 插入测试数据
            jdbcTemplate.update("INSERT INTO test_connection (name) VALUES (?)", "Test Connection");
            
            // 查询测试数据
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_connection", Integer.class);
            assertTrue(count > 0);
            System.out.println("✅ 表操作测试成功，数据条数: " + count);
            
            // 清理测试表
            jdbcTemplate.execute("DROP TABLE test_connection");
            System.out.println("🧹 测试表已清理");
            
        } catch (Exception e) {
            fail("表操作测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}