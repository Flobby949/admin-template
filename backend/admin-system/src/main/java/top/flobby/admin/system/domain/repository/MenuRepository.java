package top.flobby.admin.system.domain.repository;

import top.flobby.admin.system.domain.entity.Menu;

import java.util.List;
import java.util.Optional;

/**
 * 菜单仓储接口
 */
public interface MenuRepository {

    /**
     * 根据ID查询菜单
     */
    Optional<Menu> findById(Long id);

    /**
     * 查询所有菜单（未删除）
     */
    List<Menu> findAll();

    /**
     * 查询所有启用的菜单
     */
    List<Menu> findAllEnabled();

    /**
     * 根据父ID查询子菜单
     */
    List<Menu> findByParentId(Long parentId);

    /**
     * 根据角色ID列表查询菜单
     */
    List<Menu> findByRoleIds(List<Long> roleIds);

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> findMenuIdsByRoleId(Long roleId);

    /**
     * 保存菜单
     */
    Menu save(Menu menu);

    /**
     * 删除菜单（逻辑删除）
     */
    void deleteById(Long id);

    /**
     * 检查是否有子菜单
     */
    boolean hasChildren(Long parentId);

    /**
     * 根据权限标识查询菜单
     */
    Optional<Menu> findByPermission(String permission);
}
