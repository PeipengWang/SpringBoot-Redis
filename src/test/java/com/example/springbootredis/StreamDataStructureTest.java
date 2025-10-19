package com.example.springbootredis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StreamDataStructureTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRedisStreamOperations() {
        System.out.println("=== 开始测试 Redis Stream 数据结构 ===");

        String streamKey = "stream:test:events";
        StreamOperations<String, String, String> streamOps = stringRedisTemplate.opsForStream();

        // 清理环境
        stringRedisTemplate.delete(streamKey);

        // 1. XADD 添加消息
        Map<String, String> payload1 = new HashMap<>();
        payload1.put("type", "USER_LOGIN");
        payload1.put("userId", "u001");

        Map<String, String> payload2 = new HashMap<>();
        payload2.put("type", "USER_LOGOUT");
        payload2.put("userId", "u001");

        RecordId id1 = streamOps.add(MapRecord.create(streamKey, payload1));
        RecordId id2 = streamOps.add(MapRecord.create(streamKey, payload2));

        assertNotNull(id1);
        assertNotNull(id2);

        // 2. XRANGE 读取所有消息
        List<MapRecord<String, String, String>> records = streamOps.range(streamKey, null, null);
        assertNotNull(records);
        assertEquals(2, records.size());

        // 3. XREAD 从起始偏移读取
        List<MapRecord<String, String, String>> readRecords = streamOps.read(StreamOffset.fromStart(streamKey));
        assertNotNull(readRecords);
        assertTrue(readRecords.size() >= 2);

        // 4. XDEL 删除消息（删除第一条）
        Long delCount = streamOps.delete(streamKey, id1);
        assertEquals(1L, delCount);

        // 5. XLEN 校验长度
        Long len = streamOps.size(streamKey);
        assertEquals(1L, len);

        // 清理
        stringRedisTemplate.delete(streamKey);

        System.out.println("=== Redis Stream 数据结构测试通过 ===");
    }
}


