package top.flobby.admin.system.interfaces.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.flobby.admin.common.core.PageQuery;

/**
 * 用户查询条件
 *
 * @author Flobby
 * @date 2026-01-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 手机号（精确查询）
     */
    private String phone;

    /**
     * 邮箱（精确查询）
     */
    private String email;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 开始时间（创建时间范围查询）
     */
    private String startTime;

    /**
     * 结束时间（创建时间范围查询）
     */
    private String endTime;
}
