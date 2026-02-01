package top.flobby.admin.generator.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.flobby.admin.generator.config.EntityConfig;
import top.flobby.admin.generator.config.FieldConfig;
import top.flobby.admin.generator.config.GeneratorConfig;
import top.flobby.admin.generator.util.NameConverter;
import top.flobby.admin.generator.util.TypeMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 数据库元数据读取器
 */
public class DatabaseReader {

    private static final Logger log = LoggerFactory.getLogger(DatabaseReader.class);

    private final String url;
    private final String username;
    private final String password;
    private final String tablePrefix;

    public DatabaseReader(GeneratorConfig.DatabaseConfig dbConfig, String tablePrefix) {
        this.url = dbConfig.getUrl();
        this.username = dbConfig.getUsername();
        this.password = dbConfig.getPassword();
        this.tablePrefix = tablePrefix;
    }

    /**
     * 获取数据库连接
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 获取所有表名
     */
    public List<String> getAllTables() {
        List<String> tables = new ArrayList<>();
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    tables.add(tableName);
                }
            }
        } catch (SQLException e) {
            log.error("获取表列表失败: {}", e.getMessage());
            throw new RuntimeException("获取表列表失败", e);
        }
        return tables;
    }

    /**
     * 根据模式匹配表名
     *
     * @param patterns 表名模式（支持 * 通配符）
     * @return 匹配的表名列表
     */
    public List<String> getMatchingTables(List<String> patterns) {
        List<String> allTables = getAllTables();
        List<String> matchedTables = new ArrayList<>();

        for (String pattern : patterns) {
            // 将通配符转换为正则表达式
            String regex = pattern.replace("*", ".*");
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

            for (String table : allTables) {
                if (p.matcher(table).matches() && !matchedTables.contains(table)) {
                    matchedTables.add(table);
                }
            }
        }

        return matchedTables;
    }

    /**
     * 读取表结构并转换为 EntityConfig
     *
     * @param tableName 表名
     * @param moduleName 模块名
     * @return 实体配置
     */
    public EntityConfig readTable(String tableName, String moduleName) {
        EntityConfig entity = new EntityConfig();
        entity.setTableName(tableName);
        entity.setClassName(NameConverter.tableNameToClassName(tableName, tablePrefix));
        entity.setModuleName(moduleName);

        try (Connection conn = getConnection()) {
            // 获取表注释
            String tableComment = getTableComment(conn, tableName);
            entity.setComment(tableComment != null ? tableComment : entity.getClassName());

            // 获取主键
            List<String> primaryKeys = getPrimaryKeys(conn, tableName);

            // 获取列信息
            List<FieldConfig> fields = getColumns(conn, tableName, primaryKeys);
            entity.setFields(fields);

            log.info("读取表结构: {} -> {} ({} 个字段)", tableName, entity.getClassName(), fields.size());
        } catch (SQLException e) {
            log.error("读取表结构失败: {} - {}", tableName, e.getMessage());
            throw new RuntimeException("读取表结构失败: " + tableName, e);
        }

        return entity;
    }

    /**
     * 获取表注释
     */
    private String getTableComment(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, conn.getCatalog());
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("TABLE_COMMENT");
                }
            }
        }
        return null;
    }

    /**
     * 获取主键列表
     */
    private List<String> getPrimaryKeys(Connection conn, String tableName) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getPrimaryKeys(conn.getCatalog(), null, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }
        return primaryKeys;
    }

    /**
     * 获取列信息
     */
    private List<FieldConfig> getColumns(Connection conn, String tableName, List<String> primaryKeys) throws SQLException {
        List<FieldConfig> fields = new ArrayList<>();

        String sql = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT, " +
                "CHARACTER_MAXIMUM_LENGTH, COLUMN_KEY " +
                "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, conn.getCatalog());
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FieldConfig field = new FieldConfig();

                    String columnName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("DATA_TYPE");
                    String columnType = rs.getString("COLUMN_TYPE");
                    String isNullable = rs.getString("IS_NULLABLE");
                    String columnDefault = rs.getString("COLUMN_DEFAULT");
                    String columnComment = rs.getString("COLUMN_COMMENT");
                    Long maxLength = rs.getLong("CHARACTER_MAXIMUM_LENGTH");
                    String columnKey = rs.getString("COLUMN_KEY");

                    field.setColumnName(columnName);
                    field.setFieldName(NameConverter.toCamelCase(columnName));
                    field.setFieldType(TypeMapper.toJavaType(dataType));
                    field.setColumnType(columnType);
                    field.setComment(columnComment != null && !columnComment.isEmpty() ? columnComment : field.getFieldName());
                    field.setIsNullable("YES".equalsIgnoreCase(isNullable));
                    field.setIsPrimaryKey(primaryKeys.contains(columnName));
                    field.setIsUnique("UNI".equals(columnKey));
                    field.setDefaultValue(columnDefault);

                    if (maxLength != null && maxLength > 0 && maxLength < Integer.MAX_VALUE) {
                        field.setMaxLength(maxLength.intValue());
                    }

                    // 推断表单类型
                    field.setFormType(TypeMapper.inferFormType(field.getFieldName(), field.getFieldType()));

                    // 推断是否查询、列表、表单字段
                    field.setIsQuery(TypeMapper.shouldBeQueryField(field.getFieldName(), field.getFieldType()));
                    field.setIsList(TypeMapper.shouldBeListField(field.getFieldName()));
                    field.setIsForm(TypeMapper.shouldBeFormField(field.getFieldName()));

                    fields.add(field);
                }
            }
        }

        return fields;
    }

    /**
     * 批量读取表结构
     *
     * @param tableNames 表名列表
     * @param moduleName 模块名
     * @return 实体配置列表
     */
    public List<EntityConfig> readTables(List<String> tableNames, String moduleName) {
        List<EntityConfig> entities = new ArrayList<>();
        for (String tableName : tableNames) {
            try {
                EntityConfig entity = readTable(tableName, moduleName);
                entities.add(entity);
            } catch (Exception e) {
                log.warn("跳过表 {}: {}", tableName, e.getMessage());
            }
        }
        return entities;
    }

    /**
     * 测试数据库连接
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            log.error("数据库连接测试失败: {}", e.getMessage());
            return false;
        }
    }
}
