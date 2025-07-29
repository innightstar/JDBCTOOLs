package com.HomyStayWeb.Error;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppErrorHandler implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            System.err.println("全局异常捕获: " + ex.getMessage());
            // 发送警报或记录日志
        });
    }
}
