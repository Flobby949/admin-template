package top.flobby.admin.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.flobby.admin.common.filter.XssFilter;

/**
 * Web 配置
 */
@Configuration
public class WebConfig {

    /**
     * 注册 XSS 过滤器
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(1);
        return registration;
    }
}
