package ${packageName}.${moduleName}.interfaces.query;

<#list entity.imports as import>
import ${import};
</#list>
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ${packageName}.common.core.PageQuery;

/**
 * ${entity.comment!entity.className}查询条件
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "${entity.comment!entity.className}查询条件")
public class ${entity.className}Query extends PageQuery {

<#list entity.queryFields as field>
    /**
     * ${field.comment!field.fieldName}
     */
    @Schema(description = "${field.comment!field.fieldName}")
    private ${field.fieldType} ${field.fieldName};

</#list>
}
