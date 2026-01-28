package top.flobby.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * @author flobby
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /**
     * 操作模块
     */
    String title() default "";

    /**
     * 业务类型
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 是否保存请求参数
     */
    boolean saveRequestData() default true;

    /**
     * 是否保存响应结果
     */
    boolean saveResponseData() default true;

    /**
     * 业务类型枚举
     */
    enum BusinessType {
        /**
         * 其它
         */
        OTHER,
        /**
         * 新增
         */
        INSERT,
        /**
         * 修改
         */
        UPDATE,
        /**
         * 删除
         */
        DELETE,
        /**
         * 查询
         */
        SELECT,
        /**
         * 导出
         */
        EXPORT,
        /**
         * 导入
         */
        IMPORT
    }
}
