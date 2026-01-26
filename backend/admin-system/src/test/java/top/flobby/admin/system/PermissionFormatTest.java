package top.flobby.admin.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限标识格式校验测试
 * <p>
 * 权限标识格式规范：
 * - 格式：模块:资源:操作，如 system:user:add
 * - 每段只允许小写字母、数字和下划线
 * - 至少包含两段（模块:操作），最多三段（模块:资源:操作）
 * - 总长度不超过100字符
 *
 * @author flobby
 * @date 2026-01-26
 */
@DisplayName("权限标识格式校验测试")
class PermissionFormatTest {

    /**
     * 权限标识格式正则表达式
     * 格式：模块:资源:操作 或 模块:操作
     * 每段只允许小写字母、数字和下划线
     */
    private static final Pattern PERMISSION_PATTERN = Pattern.compile(
            "^[a-z][a-z0-9_]*:[a-z][a-z0-9_]*(:[a-z][a-z0-9_]*)?$"
    );

    /**
     * 最大长度限制
     */
    private static final int MAX_LENGTH = 100;

    /**
     * 校验权限标识格式
     *
     * @param permission 权限标识
     * @return 是否有效
     */
    private boolean isValidPermission(String permission) {
        if (permission == null || permission.isEmpty()) {
            return false;
        }
        if (permission.length() > MAX_LENGTH) {
            return false;
        }
        return PERMISSION_PATTERN.matcher(permission).matches();
    }

    @Test
    @DisplayName("有效的三段式权限标识")
    void testValidThreePartPermission() {
        assertTrue(isValidPermission("system:user:add"));
        assertTrue(isValidPermission("system:user:edit"));
        assertTrue(isValidPermission("system:user:delete"));
        assertTrue(isValidPermission("system:user:list"));
        assertTrue(isValidPermission("system:role:add"));
        assertTrue(isValidPermission("monitor:operlog:list"));
        assertTrue(isValidPermission("monitor:operlog:export"));
    }

    @Test
    @DisplayName("有效的两段式权限标识")
    void testValidTwoPartPermission() {
        assertTrue(isValidPermission("system:list"));
        assertTrue(isValidPermission("monitor:view"));
        assertTrue(isValidPermission("admin:manage"));
    }

    @Test
    @DisplayName("包含数字的有效权限标识")
    void testValidPermissionWithNumbers() {
        assertTrue(isValidPermission("system2:user:add"));
        assertTrue(isValidPermission("system:user2:add"));
        assertTrue(isValidPermission("system:user:add2"));
        assertTrue(isValidPermission("v1:api:call"));
    }

    @Test
    @DisplayName("包含下划线的有效权限标识")
    void testValidPermissionWithUnderscore() {
        assertTrue(isValidPermission("system_admin:user_manage:add_new"));
        assertTrue(isValidPermission("my_module:my_resource:my_action"));
    }

    @ParameterizedTest
    @DisplayName("无效的权限标识 - 空值")
    @ValueSource(strings = {"", " ", "  "})
    void testInvalidEmptyPermission(String permission) {
        assertFalse(isValidPermission(permission.trim().isEmpty() ? null : permission));
    }

    @Test
    @DisplayName("无效的权限标识 - null值")
    void testInvalidNullPermission() {
        assertFalse(isValidPermission(null));
    }

    @Test
    @DisplayName("无效的权限标识 - 单段")
    void testInvalidSinglePartPermission() {
        assertFalse(isValidPermission("system"));
        assertFalse(isValidPermission("user"));
        assertFalse(isValidPermission("add"));
    }

    @Test
    @DisplayName("无效的权限标识 - 超过三段")
    void testInvalidFourPartPermission() {
        assertFalse(isValidPermission("system:user:add:extra"));
        assertFalse(isValidPermission("a:b:c:d"));
        assertFalse(isValidPermission("system:module:resource:action"));
    }

    @Test
    @DisplayName("无效的权限标识 - 包含大写字母")
    void testInvalidUppercasePermission() {
        assertFalse(isValidPermission("System:user:add"));
        assertFalse(isValidPermission("system:User:add"));
        assertFalse(isValidPermission("system:user:Add"));
        assertFalse(isValidPermission("SYSTEM:USER:ADD"));
    }

    @Test
    @DisplayName("无效的权限标识 - 包含特殊字符")
    void testInvalidSpecialCharPermission() {
        assertFalse(isValidPermission("system-user:add"));
        assertFalse(isValidPermission("system:user-add"));
        assertFalse(isValidPermission("system:user:add!"));
        assertFalse(isValidPermission("system:user:add@"));
        assertFalse(isValidPermission("system:user:add#"));
        assertFalse(isValidPermission("system:user:add$"));
        assertFalse(isValidPermission("system:user:add%"));
        assertFalse(isValidPermission("system:user:add&"));
        assertFalse(isValidPermission("system:user:add*"));
        assertFalse(isValidPermission("system:user:add."));
        assertFalse(isValidPermission("system:user:add/"));
    }

    @Test
    @DisplayName("无效的权限标识 - 包含空格")
    void testInvalidPermissionWithSpaces() {
        assertFalse(isValidPermission("system :user:add"));
        assertFalse(isValidPermission("system: user:add"));
        assertFalse(isValidPermission("system:user :add"));
        assertFalse(isValidPermission("system:user: add"));
        assertFalse(isValidPermission(" system:user:add"));
        assertFalse(isValidPermission("system:user:add "));
    }

    @Test
    @DisplayName("无效的权限标识 - 以数字开头")
    void testInvalidPermissionStartingWithNumber() {
        assertFalse(isValidPermission("1system:user:add"));
        assertFalse(isValidPermission("system:1user:add"));
        assertFalse(isValidPermission("system:user:1add"));
    }

    @Test
    @DisplayName("无效的权限标识 - 以下划线开头")
    void testInvalidPermissionStartingWithUnderscore() {
        assertFalse(isValidPermission("_system:user:add"));
        assertFalse(isValidPermission("system:_user:add"));
        assertFalse(isValidPermission("system:user:_add"));
    }

    @Test
    @DisplayName("无效的权限标识 - 空段")
    void testInvalidPermissionWithEmptyPart() {
        assertFalse(isValidPermission(":user:add"));
        assertFalse(isValidPermission("system::add"));
        assertFalse(isValidPermission("system:user:"));
        assertFalse(isValidPermission("::"));
        assertFalse(isValidPermission(":::"));
    }

    @Test
    @DisplayName("无效的权限标识 - 超过最大长度")
    void testInvalidPermissionExceedingMaxLength() {
        // 构造一个超过100字符的权限标识
        String longPermission = "a".repeat(50) + ":" + "b".repeat(50) + ":c";
        assertTrue(longPermission.length() > MAX_LENGTH);
        assertFalse(isValidPermission(longPermission));
    }

    @Test
    @DisplayName("边界情况 - 刚好100字符")
    void testValidPermissionAtMaxLength() {
        // 构造一个刚好100字符的权限标识
        String permission = "a".repeat(48) + ":" + "b".repeat(48) + ":cc";
        assertEquals(100, permission.length());
        assertTrue(isValidPermission(permission));
    }

    @Test
    @DisplayName("边界情况 - 最短有效权限标识")
    void testValidShortestPermission() {
        assertTrue(isValidPermission("a:b"));
        assertTrue(isValidPermission("a:b:c"));
    }

    @Test
    @DisplayName("常见权限标识示例")
    void testCommonPermissionExamples() {
        // 用户管理
        assertTrue(isValidPermission("system:user:list"));
        assertTrue(isValidPermission("system:user:add"));
        assertTrue(isValidPermission("system:user:edit"));
        assertTrue(isValidPermission("system:user:delete"));
        assertTrue(isValidPermission("system:user:export"));
        assertTrue(isValidPermission("system:user:import"));
        assertTrue(isValidPermission("system:user:reset_pwd"));

        // 角色管理
        assertTrue(isValidPermission("system:role:list"));
        assertTrue(isValidPermission("system:role:add"));
        assertTrue(isValidPermission("system:role:edit"));
        assertTrue(isValidPermission("system:role:delete"));
        assertTrue(isValidPermission("system:role:assign"));

        // 菜单管理
        assertTrue(isValidPermission("system:menu:list"));
        assertTrue(isValidPermission("system:menu:add"));
        assertTrue(isValidPermission("system:menu:edit"));
        assertTrue(isValidPermission("system:menu:delete"));

        // 部门管理
        assertTrue(isValidPermission("system:dept:list"));
        assertTrue(isValidPermission("system:dept:add"));
        assertTrue(isValidPermission("system:dept:edit"));
        assertTrue(isValidPermission("system:dept:delete"));

        // 监控模块
        assertTrue(isValidPermission("monitor:operlog:list"));
        assertTrue(isValidPermission("monitor:operlog:export"));
        assertTrue(isValidPermission("monitor:online:list"));
        assertTrue(isValidPermission("monitor:online:force_logout"));
    }
}
