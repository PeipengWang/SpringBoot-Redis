package com.example.springbootredis.repository;

import com.example.springbootredis.entity.CommandTelemetryRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 指令-遥测参数关联Repository接口
 */
@Repository
public interface CommandTelemetryRelationRepository extends JpaRepository<CommandTelemetryRelation, Long> {
    
    /**
     * 根据指令ID查询所有关联的遥测参数
     */
    @Query("SELECT ctr FROM CommandTelemetryRelation ctr " +
           "LEFT JOIN FETCH ctr.telemetryParam " +
           "WHERE ctr.commandId = :commandId " +
           "ORDER BY ctr.weight DESC, ctr.id")
    List<CommandTelemetryRelation> findByCommandIdWithTelemetryParam(@Param("commandId") Long commandId);
    
    /**
     * 根据指令ID查询关联的遥测参数（简单查询）
     */
    List<CommandTelemetryRelation> findByCommandIdOrderByWeightDesc(Long commandId);
    
    /**
     * 根据遥测参数ID查询关联的指令
     */
    List<CommandTelemetryRelation> findByTelemetryParamId(Long telemetryParamId);
    
    /**
     * 查询必须满足的关联关系
     */
    @Query("SELECT ctr FROM CommandTelemetryRelation ctr " +
           "LEFT JOIN FETCH ctr.telemetryParam " +
           "WHERE ctr.commandId = :commandId AND ctr.required = true " +
           "ORDER BY ctr.weight DESC")
    List<CommandTelemetryRelation> findRequiredByCommandId(@Param("commandId") Long commandId);
    
    /**
     * 查询可选的关联关系
     */
    @Query("SELECT ctr FROM CommandTelemetryRelation ctr " +
           "LEFT JOIN FETCH ctr.telemetryParam " +
           "WHERE ctr.commandId = :commandId AND ctr.required = false " +
           "ORDER BY ctr.weight DESC")
    List<CommandTelemetryRelation> findOptionalByCommandId(@Param("commandId") Long commandId);
    
    /**
     * 统计指令关联的遥测参数数量
     */
    long countByCommandId(Long commandId);
    
    /**
     * 统计指令必须满足的遥测参数数量
     */
    long countByCommandIdAndRequiredTrue(Long commandId);
    
    /**
     * 删除指令的所有关联关系
     */
    void deleteByCommandId(Long commandId);
    
    /**
     * 删除遥测参数的所有关联关系
     */
    void deleteByTelemetryParamId(Long telemetryParamId);
}
