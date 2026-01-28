package top.flobby.admin.monitor.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 * <p>
 * 用于记录用户的操作行为,支持审计和问题追溯
 */
@Getter
@Setter
@Entity
@Table(name = "sys_oper_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作模块
     */
    @Column(length = 50)
    private String title;

    /**
     * 业务类型:1-新增,2-修改,3-删除,4-查询,5-导出,6-导入
     */
    @Column(name = "business_type")
    private Integer businessType;

    /**
     * 方法名称
     */
    @Column(length = 200)
    private String method;

    /**
     * 请求方式
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    /**
     * 操作类别:1-后台用户,2-手机端用户
     */
    @Column(name = "operator_type")
    private Integer operatorType;

    /**
     * 操作人员
     */
    @Column(name = "oper_name", length = 50)
    private String operName;

    /**
     * 部门名称
     */
    @Column(name = "dept_name", length = 50)
    private String deptName;

    /**
     * 请求URL
     */
    @Column(name = "oper_url", length = 500)
    private String operUrl;

    /**
     * 操作IP
     */
    @Column(name = "oper_ip", length = 50)
    private String operIp;

    /**
     * 操作地点
     */
    @Column(name = "oper_location", length = 255)
    private String operLocation;

    /**
     * 请求参数
     */
    @Column(name = "oper_param", columnDefinition = "TEXT")
    private String operParam;

    /**
     * 返回结果
     */
    @Column(name = "json_result", columnDefinition = "TEXT")
    private String jsonResult;

    /**
     * 操作状态:0-失败,1-成功
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 错误消息
     */
    @Column(name = "error_msg", length = 2000)
    private String errorMsg;

    /**
     * 操作时间
     */
    @Column(name = "oper_time")
    private LocalDateTime operTime;

    /**
     * 消耗时间(毫秒)
     */
    @Column(name = "cost_time")
    private Long costTime;

    /**
     * 租户ID(预留)
     */
    @Column(name = "tenant_id")
    private Long tenantId;
}
