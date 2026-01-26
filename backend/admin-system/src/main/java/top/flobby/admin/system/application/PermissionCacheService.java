package top.flobby.admin.system.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.flobby.admin.system.domain.entity.Menu;
import top.flobby.admin.system.domain.entity.Role;
import top.flobby.admin.system.domain.repository.MenuRepository;
import top.flobby.admin.system.domain.repository.RoleRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限缓存服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private final StringRedisTemplate redisTemplate;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;

    private static final String PERMISSION_CACHE_PREFIX = "admin:permission:user:";
    private static final String ROLE_CACHE_PREFIX = "admin:role:user:";
    private static final long CACHE_EXPIRE_HOURS = 2;

    /**
     * 获取用户权限列表（带缓存）
     */
    public Set<String> getUserPermissions(Long userId) {
        String cacheKey = PERMISSION_CACHE_PREFIX + userId;

        // 尝试从缓存获取
        Set<String> cachedPermissions = redisTemplate.opsForSet().members(cacheKey);
        if (cachedPermissions != null && !cachedPermissions.isEmpty()) {
            return cachedPermissions;
        }

        // 从数据库加载
        Set<String> permissions = loadUserPermissions(userId);

        // 写入缓存
        if (!permissions.isEmpty()) {
            redisTemplate.opsForSet().add(cacheKey, permissions.toArray(new String[0]));
            redisTemplate.expire(cacheKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return permissions;
    }

    /**
     * 获取用户角色编码列表（带缓存）
     */
    public Set<String> getUserRoleCodes(Long userId) {
        String cacheKey = ROLE_CACHE_PREFIX + userId;

        // 尝试从缓存获取
        Set<String> cachedRoles = redisTemplate.opsForSet().members(cacheKey);
        if (cachedRoles != null && !cachedRoles.isEmpty()) {
            return cachedRoles;
        }

        // 从数据库加载
        List<Role> roles = roleRepository.findByUserId(userId);
        Set<String> roleCodes = roles.stream()
                .filter(Role::isEnabled)
                .map(Role::getRoleCode)
                .collect(Collectors.toSet());

        // 写入缓存
        if (!roleCodes.isEmpty()) {
            redisTemplate.opsForSet().add(cacheKey, roleCodes.toArray(new String[0]));
            redisTemplate.expire(cacheKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return roleCodes;
    }

    /**
     * 从数据库加载用户权限
     */
    private Set<String> loadUserPermissions(Long userId) {
        // 获取用户角色
        List<Role> roles = roleRepository.findByUserId(userId);
        if (roles.isEmpty()) {
            return Collections.emptySet();
        }

        // 过滤启用的角色
        List<Long> enabledRoleIds = roles.stream()
                .filter(Role::isEnabled)
                .map(Role::getId)
                .collect(Collectors.toList());

        if (enabledRoleIds.isEmpty()) {
            return Collections.emptySet();
        }

        // 获取角色对应的菜单
        List<Menu> menus = menuRepository.findByRoleIds(enabledRoleIds);

        // 提取权限标识
        return menus.stream()
                .map(Menu::getPermission)
                .filter(Objects::nonNull)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * 获取用户最大数据权限范围
     */
    public int getUserDataScope(Long userId) {
        List<Role> roles = roleRepository.findByUserId(userId);
        return roles.stream()
                .filter(Role::isEnabled)
                .map(Role::getDataScope)
                .min(Integer::compareTo)
                .orElse(4); // 默认仅本人
    }

    /**
     * 清除用户权限缓存
     */
    public void clearUserCache(Long userId) {
        redisTemplate.delete(PERMISSION_CACHE_PREFIX + userId);
        redisTemplate.delete(ROLE_CACHE_PREFIX + userId);
        log.info("清除用户权限缓存: userId={}", userId);
    }

    /**
     * 清除角色相关用户的权限缓存
     */
    public void clearRoleUsersCache(Long roleId) {
        // 这里需要查询拥有该角色的所有用户，然后清除他们的缓存
        // 简化实现：清除所有权限缓存
        Set<String> permissionKeys = redisTemplate.keys(PERMISSION_CACHE_PREFIX + "*");
        Set<String> roleKeys = redisTemplate.keys(ROLE_CACHE_PREFIX + "*");

        if (permissionKeys != null && !permissionKeys.isEmpty()) {
            redisTemplate.delete(permissionKeys);
        }
        if (roleKeys != null && !roleKeys.isEmpty()) {
            redisTemplate.delete(roleKeys);
        }

        log.info("清除角色相关用户权限缓存: roleId={}", roleId);
    }

    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(Long userId, String permission) {
        Set<String> permissions = getUserPermissions(userId);
        return permissions.contains(permission);
    }

    /**
     * 检查用户是否有任一权限
     */
    public boolean hasAnyPermission(Long userId, String... permissions) {
        Set<String> userPermissions = getUserPermissions(userId);
        return Arrays.stream(permissions).anyMatch(userPermissions::contains);
    }

    /**
     * 检查用户是否有所有权限
     */
    public boolean hasAllPermissions(Long userId, String... permissions) {
        Set<String> userPermissions = getUserPermissions(userId);
        return Arrays.stream(permissions).allMatch(userPermissions::contains);
    }

    /**
     * 检查用户是否有指定角色
     */
    public boolean hasRole(Long userId, String roleCode) {
        Set<String> roleCodes = getUserRoleCodes(userId);
        return roleCodes.contains(roleCode);
    }

    /**
     * 检查用户是否是管理员
     */
    public boolean isAdmin(Long userId) {
        return hasRole(userId, "admin");
    }
}
