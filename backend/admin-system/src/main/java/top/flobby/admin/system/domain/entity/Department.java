package top.flobby.admin.system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 部门实体
 * <p>
 * 采用树形结构设计:
 * - parentId: 父部门ID,0表示根节点
 * - ancestors: 祖级路径,格式为 "0,1,2",用于快速查询子树
 * - 最大层级深度: 10
 */
@Getter
@Setter
@Entity
@Table(name = "sys_department")
@EntityListeners(AuditingEntityListener.class)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 父部门ID,0表示根节点
     */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    /**
     * 祖级列表,格式: "0,1,2"
     * 用于快速查询子树
     */
    @Column(length = 500)
    private String ancestors;

    /**
     * 部门名称
     */
    @Column(name = "dept_name", nullable = false, length = 50)
    private String deptName;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 负责人
     */
    @Column(length = 50)
    private String leader;

    /**
     * 联系电话
     */
    @Column(length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;

    /**
     * 状态:0-禁用,1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Column(name = "create_by", length = 50)
    private String createBy;

    /**
     * 更新人
     */
    @Column(name = "update_by", length = 50)
    private String updateBy;

    /**
     * 删除标记:0-未删除,1-已删除
     */
    @Column(nullable = false)
    private Integer deleted = 0;

    /**
     * 租户ID(预留)
     */
    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * 计算部门层级深度
     *
     * @return 层级深度(根节点为1)
     */
    public int getLevel() {
        if (ancestors == null || ancestors.isEmpty()) {
            return 1;
        }
        return ancestors.split(",").length + 1;
    }

    /**
     * 检查是否为根节点
     *
     * @return true-根节点, false-非根节点
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0L;
    }

    /**
     * 检查是否启用
     *
     * @return true-启用, false-禁用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
