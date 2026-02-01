package top.flobby.admin.cms.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 文章实体
 * <p>
 * 状态机:
 * - 0: 草稿
 * - 1: 待审核
 * - 2: 已发布
 * - 3: 已下架
 */
@Getter
@Setter
@Entity
@Table(name = "cms_article")
@EntityListeners(AuditingEntityListener.class)
public class Article {

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PUBLISHED = 2;
    public static final int STATUS_OFFLINE = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 摘要
     */
    @Column(length = 500)
    private String summary;

    /**
     * 正文(富文本)
     */
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 分类ID
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 封面URL
     */
    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    /**
     * 状态:0-草稿,1-待审核,2-已发布,3-已下架
     */
    @Column(nullable = false)
    private Integer status = STATUS_DRAFT;

    /**
     * 发布时间
     */
    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    /**
     * 下架时间
     */
    @Column(name = "revoke_time")
    private LocalDateTime revokeTime;

    /**
     * 审核人
     */
    @Column(name = "audit_by", length = 50)
    private String auditBy;

    /**
     * 审核时间
     */
    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    /**
     * 所属部门ID
     */
    @Column(name = "dept_id")
    private Long deptId;

    /**
     * 作者用户ID
     */
    @Column(name = "author_id")
    private Long authorId;

    /**
     * 浏览量
     */
    @Column(name = "view_count")
    private Long viewCount = 0L;

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
     * 检查是否可以提交审核
     */
    public boolean canSubmit() {
        return status == STATUS_DRAFT;
    }

    /**
     * 检查是否可以审核
     */
    public boolean canAudit() {
        return status == STATUS_PENDING;
    }

    /**
     * 检查是否可以发布
     */
    public boolean canPublish() {
        return status == STATUS_PENDING || status == STATUS_OFFLINE;
    }

    /**
     * 检查是否可以下架
     */
    public boolean canRevoke() {
        return status == STATUS_PUBLISHED;
    }

    /**
     * 提交审核
     */
    public void submit() {
        if (!canSubmit()) {
            throw new IllegalStateException("当前状态不允许提交审核");
        }
        this.status = STATUS_PENDING;
    }

    /**
     * 审核通过并发布
     */
    public void publish(String auditor) {
        if (!canPublish()) {
            throw new IllegalStateException("当前状态不允许发布");
        }
        this.status = STATUS_PUBLISHED;
        this.publishTime = LocalDateTime.now();
        this.auditBy = auditor;
        this.auditTime = LocalDateTime.now();
    }

    /**
     * 审核驳回
     */
    public void reject(String auditor) {
        if (!canAudit()) {
            throw new IllegalStateException("当前状态不允许驳回");
        }
        this.status = STATUS_DRAFT;
        this.auditBy = auditor;
        this.auditTime = LocalDateTime.now();
    }

    /**
     * 下架
     */
    public void revoke() {
        if (!canRevoke()) {
            throw new IllegalStateException("当前状态不允许下架");
        }
        this.status = STATUS_OFFLINE;
        this.revokeTime = LocalDateTime.now();
    }
}
