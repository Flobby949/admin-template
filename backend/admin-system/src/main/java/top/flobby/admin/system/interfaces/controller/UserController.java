package top.flobby.admin.system.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.common.annotation.OperLog;
import top.flobby.admin.common.annotation.OperLog.BusinessType;
import top.flobby.admin.common.core.PageResult;
import top.flobby.admin.common.core.Result;
import top.flobby.admin.system.application.UserService;
import top.flobby.admin.system.interfaces.dto.UserDTO;
import top.flobby.admin.system.interfaces.query.UserQuery;
import top.flobby.admin.system.interfaces.vo.UserVO;

import java.util.List;

/**
 * 用户管理控制器
 *
 * @author Flobby
 * @date 2026-01-26
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<UserVO>> list(@RequestBody UserQuery query) {
        PageResult<UserVO> result = userService.getUserList(query);
        return Result.success(result);
    }

    /**
     * 根据ID获取用户详情
     */
    @Operation(summary = "根据ID获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<UserVO> getById(@PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    @OperLog(title = "用户管理", businessType = BusinessType.INSERT)
    public Result<Long> create(@Valid @RequestBody UserDTO dto) {
        Long userId = userService.createUser(dto);
        return Result.success(userId);
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @OperLog(title = "用户管理", businessType = BusinessType.UPDATE)
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        dto.setId(id);
        userService.updateUser(dto);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:remove')")
    @OperLog(title = "用户管理", businessType = BusinessType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 批量删除用户
     */
    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:user:remove')")
    @OperLog(title = "用户管理", businessType = BusinessType.DELETE)
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        userService.batchDeleteUsers(ids);
        return Result.success();
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    @OperLog(title = "用户管理", businessType = BusinessType.UPDATE)
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }

    /**
     * 修改用户状态
     */
    @Operation(summary = "修改用户状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @OperLog(title = "用户管理", businessType = BusinessType.UPDATE)
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return Result.success();
    }
}
