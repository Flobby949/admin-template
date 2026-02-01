package top.flobby.admin.cms.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.cms.domain.entity.Category;

import java.util.List;

/**
 * Spring Data JPA 分类仓储
 */
public interface JpaCategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByDeleted(Integer deleted);

    List<Category> findByParentIdAndDeleted(Long parentId, Integer deleted);

    @Query("SELECT c FROM Category c WHERE c.ancestors LIKE CONCAT(:prefix, '%') AND c.deleted = 0")
    List<Category> findByAncestorsStartingWith(@Param("prefix") String prefix);

    boolean existsByParentIdAndDeleted(Long parentId, Integer deleted);

    boolean existsByCategoryNameAndParentIdAndDeleted(String categoryName, Long parentId, Integer deleted);

    boolean existsByCategoryNameAndParentIdAndIdNotAndDeleted(String categoryName, Long parentId, Long id, Integer deleted);

    @Modifying
    @Query("UPDATE Category c SET c.deleted = 1 WHERE c.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Category c SET c.ancestors = CONCAT(:newPrefix, SUBSTRING(c.ancestors, LENGTH(:oldPrefix) + 1)) " +
           "WHERE c.ancestors LIKE CONCAT(:oldPrefix, '%') AND c.deleted = 0")
    int updateAncestorsByPrefix(@Param("oldPrefix") String oldPrefix, @Param("newPrefix") String newPrefix);

    @Modifying
    @Query("UPDATE Category c SET c.status = :status WHERE c.deleted = 0 AND (c.id = :id OR c.ancestors LIKE CONCAT(:prefix, '%'))")
    int updateStatusCascade(@Param("id") Long id, @Param("prefix") String prefix, @Param("status") Integer status);
}
