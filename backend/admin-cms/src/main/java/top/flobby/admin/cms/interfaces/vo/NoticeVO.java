package top.flobby.admin.cms.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告 VO
 */
@Data
public class NoticeVO {

    private Long id;

    private String title;

    private String content;

    private Integer status;

    private LocalDateTime publishTime;

    private LocalDateTime revokeTime;

    private Long deptId;

    private Long publisherId;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private String createBy;

    private Long readCount;

    private Boolean read;
}
