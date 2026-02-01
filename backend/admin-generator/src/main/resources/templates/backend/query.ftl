package ${packageName}.${moduleName}.interfaces.query;

<#list entity.imports as import>
import ${import};
</#list>
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ${entity.comment!entity.className}查询条件
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@Schema(description = "${entity.comment!entity.className}查询条件")
public class ${entity.className}Query {

<#list entity.queryFields as field>
    /**
     * ${field.comment!field.fieldName}
     */
    @Schema(description = "${field.comment!field.fieldName}")
    private ${field.fieldType} ${field.fieldName};

</#list>
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
