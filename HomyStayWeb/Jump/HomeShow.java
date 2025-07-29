package com.HomyStayWeb.Jump;

import com.HomyStayWeb.Service.HomeService;
import com.HomyStayWeb.beans.Home;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/home/show")
public class HomeShow extends HttpServlet {
    private final HomeService homeService= new HomeService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String id=request.getParameter("id");
        List<Home> HomeList = homeService.getHomeById(id);
        request.setAttribute("list", HomeList);
        request.getRequestDispatcher("/jsp/home_show.jsp").forward(request, response);
    }
}