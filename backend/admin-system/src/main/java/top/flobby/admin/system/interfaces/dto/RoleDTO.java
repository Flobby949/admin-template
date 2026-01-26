package top.flobby.admin.system.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色 DTO
 */
@Data
@Schema(description = "角色请求")
public class RoleDTO {

    @Schema(description = "角色ID（更新时必填）")
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度必须在2-50之间")
    @Schema(description = "角色名称", required = true)
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 50, message = "角色编码长度必须在2-50之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "角色编码必须以字母开头，只能包含字母、数字和下划线")
    @Schema(description = "角色编码", required = true)
    private String roleCode;

    @Schema(description = "数据权限范围：1-全部，2-本部门及下级，3-仅本部门，4-仅本人")
    private Integer dataScope = 1;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status = 1;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    @Schema(description = "菜单ID列表")
    private List<Long> menuIds;
}
