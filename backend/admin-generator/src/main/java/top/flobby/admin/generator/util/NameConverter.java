package top.flobby.admin.generator.util;

/**
 * 命名转换工具类
 */
public final class NameConverter {

    private NameConverter() {
        // 工具类禁止实例化
    }

    /**
     * 下划线转驼峰（首字母小写）
     * 例如：user_name -> userName
     */
    public static String toCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    /**
     * 下划线转帕斯卡（首字母大写）
     * 例如：user_name -> UserName
     */
    public static String toPascalCase(String name) {
        String camelCase = toCamelCase(name);
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        return camelCase.substring(0, 1).toUpperCase() + camelCase.substring(1);
    }

    /**
     * 驼峰转下划线
     * 例如：userName -> user_name
     */
    public static String toSnakeCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 驼峰转连字符（kebab-case）
     * 例如：userName -> user-name
     */
    public static String toKebabCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('-');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * 首字母小写
     */
    public static String uncapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    /**
     * 去除表前缀并转换为类名
     * 例如：sys_user -> User, cms_article -> Article
     */
    public static String tableNameToClassName(String tableName, String tablePrefix) {
        if (tableName == null || tableName.isEmpty()) {
            return tableName;
        }
        String name = tableName;
        // 去除前缀
        if (tablePrefix != null && !tablePrefix.isEmpty()) {
            String[] prefixes = tablePrefix.split(",");
            for (String prefix : prefixes) {
                prefix = prefix.trim();
                if (name.startsWith(prefix)) {
                    name = name.substring(prefix.length());
                    break;
                }
            }
        }
        return toPascalCase(name);
    }

    /**
     * 类名转表名
     * 例如：User -> user, UserRole -> user_role
     */
    public static String classNameToTableName(String className, String tablePrefix) {
        if (className == null || className.isEmpty()) {
            return className;
        }
        String tableName = toSnakeCase(className);
        if (tablePrefix != null && !tablePrefix.isEmpty()) {
            tableName = tablePrefix + tableName;
        }
        return tableName;
    }
}
