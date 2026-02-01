package top.flobby.admin.cms.interfaces.query;

import lombok.Data;

/**
 * 公告查询参数
 */
@Data
public class NoticeQuery {

    private String title;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
