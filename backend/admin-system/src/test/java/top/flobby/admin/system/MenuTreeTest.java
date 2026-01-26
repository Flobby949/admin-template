package top.flobby.admin.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.flobby.admin.system.domain.entity.Menu;
import top.flobby.admin.system.interfaces.vo.MenuTreeVO;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 菜单权限树构建测试
 */
@DisplayName("菜单权限树构建测试")
class MenuTreeTest {

    private List<Menu> allMenus;

    @BeforeEach
    void setUp() {
        allMenus = new ArrayList<>();

        // 一级菜单：系统管理
        Menu systemMenu = createMenu(1L, 0L, "系统管理", 1, "/system", "Layout", null, "setting", 1);
        allMenus.add(systemMenu);

        // 二级菜单：用户管理
        Menu userMenu = createMenu(2L, 1L, "用户管理", 2, "user", "system/user/index", "system:user:list", "user", 1);
        allMenus.add(userMenu);

        // 三级按钮：用户新增
        Menu userAddBtn = createMenu(3L, 2L, "用户新增", 3, null, null, "system:user:add", null, 1);
        allMenus.add(userAddBtn);

        // 三级按钮：用户编辑
        Menu userEditBtn = createMenu(4L, 2L, "用户编辑", 3, null, null, "system:user:edit", null, 2);
        allMenus.add(userEditBtn);

        // 三级按钮：用户删除
        Menu userDeleteBtn = createMenu(5L, 2L, "用户删除", 3, null, null, "system:user:delete", null, 3);
        allMenus.add(userDeleteBtn);

        // 二级菜单：角色管理
        Menu roleMenu = createMenu(6L, 1L, "角色管理", 2, "role", "system/role/index", "system:role:list", "peoples", 2);
        allMenus.add(roleMenu);

        // 三级按钮：角色新增
        Menu roleAddBtn = createMenu(7L, 6L, "角色新增", 3, null, null, "system:role:add", null, 1);
        allMenus.add(roleAddBtn);

        // 一级菜单：监控管理
        Menu monitorMenu = createMenu(8L, 0L, "监控管理", 1, "/monitor", "Layout", null, "monitor", 2);
        allMenus.add(monitorMenu);

        // 二级菜单：操作日志
        Menu operLogMenu = createMenu(9L, 8L, "操作日志", 2, "operlog", "monitor/operlog/index", "monitor:operlog:list", "form", 1);
        allMenus.add(operLogMenu);
    }

    private Menu createMenu(Long id, Long parentId, String name, Integer type,
                            String path, String component, String permission,
                            String icon, Integer sort) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuName(name);
        menu.setMenuType(type);
        menu.setRoutePath(path);
        menu.setComponent(component);
        menu.setPermission(permission);
        menu.setIcon(icon);
        menu.setSortOrder(sort);
        menu.setStatus(1);
        menu.setVisible(1);
        return menu;
    }

    @Test
    @DisplayName("构建完整菜单树")
    void testBuildFullMenuTree() {
        List<MenuTreeVO> tree = buildMenuTree(allMenus, 0L);

        // 应该有2个一级菜单
        assertEquals(2, tree.size());

        // 第一个是系统管理（sort=1）
        MenuTreeVO systemMenu = tree.get(0);
        assertEquals("系统管理", systemMenu.getMenuName());
        assertEquals(2, systemMenu.getChildren().size()); // 用户管理、角色管理

        // 用户管理下有3个按钮
        MenuTreeVO userMenu = systemMenu.getChildren().get(0);
        assertEquals("用户管理", userMenu.getMenuName());
        assertEquals(3, userMenu.getChildren().size());

        // 第二个是监控管理（sort=2）
        MenuTreeVO monitorMenu = tree.get(1);
        assertEquals("监控管理", monitorMenu.getMenuName());
        assertEquals(1, monitorMenu.getChildren().size());
    }

    @Test
    @DisplayName("菜单树按排序字段排序")
    void testMenuTreeSorting() {
        List<MenuTreeVO> tree = buildMenuTree(allMenus, 0L);

        // 一级菜单排序
        assertEquals("系统管理", tree.get(0).getMenuName()); // sort=1
        assertEquals("监控管理", tree.get(1).getMenuName()); // sort=2

        // 二级菜单排序
        MenuTreeVO systemMenu = tree.get(0);
        assertEquals("用户管理", systemMenu.getChildren().get(0).getMenuName()); // sort=1
        assertEquals("角色管理", systemMenu.getChildren().get(1).getMenuName()); // sort=2

        // 三级按钮排序
        MenuTreeVO userMenu = systemMenu.getChildren().get(0);
        assertEquals("用户新增", userMenu.getChildren().get(0).getMenuName()); // sort=1
        assertEquals("用户编辑", userMenu.getChildren().get(1).getMenuName()); // sort=2
        assertEquals("用户删除", userMenu.getChildren().get(2).getMenuName()); // sort=3
    }

    @Test
    @DisplayName("过滤禁用菜单")
    void testFilterDisabledMenus() {
        // 禁用用户管理菜单
        allMenus.stream()
                .filter(m -> m.getId().equals(2L))
                .findFirst()
                .ifPresent(m -> m.setStatus(0));

        List<Menu> enabledMenus = allMenus.stream()
                .filter(m -> m.getStatus() == 1)
                .collect(Collectors.toList());

        List<MenuTreeVO> tree = buildMenuTree(enabledMenus, 0L);

        // 系统管理下只有角色管理
        MenuTreeVO systemMenu = tree.get(0);
        assertEquals(1, systemMenu.getChildren().size());
        assertEquals("角色管理", systemMenu.getChildren().get(0).getMenuName());
    }

    @Test
    @DisplayName("过滤隐藏菜单")
    void testFilterHiddenMenus() {
        // 隐藏监控管理菜单
        allMenus.stream()
                .filter(m -> m.getId().equals(8L))
                .findFirst()
                .ifPresent(m -> m.setVisible(0));

        List<Menu> visibleMenus = allMenus.stream()
                .filter(m -> m.getVisible() == 1)
                .collect(Collectors.toList());

        List<MenuTreeVO> tree = buildMenuTree(visibleMenus, 0L);

        // 只有系统管理
        assertEquals(1, tree.size());
        assertEquals("系统管理", tree.get(0).getMenuName());
    }

    @Test
    @DisplayName("只获取菜单和目录（排除按钮）")
    void testFilterButtonMenus() {
        List<Menu> menuOnly = allMenus.stream()
                .filter(m -> m.getMenuType() != 3) // 排除按钮
                .collect(Collectors.toList());

        List<MenuTreeVO> tree = buildMenuTree(menuOnly, 0L);

        // 系统管理下的用户管理没有子节点（按钮被过滤）
        MenuTreeVO systemMenu = tree.get(0);
        MenuTreeVO userMenu = systemMenu.getChildren().get(0);
        assertTrue(userMenu.getChildren() == null || userMenu.getChildren().isEmpty());
    }

    @Test
    @DisplayName("空菜单列表返回空树")
    void testEmptyMenuList() {
        List<MenuTreeVO> tree = buildMenuTree(Collections.emptyList(), 0L);
        assertTrue(tree.isEmpty());
    }

    @Test
    @DisplayName("获取所有权限标识")
    void testGetAllPermissions() {
        Set<String> permissions = allMenus.stream()
                .map(Menu::getPermission)
                .filter(Objects::nonNull)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toSet());

        assertEquals(7, permissions.size());
        assertTrue(permissions.contains("system:user:list"));
        assertTrue(permissions.contains("system:user:add"));
        assertTrue(permissions.contains("system:user:edit"));
        assertTrue(permissions.contains("system:user:delete"));
        assertTrue(permissions.contains("system:role:list"));
        assertTrue(permissions.contains("system:role:add"));
        assertTrue(permissions.contains("monitor:operlog:list"));
    }

    @Test
    @DisplayName("根据角色菜单ID过滤菜单树")
    void testFilterMenuTreeByRoleMenuIds() {
        // 假设角色只有部分菜单权限
        Set<Long> roleMenuIds = Set.of(1L, 2L, 3L, 4L); // 系统管理、用户管理、用户新增、用户编辑

        List<Menu> roleMenus = allMenus.stream()
                .filter(m -> roleMenuIds.contains(m.getId()))
                .collect(Collectors.toList());

        List<MenuTreeVO> tree = buildMenuTree(roleMenus, 0L);

        // 只有系统管理
        assertEquals(1, tree.size());
        MenuTreeVO systemMenu = tree.get(0);
        assertEquals("系统管理", systemMenu.getMenuName());

        // 系统管理下只有用户管理
        assertEquals(1, systemMenu.getChildren().size());
        MenuTreeVO userMenu = systemMenu.getChildren().get(0);
        assertEquals("用户管理", userMenu.getMenuName());

        // 用户管理下只有2个按钮
        assertEquals(2, userMenu.getChildren().size());
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
                    // 如果没有子节点，设置为null
                    if (vo.getChildren() != null && vo.getChildren().isEmpty()) {
                        vo.setChildren(null);
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
