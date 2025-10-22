package com.example.springbootredis.entity.enums;

/**
 * 判断操作符枚举
 */
public enum JudgeOperator {
    /**
     * 等于
     */
    EQUALS("==", "等于"),
    
    /**
     * 不等于
     */
    NOT_EQUALS("!=", "不等于"),
    
    /**
     * 大于
     */
    GREATER_THAN(">", "大于"),
    
    /**
     * 小于
     */
    LESS_THAN("<", "小于"),
    
    /**
     * 大于等于
     */
    GREATER_THAN_OR_EQUALS(">=", "大于等于"),
    
    /**
     * 小于等于
     */
    LESS_THAN_OR_EQUALS("<=", "小于等于");
    
    private final String symbol;
    private final String description;
    
    JudgeOperator(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据符号获取操作符
     */
    public static JudgeOperator fromSymbol(String symbol) {
        for (JudgeOperator operator : values()) {
            if (operator.symbol.equals(symbol)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("Unknown operator symbol: " + symbol);
    }
    
    @Override
    public String toString() {
        return symbol;
    }
}
