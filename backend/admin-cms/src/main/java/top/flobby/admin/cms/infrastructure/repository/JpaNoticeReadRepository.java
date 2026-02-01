package top.flobby.admin.cms.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.cms.domain.entity.NoticeRead;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA 公告已读仓储
 */
public interface JpaNoticeReadRepository extends JpaRepository<NoticeRead, Long> {

    Optional<NoticeRead> findByNoticeIdAndUserId(Long noticeId, Long userId);

    List<NoticeRead> findByNoticeId(Long noticeId);

    @Query("SELECT nr.noticeId FROM NoticeRead nr WHERE nr.userId = :userId")
    Set<Long> findReadNoticeIdsByUserId(@Param("userId") Long userId);

    long countByNoticeId(Long noticeId);

    boolean existsByNoticeIdAndUserId(Long noticeId, Long userId);
}
