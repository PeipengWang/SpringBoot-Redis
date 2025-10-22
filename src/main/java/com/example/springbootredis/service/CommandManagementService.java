package com.example.springbootredis.service;

import com.example.springbootredis.entity.Command;
import com.example.springbootredis.entity.enums.CommandStatus;
import com.example.springbootredis.repository.CommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 指令管理服务
 */
@Service
public class CommandManagementService {
    
    @Autowired
    private CommandRepository commandRepository;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String COMMAND_CACHE_PREFIX = "command:";
    private static final String EXECUTING_COMMANDS_KEY = "executing_commands";
    
    /**
     * 同步数据库中未执行和已编制的指令到Redis缓存
     */
    @Transactional(readOnly = true)
    public List<Command> syncCommandsFromDatabase() {
        List<CommandStatus> statuses = List.of(CommandStatus.PENDING, CommandStatus.PREPARED);
        List<Command> commands = commandRepository.findByStatusInOrderByCreateTimeDesc(statuses);
        
        // 缓存到Redis
        for (Command command : commands) {
            String cacheKey = COMMAND_CACHE_PREFIX + command.getId();
            redisTemplate.opsForValue().set(cacheKey, command.toString(), 1, TimeUnit.HOURS);
        }
        
        System.out.println("同步了 " + commands.size() + " 个未执行和已编制的指令到缓存");
        return commands;
    }
    
    /**
     * 更新指令状态
     * 
     * @param commandId 指令ID
     * @param newStatus 新状态
     * @param reason 状态变更原因
     * @return 是否更新成功
     */
    @Transactional
    public boolean updateCommandStatus(Long commandId, CommandStatus newStatus, String reason) {
        Command command = commandRepository.findById(commandId).orElse(null);
        if (command == null) {
            System.out.println("指令不存在: " + commandId);
            return false;
        }
        
        CommandStatus oldStatus = command.getStatus();
        command.setStatus(newStatus);
        commandRepository.save(command);
        
        // 更新Redis缓存
        updateCommandCache(command);
        
        // 如果是执行中状态，添加到执行中指令集合
        if (CommandStatus.EXECUTING.equals(newStatus)) {
            redisTemplate.opsForSet().add(EXECUTING_COMMANDS_KEY, commandId.toString());
        } else {
            // 其他状态从执行中集合移除
            redisTemplate.opsForSet().remove(EXECUTING_COMMANDS_KEY, commandId.toString());
        }
        
        System.out.println(String.format("指令状态更新: ID=%d, %s -> %s, 原因=%s", 
            commandId, oldStatus, newStatus, reason));
        
        return true;
    }
    
    /**
     * 检查指令是否超时
     * 
     * @param commandId 指令ID
     * @return 是否超时
     */
    public boolean checkCommandTimeout(Long commandId) {
        Command command = commandRepository.findById(commandId).orElse(null);
        if (command == null) {
            return false;
        }
        
        return command.isExpired();
    }
    
    /**
     * 处理超时指令
     * 
     * @param commandId 指令ID
     * @return 是否处理成功
     */
    @Transactional
    public boolean handleCommandTimeout(Long commandId) {
        Command command = commandRepository.findById(commandId).orElse(null);
        if (command == null) {
            return false;
        }
        
        if (command.isExpired() && CommandStatus.EXECUTING.equals(command.getStatus())) {
            return updateCommandStatus(commandId, CommandStatus.FAILED, "指令执行超时");
        }
        
        return false;
    }
    
    /**
     * 获取执行中的指令列表
     */
    public List<Command> getExecutingCommands() {
        return commandRepository.findByStatusOrderByCreateTimeDesc(CommandStatus.EXECUTING);
    }
    
    /**
     * 获取超时的执行中指令
     */
    public List<Command> getExpiredExecutingCommands() {
        return commandRepository.findExecutingCommandsExpired(CommandStatus.EXECUTING, LocalDateTime.now());
    }
    
    /**
     * 根据指令代码查找指令
     */
    public Command findByCommandCode(String commandCode) {
        return commandRepository.findByCommandCode(commandCode);
    }
    
    /**
     * 根据卫星ID获取指令列表
     */
    public List<Command> getCommandsBySatellite(String satelliteId) {
        return commandRepository.findBySatelliteIdOrderByCreateTimeDesc(satelliteId);
    }
    
    /**
     * 根据状态获取指令列表
     */
    public List<Command> getCommandsByStatus(CommandStatus status) {
        return commandRepository.findByStatusOrderByCreateTimeDesc(status);
    }
    
    /**
     * 保存指令
     */
    @Transactional
    public Command saveCommand(Command command) {
        Command savedCommand = commandRepository.save(command);
        updateCommandCache(savedCommand);
        return savedCommand;
    }
    
    /**
     * 更新指令缓存
     */
    private void updateCommandCache(Command command) {
        String cacheKey = COMMAND_CACHE_PREFIX + command.getId();
        redisTemplate.opsForValue().set(cacheKey, command.toString(), 1, TimeUnit.HOURS);
    }
    
    /**
     * 从缓存获取指令
     */
    public String getCommandFromCache(Long commandId) {
        String cacheKey = COMMAND_CACHE_PREFIX + commandId;
        return redisTemplate.opsForValue().get(cacheKey);
    }
    
    /**
     * 清除指令缓存
     */
    public void clearCommandCache(Long commandId) {
        String cacheKey = COMMAND_CACHE_PREFIX + commandId;
        redisTemplate.delete(cacheKey);
    }
    
    /**
     * 获取指令统计信息
     */
    public CommandStats getCommandStats() {
        long totalCommands = commandRepository.count();
        long pendingCount = commandRepository.countByStatus(CommandStatus.PENDING);
        long preparedCount = commandRepository.countByStatus(CommandStatus.PREPARED);
        long executingCount = commandRepository.countByStatus(CommandStatus.EXECUTING);
        long completedCount = commandRepository.countByStatus(CommandStatus.COMPLETED);
        long failedCount = commandRepository.countByStatus(CommandStatus.FAILED);
        
        return new CommandStats(totalCommands, pendingCount, preparedCount, 
            executingCount, completedCount, failedCount);
    }
    
    /**
     * 指令统计信息内部类
     */
    public static class CommandStats {
        private final long totalCommands;
        private final long pendingCount;
        private final long preparedCount;
        private final long executingCount;
        private final long completedCount;
        private final long failedCount;
        
        public CommandStats(long totalCommands, long pendingCount, long preparedCount,
                           long executingCount, long completedCount, long failedCount) {
            this.totalCommands = totalCommands;
            this.pendingCount = pendingCount;
            this.preparedCount = preparedCount;
            this.executingCount = executingCount;
            this.completedCount = completedCount;
            this.failedCount = failedCount;
        }
        
        // Getters
        public long getTotalCommands() { return totalCommands; }
        public long getPendingCount() { return pendingCount; }
        public long getPreparedCount() { return preparedCount; }
        public long getExecutingCount() { return executingCount; }
        public long getCompletedCount() { return completedCount; }
        public long getFailedCount() { return failedCount; }
        
        @Override
        public String toString() {
            return String.format("指令统计: 总计=%d, 未执行=%d, 已编制=%d, 执行中=%d, 已完成=%d, 失败=%d",
                totalCommands, pendingCount, preparedCount, executingCount, completedCount, failedCount);
        }
    }
}
