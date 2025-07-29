package com.HomyStayWeb.Jump;

import com.HomyStayWeb.Service.VipService;
import com.HomyStayWeb.beans.Vip;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/vip/list")
public class vip_list extends HttpServlet {
    private final VipService vipService= new VipService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        List<Vip> vipList = vipService.VipQueryService();
        request.setAttribute("list", vipList);
        request.getRequestDispatcher("/jsp/vip_list.jsp").forward(request, response);
        }
}