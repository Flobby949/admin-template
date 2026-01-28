package top.flobby.admin.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import top.flobby.admin.common.annotation.DataScope;
import top.flobby.admin.common.context.DataScopeContext;
import top.flobby.admin.common.context.DataScopeInfo;
import top.flobby.admin.system.application.PermissionCacheService;
import top.flobby.admin.system.domain.entity.Department;
import top.flobby.admin.system.domain.entity.UserDept;
import top.flobby.admin.system.domain.repository.DepartmentRepository;
import top.flobby.admin.system.domain.repository.UserRepository;
import top.flobby.admin.system.infrastructure.repository.JpaUserDeptRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器
 * <p>
 * 使用 AOP 拦截 @DataScope 标注的方法，计算并设置数据权限上下文
 *
 * @author flobby
 * @date 2026-01-28
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeInterceptor {

    private final UserRepository userRepository;
    private final PermissionCacheService permissionCacheService;
    private final JpaUserDeptRepository userDeptRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * 拦截 @DataScope 标注的方法
     */
    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        // 如果数据权限未启用，直接执行方法
        if (!dataScope.enabled()) {
            return joinPoint.proceed();
        }

        try {
            // 1. 解析当前用户
            Long userId = resolveCurrentUserId();
            if (userId == null) {
                log.warn("无法解析当前用户ID，数据权限过滤将不生效");
                return joinPoint.proceed();
            }

            // 2. 获取用户的数据权限范围
            Integer dataScopeValue = permissionCacheService.getUserDataScope(userId);

            // 3. 计算允许访问的部门ID集合
            Set<Long> allowedDeptIds = resolveDeptIds(userId, dataScopeValue);

            // 4. 设置数据权限上下文
            DataScopeInfo info = new DataScopeInfo(userId, dataScopeValue, allowedDeptIds);
            DataScopeContext.set(info);

            log.debug("数据权限上下文已设置: userId={}, dataScope={}, deptIds={}",
                    userId, dataScopeValue, allowedDeptIds);

            // 5. 执行目标方法
            return joinPoint.proceed();

        } finally {
            // 6. 清除 ThreadLocal，避免内存泄漏
            DataScopeContext.clear();
        }
    }

    /**
     * 解析当前用户ID
     */
    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        if ("anonymousUser".equals(username)) {
            return null;
        }

        // 根据用户名查询用户ID
        return userRepository.findByUsername(username)
                .map(user -> user.getId())
                .orElse(null);
    }

    /**
     * 计算允许访问的部门ID集合
     *
     * @param userId    用户ID
     * @param dataScope 数据权限范围
     * @return 部门ID集合
     */
    private Set<Long> resolveDeptIds(Long userId, Integer dataScope) {
        // 1 - 全部数据：不需要部门过滤
        if (dataScope == 1) {
            return new HashSet<>();
        }

        // 4 - 仅本人：不需要部门过滤（通过用户ID过滤）
        if (dataScope == 4) {
            return new HashSet<>();
        }

        // 2 - 本部门及下级 或 3 - 仅本部门：需要计算部门ID集合
        if (dataScope == 2 || dataScope == 3) {
            // 查询用户所属的部门
            List<UserDept> userDepts = userDeptRepository.findByUserId(userId);
            if (userDepts.isEmpty()) {
                log.warn("用户 {} 未关联任何部门，数据权限过滤将返回空结果", userId);
                return new HashSet<>();
            }

            Set<Long> deptIds = userDepts.stream()
                    .map(UserDept::getDeptId)
                    .collect(Collectors.toSet());

            // 如果是"本部门及下级"，需要展开子部门
            if (dataScope == 2) {
                Set<Long> expandedDeptIds = new HashSet<>(deptIds);
                for (Long deptId : deptIds) {
                    expandedDeptIds.addAll(getDescendantDeptIds(deptId));
                }
                return expandedDeptIds;
            }

            // 如果是"仅本部门"，直接返回用户所属部门
            return deptIds;
        }

        // 其他情况（理论上不应该出现），返回空集合
        return new HashSet<>();
    }

    /**
     * 获取部门的所有子孙部门ID
     *
     * @param deptId 部门ID
     * @return 子孙部门ID集合（包含自身）
     */
    private Set<Long> getDescendantDeptIds(Long deptId) {
        Set<Long> result = new HashSet<>();
        result.add(deptId);

        // 查询当前部门
        Department dept = departmentRepository.findById(deptId).orElse(null);
        if (dept == null) {
            return result;
        }

        // 构建祖级路径前缀：ancestors + "," + deptId
        String ancestorsPrefix;
        if (dept.getAncestors() == null || dept.getAncestors().isEmpty()) {
            ancestorsPrefix = String.valueOf(deptId);
        } else {
            ancestorsPrefix = dept.getAncestors() + "," + deptId;
        }

        // 查询所有子孙部门
        List<Department> descendants = departmentRepository.findByAncestorsStartingWith(ancestorsPrefix);
        result.addAll(descendants.stream()
                .map(Department::getId)
                .collect(Collectors.toSet()));

        return result;
    }
}
