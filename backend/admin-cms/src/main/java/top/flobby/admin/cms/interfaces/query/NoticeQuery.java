package top.flobby.admin.cms.interfaces.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.flobby.admin.common.core.PageQuery;

/**
 * 公告查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeQuery extends PageQuery {

    private String title;

    private Integer status;
}
