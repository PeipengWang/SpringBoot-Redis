package com.example.springbootredis.repository;

import com.example.springbootredis.entity.TelemetryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 遥测数据Repository接口
 */
@Repository
public interface TelemetryDataRepository extends JpaRepository<TelemetryData, Long> {
    
    /**
     * 根据指令ID查询遥测数据
     */
    List<TelemetryData> findByCommandIdOrderByReceiveTimeDesc(Long commandId);
    
    /**
     * 根据指令ID和参数代码查询遥测数据
     */
    List<TelemetryData> findByCommandIdAndParamCodeOrderByReceiveTimeDesc(Long commandId, String paramCode);
    
    /**
     * 获取指令指定参数的最新遥测数据
     */
    @Query("SELECT td FROM TelemetryData td " +
           "WHERE td.commandId = :commandId AND td.paramCode = :paramCode " +
           "ORDER BY td.receiveTime DESC LIMIT 1")
    Optional<TelemetryData> findLatestByCommandIdAndParamCode(@Param("commandId") Long commandId, @Param("paramCode") String paramCode);
    
    /**
     * 查询满足条件的遥测数据
     */
    List<TelemetryData> findByCommandIdAndJudgeResultTrueOrderByReceiveTimeDesc(Long commandId);
    
    /**
     * 查询不满足条件的遥测数据
     */
    List<TelemetryData> findByCommandIdAndJudgeResultFalseOrderByReceiveTimeDesc(Long commandId);
    
    /**
     * 根据数据来源查询遥测数据
     */
    List<TelemetryData> findByDataSourceOrderByReceiveTimeDesc(String dataSource);
    
    /**
     * 查询指定时间范围内的遥测数据
     */
    @Query("SELECT td FROM TelemetryData td " +
           "WHERE td.receiveTime BETWEEN :startTime AND :endTime " +
           "ORDER BY td.receiveTime DESC")
    List<TelemetryData> findByReceiveTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指令的遥测数据数量
     */
    long countByCommandId(Long commandId);
    
    /**
     * 统计指令满足条件的遥测数据数量
     */
    long countByCommandIdAndJudgeResultTrue(Long commandId);
    
    /**
     * 统计指令不满足条件的遥测数据数量
     */
    long countByCommandIdAndJudgeResultFalse(Long commandId);
    
    /**
     * 删除指定时间之前的遥测数据（用于数据清理）
     */
    void deleteByReceiveTimeBefore(LocalDateTime cutoffTime);
    
    /**
     * 查询指令的所有参数代码
     */
    @Query("SELECT DISTINCT td.paramCode FROM TelemetryData td WHERE td.commandId = :commandId")
    List<String> findDistinctParamCodesByCommandId(@Param("commandId") Long commandId);
}
