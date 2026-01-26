package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.system.domain.entity.Menu;

import java.util.List;

/**
 * Spring Data JPA 菜单仓储
 */
public interface JpaMenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByDeleted(Integer deleted);

    List<Menu> findByStatusAndDeleted(Integer status, Integer deleted);

    List<Menu> findByParentIdAndDeleted(Long parentId, Integer deleted);

    @Query("SELECT m FROM Menu m JOIN RoleMenu rm ON m.id = rm.menuId " +
           "WHERE rm.roleId IN :roleIds AND m.deleted = 0 AND m.status = 1 " +
           "ORDER BY m.sortOrder")
    List<Menu> findByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Query("SELECT rm.menuId FROM RoleMenu rm WHERE rm.roleId = :roleId")
    List<Long> findMenuIdsByRoleId(@Param("roleId") Long roleId);

    Menu findByPermissionAndDeleted(String permission, Integer deleted);

    boolean existsByParentIdAndDeleted(Long parentId, Integer deleted);

    @Modifying
    @Query("UPDATE Menu m SET m.deleted = 1 WHERE m.id = :id")
    void softDeleteById(@Param("id") Long id);
}
