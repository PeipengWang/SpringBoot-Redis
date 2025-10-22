package com.example.springbootredis.repository;

import com.example.springbootredis.entity.TelemetryParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 遥测参数Repository接口
 */
@Repository
public interface TelemetryParamRepository extends JpaRepository<TelemetryParam, Long> {
    
    /**
     * 根据参数代码查询遥测参数
     */
    Optional<TelemetryParam> findByParamCode(String paramCode);
    
    /**
     * 根据参数名称查询遥测参数
     */
    List<TelemetryParam> findByParamNameContaining(String paramName);
    
    /**
     * 查询启用的遥测参数
     */
    List<TelemetryParam> findByEnabledTrueOrderByParamName();
    
    /**
     * 根据参数类型查询遥测参数
     */
    List<TelemetryParam> findByParamTypeOrderByParamName(String paramType);
    
    /**
     * 检查参数代码是否存在
     */
    boolean existsByParamCode(String paramCode);
    
    /**
     * 根据参数代码列表查询遥测参数
     */
    @Query("SELECT tp FROM TelemetryParam tp WHERE tp.paramCode IN :paramCodes")
    List<TelemetryParam> findByParamCodeIn(@Param("paramCodes") List<String> paramCodes);
}
