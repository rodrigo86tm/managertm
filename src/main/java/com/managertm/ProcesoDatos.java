package com.managertm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

@WebServlet(name = "ProcesoDatos", urlPatterns = {"/ProcesoDatos"})
public class ProcesoDatos extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //HttpSession session = request.getSession(false);
        //String token = (session != null) ? (String) session.getAttribute("token") : null;
        String token = getToken.token(request);
        
        //if (token != null){
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            StringBuilder jsonBody = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                jsonBody.append(line);
            }
            JSONObject json = new JSONObject(jsonBody.toString());
            String action = "";
            if(json.has("Accion"))action = json.getString("Accion");
            
        if ("CambiarPasswordInicial".equals(action)) {
            Enviar(response, jsonBody.toString(), "");
            return;
        }

        // Las demás operaciones requieren token.
        if (token == null || token.trim().isEmpty()) {
            Salir(response);
            return;
        }
            
        Enviar(response,jsonBody.toString(),token);
    }
    

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    private void Enviar(HttpServletResponse response,String jsonBody,String token) throws IOException{
        ApiResponse api = enviarSolicitud(jsonBody,token);
            
            response.setStatus(api.status);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(api.body);
    }
    private void Salir(HttpServletResponse response) throws IOException{
         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"TokenVencido\"}");
            return;
    }
    private ApiResponse enviarSolicitud(String solicitud,String token) throws IOException{
        URL url = new URL(urlstatic.url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json");
        if(!token.equals("")) conn.setRequestProperty("Authorization", "Bearer "+ token);
        
        String jsonInputString = new JSONObject(solicitud).toString();
        
        conn.setDoOutput(true);
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
        return new ApiResponse(status, response.toString());
    }
}

