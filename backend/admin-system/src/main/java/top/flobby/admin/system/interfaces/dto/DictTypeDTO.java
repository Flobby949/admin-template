package top.flobby.admin.system.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典类型 DTO
 */
@Data
@Schema(description = "字典类型请求")
public class DictTypeDTO {

    @Schema(description = "字典类型ID（更新时必填）")
    private Long id;

    @NotBlank(message = "字典名称不能为空")
    @Size(min = 1, max = 100, message = "字典名称长度必须在1-100之间")
    @Schema(description = "字典名称", required = true)
    private String dictName;

    @NotBlank(message = "字典类型不能为空")
    @Size(min = 1, max = 100, message = "字典类型长度必须在1-100之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "字典类型必须以字母开头，只能包含字母、数字和下划线")
    @Schema(description = "字典类型", required = true)
    private String dictType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status = 1;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
