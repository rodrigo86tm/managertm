
package com.managertm;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class VerificarToken extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        if(getToken.token(request) == null){
            response.sendRedirect("index.jsp"); 
        } else {
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
        }
    }
}
