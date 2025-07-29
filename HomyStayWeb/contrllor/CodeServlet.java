package com.HomyStayWeb.contrllor;


import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

@WebServlet("/CodeServlet.action")
public class CodeServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int length = 120;
        int width = 50;

        BufferedImage image = new BufferedImage(length,width,BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();

        graphics.setColor(Color.gray);
        graphics.fillRect(0,0,length,width);

        String[] codes = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r",
                "s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N",
                "O","P","Q","R","S","T","U","V","W","X","Y","Z","0","1","2","3","4","5","6","7","8","9"};
        Color[] colors = {Color.red,Color.yellow,Color.green,Color.cyan,Color.pink,Color.orange,Color.blue};

        Random ran = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            graphics.setColor(colors[ran.nextInt(colors.length)]);
            graphics.setFont(new Font("宋体",Font.BOLD,20+ran.nextInt(20)));
            String str = codes[ran.nextInt(codes.length)];
            graphics.drawString(str,20+i*20,25+ran.nextInt(20));
            sb.append(str);
        }

        HttpSession session = request.getSession();
        session.setAttribute("systemCode",sb.toString());
        System.out.println(sb.toString());

        graphics.setColor(Color.ORANGE);
        for (int i = 0; i < 5; i++) {
            graphics.drawLine(ran.nextInt(length),ran.nextInt(width),ran.nextInt(length),ran.nextInt(width));
        }
        ImageIO.write(image,"jpg",response.getOutputStream());
    }

}

