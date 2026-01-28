package top.flobby.admin.monitor.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志VO
 */
@Data
@Schema(description = "操作日志VO")
public class OperationLogVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "操作模块")
    private String title;

    @Schema(description = "业务类型:1-新增,2-修改,3-删除,4-查询,5-导出,6-导入")
    private Integer businessType;

    @Schema(description = "方法名称")
    private String method;

    @Schema(description = "请求方式")
    private String requestMethod;

    @Schema(description = "操作类别:1-后台用户,2-手机端用户")
    private Integer operatorType;

    @Schema(description = "操作人员")
    private String operName;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "请求URL")
    private String operUrl;

    @Schema(description = "操作IP")
    private String operIp;

    @Schema(description = "操作地点")
    private String operLocation;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "返回结果")
    private String jsonResult;

    @Schema(description = "操作状态:0-失败,1-成功")
    private Integer status;

    @Schema(description = "错误消息")
    private String errorMsg;

    @Schema(description = "操作时间")
    private LocalDateTime operTime;

    @Schema(description = "消耗时间(毫秒)")
    private Long costTime;
}
