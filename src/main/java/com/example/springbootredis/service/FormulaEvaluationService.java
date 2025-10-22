package com.example.springbootredis.service;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 公式计算服务
 * 使用阿里巴巴Aviator框架进行复杂公式计算
 */
@Service
public class FormulaEvaluationService {
    
    /**
     * 计算简单表达式（兼容原有逻辑）
     * 
     * @param actualValue 实际值
     * @param expectedValue 期望值
     * @param operator 操作符
     * @param paramType 参数类型
     * @return 计算结果
     */
    public boolean evaluateSimpleExpression(String actualValue, String expectedValue, 
                                          String operator, String paramType) {
        try {
            // 构建简单表达式
            String expression = buildSimpleExpression(actualValue, expectedValue, operator, paramType);
            return evaluateExpression(expression, createSimpleContext(actualValue, expectedValue, paramType));
        } catch (Exception e) {
            System.err.println("简单表达式计算失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 计算复杂公式表达式
     * 
     * @param formulaExpression 公式表达式
     * @param context 上下文变量
     * @return 计算结果
     */
    public boolean evaluateComplexExpression(String formulaExpression, Map<String, Object> context) {
        try {
            return evaluateExpression(formulaExpression, context);
        } catch (Exception e) {
            System.err.println("复杂公式计算失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 计算表达式并返回布尔结果
     * 
     * @param expression 表达式
     * @param context 上下文
     * @return 布尔结果
     */
    private boolean evaluateExpression(String expression, Map<String, Object> context) {
        try {
            // 编译表达式
            Expression compiledExp = AviatorEvaluator.compile(expression);
            
            // 执行表达式
            Object result = compiledExp.execute(context);
            
            // 转换为布尔值
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else if (result instanceof Number) {
                return ((Number) result).doubleValue() != 0;
            } else if (result instanceof String) {
                return Boolean.parseBoolean((String) result);
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("表达式计算异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 构建简单表达式
     */
    private String buildSimpleExpression(String actualValue, String expectedValue, 
                                       String operator, String paramType) {
        String actualVar = "actual";
        String expectedVar = "expected";
        
        switch (operator) {
            case "==":
                return actualVar + " == " + expectedVar;
            case "!=":
                return actualVar + " != " + expectedVar;
            case ">":
                return actualVar + " > " + expectedVar;
            case "<":
                return actualVar + " < " + expectedVar;
            case ">=":
                return actualVar + " >= " + expectedVar;
            case "<=":
                return actualVar + " <= " + expectedVar;
            default:
                throw new IllegalArgumentException("不支持的操作符: " + operator);
        }
    }
    
    /**
     * 创建简单表达式的上下文
     */
    private Map<String, Object> createSimpleContext(String actualValue, String expectedValue, String paramType) {
        Map<String, Object> context = new HashMap<>();
        
        switch (paramType.toUpperCase()) {
            case "NUMBER":
                context.put("actual", new BigDecimal(actualValue));
                context.put("expected", new BigDecimal(expectedValue));
                break;
            case "STRING":
                context.put("actual", actualValue);
                context.put("expected", expectedValue);
                break;
            case "BOOLEAN":
                context.put("actual", Boolean.parseBoolean(actualValue));
                context.put("expected", Boolean.parseBoolean(expectedValue));
                break;
            default:
                // 默认按字符串处理
                context.put("actual", actualValue);
                context.put("expected", expectedValue);
                break;
        }
        
        return context;
    }
    
    /**
     * 创建复杂公式的上下文
     * 
     * @param telemetryData 遥测数据
     * @return 上下文变量
     */
    public Map<String, Object> createComplexContext(Map<String, Object> telemetryData) {
        Map<String, Object> context = new HashMap<>();
        
        // 添加遥测数据到上下文
        for (Map.Entry<String, Object> entry : telemetryData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // 尝试转换为数字
            if (value instanceof String) {
                try {
                    BigDecimal numValue = new BigDecimal((String) value);
                    context.put(key, numValue);
                } catch (NumberFormatException e) {
                    // 不是数字，保持字符串
                    context.put(key, value);
                }
            } else {
                context.put(key, value);
            }
        }
        
        // 添加常用函数
        addCommonFunctions(context);
        
        return context;
    }
    
    /**
     * 添加常用函数到上下文
     */
    private void addCommonFunctions(Map<String, Object> context) {
        // 添加数学函数
        context.put("abs", Math::abs);
        context.put("max", Math::max);
        context.put("min", Math::min);
        context.put("sqrt", Math::sqrt);
        context.put("pow", Math::pow);
        context.put("sin", Math::sin);
        context.put("cos", Math::cos);
        context.put("tan", Math::tan);
        context.put("log", Math::log);
        context.put("log10", Math::log10);
        
        // 添加时间函数
        context.put("now", System::currentTimeMillis);
        
        // 添加字符串函数
        context.put("length", (str) -> str != null ? str.toString().length() : 0);
        context.put("upper", (str) -> str != null ? str.toString().toUpperCase() : "");
        context.put("lower", (str) -> str != null ? str.toString().toLowerCase() : "");
    }
    
    /**
     * 验证公式表达式是否有效
     * 
     * @param expression 表达式
     * @return 是否有效
     */
    public boolean validateExpression(String expression) {
        try {
            AviatorEvaluator.compile(expression);
            return true;
        } catch (Exception e) {
            System.err.println("公式表达式验证失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取表达式中使用的变量
     * 
     * @param expression 表达式
     * @return 变量列表
     */
    public String[] getExpressionVariables(String expression) {
        try {
            Expression compiledExp = AviatorEvaluator.compile(expression);
            return compiledExp.getVariableNames().toArray(new String[0]);
        } catch (Exception e) {
            System.err.println("获取表达式变量失败: " + e.getMessage());
            return new String[0];
        }
    }
    
    /**
     * 计算表达式并返回数值结果
     * 
     * @param expression 表达式
     * @param context 上下文
     * @return 数值结果
     */
    public Double evaluateNumericExpression(String expression, Map<String, Object> context) {
        try {
            Expression compiledExp = AviatorEvaluator.compile(expression);
            Object result = compiledExp.execute(context);
            
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("数值表达式计算失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 预编译表达式（提高性能）
     * 
     * @param expression 表达式
     * @return 编译后的表达式
     */
    public Expression preCompileExpression(String expression) {
        try {
            return AviatorEvaluator.compile(expression);
        } catch (Exception e) {
            System.err.println("表达式预编译失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 使用预编译的表达式计算结果
     * 
     * @param compiledExp 预编译的表达式
     * @param context 上下文
     * @return 计算结果
     */
    public boolean evaluatePreCompiledExpression(Expression compiledExp, Map<String, Object> context) {
        try {
            Object result = compiledExp.execute(context);
            
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else if (result instanceof Number) {
                return ((Number) result).doubleValue() != 0;
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("预编译表达式计算失败: " + e.getMessage());
            return false;
        }
    }
}
