package com.example.springbootredis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.RedisHashCommands;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SpringBootRedisApplicationTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void testRedisString() {
        // 测试键名和值
        String testKey = "testKey";
        String testValue = "testValue";

        // 1. 测试存储字符串
        stringRedisTemplate.opsForValue().set(testKey, testValue);

        // 2. 测试读取字符串
        String retrievedValue = stringRedisTemplate.opsForValue().get(testKey);
        assertEquals(testValue, retrievedValue, "存储和读取的值应该相等");

        // 3. 测试判断键是否存在
        Boolean hasKey = stringRedisTemplate.hasKey(testKey);
        assertTrue(hasKey, "键应该存在");

        // 4. 测试删除键
        Boolean deleteResult = stringRedisTemplate.delete(testKey);
        assertTrue(deleteResult, "删除操作应该成功");

        // 5. 验证删除后键不存在
        Boolean keyExistsAfterDelete = stringRedisTemplate.hasKey(testKey);
        assertFalse(keyExistsAfterDelete, "删除后键应该不存在");

        // 6. 验证删除后读取返回null
        String valueAfterDelete = stringRedisTemplate.opsForValue().get(testKey);
        assertNull(valueAfterDelete, "删除后读取应该返回null");

        System.out.println("Redis String 数据类型测试通过！");
    }

    @Test
    void testRedisString2() {
        // 测试键名和值
        String testKey = "testKey:";
        String testValue = "testValue";

        // 1. 测试存储字符串
        stringRedisTemplate.opsForValue().set(testKey + "a:b", testValue);


        System.out.println("Redis String 数据类型测试通过！");
    }

    @Test
    void testRedisHash() {
        System.out.println("=== 开始测试 Redis Hash 数据结构 ===");

        // 测试 Hash 键名
        String hashKey = "user:1001";

        // 获取 Hash 操作对象
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();

        // 1. 测试存储 Hash 字段
        hashOps.put(hashKey, "name", "张三");
        hashOps.put(hashKey, "age", "25");
        hashOps.put(hashKey, "email", "zhangsan@example.com");
        hashOps.put(hashKey, "city", "北京");

        // 2. 测试读取单个 Hash 字段
        String name = hashOps.get(hashKey, "name");
        assertEquals("张三", name, "姓名应该正确");

        String age = hashOps.get(hashKey, "age");
        assertEquals("25", age, "年龄应该正确");

        // 3. 测试读取不存在的字段
        String nonExistentField = hashOps.get(hashKey, "phone");
        assertNull(nonExistentField, "不存在的字段应该返回null");

        // 4. 测试判断 Hash 字段是否存在
        Boolean hasName = hashOps.hasKey(hashKey, "name");
        assertTrue(hasName, "name字段应该存在");

        Boolean hasPhone = hashOps.hasKey(hashKey, "phone");
        assertFalse(hasPhone, "phone字段不应该存在");

        // 5. 测试获取所有 Hash 字段和值
        Map<String, String> allFields = hashOps.entries(hashKey);
        assertEquals(4, allFields.size(), "应该有4个字段");
        assertTrue(allFields.containsKey("name"), "应该包含name字段");
        assertTrue(allFields.containsKey("age"), "应该包含age字段");
        assertTrue(allFields.containsKey("email"), "应该包含email字段");
        assertTrue(allFields.containsKey("city"), "应该包含city字段");

        System.out.println("Hash 所有字段: " + allFields);

        // 6. 测试获取所有字段名
        Set<String> fieldNames = hashOps.keys(hashKey);
        assertEquals(4, fieldNames.size(), "应该有4个字段名");
        assertTrue(fieldNames.contains("name"), "应该包含name字段名");
        assertTrue(fieldNames.contains("age"), "应该包含age字段名");

        System.out.println("Hash 字段名: " + fieldNames);

        // 7. 测试获取所有字段值
        java.util.List<String> fieldValues = hashOps.values(hashKey);
        assertEquals(4, fieldValues.size(), "应该有4个字段值");
        assertTrue(fieldValues.contains("张三"), "应该包含姓名值");
        assertTrue(fieldValues.contains("25"), "应该包含年龄值");

        System.out.println("Hash 字段值: " + fieldValues);

        // 8. 测试获取 Hash 字段数量
        Long fieldCount = hashOps.size(hashKey);
        assertEquals(4L, fieldCount, "应该有4个字段");

        // 9. 测试删除单个 Hash 字段
        Long deletedCount = hashOps.delete(hashKey, "city");
        assertEquals(1L, deletedCount, "应该删除1个字段");

        // 验证删除后的状态
        String cityAfterDelete = hashOps.get(hashKey, "city");
        assertNull(cityAfterDelete, "删除后city字段应该不存在");

        Long fieldCountAfterDelete = hashOps.size(hashKey);
        assertEquals(3L, fieldCountAfterDelete, "删除后应该有3个字段");

        // 10. 测试更新 Hash 字段值
        hashOps.put(hashKey, "age", "26");
        String updatedAge = hashOps.get(hashKey, "age");
        assertEquals("26", updatedAge, "年龄应该更新为26");

        // 11. 测试判断整个 Hash 是否存在
        Boolean hashExists = stringRedisTemplate.hasKey(hashKey);
        assertTrue(hashExists, "Hash应该存在");

        // 12. 测试删除整个 Hash
        Boolean hashDeleted = stringRedisTemplate.delete(hashKey);
        assertTrue(hashDeleted, "删除Hash应该成功");

        // 验证删除后的状态
        Boolean hashExistsAfterDelete = stringRedisTemplate.hasKey(hashKey);
        assertFalse(hashExistsAfterDelete, "删除后Hash应该不存在");

        System.out.println("=== Redis Hash 数据结构测试通过 ===");
    }

    @Test
    void testRedisScan() {
        System.out.println("=== 开始测试 Redis SCAN/ HSCAN ===");

        // 构造一些测试键
        String[] keysToCreate = new String[]{
                "scan:user:1", "scan:user:2", "scan:user:3",
                "scan:order:1", "other:ignore:1"
        };
        for (String k : keysToCreate) {
            stringRedisTemplate.opsForValue().set(k, "v:" + k);
        }

        // 使用 SCAN 匹配 scan:user:*
        List<String> scannedKeys = stringRedisTemplate.execute((RedisCallback<List<String>>) connection -> {
            List<String> result = new ArrayList<>();
            ScanOptions options = ScanOptions.scanOptions()
                    .match("scan:user:*")
                    .count(1000)
                    .build();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    result.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return result;
        });

        assertNotNull(scannedKeys);
        assertTrue(scannedKeys.contains("scan:user:1"));
        assertTrue(scannedKeys.contains("scan:user:2"));
        assertTrue(scannedKeys.contains("scan:user:3"));
        assertFalse(scannedKeys.contains("scan:order:1"));
        assertFalse(scannedKeys.contains("other:ignore:1"));

        // 构造一个 Hash 并使用 HSCAN
        String hashKey = "scan:hash:users";
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        hashOps.put(hashKey, "field:001", "Alice");
        hashOps.put(hashKey, "field:002", "Bob");
        hashOps.put(hashKey, "other:003", "Charlie");

        List<String> scannedHashFields = stringRedisTemplate.execute((RedisCallback<List<String>>) connection -> {
            List<String> result = new ArrayList<>();
            ScanOptions options = ScanOptions.scanOptions()
                    .match("field:*")
                    .count(1000)
                    .build();
            try (Cursor<Map.Entry<byte[], byte[]>> cursor = connection.hScan(hashKey.getBytes(StandardCharsets.UTF_8), options)) {
                while (cursor.hasNext()) {
                    Map.Entry<byte[], byte[]> entry = cursor.next();
                    result.add(new String(entry.getKey(), StandardCharsets.UTF_8));
                }
            }
            return result;
        });

        assertNotNull(scannedHashFields);
        assertTrue(scannedHashFields.contains("field:001"));
        assertTrue(scannedHashFields.contains("field:002"));
        assertFalse(scannedHashFields.contains("other:003"));

        // 清理数据
        for (String k : keysToCreate) {
            stringRedisTemplate.delete(k);
        }
        stringRedisTemplate.delete(hashKey);

        System.out.println("=== Redis SCAN / HSCAN 测试通过 ===");
    }

}
