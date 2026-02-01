package top.flobby.admin.cms.domain.repository;

import top.flobby.admin.cms.domain.entity.NoticeRead;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 公告已读仓储接口
 */
public interface NoticeReadRepository {

    Optional<NoticeRead> findByNoticeIdAndUserId(Long noticeId, Long userId);

    List<NoticeRead> findByNoticeId(Long noticeId);

    Set<Long> findReadNoticeIdsByUserId(Long userId);

    NoticeRead save(NoticeRead noticeRead);

    long countByNoticeId(Long noticeId);

    boolean existsByNoticeIdAndUserId(Long noticeId, Long userId);
}
