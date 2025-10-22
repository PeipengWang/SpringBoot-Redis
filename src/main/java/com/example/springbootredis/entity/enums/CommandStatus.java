package com.example.springbootredis.entity.enums;

/**
 * 指令状态枚举
 */
public enum CommandStatus {
    /**
     * 未执行
     */
    PENDING("未执行"),
    
    /**
     * 已编制
     */
    PREPARED("已编制"),
    
    /**
     * 执行中
     */
    EXECUTING("执行中"),
    
    /**
     * 已完成
     */
    COMPLETED("已完成"),
    
    /**
     * 执行失败
     */
    FAILED("执行失败");
    
    private final String description;
    
    CommandStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
