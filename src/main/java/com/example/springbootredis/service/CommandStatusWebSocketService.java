package com.example.springbootredis.service;

import com.alibaba.fastjson2.JSON;
import com.example.springbootredis.entity.Command;
import com.example.springbootredis.entity.enums.CommandStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket推送服务
 */
@Service
public class CommandStatusWebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * 推送遥测参数判定结果
     * 
     * @param commandId 指令ID
     * @param paramCode 参数代码
     * @param paramName 参数名称
     * @param expectedValue 期望值
     * @param actualValue 实际值
     * @param judgeResult 判定结果
     */
    public void pushTelemetryJudgeResult(Long commandId, String paramCode, String paramName,
                                       String expectedValue, String actualValue, boolean judgeResult) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TELEMETRY_JUDGE_RESULT");
        message.put("commandId", commandId);
        message.put("paramCode", paramCode);
        message.put("paramName", paramName);
        message.put("expectedValue", expectedValue);
        message.put("actualValue", actualValue);
        message.put("judgeResult", judgeResult);
        message.put("judgeTime", LocalDateTime.now());
        
        // 推送到指令专用频道
        messagingTemplate.convertAndSend("/topic/command/" + commandId, message);
        
        // 推送到全局频道
        messagingTemplate.convertAndSend("/topic/telemetry", message);
        
        System.out.println("推送遥测判定结果: " + JSON.toJSONString(message));
    }
    
    /**
     * 推送指令状态变更
     * 
     * @param commandId 指令ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param reason 变更原因
     */
    public void pushCommandStatusChange(Long commandId, CommandStatus oldStatus, 
                                      CommandStatus newStatus, String reason) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "COMMAND_STATUS_CHANGE");
        message.put("commandId", commandId);
        message.put("oldStatus", oldStatus);
        message.put("newStatus", newStatus);
        message.put("reason", reason);
        message.put("changeTime", LocalDateTime.now());
        
        // 推送到指令专用频道
        messagingTemplate.convertAndSend("/topic/command/" + commandId, message);
        
        // 推送到全局状态变更频道
        messagingTemplate.convertAndSend("/topic/status", message);
        
        System.out.println("推送状态变更: " + JSON.toJSONString(message));
    }
    
    /**
     * 推送指令执行进度
     * 
     * @param commandId 指令ID
     * @param totalParams 总参数数
     * @param satisfiedParams 已满足参数数
     * @param progress 进度百分比
     * @param remainingParams 剩余参数列表
     */
    public void pushCommandProgress(Long commandId, int totalParams, int satisfiedParams, 
                                  int progress, String[] remainingParams) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "COMMAND_PROGRESS");
        message.put("commandId", commandId);
        message.put("totalParams", totalParams);
        message.put("satisfiedParams", satisfiedParams);
        message.put("progress", progress);
        message.put("remainingParams", remainingParams);
        message.put("updateTime", LocalDateTime.now());
        
        // 推送到指令专用频道
        messagingTemplate.convertAndSend("/topic/command/" + commandId, message);
        
        // 推送到全局进度频道
        messagingTemplate.convertAndSend("/topic/progress", message);
        
        System.out.println("推送执行进度: " + JSON.toJSONString(message));
    }
    
    /**
     * 推送指令完成通知
     * 
     * @param command 完成的指令
     */
    public void pushCommandCompleted(Command command) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "COMMAND_COMPLETED");
        message.put("commandId", command.getId());
        message.put("commandCode", command.getCommandCode());
        message.put("satelliteId", command.getSatelliteId());
        message.put("content", command.getContent());
        message.put("completeTime", LocalDateTime.now());
        
        // 推送到指令专用频道
        messagingTemplate.convertAndSend("/topic/command/" + command.getId(), message);
        
        // 推送到全局完成频道
        messagingTemplate.convertAndSend("/topic/completed", message);
        
        System.out.println("推送指令完成: " + JSON.toJSONString(message));
    }
    
    /**
     * 推送指令失败通知
     * 
     * @param command 失败的指令
     * @param reason 失败原因
     */
    public void pushCommandFailed(Command command, String reason) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "COMMAND_FAILED");
        message.put("commandId", command.getId());
        message.put("commandCode", command.getCommandCode());
        message.put("satelliteId", command.getSatelliteId());
        message.put("content", command.getContent());
        message.put("reason", reason);
        message.put("failTime", LocalDateTime.now());
        
        // 推送到指令专用频道
        messagingTemplate.convertAndSend("/topic/command/" + command.getId(), message);
        
        // 推送到全局失败频道
        messagingTemplate.convertAndSend("/topic/failed", message);
        
        System.out.println("推送指令失败: " + JSON.toJSONString(message));
    }
    
    /**
     * 推送系统状态信息
     * 
     * @param stats 统计信息
     */
    public void pushSystemStats(CommandManagementService.CommandStats stats) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SYSTEM_STATS");
        message.put("totalCommands", stats.getTotalCommands());
        message.put("pendingCount", stats.getPendingCount());
        message.put("preparedCount", stats.getPreparedCount());
        message.put("executingCount", stats.getExecutingCount());
        message.put("completedCount", stats.getCompletedCount());
        message.put("failedCount", stats.getFailedCount());
        message.put("updateTime", LocalDateTime.now());
        
        // 推送到系统状态频道
        messagingTemplate.convertAndSend("/topic/system", message);
        
        System.out.println("推送系统状态: " + JSON.toJSONString(message));
    }
}
