package com.example.springbootredis.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.example.springbootredis.entity.enums.CommandStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 卫星指令实体类
 */
@Entity
@Table(name = "tb_command")
public class Command {
    
    /**
     * 指令ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 指令内容
     */
    @Column(name = "content", nullable = false, length = 500)
    private String content;
    
    /**
     * 执行时间
     */
    @Column(name = "execute_time", nullable = false)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;
    
    /**
     * 卫星ID
     */
    @Column(name = "satellite_id", nullable = false, length = 50)
    private String satelliteId;
    
    /**
     * 指令状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommandStatus status;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 过期时间（执行时间 + 超时时间）
     */
    @Column(name = "expire_time", nullable = false)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
    
    /**
     * 超时时间（分钟）
     */
    @Column(name = "timeout_duration", nullable = false)
    private Integer timeoutDuration = 30;
    
    /**
     * 指令代码（用于Kafka消息关联）
     */
    @Column(name = "command_code", length = 50)
    private String commandCode;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 200)
    private String remark;
    
    // 构造函数
    public Command() {
        this.createTime = LocalDateTime.now();
        this.status = CommandStatus.PENDING;
    }
    
    public Command(String content, LocalDateTime executeTime, String satelliteId) {
        this();
        this.content = content;
        this.executeTime = executeTime;
        this.satelliteId = satelliteId;
        // 设置过期时间为执行时间后超时时间
        this.expireTime = executeTime.plusMinutes(timeoutDuration);
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getExecuteTime() {
        return executeTime;
    }
    
    public void setExecuteTime(LocalDateTime executeTime) {
        this.executeTime = executeTime;
        // 更新过期时间
        this.expireTime = executeTime.plusMinutes(timeoutDuration);
    }
    
    public String getSatelliteId() {
        return satelliteId;
    }
    
    public void setSatelliteId(String satelliteId) {
        this.satelliteId = satelliteId;
    }
    
    public CommandStatus getStatus() {
        return status;
    }
    
    public void setStatus(CommandStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
    
    public Integer getTimeoutDuration() {
        return timeoutDuration;
    }
    
    public void setTimeoutDuration(Integer timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        // 更新过期时间
        if (executeTime != null) {
            this.expireTime = executeTime.plusMinutes(timeoutDuration);
        }
    }
    
    public String getCommandCode() {
        return commandCode;
    }
    
    public void setCommandCode(String commandCode) {
        this.commandCode = commandCode;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    /**
     * 检查指令是否已过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
    
    /**
     * 检查指令是否到了执行时间
     */
    public boolean isReadyToExecute() {
        return LocalDateTime.now().isAfter(executeTime) && CommandStatus.PENDING.equals(status);
    }
    
    @Override
    public String toString() {
        return "Command{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", executeTime=" + executeTime +
                ", satelliteId='" + satelliteId + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", expireTime=" + expireTime +
                ", timeoutDuration=" + timeoutDuration +
                ", commandCode='" + commandCode + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
