package top.flobby.admin.system.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.flobby.admin.system.domain.entity.UserRole;
import top.flobby.admin.system.domain.repository.UserRoleRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色关联仓储实现
 *
 * @author Flobby
 * @date 2026-01-26
 */
@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final JpaUserRoleRepository jpaUserRoleRepository;

    @Override
    public UserRole save(UserRole userRole) {
        return jpaUserRoleRepository.save(userRole);
    }

    @Override
    public List<UserRole> saveAll(List<UserRole> userRoles) {
        return jpaUserRoleRepository.saveAll(userRoles);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaUserRoleRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        jpaUserRoleRepository.deleteByRoleId(roleId);
    }

    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        return jpaUserRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findUserIdsByRoleId(Long roleId) {
        return jpaUserRoleRepository.findByRoleId(roleId).stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());
    }
}
