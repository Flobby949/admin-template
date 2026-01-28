package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
    void deleteByUserId(Long userId);

    /**
     * 删除部门的所有用户关联
     */
    void deleteByDeptId(Long deptId);
}
