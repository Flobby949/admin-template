package top.flobby.admin.system.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.flobby.admin.system.domain.entity.Menu;
import top.flobby.admin.system.domain.repository.MenuRepository;

import java.util.List;
import java.util.Optional;

/**
 * 菜单仓储实现
 */
@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepository {

    private final JpaMenuRepository jpaMenuRepository;

    @Override
    public Optional<Menu> findById(Long id) {
        return jpaMenuRepository.findById(id)
                .filter(menu -> menu.getDeleted() == 0);
    }

    @Override
    public List<Menu> findAll() {
        return jpaMenuRepository.findByDeleted(0);
    }

    @Override
    public List<Menu> findAllEnabled() {
        return jpaMenuRepository.findByStatusAndDeleted(1, 0);
    }

    @Override
    public List<Menu> findByParentId(Long parentId) {
        return jpaMenuRepository.findByParentIdAndDeleted(parentId, 0);
    }

    @Override
    public List<Menu> findByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return jpaMenuRepository.findByRoleIds(roleIds);
    }

    @Override
    public List<Long> findMenuIdsByRoleId(Long roleId) {
        return jpaMenuRepository.findMenuIdsByRoleId(roleId);
    }

    @Override
    public Menu save(Menu menu) {
        return jpaMenuRepository.save(menu);
    }

    @Override
    public void deleteById(Long id) {
        jpaMenuRepository.softDeleteById(id);
    }

    @Override
    public boolean hasChildren(Long parentId) {
        return jpaMenuRepository.existsByParentIdAndDeleted(parentId, 0);
    }

    @Override
    public Optional<Menu> findByPermission(String permission) {
        return Optional.ofNullable(jpaMenuRepository.findByPermissionAndDeleted(permission, 0));
    }
}
