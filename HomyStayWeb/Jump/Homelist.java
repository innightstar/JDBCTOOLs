package com.HomyStayWeb.Jump;

import com.HomyStayWeb.Service.HomeService;
import com.HomyStayWeb.beans.Home;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@WebServlet("/home/list")
public class Homelist extends HttpServlet {
    private static final String LOCAL_BACKUP_PATH = "D:/HomeStayWeb/static/images";
    private static final String SERVER_UPLOAD_DIR = "upload";
    static {
        // 1. 创建上传目录（如果不存在）
        String tomcatRootPath =  System.getenv("CATALINA_HOME") + "/webapps/ROOT";
        File uploadDir = new File(tomcatRootPath, SERVER_UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                System.err.println("无法创建上传目录: " + uploadDir.getAbsolutePath());
            }
        }

        // 2. 获取源目录中的所有文件
        File dir = new File(LOCAL_BACKUP_PATH);
        File[] images = dir.listFiles();

        if (images == null) {
            System.err.println("无法读取目录: " + dir.getAbsolutePath());
        }

        // 3. 复制文件到上传目录
        for (File image : images) {
            // 跳过子目录，只处理文件
            if (!image.isFile()) {
                continue;
            }

            Path source = image.toPath();
            Path target = uploadDir.toPath().resolve(image.getName());
            File upload=new File(target.toFile().getAbsolutePath());
            File[] WebImages = upload.listFiles();

            try {
                // 使用Files.copy方法复制文件
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("已复制文件: " + image.getName());
            } catch (IOException e) {
                System.err.println("复制文件失败: " + image.getName() + " - " + e.getMessage());
            }
        }
    }
    private final HomeService homeService= new HomeService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        List<Home> HomeList = homeService.getAllHome(null);
        request.setAttribute("list", HomeList);
        request.getRequestDispatcher("/jsp/home_list.jsp").forward(request, response);
    }
}