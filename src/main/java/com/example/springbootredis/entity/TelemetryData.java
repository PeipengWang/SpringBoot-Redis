package com.example.springbootredis.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 遥测数据实体类
 */
@Entity
@Table(name = "tb_telemetry_data")
public class TelemetryData {
    
    /**
     * 数据ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 指令ID
     */
    @Column(name = "command_id", nullable = false)
    private Long commandId;
    
    /**
     * 参数代码
     */
    @Column(name = "param_code", nullable = false, length = 50)
    private String paramCode;
    
    /**
     * 实际值
     */
    @Column(name = "actual_value", nullable = false, length = 100)
    private String actualValue;
    
    /**
     * 接收时间
     */
    @Column(name = "receive_time", nullable = false)
    private LocalDateTime receiveTime;
    
    /**
     * 判断结果（true：满足要求，false：不满足要求）
     */
    @Column(name = "judge_result")
    private Boolean judgeResult;
    
    /**
     * 判断时间
     */
    @Column(name = "judge_time")
    private LocalDateTime judgeTime;
    
    /**
     * 数据来源（KAFKA, MANUAL等）
     */
    @Column(name = "data_source", length = 20)
    private String dataSource = "KAFKA";
    
    /**
     * 原始消息（用于调试）
     */
    @Column(name = "raw_message", length = 1000)
    private String rawMessage;
    
    // 构造函数
    public TelemetryData() {
        this.receiveTime = LocalDateTime.now();
    }
    
    public TelemetryData(Long commandId, String paramCode, String actualValue) {
        this();
        this.commandId = commandId;
        this.paramCode = paramCode;
        this.actualValue = actualValue;
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCommandId() {
        return commandId;
    }
    
    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
    
    public String getParamCode() {
        return paramCode;
    }
    
    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }
    
    public String getActualValue() {
        return actualValue;
    }
    
    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }
    
    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }
    
    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }
    
    public Boolean getJudgeResult() {
        return judgeResult;
    }
    
    public void setJudgeResult(Boolean judgeResult) {
        this.judgeResult = judgeResult;
        if (judgeResult != null) {
            this.judgeTime = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getJudgeTime() {
        return judgeTime;
    }
    
    public void setJudgeTime(LocalDateTime judgeTime) {
        this.judgeTime = judgeTime;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    
    public String getRawMessage() {
        return rawMessage;
    }
    
    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }
    
    @Override
    public String toString() {
        return "TelemetryData{" +
                "id=" + id +
                ", commandId=" + commandId +
                ", paramCode='" + paramCode + '\'' +
                ", actualValue='" + actualValue + '\'' +
                ", receiveTime=" + receiveTime +
                ", judgeResult=" + judgeResult +
                ", judgeTime=" + judgeTime +
                ", dataSource='" + dataSource + '\'' +
                ", rawMessage='" + rawMessage + '\'' +
                '}';
    }
}
