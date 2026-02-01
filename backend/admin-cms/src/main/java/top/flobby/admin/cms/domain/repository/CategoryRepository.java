package top.flobby.admin.cms.domain.repository;

import top.flobby.admin.cms.domain.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * 分类仓储接口
 */
public interface CategoryRepository {

    Optional<Category> findById(Long id);

    List<Category> findAll();

    List<Category> findByParentId(Long parentId);

    List<Category> findByAncestorsStartingWith(String ancestorsPrefix);

    boolean existsByParentId(Long parentId);

    boolean existsByCategoryNameAndParentIdAndDeleted(String categoryName, Long parentId, Integer deleted);

    boolean existsByCategoryNameAndParentIdAndIdNotAndDeleted(String categoryName, Long parentId, Long id, Integer deleted);

    Category save(Category category);

    void deleteById(Long id);

    int updateAncestorsByPrefix(String oldPrefix, String newPrefix);

    int updateStatusCascade(Long id, String prefix, Integer status);
}
