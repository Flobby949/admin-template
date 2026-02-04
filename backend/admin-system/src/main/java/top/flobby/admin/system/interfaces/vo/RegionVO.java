package top.flobby.admin.system.interfaces.vo;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 区域表VO
 *
 * @author Code Generator
 * @date 2026-02-01
 */
@Data
@Schema(description = "区域表VO")
public class RegionVO {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

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
     * 层级：1-省，2-市，3-区县
     */
    @Schema(description = "层级：1-省，2-市，3-区县")
    private Integer level;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

}
