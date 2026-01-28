package top.flobby.admin.monitor.interfaces.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 操作日志查询条件
 */
@Data
@Schema(description = "操作日志查询条件")
public class OperationLogQuery {

    @Schema(description = "操作模块")
    private String title;

    @Schema(description = "操作人员")
    private String operName;

    @Schema(description = "业务类型")
    private Integer businessType;

    @Schema(description = "操作状态:0-失败,1-成功")
    private Integer status;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}
