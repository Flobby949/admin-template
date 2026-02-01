package top.flobby.admin.cms.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类 VO
 */
@Data
public class CategoryVO {

    private Long id;

    private Long parentId;

    private String categoryName;

    private Integer sortOrder;

    private Integer status;

    private Long deptId;

    private LocalDateTime createTime;

    private List<CategoryVO> children;
}
