package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.system.domain.entity.RoleMenu;

import java.util.List;

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
}
