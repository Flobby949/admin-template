package top.flobby.admin.system.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门VO
 */
@Data
@Schema(description = "部门VO")
public class DepartmentVO {

    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "祖级列表")
    private String ancestors;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "负责人")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态:0-禁用,1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "子部门列表")
    private List<DepartmentVO> children;
}
