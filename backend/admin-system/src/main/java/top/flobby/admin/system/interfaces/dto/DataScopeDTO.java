package top.flobby.admin.system.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 数据权限配置 DTO
 * <p>
 * 用于未来的数据权限配置 API，预留接口
 *
 * @author flobby
 * @date 2026-01-28
 */
@Data
@Schema(description = "数据权限配置")
public class DataScopeDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "数据权限范围：1-全部，2-本部门及下级，3-仅本部门，4-仅本人")
    private Integer dataScope;

    @Schema(description = "自定义部门ID列表（预留，用于未来的自定义数据权限）")
    private List<Long> deptIds;
}
