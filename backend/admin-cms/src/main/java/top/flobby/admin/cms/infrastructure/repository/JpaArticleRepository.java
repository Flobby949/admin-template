package top.flobby.admin.cms.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.cms.domain.entity.Article;

/**
 * Spring Data JPA 文章仓储
 */
public interface JpaArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    long countByCategoryIdAndDeleted(Long categoryId, Integer deleted);

    @Modifying
    @Query("UPDATE Article a SET a.deleted = 1 WHERE a.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    int incrementViewCount(@Param("id") Long id);
}
