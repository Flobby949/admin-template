package top.flobby.admin.system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户部门关联实体
 * <p>
 * 用户与部门多对多关系
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user_dept")
public class UserDept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 部门ID
     */
    @Column(name = "dept_id", nullable = false)
    private Long deptId;
}
