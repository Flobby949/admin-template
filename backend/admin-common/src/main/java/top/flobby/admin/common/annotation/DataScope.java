package top.flobby.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>
 * 用于标注需要进行数据权限过滤的方法。
 * 数据权限基于用户所属部门进行过滤，支持以下范围：
 * <ul>
 *   <li>1 - 全部数据：不进行任何过滤</li>
 *   <li>2 - 本部门及下级：用户所属部门及其子部门的数据</li>
 *   <li>3 - 仅本部门：仅用户所属部门的数据</li>
 *   <li>4 - 仅本人：仅用户自己创建的数据</li>
 * </ul>
 *
 * @author flobby
 * @date 2026-01-28
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 部门字段名称（逻辑字段名）
     * <p>
     * 用于指定实体中表示部门的字段名，默认为 "deptId"
     */
    String deptField() default "deptId";

    /**
     * 用户字段名称（逻辑字段名）
     * <p>
     * 用于指定实体中表示用户的字段名，默认为 "id"
     * 注意：为避免 createBy 审计字段未填充的问题，默认使用主键 id
     */
    String userField() default "id";

    /**
     * 是否启用数据权限过滤
     * <p>
     * 默认为 true，可通过设置为 false 临时禁用数据权限
     */
    boolean enabled() default true;
}
