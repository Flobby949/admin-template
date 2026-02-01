package top.flobby.admin.cms.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 公告 DTO
 */
@Data
public class NoticeDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    private String content;

    private Integer sortOrder;

    private Long deptId;
}
