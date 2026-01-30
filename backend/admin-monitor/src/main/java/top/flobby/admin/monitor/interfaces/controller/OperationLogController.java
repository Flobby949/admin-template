package top.flobby.admin.monitor.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.flobby.admin.common.core.PageResult;
import top.flobby.admin.common.core.Result;
import top.flobby.admin.monitor.application.OperationLogService;
import top.flobby.admin.monitor.interfaces.query.OperationLogQuery;
import top.flobby.admin.monitor.interfaces.vo.OperationLogVO;

import java.util.List;

/**
 * 操作日志控制器
 * <p>
 * 职责:
 * - 处理操作日志的HTTP请求
 * - 参数验证和DTO转换
 * - 统一异常处理
 * <p>
 * 权限控制:
 * - 所有认证用户可访问
 *
 * @author flobby
 * @date 2026-01-28
 */
@Tag(name = "操作日志", description = "操作日志相关接口")
@RestController
@RequestMapping("/api/monitor/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     * <p>
     * 支持按操作模块、操作人、状态、时间范围等条件查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询操作日志", description = "支持多条件查询和分页")
    @GetMapping
    public Result<PageResult<OperationLogVO>> listOperationLogs(OperationLogQuery query) {
        PageResult<OperationLogVO> result = operationLogService.listOperationLogs(query);
        return Result.success(result);
    }

    /**
     * 获取操作日志详情
     * <p>
     * 根据日志ID查询详细信息,包括完整的请求参数和响应结果
     *
     * @param id 日志ID
     * @return 日志详情
     */
    @Operation(summary = "获取操作日志详情", description = "根据日志ID查询详细信息")
    @GetMapping("/{id}")
    public Result<OperationLogVO> getOperationLogById(
            @Parameter(description = "日志ID", required = true)
            @PathVariable Long id) {
        OperationLogVO vo = operationLogService.getOperationLogById(id);
        return Result.success(vo);
    }

    /**
     * 删除操作日志
     * <p>
     * 物理删除指定的操作日志
     *
     * @param id 日志ID
     * @return 成功标识
     */
    @Operation(summary = "删除操作日志", description = "物理删除指定的操作日志")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOperationLog(
            @Parameter(description = "日志ID", required = true)
            @PathVariable Long id) {
        operationLogService.deleteOperationLog(id);
        return Result.success();
    }

    /**
     * 批量删除操作日志
     * <p>
     * 物理删除多条操作日志
     *
     * @param ids 日志ID列表
     * @return 成功标识
     */
    @Operation(summary = "批量删除操作日志", description = "物理删除多条操作日志")
    @DeleteMapping
    public Result<Void> deleteOperationLogs(
            @Parameter(description = "日志ID列表", required = true)
            @RequestParam List<Long> ids) {
        operationLogService.deleteOperationLogs(ids);
        return Result.success();
    }

    /**
     * 清空所有操作日志
     * <p>
     * 危险操作:删除所有操作日志记录
     *
     * @return 成功标识
     */
    @Operation(summary = "清空所有操作日志", description = "危险操作:删除所有操作日志记录")
    @DeleteMapping("/clear")
    public Result<Void> clearAllOperationLogs() {
        operationLogService.clearAllOperationLogs();
        return Result.success();
    }

    /**
     * 清理历史日志
     * <p>
     * 删除指定天数之前的日志
     *
     * @param days 保留天数
     * @return 成功标识
     */
    @Operation(summary = "清理历史日志", description = "删除指定天数之前的日志")
    @DeleteMapping("/clean")
    public Result<Void> cleanHistoryLogs(
            @Parameter(description = "保留天数", required = true)
            @RequestParam Integer days) {
        operationLogService.cleanHistoryLogs(days);
        return Result.success();
    }
}
