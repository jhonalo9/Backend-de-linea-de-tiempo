package com.utp.timeline.config;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

   // Almacenar tokens revocados (en producción usa Redis)
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();
    
    // Almacenar refresh tokens válidos
    private final Map<String, String> validRefreshTokens = new ConcurrentHashMap<>();

    // Generar clave secreta SEGURA
    private SecretKey getSigningKey() {
        // IMPORTANTE: jwt.secret debe tener al menos 256 bits (32 bytes)
        byte[] keyBytes = jwtSecret.getBytes();
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                "JWT secret debe tener al menos 256 bits (32 caracteres)"
            );
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extraer username del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraer fecha de expiración
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraer tipo de token
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    // Extraer claim específico
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extraer todos los claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Verificar si el token ha expirado
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Verificar si el token está revocado
    public Boolean isTokenRevoked(String token) {
        return revokedTokens.contains(token);
    }

    // Generar Access Token
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    // Generar Refresh Token
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("jti", generateTokenId()); // Token ID único
        
        String refreshToken = createToken(claims, userDetails.getUsername(), refreshExpiration);
        
        // Guardar refresh token como válido
        validRefreshTokens.put(userDetails.getUsername(), refreshToken);
        
        return refreshToken;
    }

    // Generar par de tokens (access + refresh)
    public Map<String, String> generateTokenPair(UserDetails userDetails) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", generateAccessToken(userDetails));
        tokens.put("refreshToken", generateRefreshToken(userDetails));
        return tokens;
    }

    // Crear token JWT
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString()) // JTI para tracking
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validar Access Token
    public Boolean validateAccessToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final String tokenType = extractTokenType(token);
            
            return username.equals(userDetails.getUsername()) 
                && !isTokenExpired(token)
                && !isTokenRevoked(token)
                && "access".equals(tokenType);
        } catch (JwtException e) {
            return false;
        }
    }

    // Validar Refresh Token
    public Boolean validateRefreshToken(String token) {
        try {
            String username = extractUsername(token);
            String tokenType = extractTokenType(token);
            
            String storedToken = validRefreshTokens.get(username);
            
            return "refresh".equals(tokenType)
                && !isTokenExpired(token)
                && !isTokenRevoked(token)
                && token.equals(storedToken);
        } catch (JwtException e) {
            return false;
        }
    }

    // Validar token (genérico)
    public Boolean validateToken(String token, UserDetails userDetails) {
        return validateAccessToken(token, userDetails);
    }

    // Validar token sin UserDetails
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token) && !isTokenRevoked(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Refrescar Access Token usando Refresh Token
    public String refreshAccessToken(String refreshToken, UserDetails userDetails) {
        if (!validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido o expirado");
        }
        
        // Generar nuevo access token
        return generateAccessToken(userDetails);
    }

    // Revocar token (logout)
    public void revokeToken(String token) {
        revokedTokens.add(token);
        
        // Limpiar tokens expirados periódicamente
        cleanupExpiredTokens();
    }

    // Revocar todos los tokens de un usuario
    public void revokeAllUserTokens(String username) {
        validRefreshTokens.remove(username);
    }

    // Limpiar tokens expirados del conjunto de revocados
    private void cleanupExpiredTokens() {
        revokedTokens.removeIf(token -> {
            try {
                return isTokenExpired(token);
            } catch (Exception e) {
                return true; // Remover tokens inválidos
            }
        });
    }

    // Obtener tiempo restante hasta expiración
    public Long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return remaining > 0 ? remaining : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    // Generar ID único para tokens
    private String generateTokenId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // MÉTODO DEPRECADO - Usar generateTokenPair
    @Deprecated
    public String generateToken(UserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    // MÉTODO DEPRECADO - Usar refreshAccessToken
    @Deprecated
    public String refreshToken(String token) {
        throw new UnsupportedOperationException(
            "Usar refreshAccessToken con refresh token"
        );
    }
}