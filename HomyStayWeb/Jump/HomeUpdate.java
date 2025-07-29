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

@WebServlet("/home/update")
public class HomeUpdate extends HttpServlet {
    private final HomeService homeService= new HomeService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String id=request.getParameter("id");
        List<Home> HomeList=homeService.getHomeById(id);
        request.setAttribute("list", HomeList);
        request.getRequestDispatcher("/jsp/home_update.jsp").forward(request, response);
    }
}