package com.example.springbootredis.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import java.time.LocalDateTime;

/**
 * 卫星指令实体类
 */
public class Command {
    
    /**
     * 指令ID
     */
    private String id;
    
    /**
     * 指令内容
     */
    private String content;
    
    /**
     * 执行时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;
    
    /**
     * 卫星ID
     */
    private String satelliteId;
    
    /**
     * 指令状态：PENDING(待执行), EXECUTING(执行中), COMPLETED(已完成), EXPIRED(已过期)
     */
    private String status;
    
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 过期时间（执行时间 + 30分钟）
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
    
    // 构造函数
    public Command() {
        this.createTime = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    public Command(String id, String content, LocalDateTime executeTime, String satelliteId) {
        this();
        this.id = id;
        this.content = content;
        this.executeTime = executeTime;
        this.satelliteId = satelliteId;
        // 设置过期时间为执行时间后30分钟
        this.expireTime = executeTime.plusMinutes(30);
    }
    
    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
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
        this.expireTime = executeTime.plusMinutes(30);
    }
    
    public String getSatelliteId() {
        return satelliteId;
    }
    
    public void setSatelliteId(String satelliteId) {
        this.satelliteId = satelliteId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
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
        return LocalDateTime.now().isAfter(executeTime) && "PENDING".equals(status);
    }
    
    @Override
    public String toString() {
        return "Command{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", executeTime=" + executeTime +
                ", satelliteId='" + satelliteId + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                ", expireTime=" + expireTime +
                '}';
    }
}
