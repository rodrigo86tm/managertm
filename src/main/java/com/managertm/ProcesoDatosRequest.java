
package com.managertm;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;

@WebServlet(name = "ProcesoDatosRequest", urlPatterns = {"/ProcesoDatosRequest"})
public class ProcesoDatosRequest extends HttpServlet {
private static ObjectMapper mapper = new ObjectMapper();
     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = getToken.token(request);
        //if(token == null){
        if (token != null){
            RepoQuerys repo = new RepoQuerys();
            String jsonResponse,jsonFinal;
            boolean condTrue = false;
            request.setCharacterEncoding("UTF-8");
            String jsonBody;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
                jsonBody = reader.lines().collect(Collectors.joining());
            }
            if(jsonBody.trim().startsWith("{")){
                try{
                    JSONObject json = new JSONObject(jsonBody);            
                    if( json.has("Parametro1")){
                        String type = json.getString("Parametro1");
                        JSONArray conditions = json.getString("Parametro3") !=null && json.getString("Parametro3").trim().startsWith("[")?
                                new JSONArray(json.getString("Parametro3")): null;
                        if(json.has("Parametro4")){
                            Object val = json.get("Parametro4");
                            if((val instanceof Boolean && (Boolean) val) || (val instanceof String && ((String) val).equalsIgnoreCase("true"))){
                                condTrue = true;
                            }
                        }
                        JSONObject params = json.has("Parametro2") ? new JSONObject(json.getString("Parametro2")) : null;                             
                        Request req = repo.QuerysEnvios(type, conditions, params,condTrue);
                        if(condTrue == false){
                            String query = req.Parametro1;
                            req.Parametro1 = query + json.getString("Parametro3");
                        }
                        jsonFinal = mapper.writeValueAsString(req);                    
                    }else{
                        SendErrorResponse(response);
                        return;
                    }
                }catch(JSONException e){
                    SendErrorResponse(response);
                    return;
                }
            }else{
                jsonFinal = jsonBody;
            } 
            /*try (FileWriter fw = new FileWriter("C:\\temp\\debug_response.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {

               out.println("=== " + java.time.LocalDateTime.now() + " ===");
               out.println("Token: " + token);
               out.println("Json Enviado:");
               out.println(jsonFinal);
               out.println("========================================");
           } catch (IOException e) {
               e.printStackTrace();
           }*/
            String[] resultado = enviarSolicitud(jsonFinal,token); 
            int statusCode = Integer.parseInt(resultado[0]);
            jsonResponse = resultado[1];
            response.setStatus(statusCode);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse);            
        }else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"TokenVencido\"}");
            return;
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    private String[] enviarSolicitud(String solicitud,String token) throws IOException{
        //URL url = new URL("http://localhost:47994/api/ApiTM/");
        URL url = new URL(urlstatic.url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
        conn.setRequestProperty("Authorization", "Bearer "+ token);
        
        String jsonInputString = solicitud;
        
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
        /*try (FileWriter fw = new FileWriter("C:\\temp\\debug_response.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)) {

           out.println("=== " + java.time.LocalDateTime.now() + " ===");
           out.println("Status Code: " + String.valueOf(status));
           out.println("Response JSON:");
           out.println(response.toString());
           out.println("========================================");
       } catch (IOException e) {
           e.printStackTrace();
       }*/
        return new String[]{String.valueOf(status), response.toString()};
    }
    private void SendErrorResponse(HttpServletResponse response) throws IOException{
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"JSON inválido\"}");
    }
}
