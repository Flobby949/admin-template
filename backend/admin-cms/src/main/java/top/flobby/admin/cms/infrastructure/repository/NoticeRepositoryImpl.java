package top.flobby.admin.cms.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import top.flobby.admin.cms.domain.entity.Notice;
import top.flobby.admin.cms.domain.repository.NoticeRepository;

import java.util.Optional;

/**
 * 公告仓储实现
 */
@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {

    private final JpaNoticeRepository jpaNoticeRepository;

    @Override
    public Optional<Notice> findById(Long id) {
        return jpaNoticeRepository.findById(id)
                .filter(n -> n.getDeleted() == 0);
    }

    @Override
    public Page<Notice> findAll(Specification<Notice> spec, Pageable pageable) {
        Specification<Notice> notDeleted = (root, query, cb) -> cb.equal(root.get("deleted"), 0);
        Specification<Notice> combined = spec == null ? notDeleted : spec.and(notDeleted);
        return jpaNoticeRepository.findAll(combined, pageable);
    }

    @Override
    public Notice save(Notice notice) {
        return jpaNoticeRepository.save(notice);
    }

    @Override
    public void deleteById(Long id) {
        jpaNoticeRepository.softDeleteById(id);
    }
}
