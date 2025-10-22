package com.example.springbootredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Redis 应用主类
 * 卫星指令监控系统
 */
@SpringBootApplication
@EnableScheduling
public class SpringBootRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRedisApplication.class, args);
        System.out.println("=== 卫星指令监控系统启动完成 ===");
        System.out.println("WebSocket端点: ws://localhost:8080/ws");
        System.out.println("Kafka消费者已启动，监听topic: satellite-telemetry");
        System.out.println("定时任务已启动，监控指令状态");
    }
}
