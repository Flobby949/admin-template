package ${packageName}.${moduleName}.infrastructure.repository;

import ${packageName}.${moduleName}.domain.entity.${entity.className};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * ${entity.comment!entity.className} JPA 仓储接口
 *
 * @author ${author}
 * @date ${date}
 */
public interface Jpa${entity.className}Repository extends JpaRepository<${entity.className}, ${entity.primaryKeyType}>,
        JpaSpecificationExecutor<${entity.className}> {

<#list entity.queryFields as field>
    /**
     * 根据${field.comment!field.fieldName}查询
     */
<#if field.fieldType == "String">
    List<${entity.className}> findBy${field.fieldNameCapitalized}Containing(${field.fieldType} ${field.fieldName});
<#else>
    List<${entity.className}> findBy${field.fieldNameCapitalized}(${field.fieldType} ${field.fieldName});
</#if>

</#list>
    /**
     * 根据删除标记查询
     */
    List<${entity.className}> findByDeleted(Integer deleted);
}
