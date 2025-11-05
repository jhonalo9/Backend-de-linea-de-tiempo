package com.utp.timeline.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    public GoogleAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, String> exchangeCodeForTokens(String code) {
        try {
            String tokenUrl = "https://oauth2.googleapis.com/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("code", code);
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("redirect_uri", redirectUri);
            body.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", (String) response.getBody().get("access_token"));
                tokens.put("id_token", (String) response.getBody().get("id_token"));
                return tokens;
            } else {
                throw new RuntimeException("Error al intercambiar código por tokens");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error en el intercambio de tokens: " + e.getMessage());
        }
    }

    public GoogleUserInfo verifyToken(String idToken) {
        try {
            String verifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

            ResponseEntity<Map> response = restTemplate.getForEntity(verifyUrl, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> tokenInfo = response.getBody();

                // Verificar que el token es para nuestra aplicación
                if (!clientId.equals(tokenInfo.get("aud"))) {
                    throw new RuntimeException("Token no válido para esta aplicación");
                }

                return new GoogleUserInfo(
                        (String) tokenInfo.get("sub"),
                        (String) tokenInfo.get("email"),
                        (String) tokenInfo.get("name"),
                        (String) tokenInfo.get("picture"),
                        "true".equals(tokenInfo.get("email_verified"))
                );
            } else {
                throw new RuntimeException("Token de Google inválido");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al verificar token: " + e.getMessage());
        }
    }

    public static class GoogleUserInfo {
        private final String sub;
        private final String email;
        private final String name;
        private final String picture;
        private final boolean emailVerified;

        public GoogleUserInfo(String sub, String email, String name, String picture, boolean emailVerified) {
            this.sub = sub;
            this.email = email;
            this.name = name;
            this.picture = picture;
            this.emailVerified = emailVerified;
        }

        // Getters
        public String getSub() { return sub; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getPicture() { return picture; }
        public boolean isEmailVerified() { return emailVerified; }
    }
}