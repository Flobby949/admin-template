package top.flobby.admin.system.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import top.flobby.admin.common.exception.BusinessException;
import top.flobby.admin.common.utils.JwtUtils;
import top.flobby.admin.system.domain.entity.User;
import top.flobby.admin.system.domain.repository.UserRepository;
import top.flobby.admin.system.interfaces.dto.LoginDTO;
import top.flobby.admin.system.interfaces.vo.LoginVO;
import top.flobby.admin.system.interfaces.vo.RouterVO;
import top.flobby.admin.system.interfaces.vo.UserInfoVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final LoginLockService loginLockService;
    private final UserRepository userRepository;
    private final PermissionCacheService permissionCacheService;
    private final MenuService menuService;

    /**
     * 登录
     */
    public LoginVO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();

        // 检查是否被锁定
        loginLockService.checkLock(username);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginDTO.getPassword())
            );

            // 登录成功清除锁定记录
            loginLockService.clearLock(username);

            // 生成 Token
            String token = jwtUtils.generateToken(username);
            return LoginVO.builder().token(token).build();

        } catch (BadCredentialsException e) {
            // 记录登录失败
            loginLockService.recordFail(username);
            throw new BusinessException("用户名或密码错误");
        }
    }

    /**
     * 获取当前用户信息
     */
    public UserInfoVO getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 获取用户角色编码
        Set<String> roleCodes = permissionCacheService.getUserRoleCodes(user.getId());

        return UserInfoVO.builder()
                .name(user.getRealName() != null ? user.getRealName() : user.getUsername())
                .avatar(user.getAvatar())
                .roles(new ArrayList<>(roleCodes.isEmpty() ? Set.of("user") : roleCodes))
                .build();
    }

    /**
     * 获取当前用户权限列表
     */
    public Set<String> getCurrentUserPermissions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        return permissionCacheService.getUserPermissions(user.getId());
    }

    /**
     * 获取当前用户路由
     * <p>
     * 根据用户角色权限动态生成前端路由
     *
     * @return 路由列表
     */
    public List<RouterVO> getUserRouters() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        return menuService.buildRoutersByUserId(user.getId());
    }
}