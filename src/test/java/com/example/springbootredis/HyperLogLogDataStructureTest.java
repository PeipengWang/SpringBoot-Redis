package com.example.springbootredis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HyperLogLogDataStructureTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRedisHyperLogLogOperations() {
        System.out.println("=== 开始测试 Redis HyperLogLog 数据结构 ===");

        String hllKeyA = "hll:test:A";
        String hllKeyB = "hll:test:B";
        String hllKeyUnion = "hll:test:UNION";

        // 清理环境
        stringRedisTemplate.delete(hllKeyA);
        stringRedisTemplate.delete(hllKeyB);
        stringRedisTemplate.delete(hllKeyUnion);

        // 1. PFADD 添加基数
        long addA = stringRedisTemplate.opsForHyperLogLog().add(hllKeyA, "a", "b", "c", "d", "e");
        long addB = stringRedisTemplate.opsForHyperLogLog().add(hllKeyB, "c", "d", "e", "f");
        assertTrue(addA >= 1);
        assertTrue(addB >= 1);

        // 2. PFCOUNT 统计基数
        Long countA = stringRedisTemplate.opsForHyperLogLog().size(hllKeyA);
        Long countB = stringRedisTemplate.opsForHyperLogLog().size(hllKeyB);
        assertNotNull(countA);
        assertNotNull(countB);
        assertTrue(countA >= 5);
        assertTrue(countB >= 4);

        // 3. PFMERGE 并集
        stringRedisTemplate.opsForHyperLogLog().union(hllKeyUnion, hllKeyA, hllKeyB);
        Long countUnion = stringRedisTemplate.opsForHyperLogLog().size(hllKeyUnion);
        assertNotNull(countUnion);
        assertTrue(countUnion >= 6);

        // 清理
        stringRedisTemplate.delete(hllKeyA);
        stringRedisTemplate.delete(hllKeyB);
        stringRedisTemplate.delete(hllKeyUnion);

        System.out.println("=== Redis HyperLogLog 数据结构测试通过 ===");
    }
}



