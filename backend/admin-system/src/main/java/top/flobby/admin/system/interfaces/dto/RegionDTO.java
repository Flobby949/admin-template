package top.flobby.admin.system.interfaces.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 区域表DTO
 *
 * @author Code Generator
 * @date 2026-02-01
 */
@Data
@Schema(description = "区域表DTO")
public class RegionDTO {

    @Schema(description = "主键ID")
    private Long id;

    /**
     * 区域名称
     */
    @NotBlank(message = "区域名称不能为空")
    @Size(max = 100, message = "区域名称长度不能超过100")
    @Schema(description = "区域名称")
    private String name;

    /**
     * 区域编码
     */
    @NotBlank(message = "区域编码不能为空")
    @Size(max = 50, message = "区域编码长度不能超过50")
    @Schema(description = "区域编码")
    private String regionCode;

    /**
     * 父级编码
     */
    @Size(max = 50, message = "父级编码长度不能超过50")
    @Schema(description = "父级编码")
    private String parentCode;

    /**
     * 层级：1-省，2-市，3-区县
     */
    @NotNull(message = "层级：1-省，2-市，3-区县不能为空")
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
    @NotNull(message = "状态：0-禁用，1-启用不能为空")
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

}
