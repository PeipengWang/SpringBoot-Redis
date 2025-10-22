package com.example.springbootredis.repository;

import com.example.springbootredis.entity.Command;
import com.example.springbootredis.entity.enums.CommandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 指令Repository接口
 */
@Repository
public interface CommandRepository extends JpaRepository<Command, Long> {
    
    /**
     * 根据卫星ID查询指令列表
     */
    List<Command> findBySatelliteIdOrderByCreateTimeDesc(String satelliteId);
    
    /**
     * 根据状态查询指令列表
     */
    List<Command> findByStatusOrderByCreateTimeDesc(CommandStatus status);
    
    /**
     * 查询未执行和已编制的指令（用于定时同步）
     */
    @Query("SELECT c FROM Command c WHERE c.status IN :statuses ORDER BY c.createTime DESC")
    List<Command> findByStatusInOrderByCreateTimeDesc(@Param("statuses") List<CommandStatus> statuses);
    
    /**
     * 查询执行中的指令（用于超时检查）
     */
    @Query("SELECT c FROM Command c WHERE c.status = :status AND c.expireTime < :currentTime")
    List<Command> findExecutingCommandsExpired(@Param("status") CommandStatus status, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 根据指令代码查询指令
     */
    Command findByCommandCode(String commandCode);
    
    /**
     * 根据卫星ID和状态查询指令
     */
    List<Command> findBySatelliteIdAndStatusOrderByCreateTimeDesc(String satelliteId, CommandStatus status);
    
    /**
     * 查询指定时间范围内的指令
     */
    @Query("SELECT c FROM Command c WHERE c.executeTime BETWEEN :startTime AND :endTime ORDER BY c.executeTime")
    List<Command> findByExecuteTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定状态的指令数量
     */
    long countByStatus(CommandStatus status);
    
    /**
     * 统计指定卫星的指令数量
     */
    long countBySatelliteId(String satelliteId);
}
