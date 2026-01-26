package top.flobby.admin.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流 key 前缀
     */
    String key() default "rate_limit:";

    /**
     * 限流时间窗口
     */
    int time() default 60;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 限流次数
     */
    int count() default 100;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;

    enum LimitType {
        /**
         * 默认策略：全局限流
         */
        DEFAULT,
        /**
         * 根据 IP 限流
         */
        IP,
        /**
         * 根据用户限流
         */
        USER
    }
}
