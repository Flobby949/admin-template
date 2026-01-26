package top.flobby.admin.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.flobby.admin.system.domain.entity.Menu;
import top.flobby.admin.system.domain.entity.Role;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色权限计算测试
 * 测试多角色权限并集计算逻辑
 */
@DisplayName("角色权限计算测试")
class RolePermissionTest {

    private List<Role> roles;
    private Map<Long, List<Menu>> roleMenuMap;

    @BeforeEach
    void setUp() {
        // 创建测试角色
        roles = new ArrayList<>();
        roleMenuMap = new HashMap<>();

        // 角色1：管理员
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName("管理员");
        adminRole.setRoleCode("admin");
        adminRole.setDataScope(1); // 全部数据
        adminRole.setStatus(1);
        roles.add(adminRole);

        // 角色2：普通用户
        Role userRole = new Role();
        userRole.setId(2L);
        userRole.setRoleName("普通用户");
        userRole.setRoleCode("user");
        userRole.setDataScope(4); // 仅本人
        userRole.setStatus(1);
        roles.add(userRole);

        // 角色3：部门经理
        Role managerRole = new Role();
        managerRole.setId(3L);
        managerRole.setRoleName("部门经理");
        managerRole.setRoleCode("manager");
        managerRole.setDataScope(2); // 本部门及下级
        managerRole.setStatus(1);
        roles.add(managerRole);

        // 创建菜单
        Menu menu1 = createMenu(1L, "system:user:list");
        Menu menu2 = createMenu(2L, "system:user:add");
        Menu menu3 = createMenu(3L, "system:user:edit");
        Menu menu4 = createMenu(4L, "system:role:list");
        Menu menu5 = createMenu(5L, "system:role:add");

        // 角色1拥有所有权限
        roleMenuMap.put(1L, Arrays.asList(menu1, menu2, menu3, menu4, menu5));
        // 角色2只有查看权限
        roleMenuMap.put(2L, Arrays.asList(menu1, menu4));
        // 角色3有用户管理权限
        roleMenuMap.put(3L, Arrays.asList(menu1, menu2, menu3));
    }

    private Menu createMenu(Long id, String permission) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setPermission(permission);
        menu.setStatus(1);
        return menu;
    }

    @Test
    @DisplayName("单角色权限获取")
    void testSingleRolePermissions() {
        // 获取角色2的权限
        List<Menu> menus = roleMenuMap.get(2L);
        Set<String> permissions = menus.stream()
                .map(Menu::getPermission)
                .collect(Collectors.toSet());

        assertEquals(2, permissions.size());
        assertTrue(permissions.contains("system:user:list"));
        assertTrue(permissions.contains("system:role:list"));
        assertFalse(permissions.contains("system:user:add"));
    }

    @Test
    @DisplayName("多角色权限并集计算")
    void testMultiRolePermissionUnion() {
        // 用户同时拥有角色2和角色3
        List<Long> userRoleIds = Arrays.asList(2L, 3L);

        // 计算权限并集
        Set<String> permissions = userRoleIds.stream()
                .flatMap(roleId -> roleMenuMap.getOrDefault(roleId, Collections.emptyList()).stream())
                .map(Menu::getPermission)
                .collect(Collectors.toSet());

        // 角色2: system:user:list, system:role:list
        // 角色3: system:user:list, system:user:add, system:user:edit
        // 并集: system:user:list, system:role:list, system:user:add, system:user:edit
        assertEquals(4, permissions.size());
        assertTrue(permissions.contains("system:user:list"));
        assertTrue(permissions.contains("system:role:list"));
        assertTrue(permissions.contains("system:user:add"));
        assertTrue(permissions.contains("system:user:edit"));
        assertFalse(permissions.contains("system:role:add")); // 只有角色1有
    }

    @Test
    @DisplayName("数据权限范围取最大值")
    void testDataScopeMaxValue() {
        // 用户同时拥有角色2(dataScope=4)和角色3(dataScope=2)
        List<Long> userRoleIds = Arrays.asList(2L, 3L);

        // 获取最大数据权限范围（数值越小权限越大）
        int maxDataScope = userRoleIds.stream()
                .map(roleId -> roles.stream()
                        .filter(r -> r.getId().equals(roleId))
                        .findFirst()
                        .map(Role::getDataScope)
                        .orElse(4))
                .min(Integer::compareTo)
                .orElse(4);

        // 角色2: 4(仅本人), 角色3: 2(本部门及下级)
        // 取最小值（权限最大）: 2
        assertEquals(2, maxDataScope);
    }

    @Test
    @DisplayName("空角色列表返回空权限")
    void testEmptyRoleList() {
        List<Long> emptyRoleIds = Collections.emptyList();

        Set<String> permissions = emptyRoleIds.stream()
                .flatMap(roleId -> roleMenuMap.getOrDefault(roleId, Collections.emptyList()).stream())
                .map(Menu::getPermission)
                .collect(Collectors.toSet());

        assertTrue(permissions.isEmpty());
    }

    @Test
    @DisplayName("禁用角色不参与权限计算")
    void testDisabledRoleExcluded() {
        // 禁用角色3
        roles.stream()
                .filter(r -> r.getId().equals(3L))
                .findFirst()
                .ifPresent(r -> r.setStatus(0));

        // 用户同时拥有角色2和角色3（已禁用）
        List<Long> userRoleIds = Arrays.asList(2L, 3L);

        // 只计算启用角色的权限
        Set<String> permissions = userRoleIds.stream()
                .filter(roleId -> roles.stream()
                        .filter(r -> r.getId().equals(roleId))
                        .findFirst()
                        .map(Role::isEnabled)
                        .orElse(false))
                .flatMap(roleId -> roleMenuMap.getOrDefault(roleId, Collections.emptyList()).stream())
                .map(Menu::getPermission)
                .collect(Collectors.toSet());

        // 只有角色2的权限
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains("system:user:list"));
        assertTrue(permissions.contains("system:role:list"));
    }

    @Test
    @DisplayName("权限检查 - 包含任一权限")
    void testHasAnyPermission() {
        Set<String> userPermissions = Set.of("system:user:list", "system:role:list");
        String[] requiredPermissions = {"system:user:add", "system:user:list"};

        boolean hasAny = Arrays.stream(requiredPermissions)
                .anyMatch(userPermissions::contains);

        assertTrue(hasAny);
    }

    @Test
    @DisplayName("权限检查 - 包含所有权限")
    void testHasAllPermissions() {
        Set<String> userPermissions = Set.of("system:user:list", "system:role:list", "system:user:add");
        String[] requiredPermissions = {"system:user:list", "system:user:add"};

        boolean hasAll = Arrays.stream(requiredPermissions)
                .allMatch(userPermissions::contains);

        assertTrue(hasAll);

        // 缺少权限的情况
        String[] morePermissions = {"system:user:list", "system:user:edit"};
        boolean hasAllMore = Arrays.stream(morePermissions)
                .allMatch(userPermissions::contains);

        assertFalse(hasAllMore);
    }
}
