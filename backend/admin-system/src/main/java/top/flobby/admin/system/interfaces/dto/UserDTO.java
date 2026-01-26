package top.flobby.admin.system.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 用户数据传输对象
 *
 * @author Flobby
 * @date 2026-01-26
 */
@Data
public class UserDTO {

    /**
     * 用户ID（更新时必填）
     */
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度必须在4-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码（新增时必填，更新时选填）
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;

    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值只能是0或1")
    @Max(value = 1, message = "状态值只能是0或1")
    private Integer status;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;

    /**
     * 部门ID列表
     */
    private List<Long> deptIds;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
