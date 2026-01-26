package top.flobby.admin.system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 菜单实体
 */
@Getter
@Setter
@Entity
@Table(name = "sys_menu")
@EntityListeners(AuditingEntityListener.class)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 父菜单ID
     */
    @Column(name = "parent_id")
    private Long parentId = 0L;

    /**
     * 菜单名称
     */
    @Column(name = "menu_name", nullable = false, length = 50)
    private String menuName;

    /**
     * 菜单类型：1-目录，2-菜单，3-按钮
     */
    @Column(name = "menu_type", nullable = false)
    private Integer menuType;

    /**
     * 路由路径
     */
    @Column(name = "route_path", length = 200)
    private String routePath;

    /**
     * 组件路径
     */
    @Column(length = 200)
    private String component;

    /**
     * 权限标识
     */
    @Column(length = 100)
    private String permission;

    /**
     * 图标
     */
    @Column(length = 100)
    private String icon;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 是否可见：0-隐藏，1-显示
     */
    @Column(nullable = false)
    private Integer visible = 1;

    /**
     * 状态：0-禁用，1-启用
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
     * 删除标记：0-未删除，1-已删除
     */
    @Column(nullable = false)
    private Integer deleted = 0;

    /**
     * 租户ID（预留）
     */
    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 是否可见
     */
    public boolean isVisible() {
        return this.visible != null && this.visible == 1;
    }

    /**
     * 是否目录
     */
    public boolean isDirectory() {
        return this.menuType != null && this.menuType == 1;
    }

    /**
     * 是否菜单
     */
    public boolean isMenu() {
        return this.menuType != null && this.menuType == 2;
    }

    /**
     * 是否按钮
     */
    public boolean isButton() {
        return this.menuType != null && this.menuType == 3;
    }
}
