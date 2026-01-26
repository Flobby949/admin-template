package top.flobby.admin.system.domain.repository;

import top.flobby.admin.system.domain.entity.UserRole;

import java.util.List;

/**
 * 用户角色关联仓储接口
 *
 * @author Flobby
 * @date 2026-01-26
 */
public interface UserRoleRepository {

    /**
     * 保存用户角色关联
     */
    UserRole save(UserRole userRole);

    /**
     * 批量保存用户角色关联
     */
    List<UserRole> saveAll(List<UserRole> userRoles);

    /**
     * 根据用户ID删除关联
     */
    void deleteByUserId(Long userId);

    /**
     * 根据角色ID删除关联
     */
    void deleteByRoleId(Long roleId);

    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> findRoleIdsByUserId(Long userId);

    /**
     * 根据角色ID查询用户ID列表
     */
    List<Long> findUserIdsByRoleId(Long roleId);
}
