package com.example.springbootredis;

import com.example.springbootredis.entity.Command;
import com.example.springbootredis.entity.TelemetryParam;
import com.example.springbootredis.entity.CommandTelemetryRelation;
import com.example.springbootredis.entity.enums.CommandStatus;
import com.example.springbootredis.entity.enums.JudgeOperator;
import com.example.springbootredis.repository.CommandRepository;
import com.example.springbootredis.repository.TelemetryParamRepository;
import com.example.springbootredis.repository.CommandTelemetryRelationRepository;
import com.example.springbootredis.service.CommandManagementService;
import com.example.springbootredis.service.TelemetryJudgeService;
import com.example.springbootredis.service.FormulaEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 卫星指令监控系统集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
class SatelliteCommandMonitoringTest {

    @Autowired
    private CommandRepository commandRepository;
    
    @Autowired
    private TelemetryParamRepository telemetryParamRepository;
    
    @Autowired
    private CommandTelemetryRelationRepository relationRepository;
    
    @Autowired
    private CommandManagementService commandManagementService;
    
    @Autowired
    private TelemetryJudgeService telemetryJudgeService;
    
    @Autowired
    private FormulaEvaluationService formulaEvaluationService;

    @Test
    void testCommandLifecycle() {
        System.out.println("=== 开始测试指令生命周期 ===");
        
        // 1. 创建遥测参数
        TelemetryParam temperatureParam = new TelemetryParam("温度", "TEMP_001");
        temperatureParam.setParamType("NUMBER");
        temperatureParam.setUnit("°C");
        telemetryParamRepository.save(temperatureParam);
        
        TelemetryParam pressureParam = new TelemetryParam("压力", "PRESS_001");
        pressureParam.setParamType("NUMBER");
        pressureParam.setUnit("Pa");
        telemetryParamRepository.save(pressureParam);
        
        // 2. 创建指令
        LocalDateTime executeTime = LocalDateTime.now().plusMinutes(5);
        Command command = new Command("启动引擎", executeTime, "satellite_001");
        command.setCommandCode("CMD_ENGINE_START");
        command.setTimeoutDuration(10); // 10分钟超时
        commandRepository.save(command);
        
        // 3. 创建指令-遥测参数关联
        CommandTelemetryRelation tempRelation = new CommandTelemetryRelation(
            command.getId(), temperatureParam.getId(), JudgeOperator.GREATER_THAN, "80");
        relationRepository.save(tempRelation);
        
        CommandTelemetryRelation pressRelation = new CommandTelemetryRelation(
            command.getId(), pressureParam.getId(), JudgeOperator.LESS_THAN, "1000000");
        relationRepository.save(pressRelation);
        
        // 4. 验证指令状态
        assertEquals(CommandStatus.PENDING, command.getStatus());
        assertFalse(command.isExpired());
        assertFalse(command.isReadyToExecute());
        
        // 5. 测试状态更新
        boolean updated = commandManagementService.updateCommandStatus(
            command.getId(), CommandStatus.PREPARED, "指令已编制");
        assertTrue(updated);
        
        Command updatedCommand = commandRepository.findById(command.getId()).orElse(null);
        assertNotNull(updatedCommand);
        assertEquals(CommandStatus.PREPARED, updatedCommand.getStatus());
        
        // 6. 测试遥测参数判断
        boolean tempResult = telemetryJudgeService.processTelemetryData(
            command.getId(), "TEMP_001", "85", "Kafka消息");
        assertTrue(tempResult); // 85 > 80，应该满足
        
        boolean pressResult = telemetryJudgeService.processTelemetryData(
            command.getId(), "PRESS_001", "900000", "Kafka消息");
        assertTrue(pressResult); // 900000 < 1000000，应该满足
        
        // 7. 测试指令完成检查
        boolean isCompleted = telemetryJudgeService.checkCommandCompletion(command.getId());
        assertTrue(isCompleted); // 所有必须参数都满足
        
        // 8. 测试进度计算
        int progress = telemetryJudgeService.calculateCommandProgress(command.getId());
        assertEquals(100, progress); // 100%完成
        
        // 9. 测试统计信息
        CommandManagementService.CommandStats stats = commandManagementService.getCommandStats();
        assertTrue(stats.getTotalCommands() > 0);
        
        System.out.println("=== 指令生命周期测试通过 ===");
    }
    
    @Test
    void testComplexFormulaEvaluation() {
        System.out.println("=== 开始测试复杂公式计算 ===");
        
        // 测试简单数学表达式
        Map<String, Object> context1 = new HashMap<>();
        context1.put("temperature", 85);
        context1.put("pressure", 900000);
        context1.put("threshold", 80);
        
        boolean result1 = formulaEvaluationService.evaluateComplexExpression(
            "temperature > threshold", context1);
        assertTrue(result1, "温度85应该大于阈值80");
        
        // 测试复杂数学表达式
        Map<String, Object> context2 = new HashMap<>();
        context2.put("temp1", 85);
        context2.put("temp2", 90);
        context2.put("pressure", 900000);
        context2.put("maxPressure", 1000000);
        
        boolean result2 = formulaEvaluationService.evaluateComplexExpression(
            "(temp1 + temp2) / 2 > 80 && pressure < maxPressure", context2);
        assertTrue(result2, "平均温度87.5应该大于80且压力小于最大值");
        
        // 测试数学函数
        Map<String, Object> context3 = new HashMap<>();
        context3.put("value", 100);
        context3.put("threshold", 10);
        
        boolean result3 = formulaEvaluationService.evaluateComplexExpression(
            "sqrt(value) > threshold", context3);
        assertTrue(result3, "sqrt(100) = 10应该等于阈值10");
        
        // 测试字符串表达式
        Map<String, Object> context4 = new HashMap<>();
        context4.put("status", "NORMAL");
        context4.put("mode", "AUTO");
        
        boolean result4 = formulaEvaluationService.evaluateComplexExpression(
            "status == 'NORMAL' && mode == 'AUTO'", context4);
        assertTrue(result4, "状态为NORMAL且模式为AUTO应该满足条件");
        
        System.out.println("=== 复杂公式计算测试通过 ===");
    }
    
    @Test
    void testFormulaWithTelemetryData() {
        System.out.println("=== 开始测试遥测数据公式计算 ===");
        
        // 创建遥测参数
        TelemetryParam tempParam = new TelemetryParam("温度", "TEMP_001");
        tempParam.setParamType("NUMBER");
        tempParam.setSupportFormula(true);
        tempParam.setFormulaExpression("temperature > 80");
        telemetryParamRepository.save(tempParam);
        
        TelemetryParam pressParam = new TelemetryParam("压力", "PRESS_001");
        pressParam.setParamType("NUMBER");
        telemetryParamRepository.save(pressParam);
        
        // 创建指令
        LocalDateTime executeTime = LocalDateTime.now().plusMinutes(5);
        Command command = new Command("复杂公式测试", executeTime, "satellite_formula_test");
        command.setCommandCode("CMD_FORMULA_TEST");
        commandRepository.save(command);
        
        // 创建简单关联（压力）
        CommandTelemetryRelation pressRelation = new CommandTelemetryRelation(
            command.getId(), pressParam.getId(), JudgeOperator.LESS_THAN, "1000000");
        pressRelation.setFormulaType("SIMPLE");
        relationRepository.save(pressRelation);
        
        // 创建复杂公式关联（温度）
        CommandTelemetryRelation tempRelation = new CommandTelemetryRelation(
            command.getId(), tempParam.getId(), JudgeOperator.GREATER_THAN, "80");
        tempRelation.setFormulaType("COMPLEX");
        tempRelation.setFormulaExpression("temperature > 80 && temperature < 100");
        relationRepository.save(tempRelation);
        
        // 测试简单公式处理
        boolean pressResult = telemetryJudgeService.processTelemetryDataSimple(
            command.getId(), "PRESS_001", "900000", "压力测试");
        assertTrue(pressResult, "压力900000应该小于1000000");
        
        // 测试复杂公式处理
        boolean tempResult = telemetryJudgeService.processTelemetryData(
            command.getId(), "TEMP_001", "85", "温度测试");
        assertTrue(tempResult, "温度85应该满足复杂公式条件");
        
        // 测试不满足复杂公式的情况
        boolean tempResult2 = telemetryJudgeService.processTelemetryData(
            command.getId(), "TEMP_001", "105", "温度测试");
        assertFalse(tempResult2, "温度105不应该满足复杂公式条件");
        
        System.out.println("=== 遥测数据公式计算测试通过 ===");
    }
    
    @Test
    void testFormulaValidation() {
        System.out.println("=== 开始测试公式验证 ===");
        
        // 测试有效公式
        assertTrue(formulaEvaluationService.validateExpression("temperature > 80"));
        assertTrue(formulaEvaluationService.validateExpression("(temp1 + temp2) / 2 > threshold"));
        assertTrue(formulaEvaluationService.validateExpression("sqrt(value) > 10"));
        
        // 测试无效公式
        assertFalse(formulaEvaluationService.validateExpression("temperature >"));
        assertFalse(formulaEvaluationService.validateExpression("invalid_function(value)"));
        assertFalse(formulaEvaluationService.validateExpression("temperature > 80 &&"));
        
        // 测试获取表达式变量
        String[] variables1 = formulaEvaluationService.getExpressionVariables("temperature > threshold");
        assertEquals(2, variables1.length);
        assertTrue(java.util.Arrays.asList(variables1).contains("temperature"));
        assertTrue(java.util.Arrays.asList(variables1).contains("threshold"));
        
        String[] variables2 = formulaEvaluationService.getExpressionVariables("(temp1 + temp2) / 2 > 80");
        assertEquals(2, variables2.length);
        assertTrue(java.util.Arrays.asList(variables2).contains("temp1"));
        assertTrue(java.util.Arrays.asList(variables2).contains("temp2"));
        
        System.out.println("=== 公式验证测试通过 ===");
    }
    
    @Test
    void testCommandStatusTransition() {
        System.out.println("=== 开始测试指令状态转换 ===");
        
        // 创建测试指令
        LocalDateTime executeTime = LocalDateTime.now().plusMinutes(5);
        Command command = new Command("测试指令", executeTime, "satellite_test");
        command.setCommandCode("CMD_TEST");
        commandRepository.save(command);
        
        // 测试状态转换
        assertEquals(CommandStatus.PENDING, command.getStatus());
        
        // PENDING -> PREPARED
        commandManagementService.updateCommandStatus(command.getId(), CommandStatus.PREPARED, "测试转换");
        Command updatedCommand = commandRepository.findById(command.getId()).orElse(null);
        assertEquals(CommandStatus.PREPARED, updatedCommand.getStatus());
        
        // PREPARED -> EXECUTING
        commandManagementService.updateCommandStatus(command.getId(), CommandStatus.EXECUTING, "开始执行");
        updatedCommand = commandRepository.findById(command.getId()).orElse(null);
        assertEquals(CommandStatus.EXECUTING, updatedCommand.getStatus());
        
        // EXECUTING -> COMPLETED
        commandManagementService.updateCommandStatus(command.getId(), CommandStatus.COMPLETED, "执行完成");
        updatedCommand = commandRepository.findById(command.getId()).orElse(null);
        assertEquals(CommandStatus.COMPLETED, updatedCommand.getStatus());
        
        System.out.println("=== 指令状态转换测试通过 ===");
    }
}
