
package com.managertm;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public final class AuthApiClient {

    private AuthApiClient() {}

    public static JSONObject post(
            String path,
            JSONObject body,
            String accessToken) throws IOException {

        return execute("POST", path, body, accessToken);
    }

    public static JSONObject get(
            String path,
            String accessToken) throws IOException {

        return execute("GET", path, null, accessToken);
    }

    private static JSONObject execute(
            String method,
            String path,
            JSONObject body,
            String accessToken) throws IOException {

        HttpURLConnection conn = (HttpURLConnection)
                new URL(urlstatic.AUTH_URL + path)
                        .openConnection();

        try {
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty(
                    "Accept", "application/json");

            if (accessToken != null) {
                conn.setRequestProperty(
                        "Authorization",
                        "Bearer " + accessToken);
            }

            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(
                            StandardCharsets.UTF_8));
                }
            }

            int status = conn.getResponseCode();

            InputStream stream = status >= 200 && status < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            StringBuilder result = new StringBuilder();

            if (stream != null) {
                try (BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        stream,
                                        StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }
            }

            JSONObject json = result.length() > 0
                    ? new JSONObject(result.toString())
                    : new JSONObject();

            if (status < 200 || status >= 300) {
                throw new AuthApiException(
                        status,
                        json.optString("error", "AUTH_ERROR"));
            }

            return json;

        } finally {
            conn.disconnect();
        }
    }

    public static final class AuthApiException
            extends IOException {

        private final int status;

        public AuthApiException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
