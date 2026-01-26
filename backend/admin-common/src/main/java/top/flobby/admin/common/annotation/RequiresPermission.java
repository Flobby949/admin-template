package top.flobby.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 权限注解
 * 用于标记需要特定权限才能访问的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 需要的权限标识
     */
    String[] value();

    /**
     * 逻辑关系：AND-所有权限都需要，OR-任一权限即可
     */
    Logical logical() default Logical.AND;

    enum Logical {
        AND, OR
    }
}
