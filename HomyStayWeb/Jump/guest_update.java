package com.HomyStayWeb.Jump;

import com.HomyStayWeb.Service.GuestService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/guest/update")
public class guest_update extends HttpServlet {
    GuestService guestService = new GuestService();
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("list",guestService.QueryGuestById(request.getParameter("id")));
        request.getRequestDispatcher("/jsp/guests_update.jsp").forward(request, response);
    }
}