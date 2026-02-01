package top.flobby.admin.cms.interfaces.query;

import lombok.Data;

/**
 * 文章查询参数
 */
@Data
public class ArticleQuery {

    private String title;

    private Long categoryId;

    private Integer status;

    private Long authorId;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
