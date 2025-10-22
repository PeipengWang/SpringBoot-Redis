# 卫星指令监控系统

## 系统概述

本系统实现了卫星指令的全生命周期状态管理，包括数据库持久化、Kafka遥测数据消费、状态实时判断和WebSocket实时推送等功能。

## 技术架构

- **后端框架**: Spring Boot 3.5.6
- **持久化层**: MySQL + Spring Data JPA
- **缓存层**: Redis
- **消息队列**: Kafka
- **实时通信**: WebSocket + STOMP
- **Java版本**: 17

## 核心功能

### 1. 指令状态管理
- **未执行 (PENDING)**: 指令创建后的初始状态
- **已编制 (PREPARED)**: 指令已准备就绪
- **执行中 (EXECUTING)**: 指令正在执行
- **已完成 (COMPLETED)**: 所有遥测参数满足要求
- **执行失败 (FAILED)**: 超时或参数不满足

### 2. 遥测参数判断
- 支持数值、字符串、布尔类型的参数判断
- 支持多种比较操作符：`==`, `!=`, `>`, `<`, `>=`, `<=`
- **支持复杂公式计算**：使用阿里巴巴Aviator框架
- 实时判断并存储结果

### 3. Kafka集成
- 消费遥测数据消息
- 自动触发指令状态转换
- 实时参数判断和状态更新

### 4. WebSocket实时推送
- 遥测参数判定结果推送
- 指令状态变更通知
- 执行进度实时更新
- 系统状态统计推送

## 数据库设计

### 表结构

1. **tb_command** - 指令表
   - id, content, satellite_id, execute_time, status, create_time, expire_time, timeout_duration, command_code, remark

2. **tb_telemetry_param** - 遥测参数表
   - id, param_name, param_code, description, unit, param_type, enabled, **support_formula, formula_expression, formula_description**, create_time, update_time

3. **tb_command_telemetry_relation** - 指令-遥测参数关联表
   - id, command_id, telemetry_param_id, judge_operator, expected_value, **formula_expression, formula_type**, required, weight, create_time, update_time

4. **tb_telemetry_data** - 遥测数据记录表
   - id, command_id, param_code, actual_value, receive_time, judge_result, judge_time, data_source, raw_message

## 核心服务

### CommandManagementService
- 指令状态管理
- 数据库同步
- 超时检查
- 统计信息

### TelemetryJudgeService
- 遥测参数判断（支持简单比较和复杂公式）
- 指令完成度计算
- 进度统计

### FormulaEvaluationService
- **Aviator公式计算引擎**
- 支持复杂数学表达式
- 公式验证和变量提取
- 预编译表达式优化性能

### CommandStatusWebSocketService
- WebSocket消息推送
- 实时状态通知
- 进度更新

### TelemetryKafkaConsumer
- Kafka消息消费
- 状态自动转换
- 实时判断触发

### CommandStatusMonitorService
- 定时任务管理
- 状态监控
- 数据清理

## 配置说明

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/satellite_command_db
    username: root
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: satellite-telemetry-group
```

### WebSocket端点
- 连接端点: `ws://localhost:8080/ws`
- 消息代理: `/topic`, `/queue`
- 应用前缀: `/app`

### Kafka Topic
- 遥测数据Topic: `satellite-telemetry`

## 消息格式

### Kafka遥测消息格式
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

### WebSocket推送消息格式

#### 遥测判定结果
```json
{
  "type": "TELEMETRY_JUDGE_RESULT",
  "commandId": 1,
  "paramCode": "TEMP_001",
  "paramName": "温度",
  "expectedValue": "80",
  "actualValue": "85",
  "judgeResult": true,
  "judgeTime": "2023-12-01T10:30:00"
}
```

#### 状态变更通知
```json
{
  "type": "COMMAND_STATUS_CHANGE",
  "commandId": 1,
  "oldStatus": "EXECUTING",
  "newStatus": "COMPLETED",
  "reason": "所有遥测参数满足要求",
  "changeTime": "2023-12-01T10:30:00"
}
```

## 定时任务

- **每30秒**: 同步未执行和已编制的指令
- **每1分钟**: 检查执行中指令是否超时
- **每5分钟**: 推送系统状态统计
- **每天凌晨2点**: 清理过期数据

## 使用说明

### 1. 启动系统
```bash
mvn spring-boot:run
```

### 2. 数据库初始化
系统启动后会自动创建表结构，需要手动导入Excel中的基础数据：
- 指令数据 → `tb_command`
- 遥测参数 → `tb_telemetry_param`
- 关联关系 → `tb_command_telemetry_relation`

### 3. Kafka消息发送
向 `satellite-telemetry` topic发送遥测数据消息，系统会自动：
- 解析消息内容
- 查找对应指令
- 更新指令状态
- 判断参数是否满足
- 推送实时结果

### 4. WebSocket连接
前端可以连接到WebSocket端点实时接收：
- 指令状态变更
- 遥测参数判定结果
- 执行进度更新
- 系统统计信息

## 测试

运行集成测试：
```bash
mvn test
```

测试覆盖：
- 指令生命周期管理
- 遥测参数判断逻辑
- 状态转换流程
- WebSocket推送功能

## 复杂公式支持

### Aviator框架特性
- **数学运算**: `+`, `-`, `*`, `/`, `%`, `^`（幂运算）
- **比较操作**: `>`, `<`, `>=`, `<=`, `==`, `!=`
- **逻辑操作**: `&&`, `||`, `!`
- **数学函数**: `abs()`, `max()`, `min()`, `sqrt()`, `pow()`, `sin()`, `cos()`, `tan()`, `log()`, `log10()`
- **字符串函数**: `length()`, `upper()`, `lower()`
- **时间函数**: `now()`

### 公式示例

#### 简单比较
```javascript
temperature > 80
pressure < 1000000
status == 'NORMAL'
```

#### 复杂数学表达式
```javascript
(temperature + pressure) / 2 > 500000
sqrt(temperature * pressure) > 1000
max(temp1, temp2) - min(temp1, temp2) < 10
```

#### 多条件组合
```javascript
temperature > 80 && pressure < 1000000 && status == 'NORMAL'
(temperature > 80 || pressure < 500000) && mode == 'AUTO'
```

#### 使用历史数据
```javascript
temperature > avg(temperature_history) + 2 * std(temperature_history)
```

### 公式配置

#### 在遥测参数中配置公式
```sql
UPDATE tb_telemetry_param 
SET support_formula = true, 
    formula_expression = 'temperature > 80 && temperature < 100',
    formula_description = '温度范围检查'
WHERE param_code = 'TEMP_001';
```

#### 在指令关联中配置公式
```sql
UPDATE tb_command_telemetry_relation 
SET formula_type = 'COMPLEX',
    formula_expression = '(temp1 + temp2) / 2 > threshold'
WHERE id = 1;
```

### 性能优化
- **预编译表达式**: 提高重复计算性能
- **上下文缓存**: 避免重复创建计算上下文
- **批量计算**: 支持批量公式计算

## 扩展功能

### 2. 性能优化
- Redis缓存优化
- 数据库查询优化
- Kafka消费性能调优
- WebSocket连接池管理

### 3. 监控告警
- 指令执行异常告警
- 系统性能监控
- 业务指标统计
- 日志分析

## 注意事项

1. **数据库配置**: 确保MySQL服务正常运行，数据库已创建
2. **Kafka配置**: 确保Kafka服务正常运行，topic已创建
3. **Redis配置**: 确保Redis服务正常运行
4. **时区设置**: 系统使用UTC时区，注意时间转换
5. **并发处理**: 考虑高并发场景下的状态更新一致性
6. **数据清理**: 定期清理过期的遥测数据，避免数据量过大

## 联系方式

如有问题或建议，请联系开发团队。
