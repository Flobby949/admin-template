package top.flobby.admin.cms.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.cms.application.CategoryService;
import top.flobby.admin.cms.interfaces.dto.CategoryDTO;
import top.flobby.admin.cms.interfaces.vo.CategoryVO;
import top.flobby.admin.common.annotation.RequiresPermission;
import top.flobby.admin.common.core.Result;

import java.util.List;

/**
 * 分类管理控制器
 */
@Tag(name = "分类管理", description = "CMS分类管理相关接口")
@RestController
@RequestMapping("/api/cms/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    @RequiresPermission("cms:category:list")
    public Result<List<CategoryVO>> listCategoryTree() {
        List<CategoryVO> tree = categoryService.listCategoryTree();
        return Result.success(tree);
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    @RequiresPermission("cms:category:list")
    public Result<CategoryVO> getCategoryById(@PathVariable Long id) {
        CategoryVO category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    @Operation(summary = "新增分类")
    @PostMapping
    @RequiresPermission("cms:category:add")
    public Result<Long> createCategory(@Valid @RequestBody CategoryDTO dto) {
        Long id = categoryService.createCategory(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @RequiresPermission("cms:category:edit")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        categoryService.updateCategory(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @RequiresPermission("cms:category:delete")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "更新分类状态")
    @PutMapping("/{id}/status")
    @RequiresPermission("cms:category:edit")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        categoryService.updateStatus(id, status);
        return Result.success();
    }
}
