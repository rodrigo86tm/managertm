
package com.managertm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;


@WebServlet(name = "Login", urlPatterns = {"/Login"})
public class Login extends HttpServlet {

 @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("Login POST request received.");
        String username = request.getParameter("userN");
        String password = request.getParameter("passW");
        String types = request.getParameter("type");
        Map<String,String> data = new HashMap<>();
        
        if("login".equals(types)){
            data.put("Accion","LoginNew");
        }else{
            data.put("Accion","LoginMfa"); 
        }
        data.put("Parametro1", username);
        data.put("Parametro2", password);
        String[] list = {"Jefe","Logistica","Administrador","Director"};
        List<String> forbiden = Arrays.asList(list);        
        
        String jsonResponse = enviarSolicitud(data);
        JSONObject responseObj = new JSONObject(jsonResponse);
        
        boolean success  = false;
        
        if (!responseObj.optString("requireMfa").isEmpty() ||
            !responseObj.optString("mToken").isEmpty()) {
            success = true;
        }

        if(!success) {

            String msg = responseObj.optString(
                            "message",
                            "Error login");

            EnviarResponseError(response, request, msg);
            return;
        }
        boolean req = responseObj.optBoolean("requireMfa",false);
        if(req){
            response.getWriter().write(new JSONObject().put("success", true).put("requireMfa", true).toString());
            return;
        }        
        String token = responseObj.optString("mToken");
        String rol = responseObj.optString("rol");
        String suc = responseObj.optString("sucursal");
        if(!forbiden.contains(rol)) {
            EnviarResponseError(response,request,"No autorizado");
            return;
        }        
        String expire = responseObj.optString("Expiresin");
        int duration;
        duration = calcExpiration2(expire);
        if (token != null && !token.isEmpty()) {
            Cookie tokenCookie = new Cookie("ssid",token);
            tokenCookie.setHttpOnly(true);
            tokenCookie.setSecure(true);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(duration);
            //response.setHeader("Set-Cookie", String.format("ssid=%s;Max-Age=%d;Path=/;HttpOnly;SameSite=Strict",token,3600));
            
            Cookie rolCookie = new Cookie("rol", rol);
            rolCookie.setHttpOnly(false);
            rolCookie.setSecure(true);
            rolCookie.setPath("/");
            rolCookie.setMaxAge(duration);

            Cookie sucursalCookie = new Cookie("sucursal", suc);
            sucursalCookie.setHttpOnly(true);
            sucursalCookie.setSecure(true);
            sucursalCookie.setPath("/");
            sucursalCookie.setMaxAge(duration);
            
            Cookie userCookie = new Cookie("user", username);
            userCookie.setHttpOnly(true);
            userCookie.setSecure(true);
            userCookie.setPath("/");
            userCookie.setMaxAge(duration);
            
            
            response.addCookie(tokenCookie);
            response.addCookie(sucursalCookie);
            response.addCookie(rolCookie);
            response.addCookie(userCookie);
            
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new JSONObject().put("success", true).toString());
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            // Enviar respuesta JSON de error
            response.setContentType("application/json");
            response.getWriter().write(new JSONObject().put("success", false).put("message", "Invalid credentials").toString());
        }
    }
    public int calcExpiration(String expire) throws ParseException{
        if(expire.equals("0001-01-01T00:00:00")){
            return 3600;
        }
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
            Date expirationDate = sdf.parse(expire);
            Date now = new Date();
            long difMillis = expirationDate.getTime() - now.getTime();
            long difSeconds = difMillis / 1000;            
            return (int)(difMillis /1000);
        }catch(ParseException e){
            return 3600;
        }
    }
    private void EnviarResponseError(HttpServletResponse response,HttpServletRequest request,String msg) throws IOException{
        HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            // Enviar respuesta JSON de error
            response.setContentType("application/json");
            response.getWriter().write(new JSONObject().put("success", false).put("message", msg).toString());
    }
   
    private void enviarJson(HttpURLConnection conn,String jsonInputString)throws IOException{
        try(OutputStream os = conn.getOutputStream()){
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input,0,input.length);
        }
    }

    private String enviarSolicitud(Map<String,String> solicitud) throws IOException{
        URL url = new URL(urlstatic.url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json");
        
        String jsonInputString = new JSONObject(solicitud).toString();
        
        conn.setDoOutput(true);
        
        try(OutputStream os = conn.getOutputStream()){
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input,0,input.length);
        }
        int statusCode = conn.getResponseCode();
        InputStream stream;
        if(statusCode>=200 && statusCode <=300){
            stream = conn.getInputStream();
        }else{
            stream = conn.getErrorStream();
        }
        try(BufferedReader br = new BufferedReader(new InputStreamReader(stream,"utf-8"))){
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine())!= null){
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
    private int calcExpiration2(String fecha){        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localExp;
        try{
            localExp = LocalDateTime.parse(fecha, formatter);
        } catch (Exception e) {
            return 3600;
        }
        ZonedDateTime fechaExpiracion = localExp.atZone(ZoneOffset.UTC);
        ZonedDateTime ahora = ZonedDateTime.now(ZoneOffset.UTC);
        
        if (fechaExpiracion.isBefore(ahora) || fechaExpiracion.getYear() < 2000) {
            return 3600; 
        }
        Duration duration = Duration.between(ahora, fechaExpiracion);
        return (int) duration.getSeconds();
    }

}
