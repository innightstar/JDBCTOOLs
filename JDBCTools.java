package com.HomyStayWeb.Tools;


import lombok.Getter;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * JDBC数据库工具类
 * <p>
 * 版本演进记录：
 * <p>
 * Demo1 - 基础版本：
 * 1. 初始版本实现基本的JDBC操作
 * 2. 重写select方法，利用反射机制将ResultSet自动映射到Java对象
 * 3. 查询结果直接返回对象或对象列表
 * <p>
 * Demo2 - 连接池版本：
 * 1. 增加数据库连接池支持
 * 2. 使用BlockingQueue实现简单的连接池
 * 3. 添加连接池初始化方法initializeConnectionPool
 * 4. 实现连接复用和有效性检查
 * <p>
 * Demo3 - 双模态连接版本：
 * 1. 增加连接池和单连接双模态支持
 * 2. 新增connectionPoolEnabled开关控制连接模式
 * 3. 使用ThreadLocal管理线程专属连接
 * <p>
 * Demo4 - 多线程安全版本：
 * 1. 将静态类更新为实例类以适应多线程环境
 * 2. 增加ReentrantLock保证连接池线程安全
 * 3. 使用双重检查锁定模式初始化连接池
 * <p>
 * Demo5 - SQLPrepared重构版本：
 * 1. 引入SQLPrepared类封装SQL和参数
 * 2. 统一所有数据库操作方法参数类型
 * 3. 增强事务和批量操作支持
 * 4. 改进参数校验和空值处理
 * <p>
 * Demo6 - SQLPrepared封装版本：
 * 1. 新增SQLPrepared提供流畅的SQL构建方式
 * 2. 支持链式调用构建复杂SQL语句
 * 3. 增强SQL语句的可读性和可维护性
 * 4. 提供条件、排序、分页等常用操作封装
 * <p>
 * Demo7 - 基本类型映射修复版本：
 * 1. 修复查询结果不能映射到基本类型的bug
 * 2. 增强ResultSet到Java类型的转换逻辑
 * 3. 支持基本类型及其包装类的自动转换
 * 4. 改进类型推断和异常处理机制
 */

public class JDBCTools {
    // 静态配置
    private static final String url;
    private static final String username;
    private static final String password;
    private static final String driverClassName;
    private static int Active_connections;

    // 连接池配置
    private static int POOL_SIZE = 10;
    private static final BlockingQueue<Connection> connectionPool = new LinkedBlockingQueue<>(POOL_SIZE);
    private static final Lock poolInitLock = new ReentrantLock();
    private static volatile boolean poolInitialized = false;


    // 初始化配置
    static {
        try (InputStream in = JDBCTools.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            prop.load(in);
            url = prop.getProperty("jdbc.url");
            username = prop.getProperty("jdbc.username");
            password = prop.getProperty("jdbc.password");
            driverClassName = prop.getProperty("jdbc.driverClassName");
            initializeConnectionPool();
        } catch (Exception e) {
            throw new RuntimeException("数据库配置初始化失败", e);
        }
    }

    /**
     * -- GETTER --
     *  检查当前是否处于连接池模式
     *
     *
     */
    // 实例成员
    @Getter
    private boolean connectionPoolEnabled = false;
    private final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();

    /**
     * 启用或禁用连接池模式
     *
     * @param enabled true-使用连接池模式，false-使用单连接模式
     * @modified Demo4 (增加线程安全处理)
     * @since Demo3 (双模态连接版本引入)
     */
    public void setConnectionPoolEnabled(boolean enabled) {
        this.connectionPoolEnabled = enabled;
        if (!enabled) {
            // 当切换到单连接模式时，清理可能存在的连接池连接
            cleanupThreadResources();
        }
    }

    /**
     * 切换到连接池模式（等效于setConnectionPoolEnabled(true)）
     *
     * @since Demo3 (双模态连接版本引入)
     */
    public void enableConnectionPool() {
        setConnectionPoolEnabled(true);
    }

    /**
     * 切换到单连接模式（等效于setConnectionPoolEnabled(false)）
     *
     * @since Demo3 (双模态连接版本引入)
     */
    public void disableConnectionPool() {
        setConnectionPoolEnabled(false);
    }
    /**
     * 初始化连接池
     */
    private static void initializeConnectionPool() throws SQLException {
        if (poolInitialized) return;

        poolInitLock.lock();
        try {
            if (!poolInitialized) {
                Class.forName(driverClassName);
                connectionPool.clear();
                for (int i = 0; i < POOL_SIZE; i++) {
                    connectionPool.offer(DriverManager.getConnection(url, username, password));
                }
                poolInitialized = true;
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC驱动加载失败", e);
        } finally {
            poolInitLock.unlock();
        }
    }

    /**
     * 设置连接池大小
     */
    public static boolean setPoolSize(int poolSize) throws SQLException {
        if (poolSize <= 0) return false;

        synchronized (JDBCTools.class) {
            shutdownPool();
            POOL_SIZE = poolSize;
            initializeConnectionPool();
            return true;
        }
    }
    public static void getActive_connections() {
        for (Connection connection : connectionPool) if(connection!=null) Active_connections++;
    }
    public static  double getUsageRate() {
        return POOL_SIZE == 0 ? 0 : Active_connections * 100.0 / POOL_SIZE;
    }

    /**
     * 关闭连接池
     */
    public static void shutdownPool() {
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            try {
                if (!conn.isClosed()) conn.close();
            } catch (SQLException e) {
                System.err.println("关闭连接时出错: " + e.getMessage());
            }
        }
        poolInitialized = false;
    }

    /**
     * 获取数据库连接
     */
    public Connection getConnection() throws SQLException {
        if (connectionPoolEnabled) {
            try {
                Connection conn = connectionPool.poll(2, TimeUnit.SECONDS);
                if (conn == null) throw new SQLTimeoutException("获取连接超时");
                if (conn.isClosed() || !conn.isValid(2)) {
                    return createNewConnection();
                }
                return conn;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("获取连接被中断", e);
            }
        } else {
            Connection conn = threadLocalConnection.get();
            if (conn == null || conn.isClosed()) {
                conn = createNewConnection();
                threadLocalConnection.set(conn);
            }
            return conn;
        }
    }

    /**
     * 创建新连接（基础实现）
     */
    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 释放连接
     */
    public void releaseConnection(Connection conn) {
        if (conn == null) return;

        try {
            if (connectionPoolEnabled && !conn.isClosed() && conn.isValid(2)) {
                connectionPool.offer(conn);
            }
        } catch (SQLException e) {
            System.err.println("连接有效性检查失败: " + e.getMessage());
        }
    }

    public int executeUpdate(SQLPrepared sqlPrepared) throws SQLException {
        // 参数校验
        if (sqlPrepared == null) {
            throw new IllegalArgumentException("SQLPrepared不能为null");
        }

        // 调用底层执行方法
        return executeUpdate(sqlPrepared.getSql(), sqlPrepared.getParams());
    }

    /**
     * 实际执行SQL更新的底层方法
     *
     * @param sql    SQL语句
     * @param params 参数列表
     * @return 受影响的行数
     * @throws SQLException 如果数据库操作出错
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            // 获取数据库连接
            conn = getConnection();
            // 创建预处理语句
            ps = conn.prepareStatement(sql);

            // 设置参数
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            // 执行更新
            return ps.executeUpdate();
        } finally {
            // 确保资源释放
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    // 记录日志但不抛出，确保连接能被关闭
                    System.err.println("关闭PreparedStatement失败");
                }
            }
            releaseConnection(conn);
        }
    }
    /**
     * 执行一个包含多个SQL语句的事务
     * 如果任何语句执行失败，则整个事务回滚
     *
     * @param PreparedSql 可变参数的SQLPrepared对象数组，包含SQL语句和参数
     * @return true表示事务执行成功，false表示没有提供任何语句
     * @throws SQLException 如果执行过程中发生数据库错误
     */
    public boolean executeTransaction(SQLPrepared... PreparedSql) throws SQLException {
        // 空参数检查
        if (PreparedSql == null || PreparedSql.length == 0) {
            return true;
        }

        // 转换为另一种参数格式调用核心实现
        String[] SQLs = new String[PreparedSql.length];
        Object[][] params = new Object[PreparedSql.length][];

        for (int i = 0; i < PreparedSql.length; i++) {
            if (PreparedSql[i] == null) {
                throw new IllegalArgumentException("第" + (i + 1) + "个SQLPrepared不能为null");
            }
            SQLs[i] = PreparedSql[i].getSql();
            params[i] = PreparedSql[i].getParams();
        }

        return executeTransaction(SQLs, params);
    }

    /**
     * 执行事务（原始参数版本）
     *
     * @param SQLs   SQL语句数组
     * @param params 对应的参数二维数组
     * @return 执行成功返回true
     * @throws SQLException             数据库操作异常
     * @throws IllegalArgumentException 参数不合法
     */
    public boolean executeTransaction(String[] SQLs, Object[][] params) throws SQLException {
        // 参数校验
        if (SQLs == null || params == null) {
            throw new IllegalArgumentException("参数数组不能为null");
        }
        if (SQLs.length != params.length) {
            throw new IllegalArgumentException("SQL数量与参数数量不匹配");
        }
        if (SQLs.length == 0) {
            return true;
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try {
                for (int i = 0; i < SQLs.length; i++) {
                    if (SQLs[i] == null) {
                        throw new IllegalArgumentException("第" + (i + 1) + "个SQL不能为null");
                    }

                    try (PreparedStatement ps = conn.prepareStatement(SQLs[i])) {
                        setParameters(ps, params[i]);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println(e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } finally {
            releaseConnection(conn);
        }
    }

    /**
     * 执行批量SQL操作（支持事务）
     *
     * @param sqlPrepares SQLPrepared对象数组
     * @return true表示执行成功，false表示无有效操作
     * @throws RuntimeException 执行失败时抛出（包含原始SQLException）
     */
    public boolean executeBatch(SQLPrepared[] sqlPrepares) {
        // 空输入检查
        if (sqlPrepares == null || sqlPrepares.length == 0) {
            return false;
        }

        // 转换为统一参数格式
        String[] SQLs = new String[sqlPrepares.length];
        Object[][] params = new Object[sqlPrepares.length][];

        for (int i = 0; i < sqlPrepares.length; i++) {
            if (sqlPrepares[i] == null) {
                throw new IllegalArgumentException("第" + (i + 1) + "个SQLPrepared不能为null");
            }
            SQLs[i] = sqlPrepares[i].getSql();
            params[i] = sqlPrepares[i].getParams();
        }

        return executeBatch(SQLs, params);
    }

    /**
     * 执行批量SQL操作（核心实现）
     *
     * @param SQLs   SQL语句数组
     * @param params 参数二维数组（与SQLs一一对应）
     * @return true表示执行成功
     * @throws RuntimeException 执行失败时抛出
     */
    public boolean executeBatch(String[] SQLs, Object[][] params) {
        // 参数校验
        if (SQLs == null || params == null) {
            throw new IllegalArgumentException("参数数组不能为null");
        }
        if (SQLs.length != params.length) {
            throw new IllegalArgumentException("SQL数量与参数数量不匹配");
        }
        if (SQLs.length == 0) {
            return false;
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 按SQL分组（优化批处理）
            Map<String, List<Object[]>> sqlGroups = new HashMap<>();
            for (int i = 0; i < SQLs.length; i++) {
                if (SQLs[i] == null) {
                    throw new IllegalArgumentException("第" + (i + 1) + "个SQL不能为null");
                }
                sqlGroups.computeIfAbsent(SQLs[i], k -> new ArrayList<>()).add(params[i]);
            }

            try {
                // 按SQL分组执行
                for (Map.Entry<String, List<Object[]>> entry : sqlGroups.entrySet()) {
                    try (PreparedStatement ps = conn.prepareStatement(entry.getKey())) {
                        for (Object[] paramGroup : entry.getValue()) {
                            setParameters(ps, paramGroup);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("批量执行失败", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库连接失败", e);
        } finally {
            releaseConnection(conn);
        }
    }

    /**
     * 执行查询并将结果映射到对象列表
     *
     * @param <T>        返回的对象类型
     * @param sql        SQL查询语句
     * @param resultType 要映射的结果类
     * @param params     查询参数
     * @return 包含查询结果的列表
     * @throws SQLException 如果查询执行失败
     */
    public <T> List<T> executeQuery(String sql, Class<T> resultType, Object... params) throws SQLException {
        // 处理简单类型查询
        if (isSimpleType(resultType)) {
            return executeSimpleTypeQuery(sql, resultType, params);
        }

        // 处理对象类型查询
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSetToObjects(rs, resultType);
                }
            }
        } finally {
            releaseConnection(conn);
        }
    }

    // 判断是否是简单类型（基本数据类型及其包装类、String等）
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(String.class) ||
                Number.class.isAssignableFrom(type) ||
                type.equals(Boolean.class) || type.equals(boolean.class) ||
                type.equals(Character.class) || type.equals(char.class);
    }

    // 处理简单类型的查询
    private <T> List<T> executeSimpleTypeQuery(String sql, Class<T> resultType, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> results = new ArrayList<>();
                    while (rs.next()) {
                        results.add(mapSimpleType(rs, resultType, 1));
                    }
                    return results;
                }
            }
        } finally {
            releaseConnection(conn);
        }
    }

    // 映射简单类型
    @SuppressWarnings("unchecked")
    private <T> T mapSimpleType(ResultSet rs, Class<T> type, int columnIndex) throws SQLException {
        if (type.equals(String.class)) {
            return (T) rs.getString(columnIndex);
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return (T) Integer.valueOf(rs.getInt(columnIndex));
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return (T) Long.valueOf(rs.getLong(columnIndex));
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return (T) Double.valueOf(rs.getDouble(columnIndex));
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return (T) Float.valueOf(rs.getFloat(columnIndex));
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return (T) Boolean.valueOf(rs.getBoolean(columnIndex));
        } else if (type.equals(Short.class) || type.equals(short.class)) {
            return (T) Short.valueOf(rs.getShort(columnIndex));
        } else if (type.equals(Byte.class) || type.equals(byte.class)) {
            return (T) Byte.valueOf(rs.getByte(columnIndex));
        } else if (type.equals(Character.class) || type.equals(char.class)) {
            String s = rs.getString(columnIndex);
            return (T) Character.valueOf(s != null && !s.isEmpty() ? s.charAt(0) : '\0');
        }
        throw new SQLException("不支持的简单类型: " + type.getName());
    }

    // 将结果集映射到对象列表
    private <T> List<T> mapResultSetToObjects(ResultSet rs, Class<T> resultType) throws SQLException {
        try {
            List<T> results = new ArrayList<>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Field[] fields = resultType.getDeclaredFields();

            // 创建字段映射缓存（列名不区分大小写）
            Map<String, Field> fieldMap = new HashMap<>();
            for (Field field : fields) {
                fieldMap.put(field.getName().toLowerCase(), field);
            }

            while (rs.next()) {
                T instance = resultType.getDeclaredConstructor().newInstance();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i).toLowerCase();
                    Object value = rs.getObject(i);

                    Field field = fieldMap.get(columnName);
                    if (field != null) {
                        try {
                            field.setAccessible(true);
                            field.set(instance, convertValue(value, field.getType()));
                        } catch (IllegalAccessException e) {
                            throw new SQLException("无法设置字段值: " + field.getName(), e);
                        }
                    }
                }
                results.add(instance);
            }
            return results;
        } catch (Exception e) {
            throw new SQLException("对象映射失败", e);
        }
    }

    public <T> List<T> executeQuery(SQLPrepared sqlPrepared, Class<T> resultType) throws SQLException {
        String sql = sqlPrepared.getSql();
        Object[] params = sqlPrepared.getParams();
        return executeQuery(sql, resultType, params);
    }

    /**
     * 类型转换辅助方法
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        // 处理数字类型转换
        if (value instanceof Number) {
            Number number = (Number) value;
            if (targetType == int.class || targetType == Integer.class) {
                return number.intValue();
            } else if (targetType == long.class || targetType == Long.class) {
                return number.longValue();
            } else if (targetType == double.class || targetType == Double.class) {
                return number.doubleValue();
            } else if (targetType == float.class || targetType == Float.class) {
                return number.floatValue();
            }
        }

        // 处理日期类型转换
        if (value instanceof java.sql.Date && targetType == java.util.Date.class) {
            return new java.util.Date(((java.sql.Date) value).getTime());
        }

        if (value instanceof Timestamp && targetType == java.util.Date.class) {
            return new java.util.Date(((Timestamp) value).getTime());
        }

        // 其他情况直接返回
        return value;
    }

    /**
     * 清理线程资源
     */
    public void cleanupThreadResources() {
        if (!connectionPoolEnabled) {
            Connection conn = threadLocalConnection.get();
            if (conn != null) {
                try {
                    if (!conn.isClosed()) conn.close();
                } catch (SQLException e) {
                    System.err.println("关闭连接时出错: " + e.getMessage());
                } finally {
                    threadLocalConnection.remove();
                }
            }
        }
    }

    public Object[] getParams(Object... params) {
        return params;
    }

    @Getter
    public static class SQLPrepared {
        private final String sql;
        private final Object[] params;

        public SQLPrepared(String sql, Object... params) {
            if (sql == null || sql.trim().isEmpty()) {
                throw new IllegalArgumentException("SQL语句不能为空");
            }
            this.sql = sql;
            this.params = params != null ? params : new Object[0];
        }

    }

    private void setParameters(PreparedStatement ps, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }
}
/**
JDBCTools
├── 静态初始化块
│   ├── 加载db.properties
│   ├── 初始化连接参数(url/username/password/driver)
│   └── 初始化连接池(initializeConnectionPool)
│       ├── 双重检查锁定
│       ├── 加载JDBC驱动
│       └── 创建初始连接(POOL_SIZE个)
│
        ├── 连接管理
│   ├── getConnection()
│   │   ├── 连接池模式(connectionPoolEnabled=true)
│   │   │   ├── 从队列获取连接(poll with timeout)
│   │   │   ├── 检查连接有效性
│   │   │   └── 无效时创建新连接
│   │   └── 单连接模式(connectionPoolEnabled=false)
│   │       ├── ThreadLocal获取连接
│   │       └── 无连接时创建新连接
│   │
        │   ├── releaseConnection()
│   │   ├── 连接池模式：归还到队列
│   │   └── 单连接模式：保持连接不释放
│   │
        │   └── 连接池管理
│       ├── setPoolSize() 动态调整大小
│       ├── shutdownPool() 关闭所有连接
│       └── createNewConnection() 创建新连接
│
        ├── SQL操作核心
│   ├── executeUpdate()
│   │   ├── SQLPrepared参数处理
│   │   ├── 获取连接
│   │   ├── 准备语句
│   │   ├── 参数绑定(setParameters)
│   │   └── 执行更新
│   │
        │   ├── executeQuery()
│   │   ├── 简单类型处理(isSimpleType)
│   │   │   └── mapSimpleType()
│   │   └── 对象类型处理
│   │       └── mapResultSetToObjects()
│   │           ├── 反射创建实例
│   │           ├── 结果集元数据分析
│   │           └── 字段映射(convertValue)
│   │
        │   ├── executeTransaction()
│   │   ├── 多语句事务处理
│   │   └── 自动回滚机制
│   │
        │   └── executeBatch()
│       ├── SQL分组优化
│       └── 批量执行
│
        ├── 工具方法
│   ├── setParameters() 参数绑定
│   ├── convertValue() 类型转换
│   ├── cleanupThreadResources() 清理资源
│   └── isSimpleType() 类型判断
│
        └── SQLPrepared内部类
    ├── 封装SQL语句
    └── 封装参数数组
 */