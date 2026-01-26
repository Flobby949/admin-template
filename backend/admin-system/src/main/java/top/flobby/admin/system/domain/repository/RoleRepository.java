package top.flobby.admin.system.domain.repository;

import top.flobby.admin.system.domain.entity.Role;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓储接口
 */
public interface RoleRepository {

    /**
     * 根据ID查询角色
     */
    Optional<Role> findById(Long id);

    /**
     * 根据角色编码查询
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 查询所有启用的角色
     */
    List<Role> findAllEnabled();

    /**
     * 查询所有角色（未删除）
     */
    List<Role> findAll();

    /**
     * 根据用户ID查询角色列表
     */
    List<Role> findByUserId(Long userId);

    /**
     * 保存角色
     */
    Role save(Role role);

    /**
     * 删除角色（逻辑删除）
     */
    void deleteById(Long id);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色编码是否存在（排除指定ID）
     */
    boolean existsByRoleCodeAndIdNot(String roleCode, Long id);
}
