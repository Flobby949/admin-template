package top.flobby.admin.generator.config;

import lombok.Data;

/**
 * 字段配置
 */
@Data
public class FieldConfig {

    /**
     * 数据库列名
     */
    private String columnName;

    /**
     * Java 字段名（camelCase）
     */
    private String fieldName;

    /**
     * Java 类型（如 String, Long, Integer, LocalDateTime）
     */
    private String fieldType;

    /**
     * 字段注释
     */
    private String comment;

    /**
     * 是否主键
     */
    private Boolean isPrimaryKey = false;

    /**
     * 是否可空
     */
    private Boolean isNullable = true;

    /**
     * 是否作为查询条件
     */
    private Boolean isQuery = false;

    /**
     * 是否在列表中显示
     */
    private Boolean isList = true;

    /**
     * 是否在表单中显示
     */
    private Boolean isForm = true;

    /**
     * 表单类型（input, textarea, select, radio, checkbox, datetime, date, number）
     */
    private String formType = "input";

    /**
     * 字典类型（用于 select 类型）
     */
    private String dictType;

    /**
     * 最大长度（用于 String 类型）
     */
    private Integer maxLength;

    /**
     * 数据库列类型（如 VARCHAR, BIGINT）
     */
    private String columnType;

    /**
     * 是否唯一
     */
    private Boolean isUnique = false;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 获取字段名首字母大写形式
     */
    public String getFieldNameCapitalized() {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 是否需要导入类型
     */
    public boolean needsImport() {
        return fieldType != null && (
                fieldType.equals("LocalDateTime") ||
                fieldType.equals("LocalDate") ||
                fieldType.equals("LocalTime") ||
                fieldType.equals("BigDecimal")
        );
    }

    /**
     * 获取完整的 Java 类型（带包名）
     */
    public String getFullFieldType() {
        if (fieldType == null) {
            return null;
        }
        return switch (fieldType) {
            case "LocalDateTime" -> "java.time.LocalDateTime";
            case "LocalDate" -> "java.time.LocalDate";
            case "LocalTime" -> "java.time.LocalTime";
            case "BigDecimal" -> "java.math.BigDecimal";
            default -> fieldType;
        };
    }
}
