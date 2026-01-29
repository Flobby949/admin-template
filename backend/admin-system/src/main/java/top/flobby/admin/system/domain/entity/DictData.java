package top.flobby.admin.system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 字典数据实体
 * <p>
 * 存储字典类型下的具体键值对数据
 * </p>
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dict_data",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dict_type", "dict_value"}))
@EntityListeners(AuditingEntityListener.class)
public class DictData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 字典类型
     */
    @Column(name = "dict_type", nullable = false, length = 100)
    private String dictType;

    /**
     * 字典标签(显示值)
     */
    @Column(name = "dict_label", nullable = false, length = 100)
    private String dictLabel;

    /**
     * 字典值(实际值)
     */
    @Column(name = "dict_value", nullable = false, length = 100)
    private String dictValue;

    /**
     * 排序
     */
    @Column(name = "dict_sort")
    private Integer dictSort = 0;

    /**
     * 样式类名(CSS类)
     */
    @Column(name = "css_class", length = 100)
    private String cssClass;

    /**
     * 列表样式(Element Plus Tag type)
     * 可选值: default, primary, success, warning, danger
     */
    @Column(name = "list_class", length = 100)
    private String listClass;

    /**
     * 是否默认：0-否，1-是
     */
    @Column(name = "is_default")
    private Integer isDefault = 0;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;

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
     * 是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }
}
