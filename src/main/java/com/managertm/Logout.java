/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.managertm;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

@WebServlet(name = "Logout", urlPatterns = {"/Logout"})
public class Logout extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        boolean remoteRevoked = false;

        if (session != null) {

            try {
                // Renueva el Access Token si hace falta.
                String token = AuthSessionManager
                        .getValidAccessToken(request);

                // Revoca la sesión en la API .NET.
                AuthApiClient.post(
                        "/logout",
                        new JSONObject(),
                        token
                );

                remoteRevoked = true;

            } catch (AuthSessionManager.SessionExpiredException ex) {

                // La sesión ya no es renovable.
                getServletContext().log(
                        "Logout: sesión expirada");

            } catch (IOException ex) {

                // No impedir el cierre local.
                getServletContext().log(
                        "Logout: no se pudo confirmar la revocación",
                        ex);

            } finally {

                // Impide reutilizar la sesión en Tomcat.
                try {
                    session.invalidate();
                } catch (IllegalStateException ignored) {
                    // Ya fue invalidada.
                }
            }
        }

        // Eliminar la cookie de sesión.
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);

        String path = request.getContextPath();
        cookie.setPath(path.isEmpty() ? "/" : path);

        cookie.setSecure(request.isSecure());

        response.addCookie(cookie);

        response.sendRedirect(
            request.getContextPath() + "/index.jsp"
        );
    }
}