package ${packageName}.${moduleName}.interfaces.vo;

<#list entity.imports as import>
import ${import};
</#list>
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ${entity.comment!entity.className}VO
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@Schema(description = "${entity.comment!entity.className}VO")
public class ${entity.className}VO {

<#list entity.listFields as field>
    /**
     * ${field.comment!field.fieldName}
     */
    @Schema(description = "${field.comment!field.fieldName}")
    private ${field.fieldType} ${field.fieldName};

</#list>
}
