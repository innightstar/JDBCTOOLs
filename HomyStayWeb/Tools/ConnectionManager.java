package com.HomyStayWeb.Tools;

import com.sun.jndi.ldap.pool.Pool;
import lombok.Data;
import lombok.Getter;

import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据库连接管理器（带监控功能）
 *
 * 功能概述：
 * 1. 管理数据库连接池
 * 2. 提供连接获取/归还接口
 * 3. 连接池状态监控
 * 4. 连接泄漏检测
 *
 * 设计模式：
 * - 单例模式（静态初始化）
 * - 工厂模式（连接创建）
 * - 对象池模式（连接池）
 */
@Data
public class ConnectionManager {
    // 配置信息
    private static final String configPath="db.properties"; // 配置文件路径
    private static String url;         // 数据库URL
    private static String username;    // 数据库用户名
    private static String password;    // 数据库密码
    private static String driverClassName; // JDBC驱动类名

    // 连接池配置
    private static int POOL_SIZE = 10; // 默认连接池大小
    private static final BlockingQueue<Connection> connectionPool = new LinkedBlockingQueue<>(POOL_SIZE); // 连接池队列
    private static final Lock poolInitLock = new ReentrantLock(); // 连接池初始化锁
    private static volatile boolean poolInitialized = false; // 连接池初始化标志

    // 监控统计
    private static final AtomicLong totalCreated = new AtomicLong(); // 创建连接总数
    private static final AtomicLong totalDestroyed = new AtomicLong(); // 销毁连接总数
    private static final AtomicLong maxWaitTime = new AtomicLong(); // 最大等待时间(ms)
    private static final AtomicInteger waitingThreads = new AtomicInteger(); // 等待线程数
    private static final Map<Connection, BorrowRecord> borrowedConnections = new ConcurrentHashMap<>(); // 借出连接记录

    private boolean connectionPoolEnabled = true; // 是否启用连接池模式

    // 静态初始化块（加载配置并初始化连接池）
    static {
        try (InputStream in = ConnectionManager.class.getClassLoader().getResourceAsStream(configPath)) {
            Properties prop = new Properties();
            prop.load(in);
            url = prop.getProperty("jdbc.url");
            username = prop.getProperty("jdbc.username");
            password = prop.getProperty("jdbc.password");
            driverClassName = prop.getProperty("jdbc.driverClassName");
            initializeConnectionPool(); // 初始化连接池
        } catch (Exception e) {
            throw new RuntimeException("数据库配置初始化失败", e);
        }
    }

    /**
     * 连接借出记录（用于泄漏检测）
     */
    private static class BorrowRecord {
        final long borrowTime;       // 借出时间戳
        final Thread borrowThread;   // 借出线程
        final StackTraceElement[] stackTrace; // 借出时的调用栈

        BorrowRecord() {
            this.borrowTime = System.currentTimeMillis();
            this.borrowThread = Thread.currentThread();
            this.stackTrace = Thread.currentThread().getStackTrace();
        }
    }

    /**
     * 连接池状态信息（监控用DTO）
     */
    @Data
    public static class PoolStats {
        private final int totalSize;       // 连接池总大小
        private final int idleCount;      // 空闲连接数
        private final int activeCount;    // 活跃连接数
        private final int waitingThreads; // 等待线程数
        private final long maxWaitTime;   // 最大等待时间(ms)
        private final long totalCreated;  // 总创建连接数
        private final long totalDestroyed; // 总销毁连接数
        private final Date snapshotTime;  // 快照时间
        public PoolStats(int totalSize, int idleCount, int activeCount,
                         int waitingThreads, long maxWaitTime,
                         long totalCreated, long totalDestroyed) {
            this.totalSize = totalSize;
            this.idleCount = idleCount;
            this.activeCount = activeCount;
            this.waitingThreads = waitingThreads;
            this.maxWaitTime = maxWaitTime;
            this.totalCreated = totalCreated;
            this.totalDestroyed = totalDestroyed;
            this.snapshotTime = new Date();
        }

        /**
         * 计算连接池使用率
         * @return 使用百分比(0-100)
         */
        public double getUsageRate() {
            return totalSize == 0 ? 0 : activeCount * 100.0 / totalSize;
        }
    }

    /**
     * 初始化连接池（双重检查锁定）
     */
    private static void initializeConnectionPool() throws SQLException {
        if (poolInitialized) return;

        poolInitLock.lock();
        try {
            // 双重检查
            if (!poolInitialized) {
                Class.forName(driverClassName); // 加载驱动
                connectionPool.clear(); // 清空连接池

                // 创建初始连接
                for (int i = 0; i < POOL_SIZE; i++) {
                    connectionPool.offer(createPhysicalConnection());
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
     * 创建物理连接（实际建立数据库连接）
     */
    private static Connection createPhysicalConnection() throws SQLException {
        totalCreated.incrementAndGet(); // 计数增加
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 动态调整连接池大小
     * @param poolSize 新的连接池大小
     * @return 是否调整成功
     */
    public static boolean setPoolSize(int poolSize) throws SQLException {
        if (poolSize <= 0) return false;

        synchronized (ConnectionManager.class) {
            shutdownPool(); // 先关闭现有连接池
            POOL_SIZE = poolSize; // 设置新大小
            initializeConnectionPool(); // 重新初始化
            return true;
        }
    }

    /**
     * 获取连接池当前状态
     */
    public static PoolStats getPoolStats() {
        return new PoolStats(
                POOL_SIZE,
                connectionPool.size(),
                POOL_SIZE - connectionPool.size(),
                waitingThreads.get(),
                maxWaitTime.get(),
                totalCreated.get(),
                totalDestroyed.get()
        );
    }

    /**
     * 检测连接泄漏
     * @param thresholdMillis 阈值毫秒数
     * @return 泄漏连接映射表（连接->持有时间）
     */
    public static Map<Connection, Long> getLeakedConnections(long thresholdMillis) {
        Map<Connection, Long> leaks = new HashMap<>();
        long now = System.currentTimeMillis();

        // 检查所有借出连接
        borrowedConnections.forEach((conn, record) -> {
            long holdTime = now - record.borrowTime;
            if (holdTime > thresholdMillis) {
                leaks.put(conn, holdTime); // 记录超时连接
            }
        });

        return leaks;
    }

    /**
     * 关闭连接池（释放所有连接）
     */
    public static void shutdownPool() {
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            closeConnection(conn); // 逐个关闭连接
        }
        poolInitialized = false;
    }

    /**
     * 安全关闭连接
     */
    private static void closeConnection(Connection conn) {
        totalDestroyed.incrementAndGet(); // 计数增加
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close(); // 实际关闭连接
            }
        } catch (SQLException e) {
            System.err.println("关闭连接时出错: " + e.getMessage());
        }
    }

    /**
     * 获取数据库连接（核心方法）
     * @return 可用数据库连接
     * @throws SQLException 如果获取失败
     */
    public Connection getConnection() throws SQLException {
        // 非连接池模式
        if (!connectionPoolEnabled) {
            return createNewConnection();
        }

        // 连接池模式
        waitingThreads.incrementAndGet(); // 等待线程数+1
        long start = System.currentTimeMillis();

        try {
            // 从池中获取连接（带超时）
            Connection conn = connectionPool.poll(2, TimeUnit.SECONDS);
            if (conn == null) {
                throw new SQLTimeoutException("获取连接超时");
            }

            // 记录等待时间
            long waitTime = System.currentTimeMillis() - start;
            maxWaitTime.updateAndGet(curr -> Math.max(curr, waitTime));

            // 验证连接有效性
            if (!isConnectionValid(conn)) {
                closeConnection(conn); // 关闭无效连接
                conn = createPhysicalConnection(); // 创建新连接
            }

            borrowedConnections.put(conn, new BorrowRecord()); // 记录借出
            System.out.println("Get Connection"+getPoolStats());
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接被中断", e);
        } finally {
            waitingThreads.decrementAndGet(); // 等待线程数-1
        }
    }

    /**
     * 验证连接是否有效
     */
    private boolean isConnectionValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 归还连接（核心方法）
     */
    public void releaseConnection(Connection conn) {
        if (conn == null) return;

        // 从借出记录中移除
        borrowedConnections.remove(conn);

        // 连接池模式处理
        if (connectionPoolEnabled) {
            if (isConnectionValid(conn)) {
                if (!connectionPool.offer(conn)) { // 尝试归还到池中
                    closeConnection(conn); // 归还失败则关闭
                }
            } else {
                closeConnection(conn); // 无效连接直接关闭
            }
        } else {
            // 非连接池模式直接关闭
            closeConnection(conn);
        }
        System.out.println("Back Connection"+getPoolStats());
    }

    /**
     * 创建新连接（非池模式专用）r
     */
    private Connection createNewConnection() throws SQLException {
        Connection conn = createPhysicalConnection();
        if (!connectionPoolEnabled) {
            borrowedConnections.put(conn, new BorrowRecord()); // 记录借出
        }
        return conn;
    }
}
/*
  ConnectionManager
 ├── 配置管理
 │   ├── 静态初始化加载db.properties
 │   ├── 连接参数配置(url/username/password/driver)
 │   └── 连接池大小配置(POOL_SIZE)
 │
 ├── 连接池核心功能
 │   ├── initializeConnectionPool() - 初始化连接池（双重检查锁）
 │   ├── getConnection() - 获取连接
 │   │   ├── 连接池模式
 │   │   │   ├── 从队列获取（带超时）
 │   │   │   ├── 连接有效性检查
 │   │   │   └── 记录借出信息
 │   │   └── 单连接模式
 │   │       └── 直接创建新连接
 │   │
 │   ├── releaseConnection() - 归还连接
 │   │   ├── 连接池模式：尝试归还
 │   │   └── 单连接模式：直接关闭
 │   │
 │   └── shutdownPool() - 关闭连接池
 │
 ├── 连接生命周期
 │   ├── createPhysicalConnection() - 创建原始连接
 │   ├── isConnectionValid() - 验证连接
 │   └── closeConnection() - 安全关闭连接
 │
 ├── 监控统计
 │   ├── PoolStats - 状态快照
 │   ├── getPoolStats() - 获取当前状态
 │   ├── getLeakedConnections() - 泄漏检测
 │   └── 统计指标
 │       ├── 创建/销毁连接数
 │       ├── 最大等待时间
 │       └── 等待线程数
 │
 └── 配置管理
 ├── setPoolSize() - 动态调整大小
 └── connectionPoolEnabled - 模式切换
 */