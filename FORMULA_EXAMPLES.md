# 复杂公式配置示例

## 数据库配置示例

### 1. 遥测参数配置（支持复杂公式）

```sql
-- 创建支持复杂公式的遥测参数
INSERT INTO tb_telemetry_param (param_name, param_code, description, unit, param_type, enabled, support_formula, formula_expression, formula_description) VALUES
('温度', 'TEMP_001', '发动机温度', '°C', 'NUMBER', true, true, 'temperature > 80 && temperature < 100', '温度范围检查'),
('压力', 'PRESS_001', '系统压力', 'Pa', 'NUMBER', true, false, NULL, NULL),
('状态', 'STATUS_001', '系统状态', '', 'STRING', true, true, 'status == "NORMAL" || status == "STANDBY"', '状态检查'),
('综合指标', 'COMPOSITE_001', '综合性能指标', '', 'NUMBER', true, true, '(temperature + pressure/1000) / 2 > 50', '综合性能评估');
```

### 2. 指令-遥测参数关联配置

```sql
-- 简单比较关联
INSERT INTO tb_command_telemetry_relation (command_id, telemetry_param_id, judge_operator, expected_value, formula_type, required, weight) VALUES
(1, 2, 'LESS_THAN', '1000000', 'SIMPLE', true, 1);

-- 复杂公式关联
INSERT INTO tb_command_telemetry_relation (command_id, telemetry_param_id, judge_operator, expected_value, formula_expression, formula_type, required, weight) VALUES
(1, 1, 'GREATER_THAN', '80', 'temperature > 80 && temperature < 100', 'COMPLEX', true, 2),
(1, 3, 'EQUALS', 'NORMAL', 'status == "NORMAL" && mode == "AUTO"', 'COMPLEX', true, 1),
(1, 4, 'GREATER_THAN', '50', 'sqrt(temperature * pressure) > 1000', 'COMPLEX', false, 1);
```

## 公式表达式示例

### 1. 基础比较表达式
```javascript
// 数值比较
temperature > 80
pressure < 1000000
speed >= 5000

// 字符串比较
status == "NORMAL"
mode != "MANUAL"
level == "HIGH"
```

### 2. 数学运算表达式
```javascript
// 基本运算
(temperature + pressure) / 2 > 500000
temperature * 1.8 + 32 > 180  // 摄氏度转华氏度
pressure / 1000 > 100  // Pa转kPa

// 复杂数学表达式
sqrt(temperature * pressure) > 1000
pow(temperature, 2) + pow(pressure, 2) < 1000000000
abs(temperature - target_temp) < 5
```

### 3. 逻辑组合表达式
```javascript
// 与操作
temperature > 80 && pressure < 1000000 && status == "NORMAL"

// 或操作
temperature > 100 || pressure > 2000000 || status == "ERROR"

// 复杂逻辑
(temperature > 80 || pressure < 500000) && mode == "AUTO"
!(status == "ERROR") && (temperature > 70 && temperature < 90)
```

### 4. 数学函数表达式
```javascript
// 最大值最小值
max(temp1, temp2) > 80
min(temp1, temp2) < 100
max(temp1, temp2) - min(temp1, temp2) < 10

// 数学函数
sqrt(temperature * pressure) > 1000
pow(temperature, 2) > 6400
abs(temperature - target_temp) < 5
log(temperature) > 4
sin(angle) > 0.5
```

### 5. 字符串函数表达式
```javascript
// 字符串长度
length(status) > 3
length(mode) == 4

// 大小写转换
upper(status) == "NORMAL"
lower(mode) == "auto"
```

### 6. 时间相关表达式
```javascript
// 时间戳比较
now() - start_time > 300000  // 5分钟
timestamp > start_time + 60000  // 1分钟后
```

## Kafka消息示例

### 1. 简单遥测数据
```json
{
  "commandCode": "CMD_ENGINE_START",
  "paramCode": "TEMP_001",
  "paramName": "温度",
  "actualValue": "85",
  "expectedValue": "80",
  "timestamp": 1640995200000
}
```

### 2. 复杂遥测数据（多参数）
```json
{
  "commandCode": "CMD_ENGINE_START",
  "telemetryData": {
    "TEMP_001": "85",
    "PRESS_001": "900000",
    "STATUS_001": "NORMAL",
    "MODE_001": "AUTO"
  },
  "timestamp": 1640995200000
}
```

## 使用场景示例

### 1. 发动机启动检查
```javascript
// 温度检查
temperature > 80 && temperature < 100

// 压力检查
pressure > 500000 && pressure < 1500000

// 综合检查
(temperature > 80 && pressure > 500000) && status == "NORMAL"
```

### 2. 轨道调整检查
```javascript
// 速度检查
speed > 5000 && speed < 8000

// 高度检查
altitude > 300000 && altitude < 500000

// 综合轨道检查
sqrt(speed * altitude) > 1000000 && status == "STABLE"
```

### 3. 通信测试检查
```javascript
// 信号强度检查
signal_strength > -80 && signal_strength < -30

// 信噪比检查
snr > 10 && snr < 50

// 通信质量综合检查
(signal_strength > -80 && snr > 10) && connection_status == "ESTABLISHED"
```

## 性能优化建议

### 1. 表达式预编译
```java
// 预编译常用表达式
Expression compiledExp = formulaEvaluationService.preCompileExpression(
    "temperature > 80 && pressure < 1000000");

// 重复使用预编译表达式
boolean result = formulaEvaluationService.evaluatePreCompiledExpression(
    compiledExp, context);
```

### 2. 上下文优化
```java
// 创建优化的上下文
Map<String, Object> context = new HashMap<>();
context.put("temperature", new BigDecimal("85"));
context.put("pressure", new BigDecimal("900000"));
context.put("status", "NORMAL");

// 批量计算
boolean result = formulaEvaluationService.evaluateComplexExpression(
    "temperature > 80 && pressure < 1000000 && status == 'NORMAL'", context);
```

### 3. 缓存策略
```java
// 缓存计算结果
@Cacheable("formulaResults")
public boolean evaluateFormula(String expression, Map<String, Object> context) {
    return formulaEvaluationService.evaluateComplexExpression(expression, context);
}
```
