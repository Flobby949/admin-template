package top.flobby.admin.system.interfaces.query;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 区域表查询条件
 *
 * @author Code Generator
 * @date 2026-02-01
 */
@Data
@Schema(description = "区域表查询条件")
public class RegionQuery {

    /**
     * 区域名称
     */
    @Schema(description = "区域名称")
    private String name;

    /**
     * 区域编码
     */
    @Schema(description = "区域编码")
    private String regionCode;

    /**
     * 父级编码
     */
    @Schema(description = "父级编码")
    private String parentCode;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 页码
     */
    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", defaultValue = "10")
    private Integer pageSize = 10;
}
