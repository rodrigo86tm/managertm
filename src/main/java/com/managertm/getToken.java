package com.managertm;

import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class getToken {
    public static String token(
            HttpServletRequest request) throws IOException {

        return AuthSessionManager
                .getValidAccessToken(request);
    }
}
