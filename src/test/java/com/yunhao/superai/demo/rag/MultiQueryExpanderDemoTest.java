package com.yunhao.superai.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MultiQueryExpanderDemoTest {

    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;  // 改为实际类型

    @Test
    void expand() {
        // 如果 MultiQueryExpanderDemo 有 expand 方法
        List<Query> queries = multiQueryExpanderDemo.expand("啥是程序员鱼皮啊啊啊？请回答我哈哈");
        Assertions.assertNotNull(queries);
    }
}