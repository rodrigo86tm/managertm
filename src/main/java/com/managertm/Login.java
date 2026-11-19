
package com.managertm;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.JSONObject;

@WebServlet(name = "Login", urlPatterns = {"/Login"})
public class Login extends HttpServlet {

    private static final Set<String> ALLOWED_ROLES =
            new HashSet<>(Arrays.asList(
                    "Jefe",
                    "Logistica",
                    "Administrador",
                    "Director",
                    "VendedorOnline",
                    "Colector",
                    "Operador"
            ));

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String username = request.getParameter("userN");
        String password = request.getParameter("passW");
        String type = request.getParameter("type");

        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            error(response, 400, "Datos incompletos");
            return;
        }

        HttpSession session = request.getSession();

        try {
            JSONObject body = new JSONObject();
            body.put("Username", username);
            body.put("ClientType", "WEB");
            body.put("DeviceName", "logisticatm");

            JSONObject result;

            if ("login".equals(type)) {

                session.removeAttribute("mfaTicket");
                session.removeAttribute("mfaUsername");

                body.put("Password", password);

                result = AuthApiClient.post(
                        "/login", body, null);

                if (result.optBoolean("changePassword")) {
                    response.getWriter().write(
                            new JSONObject()
                                    .put("success", true)
                                    .put("ChangePassword", true)
                                    .toString());
                    return;
                }

                if (result.optBoolean("requireMfa")) {

                    String ticket =
                            result.optString("mfaTicket");

                    if (ticket.isEmpty()) {
                        error(response, 502, "Ticket MFA ausente");
                        return;
                    }

                    session.setAttribute("mfaTicket", ticket);
                    session.setAttribute("mfaUsername", username);

                    response.getWriter().write(
                            new JSONObject()
                                    .put("success", true)
                                    .put("requireMfa", true)
                                    .toString());
                    return;
                }

            } else if ("code".equals(type)) {

                String ticket = (String)
                        session.getAttribute("mfaTicket");

                String pendingUser = (String)
                        session.getAttribute("mfaUsername");

                if (ticket == null
                        || !username.equals(pendingUser)) {
                    error(response, 401, "MFA no iniciado");
                    return;
                }

                body.put("MfaTicket", ticket);
                body.put("Code", password);

                result = AuthApiClient.post(
                        "/login/mfa", body, null);

            } else {
                error(response, 400, "Operación inválida");
                return;
            }

            String accessToken =
                    result.optString("AccessToken");

            String refreshToken =
                    result.optString("RefreshToken");

            if (accessToken.isEmpty()
                    || refreshToken.isEmpty()) {
                error(response, 502, "Respuesta de login inválida");
                return;
            }

            JSONObject profileResponse = AuthApiClient.get(
                    "/me", accessToken);

            JSONObject profile =
                    profileResponse.getJSONObject("data");

            String rol = profile.optString("Rol");

            if (!ALLOWED_ROLES.contains(rol)) {
                // No conservar credenciales de un usuario sin acceso.
                try {
                    AuthApiClient.post(
                            "/logout",
                            new JSONObject(),
                            accessToken);
                } catch (IOException ex) {
                    getServletContext().log(
                            "No se pudo revocar sesión no autorizada",
                            ex);
                }

                error(response, 403, "No autorizado");
                return;
            }

            // Evitar reutilizar una sesión previa al login.
            session.invalidate();
            session = request.getSession(true);

            session.setAttribute("accessToken", accessToken);
            session.setAttribute("refreshToken", refreshToken);
            session.setAttribute(
                    "accessExpiresAtUtc",
                    result.optString("AccessExpiresAtUtc"));
            session.setAttribute(
                    "refreshExpiresAtUtc",
                    result.optString("RefreshExpiresAtUtc"));

            session.setAttribute("profile", profile);
            session.setAttribute("usuario",
                    profile.optString("Usuario"));
            session.setAttribute("rol", rol);
            session.setAttribute("sucursal",
                    profile.optString("Sucursal"));

            // No sustituye los vencimientos impuestos por .NET.
            session.setMaxInactiveInterval(12 * 60 * 60);

            response.getWriter().write(
                    new JSONObject()
                            .put("success", true)
                            .toString());

        } catch (AuthApiClient.AuthApiException ex) {

            int status = ex.getStatus();

            if (status == 401) {
                error(response, 401, "Credenciales inválidas");
            } else {
                error(response, 502, "Error de autenticación");
            }

        } catch (Exception ex) {

            getServletContext().log(
                    "Error en Login", ex);

            error(response, 500, "Error interno");
        }
    }

    private void error(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        response.setStatus(status);

        response.getWriter().write(
                new JSONObject()
                        .put("success", false)
                        .put("message", message)
                        .toString());
    }
}