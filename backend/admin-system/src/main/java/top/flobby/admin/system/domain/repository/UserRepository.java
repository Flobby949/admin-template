package top.flobby.admin.system.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import top.flobby.admin.system.domain.entity.User;
import top.flobby.admin.system.interfaces.query.UserQuery;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface UserRepository {
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据ID查找用户
     */
    Optional<User> findById(Long id);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 保存用户
     */
    User save(User user);

    /**
     * 删除用户（逻辑删除）
     */
    void deleteById(Long id);

    /**
     * 分页查询用户
     */
    Page<User> findByQuery(UserQuery query, Pageable pageable);

    /**
     * 根据角色ID查询用户列表
     */
    List<User> findByRoleId(Long roleId);

    /**
     * 根据部门ID查询用户列表
     * TODO: Phase 6 实现 - 需要 UserDept 实体
     */
    // List<User> findByDeptId(Long deptId);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);
}
