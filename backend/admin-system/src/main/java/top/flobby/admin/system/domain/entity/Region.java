package top.flobby.admin.system.domain.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 区域表实体
 *
 * @author Code Generator
 * @date 2026-02-01
 */
@Getter
@Setter
@Entity
@Table(name = "t_region")
@EntityListeners(AuditingEntityListener.class)
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 区域名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 区域编码
     */
    @Column(name = "region_code", nullable = false, length = 50)
    private String regionCode;

    /**
     * 父级编码
     */
    @Column(name = "parent_code", length = 50)
    private String parentCode = "0";

    /**
     * 层级：1-省，2-市，3-区县
     */
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
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
    @Column(name = "deleted", nullable = false)
    private Integer deleted = 0;

}
