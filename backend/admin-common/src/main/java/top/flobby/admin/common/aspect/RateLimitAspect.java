package top.flobby.admin.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.flobby.admin.common.annotation.RateLimit;
import top.flobby.admin.common.exception.BusinessException;

/**
 * 接口限流 AOP 切面
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimitAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        // 构建限流 key
        String key = buildKey(rateLimit, request);

        // 获取当前计数
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            count = 0L;
        }

        // 第一次访问，设置过期时间
        if (count == 1) {
            redisTemplate.expire(key, rateLimit.time(), rateLimit.timeUnit());
        }

        // 检查是否超过限流次数
        if (count > rateLimit.count()) {
            log.warn("接口限流触发: key={}, count={}, limit={}", key, count, rateLimit.count());
            throw new BusinessException(429, "访问过于频繁，请稍后再试");
        }

        log.debug("接口限流检查通过: key={}, count={}/{}", key, count, rateLimit.count());

        return joinPoint.proceed();
    }

    /**
     * 构建限流 key
     */
    private String buildKey(RateLimit rateLimit, HttpServletRequest request) {
        StringBuilder key = new StringBuilder(rateLimit.key());

        switch (rateLimit.limitType()) {
            case IP:
                key.append(getIpAddress(request));
                break;
            case USER:
                // TODO: 获取当前用户 ID
                key.append("user:unknown");
                break;
            default:
                key.append("global");
                break;
        }

        key.append(":").append(request.getRequestURI());

        return key.toString();
    }

    /**
     * 获取请求对象
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
