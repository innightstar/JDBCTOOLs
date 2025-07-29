package com.HomyStayWeb.Jump;

import com.HomyStayWeb.Service.VipService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/vip/update")
public class vip_update extends HttpServlet {
    private final VipService vipService= new VipService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String id=request.getParameter("id");
        request.setAttribute("id",id);
        request.setAttribute("list",vipService.VipQueryServiceById(id));
        request.getRequestDispatcher("/jsp/vip_update.jsp").forward(request, response);
    }
}