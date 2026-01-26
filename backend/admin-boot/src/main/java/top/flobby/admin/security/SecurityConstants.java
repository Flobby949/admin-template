package top.flobby.admin.security;

import java.util.List;

/**
 * 安全相关常量
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    /**
     * 白名单路径，这些路径不需要认证
     */
    public static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/actuator/health",
            "/actuator/info"
    );
}
