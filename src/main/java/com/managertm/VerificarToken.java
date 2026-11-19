
package com.managertm;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class VerificarToken extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        try {
            AuthSessionManager.getValidAccessToken(request);

            request.getRequestDispatcher("/dashboard.jsp")
                    .forward(request, response);

        } catch (AuthSessionManager.SessionExpiredException ex) {

            response.sendRedirect(
                    request.getContextPath() + "/index.jsp");

        } catch (IOException ex) {

            getServletContext().log(
                    "Error validando sesión", ex);

            response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "No se pudo verificar la sesión");
        }
    }
}
