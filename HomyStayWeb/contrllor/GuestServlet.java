package com.HomyStayWeb.contrllor;

import com.HomyStayWeb.Service.GuestService;
import com.HomyStayWeb.Tools.FormBeanTools;
import com.HomyStayWeb.Tools.SQLExecutor;
import com.HomyStayWeb.beans.Guest;
import com.HomyStayWeb.beans.Home;
import com.HomyStayWeb.beans.Vip;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/GuestServlet.action")
public class GuestServlet extends HttpServlet {
    GuestService guestService = new GuestService();
    FormBeanTools formBeanTools = new FormBeanTools();
    SQLExecutor sqlExecutor=new SQLExecutor();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)  {
        try {
            request.setCharacterEncoding("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        // 必须包含 charset
        String action = request.getParameter("action");
        try {
            switch (action) {
                case "add": {
                    Guest guest = formBeanTools.formToClass(request, Guest.class);
                    if (guestService.addGuest(guest)) {
                        //添加成功
                        response.getWriter().write("<script>alert('订单新增成功！');location.assign('/guests/list')</script>");
                    } else {
                        //添加失败
                        response.getWriter().write("<script>alert('订单新增失败！');location.assign('/guests/list')</script>");
                    }
                    break;
                }
                case "QueryList": {
                    String name = request.getParameter("name");
                    List<Vip> listVip = guestService.QueryListByName(name);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    Map<String, Object> result = new HashMap<>();
                    result.put("listVip", listVip);
                    String json = new Gson().toJson(result);
                    response.getWriter().write(json);
                    System.out.println(json);
                    break;
                }
                case "QueryNumByh_Type": {
                        response.setContentType("application/json;charset=UTF-8");
                        String h_Type = request.getParameter("h_Type");
                        System.out.println(h_Type);
                        List<Integer> listNum = guestService.QueryNumByH_Type(h_Type);
                        // 构建标准JSON响应
                        Map<String, Object> result = new HashMap<>();
                        result.put("listNum", listNum);
                        // 返回JSON
                        String jsonResponse = new Gson().toJson(result);
                        System.out.println("返回的JSON数据：" + jsonResponse);
                        response.getWriter().write(jsonResponse);

                    break;
                }
                case "delete": {
                    String id = request.getParameter("id");
                    if (id != null) {
                        if (guestService.Delete(id))
                            response.getWriter().write("<script>alert('退房成功');location.assign('/guests/list')</script>");
                        else
                            response.getWriter().write("<script>alert('退房失败');location.assign('/guests/list')</script>");
                    } else
                        response.getWriter().write("<script>alert('退房失败!未能获取到ID');location.assign('/guests/list');</script>");
                    break;
                }
                case "update": {
                    Guest guest = formBeanTools.formToClass(request, Guest.class);
                    String id = request.getParameter("id");
                    if (guestService.UpdateGuest(guest, id)) {
                        response.getWriter().write("<script>alert('修改成功');location.assign('/guests/list')</script>");
                    } else {
                        response.getWriter().write("<script>alert('修改失败');location.assign('/guests/list')</script>");
                    }
                    break;
                }
                case "QueryALL": {
                    response.setContentType("application/json;charset=UTF-8");
                    List<Vip> list = guestService.QueryAll();
                    if (!list.isEmpty()) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("listVip_Guest", list);
                        String jsonResponse = new Gson().toJson(result);
                        response.getWriter().write(jsonResponse);
                        System.out.println(jsonResponse);
                    }
                    break;
                }
                case "GuestList": {
                    response.setContentType("application/json;charset=UTF-8");
                    List<Guest> list = guestService.GuestList();
                    Map<String, Object> result = new HashMap<>();
                    result.put("listGuest",list);
                    String jsonResponse = new Gson().toJson(result);
                    response.getWriter().write(jsonResponse);
                    System.out.println(jsonResponse);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            sqlExecutor.ResetIdForPrimaryKey("tb_guest",Guest.class);
        }
    }
}
