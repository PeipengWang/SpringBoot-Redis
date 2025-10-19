package com.example.springbootredis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ListDataStructureTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRedisListOperations() {
        System.out.println("=== 开始测试 Redis List 数据结构 ===");

        String listKey = "list:test:tasks";
        ListOperations<String, String> listOps = stringRedisTemplate.opsForList();

        // 清理环境
        stringRedisTemplate.delete(listKey);

        // 1. 左侧推入元素
        Long len = listOps.leftPush(listKey, "task3");
        assertEquals(1L, len);
        listOps.leftPush(listKey, "task2");
        listOps.leftPush(listKey, "task1");

        // 当前列表应为: task1, task2, task3
        Long size = listOps.size(listKey);
        assertEquals(3L, size);

        // 2. 读取范围
        List<String> range = listOps.range(listKey, 0, -1);
        assertNotNull(range);
        assertEquals(List.of("task1", "task2", "task3"), range);

        // 3. 索引读取
        assertEquals("task1", listOps.index(listKey, 0));
        assertEquals("task3", listOps.index(listKey, 2));

        // 4. 右侧推入和弹出
        listOps.rightPush(listKey, "task4");
        assertEquals("task4", listOps.rightPop(listKey));

        // 5. 左弹出
        assertEquals("task1", listOps.leftPop(listKey));
        assertEquals(2L, listOps.size(listKey));

        // 6. 设置指定索引的值
        listOps.set(listKey, 0, "task2-updated");
        assertEquals("task2-updated", listOps.index(listKey, 0));

        // 7. 删除指定值
        Long removed = listOps.remove(listKey, 1, "task2-updated");
        assertEquals(1L, removed);

        // 8. 清理
        Boolean deleted = stringRedisTemplate.delete(listKey);
        assertTrue(Boolean.TRUE.equals(deleted));

        System.out.println("=== Redis List 数据结构测试通过 ===");
    }
}


