package top.flobby.admin.system.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.flobby.admin.system.domain.entity.Menu;
import top.flobby.admin.system.domain.repository.MenuRepository;
import top.flobby.admin.system.infrastructure.repository.JpaRoleMenuRepository;
import top.flobby.admin.system.interfaces.dto.MenuDTO;
import top.flobby.admin.system.interfaces.vo.MenuTreeVO;
import top.flobby.admin.system.interfaces.vo.RouterVO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单应用服务
 * <p>
 * 职责：
 * - 菜单业务逻辑编排
 * - 树形结构构建
 * - 删除前置检查
 * - 事务管理
 * <p>
 * 使用场景：
 * - 菜单管理界面的后端服务
 * - 角色权限分配时的菜单树查询
 * - 用户登录后的菜单权限加载
 *
 * @author flobby
 * @date 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final JpaRoleMenuRepository jpaRoleMenuRepository;

    /**
     * 获取菜单树
     * <p>
     * 查询所有菜单并构建树形结构，用于菜单管理页面展示
     *
     * @return 菜单树列表
     */
    public List<MenuTreeVO> listMenuTree() {
        List<Menu> menus = menuRepository.findAll();
        return buildMenuTree(menus, 0L);
    }

    /**
     * 新增菜单
     * <p>
     * 创建新菜单，包含参数验证和权限标识唯一性校验
     *
     * @param dto 菜单数据传输对象
     * @return 创建的菜单实体
     * @throws IllegalArgumentException 参数验证失败或权限标识重复
     */
    @Transactional
    public Menu createMenu(MenuDTO dto) {
        // 参数验证
        validateMenuDTO(dto);

        // 权限标识唯一性校验
        if (StringUtils.hasText(dto.getPermission())) {
            menuRepository.findByPermission(dto.getPermission())
                    .ifPresent(m -> {
                        throw new IllegalArgumentException("权限标识已存在: " + dto.getPermission());
                    });
        }

        // DTO 转实体
        Menu menu = new Menu();
        menu.setParentId(dto.getParentId());
        menu.setMenuName(dto.getMenuName());
        menu.setMenuType(dto.getMenuType());
        menu.setRoutePath(dto.getRoutePath());
        menu.setComponent(dto.getComponent());
        menu.setPermission(dto.getPermission());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSortOrder());
        menu.setVisible(dto.getVisible());
        menu.setStatus(dto.getStatus());

        // 保存菜单
        Menu savedMenu = menuRepository.save(menu);
        log.info("创建菜单成功: id={}, name={}, type={}", savedMenu.getId(), savedMenu.getMenuName(), savedMenu.getMenuType());

        return savedMenu;
    }

    /**
     * 根据ID获取菜单
     * <p>
     * 查询单个菜单详情，用于编辑时数据回显
     *
     * @param id 菜单ID
     * @return 菜单实体
     * @throws IllegalArgumentException 菜单不存在
     */
    public Menu getMenuById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + id));
    }

    /**
     * 更新菜单
     * <p>
     * 更新菜单信息，包含参数验证和权限标识唯一性校验
     *
     * @param id  菜单ID
     * @param dto 菜单数据传输对象
     * @return 更新后的菜单实体
     * @throws IllegalArgumentException 参数验证失败、菜单不存在或权限标识重复
     */
    @Transactional
    public Menu updateMenu(Long id, MenuDTO dto) {
        // 检查菜单是否存在
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + id));

        // 参数验证
        validateMenuDTO(dto);

        // 权限标识唯一性校验（排除当前菜单）
        if (StringUtils.hasText(dto.getPermission())) {
            menuRepository.findByPermission(dto.getPermission())
                    .ifPresent(m -> {
                        if (!m.getId().equals(id)) {
                            throw new IllegalArgumentException("权限标识已存在: " + dto.getPermission());
                        }
                    });
        }

        // 更新菜单信息
        menu.setParentId(dto.getParentId());
        menu.setMenuName(dto.getMenuName());
        menu.setMenuType(dto.getMenuType());
        menu.setRoutePath(dto.getRoutePath());
        menu.setComponent(dto.getComponent());
        menu.setPermission(dto.getPermission());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSortOrder());
        menu.setVisible(dto.getVisible());
        menu.setStatus(dto.getStatus());

        // 保存更新
        Menu updatedMenu = menuRepository.save(menu);
        log.info("更新菜单成功: id={}, name={}, type={}", updatedMenu.getId(), updatedMenu.getMenuName(), updatedMenu.getMenuType());

        return updatedMenu;
    }

    /**
     * 删除菜单
     * <p>
     * 删除菜单前进行两层检查：
     * 1. 检查是否有子菜单
     * 2. 检查是否被角色使用
     *
     * @param id 菜单ID
     * @throws IllegalArgumentException 菜单不存在、有子菜单或被角色使用
     */
    @Transactional
    public void deleteMenu(Long id) {
        // 检查菜单是否存在
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + id));

        // 检查是否有子菜单
        if (menuRepository.hasChildren(id)) {
            throw new IllegalArgumentException("该菜单下存在子菜单，无法删除");
        }

        // 检查是否被角色使用
        if (jpaRoleMenuRepository.existsByMenuId(id)) {
            throw new IllegalArgumentException("该菜单已被角色使用，无法删除");
        }

        // 执行删除（逻辑删除）
        menuRepository.deleteById(id);
        log.info("删除菜单成功: id={}, name={}", id, menu.getMenuName());
    }

    /**
     * 更新菜单状态
     * <p>
     * 快速切换菜单的启用/禁用状态
     *
     * @param id     菜单ID
     * @param status 状态值（0-禁用，1-启用）
     * @throws IllegalArgumentException 菜单不存在
     */
    @Transactional
    public void updateMenuStatus(Long id, Integer status) {
        // 检查菜单是否存在
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + id));

        // 更新状态
        menu.setStatus(status);
        menuRepository.save(menu);
        log.info("更新菜单状态成功: id={}, name={}, status={}", id, menu.getMenuName(), status);
    }

    /**
     * 验证菜单 DTO
     * <p>
     * 根据菜单类型验证必填字段
     *
     * @param dto 菜单数据传输对象
     * @throws IllegalArgumentException 验证失败
     */
    private void validateMenuDTO(MenuDTO dto) {
        // 菜单类型为菜单(2)时，routePath 必填
        if (dto.getMenuType() == 2 && !StringUtils.hasText(dto.getRoutePath())) {
            throw new IllegalArgumentException("菜单类型为菜单时，路由路径不能为空");
        }

        // 菜单类型为按钮(3)时，permission 必填
        if (dto.getMenuType() == 3 && !StringUtils.hasText(dto.getPermission())) {
            throw new IllegalArgumentException("菜单类型为按钮时，权限标识不能为空");
        }
    }

    /**
     * 构建菜单树
     * <p>
     * 递归构建父子关系，按 sortOrder 排序，空子节点设为 null
     * <p>
     * 参考 RoleService 中的实现
     *
     * @param menus    菜单列表
     * @param parentId 父菜单ID
     * @return 菜单树列表
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
     * 根据用户菜单ID列表构建动态路由
     * <p>
     * 将菜单数据转换为前端 Vue Router 格式，用于动态路由加载
     * <p>
     * 转换规则：
     * - 目录类型(1)：component 为 Layout，有 children
     * - 菜单类型(2)：component 为实际组件路径
     * - 按钮类型(3)：不生成路由，仅作为权限标识
     *
     * @param menuIds 用户拥有的菜单ID列表
     * @return 路由列表
     */
    public List<RouterVO> buildRoutersByMenuIds(Set<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询用户拥有的菜单（排除按钮类型，只保留目录和菜单）
        List<Menu> menus = menuRepository.findAll().stream()
                .filter(m -> menuIds.contains(m.getId()))
                .filter(m -> m.getMenuType() != 3) // 排除按钮
                .filter(m -> m.getStatus() == 1) // 只保留启用的
                .collect(Collectors.toList());

        return buildRouterTree(menus, 0L);
    }

    /**
     * 根据用户ID构建动态路由
     * <p>
     * 查询用户拥有的所有菜单并转换为路由格式
     *
     * @param userId 用户ID
     * @return 路由列表
     */
    public List<RouterVO> buildRoutersByUserId(Long userId) {
        // 查询用户拥有的菜单ID
        Set<Long> menuIds = jpaRoleMenuRepository.findMenuIdsByUserId(userId);
        return buildRoutersByMenuIds(menuIds);
    }

    /**
     * 递归构建路由树
     *
     * @param menus    菜单列表
     * @param parentId 父菜单ID
     * @return 路由树列表
     */
    private List<RouterVO> buildRouterTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .sorted(Comparator.comparing(Menu::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(m -> {
                    RouterVO router = new RouterVO();

                    // 设置路由路径
                    if (parentId == 0L) {
                        // 顶级菜单，路径以 / 开头
                        router.setPath(m.getRoutePath().startsWith("/") ? m.getRoutePath() : "/" + m.getRoutePath());
                    } else {
                        // 子菜单，相对路径
                        router.setPath(m.getRoutePath());
                    }

                    // 设置路由名称（使用菜单名称的驼峰形式）
                    router.setName(generateRouteName(m.getMenuName()));

                    // 设置组件路径
                    if (m.getMenuType() == 1) {
                        // 目录类型使用 Layout
                        router.setComponent("Layout");
                    } else {
                        // 菜单类型使用实际组件路径
                        router.setComponent(m.getComponent());
                    }

                    // 设置元信息
                    RouterVO.MetaVO meta = RouterVO.MetaVO.builder()
                            .title(m.getMenuName())
                            .icon(m.getIcon())
                            .hidden(m.getVisible() == 0)
                            .keepAlive(true)
                            .build();

                    // 如果有权限标识，添加到 meta
                    if (StringUtils.hasText(m.getPermission())) {
                        meta.setPermissions(Collections.singletonList(m.getPermission()));
                    }

                    router.setMeta(meta);

                    // 递归构建子路由
                    List<RouterVO> children = buildRouterTree(menus, m.getId());
                    if (!children.isEmpty()) {
                        router.setChildren(children);
                        // 目录类型设置重定向到第一个子菜单
                        if (m.getMenuType() == 1 && !children.isEmpty()) {
                            String firstChildPath = children.get(0).getPath();
                            router.setRedirect(router.getPath() + "/" + firstChildPath);
                        }
                    }

                    return router;
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成路由名称
     * <p>
     * 将中文菜单名称转换为英文路由名称
     * 简单实现：使用菜单ID的哈希值作为唯一标识
     *
     * @param menuName 菜单名称
     * @return 路由名称
     */
    private String generateRouteName(String menuName) {
        // 简单实现：移除空格，首字母大写
        if (menuName == null || menuName.isEmpty()) {
            return "Route" + System.currentTimeMillis();
        }
        // 对于中文名称，使用拼音首字母或直接使用名称的哈希
        return "Route" + Math.abs(menuName.hashCode());
    }
}
