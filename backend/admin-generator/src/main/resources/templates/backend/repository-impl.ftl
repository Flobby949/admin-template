package ${packageName}.${moduleName}.infrastructure.repository;

import ${packageName}.${moduleName}.domain.entity.${entity.className};
import ${packageName}.${moduleName}.domain.repository.${entity.className}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ${entity.comment!entity.className}仓储实现
 *
 * @author ${author}
 * @date ${date}
 */
@Repository
@RequiredArgsConstructor
public class ${entity.className}RepositoryImpl implements ${entity.className}Repository {

    private final Jpa${entity.className}Repository jpaRepository;

    @Override
    public Optional<${entity.className}> findById(${entity.primaryKeyType} id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<${entity.className}> findAll() {
        return jpaRepository.findByDeleted(0);
    }

    @Override
    public Page<${entity.className}> findAll(Specification<${entity.className}> spec, Pageable pageable) {
        Specification<${entity.className}> notDeleted = (root, query, cb) -> cb.equal(root.get("deleted"), 0);
        Specification<${entity.className}> combined = spec == null ? notDeleted : spec.and(notDeleted);
        return jpaRepository.findAll(combined, pageable);
    }

    @Override
    public ${entity.className} save(${entity.className} entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void deleteById(${entity.primaryKeyType} id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(${entity.primaryKeyType} id) {
        return jpaRepository.existsById(id);
    }
<#list entity.queryFields as field>

    @Override
    public List<${entity.className}> findBy${field.fieldNameCapitalized}(${field.fieldType} ${field.fieldName}) {
<#if field.fieldType == "String">
        return jpaRepository.findBy${field.fieldNameCapitalized}Containing(${field.fieldName});
<#else>
        return jpaRepository.findBy${field.fieldNameCapitalized}(${field.fieldName});
</#if>
    }
</#list>
}
