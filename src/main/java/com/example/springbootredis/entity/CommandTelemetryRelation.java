package com.example.springbootredis.entity;

import com.example.springbootredis.entity.enums.JudgeOperator;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 指令-遥测参数关联实体类
 */
@Entity
@Table(name = "tb_command_telemetry_relation")
public class CommandTelemetryRelation {
    
    /**
     * 关联ID
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
     * 遥测参数ID
     */
    @Column(name = "telemetry_param_id", nullable = false)
    private Long telemetryParamId;
    
    /**
     * 判断操作符
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "judge_operator", nullable = false, length = 10)
    private JudgeOperator judgeOperator;
    
    /**
     * 期望值
     */
    @Column(name = "expected_value", nullable = false, length = 100)
    private String expectedValue;
    
    /**
     * 复杂公式表达式（替代简单的expectedValue和judgeOperator）
     */
    @Column(name = "formula_expression", length = 1000)
    private String formulaExpression;
    
    /**
     * 公式类型（SIMPLE: 简单比较, COMPLEX: 复杂公式）
     */
    @Column(name = "formula_type", length = 20)
    private String formulaType = "SIMPLE";
    
    /**
     * 是否必须满足（true：必须满足才能完成指令，false：可选参数）
     */
    @Column(name = "required", nullable = false)
    private Boolean required = true;
    
    /**
     * 参数权重（用于计算完成度）
     */
    @Column(name = "weight", nullable = false)
    private Integer weight = 1;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 关联对象
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_id", insertable = false, updatable = false)
    private Command command;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "telemetry_param_id", insertable = false, updatable = false)
    private TelemetryParam telemetryParam;
    
    // 构造函数
    public CommandTelemetryRelation() {
        this.createTime = LocalDateTime.now();
    }
    
    public CommandTelemetryRelation(Long commandId, Long telemetryParamId, JudgeOperator judgeOperator, String expectedValue) {
        this();
        this.commandId = commandId;
        this.telemetryParamId = telemetryParamId;
        this.judgeOperator = judgeOperator;
        this.expectedValue = expectedValue;
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
    
    public Long getTelemetryParamId() {
        return telemetryParamId;
    }
    
    public void setTelemetryParamId(Long telemetryParamId) {
        this.telemetryParamId = telemetryParamId;
    }
    
    public JudgeOperator getJudgeOperator() {
        return judgeOperator;
    }
    
    public void setJudgeOperator(JudgeOperator judgeOperator) {
        this.judgeOperator = judgeOperator;
    }
    
    public String getExpectedValue() {
        return expectedValue;
    }
    
    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }
    
    public String getFormulaExpression() {
        return formulaExpression;
    }
    
    public void setFormulaExpression(String formulaExpression) {
        this.formulaExpression = formulaExpression;
    }
    
    public String getFormulaType() {
        return formulaType;
    }
    
    public void setFormulaType(String formulaType) {
        this.formulaType = formulaType;
    }
    
    public Boolean getRequired() {
        return required;
    }
    
    public void setRequired(Boolean required) {
        this.required = required;
    }
    
    public Integer getWeight() {
        return weight;
    }
    
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public Command getCommand() {
        return command;
    }
    
    public void setCommand(Command command) {
        this.command = command;
    }
    
    public TelemetryParam getTelemetryParam() {
        return telemetryParam;
    }
    
    public void setTelemetryParam(TelemetryParam telemetryParam) {
        this.telemetryParam = telemetryParam;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "CommandTelemetryRelation{" +
                "id=" + id +
                ", commandId=" + commandId +
                ", telemetryParamId=" + telemetryParamId +
                ", judgeOperator=" + judgeOperator +
                ", expectedValue='" + expectedValue + '\'' +
                ", formulaExpression='" + formulaExpression + '\'' +
                ", formulaType='" + formulaType + '\'' +
                ", required=" + required +
                ", weight=" + weight +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
