
package com.managertm;

import io.jsonwebtoken.Jwts;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;


public class WebChatTokenService {

    private final PrivateKey privateKey;


    public WebChatTokenService(
            String privateKeyPath)
            throws Exception {

        this.privateKey =
                loadPrivateKey(
                        privateKeyPath
                );
    }


    private PrivateKey loadPrivateKey(
            String path)
            throws Exception {

        /*
         * Compatible con Java 8.
         */
        byte[] fileBytes =
                Files.readAllBytes(
                        Paths.get(path)
                );


        String pem =
                new String(
                        fileBytes,
                        StandardCharsets.UTF_8
                );


        pem = pem
                .replace(
                        "-----BEGIN PRIVATE KEY-----",
                        ""
                )
                .replace(
                        "-----END PRIVATE KEY-----",
                        ""
                )
                .replaceAll(
                        "\\s+",
                        ""
                );


        byte[] decoded =
                Base64
                        .getDecoder()
                        .decode(pem);


        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(
                        decoded
                );


        KeyFactory factory =
                KeyFactory.getInstance(
                        "RSA"
                );


        return factory.generatePrivate(
                spec
        );
    }


    public String generateToken(
            String userId) {

        long now =
                System.currentTimeMillis();


        Date issuedAt =
                new Date(now);


        Date expiration =
                new Date(
                        now +
                        (5 * 60 * 1000)
                );


        return Jwts.builder()
                .setSubject(userId)
                .setIssuer("TM_WEB_SERVLET")
                .setAudience(
                        "COMPANY_AI_WEB_CHAT"
                )
                .claim(
                        "purpose",
                        "WEB_CHAT"
                )
                .setId(
                        UUID.randomUUID()
                                .toString()
                )
                .setIssuedAt(
                        issuedAt
                )
                .setExpiration(
                        expiration
                )
                .signWith(
                        privateKey,
                        Jwts.SIG.RS256
                )
                .compact();
    }
}

