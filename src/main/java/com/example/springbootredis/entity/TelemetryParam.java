package com.example.springbootredis.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 遥测参数实体类
 */
@Entity
@Table(name = "tb_telemetry_param")
public class TelemetryParam {
    
    /**
     * 参数ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 参数名称
     */
    @Column(name = "param_name", nullable = false, length = 100)
    private String paramName;
    
    /**
     * 参数代码
     */
    @Column(name = "param_code", nullable = false, length = 50, unique = true)
    private String paramCode;
    
    /**
     * 参数描述
     */
    @Column(name = "description", length = 200)
    private String description;
    
    /**
     * 参数单位
     */
    @Column(name = "unit", length = 20)
    private String unit;
    
    /**
     * 参数类型（STRING, NUMBER, BOOLEAN等）
     */
    @Column(name = "param_type", length = 20)
    private String paramType = "NUMBER";
    
    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    /**
     * 是否支持复杂公式计算
     */
    @Column(name = "support_formula", nullable = false)
    private Boolean supportFormula = false;
    
    /**
     * 公式表达式（当supportFormula为true时使用）
     */
    @Column(name = "formula_expression", length = 1000)
    private String formulaExpression;
    
    /**
     * 公式描述
     */
    @Column(name = "formula_description", length = 200)
    private String formulaDescription;
    
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
    
    // 构造函数
    public TelemetryParam() {
        this.createTime = LocalDateTime.now();
    }
    
    public TelemetryParam(String paramName, String paramCode) {
        this();
        this.paramName = paramName;
        this.paramCode = paramCode;
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getParamName() {
        return paramName;
    }
    
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    
    public String getParamCode() {
        return paramCode;
    }
    
    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getParamType() {
        return paramType;
    }
    
    public void setParamType(String paramType) {
        this.paramType = paramType;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Boolean getSupportFormula() {
        return supportFormula;
    }
    
    public void setSupportFormula(Boolean supportFormula) {
        this.supportFormula = supportFormula;
    }
    
    public String getFormulaExpression() {
        return formulaExpression;
    }
    
    public void setFormulaExpression(String formulaExpression) {
        this.formulaExpression = formulaExpression;
    }
    
    public String getFormulaDescription() {
        return formulaDescription;
    }
    
    public void setFormulaDescription(String formulaDescription) {
        this.formulaDescription = formulaDescription;
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
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "TelemetryParam{" +
                "id=" + id +
                ", paramName='" + paramName + '\'' +
                ", paramCode='" + paramCode + '\'' +
                ", description='" + description + '\'' +
                ", unit='" + unit + '\'' +
                ", paramType='" + paramType + '\'' +
                ", enabled=" + enabled +
                ", supportFormula=" + supportFormula +
                ", formulaExpression='" + formulaExpression + '\'' +
                ", formulaDescription='" + formulaDescription + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
