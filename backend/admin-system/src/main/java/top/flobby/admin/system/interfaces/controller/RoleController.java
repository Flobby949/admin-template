package top.flobby.admin.system.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.common.annotation.RequiresPermission;
import top.flobby.admin.common.core.Result;
import top.flobby.admin.system.application.RoleService;
import top.flobby.admin.system.interfaces.dto.RoleDTO;
import top.flobby.admin.system.interfaces.vo.MenuTreeVO;
import top.flobby.admin.system.interfaces.vo.RoleVO;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取角色列表")
    @GetMapping
    @RequiresPermission("system:role:list")
    public Result<List<RoleVO>> list() {
        List<RoleVO> roles = roleService.listRoles();
        return Result.success(roles);
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @RequiresPermission("system:role:query")
    public Result<RoleVO> getById(
            @Parameter(description = "角色ID") @PathVariable("id") Long id) {
        RoleVO role = roleService.getRoleById(id);
        return Result.success(role);
    }

    @Operation(summary = "创建角色")
    @PostMapping
    @RequiresPermission("system:role:add")
    public Result<Long> create(@Valid @RequestBody RoleDTO dto) {
        Long id = roleService.createRole(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @RequiresPermission("system:role:edit")
    public Result<Void> update(
            @Parameter(description = "角色ID") @PathVariable("id") Long id,
            @Valid @RequestBody RoleDTO dto) {
        dto.setId(id);
        roleService.updateRole(dto);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @RequiresPermission("system:role:delete")
    public Result<Void> delete(
            @Parameter(description = "角色ID") @PathVariable("id") Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "修改角色状态")
    @PutMapping("/{id}/status")
    @RequiresPermission("system:role:edit")
    public Result<Void> updateStatus(
            @Parameter(description = "角色ID") @PathVariable("id") Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam("status") Integer status) {
        roleService.updateRoleStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "获取角色菜单ID列表")
    @GetMapping("/{id}/menus")
    @RequiresPermission("system:role:query")
    public Result<List<Long>> getRoleMenuIds(
            @Parameter(description = "角色ID") @PathVariable("id") Long id) {
        List<Long> menuIds = roleService.getRoleMenuIds(id);
        return Result.success(menuIds);
    }

    @Operation(summary = "获取菜单树")
    @GetMapping("/menus/tree")
    @RequiresPermission("system:role:list")
    public Result<List<MenuTreeVO>> getMenuTree() {
        List<MenuTreeVO> tree = roleService.getMenuTree();
        return Result.success(tree);
    }

    @Operation(summary = "获取启用的角色列表（下拉选择）")
    @GetMapping("/enabled")
    public Result<List<RoleVO>> listEnabled() {
        List<RoleVO> roles = roleService.listEnabledRoles();
        return Result.success(roles);
    }
}
