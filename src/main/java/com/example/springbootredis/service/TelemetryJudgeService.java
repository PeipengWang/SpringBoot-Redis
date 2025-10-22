package com.example.springbootredis.service;

import com.example.springbootredis.entity.Command;
import com.example.springbootredis.entity.CommandTelemetryRelation;
import com.example.springbootredis.entity.TelemetryData;
import com.example.springbootredis.entity.TelemetryParam;
import com.example.springbootredis.entity.enums.CommandStatus;
import com.example.springbootredis.entity.enums.JudgeOperator;
import com.example.springbootredis.repository.CommandTelemetryRelationRepository;
import com.example.springbootredis.repository.TelemetryDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 遥测参数判断服务
 */
@Service
public class TelemetryJudgeService {
    
    @Autowired
    private CommandTelemetryRelationRepository relationRepository;
    
    @Autowired
    private TelemetryDataRepository telemetryDataRepository;
    
    @Autowired
    private FormulaEvaluationService formulaEvaluationService;
    
    /**
     * 判断单个遥测参数是否满足要求（支持简单比较和复杂公式）
     * 
     * @param actualValue 实际值
     * @param expectedValue 期望值
     * @param operator 判断操作符
     * @param paramType 参数类型
     * @return 是否满足要求
     */
    public boolean judgeParameter(String actualValue, String expectedValue, JudgeOperator operator, String paramType) {
        try {
            return formulaEvaluationService.evaluateSimpleExpression(
                actualValue, expectedValue, operator.getSymbol(), paramType);
        } catch (Exception e) {
            System.err.println("参数判断异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 判断单个遥测参数是否满足要求（使用复杂公式）
     * 
     * @param relation 指令-遥测参数关联关系
     * @param telemetryData 遥测数据上下文
     * @return 是否满足要求
     */
    public boolean judgeParameterWithFormula(CommandTelemetryRelation relation, Map<String, Object> telemetryData) {
        try {
            if ("COMPLEX".equals(relation.getFormulaType()) && relation.getFormulaExpression() != null) {
                // 使用复杂公式计算
                Map<String, Object> context = formulaEvaluationService.createComplexContext(telemetryData);
                return formulaEvaluationService.evaluateComplexExpression(relation.getFormulaExpression(), context);
            } else {
                // 使用简单比较
                TelemetryParam param = relation.getTelemetryParam();
                if (param != null) {
                    return judgeParameter(
                        telemetryData.get(param.getParamCode()).toString(),
                        relation.getExpectedValue(),
                        relation.getJudgeOperator(),
                        param.getParamType()
                    );
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("公式参数判断异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查指令的所有遥测参数是否都满足
     * 
     * @param commandId 指令ID
     * @return 是否所有必须参数都满足
     */
    public boolean checkCommandCompletion(Long commandId) {
        List<CommandTelemetryRelation> relations = relationRepository.findRequiredByCommandId(commandId);
        
        if (relations.isEmpty()) {
            return true; // 没有必须满足的参数，认为已完成
        }
        
        for (CommandTelemetryRelation relation : relations) {
            TelemetryParam param = relation.getTelemetryParam();
            if (param == null) {
                continue;
            }
            
            // 获取该参数的最新遥测数据
            TelemetryData latestData = telemetryDataRepository
                .findLatestByCommandIdAndParamCode(commandId, param.getParamCode())
                .orElse(null);
            
            if (latestData == null || latestData.getJudgeResult() == null || !latestData.getJudgeResult()) {
                return false; // 有必须参数未满足
            }
        }
        
        return true;
    }
    
    /**
     * 计算指令完成度
     * 
     * @param commandId 指令ID
     * @return 完成度（0-100）
     */
    public int calculateCommandProgress(Long commandId) {
        List<CommandTelemetryRelation> allRelations = relationRepository.findByCommandIdWithTelemetryParam(commandId);
        
        if (allRelations.isEmpty()) {
            return 100; // 没有参数，认为100%完成
        }
        
        int totalWeight = 0;
        int satisfiedWeight = 0;
        
        for (CommandTelemetryRelation relation : allRelations) {
            TelemetryParam param = relation.getTelemetryParam();
            if (param == null) {
                continue;
            }
            
            totalWeight += relation.getWeight();
            
            // 获取该参数的最新遥测数据
            TelemetryData latestData = telemetryDataRepository
                .findLatestByCommandIdAndParamCode(commandId, param.getParamCode())
                .orElse(null);
            
            if (latestData != null && latestData.getJudgeResult() != null && latestData.getJudgeResult()) {
                satisfiedWeight += relation.getWeight();
            }
        }
        
        if (totalWeight == 0) {
            return 100;
        }
        
        return (int) Math.round((double) satisfiedWeight / totalWeight * 100);
    }
    
    /**
     * 处理遥测数据并判断（支持复杂公式）
     * 
     * @param commandId 指令ID
     * @param paramCode 参数代码
     * @param actualValue 实际值
     * @param rawMessage 原始消息
     * @return 判断结果
     */
    @Transactional
    public boolean processTelemetryData(Long commandId, String paramCode, String actualValue, String rawMessage) {
        // 查找关联关系
        List<CommandTelemetryRelation> relations = relationRepository.findByCommandIdWithTelemetryParam(commandId);
        CommandTelemetryRelation relation = relations.stream()
            .filter(r -> r.getTelemetryParam() != null && paramCode.equals(r.getTelemetryParam().getParamCode()))
            .findFirst()
            .orElse(null);
        
        if (relation == null) {
            System.out.println("未找到指令 " + commandId + " 与参数 " + paramCode + " 的关联关系");
            return false;
        }
        
        TelemetryParam param = relation.getTelemetryParam();
        
        // 创建遥测数据上下文
        Map<String, Object> telemetryData = new HashMap<>();
        telemetryData.put(paramCode, actualValue);
        
        // 获取该指令的所有历史遥测数据，用于复杂公式计算
        List<TelemetryData> historicalData = telemetryDataRepository.findByCommandIdOrderByReceiveTimeDesc(commandId);
        for (TelemetryData data : historicalData) {
            telemetryData.put(data.getParamCode(), data.getActualValue());
        }
        
        // 判断参数是否满足要求
        boolean judgeResult = judgeParameterWithFormula(relation, telemetryData);
        
        // 保存遥测数据
        TelemetryData telemetryDataEntity = new TelemetryData(commandId, paramCode, actualValue);
        telemetryDataEntity.setJudgeResult(judgeResult);
        telemetryDataEntity.setRawMessage(rawMessage);
        telemetryDataRepository.save(telemetryDataEntity);
        
        System.out.println(String.format("遥测参数判断: 指令ID=%d, 参数=%s, 实际值=%s, 公式类型=%s, 结果=%s",
            commandId, paramCode, actualValue, relation.getFormulaType(), judgeResult ? "满足" : "不满足"));
        
        return judgeResult;
    }
    
    /**
     * 处理遥测数据并判断（兼容原有方法）
     * 
     * @param commandId 指令ID
     * @param paramCode 参数代码
     * @param actualValue 实际值
     * @param rawMessage 原始消息
     * @return 判断结果
     */
    @Transactional
    public boolean processTelemetryDataSimple(Long commandId, String paramCode, String actualValue, String rawMessage) {
        // 查找关联关系
        List<CommandTelemetryRelation> relations = relationRepository.findByCommandIdWithTelemetryParam(commandId);
        CommandTelemetryRelation relation = relations.stream()
            .filter(r -> r.getTelemetryParam() != null && paramCode.equals(r.getTelemetryParam().getParamCode()))
            .findFirst()
            .orElse(null);
        
        if (relation == null) {
            System.out.println("未找到指令 " + commandId + " 与参数 " + paramCode + " 的关联关系");
            return false;
        }
        
        TelemetryParam param = relation.getTelemetryParam();
        
        // 判断参数是否满足要求
        boolean judgeResult = judgeParameter(
            actualValue, 
            relation.getExpectedValue(), 
            relation.getJudgeOperator(),
            param.getParamType()
        );
        
        // 保存遥测数据
        TelemetryData telemetryData = new TelemetryData(commandId, paramCode, actualValue);
        telemetryData.setJudgeResult(judgeResult);
        telemetryData.setRawMessage(rawMessage);
        telemetryDataRepository.save(telemetryData);
        
        System.out.println(String.format("遥测参数判断: 指令ID=%d, 参数=%s, 实际值=%s, 期望值=%s, 操作符=%s, 结果=%s",
            commandId, paramCode, actualValue, relation.getExpectedValue(), 
            relation.getJudgeOperator(), judgeResult ? "满足" : "不满足"));
        
        return judgeResult;
    }
    
    /**
     * 获取指令的遥测参数统计信息
     * 
     * @param commandId 指令ID
     * @return 统计信息
     */
    public Map<String, Object> getCommandTelemetryStats(Long commandId) {
        List<CommandTelemetryRelation> relations = relationRepository.findByCommandIdWithTelemetryParam(commandId);
        
        int totalParams = relations.size();
        int requiredParams = (int) relations.stream().filter(CommandTelemetryRelation::getRequired).count();
        int satisfiedParams = 0;
        int unsatisfiedParams = 0;
        
        for (CommandTelemetryRelation relation : relations) {
            TelemetryParam param = relation.getTelemetryParam();
            if (param == null) {
                continue;
            }
            
            TelemetryData latestData = telemetryDataRepository
                .findLatestByCommandIdAndParamCode(commandId, param.getParamCode())
                .orElse(null);
            
            if (latestData != null && latestData.getJudgeResult() != null) {
                if (latestData.getJudgeResult()) {
                    satisfiedParams++;
                } else {
                    unsatisfiedParams++;
                }
            }
        }
        
        return Map.of(
            "totalParams", totalParams,
            "requiredParams", requiredParams,
            "satisfiedParams", satisfiedParams,
            "unsatisfiedParams", unsatisfiedParams,
            "progress", calculateCommandProgress(commandId)
        );
    }
}
