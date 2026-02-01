package top.flobby.admin.cms.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.flobby.admin.cms.domain.entity.Category;
import top.flobby.admin.cms.domain.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

/**
 * 分类仓储实现
 */
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final JpaCategoryRepository jpaCategoryRepository;

    @Override
    public Optional<Category> findById(Long id) {
        return jpaCategoryRepository.findById(id)
                .filter(c -> c.getDeleted() == 0);
    }

    @Override
    public List<Category> findAll() {
        return jpaCategoryRepository.findByDeleted(0);
    }

    @Override
    public List<Category> findByParentId(Long parentId) {
        return jpaCategoryRepository.findByParentIdAndDeleted(parentId, 0);
    }

    @Override
    public List<Category> findByAncestorsStartingWith(String ancestorsPrefix) {
        return jpaCategoryRepository.findByAncestorsStartingWith(ancestorsPrefix);
    }

    @Override
    public boolean existsByParentId(Long parentId) {
        return jpaCategoryRepository.existsByParentIdAndDeleted(parentId, 0);
    }

    @Override
    public boolean existsByCategoryNameAndParentIdAndDeleted(String categoryName, Long parentId, Integer deleted) {
        return jpaCategoryRepository.existsByCategoryNameAndParentIdAndDeleted(categoryName, parentId, deleted);
    }

    @Override
    public boolean existsByCategoryNameAndParentIdAndIdNotAndDeleted(String categoryName, Long parentId, Long id, Integer deleted) {
        return jpaCategoryRepository.existsByCategoryNameAndParentIdAndIdNotAndDeleted(categoryName, parentId, id, deleted);
    }

    @Override
    public Category save(Category category) {
        return jpaCategoryRepository.save(category);
    }

    @Override
    public void deleteById(Long id) {
        jpaCategoryRepository.softDeleteById(id);
    }

    @Override
    public int updateAncestorsByPrefix(String oldPrefix, String newPrefix) {
        return jpaCategoryRepository.updateAncestorsByPrefix(oldPrefix, newPrefix);
    }

    @Override
    public int updateStatusCascade(Long id, String prefix, Integer status) {
        return jpaCategoryRepository.updateStatusCascade(id, prefix, status);
    }
}
