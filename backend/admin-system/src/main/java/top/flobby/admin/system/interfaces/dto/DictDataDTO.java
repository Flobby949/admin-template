package top.flobby.admin.system.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典数据 DTO
 */
@Data
@Schema(description = "字典数据请求")
public class DictDataDTO {

    @Schema(description = "字典数据ID（更新时必填）")
    private Long id;

    @NotBlank(message = "字典类型不能为空")
    @Size(min = 1, max = 100, message = "字典类型长度必须在1-100之间")
    @Schema(description = "字典类型", required = true)
    private String dictType;

    @NotBlank(message = "字典标签不能为空")
    @Size(min = 1, max = 100, message = "字典标签长度必须在1-100之间")
    @Schema(description = "字典标签", required = true)
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    @Size(min = 1, max = 100, message = "字典值长度必须在1-100之间")
    @Schema(description = "字典值", required = true)
    private String dictValue;

    @Min(value = 0, message = "排序必须大于等于0")
    @Schema(description = "排序")
    private Integer dictSort = 0;

    @Schema(description = "样式类名")
    @Size(max = 100, message = "样式类名长度不能超过100")
    private String cssClass;

    @Schema(description = "列表样式(default/primary/success/warning/danger)")
    @Size(max = 100, message = "列表样式长度不能超过100")
    private String listClass;

    @Schema(description = "是否默认：0-否，1-是")
    private Integer isDefault = 0;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status = 1;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
