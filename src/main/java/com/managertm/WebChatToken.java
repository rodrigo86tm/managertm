
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

        response.setCharacterEncoding("UTF-8");

        // 1. Obtener ssid
        String ssid = getToken.token(request);

        if (ssid == null || ssid.isEmpty()) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    new JSONObject()
                            .put("success", false)
                            .put("message", "No autenticado")
                            .toString());

            return;
        }

        // 2. Validar ssid contra API
        String userId = validarToken(ssid);

        if (userId == null || userId.isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    new JSONObject()
                            .put("success", false)
                            .put("message", "Sesion invalida o vencida")
                            .toString());

            return;
        }

        // 3. Generar JWT WebChat
        String jwt =
                tokenService.generateToken(userId);

        // 4. Devolver JWT al navegador
        JSONObject result =
                new JSONObject()
                        .put("success", true)
                        .put("token", jwt);

        response.getWriter().write(
                result.toString());
    }

    private String validarToken(String ssid)
            throws IOException {
        URL url = new URL(urlstatic.url);

        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");

        conn.setRequestProperty(
                "Content-Type",
                "application/json");

        conn.setRequestProperty(
                "Authorization",
                "Bearer " + ssid);

        conn.setDoOutput(true);

        JSONObject body =
                new JSONObject()
                        .put("Accion", "WebChatValidate");

        byte[] input =
                body.toString().getBytes("UTF-8");

        try (OutputStream os =
                     conn.getOutputStream()) {

            os.write(input, 0, input.length);
        }

        int status =
                conn.getResponseCode();

        InputStream stream;

        if (status >= 200 && status < 300) {

            stream = conn.getInputStream();

        } else {

            stream = conn.getErrorStream();
        }

        if (stream == null)
            return null;

        StringBuilder result =
                new StringBuilder();

        try (BufferedReader br =
                     new BufferedReader(
                             new InputStreamReader(
                                     stream,
                                     "UTF-8"))) {

            String line;

            while ((line = br.readLine()) != null) {

                result.append(line);
            }
        }

        if (status < 200 || status >= 300)
            return null;

        JSONObject json =
                new JSONObject(result.toString());

        if (!json.optBoolean("success", false))
            return null;

        return json.optString("user", null);
    }
}
