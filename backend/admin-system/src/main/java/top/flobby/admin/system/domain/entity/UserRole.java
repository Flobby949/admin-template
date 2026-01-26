package top.flobby.admin.system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user_role", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "role_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    public UserRole() {
    }

    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
}
