package com.managertm;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class getToken {
    public static String token(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        String token = null;
        for(Cookie cookie : cookies){
            if("ssid".equals(cookie.getName())){
                token = cookie.getValue();
                break;
            }
        }
        return token != null ? token : null;
    }
}
