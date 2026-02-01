package top.flobby.admin.cms.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.flobby.admin.cms.domain.entity.Category;
import top.flobby.admin.cms.domain.repository.ArticleRepository;
import top.flobby.admin.cms.domain.repository.CategoryRepository;
import top.flobby.admin.cms.interfaces.dto.CategoryDTO;
import top.flobby.admin.cms.interfaces.vo.CategoryVO;
import top.flobby.admin.common.exception.BusinessException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    private static final int MAX_LEVEL = 10;

    /**
     * 获取分类树
     */
    public List<CategoryVO> listCategoryTree() {
        List<Category> categories = categoryRepository.findAll();
        return buildCategoryTree(categories, 0L);
    }

    /**
     * 获取分类详情
     */
    public CategoryVO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
        return toCategoryVO(category);
    }

    /**
     * 创建分类
     */
    @Transactional
    public Long createCategory(CategoryDTO dto) {
        Category parent = null;
        if (dto.getParentId() != null && dto.getParentId() != 0) {
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new BusinessException("父分类不存在"));
        }

        if (categoryRepository.existsByCategoryNameAndParentIdAndDeleted(
                dto.getCategoryName(), dto.getParentId(), 0)) {
            throw new BusinessException("同级分类名称已存在");
        }

        String ancestors = calculateAncestors(parent);
        int level = ancestors.isEmpty() ? 1 : ancestors.split(",").length + 1;
        if (level > MAX_LEVEL) {
            throw new BusinessException("分类层级不能超过" + MAX_LEVEL + "级");
        }

        Category category = new Category();
        category.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        category.setAncestors(ancestors);
        category.setCategoryName(dto.getCategoryName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        category.setDeptId(dto.getDeptId());
        category.setDeleted(0);

        Category saved = categoryRepository.save(category);
        log.info("创建分类成功: id={}, name={}", saved.getId(), saved.getCategoryName());
        return saved.getId();
    }

    /**
     * 更新分类
     */
    @Transactional
    public void updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));

        if (categoryRepository.existsByCategoryNameAndParentIdAndIdNotAndDeleted(
                dto.getCategoryName(), dto.getParentId(), id, 0)) {
            throw new BusinessException("同级分类名称已存在");
        }

        if (dto.getParentId() != null && !dto.getParentId().equals(category.getParentId())) {
            if (dto.getParentId().equals(id)) {
                throw new BusinessException("父分类不能是自己");
            }
            Category newParent = null;
            if (dto.getParentId() != 0) {
                newParent = categoryRepository.findById(dto.getParentId())
                        .orElseThrow(() -> new BusinessException("父分类不存在"));
                if (newParent.getAncestors() != null && newParent.getAncestors().contains("," + id + ",")) {
                    throw new BusinessException("父分类不能是自己的子分类");
                }
            }

            String oldAncestors = category.getAncestors();
            String newAncestors = calculateAncestors(newParent);
            category.setParentId(dto.getParentId());
            category.setAncestors(newAncestors);

            String oldPrefix = oldAncestors + "," + id;
            String newPrefix = newAncestors + "," + id;
            categoryRepository.updateAncestorsByPrefix(oldPrefix, newPrefix);
        }

        category.setCategoryName(dto.getCategoryName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : category.getSortOrder());
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }
        if (dto.getDeptId() != null) {
            category.setDeptId(dto.getDeptId());
        }

        categoryRepository.save(category);
        log.info("更新分类成功: id={}", id);
    }

    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));

        if (categoryRepository.existsByParentId(id)) {
            throw new BusinessException("存在子分类，无法删除");
        }

        if (articleRepository.countByCategoryId(id) > 0) {
            throw new BusinessException("分类下存在文章，无法删除");
        }

        categoryRepository.deleteById(id);
        log.info("删除分类成功: id={}", id);
    }

    /**
     * 更新分类状态
     */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));

        String prefix = (category.getAncestors() == null ? "" : category.getAncestors()) + "," + id;
        categoryRepository.updateStatusCascade(id, prefix, status);
        log.info("更新分类状态成功: id={}, status={}", id, status);
    }

    private String calculateAncestors(Category parent) {
        if (parent == null) {
            return "0";
        }
        return (parent.getAncestors() == null ? "0" : parent.getAncestors()) + "," + parent.getId();
    }

    private List<CategoryVO> buildCategoryTree(List<Category> categories, Long parentId) {
        return categories.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .sorted(Comparator.comparingInt(c -> c.getSortOrder() != null ? c.getSortOrder() : 0))
                .map(c -> {
                    CategoryVO vo = toCategoryVO(c);
                    vo.setChildren(buildCategoryTree(categories, c.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private CategoryVO toCategoryVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setCategoryName(category.getCategoryName());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus());
        vo.setDeptId(category.getDeptId());
        vo.setCreateTime(category.getCreateTime());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
