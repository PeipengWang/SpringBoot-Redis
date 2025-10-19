package com.example.springbootredis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BitmapDataStructureTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    @Test
    void testRedisBitmapOperations() {
        System.out.println("=== 开始测试 Redis Bitmap 数据结构 ===");

        String bitmapKey = "bitmap:test:signIn:2025-01";

        // 清理环境
        stringRedisTemplate.delete(bitmapKey);

        // 1. SETBIT 标记打卡日 (从0开始的bit位)
        stringRedisTemplate.opsForValue().setBit(bitmapKey, 0, true);  // 1号
        stringRedisTemplate.opsForValue().setBit(bitmapKey, 1, false); // 2号
        stringRedisTemplate.opsForValue().setBit(bitmapKey, 2, true);  // 3号
        stringRedisTemplate.opsForValue().setBit(bitmapKey, 30, true); // 31号

        // 2. GETBIT 校验
        assertTrue(stringRedisTemplate.opsForValue().getBit(bitmapKey, 0));
        assertFalse(stringRedisTemplate.opsForValue().getBit(bitmapKey, 1));
        assertTrue(stringRedisTemplate.opsForValue().getBit(bitmapKey, 2));
        assertTrue(stringRedisTemplate.opsForValue().getBit(bitmapKey, 30));

        // 3. BITCOUNT 统计打卡天数
        Long count = stringRedisTemplate.execute((RedisCallback<Long>) connection -> connection.bitCount(bitmapKey.getBytes()));
        assertNotNull(count);
        assertEquals(3L, count);

        // 4. BITFIELD 读取指定范围（示例读取前8位作为有符号整型）
        List<Long> field = stringRedisTemplate.execute((RedisCallback<List<Long>>) connection ->
                connection.bitField(bitmapKey.getBytes(), BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.INT_8)
                        .valueAt(0))
        );
        assertNotNull(field);

        // 清理
        assertTrue(Boolean.TRUE.equals(stringRedisTemplate.delete(bitmapKey)));

        System.out.println("=== Redis Bitmap 数据结构测试通过 ===");
    }
}


