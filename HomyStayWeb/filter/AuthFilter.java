package com.HomyStayWeb.filter;

import com.HomyStayWeb.Jump.JumpJump;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*") // 拦截所有请求
public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        request.setAttribute("loginTime", JumpJump.getEncodedTime());
        // 排除登录页、静态资源等
        String path = request.getRequestURI();
        if(path.contains("/GuestServlet.action")){
            System.out.println(111);
        }
        if (path.endsWith("/login.jsp") || path.contains("/css/") || path.contains("/js/")||path.contains("/AdminServlet.action")||path.contains("/CodeServlet.action")) {
            chain.doFilter(request, response);
            return;
        }

        // 检查 Session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userID") == null) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
            return;
        }
        chain.doFilter(request, response);
    }
    @Override
    public void destroy() {}
}