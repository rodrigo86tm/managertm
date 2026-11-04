
package com.managertm;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        if (
                uri.endsWith("index.jsp")
                || uri.contains("/Login")
                || uri.contains("/css/")
                || uri.contains("/js/")
                || uri.contains("/images/")
           ) {

            chain.doFilter(request,response);
            return;
        }

        boolean autenticado = false;

        Cookie[] cookies = req.getCookies();

        if(cookies != null){
            for(Cookie c : cookies){
                if("ssid".equals(c.getName())
                        && c.getValue() != null
                        && !c.getValue().isEmpty()){

                    autenticado = true;
                    break;
                }
            }
        }

        if(!autenticado){
            resp.sendRedirect(
                req.getContextPath() + "/index.jsp");
            return;
        }

        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {
        
    }
    
}