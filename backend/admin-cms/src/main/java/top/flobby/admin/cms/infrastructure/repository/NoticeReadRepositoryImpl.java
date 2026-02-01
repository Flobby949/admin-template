package top.flobby.admin.cms.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.flobby.admin.cms.domain.entity.NoticeRead;
import top.flobby.admin.cms.domain.repository.NoticeReadRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 公告已读仓储实现
 */
@Repository
@RequiredArgsConstructor
public class NoticeReadRepositoryImpl implements NoticeReadRepository {

    private final JpaNoticeReadRepository jpaNoticeReadRepository;

    @Override
    public Optional<NoticeRead> findByNoticeIdAndUserId(Long noticeId, Long userId) {
        return jpaNoticeReadRepository.findByNoticeIdAndUserId(noticeId, userId);
    }

    @Override
    public List<NoticeRead> findByNoticeId(Long noticeId) {
        return jpaNoticeReadRepository.findByNoticeId(noticeId);
    }

    @Override
    public Set<Long> findReadNoticeIdsByUserId(Long userId) {
        return jpaNoticeReadRepository.findReadNoticeIdsByUserId(userId);
    }

    @Override
    public NoticeRead save(NoticeRead noticeRead) {
        return jpaNoticeReadRepository.save(noticeRead);
    }

    @Override
    public long countByNoticeId(Long noticeId) {
        return jpaNoticeReadRepository.countByNoticeId(noticeId);
    }

    @Override
    public boolean existsByNoticeIdAndUserId(Long noticeId, Long userId) {
        return jpaNoticeReadRepository.existsByNoticeIdAndUserId(noticeId, userId);
    }

    @Override
    public Map<Long, Long> countByNoticeIds(Set<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, Long> result = new HashMap<>();
        List<Object[]> counts = jpaNoticeReadRepository.countByNoticeIdIn(noticeIds);
        for (Object[] row : counts) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }
}
