package top.flobby.admin.system.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 部门DTO
 */
@Data
@Schema(description = "部门DTO")
public class DepartmentDTO {

    @Schema(description = "部门ID(更新时必填)")
    private Long id;

    @Schema(description = "父部门ID,0表示根节点", example = "0")
    @NotNull(message = "父部门ID不能为空")
    private Long parentId;

    @Schema(description = "部门名称", example = "技术部")
    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "负责人", example = "张三")
    private String leader;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "tech@example.com")
    private String email;

    @Schema(description = "状态:0-禁用,1-启用", example = "1")
    private Integer status;
}
