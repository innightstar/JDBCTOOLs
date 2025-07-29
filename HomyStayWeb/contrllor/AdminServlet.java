package com.HomyStayWeb.contrllor;

import com.HomyStayWeb.Dao.Admindao.AdminDaoImpl;
import com.HomyStayWeb.Tools.CookiesTools;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@WebServlet("/AdminServlet.action")
public class AdminServlet extends HttpServlet {
    AdminDaoImpl AdminSDI=new AdminDaoImpl();
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=UTF-8");
        String action=request.getParameter("action");
        switch(action){
            case "login":{    String username= request.getParameter("username");
                String password= request.getParameter("password");
                String syscode= request.getParameter("syscode");
                if(AdminSDI.Login(username,password)&&request.getSession().getAttribute("systemCode").equals(syscode)){
                    //登录成功
                    try {
                        HttpSession session=request.getSession();
                        session.setAttribute("userID",username);
                        String loginTime=CookiesTools.CookiesGet(request,"loginTime");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        response.addCookie( CookiesTools.CookiesSet(request,"loginTime",sdf.format(new Date())));
                        response.sendRedirect("/jump/index?encodedTime=" + loginTime);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    //登录失败
                    try {
                        response.getWriter().write("<script>alert('登录失败');location.assign('/login')</script>");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case "logout":{
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();  // 销毁服务器端 Session
                }
                Cookie killCookie = new Cookie("JSESSIONID", "");
                killCookie.setMaxAge(0);
                killCookie.setPath(request.getContextPath() + "/");
                response.addCookie(killCookie);
                response.sendRedirect("/jsp/login.jsp");
                break;
            }
        }

    }
}

