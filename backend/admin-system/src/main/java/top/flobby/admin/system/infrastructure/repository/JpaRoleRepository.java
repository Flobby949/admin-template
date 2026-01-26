package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.system.domain.entity.Role;

import java.util.List;

/**
 * Spring Data JPA 角色仓储
 */
public interface JpaRoleRepository extends JpaRepository<Role, Long> {

    Role findByRoleCodeAndDeleted(String roleCode, Integer deleted);

    List<Role> findByStatusAndDeleted(Integer status, Integer deleted);

    List<Role> findByDeleted(Integer deleted);

    @Query("SELECT r FROM Role r JOIN UserRole ur ON r.id = ur.roleId " +
           "WHERE ur.userId = :userId AND r.deleted = 0")
    List<Role> findByUserId(@Param("userId") Long userId);

    boolean existsByRoleCodeAndDeleted(String roleCode, Integer deleted);

    boolean existsByRoleCodeAndIdNotAndDeleted(String roleCode, Long id, Integer deleted);

    @Modifying
    @Query("UPDATE Role r SET r.deleted = 1 WHERE r.id = :id")
    void softDeleteById(@Param("id") Long id);
}
