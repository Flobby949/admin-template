package top.flobby.admin.system.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.flobby.admin.system.domain.entity.User;

/**
 * Spring Data JPA 用户仓储
 */
public interface JpaUserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
