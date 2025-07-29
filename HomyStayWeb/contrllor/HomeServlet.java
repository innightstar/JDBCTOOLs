package com.HomyStayWeb.contrllor;
import com.HomyStayWeb.Service.HomeService;
import com.HomyStayWeb.Tools.FormBeanTools;
import com.HomyStayWeb.Tools.JsonMsg;
import com.HomyStayWeb.Tools.SQLExecutor;
import com.HomyStayWeb.beans.Home;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.System.out;

@WebServlet("/HomeServlet.action")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
        maxFileSize = 1024 * 1024 * 10,       // 10MB
        maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class HomeServlet extends HttpServlet {
    SQLExecutor sqlExecutor=new SQLExecutor();
    private final HomeService homeService = new HomeService();
    private final FormBeanTools formBeanTools = new FormBeanTools();

    // 配置常量（建议通过外部配置文件管理）
    private static final String LOCAL_BACKUP_PATH = "D:/HomeStayWeb/static/images";
    private static final String SERVER_UPLOAD_DIR = "upload";
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif"));
    private static final String REDIRECT_QUERY = "/HomeServlet.action?action=query";
    JsonObject json=new JsonObject();


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try {
            String action = request.getParameter("action");

            switch (action) {
                case "query":
                    handleHomeQuery(request, response);
                    break;
                case "add":
                    handleAddHome(request, response);
                    break;
                case "CheckByNum":
                    handleCheckByNum(request, response);
                    break;
                case "update":
                    handleUpdateHome(request, response);
                    break;
                case "delete":
                    handleDeleteHome(request, response);
                    break;
                default:
                    sendError(response, "无效操作", HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            handleException(response, e);
        }finally {
            sqlExecutor.ResetIdForPrimaryKey("tb_home",Home.class);
        }
    }

    //====================== 核心处理方法 ======================
    private void handleAddHome(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String savedFileName = processFileUpload(request);
        try {
            Home home = formBeanTools.formToClass(request, Home.class);
            home.setFile("/upload/"+savedFileName);
            json=new JsonObject();
            boolean success = homeService.addHome(home) > 0;
            json= success ? JsonMsg.responseMsg(200,"新增成功"):JsonMsg.responseMsg(400,"新增失败");
            response.getWriter().write(json.toString());
        } catch (IOException e) {
            json=JsonMsg.responseMsg(500,"服务器错误"+e.getMessage());
            response.getWriter().write(json.toString());
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    private void handleUpdateHome(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Home updateHome = formBeanTools.formToClass(request, Home.class);
        String id = getRequiredParameter(request, "id");

        // 处理文件更新（如果上传了新文件）
        Part filePart = request.getPart("file");
        if (filePart != null && filePart.getSize() > 0) {
            String newFilename=processFileUpload(request);
            updateHome.setFile("/upload/"+newFilename);
        }

        boolean success = homeService.UpdateById(id, updateHome);
        sendAlert(response,
                success ? "房间更新成功!" : "房间更新失败!",
                success ? REDIRECT_QUERY : "/home/update");
    }

    private void handleDeleteHome(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        json=new JsonObject();
        try {
            String id = getRequiredParameter(request, "id");
            boolean success = homeService.DeleteById(id);
            json=success?JsonMsg.responseMsg(200,"删除成功"):JsonMsg.responseMsg(400,"删除失败");
            response.getWriter().write(json.toString());
        } catch (ServletException e) {
            json=JsonMsg.responseMsg(500,"服务器错误"+e.getMessage());
            response.getWriter().write(json.toString());
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    private void handleCheckByNum(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String findByNum = request.getParameter("findByNum");
        request.setAttribute("list", homeService.CheckByNum(findByNum));
        forwardToJsp(request, response, "/jsp/home_list.jsp");
    }

    private void handleHomeQuery(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String num = request.getParameter("num");
        List<Home> homeList = homeService.getAllHome(num);

        request.setAttribute("list", homeList);
        request.setAttribute("num", num);
        request.setAttribute("error", homeList.isEmpty() ? "未查询到数据" : "");
        forwardToJsp(request, response, "/home/list");
    }

    //====================== 文件处理方法 ======================
    private String processFileUpload(HttpServletRequest request)
            throws IOException, ServletException {
        // 1. 准备目录
        String serverPath = request.getServletContext().getRealPath(SERVER_UPLOAD_DIR);
        ensureDirectoryExists(serverPath);
        ensureDirectoryExists(LOCAL_BACKUP_PATH);

        // 2. 处理文件
        for (Part part : request.getParts()) {
            String fileName = part.getSubmittedFileName();
            if (fileName == null || fileName.isEmpty()) continue;

            // 3. 验证并生成文件名
            String fileExtension = validateAndGetExtension(fileName);
            String safeFileName = generateSafeFileName(fileExtension);

            // 4. 双存储
            saveFile(part, Paths.get(serverPath, safeFileName));
            saveFile(part, Paths.get(LOCAL_BACKUP_PATH, safeFileName));
            return safeFileName;
        }
        return null;
    }

    private String validateAndGetExtension(String fileName) throws ServletException {
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new ServletException("不支持的文件类型: " + fileExtension);
        }
        return fileExtension;
    }

    private void saveFile(Part part, Path targetPath) throws IOException {
        part.write(targetPath.toString());
    }

    //====================== 工具方法 ======================
    private String generateSafeFileName(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    private void ensureDirectoryExists(String path) {
        Paths.get(path).toFile().mkdirs();
    }

    private String getRequiredParameter(HttpServletRequest request, String paramName)
            throws ServletException {
        String value = request.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            throw new ServletException("缺少必要参数: " + paramName);
        }
        return value;
    }

    private void forwardToJsp(HttpServletRequest request,
                              HttpServletResponse response,
                              String jspPath)
            throws ServletException, IOException {
        request.getRequestDispatcher(jspPath).forward(request, response);
    }

    private void sendAlert(HttpServletResponse response, String message, String location)
            throws IOException {
        response.getWriter().write(
                String.format("<script>alert('%s');location.href='%s';</script>",
                        message, location)
        );
    }

    private void handleException(HttpServletResponse response, Exception e)
            throws IOException {
        String errorMsg = "系统错误: " + e.getMessage();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMsg);
    }

    private void sendError(HttpServletResponse response, String message, int statusCode)
            throws IOException {
        response.sendError(statusCode, message);
    }
}