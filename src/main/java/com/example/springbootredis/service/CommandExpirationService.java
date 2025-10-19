package com.example.springbootredis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 指令过期清理定时任务服务
 */
@Service
@EnableScheduling
public class CommandExpirationService {
    
    @Autowired
    private SatelliteCommandService satelliteCommandService;
    
    /**
     * 每分钟执行一次过期指令清理任务
     */
    @Scheduled(fixedRate = 60000) // 60秒 = 60000毫秒
    public void cleanupExpiredCommands() {
        try {
            int cleanedCount = satelliteCommandService.cleanupExpiredCommands();
            if (cleanedCount > 0) {
                System.out.println("定时清理任务执行完成，清理了 " + cleanedCount + " 个过期指令");
            }
        } catch (Exception e) {
            System.err.println("定时清理任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 每小时执行一次更全面的清理任务
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void comprehensiveCleanup() {
        try {
            System.out.println("开始执行全面清理任务...");
            
            // 获取所有卫星
            var satelliteIds = satelliteCommandService.getAllSatelliteIds();
            System.out.println("当前共有 " + satelliteIds.size() + " 个卫星");
            
            // 统计每个卫星的指令数量
            for (String satelliteId : satelliteIds) {
                long commandCount = satelliteCommandService.getCommandCountBySatellite(satelliteId);
                System.out.println("卫星 " + satelliteId + " 当前有 " + commandCount + " 个指令");
            }
            
            // 执行清理
            int cleanedCount = satelliteCommandService.cleanupExpiredCommands();
            System.out.println("全面清理任务完成，清理了 " + cleanedCount + " 个过期指令");
            
        } catch (Exception e) {
            System.err.println("全面清理任务执行失败: " + e.getMessage());
        }
    }
}
