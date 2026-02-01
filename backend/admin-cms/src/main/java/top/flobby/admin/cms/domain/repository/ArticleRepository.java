package top.flobby.admin.cms.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import top.flobby.admin.cms.domain.entity.Article;

import java.util.Optional;

/**
 * 文章仓储接口
 */
public interface ArticleRepository {

    Optional<Article> findById(Long id);

    Page<Article> findAll(Specification<Article> spec, Pageable pageable);

    Article save(Article article);

    void deleteById(Long id);

    long countByCategoryId(Long categoryId);

    int incrementViewCount(Long id);
}
