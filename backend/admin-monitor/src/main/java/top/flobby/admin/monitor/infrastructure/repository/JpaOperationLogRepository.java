package top.flobby.admin.monitor.infrastructure.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.monitor.domain.entity.OperationLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA 操作日志仓储
 */
public interface JpaOperationLogRepository extends JpaRepository<OperationLog, Long>,
        JpaSpecificationExecutor<OperationLog> {

    /**
     * 根据操作人查询
     */
    Page<OperationLog> findByOperName(String operName, Pageable pageable);

    /**
     * 根据状态查询
     */
    Page<OperationLog> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据时间范围查询
     */
    @Query("SELECT o FROM OperationLog o WHERE o.operTime BETWEEN :startTime AND :endTime")
    Page<OperationLog> findByOperTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 删除指定时间之前的日志
     */
    @Modifying
    @Query("DELETE FROM OperationLog o WHERE o.operTime < :time")
    void deleteByOperTimeBefore(@Param("time") LocalDateTime time);

    /**
     * 批量删除
     */
    @Modifying
    @Query("DELETE FROM OperationLog o WHERE o.id IN :ids")
    void deleteByIdIn(@Param("ids") List<Long> ids);
}
