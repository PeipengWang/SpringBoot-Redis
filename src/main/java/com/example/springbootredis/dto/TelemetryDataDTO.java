package com.example.springbootredis.dto;

import java.time.LocalDateTime;

/**
 * 遥测数据传输对象
 */
public class TelemetryDataDTO {
    
    private Long commandId;
    private String commandCode;
    private String paramCode;
    private String paramName;
    private String actualValue;
    private String expectedValue;
    private Boolean judgeResult;
    private LocalDateTime receiveTime;
    private LocalDateTime judgeTime;
    private String dataSource;
    
    // 构造函数
    public TelemetryDataDTO() {}
    
    public TelemetryDataDTO(Long commandId, String commandCode, String paramCode, 
                          String paramName, String actualValue, String expectedValue) {
        this.commandId = commandId;
        this.commandCode = commandCode;
        this.paramCode = paramCode;
        this.paramName = paramName;
        this.actualValue = actualValue;
        this.expectedValue = expectedValue;
        this.receiveTime = LocalDateTime.now();
        this.dataSource = "KAFKA";
    }
    
    // Getter 和 Setter 方法
    public Long getCommandId() { return commandId; }
    public void setCommandId(Long commandId) { this.commandId = commandId; }
    
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
    
    public Boolean getJudgeResult() { return judgeResult; }
    public void setJudgeResult(Boolean judgeResult) { 
        this.judgeResult = judgeResult;
        if (judgeResult != null) {
            this.judgeTime = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getReceiveTime() { return receiveTime; }
    public void setReceiveTime(LocalDateTime receiveTime) { this.receiveTime = receiveTime; }
    
    public LocalDateTime getJudgeTime() { return judgeTime; }
    public void setJudgeTime(LocalDateTime judgeTime) { this.judgeTime = judgeTime; }
    
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    
    @Override
    public String toString() {
        return "TelemetryDataDTO{" +
                "commandId=" + commandId +
                ", commandCode='" + commandCode + '\'' +
                ", paramCode='" + paramCode + '\'' +
                ", paramName='" + paramName + '\'' +
                ", actualValue='" + actualValue + '\'' +
                ", expectedValue='" + expectedValue + '\'' +
                ", judgeResult=" + judgeResult +
                ", receiveTime=" + receiveTime +
                ", judgeTime=" + judgeTime +
                ", dataSource='" + dataSource + '\'' +
                '}';
    }
}
