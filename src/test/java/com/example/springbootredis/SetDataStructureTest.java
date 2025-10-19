package com.example.springbootredis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SetDataStructureTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRedisSetOperations() {
        System.out.println("=== 开始测试 Redis Set 数据结构 ===");

        String setKeyA = "set:test:A";
        String setKeyB = "set:test:B";
        SetOperations<String, String> setOps = stringRedisTemplate.opsForSet();

        // 清理环境
        stringRedisTemplate.delete(setKeyA);
        stringRedisTemplate.delete(setKeyB);

        // 1. 添加成员
        Long countA = setOps.add(setKeyA, "a", "b", "c", "d");
        Long countB = setOps.add(setKeyB, "c", "d", "e");
        assertEquals(4L, countA);
        assertEquals(3L, countB);

        // 2. 成员存在性
        assertTrue(Boolean.TRUE.equals(setOps.isMember(setKeyA, "a")));
        assertFalse(Boolean.TRUE.equals(setOps.isMember(setKeyA, "z")));

        // 3. 获取所有成员
        Set<String> membersA = setOps.members(setKeyA);
        assertNotNull(membersA);
        assertEquals(4, membersA.size());

        // 4. 交集、并集、差集
        Set<String> inter = setOps.intersect(setKeyA, setKeyB);
        assertEquals(Set.of("c", "d"), inter);

        Set<String> union = setOps.union(setKeyA, setKeyB);
        assertEquals(Set.of("a", "b", "c", "d", "e"), union);

        Set<String> diff = setOps.difference(setKeyA, setKeyB);
        assertEquals(Set.of("a", "b"), diff);

        // 5. 随机弹出/随机成员
        String randMember = setOps.randomMember(setKeyA);
        assertNotNull(randMember);
        String popped = setOps.pop(setKeyA);
        assertNotNull(popped);

        // 6. 移除成员
        Long removed = setOps.remove(setKeyB, "e");
        assertEquals(1L, removed);

        // 7. 清理
        assertTrue(Boolean.TRUE.equals(stringRedisTemplate.delete(setKeyA)));
        assertTrue(Boolean.TRUE.equals(stringRedisTemplate.delete(setKeyB)));

        System.out.println("=== Redis Set 数据结构测试通过 ===");
    }
}


