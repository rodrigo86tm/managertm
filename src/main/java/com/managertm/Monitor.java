/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.managertm;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Usuario
 */
@WebServlet(
    name = "Monitor",
    urlPatterns = {"/Monitor"}
)
public class Monitor extends HttpServlet {   
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        getStatus(response);
    }


    private void getStatus(
            HttpServletResponse response)
            throws IOException {

        HttpURLConnection connection = null;

        try {

            URL url =
                new URL(urlstatic.STATUS_API);

            connection =
                (HttpURLConnection)
                    url.openConnection();

            connection.setRequestMethod("GET");

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            connection.setRequestProperty(
                "Accept",
                "application/json"
            );


            int statusCode =
                connection.getResponseCode();


            InputStream stream;

            if (
                statusCode >= 200 &&
                statusCode < 300
            ) {

                stream =
                    connection.getInputStream();

            } else {

                stream =
                    connection.getErrorStream();
            }


            String json =
                readStream(stream);


            response.setStatus(statusCode);

            response.setContentType(
                "application/json"
            );

            response.setCharacterEncoding(
                "UTF-8"
            );

            response
                .getWriter()
                .write(json);

        }
        catch (Exception ex) {

            ex.printStackTrace();

            response.setStatus(
                HttpServletResponse
                    .SC_SERVICE_UNAVAILABLE
            );

            response.setContentType(
                "application/json"
            );

            response.setCharacterEncoding(
                "UTF-8"
            );

            response
                .getWriter()
                .write(
                    "{\"success\":false,"
                    + "\"message\":\""
                    + "No se pudo consultar el monitor."
                    + "\"}"
                );
        }
        finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    private String readStream(
            InputStream stream)
            throws IOException {

        if (stream == null) {
            return "{}";
        }

        StringBuilder result =
            new StringBuilder();

        try (
            BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(
                        stream,
                        StandardCharsets.UTF_8
                    )
                )
        ) {

            String line;

            while (
                (line = reader.readLine())
                != null
            ) {

                result.append(line);
            }
        }

        return result.toString();
    }
}