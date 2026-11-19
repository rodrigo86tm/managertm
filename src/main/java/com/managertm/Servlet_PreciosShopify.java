/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.managertm;
import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
    name = "Servlet_PreciosShopify",
    urlPatterns = {"/PreciosShopify"}
)
public class Servlet_PreciosShopify extends HttpServlet {

    private static final java.util.logging.Logger LOG =
        java.util.logging.Logger.getLogger(
            Servlet_PreciosShopify.class.getName()
        );

    @Override
    protected void service(
        HttpServletRequest req,
        HttpServletResponse resp
    ) throws ServletException, IOException {

        String token;

        try {
            token = getToken.token(req);
        } catch (RuntimeException ex) {
            resp.sendError(401, "Sesión vencida");
            return;
        }

        String method = req.getMethod();
        String target;

        if ("GET".equals(method)) {

            String sku = req.getParameter("sku");

            if (sku == null || sku.trim().isEmpty()) {
                resp.sendError(400, "SKU requerido");
                return;
            }
            switch(sku){
                case "total":
                    target = "/databaseproductos?total=" +
                    java.net.URLEncoder.encode(sku.trim(), "UTF-8");
                    break;
                case "variants":
                case "prices":
                    target = "/sync_shopify?type=" +
                    java.net.URLEncoder.encode(sku.trim(), "UTF-8");
                    break;     
                default:
                    target = "/buscar?sku=" +
                    java.net.URLEncoder.encode(sku.trim(), "UTF-8");
            }            

        } else if ("POST".equals(method)) {

            target = "/sincronizar";

        } else {
            resp.sendError(405);
            return;
        }

        String apiUrl = urlstatic.url_Precios + target;

        LOG.info("[SHOPIFY] Solicitando: " + apiUrl);

        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection)
                new URL(apiUrl).openConnection();

            conn.setRequestMethod(method);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(60000);
            conn.setInstanceFollowRedirects(false);

            conn.setRequestProperty(
                "Authorization",
                "Bearer " + token
            );

            conn.setRequestProperty(
                "Accept",
                "application/json"
            );

            if ("POST".equals(method)) {

                conn.setDoOutput(true);

                conn.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                );

                try (
                    InputStream input = req.getInputStream();
                    OutputStream output = conn.getOutputStream()
                ) {
                    copiar(input, output);
                }
            }

            int status = conn.getResponseCode();

            InputStream apiStream = status >= 400
                ? conn.getErrorStream()
                : conn.getInputStream();

            byte[] body = new byte[0];

            if (apiStream != null) {
                try (InputStream input = apiStream;
                     ByteArrayOutputStream buffer =
                         new ByteArrayOutputStream()) {

                    copiar(input, buffer);
                    body = buffer.toByteArray();
                }
            }

            LOG.info(
                "[SHOPIFY] HTTP=" + status +
                " | Bytes=" + body.length +
                " | Content-Type=" + conn.getContentType()
            );

            if (body.length == 0) {

                LOG.warning(
                    "[SHOPIFY] API devolvió cuerpo vacío. URL=" +
                    apiUrl
                );

                resp.setStatus(502);
                resp.setContentType(
                    "application/json;charset=UTF-8"
                );

                resp.getWriter().write(
                    "{\"success\":false," +
                    "\"message\":\"La API devolvió una respuesta vacía.\"}"
                );

                return;
            }

            resp.setStatus(status);
            resp.setContentType(
                "application/json;charset=UTF-8"
            );

            resp.setContentLength(body.length);
            resp.getOutputStream().write(body);

        } catch (IOException ex) {

            LOG.log(
                java.util.logging.Level.SEVERE,
                "[SHOPIFY] Error comunicando con API: " + apiUrl,
                ex
            );

            if (!resp.isCommitted()) {
                resp.reset();
                resp.setStatus(502);
                resp.setContentType(
                    "application/json;charset=UTF-8"
                );

                resp.getWriter().write(
                    "{\"success\":false," +
                    "\"message\":\"Error de comunicación con la API.\"}"
                );
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void copiar(
        InputStream input,
        OutputStream output
    ) throws IOException {

        byte[] buffer = new byte[8192];
        int leidos;

        while ((leidos = input.read(buffer)) != -1) {
            output.write(buffer, 0, leidos);
        }
    }
}