package ${packageName}.${moduleName}.application;

<#list entity.imports as import>
import ${import};
</#list>
import ${packageName}.${moduleName}.domain.entity.${entity.className};
import ${packageName}.${moduleName}.domain.repository.${entity.className}Repository;
import ${packageName}.${moduleName}.interfaces.dto.${entity.className}DTO;
import ${packageName}.${moduleName}.interfaces.vo.${entity.className}VO;
<#if entity.hasQueryFields()>
import ${packageName}.${moduleName}.interfaces.query.${entity.className}Query;
</#if>
import ${packageName}.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ${entity.comment!entity.className}服务
 *
 * @author ${author}
 * @date ${date}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ${entity.className}Service {

    private final ${entity.className}Repository ${entity.classNameLower}Repository;

    /**
     * 查询${entity.comment!entity.className}列表
     *
     * @return ${entity.comment!entity.className}列表
     */
    public List<${entity.className}VO> list() {
        List<${entity.className}> list = ${entity.classNameLower}Repository.findAll();
        return list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询${entity.comment!entity.className}
     *
     * @param id 主键ID
     * @return ${entity.comment!entity.className}
     */
    public ${entity.className}VO getById(${entity.primaryKeyType} id) {
        ${entity.className} entity = ${entity.classNameLower}Repository.findById(id)
                .orElseThrow(() -> new BusinessException("${entity.comment!entity.className}不存在"));
        return toVO(entity);
    }

    /**
     * 创建${entity.comment!entity.className}
     *
     * @param dto 创建参数
     * @return 主键ID
     */
    @Transactional
    public ${entity.primaryKeyType} create(${entity.className}DTO dto) {
        ${entity.className} entity = new ${entity.className}();
        copyProperties(dto, entity);
        entity.setDeleted(0);
        ${entity.className} saved = ${entity.classNameLower}Repository.save(entity);
        log.info("创建${entity.comment!entity.className}成功: id={}", saved.getId());
        return saved.getId();
    }

    /**
     * 更新${entity.comment!entity.className}
     *
     * @param dto 更新参数
     */
    @Transactional
    public void update(${entity.className}DTO dto) {
        ${entity.className} entity = ${entity.classNameLower}Repository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException("${entity.comment!entity.className}不存在"));
        copyProperties(dto, entity);
        ${entity.classNameLower}Repository.save(entity);
        log.info("更新${entity.comment!entity.className}成功: id={}", dto.getId());
    }

    /**
     * 删除${entity.comment!entity.className}
     *
     * @param id 主键ID
     */
    @Transactional
    public void delete(${entity.primaryKeyType} id) {
        ${entity.className} entity = ${entity.classNameLower}Repository.findById(id)
                .orElseThrow(() -> new BusinessException("${entity.comment!entity.className}不存在"));
        entity.setDeleted(1);
        ${entity.classNameLower}Repository.save(entity);
        log.info("删除${entity.comment!entity.className}成功: id={}", id);
    }

    /**
     * 实体转VO
     */
    private ${entity.className}VO toVO(${entity.className} entity) {
        ${entity.className}VO vo = new ${entity.className}VO();
<#list entity.listFields as field>
        vo.set${field.fieldNameCapitalized}(entity.get${field.fieldNameCapitalized}());
</#list>
        return vo;
    }

    /**
     * DTO属性复制到实体
     */
    private void copyProperties(${entity.className}DTO dto, ${entity.className} entity) {
<#list entity.formFields as field>
        if (dto.get${field.fieldNameCapitalized}() != null) {
            entity.set${field.fieldNameCapitalized}(dto.get${field.fieldNameCapitalized}());
        }
</#list>
    }
}
