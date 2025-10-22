package com.example.springbootredis.kafka;

import com.alibaba.fastjson2.JSON;
import com.example.springbootredis.entity.Command;
import com.example.springbootredis.entity.enums.CommandStatus;
import com.example.springbootredis.service.CommandManagementService;
import com.example.springbootredis.service.CommandStatusWebSocketService;
import com.example.springbootredis.service.TelemetryJudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 遥测数据Kafka消费者
 */
@Service
public class TelemetryKafkaConsumer {
    
    @Autowired
    private CommandManagementService commandManagementService;
    
    @Autowired
    private TelemetryJudgeService telemetryJudgeService;
    
    @Autowired
    private CommandStatusWebSocketService webSocketService;
    
    /**
     * 消费遥测数据
     * 
     * @param message Kafka消息
     */
    @KafkaListener(topics = "satellite-telemetry", groupId = "satellite-telemetry-group")
    public void consumeTelemetryData(String message) {
        try {
            System.out.println("收到Kafka遥测数据: " + message);
            
            // 解析Kafka消息
            TelemetryMessage telemetryMessage = parseTelemetryMessage(message);
            if (telemetryMessage == null) {
                System.out.println("遥测数据解析失败");
                return;
            }
            
            // 根据指令代码查找指令
            Command command = commandManagementService.findByCommandCode(telemetryMessage.getCommandCode());
            if (command == null) {
                System.out.println("未找到指令代码: " + telemetryMessage.getCommandCode());
                return;
            }
            
            // 如果指令是已编制状态，更新为执行中
            if (CommandStatus.PREPARED.equals(command.getStatus())) {
                commandManagementService.updateCommandStatus(command.getId(), CommandStatus.EXECUTING, 
                    "收到遥测数据，开始执行");
                webSocketService.pushCommandStatusChange(command.getId(), CommandStatus.PREPARED, 
                    CommandStatus.EXECUTING, "收到遥测数据，开始执行");
            }
            
            // 处理遥测数据并判断
            boolean judgeResult = telemetryJudgeService.processTelemetryData(
                command.getId(),
                telemetryMessage.getParamCode(),
                telemetryMessage.getActualValue(),
                message
            );
            
            // 推送判定结果
            webSocketService.pushTelemetryJudgeResult(
                command.getId(),
                telemetryMessage.getParamCode(),
                telemetryMessage.getParamName(),
                telemetryMessage.getExpectedValue(),
                telemetryMessage.getActualValue(),
                judgeResult
            );
            
            // 检查指令是否完成
            if (CommandStatus.EXECUTING.equals(command.getStatus())) {
                boolean isCompleted = telemetryJudgeService.checkCommandCompletion(command.getId());
                if (isCompleted) {
                    commandManagementService.updateCommandStatus(command.getId(), CommandStatus.COMPLETED, 
                        "所有遥测参数满足要求");
                    webSocketService.pushCommandStatusChange(command.getId(), CommandStatus.EXECUTING, 
                        CommandStatus.COMPLETED, "所有遥测参数满足要求");
                    webSocketService.pushCommandCompleted(command);
                } else {
                    // 推送进度信息
                    Map<String, Object> stats = telemetryJudgeService.getCommandTelemetryStats(command.getId());
                    int progress = (Integer) stats.get("progress");
                    int totalParams = (Integer) stats.get("totalParams");
                    int satisfiedParams = (Integer) stats.get("satisfiedParams");
                    
                    webSocketService.pushCommandProgress(command.getId(), totalParams, satisfiedParams, 
                        progress, new String[]{}); // 这里可以添加剩余参数列表
                }
            }
            
        } catch (Exception e) {
            System.err.println("处理遥测数据异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 解析遥测消息
     * 
     * @param message 原始消息
     * @return 解析后的遥测消息对象
     */
    private TelemetryMessage parseTelemetryMessage(String message) {
        try {
            Map<String, Object> data = JSON.parseObject(message, Map.class);
            
            TelemetryMessage telemetryMessage = new TelemetryMessage();
            telemetryMessage.setCommandCode((String) data.get("commandCode"));
            telemetryMessage.setParamCode((String) data.get("paramCode"));
            telemetryMessage.setParamName((String) data.get("paramName"));
            telemetryMessage.setActualValue(String.valueOf(data.get("actualValue")));
            telemetryMessage.setExpectedValue((String) data.get("expectedValue"));
            
            // 解析时间戳
            Object timestamp = data.get("timestamp");
            if (timestamp != null) {
                if (timestamp instanceof Long) {
                    telemetryMessage.setTimestamp(LocalDateTime.ofEpochSecond((Long) timestamp / 1000, 0,
                            java.time.ZoneOffset.UTC));
                } else if (timestamp instanceof String) {
                    telemetryMessage.setTimestamp(LocalDateTime.parse((String) timestamp));
                }
            } else {
                telemetryMessage.setTimestamp(LocalDateTime.now());
            }
            
            return telemetryMessage;
            
        } catch (Exception e) {
            System.err.println("解析遥测消息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 遥测消息内部类
     */
    public static class TelemetryMessage {
        private String commandCode;
        private String paramCode;
        private String paramName;
        private String actualValue;
        private String expectedValue;
        private LocalDateTime timestamp;
        
        // Getters and Setters
        public String getCommandCode() { return commandCode; }
        public void setCommandCode(String commandCode) { this.commandCode = commandCode; }
        
        public String getParamCode() { return paramCode; }
        public void setParamCode(String paramCode) { this.paramCode = paramCode; }
        
        public String getParamName() { return paramName; }
        public void setParamName(String paramName) { this.paramName = paramName; }
        
        public String getActualValue() { return actualValue; }
        public void setActualValue(String actualValue) { this.actualValue = actualValue; }
        
        public String getExpectedValue() { return expectedValue; }
        public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        @Override
        public String toString() {
            return "TelemetryMessage{" +
                    "commandCode='" + commandCode + '\'' +
                    ", paramCode='" + paramCode + '\'' +
                    ", paramName='" + paramName + '\'' +
                    ", actualValue='" + actualValue + '\'' +
                    ", expectedValue='" + expectedValue + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
