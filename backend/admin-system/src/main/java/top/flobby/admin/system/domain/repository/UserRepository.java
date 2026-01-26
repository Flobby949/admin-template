package top.flobby.admin.system.domain.repository;

import top.flobby.admin.system.domain.entity.User;

import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface UserRepository {
    Optional<User> findByUsername(String username);
    User save(User user);
}
