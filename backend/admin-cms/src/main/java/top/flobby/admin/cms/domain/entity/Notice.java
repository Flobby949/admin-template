package top.flobby.admin.cms.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 通知公告实体
 * <p>
 * 状态机:
 * - 0: 草稿
 * - 1: 已发布
 * - 2: 已撤回
 */
@Getter
@Setter
@Entity
@Table(name = "cms_notice")
@EntityListeners(AuditingEntityListener.class)
public class Notice {

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_REVOKED = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 内容(富文本)
     */
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 状态:0-草稿,1-已发布,2-已撤回
     */
    @Column(nullable = false)
    private Integer status = STATUS_DRAFT;

    /**
     * 发布时间
     */
    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    /**
     * 撤回时间
     */
    @Column(name = "revoke_time")
    private LocalDateTime revokeTime;

    /**
     * 所属部门ID
     */
    @Column(name = "dept_id")
    private Long deptId;

    /**
     * 发布人用户ID
     */
    @Column(name = "publisher_id")
    private Long publisherId;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "create_by", length = 50)
    private String createBy;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    /**
     * 删除标记:0-未删除,1-已删除
     */
    @Column(nullable = false)
    private Integer deleted = 0;

    /**
     * 检查是否可以发布
     */
    public boolean canPublish() {
        return status == STATUS_DRAFT || status == STATUS_REVOKED;
    }

    /**
     * 检查是否可以撤回
     */
    public boolean canRevoke() {
        return status == STATUS_PUBLISHED;
    }

    /**
     * 发布
     */
    public void publish() {
        if (!canPublish()) {
            throw new IllegalStateException("当前状态不允许发布");
        }
        this.status = STATUS_PUBLISHED;
        this.publishTime = LocalDateTime.now();
    }

    /**
     * 撤回
     */
    public void revoke() {
        if (!canRevoke()) {
            throw new IllegalStateException("当前状态不允许撤回");
        }
        this.status = STATUS_REVOKED;
        this.revokeTime = LocalDateTime.now();
    }
}
