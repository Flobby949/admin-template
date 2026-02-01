package top.flobby.admin.cms.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import top.flobby.admin.cms.domain.entity.Article;
import top.flobby.admin.cms.domain.repository.ArticleRepository;

import java.util.Optional;

/**
 * 文章仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepository {

    private final JpaArticleRepository jpaArticleRepository;

    @Override
    public Optional<Article> findById(Long id) {
        return jpaArticleRepository.findById(id)
                .filter(a -> a.getDeleted() == 0);
    }

    @Override
    public Page<Article> findAll(Specification<Article> spec, Pageable pageable) {
        Specification<Article> notDeleted = (root, query, cb) -> cb.equal(root.get("deleted"), 0);
        Specification<Article> combined = spec == null ? notDeleted : spec.and(notDeleted);
        return jpaArticleRepository.findAll(combined, pageable);
    }

    @Override
    public Article save(Article article) {
        return jpaArticleRepository.save(article);
    }

    @Override
    public void deleteById(Long id) {
        jpaArticleRepository.softDeleteById(id);
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return jpaArticleRepository.countByCategoryIdAndDeleted(categoryId, 0);
    }

    @Override
    public int incrementViewCount(Long id) {
        return jpaArticleRepository.incrementViewCount(id);
    }
}
