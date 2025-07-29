package com.HomyStayWeb.Tools;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CookiesTools {
    public static String CookiesGet(HttpServletRequest request, String name){
        Cookie[] cookies=request.getCookies();
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(name))
                return cookie.getValue();
        }
        return null;
    }
    public static Cookie CookiesSet(HttpServletRequest request, String name, String value) throws UnsupportedEncodingException {
        Cookie[] cookies=request.getCookies();
        String encodedValue = Base64.getUrlEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(name))
            {cookie.setValue(encodedValue);cookie.setMaxAge(60*60*24); return cookie;}
        }
        Cookie cookie=new Cookie(name,encodedValue);
        cookie.setMaxAge(60*60*24);
        return cookie;
    }
}
