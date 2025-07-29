package com.HomyStayWeb.contrllor;


import com.HomyStayWeb.Service.VipService;
import com.HomyStayWeb.Tools.FormBeanTools;
import com.HomyStayWeb.Tools.JsonMsg;
import com.HomyStayWeb.Tools.SQLExecutor;
import com.HomyStayWeb.beans.Vip;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import static java.lang.System.out;

@WebServlet("/VipServlet.action")
@MultipartConfig
public class VipServlet extends HttpServlet {
    VipService vipService = new VipService();
    FormBeanTools formBeanTools = new FormBeanTools();
    JsonObject json = new JsonObject();
    SQLExecutor sqlExecutor = new SQLExecutor();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");
        try {
            switch (action) {
                case "add": {
                    response.setContentType("application/json;charset=UTF-8");
                    Vip vip = formBeanTools.formToClass(request, Vip.class);
                    json = new JsonObject();
                    try {
                        if (vipService.VipCreatService(vip)) {
                            //注册成功
                            json.addProperty("code", 200);
                            json.addProperty("msg", "新增成功");
                        } else {
                            //注册失败
                            json.addProperty("code", 400);
                            json.addProperty("msg", "无效操作");
                        }
                        response.getWriter().write(json.toString());
                    } catch (Exception e) {
                        json.addProperty("code", 500);
                        json.addProperty("msg", "服务器错误: " + e.getMessage());
                        out.print(json.toString());
                        e.printStackTrace();
                    } finally {
                        out.flush();
                        out.close();
                        break;
                    }
                }
                case "find": {
                    String findByPhone = request.getParameter("findByPhone");
                    List<Vip> Result = vipService.VipQueryServiceByPhone(findByPhone);
                    request.setAttribute("list", Result);
                    try {
                        request.getRequestDispatcher("/jsp/vip_list.jsp").forward(request, response);
                    } catch (ServletException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case "delete": {
                    String idStr = request.getParameter("id");
                    int id = 0;
                    if (idStr != null) {
                        id = Integer.parseInt(idStr);
                    }

                    try {
                        json = new JsonObject();
                        if (vipService.VipServiceDeleteById(id)) {
                            //删除成功
                            json = JsonMsg.responseMsg(200, "删除成功");
                        } else {
                            //删除失败
                            json = JsonMsg.responseMsg(400, "删除失败");
                        }
                        response.getWriter().write(json.toString());
                    } catch (RuntimeException e) {
                        json = JsonMsg.responseMsg(500, "服务器错误" + e.getMessage());
                        response.getWriter().write(json.toString());
                        e.printStackTrace();
                    } finally {
                        out.flush();
                        out.close();
                        break;
                    }
                }
                case "update": {
                    String idStr = request.getParameter("id");
                    int id = 0;
                    if (idStr != null && !idStr.isEmpty()) {
                        id = Integer.parseInt(idStr);
                    }
                    response.setContentType("application/json;charset=UTF-8");
                    Vip vip = formBeanTools.formToClass(request, Vip.class);
                    try {
                        json = new JsonObject();
                        if (vipService.VipServiceUpdate(id, vip)) {
                            //修改成功
                            json = JsonMsg.responseMsg(200, "修改成功");
                        } else {
                            //修改失败
                            json = JsonMsg.responseMsg(400, "修改失败");
                        }
                        response.getWriter().write(json.toString());
                    } catch (Exception e) {
                        json = JsonMsg.responseMsg(500, "服务器错误" + e.getMessage());
                        response.getWriter().write(json.toString());
                        e.printStackTrace();
                    } finally {
                        out.flush();
                        out.close();
                        break;
                    }
                }
            }

        } finally {
            sqlExecutor.ResetIdForPrimaryKey("tb_vip", Vip.class);
        }
    }
}
