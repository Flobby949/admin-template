package top.flobby.admin.system.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.common.annotation.OperLog;
import top.flobby.admin.common.core.Result;
import top.flobby.admin.system.application.DepartmentService;
import top.flobby.admin.system.interfaces.dto.DepartmentDTO;
import top.flobby.admin.system.interfaces.vo.DepartmentVO;

import java.util.List;

/**
 * 部门管理控制器
 * <p>
 * 职责:
 * - 处理部门管理的HTTP请求
 * - 参数验证和DTO转换
 * - 统一异常处理
 * <p>
 * 权限控制:
 * - 所有接口仅超级管理员可访问
 * - 使用 @PreAuthorize("hasRole('ADMIN')") 注解
 *
 * @author flobby
 * @date 2026-01-28
 */
@Tag(name = "部门管理", description = "部门管理相关接口")
@RestController
@RequestMapping("/api/system/departments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取部门树
     * <p>
     * 查询所有部门并以树形结构返回,用于部门管理页面展示和用户选择部门
     * 此接口允许所有已认证用户访问,覆盖类级别的ADMIN权限限制
     *
     * @return 部门树列表
     */
    @Operation(summary = "获取部门树", description = "查询所有部门并以树形结构返回")
    @GetMapping("/tree")
    @PreAuthorize("isAuthenticated()")
    public Result<List<DepartmentVO>> listDepartmentTree() {
        List<DepartmentVO> tree = departmentService.listDepartmentTree();
        return Result.success(tree);
    }

    /**
     * 获取部门详情
     * <p>
     * 根据部门ID查询部门详细信息
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @Operation(summary = "获取部门详情", description = "根据部门ID查询部门详细信息")
    @GetMapping("/{id}")
    public Result<DepartmentVO> getDepartmentById(
            @Parameter(description = "部门ID", required = true)
            @PathVariable Long id) {
        DepartmentVO vo = departmentService.getDepartmentById(id);
        return Result.success(vo);
    }

    /**
     * 创建部门
     * <p>
     * 创建新部门,自动计算ancestors路径和层级深度
     *
     * @param dto 部门DTO
     * @return 部门ID
     */
    @Operation(summary = "创建部门", description = "创建新部门")
    @PostMapping
    @OperLog(title = "部门管理", businessType = OperLog.BusinessType.INSERT)
    public Result<Long> createDepartment(
            @Parameter(description = "部门DTO", required = true)
            @Valid @RequestBody DepartmentDTO dto) {
        Long id = departmentService.createDepartment(dto);
        return Result.success(id);
    }

    /**
     * 更新部门
     * <p>
     * 更新部门信息,支持修改父部门(自动更新子孙部门的ancestors)
     *
     * @param id  部门ID
     * @param dto 部门DTO
     * @return 成功标识
     */
    @Operation(summary = "更新部门", description = "更新部门信息")
    @PutMapping("/{id}")
    @OperLog(title = "部门管理", businessType = OperLog.BusinessType.UPDATE)
    public Result<Void> updateDepartment(
            @Parameter(description = "部门ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "部门DTO", required = true)
            @Valid @RequestBody DepartmentDTO dto) {
        dto.setId(id);
        departmentService.updateDepartment(dto);
        return Result.success();
    }

    /**
     * 删除部门
     * <p>
     * 逻辑删除部门,删除前检查:
     * 1. 是否有子部门
     * 2. 是否有用户
     *
     * @param id 部门ID
     * @return 成功标识
     */
    @Operation(summary = "删除部门", description = "逻辑删除部门")
    @DeleteMapping("/{id}")
    @OperLog(title = "部门管理", businessType = OperLog.BusinessType.DELETE)
    public Result<Void> deleteDepartment(
            @Parameter(description = "部门ID", required = true)
            @PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }

    /**
     * 更新部门状态
     * <p>
     * 启用或禁用部门
     *
     * @param id     部门ID
     * @param status 状态:0-禁用,1-启用
     * @return 成功标识
     */
    @Operation(summary = "更新部门状态", description = "启用或禁用部门")
    @PatchMapping("/{id}/status")
    public Result<Void> updateDepartmentStatus(
            @Parameter(description = "部门ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "状态:0-禁用,1-启用", required = true)
            @RequestParam Integer status) {
        departmentService.updateDepartmentStatus(id, status);
        return Result.success();
    }
}
