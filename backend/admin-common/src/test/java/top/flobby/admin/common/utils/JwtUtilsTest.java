package top.flobby.admin.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT Token 生成和解析测试
 *
 * 测试目标:
 * 1. 验证 Token 生成
 * 2. 验证 Token 解析获取用户名
 * 3. 验证 Token 过期检测
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // 使用反射设置私有字段
        ReflectionTestUtils.setField(jwtUtils, "secret", "mySecretKeyForTestingPurposesOnlyDoNotUseInProduction1234567890");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtUtils, "refreshExpiration", 7200000L); // 2 hours
    }

    @Test
    void testGenerateToken() {
        // Given
        String username = "admin";

        // When
        String token = jwtUtils.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3, "JWT Token 应该包含 3 个部分");
    }

    @Test
    void testGetUsernameFromToken() {
        // Given
        String username = "admin";
        String token = jwtUtils.generateToken(username);

        // When
        String extractedUsername = jwtUtils.getUsernameFromToken(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void testValidateToken() {
        // Given
        String username = "admin";
        String token = jwtUtils.generateToken(username);

        // When & Then
        assertTrue(jwtUtils.validateToken(token, username));
    }

    @Test
    void testValidateTokenWithWrongUsername() {
        // Given
        String username = "admin";
        String wrongUsername = "user";
        String token = jwtUtils.generateToken(username);

        // When & Then
        assertFalse(jwtUtils.validateToken(token, wrongUsername));
    }

    @Test
    void testGenerateRefreshToken() {
        // Given
        String username = "admin";

        // When
        String refreshToken = jwtUtils.generateRefreshToken(username);

        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void testTokenExpiration() {
        // Given
        String username = "admin";
        JwtUtils shortExpirationJwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(shortExpirationJwtUtils, "secret", "mySecretKeyForTestingPurposesOnlyDoNotUseInProduction1234567890");
        ReflectionTestUtils.setField(shortExpirationJwtUtils, "expiration", 1L); // 1ms
        ReflectionTestUtils.setField(shortExpirationJwtUtils, "refreshExpiration", 1L);

        // When
        String token = shortExpirationJwtUtils.generateToken(username);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        assertThrows(ExpiredJwtException.class, () -> {
            shortExpirationJwtUtils.getUsernameFromToken(token);
        });
    }

    @Test
    void testGetClaimsFromToken() {
        // Given
        String username = "admin";
        String token = jwtUtils.generateToken(username);

        // When
        Claims claims = jwtUtils.getClaimsFromToken(token);

        // Then
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testIsTokenExpired() {
        // Given
        String username = "admin";
        String token = jwtUtils.generateToken(username);

        // When & Then
        assertFalse(jwtUtils.validateToken(token, username));
    }

    @Test
    void testDifferentUsersGenerateDifferentTokens() {
        // Given
        String user1 = "admin";
        String user2 = "user";

        // When
        String token1 = jwtUtils.generateToken(user1);
        String token2 = jwtUtils.generateToken(user2);

        // Then
        assertNotEquals(token1, token2);
        assertEquals(user1, jwtUtils.getUsernameFromToken(token1));
        assertEquals(user2, jwtUtils.getUsernameFromToken(token2));
    }
}
