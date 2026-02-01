package top.flobby.admin.cms.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import top.flobby.admin.cms.domain.entity.Notice;

import java.util.Optional;

/**
 * 公告仓储接口
 */
public interface NoticeRepository {

    Optional<Notice> findById(Long id);

    Page<Notice> findAll(Specification<Notice> spec, Pageable pageable);

    Notice save(Notice notice);

    void deleteById(Long id);
}
