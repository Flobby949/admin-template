package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import top.flobby.admin.system.domain.entity.User;

import java.util.List;

/**
 * Spring Data JPA 用户仓储
 */
public interface JpaUserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    User findByEmail(String email);

    /**
     * 根据手机号查找用户
     */
    User findByPhone(String phone);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsernameAndDeletedEquals(String username, Integer deleted);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmailAndDeletedEquals(String email, Integer deleted);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhoneAndDeletedEquals(String phone, Integer deleted);

    /**
     * 根据角色ID查询用户列表
     */
    @Query("SELECT u FROM User u JOIN UserRole ur ON u.id = ur.userId WHERE ur.roleId = :roleId AND u.deleted = 0")
    List<User> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据部门ID查询用户列表
     * TODO: Phase 6 实现 - 需要 UserDept 实体
     */
    // @Query("SELECT u FROM User u JOIN UserDept ud ON u.id = ud.userId WHERE ud.deptId = :deptId AND u.deleted = 0")
    // List<User> findByDeptId(@Param("deptId") Long deptId);

    /**
     * 逻辑删除用户
     */
    @Modifying
    @Query("UPDATE User u SET u.deleted = 1 WHERE u.id = :id")
    void logicalDeleteById(@Param("id") Long id);
}
