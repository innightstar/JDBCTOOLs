package com.HomyStayWeb.Jump;

import lombok.Getter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/jump/*")
public class JumpJump extends HttpServlet {
    @Getter
    private static String encodedTime;
    @Getter
    private static boolean encodeTimeIsGet=false;

    public static void setEncodedTime(String encodedTime) {
        JumpJump.encodedTime = encodedTime;
    }

    public static void setEncodeTimeIsGet(boolean encodeTimeIsGet) {
        JumpJump.encodeTimeIsGet = encodeTimeIsGet;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        if(!isEncodeTimeIsGet()) {
            setEncodedTime(request.getParameter("encodedTime"));
            setEncodeTimeIsGet(true);
        }
        request.setAttribute("loginTime",getEncodedTime());
        String path = request.getPathInfo(); // 获取 /jump/ 后面的部分，如 /index
        if (path == null || path.equals("/")) {
            path = "/jsp/index"; // 默认跳转 index
        }
        // 去掉开头的 "/"，并加上 .jsp 后缀
        String jspPage = path.substring(1) + ".jsp";
        request.getRequestDispatcher("/jsp"+"/" + jspPage).forward(request, response);
    }

}