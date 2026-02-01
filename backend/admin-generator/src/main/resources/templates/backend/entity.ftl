package ${packageName}.${moduleName}.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
<#list entity.imports as import>
<#if import != "java.time.LocalDateTime">
import ${import};
</#if>
</#list>

/**
 * ${entity.comment!entity.className}实体
 *
 * @author ${author}
 * @date ${date}
 */
@Getter
@Setter
@Entity
@Table(name = "${entity.tableName}")
@EntityListeners(AuditingEntityListener.class)
public class ${entity.className} {

<#list entity.fields as field>
<#if field.isPrimaryKey>
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private ${field.fieldType} ${field.fieldName};

<#else>
    /**
     * ${field.comment!field.fieldName}
     */
<#if field.fieldType == "LocalDateTime" && field.fieldName == "createTime">
    @CreatedDate
    @Column(name = "${field.columnName}", updatable = false)
<#elseif field.fieldType == "LocalDateTime" && field.fieldName == "updateTime">
    @LastModifiedDate
    @Column(name = "${field.columnName}")
<#elseif field.fieldType == "String" && field.maxLength??>
    @Column(name = "${field.columnName}"<#if !field.isNullable>, nullable = false</#if>, length = ${field.maxLength})
<#elseif field.fieldType == "String" && field.columnType?? && field.columnType?contains("TEXT")>
    @Column(name = "${field.columnName}", columnDefinition = "${field.columnType}")
<#else>
    @Column(name = "${field.columnName}"<#if !field.isNullable>, nullable = false</#if>)
</#if>
<#if field.fieldType == "LocalDateTime">
    private ${field.fieldType} ${field.fieldName};
<#elseif field.fieldType == "String" && field.defaultValue??>
    private ${field.fieldType} ${field.fieldName} = "${field.defaultValue}";
<#elseif field.defaultValue?? && field.defaultValue != "CURRENT_TIMESTAMP">
<#if field.fieldType == "Long">
    private ${field.fieldType} ${field.fieldName} = ${field.defaultValue}L;
<#else>
    private ${field.fieldType} ${field.fieldName} = ${field.defaultValue};
</#if>
<#else>
    private ${field.fieldType} ${field.fieldName};
</#if>

</#if>
</#list>
}
