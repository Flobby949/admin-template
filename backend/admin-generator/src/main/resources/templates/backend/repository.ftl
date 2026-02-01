package ${packageName}.${moduleName}.domain.repository;

import ${packageName}.${moduleName}.domain.entity.${entity.className};

import java.util.List;
import java.util.Optional;

/**
 * ${entity.comment!entity.className}仓储接口
 *
 * @author ${author}
 * @date ${date}
 */
public interface ${entity.className}Repository {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 实体
     */
    Optional<${entity.className}> findById(${entity.primaryKeyType} id);

    /**
     * 查询所有
     *
     * @return 实体列表
     */
    List<${entity.className}> findAll();

    /**
     * 保存实体
     *
     * @param entity 实体
     * @return 保存后的实体
     */
    ${entity.className} save(${entity.className} entity);

    /**
     * 根据ID删除
     *
     * @param id 主键ID
     */
    void deleteById(${entity.primaryKeyType} id);

    /**
     * 根据ID判断是否存在
     *
     * @param id 主键ID
     * @return 是否存在
     */
    boolean existsById(${entity.primaryKeyType} id);
<#list entity.queryFields as field>

    /**
     * 根据${field.comment!field.fieldName}查询
     *
     * @param ${field.fieldName} ${field.comment!field.fieldName}
     * @return 实体列表
     */
    List<${entity.className}> findBy${field.fieldNameCapitalized}(${field.fieldType} ${field.fieldName});
</#list>
}
