package top.flobby.admin.system.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.flobby.admin.system.domain.entity.Role;
import top.flobby.admin.system.domain.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓储实现
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final JpaRoleRepository jpaRoleRepository;

    @Override
    public Optional<Role> findById(Long id) {
        return jpaRoleRepository.findById(id)
                .filter(role -> role.getDeleted() == 0);
    }

    @Override
    public Optional<Role> findByRoleCode(String roleCode) {
        return Optional.ofNullable(jpaRoleRepository.findByRoleCodeAndDeleted(roleCode, 0));
    }

    @Override
    public List<Role> findAllEnabled() {
        return jpaRoleRepository.findByStatusAndDeleted(1, 0);
    }

    @Override
    public List<Role> findAll() {
        return jpaRoleRepository.findByDeleted(0);
    }

    @Override
    public List<Role> findByUserId(Long userId) {
        return jpaRoleRepository.findByUserId(userId);
    }

    @Override
    public Role save(Role role) {
        return jpaRoleRepository.save(role);
    }

    @Override
    public void deleteById(Long id) {
        jpaRoleRepository.softDeleteById(id);
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        return jpaRoleRepository.existsByRoleCodeAndDeleted(roleCode, 0);
    }

    @Override
    public boolean existsByRoleCodeAndIdNot(String roleCode, Long id) {
        return jpaRoleRepository.existsByRoleCodeAndIdNotAndDeleted(roleCode, id, 0);
    }
}
