/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.managertm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Usuario
 */
@WebServlet("/AdminUsuarios")
public class Servlet_Proxy_UsuariosAdmin extends HttpServlet {

    @Override protected void service(HttpServletRequest req,HttpServletResponse resp)
            throws ServletException,IOException {
        String tokenObj = getToken.token(req);
        if(!(tokenObj instanceof String) || ((String)tokenObj).isEmpty()) {
            resp.sendError(401,"Sesión vencida");return;
        }
        String method=req.getMethod();
        /*if(!"GET".equals(method)) {
            String expected=(String)session.getAttribute("adminUsuariosCsrf");
            String supplied=req.getHeader("X-Admin-CSRF");
            if(expected==null || supplied==null || !MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                resp.sendError(403,"CSRF inválido");return;
            }
        }*/
        String action=req.getParameter("action");
        String target;
        if("initial".equals(action) && "GET".equals(method)) target="/initial";
        else if("create".equals(action) && "POST".equals(method)) target="";
        else if("update".equals(action) && "PUT".equals(method)) target="/"+validId(req.getParameter("id"));
        else if("reset".equals(action) && "POST".equals(method)) target="/"+validId(req.getParameter("id"))+"/reset-password";
        else {resp.sendError(400,"Acción inválida");return;}
        if(target.contains("/-1")) {resp.sendError(400,"Id inválido");return;}
        HttpURLConnection conn=(HttpURLConnection)new URL(urlstatic.url_Admin+target).openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization","Bearer "+tokenObj);
        conn.setRequestProperty("Accept","application/json");
        conn.setConnectTimeout(10000);conn.setReadTimeout(25000);
        if(!"GET".equals(method)) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            try(InputStream in=req.getInputStream();OutputStream out=conn.getOutputStream()) {
                byte[] buffer=new byte[8192];int n;
                while((n=in.read(buffer))!=-1) out.write(buffer,0,n);
            }
        }
        int status=conn.getResponseCode();
        resp.setStatus(status);resp.setContentType("application/json;charset=UTF-8");
        try(InputStream in=status>=400?conn.getErrorStream():conn.getInputStream();
            OutputStream out=resp.getOutputStream()) {
            if(in!=null){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);}
        } finally {conn.disconnect();}
    }
    private static int validId(String raw) {
        try {int x=Integer.parseInt(raw);return x>0?x:-1;} catch(Exception ex){return -1;}
    }
}
