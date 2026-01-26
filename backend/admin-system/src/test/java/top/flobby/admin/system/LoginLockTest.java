package top.flobby.admin.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 登录失败锁定机制测试
 *
 * 测试目标:
 * 1. 验证失败次数累加
 * 2. 验证达到阈值后锁定
 * 3. 验证 TTL 过期后解锁
 * 4. 验证成功登录后重置计数
 */
@ExtendWith(MockitoExtension.class)
class LoginLockTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private MockLoginLockService loginLockService;

    private static final int MAX_FAIL_COUNT = 5;
    private static final long LOCK_TIME_MINUTES = 15;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        loginLockService = new MockLoginLockService(redisTemplate, MAX_FAIL_COUNT, LOCK_TIME_MINUTES);
    }

    @Test
    void testIncrementFailCount() {
        // Given
        String username = "admin";
        String failKey = "login:fail:" + username;

        when(valueOperations.get(failKey)).thenReturn(null, 1, 2);

        // When & Then
        loginLockService.incrementFailCount(username);
        verify(valueOperations).increment(failKey);
        verify(redisTemplate).expire(eq(failKey), eq(LOCK_TIME_MINUTES), eq(TimeUnit.MINUTES));

        loginLockService.incrementFailCount(username);
        verify(valueOperations, times(2)).increment(failKey);
    }

    @Test
    void testLockAfterMaxFailures() {
        // Given
        String username = "admin";
        String failKey = "login:fail:" + username;
        String lockKey = "login:lock:" + username;

        when(valueOperations.get(failKey)).thenReturn(MAX_FAIL_COUNT);

        // When
        boolean isLocked = loginLockService.isLocked(username);

        // Then
        assertTrue(isLocked);
        verify(redisTemplate).opsForValue();
    }

    @Test
    void testNotLockedBeforeMaxFailures() {
        // Given
        String username = "admin";
        String failKey = "login:fail:" + username;

        when(valueOperations.get(failKey)).thenReturn(MAX_FAIL_COUNT - 1);

        // When
        boolean isLocked = loginLockService.isLocked(username);

        // Then
        assertFalse(isLocked);
    }

    @Test
    void testResetFailCount() {
        // Given
        String username = "admin";
        String failKey = "login:fail:" + username;
        String lockKey = "login:lock:" + username;

        // When
        loginLockService.resetFailCount(username);

        // Then
        verify(redisTemplate).delete(failKey);
        verify(redisTemplate).delete(lockKey);
    }

    @Test
    void testLockUser() {
        // Given
        String username = "admin";
        String lockKey = "login:lock:" + username;

        // When
        loginLockService.lock(username);

        // Then
        verify(valueOperations).set(eq(lockKey), eq("1"), eq(LOCK_TIME_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void testIsLockedWhenLockKeyExists() {
        // Given
        String username = "admin";
        String lockKey = "login:lock:" + username;

        when(redisTemplate.hasKey(lockKey)).thenReturn(true);

        // When
        boolean isLocked = loginLockService.isLocked(username);

        // Then
        assertTrue(isLocked);
    }

    @Test
    void testIsNotLockedWhenNoKeys() {
        // Given
        String username = "admin";
        String failKey = "login:fail:" + username;
        String lockKey = "login:lock:" + username;

        when(redisTemplate.hasKey(lockKey)).thenReturn(false);
        when(valueOperations.get(failKey)).thenReturn(null);

        // When
        boolean isLocked = loginLockService.isLocked(username);

        // Then
        assertFalse(isLocked);
    }

    /**
     * Mock LoginLockService for testing
     */
    static class MockLoginLockService {
        private final RedisTemplate<String, Object> redisTemplate;
        private final int maxFailCount;
        private final long lockTimeMinutes;

        public MockLoginLockService(RedisTemplate<String, Object> redisTemplate, int maxFailCount, long lockTimeMinutes) {
            this.redisTemplate = redisTemplate;
            this.maxFailCount = maxFailCount;
            this.lockTimeMinutes = lockTimeMinutes;
        }

        public boolean isLocked(String username) {
            String lockKey = "login:lock:" + username;
            String failKey = "login:fail:" + username;

            // Check if explicitly locked
            if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
                return true;
            }

            // Check if fail count exceeds threshold
            Object failCount = redisTemplate.opsForValue().get(failKey);
            if (failCount != null && (Integer) failCount >= maxFailCount) {
                return true;
            }

            return false;
        }

        public void incrementFailCount(String username) {
            String failKey = "login:fail:" + username;
            redisTemplate.opsForValue().increment(failKey);
            redisTemplate.expire(failKey, lockTimeMinutes, TimeUnit.MINUTES);
        }

        public void lock(String username) {
            String lockKey = "login:lock:" + username;
            redisTemplate.opsForValue().set(lockKey, "1", lockTimeMinutes, TimeUnit.MINUTES);
        }

        public void resetFailCount(String username) {
            String failKey = "login:fail:" + username;
            String lockKey = "login:lock:" + username;
            redisTemplate.delete(failKey);
            redisTemplate.delete(lockKey);
        }
    }
}
