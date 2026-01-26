package top.flobby.admin.system.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.flobby.admin.common.exception.BusinessException;
import top.flobby.admin.system.domain.entity.Menu;
import top.flobby.admin.system.domain.entity.Role;
import top.flobby.admin.system.domain.entity.RoleMenu;
import top.flobby.admin.system.domain.repository.MenuRepository;
import top.flobby.admin.system.domain.repository.RoleRepository;
import top.flobby.admin.system.infrastructure.repository.JpaRoleMenuRepository;
import top.flobby.admin.system.infrastructure.repository.JpaUserRoleRepository;
import top.flobby.admin.system.interfaces.dto.RoleDTO;
import top.flobby.admin.system.interfaces.vo.MenuTreeVO;
import top.flobby.admin.system.interfaces.vo.RoleVO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final JpaRoleMenuRepository jpaRoleMenuRepository;
    private final JpaUserRoleRepository jpaUserRoleRepository;
    private final PermissionCacheService permissionCacheService;

    /**
     * 获取角色列表
     */
    public List<RoleVO> listRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::toRoleVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取角色详情
     */
    public RoleVO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        RoleVO vo = toRoleVO(role);
        // 加载菜单ID列表
        List<Long> menuIds = menuRepository.findMenuIdsByRoleId(id);
        vo.setMenuIds(menuIds);

        return vo;
    }

    /**
     * 创建角色
     */
    @Transactional
    public Long createRole(RoleDTO dto) {
        // 检查角色编码是否存在
        if (roleRepository.existsByRoleCode(dto.getRoleCode())) {
            throw new BusinessException("角色编码已存在");
        }

        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDataScope(dto.getDataScope() != null ? dto.getDataScope() : 1);
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        role.setRemark(dto.getRemark());
        role.setDeleted(0);

        Role savedRole = roleRepository.save(role);

        // 保存角色菜单关联
        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
            saveRoleMenus(savedRole.getId(), dto.getMenuIds());
        }

        log.info("创建角色成功: id={}, code={}", savedRole.getId(), savedRole.getRoleCode());
        return savedRole.getId();
    }

    /**
     * 更新角色
     */
    @Transactional
    public void updateRole(RoleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("角色ID不能为空");
        }

        Role role = roleRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException("角色不存在"));

        // 检查角色编码是否被其他角色使用
        if (roleRepository.existsByRoleCodeAndIdNot(dto.getRoleCode(), dto.getId())) {
            throw new BusinessException("角色编码已存在");
        }

        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        if (dto.getDataScope() != null) {
            role.setDataScope(dto.getDataScope());
        }
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        role.setRemark(dto.getRemark());

        roleRepository.save(role);

        // 更新角色菜单关联
        jpaRoleMenuRepository.deleteByRoleId(dto.getId());
        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
            saveRoleMenus(dto.getId(), dto.getMenuIds());
        }

        // 清除相关用户的权限缓存
        permissionCacheService.clearRoleUsersCache(dto.getId());

        log.info("更新角色成功: id={}", dto.getId());
    }

    /**
     * 删除角色
     */
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        // 检查是否有用户使用该角色
        if (jpaUserRoleRepository.existsByRoleId(id)) {
            throw new BusinessException("该角色已分配给用户，无法删除");
        }

        // 删除角色菜单关联
        jpaRoleMenuRepository.deleteByRoleId(id);

        // 逻辑删除角色
        roleRepository.deleteById(id);

        // 清除相关用户的权限缓存
        permissionCacheService.clearRoleUsersCache(id);

        log.info("删除角色成功: id={}", id);
    }

    /**
     * 修改角色状态
     */
    @Transactional
    public void updateRoleStatus(Long id, Integer status) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        role.setStatus(status);
        roleRepository.save(role);

        // 清除相关用户的权限缓存
        permissionCacheService.clearRoleUsersCache(id);

        log.info("修改角色状态成功: id={}, status={}", id, status);
    }

    /**
     * 获取角色菜单ID列表
     */
    public List<Long> getRoleMenuIds(Long roleId) {
        return menuRepository.findMenuIdsByRoleId(roleId);
    }

    /**
     * 获取菜单树
     */
    public List<MenuTreeVO> getMenuTree() {
        List<Menu> menus = menuRepository.findAllEnabled();
        return buildMenuTree(menus, 0L);
    }

    /**
     * 获取所有启用的角色（用于下拉选择）
     */
    public List<RoleVO> listEnabledRoles() {
        List<Role> roles = roleRepository.findAllEnabled();
        return roles.stream()
                .map(this::toRoleVO)
                .collect(Collectors.toList());
    }

    /**
     * 保存角色菜单关联
     */
    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        List<RoleMenu> roleMenus = menuIds.stream()
                .map(menuId -> new RoleMenu(roleId, menuId))
                .collect(Collectors.toList());
        jpaRoleMenuRepository.saveAll(roleMenus);
    }

    /**
     * 构建菜单树
     */
    private List<MenuTreeVO> buildMenuTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .sorted(Comparator.comparing(Menu::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(m -> {
                    MenuTreeVO vo = MenuTreeVO.builder()
                            .id(m.getId())
                            .parentId(m.getParentId())
                            .menuName(m.getMenuName())
                            .menuType(m.getMenuType())
                            .routePath(m.getRoutePath())
                            .component(m.getComponent())
                            .permission(m.getPermission())
                            .icon(m.getIcon())
                            .sortOrder(m.getSortOrder())
                            .visible(m.getVisible())
                            .status(m.getStatus())
                            .children(buildMenuTree(menus, m.getId()))
                            .build();
                    if (vo.getChildren() != null && vo.getChildren().isEmpty()) {
                        vo.setChildren(null);
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 转换为 RoleVO
     */
    private RoleVO toRoleVO(Role role) {
        return RoleVO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .dataScope(role.getDataScope())
                .status(role.getStatus())
                .remark(role.getRemark())
                .createTime(role.getCreateTime())
                .updateTime(role.getUpdateTime())
                .build();
    }
}
