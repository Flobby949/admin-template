package top.flobby.admin.generator.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 类型映射工具类
 * MySQL 类型到 Java 类型的映射
 */
public final class TypeMapper {

    private TypeMapper() {
        // 工具类禁止实例化
    }

    /**
     * MySQL 类型到 Java 类型的映射
     */
    private static final Map<String, String> TYPE_MAP = new HashMap<>();

    static {
        // 整数类型
        TYPE_MAP.put("BIGINT", "Long");
        TYPE_MAP.put("INT", "Integer");
        TYPE_MAP.put("INTEGER", "Integer");
        TYPE_MAP.put("MEDIUMINT", "Integer");
        TYPE_MAP.put("SMALLINT", "Integer");
        TYPE_MAP.put("TINYINT", "Integer");

        // 浮点类型
        TYPE_MAP.put("FLOAT", "Float");
        TYPE_MAP.put("DOUBLE", "Double");
        TYPE_MAP.put("DECIMAL", "BigDecimal");
        TYPE_MAP.put("NUMERIC", "BigDecimal");

        // 字符串类型
        TYPE_MAP.put("CHAR", "String");
        TYPE_MAP.put("VARCHAR", "String");
        TYPE_MAP.put("TEXT", "String");
        TYPE_MAP.put("TINYTEXT", "String");
        TYPE_MAP.put("MEDIUMTEXT", "String");
        TYPE_MAP.put("LONGTEXT", "String");
        TYPE_MAP.put("JSON", "String");
        TYPE_MAP.put("ENUM", "String");
        TYPE_MAP.put("SET", "String");

        // 日期时间类型
        TYPE_MAP.put("DATE", "LocalDate");
        TYPE_MAP.put("TIME", "LocalTime");
        TYPE_MAP.put("DATETIME", "LocalDateTime");
        TYPE_MAP.put("TIMESTAMP", "LocalDateTime");
        TYPE_MAP.put("YEAR", "Integer");

        // 布尔类型
        TYPE_MAP.put("BIT", "Boolean");
        TYPE_MAP.put("BOOLEAN", "Boolean");
        TYPE_MAP.put("BOOL", "Boolean");

        // 二进制类型
        TYPE_MAP.put("BINARY", "byte[]");
        TYPE_MAP.put("VARBINARY", "byte[]");
        TYPE_MAP.put("BLOB", "byte[]");
        TYPE_MAP.put("TINYBLOB", "byte[]");
        TYPE_MAP.put("MEDIUMBLOB", "byte[]");
        TYPE_MAP.put("LONGBLOB", "byte[]");
    }

    /**
     * 根据 MySQL 类型获取 Java 类型
     *
     * @param mysqlType MySQL 类型（如 VARCHAR, BIGINT）
     * @return Java 类型（如 String, Long）
     */
    public static String toJavaType(String mysqlType) {
        if (mysqlType == null || mysqlType.isEmpty()) {
            return "String";
        }
        // 提取基础类型（去除长度等信息）
        String baseType = mysqlType.toUpperCase().split("\\(")[0].trim();
        // 处理 UNSIGNED
        baseType = baseType.replace(" UNSIGNED", "");
        return TYPE_MAP.getOrDefault(baseType, "String");
    }

    /**
     * 根据 MySQL 类型获取 JDBC 类型
     *
     * @param mysqlType MySQL 类型
     * @return JDBC 类型
     */
    public static String toJdbcType(String mysqlType) {
        if (mysqlType == null || mysqlType.isEmpty()) {
            return "VARCHAR";
        }
        String baseType = mysqlType.toUpperCase().split("\\(")[0].trim();
        return switch (baseType) {
            case "BIGINT" -> "BIGINT";
            case "INT", "INTEGER", "MEDIUMINT" -> "INTEGER";
            case "SMALLINT" -> "SMALLINT";
            case "TINYINT" -> "TINYINT";
            case "FLOAT" -> "FLOAT";
            case "DOUBLE" -> "DOUBLE";
            case "DECIMAL", "NUMERIC" -> "DECIMAL";
            case "DATE" -> "DATE";
            case "TIME" -> "TIME";
            case "DATETIME", "TIMESTAMP" -> "TIMESTAMP";
            case "BIT", "BOOLEAN", "BOOL" -> "BOOLEAN";
            case "BLOB", "TINYBLOB", "MEDIUMBLOB", "LONGBLOB" -> "BLOB";
            case "TEXT", "TINYTEXT", "MEDIUMTEXT", "LONGTEXT" -> "LONGVARCHAR";
            default -> "VARCHAR";
        };
    }

    /**
     * 根据字段名推断表单类型
     *
     * @param fieldName 字段名
     * @param javaType  Java 类型
     * @return 表单类型
     */
    public static String inferFormType(String fieldName, String javaType) {
        if (fieldName == null) {
            return "input";
        }
        String lowerName = fieldName.toLowerCase();

        // 根据字段名推断
        if (lowerName.contains("status") || lowerName.contains("type") || lowerName.contains("state")) {
            return "select";
        }
        if (lowerName.contains("content") || lowerName.contains("remark") || lowerName.contains("description")
                || lowerName.contains("memo") || lowerName.contains("note")) {
            return "textarea";
        }
        if (lowerName.contains("time") || lowerName.contains("date")) {
            return "datetime";
        }
        if (lowerName.contains("image") || lowerName.contains("img") || lowerName.contains("avatar")
                || lowerName.contains("logo") || lowerName.contains("photo") || lowerName.contains("picture")) {
            return "image";
        }
        if (lowerName.contains("file") || lowerName.contains("attachment")) {
            return "file";
        }
        if (lowerName.contains("email")) {
            return "email";
        }
        if (lowerName.contains("phone") || lowerName.contains("mobile") || lowerName.contains("tel")) {
            return "phone";
        }
        if (lowerName.contains("url") || lowerName.contains("link") || lowerName.contains("href")) {
            return "url";
        }
        if (lowerName.contains("password") || lowerName.contains("pwd")) {
            return "password";
        }

        // 根据 Java 类型推断
        if (javaType != null) {
            return switch (javaType) {
                case "Integer", "Long", "Float", "Double", "BigDecimal" -> "number";
                case "Boolean" -> "switch";
                case "LocalDateTime" -> "datetime";
                case "LocalDate" -> "date";
                case "LocalTime" -> "time";
                default -> "input";
            };
        }

        return "input";
    }

    /**
     * 判断字段是否应该作为查询条件
     *
     * @param fieldName 字段名
     * @param javaType  Java 类型
     * @return 是否作为查询条件
     */
    public static boolean shouldBeQueryField(String fieldName, String javaType) {
        if (fieldName == null) {
            return false;
        }
        String lowerName = fieldName.toLowerCase();

        // 排除的字段
        if (lowerName.equals("id") || lowerName.contains("password") || lowerName.contains("content")
                || lowerName.contains("remark") || lowerName.contains("description")
                || lowerName.equals("deleted") || lowerName.equals("createby") || lowerName.equals("updateby")
                || lowerName.equals("updatetime") || lowerName.equals("createtime")) {
            return false;
        }

        // 排除日期时间类型（避免生成复杂的查询方法）
        if (javaType != null && (javaType.equals("LocalDateTime") || javaType.equals("LocalDate") || javaType.equals("LocalTime"))) {
            return false;
        }

        // 常见的查询字段
        if (lowerName.contains("name") || lowerName.contains("title") || lowerName.contains("code")
                || lowerName.contains("status") || lowerName.contains("type") || lowerName.contains("state")) {
            return true;
        }

        return false;
    }

    /**
     * 判断字段是否应该在列表中显示
     *
     * @param fieldName 字段名
     * @return 是否在列表中显示
     */
    public static boolean shouldBeListField(String fieldName) {
        if (fieldName == null) {
            return true;
        }
        String lowerName = fieldName.toLowerCase();

        // 排除的字段
        return !lowerName.contains("password") && !lowerName.contains("content")
                && !lowerName.equals("deleted") && !lowerName.equals("updateby")
                && !lowerName.equals("tenantid");
    }

    /**
     * 判断字段是否应该在表单中显示
     *
     * @param fieldName 字段名
     * @return 是否在表单中显示
     */
    public static boolean shouldBeFormField(String fieldName) {
        if (fieldName == null) {
            return true;
        }
        String lowerName = fieldName.toLowerCase();

        // 排除的字段（主键、审计字段、删除标记等）
        return !lowerName.equals("id") && !lowerName.equals("createtime") && !lowerName.equals("updatetime")
                && !lowerName.equals("createby") && !lowerName.equals("updateby")
                && !lowerName.equals("deleted") && !lowerName.equals("tenantid");
    }
}
