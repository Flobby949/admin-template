package top.flobby.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA 审计配置
 * <p>
 * 启用 JPA 审计功能，自动填充实体的 createBy 和 updateBy 字段
 *
 * @author flobby
 * @date 2026-01-28
 */
@Configuration
@EnableJpaAuditing
public class AuditingConfig {

    /**
     * 审计人员提供者
     * <p>
     * 从 Spring Security 上下文中获取当前用户名
     *
     * @return AuditorAware 实例
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            String username = authentication.getName();
            if ("anonymousUser".equals(username)) {
                return Optional.of("system");
            }

            return Optional.of(username);
        };
    }
}
