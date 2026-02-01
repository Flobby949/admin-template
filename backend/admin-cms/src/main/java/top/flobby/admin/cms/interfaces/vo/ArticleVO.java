package top.flobby.admin.cms.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章 VO
 */
@Data
public class ArticleVO {

    private Long id;

    private String title;

    private String summary;

    private String content;

    private Long categoryId;

    private String categoryName;

    private String coverUrl;

    private Integer status;

    private LocalDateTime publishTime;

    private LocalDateTime revokeTime;

    private String auditBy;

    private LocalDateTime auditTime;

    private Long deptId;

    private Long authorId;

    private Long viewCount;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private String createBy;
}
