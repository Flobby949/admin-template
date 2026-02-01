package top.flobby.admin.generator.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 实体配置
 */
@Data
public class EntityConfig {

    /**
     * 数据库表名
     */
    private String tableName;

    /**
     * 类名（PascalCase）
     */
    private String className;

    /**
     * 实体注释
     */
    private String comment;

    /**
     * 模块名（用于包路径）
     */
    private String moduleName;

    /**
     * 字段列表
     */
    private List<FieldConfig> fields = new ArrayList<>();

    /**
     * 获取类名首字母小写形式（camelCase）
     */
    public String getClassNameLower() {
        if (className == null || className.isEmpty()) {
            return className;
        }
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    /**
     * 获取主键字段
     */
    public FieldConfig getPrimaryKeyField() {
        return fields.stream()
                .filter(f -> Boolean.TRUE.equals(f.getIsPrimaryKey()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取主键类型
     */
    public String getPrimaryKeyType() {
        FieldConfig pk = getPrimaryKeyField();
        return pk != null ? pk.getFieldType() : "Long";
    }

    /**
     * 获取查询字段列表
     */
    public List<FieldConfig> getQueryFields() {
        return fields.stream()
                .filter(f -> Boolean.TRUE.equals(f.getIsQuery()))
                .toList();
    }

    /**
     * 获取列表显示字段
     */
    public List<FieldConfig> getListFields() {
        return fields.stream()
                .filter(f -> Boolean.TRUE.equals(f.getIsList()))
                .toList();
    }

    /**
     * 获取表单字段
     */
    public List<FieldConfig> getFormFields() {
        return fields.stream()
                .filter(f -> Boolean.TRUE.equals(f.getIsForm()))
                .toList();
    }

    /**
     * 获取非主键字段
     */
    public List<FieldConfig> getNonPrimaryKeyFields() {
        return fields.stream()
                .filter(f -> !Boolean.TRUE.equals(f.getIsPrimaryKey()))
                .toList();
    }

    /**
     * 获取需要导入的类型集合
     */
    public Set<String> getImports() {
        Set<String> imports = new TreeSet<>();
        for (FieldConfig field : fields) {
            if (field.needsImport()) {
                imports.add(field.getFullFieldType());
            }
        }
        return imports;
    }

    /**
     * 是否有查询字段
     */
    public boolean hasQueryFields() {
        return fields.stream().anyMatch(f -> Boolean.TRUE.equals(f.getIsQuery()));
    }

    /**
     * 是否有字典类型字段
     */
    public boolean hasDictFields() {
        return fields.stream().anyMatch(f -> f.getDictType() != null && !f.getDictType().isEmpty());
    }

    /**
     * 获取字典类型字段
     */
    public List<FieldConfig> getDictFields() {
        return fields.stream()
                .filter(f -> f.getDictType() != null && !f.getDictType().isEmpty())
                .toList();
    }

    /**
     * 获取 API 路径（小写，连字符分隔）
     */
    public String getApiPath() {
        if (className == null) {
            return "";
        }
        // 将 PascalCase 转换为 kebab-case
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < className.length(); i++) {
            char c = className.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('-');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 获取权限前缀
     */
    public String getPermissionPrefix() {
        return moduleName + ":" + getClassNameLower();
    }
}
