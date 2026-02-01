package top.flobby.admin.cms.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.cms.domain.entity.Notice;

/**
 * Spring Data JPA 公告仓储
 */
public interface JpaNoticeRepository extends JpaRepository<Notice, Long>, JpaSpecificationExecutor<Notice> {

    @Modifying
    @Query("UPDATE Notice n SET n.deleted = 1 WHERE n.id = :id")
    void softDeleteById(@Param("id") Long id);
}
