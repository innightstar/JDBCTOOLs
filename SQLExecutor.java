package com.HomyStayWeb.Tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL执行器（职责单一的SQL操作类）
 *
 * 功能概述：
 * 1. 执行各类SQL语句（增删改查）
 * 2. 支持事务操作
 * 3. 支持批量操作
 * 4. 结果集自动映射
 *
 * 设计原则：
 * - 单一职责原则（仅负责SQL执行）
 * - 开闭原则（通过SQLPrepared扩展SQL类型）
 * - 依赖倒置（依赖ConnectionManager抽象）
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SQLExecutor {
    private final ConnectionManager connectionManager=new ConnectionManager(); // 连接管理器
    private Connection connection; // 当前使用的连接（可外部注入）

    /**
     * SQL预编译封装类
     */
    @Data
    public static class SQLPrepared {
        private final String sql;   // SQL语句
        private final Object[] params; // 参数数组

        public SQLPrepared(String sql, Object... params) {
            if (sql == null || sql.trim().isEmpty()) {
                throw new IllegalArgumentException("SQL语句不能为空");
            }
            this.sql = sql;
            this.params = params != null ? params : new Object[0];
        }
    }

    // ==================== 基础操作方法 ====================

    /**
     * 执行更新操作（预编译形式）
     */
    public int executeUpdate(SQLPrepared sqlPrepared) throws SQLException {
        validateSQLPrepared(sqlPrepared);
        return executeUpdate(sqlPrepared.getSql(), sqlPrepared.getParams());
    }

    /**
     * 执行更新操作（原始SQL形式）
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        ensureConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeUpdate();
        }
    }

    /**
     * 执行查询操作（预编译形式）
     */
    public <T> List<T> executeQuery(SQLPrepared sqlPrepared, Class<T> resultType) throws SQLException {
        validateSQLPrepared(sqlPrepared);
        return executeQuery(sqlPrepared.getSql(), resultType, sqlPrepared.getParams());
    }

    /**
     * 执行查询操作（原始SQL形式）
     */
    public <T> List<T> executeQuery(String sql, Class<T> resultType, Object... params) throws SQLException {
        ensureConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return isSimpleType(resultType) ?
                        mapSimpleResults(rs, resultType) :
                        mapToObjects(rs, resultType);
            }
        }
    }

    // ==================== 事务操作方法 ====================

    /**
     * 执行事务（预编译形式）
     */
    public boolean executeTransaction(SQLPrepared... sqlPrepares) throws SQLException {
        if (sqlPrepares == null || sqlPrepares.length == 0) return true;

        // 转换为数组形式
        String[] SQLs = Arrays.stream(sqlPrepares)
                .peek(this::validateSQLPrepared)
                .map(SQLPrepared::getSql)
                .toArray(String[]::new);

        Object[][] params = Arrays.stream(sqlPrepares)
                .map(SQLPrepared::getParams)
                .toArray(Object[][]::new);

        return executeTransaction(SQLs, params);
    }

    /**
     * 执行事务（原始SQL形式）
     */
    public boolean executeTransaction(String[] SQLs, Object[][] params) throws SQLException {
        validateTransactionParams(SQLs, params);
        ensureConnection();

        try {
            connection.setAutoCommit(false);
            executeStatements(connection, SQLs, params);
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // ==================== 批量操作方法 ====================

    /**
     * 执行批量操作（预编译形式）
     */
    public boolean executeBatch(SQLPrepared[] sqlPrepares) throws SQLException {
        if (sqlPrepares == null || sqlPrepares.length == 0) return false;

        // 转换为数组形式
        String[] SQLs = Arrays.stream(sqlPrepares)
                .peek(this::validateSQLPrepared)
                .map(SQLPrepared::getSql)
                .toArray(String[]::new);

        Object[][] params = Arrays.stream(sqlPrepares)
                .map(SQLPrepared::getParams)
                .toArray(Object[][]::new);

        return executeBatch(SQLs, params);
    }

    /**
     * 执行批量操作（原始SQL形式）
     */
    public boolean executeBatch(String[] SQLs, Object[][] params) throws SQLException {
        validateBatchParams(SQLs, params);
        ensureConnection();

        try {
            connection.setAutoCommit(false);
            batchExecuteGroupedStatements(connection, groupStatements(SQLs, params));
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException("批量执行失败", e);
        } finally {
            connection.setAutoCommit(true);
        }
    }
    public <T> boolean ResetIdForPrimaryKey(String tableName, Class<T> target) {
        // 查询语句：按当前ID排序获取所有记录（表名直接拼接，因SQL中表名不能参数化）
        String sql = "SELECT * FROM " + tableName + " ORDER BY id";
        SQLPrepared sqlPrepared = new SQLPrepared(sql); // 表名已拼接，无需参数

        try {
            List<T> list = executeQuery(sqlPrepared, target);
            if (!list.isEmpty()) {
                int newId = 1; // 从1开始重新编号

                // 遍历每条记录并更新ID
                for (T t : list) {
                    // 通过反射获取对象的旧ID（假设有getId()方法）
                    int oldId;
                    try {
                        Method getIdMethod = target.getMethod("getId");
                        oldId = (int) getIdMethod.invoke(t);
                    } catch (Exception e) {
                        throw new RuntimeException("无法访问ID字段", e);
                    }

                    // 仅当ID需要变更时才更新
                    if (oldId != newId) {
                        String updateSql = "UPDATE " + tableName + " SET id = ? WHERE id = ?";
                        SQLPrepared updatePrepared = new SQLPrepared(updateSql, newId, oldId);
                        executeUpdate(updatePrepared); // 执行更新
                    }
                    newId++;
                }
                return true; // 操作成功
            }
            return false; // 无记录需要更新
        } catch (SQLException e) {
            throw new RuntimeException("重置ID失败", e);
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * 确保连接可用
     */
    private void ensureConnection() throws SQLException {
        if (connection == null) {
            connection = connectionManager.getConnection();
        }
    }

    /**
     * 验证SQLPrepared参数
     */
    private void validateSQLPrepared(SQLPrepared sqlPrepared) {
        if (sqlPrepared == null) {
            throw new IllegalArgumentException("SQLPrepared不能为null");
        }
    }

    /**
     * 验证事务参数
     */
    private void validateTransactionParams(String[] SQLs, Object[][] params) {
        if (SQLs == null || params == null) {
            throw new IllegalArgumentException("参数数组不能为null");
        }
        if (SQLs.length != params.length) {
            throw new IllegalArgumentException("SQL数量与参数数量不匹配");
        }
    }

    /**
     * 验证批量操作参数
     */
    private void validateBatchParams(String[] SQLs, Object[][] params) {
        validateTransactionParams(SQLs, params);
        if (SQLs.length == 0) {
            throw new IllegalArgumentException("无有效SQL语句");
        }
    }

    /**
     * 设置预处理语句参数
     */
    private void setParameters(PreparedStatement ps, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }

    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(String.class) ||
                Number.class.isAssignableFrom(type) ||
                type.equals(Boolean.class) ||
                type.equals(Character.class);
    }

    /**
     * 映射简单类型结果集
     */
    private <T> List<T> mapSimpleResults(ResultSet rs, Class<T> type) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapSimpleType(rs, type));
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private <T> T mapSimpleType(ResultSet rs, Class<T> type) throws SQLException {
        if (type.equals(String.class)) return (T) rs.getString(1);
        if (type.equals(Integer.class) || type.equals(int.class)) return (T) Integer.valueOf(rs.getInt(1));
        if (type.equals(Long.class) || type.equals(long.class)) return (T) Long.valueOf(rs.getLong(1));
        if (type.equals(Double.class) || type.equals(double.class))
            return (T) Double.valueOf(rs.getDouble(1));
        if (type.equals(Float.class) || type.equals(float.class)) return (T) Float.valueOf(rs.getFloat(1));
        if (type.equals(Boolean.class) || type.equals(boolean.class))
            return (T) Boolean.valueOf(rs.getBoolean(1));
        if (type.equals(Short.class) || type.equals(short.class)) return (T) Short.valueOf(rs.getShort(1));
        if (type.equals(Byte.class) || type.equals(byte.class)) return (T) Byte.valueOf(rs.getByte(1));
        if (type.equals(Character.class) || type.equals(char.class)) {
            String s = rs.getString(1);
            return (T) Character.valueOf(s != null && !s.isEmpty() ? s.charAt(0) : '\0');
        }
        throw new SQLException("不支持的简单类型: " + type.getName());
    }

    private <T> List<T> mapToObjects(ResultSet rs, Class<T> resultType) throws SQLException {
        try {
            List<T> results = new ArrayList<>();
            ResultSetMetaData metaData = rs.getMetaData();
            Map<String, Field> fieldMap = getFieldMap(resultType);

            while (rs.next()) {
                T instance = resultType.getDeclaredConstructor().newInstance();
                populateInstance(instance, rs, metaData, fieldMap);
                results.add(instance);
            }
            return results;
        } catch (Exception e) {
            throw new SQLException("对象映射失败", e);
        }
    }

    private Map<String, Field> getFieldMap(Class<?> type) {
        Map<String, Field> fieldMap = new ConcurrentHashMap<>();
        for (Field field : type.getDeclaredFields()) {
            fieldMap.put(field.getName().toLowerCase(), field);
        }
        return fieldMap;
    }

    private <T> void populateInstance(T instance, ResultSet rs, ResultSetMetaData metaData, Map<String, Field> fieldMap) throws Exception {
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnLabel(i).toLowerCase();
            Field field = fieldMap.get(columnName);
            if (field != null) {
                field.setAccessible(true);
                field.set(instance, convertValue(rs.getObject(i), field.getType()));
            }
        }
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (value instanceof Number) {
            return convertNumber((Number) value, targetType);
        }

        if (value instanceof java.sql.Date && targetType == java.util.Date.class) {
            return new java.util.Date(((java.sql.Date) value).getTime());
        }

        if (value instanceof Timestamp && targetType == java.util.Date.class) {
            return new java.util.Date(((Timestamp) value).getTime());
        }

        return value;
    }

    private Object convertNumber(Number number, Class<?> targetType) {
        if (targetType == int.class || targetType == Integer.class) return number.intValue();
        if (targetType == long.class || targetType == Long.class) return number.longValue();
        if (targetType == double.class || targetType == Double.class) return number.doubleValue();
        if (targetType == float.class || targetType == Float.class) return number.floatValue();
        return number;
    }

    private void executeStatements(Connection conn, String[] SQLs, Object[][] params) throws SQLException {
        for (int i = 0; i < SQLs.length; i++) {
            try (PreparedStatement ps = conn.prepareStatement(SQLs[i])) {
                setParameters(ps, params[i]);
                ps.executeUpdate();
            }
        }
    }

    private Map<String, List<Object[]>> groupStatements(String[] SQLs, Object[][] params) {
        Map<String, List<Object[]>> groups = new HashMap<>();
        for (int i = 0; i < SQLs.length; i++) {
            groups.computeIfAbsent(SQLs[i], k -> new ArrayList<>()).add(params[i]);
        }
        return groups;
    }

    private void batchExecuteGroupedStatements(Connection conn, Map<String, List<Object[]>> groups) throws SQLException {
        for (Map.Entry<String, List<Object[]>> entry : groups.entrySet()) {
            try (PreparedStatement ps = conn.prepareStatement(entry.getKey())) {
                for (Object[] paramGroup : entry.getValue()) {
                    setParameters(ps, paramGroup);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }
}
/*SQLExecutor
 ├── 核心成员
 │   ├── ConnectionManager - 连接管理器（依赖注入）
 │   └── Connection - 当前连接（可外部注入）
 │
 ├── SQLPrepared（内部类）
 │   ├── sql - SQL语句
 │   └── params - 参数数组
 │
 ├── 基础操作方法
 │   ├── executeUpdate() - 执行更新
 │   │   ├── 预编译形式
 │   │   └── 原始SQL形式
 │   │
 │   └── executeQuery() - 执行查询
 │       ├── 预编译形式
 │       └── 原始SQL形式
 │
 ├── 事务操作方法
 │   ├── executeTransaction() - 执行事务
 │   │   ├── 预编译形式
 │   │   └── 原始SQL形式
 │   │
 │   └── 事务控制
 │       ├── 自动提交设置
 │       ├── 提交/回滚
 │       └── 异常处理
 │
 ├── 批量操作方法
 │   ├── executeBatch() - 批量执行
 │   │   ├── 预编译形式
 │   │   └── 原始SQL形式
 │   │
 │   └── 批处理优化
 │       ├── SQL分组
 │       └── 批量执行
 │
 ├── 结果集映射
 │   ├── 简单类型映射
 │   │   ├── 数值类型
 │   │   ├── 字符串
 │   │   └── 布尔值
 │   │
 │   └── 对象映射
 │       ├── 反射创建实例
 │       ├── 字段映射
 │       └── 类型转换
 │
 └── 工具方法
 ├── 参数校验
 ├── 连接确保
 ├── 参数设置
 └── 异常处理
 */