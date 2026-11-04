/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.managertm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;
import org.json.JSONObject;

@WebServlet(name = "ProcesoDatosSheets", urlPatterns = {"/ProcesoDatosSheets"})
public class ProcesoDatosSheets extends HttpServlet {
  @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = getToken.token(request);
        if(token != null){
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            StringBuilder jsonBody = new StringBuilder();
            String line;
            String[] resultado = null;
            while((line = reader.readLine()) != null){
                jsonBody.append(line);
            }
            if(jsonBody.toString().trim().startsWith("{")){
                try{
                    JSONObject json = new JSONObject(jsonBody.toString());            
                    if( json.has("rows")){
                        resultado = enviarSolicitud(jsonBody.toString(),"https://script.google.com/macros/s/AKfycbw-sqERg7aKp-twDygdqrx7IMU6dHwIganGBV7RtP5Z-hv2embxSX7wFgsY7ZHHibI/exec?hoja=Data");
                    }else{
                        resultado = EnviarGet("https://script.google.com/macros/s/AKfycbw-sqERg7aKp-twDygdqrx7IMU6dHwIganGBV7RtP5Z-hv2embxSX7wFgsY7ZHHibI/exec?accion=limpiar");
                    }
                }catch(Exception e){
                    
                }
            }
            int statusCode = Integer.parseInt(resultado[0]);
            String jsonResponse = resultado[1];
            
            response.setStatus(statusCode);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse);
            
        }else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Token invalido o Expirado\"}");
            return;
        }
    }
   
    private String[] enviarSolicitud(String solicitud,String urlS) throws IOException{
        URL url = new URL(urlS);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        
        String jsonInputString = solicitud;
        try(OutputStream os = conn.getOutputStream()){
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input,0,input.length);
        }
        int status = conn.getResponseCode(); // Obtiene el código HTTP de respuesta
        InputStream stream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder response = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(stream,"utf-8"))){            
            String responseLine;
            while ((responseLine = br.readLine())!= null){
                response.append(responseLine.trim());
            }            
        }
        return new String[]{String.valueOf(status), response.toString()};
    }
    private String[] EnviarGet(String urlS) throws IOException, ProtocolException{
        URL url = new URL(urlS);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int status = conn.getResponseCode();
        InputStream stream = (status>=200 && status <300) ? conn.getInputStream() : conn.getErrorStream();
        
        StringBuilder response = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(stream,"uft-8"))){
            String responseLine;
            while((responseLine = br.readLine()) != null){
                response.append(responseLine.trim());
            }   
        }
        return new String[]{String.valueOf(status),response.toString()};
    }
    private void SendErrorResponse(HttpServletResponse response) throws IOException{
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"JSON inválido\"}");
    }
}
