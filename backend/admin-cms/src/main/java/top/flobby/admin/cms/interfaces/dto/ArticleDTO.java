package top.flobby.admin.cms.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文章 DTO
 */
@Data
public class ArticleDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @Size(max = 500, message = "摘要长度不能超过500")
    private String summary;

    private String content;

    private Long categoryId;

    @Size(max = 500, message = "封面URL长度不能超过500")
    private String coverUrl;

    private Integer sortOrder;

    private Long deptId;
}
