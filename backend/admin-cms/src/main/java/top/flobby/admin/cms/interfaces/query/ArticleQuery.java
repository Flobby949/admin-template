package top.flobby.admin.cms.interfaces.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.flobby.admin.common.core.PageQuery;

/**
 * 文章查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleQuery extends PageQuery {

    private String title;

    private Long categoryId;

    private Integer status;

    private Long authorId;
}
