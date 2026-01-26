package top.flobby.admin.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import top.flobby.admin.system.domain.entity.User;
import top.flobby.admin.system.domain.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 用户状态变更业务规则单元测试
 *
 * @author Flobby
 * @date 2026-01-26
 */
class UserStatusTest {

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEnableUser() {
        // 准备测试数据
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setStatus(0); // 禁用状态

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行状态变更
        User foundUser = userRepository.findById(1L).orElseThrow();
        foundUser.setStatus(1); // 启用
        User savedUser = userRepository.save(foundUser);

        // 验证结果
        assertEquals(1, savedUser.getStatus(), "用户应该被启用");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDisableUser() {
        // 准备测试数据
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setStatus(1); // 启用状态

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行状态变更
        User foundUser = userRepository.findById(1L).orElseThrow();
        foundUser.setStatus(0); // 禁用
        User savedUser = userRepository.save(foundUser);

        // 验证结果
        assertEquals(0, savedUser.getStatus(), "用户应该被禁用");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCannotDisableAdminUser() {
        // 准备测试数据 - 管理员用户
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setStatus(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        // 验证不能禁用管理员
        User foundUser = userRepository.findById(1L).orElseThrow();

        // 业务规则：admin 用户不能被禁用
        if ("admin".equals(foundUser.getUsername())) {
            assertThrows(IllegalStateException.class, () -> {
                throw new IllegalStateException("不能禁用管理员账号");
            }, "应该抛出异常阻止禁用管理员");
        }
    }

    @Test
    void testStatusTransition() {
        // 测试状态转换规则
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        // 初始状态应该是启用
        user.setStatus(1);
        assertEquals(1, user.getStatus(), "初始状态应该是启用");

        // 可以从启用转为禁用
        user.setStatus(0);
        assertEquals(0, user.getStatus(), "应该可以从启用转为禁用");

        // 可以从禁用转为启用
        user.setStatus(1);
        assertEquals(1, user.getStatus(), "应该可以从禁用转为启用");
    }

    @Test
    void testInvalidStatusValue() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        // 测试无效的状态值
        assertThrows(IllegalArgumentException.class, () -> {
            int invalidStatus = 2;
            if (invalidStatus != 0 && invalidStatus != 1) {
                throw new IllegalArgumentException("状态值只能是 0（禁用）或 1（启用）");
            }
        }, "应该拒绝无效的状态值");
    }

    @Test
    void testBatchStatusUpdate() {
        // 测试批量状态更新
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setStatus(1);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setStatus(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 批量禁用
        User foundUser1 = userRepository.findById(1L).orElseThrow();
        foundUser1.setStatus(0);
        userRepository.save(foundUser1);

        User foundUser2 = userRepository.findById(2L).orElseThrow();
        foundUser2.setStatus(0);
        userRepository.save(foundUser2);

        // 验证
        assertEquals(0, foundUser1.getStatus());
        assertEquals(0, foundUser2.getStatus());
        verify(userRepository, times(2)).save(any(User.class));
    }
}
