package ${packageName}.${moduleName}.interfaces.dto;

<#list entity.imports as import>
import ${import};
</#list>
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * ${entity.comment!entity.className}DTO
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@Schema(description = "${entity.comment!entity.className}DTO")
public class ${entity.className}DTO {

<#list entity.fields as field>
<#if field.isPrimaryKey>
    @Schema(description = "${field.comment!field.fieldName}")
    private ${field.fieldType} ${field.fieldName};

<#elseif field.isForm>
    /**
     * ${field.comment!field.fieldName}
     */
<#if !field.isNullable && field.fieldType == "String">
    @NotBlank(message = "${field.comment!field.fieldName}不能为空")
<#elseif !field.isNullable>
    @NotNull(message = "${field.comment!field.fieldName}不能为空")
</#if>
<#if field.maxLength??>
    @Size(max = ${field.maxLength}, message = "${field.comment!field.fieldName}长度不能超过${field.maxLength}")
</#if>
    @Schema(description = "${field.comment!field.fieldName}")
    private ${field.fieldType} ${field.fieldName};

</#if>
</#list>
}
