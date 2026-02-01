package top.flobby.admin.cms.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 分类实体
 * <p>
 * 采用树形结构设计:
 * - parentId: 父分类ID,0表示根节点
 * - ancestors: 祖级路径,格式为 "0,1,2",用于快速查询子树
 */
@Getter
@Setter
@Entity
@Table(name = "cms_category")
@EntityListeners(AuditingEntityListener.class)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 父分类ID,0表示根节点
     */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    /**
     * 祖级列表,格式: "0,1,2"
     */
    @Column(length = 500)
    private String ancestors;

    /**
     * 分类名称
     */
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 状态:0-禁用,1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 所属部门ID(用于数据权限)
     */
    @Column(name = "dept_id")
    private Long deptId;

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
     * 计算分类层级深度
     */
    public int getLevel() {
        if (ancestors == null || ancestors.isEmpty()) {
            return 1;
        }
        return ancestors.split(",").length + 1;
    }

    /**
     * 检查是否为根节点
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0L;
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
