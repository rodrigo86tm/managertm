package com.managertm;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Usuario
 */
@WebServlet(name = "ProcesoDatosChat", urlPatterns = {"/ProcesoDatosChat"})
public class ProcesoDatosChat extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = getToken.token(request);
        if (token == null
                || token.trim().isEmpty()) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"TokenVencido\"}");
            return;
        }
        String body = leerBody(request);
        System.out.println(
                "LiveChat request: " + body
        );
        try {
            JSONObject json = new JSONObject(body);
            String accion = json.optString("Accion", "");

            ApiResponse apiResponse = procesarAccion(accion, json, token);
            response.setStatus(
                    apiResponse.status
            );
            response.setContentType(
                    "application/json;charset=UTF-8"
            );
            response.setCharacterEncoding(
                    "UTF-8"
            );
            response.getWriter().write(
                    apiResponse.body
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
            response.setContentType(
                    "application/json;charset=UTF-8"
            );
            JSONObject error = new JSONObject();
            error.put("error", ex.getMessage());
            response.getWriter().write(
                    error.toString()
            );
        }
    }

    private String leerBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        try (
                BufferedReader reader
                = new BufferedReader(
                        new InputStreamReader(
                                request.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                )) {

            String line;

            while ((line = reader.readLine())
                    != null) {

                body.append(line);
            }
        }

        return body.toString();
    }

    private ApiResponse procesarAccion(String accion, JSONObject json, String token) throws IOException {
        switch (accion) {
            // ==========================
            // CONVERSACIONES
            // ==========================
            case "GetLiveChatConversations":
                String channelCode = json.optString("Parametro1");
                if (!"WHATSAPP".equals(channelCode)
                        && !"MERCADO_LIBRE".equals(channelCode)) {
                    return new ApiResponse(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "{\"error\":\"Canal inválido\"}"
                    );
                }
                return enviarSolicitud("GET", urlstatic.LIVECHAT_URL + "/conversations/" + channelCode, token, null);
            // ==========================
            // TAKE CONVERSACIONES
            // ==========================
            case "TakeConversation":
                long conversationIdt = json.getLong("Parametro1");
                return enviarSolicitud("POST", urlstatic.LIVECHAT_URL + "/conversations/" + conversationIdt + "/take", token, null);
            // ==========================
            // MENSAJES
            // ==========================
            case "GetLiveChatMessages":
                long conversationId = json.getLong("Parametro1");
                String beforeId = json.optString("Parametro2", "");
                int limit = json.optInt("Parametro3", 30);
                StringBuilder url = new StringBuilder();
                url.append(
                        urlstatic.LIVECHAT_URL
                );
                url.append("/conversations/");
                url.append(conversationId);
                url.append("/messages?limit=");
                url.append(limit);
                if (beforeId != null
                        && !beforeId.trim().isEmpty()) {

                    url.append(
                            "&beforeId="
                    );

                    url.append(
                            beforeId
                    );
                }
                return enviarSolicitud("GET", url.toString(), token, null);
            // ==========================
            // ASIGNACIÓN
            // ==========================
            case "ChangeLiveChatAssignment":
                long assignmentConversationId = json.getLong("Parametro1");
                JSONObject assignment = json.getJSONObject("Data");
                return enviarSolicitud("PUT", urlstatic.LIVECHAT_URL + "/conversations/" + assignmentConversationId + "/assignment",
                        token, assignment.toString());
            // ==========================
            // Devolvier a IA
            // ==========================    
            case "ReturnConversationToAI":
                long returnConversationId = json.getLong("Parametro1");
                return enviarSolicitud("POST", urlstatic.LIVECHAT_URL + "/conversations/" + returnConversationId + "/return-ai", token, null);

            case "MarkLiveChatRead":
                long conversationId2 = json.getLong("Parametro1");
                String url2 = urlstatic.LIVECHAT_URL + "/conversations/"
                        + conversationId2 + "/read";
                return enviarSolicitud("PUT", url2, token, "{}");

            case "GetInternalChatInitialData":
                return enviarSolicitud("GET", urlstatic.INTERNALCHAT_URL + "/initial", token, null);
            // GET {apiBase}/api/internalchat/initial               
            case "GetOrCreateDirectConversation":
                long destinationUserId = json.getLong("Parametro1");
                JSONObject jsonDestiny = new JSONObject();
                jsonDestiny.put("destinationUserId", destinationUserId);
                return enviarSolicitud("POST", urlstatic.INTERNALCHAT_URL + "/conversations", token, jsonDestiny.toString());
            // POST {apiBase}/api/internalchat/conversations
            // JSON: {"destinationUserId": Parametro1} (long)
            case "GetUnifiedChatMessages": {

                String mode = json.optString("Parametro1", "");
                String rawConversationId = json.optString("Parametro2", "");
                String beforeIdC = json.optString("Parametro3", "");
                int limitC = json.optInt("Parametro4", 30);

                if (!"AI".equals(mode) && !"DIRECT".equals(mode)) {
                    return new ApiResponse(
                            400,
                            "{\"error\":\"Modo de conversación inválido\"}"
                    );
                }

                if (rawConversationId.trim().isEmpty()) {
                    return new ApiResponse(
                            400,
                            "{\"error\":\"ConversationId no disponible\"}"
                    );
                }

                long conversationIdC;

                try {
                    conversationIdC = Long.parseLong(rawConversationId);
                    if (conversationIdC <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    return new ApiResponse(
                            400,
                            "{\"error\":\"ConversationId inválido\"}"
                    );
                }

                if ("DIRECT".equals(mode)) {
                    String urlC = urlstatic.INTERNALCHAT_URL
                            + "/conversations/" + conversationIdC
                            + "/messages?limit=" + limitC;

                    if (!beforeIdC.trim().isEmpty()) {
                        urlC += "&beforeId=" + beforeIdC;
                    }

                    return enviarSolicitud("GET", urlC, token, null);
                }
                if ("AI".equals(mode)) {
                    // El endpoint identifica al usuario desde Authorization.
                    // NO acepta un conversationId arbitrario.
                    String urlC = urlstatic.INTERNALCHAT_URL
                            + "/ai/messages?limit=" + Math.max(1, Math.min(limitC, 50));

                    if (beforeIdC != null && !beforeIdC.trim().isEmpty()) {
                        try {
                            long before = Long.parseLong(beforeIdC);
                            if (before <= 0) {
                                throw new NumberFormatException();
                            }
                            urlC += "&beforeId=" + before;
                        } catch (NumberFormatException ex) {
                            return new ApiResponse(400, "{\"error\":\"beforeId inválido\"}");
                        }
                    }

                    return enviarSolicitud("GET", urlC, token, null);
                }
                // La ruta de historial IA debe coincidir con tu API real.
                return new ApiResponse(
                        501,
                        "{\"error\":\"Falta configurar la ruta del historial IA\"}"
                );
            }
            case "MarkDirectConversationRead":
                long IconversationId2 = json.getLong("Parametro1");
                String Iurl2 = urlstatic.INTERNALCHAT_URL + "/conversations/"
                        + IconversationId2 + "/read";
                return enviarSolicitud("PUT", Iurl2, token, "{}");
            // PUT {apiBase}/api/internalchat/conversations/{Parametro1}/read
            default:
                return new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"Accion no reconocida\"}");
        }
    }

    private ApiResponse enviarSolicitud(String method, String urlString, String token, String body) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty(
                "Authorization",
                "Bearer " + token
        );
        conn.setRequestProperty(
                "Accept",
                "application/json"
        );
        /*
         * Solamente enviamos body
         * cuando corresponde.
         */
        if (body != null && !body.isEmpty()) {
            conn.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input
                        = body.getBytes(
                                StandardCharsets.UTF_8
                        );
                os.write(
                        input,
                        0,
                        input.length
                );
            }
        }
        int status = conn.getResponseCode();
        InputStream stream;
        if (status >= 200 && status < 300) {
            stream = conn.getInputStream();
        } else {
            stream = conn.getErrorStream();
        }
        StringBuilder result = new StringBuilder();
        if (stream != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }
            }
        }
        conn.disconnect();
        return new ApiResponse(
                status,
                result.toString()
        );
    }

    private static class ApiResponse {

        private final int status;
        private final String body;

        public ApiResponse(
                int status,
                String body) {

            this.status
                    = status;

            this.body
                    = body;
        }
    }
}
