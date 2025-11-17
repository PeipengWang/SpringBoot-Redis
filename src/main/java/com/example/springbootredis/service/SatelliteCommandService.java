package com.example.springbootredis.service;

import com.example.springbootredis.entity.Command;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 卫星指令 Redis 服务类
 * 使用 Hash 数据结构存储多卫星的指令信息
 */
@Service
public class SatelliteCommandService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String SATELLITE_COMMANDS_PREFIX = "satellite_commands:";
    
    /**
     * 添加指令到指定卫星
     * 
     * @param command 指令对象
     * @return 是否添加成功
     */
    public boolean addCommand(Command command) {
        String hashKey = SATELLITE_COMMANDS_PREFIX + command.getSatelliteId();
        String commandJson = JSON.toJSONString(command);

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put(hashKey, String.valueOf(command.getId()), commandJson);

        // 设置整个 Hash 的过期时间为指令过期时间
        long expireSeconds = java.time.Duration
                .between(LocalDateTime.now(), command.getExpireTime())
                .getSeconds();
        if (expireSeconds > 0) {
            redisTemplate.expire(hashKey, expireSeconds, TimeUnit.SECONDS);
        }

        return true;
    }
    
    /**
     * 获取指定卫星的所有指令
     * 
     * @param satelliteId 卫星ID
     * @return 指令列表
     */
    public List<Command> getCommandsBySatellite(String satelliteId) {
        String hashKey = SATELLITE_COMMANDS_PREFIX + satelliteId;
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Map<String, String> commandMap = hashOps.entries(hashKey);
        
        List<Command> commands = new ArrayList<>();
        for (String commandJson : commandMap.values()) {
            Command command = JSON.parseObject(commandJson, Command.class);
            if (command != null) {
                commands.add(command);
            }
        }
        
        return commands;
    }
    
    /**
     * 获取指定卫星的指定指令
     * 
     * @param satelliteId 卫星ID
     * @param commandId 指令ID
     * @return 指令对象，如果不存在返回null
     */
    public Command getCommand(String satelliteId, String commandId) {
        String hashKey = SATELLITE_COMMANDS_PREFIX + satelliteId;
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String commandJson = hashOps.get(hashKey, commandId);
        
        if (commandJson == null) {
            return null;
        }
        
        return JSON.parseObject(commandJson, Command.class);
    }
    
    /**
     * 更新指令状态
     * 
     * @param satelliteId 卫星ID
     * @param commandId 指令ID
     * @param status 新状态
     * @return 是否更新成功
     */
    public boolean updateCommandStatus(String satelliteId, String commandId, String status) {
        Command command = getCommand(satelliteId, commandId);
        if (command == null) {
            return false;
        }
        
//        command.setStatus(status);
        return addCommand(command);
    }
    
    /**
     * 删除指定卫星的指定指令
     * 
     * @param satelliteId 卫星ID
     * @param commandId 指令ID
     * @return 是否删除成功
     */
    public boolean deleteCommand(String satelliteId, String commandId) {
        String hashKey = SATELLITE_COMMANDS_PREFIX + satelliteId;
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Long deletedCount = hashOps.delete(hashKey, commandId);
        return deletedCount != null && deletedCount > 0;
    }
    
    /**
     * 删除指定卫星的所有指令
     * 
     * @param satelliteId 卫星ID
     * @return 是否删除成功
     */
    public boolean deleteAllCommandsBySatellite(String satelliteId) {
        String hashKey = SATELLITE_COMMANDS_PREFIX + satelliteId;
        return Boolean.TRUE.equals(redisTemplate.delete(hashKey));
    }
    
    /**
     * 获取所有卫星ID
     * 
     * @return 卫星ID列表
     */
    public Set<String> getAllSatelliteIds() {
        Set<String> keys = redisTemplate.keys(SATELLITE_COMMANDS_PREFIX + "*");
        Set<String> satelliteIds = new HashSet<>();
        
        if (keys != null) {
            for (String key : keys) {
                String satelliteId = key.substring(SATELLITE_COMMANDS_PREFIX.length());
                satelliteIds.add(satelliteId);
            }
        }
        
        return satelliteIds;
    }
    
    /**
     * 获取所有待执行的指令（跨所有卫星）
     * 
     * @return 待执行指令列表
     */
    public List<Command> getAllPendingCommands() {
        List<Command> pendingCommands = new ArrayList<>();
        Set<String> satelliteIds = getAllSatelliteIds();
        
        for (String satelliteId : satelliteIds) {
            List<Command> commands = getCommandsBySatellite(satelliteId);
            for (Command command : commands) {
                if ("PENDING".equals(command.getStatus()) && command.isReadyToExecute()) {
                    pendingCommands.add(command);
                }
            }
        }
        
        return pendingCommands;
    }
    
    /**
     * 清理过期的指令
     * 
     * @return 清理的指令数量
     */
    public int cleanupExpiredCommands() {
        int cleanedCount = 0;
        Set<String> satelliteIds = getAllSatelliteIds();
        
        for (String satelliteId : satelliteIds) {
            List<Command> commands = getCommandsBySatellite(satelliteId);
            for (Command command : commands) {
                if (command.isExpired()) {
                    deleteCommand(satelliteId, String.valueOf(command.getId()));
                    cleanedCount++;
                }
            }
        }
        
        return cleanedCount;
    }
    
    /**
     * 获取指定卫星的指令数量
     * 
     * @param satelliteId 卫星ID
     * @return 指令数量
     */
    public long getCommandCountBySatellite(String satelliteId) {
        String hashKey = SATELLITE_COMMANDS_PREFIX + satelliteId;
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        return hashOps.size(hashKey);
    }
    
    /**
     * 检查指定卫星是否存在
     * 
     * @param satelliteId 卫星ID
     * @return 是否存在
     */
    public boolean satelliteExists(String satelliteId) {
        String hashKey = SATELLITE_COMMANDS_PREFIX + satelliteId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(hashKey));
    }


}
