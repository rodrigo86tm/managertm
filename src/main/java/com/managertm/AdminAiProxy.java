package com.managertm;

import java.io.*;
import java.net.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/AdminAI")
public class AdminAiProxy extends HttpServlet {

    

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String token = getToken.token(req);
        if (token == null || token.isEmpty()) {
            resp.sendError(401, "Sesión vencida");
            return;
        }
        String method = req.getMethod();
        if (!method.equals("GET") && !method.equals("POST") && !method.equals("PUT")) {
            resp.sendError(405);
            return;
        }
        String resource = req.getParameter("resource");
        if (resource == null || !resource.matches("agents|tools|definitions|schedules|cases|executions|events|evidence|assignments|dashboard")) {
            resp.sendError(400, "Recurso inválido");
            return;
        }
        String id = req.getParameter("id");
        if (id != null && !id.matches("[1-9][0-9]{0,17}")) {
            resp.sendError(400, "ID inválido");
            return;
        }
        String action = req.getParameter("action");
        if (action != null && !action.matches("status|queue|create")) {
            resp.sendError(400, "Acción inválida");
            return;
        }
        String path = "/" + resource + (id != null ? "/" + id : "") + (action != null ? "/" + action : "");
        if (action != null && !method.equals("POST")) {
            resp.sendError(405);
            return;
        }
        String query = "";
        if (method.equals("GET")) {
            String page = req.getParameter("page"), size = req.getParameter("size"), search = req.getParameter("search");
            if (page != null && !page.matches("[0-9]{1,6}")) {
                resp.sendError(400);
                return;
            }
            if (size != null && !size.matches("[0-9]{1,3}")) {
                resp.sendError(400);
                return;
            }
            query = "?page=" + (page == null ? "1" : page) + "&size=" + (size == null ? "50" : size);
            if (search != null) {
                query += "&search=" + URLEncoder.encode(search, "UTF-8");
            }
        }
        HttpURLConnection conn = (HttpURLConnection) new URL(urlstatic.urlAI + path + query).openConnection();
        try {
            conn.setRequestMethod(method);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "application/json");
            if (!method.equals("GET")) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                try (InputStream in = req.getInputStream(); OutputStream out = conn.getOutputStream()) {
                    byte[] b = new byte[8192];
                    int n;
                    while ((n = in.read(b)) != -1) {
                        out.write(b, 0, n);
                    }
                }
            }
            int code = conn.getResponseCode();
            resp.setStatus(code);
            resp.setContentType("application/json;charset=UTF-8");
            try (InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream(); OutputStream out = resp.getOutputStream()) {
                if (in != null) {
                    byte[] b = new byte[8192];
                    int n;
                    while ((n = in.read(b)) != -1) {
                        out.write(b, 0, n);
                    }
                }
            }
        } finally {
            conn.disconnect();
        }
    }
}
