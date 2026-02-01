package top.flobby.admin.cms.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.flobby.admin.cms.domain.entity.Article;
import top.flobby.admin.cms.domain.entity.Category;
import top.flobby.admin.cms.domain.repository.ArticleRepository;
import top.flobby.admin.cms.domain.repository.CategoryRepository;
import top.flobby.admin.cms.interfaces.dto.ArticleDTO;
import top.flobby.admin.cms.interfaces.query.ArticleQuery;
import top.flobby.admin.cms.interfaces.vo.ArticleVO;
import top.flobby.admin.common.core.PageResult;
import top.flobby.admin.common.exception.BusinessException;
import top.flobby.admin.system.domain.entity.User;
import top.flobby.admin.system.domain.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * 分页查询文章
     */
    public PageResult<ArticleVO> listArticles(ArticleQuery query) {
        Pageable pageable = PageRequest.of(
                query.getPageNum() - 1,
                query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        Specification<Article> spec = buildSpecification(query);
        Page<Article> page = articleRepository.findAll(spec, pageable);

        // 批量加载分类名称，避免 N+1 查询
        Set<Long> categoryIds = page.getContent().stream()
                .map(Article::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryRepository.findAll().stream()
                    .filter(c -> categoryIds.contains(c.getId()))
                    .forEach(c -> categoryNameMap.put(c.getId(), c.getCategoryName()));
        }

        List<ArticleVO> list = page.getContent().stream()
                .map(article -> toArticleVO(article, categoryNameMap))
                .toList();

        return new PageResult<>(list, page.getTotalElements(), (long) query.getPageNum(), (long) query.getPageSize());
    }

    /**
     * 获取文章详情
     */
    public ArticleVO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        return toArticleVO(article);
    }

    /**
     * 获取文章详情并增加浏览量
     */
    @Transactional
    public ArticleVO getArticleDetail(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        articleRepository.incrementViewCount(id);
        ArticleVO vo = toArticleVO(article);
        vo.setViewCount(article.getViewCount() + 1);
        return vo;
    }

    /**
     * 创建文章
     */
    @Transactional
    public Long createArticle(ArticleDTO dto) {
        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new BusinessException("分类不存在"));
        }

        User currentUser = getCurrentUser();

        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCategoryId(dto.getCategoryId());
        article.setCoverUrl(dto.getCoverUrl());
        article.setStatus(Article.STATUS_DRAFT);
        article.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        article.setAuthorId(currentUser.getId());
        article.setDeptId(dto.getDeptId());
        article.setViewCount(0L);
        article.setDeleted(0);
        article.setCreateBy(currentUser.getUsername());

        Article saved = articleRepository.save(article);
        log.info("创建文章成功: id={}, title={}", saved.getId(), saved.getTitle());
        return saved.getId();
    }

    /**
     * 更新文章
     */
    @Transactional
    public void updateArticle(Long id, ArticleDTO dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));

        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new BusinessException("分类不存在"));
        }

        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCategoryId(dto.getCategoryId());
        article.setCoverUrl(dto.getCoverUrl());
        if (dto.getSortOrder() != null) {
            article.setSortOrder(dto.getSortOrder());
        }
        if (dto.getDeptId() != null) {
            article.setDeptId(dto.getDeptId());
        }
        article.setUpdateBy(getCurrentUsername());

        articleRepository.save(article);
        log.info("更新文章成功: id={}", id);
    }

    /**
     * 删除文章
     */
    @Transactional
    public void deleteArticle(Long id) {
        articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        articleRepository.deleteById(id);
        log.info("删除文章成功: id={}", id);
    }

    /**
     * 提交审核
     */
    @Transactional
    public void submitArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        article.submit();
        articleRepository.save(article);
        log.info("文章提交审核: id={}", id);
    }

    /**
     * 发布文章
     */
    @Transactional
    public void publishArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        article.publish(getCurrentUsername());
        articleRepository.save(article);
        log.info("文章发布成功: id={}", id);
    }

    /**
     * 驳回文章
     */
    @Transactional
    public void rejectArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        article.reject(getCurrentUsername());
        articleRepository.save(article);
        log.info("文章驳回: id={}", id);
    }

    /**
     * 下架文章
     */
    @Transactional
    public void revokeArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        article.revoke();
        articleRepository.save(article);
        log.info("文章下架: id={}", id);
    }

    private Specification<Article> buildSpecification(ArticleQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(query.getTitle())) {
                predicates.add(cb.like(root.get("title"), "%" + query.getTitle() + "%"));
            }
            if (query.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), query.getCategoryId()));
            }
            if (query.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            if (query.getAuthorId() != null) {
                predicates.add(cb.equal(root.get("authorId"), query.getAuthorId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ArticleVO toArticleVO(Article article) {
        return toArticleVO(article, null);
    }

    private ArticleVO toArticleVO(Article article, Map<Long, String> categoryNameMap) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
        vo.setCategoryId(article.getCategoryId());
        vo.setCoverUrl(article.getCoverUrl());
        vo.setStatus(article.getStatus());
        vo.setPublishTime(article.getPublishTime());
        vo.setRevokeTime(article.getRevokeTime());
        vo.setAuditBy(article.getAuditBy());
        vo.setAuditTime(article.getAuditTime());
        vo.setDeptId(article.getDeptId());
        vo.setAuthorId(article.getAuthorId());
        vo.setViewCount(article.getViewCount());
        vo.setSortOrder(article.getSortOrder());
        vo.setCreateTime(article.getCreateTime());
        vo.setCreateBy(article.getCreateBy());

        if (article.getCategoryId() != null) {
            if (categoryNameMap != null && categoryNameMap.containsKey(article.getCategoryId())) {
                vo.setCategoryName(categoryNameMap.get(article.getCategoryId()));
            } else {
                categoryRepository.findById(article.getCategoryId())
                        .ifPresent(c -> vo.setCategoryName(c.getCategoryName()));
            }
        }

        return vo;
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}
