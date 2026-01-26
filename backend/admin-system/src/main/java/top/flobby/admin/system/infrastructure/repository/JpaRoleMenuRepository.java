package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.system.domain.entity.RoleMenu;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA 角色菜单关联仓储
 */
public interface JpaRoleMenuRepository extends JpaRepository<RoleMenu, Long> {

    List<RoleMenu> findByRoleId(Long roleId);

    List<RoleMenu> findByMenuId(Long menuId);

    @Modifying
    @Query("DELETE FROM RoleMenu rm WHERE rm.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query("DELETE FROM RoleMenu rm WHERE rm.menuId = :menuId")
    void deleteByMenuId(@Param("menuId") Long menuId);

    boolean existsByMenuId(Long menuId);

    /**
     * 根据用户ID查询用户拥有的所有菜单ID
     * <p>
     * 通过用户角色关联表和角色菜单关联表联合查询
     *
     * @param userId 用户ID
     * @return 菜单ID集合
     */
    @Query("SELECT DISTINCT rm.menuId FROM RoleMenu rm " +
           "WHERE rm.roleId IN (SELECT ur.roleId FROM UserRole ur WHERE ur.userId = :userId)")
    Set<Long> findMenuIdsByUserId(@Param("userId") Long userId);
}
