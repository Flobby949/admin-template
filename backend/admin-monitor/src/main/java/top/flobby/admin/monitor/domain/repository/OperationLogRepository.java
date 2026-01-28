package top.flobby.admin.monitor.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import top.flobby.admin.monitor.domain.entity.OperationLog;
import top.flobby.admin.monitor.interfaces.query.OperationLogQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 操作日志仓储接口
 * <p>
 * 定义操作日志的持久化操作
 */
public interface OperationLogRepository {

    /**
     * 根据ID查询操作日志
     *
     * @param id 日志ID
     * @return 操作日志
     */
    Optional<OperationLog> findById(Long id);

    /**
     * 分页查询操作日志
     *
     * @param query    查询条件
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<OperationLog> search(OperationLogQuery query, Pageable pageable);

    /**
     * 保存操作日志
     *
     * @param log 操作日志
     * @return 保存后的日志
     */
    OperationLog save(OperationLog log);

    /**
     * 删除操作日志
     *
     * @param id 日志ID
     */
    void deleteById(Long id);

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     */
    void deleteByIds(List<Long> ids);

    /**
     * 删除指定时间之前的日志
     *
     * @param time 时间
     */
    void deleteBefore(LocalDateTime time);

    /**
     * 清空所有日志
     */
    void deleteAll();
}
