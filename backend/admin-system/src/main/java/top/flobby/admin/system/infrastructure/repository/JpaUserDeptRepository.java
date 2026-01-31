package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import top.flobby.admin.system.domain.entity.UserDept;

import java.util.List;

/**
 * Spring Data JPA 用户部门关联仓储
 */
public interface JpaUserDeptRepository extends JpaRepository<UserDept, Long> {

    /**
     * 根据用户ID查询部门关联
     */
    List<UserDept> findByUserId(Long userId);

    /**
     * 根据用户ID列表批量查询部门关联
     */
    List<UserDept> findByUserIdIn(List<Long> userIds);

    /**
     * 根据部门ID查询用户关联
     */
    List<UserDept> findByDeptId(Long deptId);

    /**
     * 检查部门下是否有用户
     */
    boolean existsByDeptId(Long deptId);

    /**
     * 删除用户的所有部门关联
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserDept ud WHERE ud.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除部门的所有用户关联
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserDept ud WHERE ud.deptId = :deptId")
    void deleteByDeptId(@Param("deptId") Long deptId);
}
