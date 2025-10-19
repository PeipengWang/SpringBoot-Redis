package com.example.springbootredis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ZSetDataStructureTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRedisZSetOperations() {
        System.out.println("=== 开始测试 Redis ZSet 数据结构 ===");

        String zsetKey = "zset:test:scores";
        ZSetOperations<String, String> zSetOps = stringRedisTemplate.opsForZSet();

        // 清理环境
        stringRedisTemplate.delete(zsetKey);

        // 1. 添加有序成员（member, score）
        assertTrue(Boolean.TRUE.equals(zSetOps.add(zsetKey, "alice", 90)));
        assertTrue(Boolean.TRUE.equals(zSetOps.add(zsetKey, "bob", 85)));
        assertTrue(Boolean.TRUE.equals(zSetOps.add(zsetKey, "carol", 95)));

        // 2. 计数
        Long size = zSetOps.size(zsetKey);
        assertEquals(3L, size);

        // 3. 按分数升序范围查询（默认）
        Set<String> ascRange = zSetOps.range(zsetKey, 0, -1);
        assertEquals(Set.of("bob", "alice", "carol").size(), ascRange.size());
        assertTrue(ascRange.contains("bob") && ascRange.contains("alice") && ascRange.contains("carol"));

        // 4. 按分数降序范围查询
        Set<String> descRange = zSetOps.reverseRange(zsetKey, 0, -1);
        assertNotNull(descRange);
        assertTrue(descRange.iterator().next().equals("carol"));

        // 5. 分数相关操作
        Double aliceScore = zSetOps.score(zsetKey, "alice");
        assertEquals(90.0, aliceScore);

        Double afterIncrement = zSetOps.incrementScore(zsetKey, "bob", 10.0);
        assertEquals(95.0, afterIncrement);

        // 6. rank 与 reverseRank
        Long bobRank = zSetOps.rank(zsetKey, "bob");
        Long bobReverseRank = zSetOps.reverseRank(zsetKey, "bob");
        assertNotNull(bobRank);
        assertNotNull(bobReverseRank);

        // 7. 按分数范围删除
        Long removedByScore = zSetOps.removeRangeByScore(zsetKey, 0, 89.9);
        assertTrue(removedByScore >= 0);

        // 8. 删除成员
        Long removed = zSetOps.remove(zsetKey, "alice", "bob", "carol");
        assertTrue(removed >= 0);

        // 9. 清理
        assertTrue(Boolean.TRUE.equals(stringRedisTemplate.delete(zsetKey)));

        System.out.println("=== Redis ZSet 数据结构测试通过 ===");
    }
}


