package top.flobby.admin.cms.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类 DTO
 */
@Data
public class CategoryDTO {

    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100")
    private String categoryName;

    private Integer sortOrder;

    private Integer status;

    private Long deptId;
}
