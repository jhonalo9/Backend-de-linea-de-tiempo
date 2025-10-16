package com.utp.timeline.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${app.google.client-id}")
    private String googleClientId;

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService() {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleUserInfo verifyToken(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                return new GoogleUserInfo(
                        payload.getSubject(),
                        payload.getEmail(),
                        (String) payload.get("name"),
                        Boolean.TRUE.equals(payload.getEmailVerified())
                );
            } else {
                throw new RuntimeException("Token de Google inválido");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error verificando token de Google: " + e.getMessage());
        }
    }

    public static class GoogleUserInfo {
        private final String sub;
        private final String email;
        private final String name;
        private final boolean emailVerified;

        public GoogleUserInfo(String sub, String email, String name, boolean emailVerified) {
            this.sub = sub;
            this.email = email;
            this.name = name;
            this.emailVerified = emailVerified;
        }

        // Getters
        public String getSub() { return sub; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public boolean isEmailVerified() { return emailVerified; }
    }
}