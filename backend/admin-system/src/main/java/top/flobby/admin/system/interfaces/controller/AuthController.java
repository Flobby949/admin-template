package top.flobby.admin.system.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.common.core.Result;
import top.flobby.admin.system.application.AuthService;
import top.flobby.admin.system.interfaces.dto.LoginDTO;
import top.flobby.admin.system.interfaces.vo.LoginVO;
import top.flobby.admin.system.interfaces.vo.RouterVO;
import top.flobby.admin.system.interfaces.vo.UserInfoVO;

import java.util.List;
import java.util.Set;

@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getInfo() {
        return Result.success(authService.getUserInfo());
    }

    @Operation(summary = "获取用户路由")
    @GetMapping("/routers")
    public Result<List<RouterVO>> getRouters() {
        return Result.success(authService.getUserRouters());
    }

    @Operation(summary = "获取用户权限列表")
    @GetMapping("/permissions")
    public Result<Set<String>> getPermissions() {
        return Result.success(authService.getCurrentUserPermissions());
    }
}
