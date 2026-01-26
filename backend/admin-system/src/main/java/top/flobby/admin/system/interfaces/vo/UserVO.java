package top.flobby.admin.system.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象
 *
 * @author Flobby
 * @date 2026-01-26
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 角色列表
     */
    private List<RoleInfo> roles;

    /**
     * 部门列表
     */
    private List<DeptInfo> depts;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色信息
     */
    @Data
    public static class RoleInfo {
        /**
         * 角色ID
         */
        private Long id;

        /**
         * 角色名称
         */
        private String name;

        /**
         * 角色编码
         */
        private String code;
    }

    /**
     * 部门信息
     */
    @Data
    public static class DeptInfo {
        /**
         * 部门ID
         */
        private Long id;

        /**
         * 部门名称
         */
        private String name;
    }
}
