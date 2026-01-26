package top.flobby.admin.system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 50, name = "real_name")
    private String realName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String avatar;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 登录失败次数
     */
    @Column(nullable = false)
    private Integer loginFailCount = 0;

    /**
     * 锁定时间
     */
    private LocalDateTime lockTime;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Column(length = 50)
    private String createBy;

    /**
     * 更新人
     */
    @Column(length = 50)
    private String updateBy;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @Column(nullable = false)
    private Integer deleted = 0;

    /**
     * 租户ID（预留）
     */
    private Long tenantId;

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 是否锁定
     */
    public boolean isLocked() {
        return this.lockTime != null && this.lockTime.isAfter(LocalDateTime.now());
    }

    /**
     * 重置登录失败次数
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.lockTime = null;
    }

    /**
     * 增加登录失败次数
     */
    public void incrementLoginFailCount() {
        if (this.loginFailCount == null) {
            this.loginFailCount = 0;
        }
        this.loginFailCount++;
    }

    /**
     * 锁定账户
     */
    public void lock(int minutes) {
        this.lockTime = LocalDateTime.now().plusMinutes(minutes);
    }
}
