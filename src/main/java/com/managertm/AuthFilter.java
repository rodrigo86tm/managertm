
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
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req =
                (HttpServletRequest) request;

        HttpServletResponse resp =
                (HttpServletResponse) response;

        try {
            AuthSessionManager.getValidAccessToken(req);

            chain.doFilter(request, response);

        } catch (AuthSessionManager.SessionExpiredException ex) {

            resp.sendRedirect(
                    req.getContextPath() + "/index.jsp");

        } catch (IOException ex) {

            req.getServletContext().log(
                    "Error verificando autenticación", ex);

            resp.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "No se pudo verificar la sesión");
        }
    }

    @Override
    public void destroy() {
        
    }
    
}