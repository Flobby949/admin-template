package top.flobby.admin.system.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.flobby.admin.system.domain.entity.User;
import top.flobby.admin.system.domain.repository.UserRepository;

import java.util.Optional;

/**
 * 用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(jpaUserRepository.findByUsername(username));
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }
}
