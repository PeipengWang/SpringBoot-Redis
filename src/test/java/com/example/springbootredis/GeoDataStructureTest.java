package com.example.springbootredis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.GeoOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GeoDataStructureTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRedisGeoOperations() {
        System.out.println("=== 开始测试 Redis GEO 数据结构 ===");

        String geoKey = "geo:test:cities";
        GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();

        // 清理环境
        stringRedisTemplate.delete(geoKey);

        // 1. GEOADD 添加地理位置（经度, 纬度, 成员）
        Long added = geoOps.add(geoKey, new Point(116.4074, 39.9042), "Beijing");
        geoOps.add(geoKey, new Point(121.4737, 31.2304), "Shanghai");
        geoOps.add(geoKey, new Point(114.0579, 22.5431), "Shenzhen");
        assertTrue(added >= 1);

        // 2. GEODIST 计算两城市之间的距离（公里）
        Distance dist = geoOps.distance(geoKey, "Beijing", "Shanghai", RedisGeoCommands.DistanceUnit.KILOMETERS);
        assertNotNull(dist);
        assertTrue(dist.getValue() > 0);

        // 3. GEORADIUS 按圆形范围查询
        Circle circle = new Circle(new Point(121.4737, 31.2304), new Distance(1500, RedisGeoCommands.DistanceUnit.KILOMETERS));
        List<RedisGeoCommands.GeoLocation<String>> results = geoOps.radius(geoKey, circle).getContent();
        assertNotNull(results);
        assertTrue(results.size() >= 2);

        // 清理
        stringRedisTemplate.delete(geoKey);

        System.out.println("=== Redis GEO 数据结构测试通过 ===");
    }
}


