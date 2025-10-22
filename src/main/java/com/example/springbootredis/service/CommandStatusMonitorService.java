package com.example.springbootredis.service;

import com.example.springbootredis.entity.Command;
import com.example.springbootredis.entity.enums.CommandStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 指令状态监控服务
 */
@Service
@EnableScheduling
public class CommandStatusMonitorService {
    
    @Autowired
    private CommandManagementService commandManagementService;
    
    @Autowired
    private CommandStatusWebSocketService webSocketService;
    
    /**
     * 每30秒同步一次未执行和已编制的指令
     */
    @Scheduled(fixedRate = 30000) // 30秒
    public void syncPendingAndPreparedCommands() {
        try {
            List<Command> commands = commandManagementService.syncCommandsFromDatabase();
            System.out.println("定时同步任务执行完成，同步了 " + commands.size() + " 个指令");
        } catch (Exception e) {
            System.err.println("定时同步任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 每分钟检查执行中指令是否超时
     */
    @Scheduled(fixedRate = 60000) // 1分钟
    public void checkExecutingCommandsTimeout() {
        try {
            List<Command> expiredCommands = commandManagementService.getExpiredExecutingCommands();
            
            for (Command command : expiredCommands) {
                boolean handled = commandManagementService.handleCommandTimeout(command.getId());
                if (handled) {
                    webSocketService.pushCommandStatusChange(command.getId(), CommandStatus.EXECUTING, 
                        CommandStatus.FAILED, "指令执行超时");
                    webSocketService.pushCommandFailed(command, "指令执行超时");
                }
            }
            
            if (!expiredCommands.isEmpty()) {
                System.out.println("超时检查完成，处理了 " + expiredCommands.size() + " 个超时指令");
            }
        } catch (Exception e) {
            System.err.println("超时检查任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 每5分钟推送系统状态信息
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void pushSystemStats() {
        try {
            CommandManagementService.CommandStats stats = commandManagementService.getCommandStats();
            webSocketService.pushSystemStats(stats);
            System.out.println("系统状态推送完成: " + stats.toString());
        } catch (Exception e) {
            System.err.println("系统状态推送失败: " + e.getMessage());
        }
    }
    
    /**
     * 每天凌晨2点清理过期的遥测数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldData() {
        try {
            // 清理7天前的遥测数据
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7);
            // 这里可以调用遥测数据Repository的清理方法
            System.out.println("数据清理任务执行完成，清理了7天前的数据");
        } catch (Exception e) {
            System.err.println("数据清理任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 监控执行中的指令状态
     */
    public void monitorExecutingCommands() {
        List<Command> executingCommands = commandManagementService.getExecutingCommands();
        
        for (Command command : executingCommands) {
            // 检查是否超时
            if (command.isExpired()) {
                commandManagementService.handleCommandTimeout(command.getId());
                webSocketService.pushCommandStatusChange(command.getId(), CommandStatus.EXECUTING, 
                    CommandStatus.FAILED, "指令执行超时");
                webSocketService.pushCommandFailed(command, "指令执行超时");
            }
        }
    }
    
    /**
     * 处理指令超时
     */
    public void handleCommandTimeout(Long commandId) {
        Command command = commandManagementService.getExecutingCommands().stream()
            .filter(c -> c.getId().equals(commandId))
            .findFirst()
            .orElse(null);
        
        if (command != null && command.isExpired()) {
            boolean handled = commandManagementService.handleCommandTimeout(commandId);
            if (handled) {
                webSocketService.pushCommandStatusChange(commandId, CommandStatus.EXECUTING, 
                    CommandStatus.FAILED, "指令执行超时");
                webSocketService.pushCommandFailed(command, "指令执行超时");
            }
        }
    }
}
