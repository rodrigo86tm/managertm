package com.managertm;

import java.io.IOException;
import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

public final class AuthSessionManager {

    private static final long RENEW_BEFORE_SECONDS = 60;

    private AuthSessionManager() {}

    public static String getValidAccessToken(
            HttpServletRequest request) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new SessionExpiredException();
        }

        // Evita que varias solicitudes de LiveChat
        // renueven simultáneamente el mismo Refresh Token.
        synchronized (session) {

            String accessToken =
                    (String) session.getAttribute("accessToken");

            String refreshToken =
                    (String) session.getAttribute("refreshToken");

            String expiresAt =
                    (String) session.getAttribute("accessExpiresAtUtc");

            if (accessToken == null || refreshToken == null) {
                throw new SessionExpiredException();
            }

            if (expiresAt == null || expiresAt.isEmpty()) {
                throw new IOException(
                        "Falta vencimiento del Access Token");
            }

            Instant expiration;

            try {
                expiration = Instant.parse(expiresAt);
            } catch (Exception ex) {
                throw new IOException(
                        "Fecha de vencimiento inválida", ex);
            }

            Instant renewAt = Instant.now()
                    .plusSeconds(RENEW_BEFORE_SECONDS);

            if (expiration.isAfter(renewAt)) {
                return accessToken;
            }

            return refreshLocked(session, refreshToken);
        }
    }

    public static String forceRefresh(
            HttpServletRequest request,
            String rejectedAccessToken) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new SessionExpiredException();
        }

        synchronized (session) {

            String currentToken =
                    (String) session.getAttribute("accessToken");

            if (currentToken == null) {
                throw new SessionExpiredException();
            }

            // Otra solicitud pudo haber renovado el token
            // mientras esta esperaba el bloqueo.
            if (!currentToken.equals(rejectedAccessToken)) {
                return currentToken;
            }

            String refreshToken =
                    (String) session.getAttribute("refreshToken");

            if (refreshToken == null) {
                throw new SessionExpiredException();
            }

            return refreshLocked(session, refreshToken);
        }
    }

    private static String refreshLocked(
            HttpSession session,
            String refreshToken) throws IOException {

        JSONObject body = new JSONObject()
                .put("refreshToken", refreshToken);

        JSONObject result;

        try {
            result = AuthApiClient.post(
                    "/refresh", body, null);

        } catch (AuthApiClient.AuthApiException ex) {

            if (ex.getStatus() == 400 ||
                    ex.getStatus() == 401) {

                clearCredentials(session);
                throw new SessionExpiredException();
            }

            throw ex;
        }

        String newAccess =
                result.optString("AccessToken");

        String newRefresh =
                result.optString("RefreshToken");

        String newAccessExpiry =
                result.optString("AccessExpiresAtUtc");

        String newRefreshExpiry =
                result.optString("RefreshExpiresAtUtc");

        if (newAccess.isEmpty() ||
                newRefresh.isEmpty() ||
                newAccessExpiry.isEmpty() ||
                newRefreshExpiry.isEmpty()) {

            throw new IOException(
                    "Respuesta de refresh incompleta");
        }

        try {
            Instant.parse(newAccessExpiry);
            Instant.parse(newRefreshExpiry);
        } catch (Exception ex) {
            throw new IOException(
                    "Vencimientos inválidos en refresh", ex);
        }

        session.setAttribute("accessToken", newAccess);
        session.setAttribute("refreshToken", newRefresh);
        session.setAttribute(
                "accessExpiresAtUtc", newAccessExpiry);
        session.setAttribute(
                "refreshExpiresAtUtc", newRefreshExpiry);

        return newAccess;
    }

    private static void clearCredentials(HttpSession session) {
        session.removeAttribute("accessToken");
        session.removeAttribute("refreshToken");
        session.removeAttribute("accessExpiresAtUtc");
        session.removeAttribute("refreshExpiresAtUtc");
        session.removeAttribute("profile");
    }

    public static class SessionExpiredException
            extends IOException {

        public SessionExpiredException() {
            super("Sesión vencida o revocada");
        }
    }
}
