package top.flobby.admin.cms.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.cms.application.ArticleService;
import top.flobby.admin.cms.interfaces.dto.ArticleDTO;
import top.flobby.admin.cms.interfaces.query.ArticleQuery;
import top.flobby.admin.cms.interfaces.vo.ArticleVO;
import top.flobby.admin.common.annotation.RequiresPermission;
import top.flobby.admin.common.core.PageResult;
import top.flobby.admin.common.core.Result;

/**
 * 文章管理控制器
 */
@Tag(name = "文章管理", description = "CMS文章管理相关接口")
@RestController
@RequestMapping("/api/cms/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "分页查询文章列表")
    @PostMapping("/list")
    @RequiresPermission("cms:article:list")
    public Result<PageResult<ArticleVO>> listArticles(@RequestBody ArticleQuery query) {
        PageResult<ArticleVO> result = articleService.listArticles(query);
        return Result.success(result);
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/{id}")
    @RequiresPermission("cms:article:list")
    public Result<ArticleVO> getArticleById(@PathVariable Long id) {
        ArticleVO article = articleService.getArticleById(id);
        return Result.success(article);
    }

    @Operation(summary = "获取文章详情（增加浏览量）")
    @GetMapping("/{id}/detail")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        ArticleVO article = articleService.getArticleDetail(id);
        return Result.success(article);
    }

    @Operation(summary = "新增文章")
    @PostMapping
    @RequiresPermission("cms:article:add")
    public Result<Long> createArticle(@Valid @RequestBody ArticleDTO dto) {
        Long id = articleService.createArticle(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    @RequiresPermission("cms:article:edit")
    public Result<Void> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleDTO dto) {
        articleService.updateArticle(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    @RequiresPermission("cms:article:delete")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    @Operation(summary = "提交审核")
    @PutMapping("/{id}/submit")
    @RequiresPermission("cms:article:edit")
    public Result<Void> submitArticle(@PathVariable Long id) {
        articleService.submitArticle(id);
        return Result.success();
    }

    @Operation(summary = "发布文章")
    @PutMapping("/{id}/publish")
    @RequiresPermission("cms:article:publish")
    public Result<Void> publishArticle(@PathVariable Long id) {
        articleService.publishArticle(id);
        return Result.success();
    }

    @Operation(summary = "驳回文章")
    @PutMapping("/{id}/reject")
    @RequiresPermission("cms:article:publish")
    public Result<Void> rejectArticle(@PathVariable Long id) {
        articleService.rejectArticle(id);
        return Result.success();
    }

    @Operation(summary = "下架文章")
    @PutMapping("/{id}/revoke")
    @RequiresPermission("cms:article:publish")
    public Result<Void> revokeArticle(@PathVariable Long id) {
        articleService.revokeArticle(id);
        return Result.success();
    }
}
