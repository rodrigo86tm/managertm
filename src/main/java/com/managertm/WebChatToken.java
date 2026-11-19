
package com.managertm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

@WebServlet(
    name = "WebChatToken",
    urlPatterns = {"/chat/token"}
)
public class WebChatToken extends HttpServlet {

    private WebChatTokenService tokenService;

    @Override
    public void init() throws ServletException {

        try {

            String privateKeyPath =
                    "C:\\Security\\chat-private.pem";

            tokenService =
                    new WebChatTokenService(privateKeyPath);

        } catch (Exception e) {

            throw new ServletException(
                    "No se pudo cargar la clave privada WebChat",
                    e);
        }
    }

    @Override
    protected void doPost( HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        // 1. Validar sesión y renovar token si hace falta.
        try {
            getToken.token(request);
        } catch (AuthSessionManager.SessionExpiredException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    new JSONObject()
                            .put("success", false)
                            .put("message", "Sesión vencida")
                            .toString());
            return;
        } catch (IOException ex) {
            getServletContext().log("Error validando sesión WebSocket", ex);

            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write(
                    new JSONObject()
                            .put("success", false)
                            .put("message", "No se pudo validar la sesión")
                            .toString());

            return;
        }
        // 2. Obtener el perfil guardado durante el login.
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        JSONObject profile =(JSONObject) session.getAttribute("profile");

        if (profile == null) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String userId = profile.optString("Usuario", "");
        if (userId.isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // 3. Generar el JWT RSA de WebChat.
        String jwt = tokenService.generateToken(userId);
        // 4. Devolver el JWT al navegador.
        JSONObject result = new JSONObject()
                .put("success", true)
                .put("token", jwt);

        response.getWriter().write(result.toString());
    }
}
