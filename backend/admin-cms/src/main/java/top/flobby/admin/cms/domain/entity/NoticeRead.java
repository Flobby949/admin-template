package top.flobby.admin.cms.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 公告已读记录实体
 */
@Getter
@Setter
@Entity
@Table(name = "cms_notice_read", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"notice_id", "user_id"})
})
public class NoticeRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 公告ID
     */
    @Column(name = "notice_id", nullable = false)
    private Long noticeId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 阅读时间
     */
    @Column(name = "read_time")
    private LocalDateTime readTime = LocalDateTime.now();
}
