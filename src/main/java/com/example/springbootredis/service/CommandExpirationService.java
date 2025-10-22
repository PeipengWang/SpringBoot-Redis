package com.example.springbootredis.service;

import com.example.springbootredis.entity.Command;
import com.example.springbootredis.entity.enums.CommandStatus;
import com.example.springbootredis.repository.CommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 指令过期清理定时任务服务（已重构为监控服务）
 * @deprecated 使用 CommandStatusMonitorService 替代
 */
@Service
@EnableScheduling
@Deprecated
public class CommandExpirationService {
    
    @Autowired
    private CommandRepository commandRepository;
    
    /**
     * 每分钟执行一次过期指令清理任务
     */
    @Scheduled(fixedRate = 60000) // 60秒 = 60000毫秒
    public void cleanupExpiredCommands() {
        try {
            // 查找过期的执行中指令
            List<Command> expiredCommands = commandRepository.findExecutingCommandsExpired(
                CommandStatus.EXECUTING, LocalDateTime.now());
            
            int cleanedCount = 0;
            for (Command command : expiredCommands) {
                command.setStatus(CommandStatus.FAILED);
                commandRepository.save(command);
                cleanedCount++;
            }
            
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
            
            // 统计各状态指令数量
            long totalCommands = commandRepository.count();
            long pendingCount = commandRepository.countByStatus(CommandStatus.PENDING);
            long preparedCount = commandRepository.countByStatus(CommandStatus.PREPARED);
            long executingCount = commandRepository.countByStatus(CommandStatus.EXECUTING);
            long completedCount = commandRepository.countByStatus(CommandStatus.COMPLETED);
            long failedCount = commandRepository.countByStatus(CommandStatus.FAILED);
            
            System.out.println(String.format("指令统计: 总计=%d, 未执行=%d, 已编制=%d, 执行中=%d, 已完成=%d, 失败=%d",
                totalCommands, pendingCount, preparedCount, executingCount, completedCount, failedCount));
            
            // 执行清理
            cleanupExpiredCommands();
            System.out.println("全面清理任务完成");
            
        } catch (Exception e) {
            System.err.println("全面清理任务执行失败: " + e.getMessage());
        }
    }
}
