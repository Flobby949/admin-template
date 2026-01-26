package top.flobby.admin.system.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.common.core.Result;
import top.flobby.admin.system.application.MenuService;
import top.flobby.admin.system.domain.entity.Menu;
import top.flobby.admin.system.interfaces.dto.MenuDTO;
import top.flobby.admin.system.interfaces.vo.MenuTreeVO;

import java.util.List;

/**
 * 菜单管理控制器
 * <p>
 * 职责：
 * - 处理菜单管理的 HTTP 请求
 * - 参数验证和 DTO 转换
 * - 统一异常处理
 * <p>
 * 权限控制：
 * - 所有接口仅超级管理员可访问
 * - 使用 @PreAuthorize("hasRole('ADMIN')") 注解
 *
 * @author flobby
 * @date 2026-01-26
 */
@Tag(name = "菜单管理", description = "菜单管理相关接口")
@RestController
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取菜单树
     * <p>
     * 查询所有菜单并以树形结构返回，用于菜单管理页面展示
     *
     * @return 菜单树列表
     */
    @Operation(summary = "获取菜单树", description = "查询所有菜单并以树形结构返回")
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> listMenuTree() {
        List<MenuTreeVO> tree = menuService.listMenuTree();
        return Result.success(tree);
    }

    /**
     * 新增菜单
     * <p>
     * 创建新菜单，支持三种类型（目录/菜单/按钮）
     *
     * @param dto 菜单数据传输对象
     * @return 创建的菜单信息
     */
    @Operation(summary = "新增菜单", description = "创建新菜单，支持三种类型（目录/菜单/按钮）")
    @PostMapping
    public Result<Menu> createMenu(@Valid @RequestBody MenuDTO dto) {
        Menu menu = menuService.createMenu(dto);
        return Result.success(menu);
    }

    /**
     * 获取菜单详情
     * <p>
     * 根据ID查询单个菜单信息，用于编辑时数据回显
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    @Operation(summary = "获取菜单详情", description = "根据ID查询单个菜单信息")
    @GetMapping("/{id}")
    public Result<Menu> getMenuById(@PathVariable Long id) {
        Menu menu = menuService.getMenuById(id);
        return Result.success(menu);
    }

    /**
     * 更新菜单
     * <p>
     * 更新菜单信息，支持修改所有字段
     *
     * @param id  菜单ID
     * @param dto 菜单数据传输对象
     * @return 更新后的菜单信息
     */
    @Operation(summary = "更新菜单", description = "更新菜单信息，支持修改所有字段")
    @PutMapping("/{id}")
    public Result<Menu> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuDTO dto) {
        Menu menu = menuService.updateMenu(id, dto);
        return Result.success(menu);
    }

    /**
     * 删除菜单
     * <p>
     * 删除菜单，包含前置检查（子菜单、角色关联）
     *
     * @param id 菜单ID
     * @return 删除结果
     */
    @Operation(summary = "删除菜单", description = "删除菜单，包含前置检查（子菜单、角色关联）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }

    /**
     * 更新菜单状态
     * <p>
     * 快速切换菜单的启用/禁用状态
     *
     * @param id     菜单ID
     * @param status 状态值（0-禁用，1-启用）
     * @return 更新结果
     */
    @Operation(summary = "更新菜单状态", description = "快速切换菜单的启用/禁用状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateMenuStatus(@PathVariable Long id, @RequestParam Integer status) {
        menuService.updateMenuStatus(id, status);
        return Result.success();
    }
}
