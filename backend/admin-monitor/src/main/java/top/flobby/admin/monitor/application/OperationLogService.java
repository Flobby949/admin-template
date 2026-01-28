package top.flobby.admin.monitor.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.flobby.admin.common.core.PageResult;
import top.flobby.admin.common.exception.BusinessException;
import top.flobby.admin.monitor.domain.entity.OperationLog;
import top.flobby.admin.monitor.domain.repository.OperationLogRepository;
import top.flobby.admin.monitor.interfaces.query.OperationLogQuery;
import top.flobby.admin.monitor.interfaces.vo.OperationLogVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务
 * <p>
 * 职责:
 * - 操作日志查询
 * - 操作日志记录(由AOP调用)
 * - 操作日志清理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    /**
     * 最大日志内容长度
     */
    private static final int MAX_CONTENT_LENGTH = 2000;

    /**
     * 分页查询操作日志
     *
     * @param query 查询条件
     * @return 分页结果
     */
    public PageResult<OperationLogVO> listOperationLogs(OperationLogQuery query) {
        Pageable pageable = PageRequest.of(
                query.getPageNum() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "operTime")
        );

        Page<OperationLog> page = operationLogRepository.search(query, pageable);

        List<OperationLogVO> list = page.getContent().stream()
                .map(this::toOperationLogVO)
                .collect(Collectors.toList());

        return PageResult.build(
                list,
                page.getTotalElements(),
                (long) query.getPageNum(),
                (long) query.getPageSize());
    }

    /**
     * 获取操作日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    public OperationLogVO getOperationLogById(Long id) {
        OperationLog log = operationLogRepository.findById(id)
                .orElseThrow(() -> new BusinessException("操作日志不存在"));
        return toOperationLogVO(log);
    }

    /**
     * 记录操作日志
     * <p>
     * 使用REQUIRES_NEW事务,避免影响主业务
     *
     * @param operLog 操作日志
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordOperationLog(OperationLog operLog) {
        try {
            // 裁剪日志内容
            if (operLog.getOperParam() != null && operLog.getOperParam().length() > MAX_CONTENT_LENGTH) {
                operLog.setOperParam(operLog.getOperParam().substring(0, MAX_CONTENT_LENGTH) + "...");
            }
            if (operLog.getJsonResult() != null && operLog.getJsonResult().length() > MAX_CONTENT_LENGTH) {
                operLog.setJsonResult(operLog.getJsonResult().substring(0, MAX_CONTENT_LENGTH) + "...");
            }
            if (operLog.getErrorMsg() != null && operLog.getErrorMsg().length() > MAX_CONTENT_LENGTH) {
                operLog.setErrorMsg(operLog.getErrorMsg().substring(0, MAX_CONTENT_LENGTH) + "...");
            }

            // 脱敏处理
            if (operLog.getOperParam() != null) {
                operLog.setOperParam(desensitize(operLog.getOperParam()));
            }

            operationLogRepository.save(operLog);
        } catch (Exception e) {
            // 记录日志失败不影响主业务
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 删除操作日志
     *
     * @param id 日志ID
     */
    @Transactional
    public void deleteOperationLog(Long id) {
        operationLogRepository.deleteById(id);
        log.info("删除操作日志成功: id={}", id);
    }

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     */
    @Transactional
    public void deleteOperationLogs(List<Long> ids) {
        operationLogRepository.deleteByIds(ids);
        log.info("批量删除操作日志成功: count={}", ids.size());
    }

    /**
     * 清空所有操作日志
     */
    @Transactional
    public void clearAllOperationLogs() {
        operationLogRepository.deleteAll();
        log.info("清空所有操作日志成功");
    }

    /**
     * 清理历史日志
     *
     * @param days 保留天数
     */
    @Transactional
    public void cleanHistoryLogs(int days) {
        LocalDateTime time = LocalDateTime.now().minusDays(days);
        operationLogRepository.deleteBefore(time);
        log.info("清理历史日志成功: before={}", time);
    }

    /**
     * 脱敏处理
     *
     * @param content 内容
     * @return 脱敏后的内容
     */
    private String desensitize(String content) {
        if (content == null) {
            return null;
        }

        // 脱敏密码字段
        content = content.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"******\"");
        content = content.replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"******\"");
        content = content.replaceAll("\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"******\"");

        return content;
    }

    /**
     * 转换为VO
     *
     * @param log 操作日志实体
     * @return 操作日志VO
     */
    private OperationLogVO toOperationLogVO(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setTitle(log.getTitle());
        vo.setBusinessType(log.getBusinessType());
        vo.setMethod(log.getMethod());
        vo.setRequestMethod(log.getRequestMethod());
        vo.setOperatorType(log.getOperatorType());
        vo.setOperName(log.getOperName());
        vo.setDeptName(log.getDeptName());
        vo.setOperUrl(log.getOperUrl());
        vo.setOperIp(log.getOperIp());
        vo.setOperLocation(log.getOperLocation());
        vo.setOperParam(log.getOperParam());
        vo.setJsonResult(log.getJsonResult());
        vo.setStatus(log.getStatus());
        vo.setErrorMsg(log.getErrorMsg());
        vo.setOperTime(log.getOperTime());
        vo.setCostTime(log.getCostTime());
        return vo;
    }
}
