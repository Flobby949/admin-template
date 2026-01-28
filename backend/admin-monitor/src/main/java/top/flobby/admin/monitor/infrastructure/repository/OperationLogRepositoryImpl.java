package top.flobby.admin.monitor.infrastructure.repository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.flobby.admin.monitor.domain.entity.OperationLog;
import top.flobby.admin.monitor.domain.repository.OperationLogRepository;
import top.flobby.admin.monitor.interfaces.query.OperationLogQuery;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 操作日志仓储实现
 */
@Repository
@RequiredArgsConstructor
public class OperationLogRepositoryImpl implements OperationLogRepository {

    private final JpaOperationLogRepository jpaOperationLogRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Optional<OperationLog> findById(Long id) {
        return jpaOperationLogRepository.findById(id);
    }

    @Override
    public Page<OperationLog> search(OperationLogQuery query, Pageable pageable) {
        Specification<OperationLog> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 操作模块
            if (StringUtils.hasText(query.getTitle())) {
                predicates.add(criteriaBuilder.like(root.get("title"), "%" + query.getTitle() + "%"));
            }

            // 操作人员
            if (StringUtils.hasText(query.getOperName())) {
                predicates.add(criteriaBuilder.like(root.get("operName"), "%" + query.getOperName() + "%"));
            }

            // 业务类型
            if (query.getBusinessType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessType"), query.getBusinessType()));
            }

            // 操作状态
            if (query.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.getStatus()));
            }

            // 时间范围
            if (StringUtils.hasText(query.getStartTime())) {
                LocalDateTime startTime = LocalDateTime.parse(query.getStartTime() + " 00:00:00", FORMATTER);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("operTime"), startTime));
            }
            if (StringUtils.hasText(query.getEndTime())) {
                LocalDateTime endTime = LocalDateTime.parse(query.getEndTime() + " 23:59:59", FORMATTER);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("operTime"), endTime));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return jpaOperationLogRepository.findAll(spec, pageable);
    }

    @Override
    public OperationLog save(OperationLog log) {
        return jpaOperationLogRepository.save(log);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaOperationLogRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        jpaOperationLogRepository.deleteByIdIn(ids);
    }

    @Override
    @Transactional
    public void deleteBefore(LocalDateTime time) {
        jpaOperationLogRepository.deleteByOperTimeBefore(time);
    }

    @Override
    @Transactional
    public void deleteAll() {
        jpaOperationLogRepository.deleteAll();
    }
}
