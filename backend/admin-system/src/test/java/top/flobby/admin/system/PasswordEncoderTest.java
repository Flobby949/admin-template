package top.flobby.admin.system;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BCrypt 密码加密测试
 *
 * 测试目标:
 * 1. 验证 BCrypt(12) 加密结果匹配
 * 2. 验证错误密码不匹配
 * 3. 验证相同密码生成不同哈希
 */
class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Test
    void testPasswordEncoding() {
        // Given
        String rawPassword = "admin123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println(encodedPassword);
        // Then
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$12$") || encodedPassword.startsWith("$2b$12$"));
    }

    @Test
    void testPasswordMatching() {
        // Given
        String rawPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println(encodedPassword);
        // When & Then
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testPasswordNotMatching() {
        // Given
        String rawPassword = "admin123";
        String wrongPassword = "wrong123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When & Then
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword));
    }

    @Test
    void testSamePasswordGeneratesDifferentHash() {
        // Given
        String rawPassword = "admin123";

        // When
        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);

        // Then
        assertNotEquals(hash1, hash2, "相同密码应该生成不同的哈希值(因为盐值不同)");
        assertTrue(passwordEncoder.matches(rawPassword, hash1));
        assertTrue(passwordEncoder.matches(rawPassword, hash2));
    }

    @Test
    void testEmptyPasswordHandling() {
        // Given
        String emptyPassword = "";

        // When
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        // Then
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword));
    }

    @Test
    void testLongPasswordHandling() {
        // Given
        String longPassword = "a".repeat(100);

        // When
        String encodedPassword = passwordEncoder.encode(longPassword);

        // Then
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword));
    }
}
