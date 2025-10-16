package com.utp.timeline.controller;

import com.utp.timeline.config.JwtService;
import com.utp.timeline.entity.Usuario;
import com.utp.timeline.service.CustomUserDetailsService;
import com.utp.timeline.service.GoogleAuthService;
import com.utp.timeline.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GoogleAuthService googleAuthService;

    // Login tradicional
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        // Autenticar usuario
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Cargar detalles del usuario
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(loginRequest.getEmail());

        // Generar token
        String token = jwtService.generateToken(userDetails);

        // Calcular tiempo de expiración
        Long expiraEn = jwtService.getRemainingTime(token);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("tipo", "Bearer");
        response.put("expiraEn", expiraEn);
        response.put("usuario", Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "email", usuario.getEmail(),
                "rol", usuario.getRol(),
                "plan", usuario.getPlan()
        ));

        return ResponseEntity.ok(response);
    }

    // Login con Google
    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginGoogle(@RequestBody GoogleLoginRequest googleRequest) {
        try {
            // Validar token con Google
            GoogleAuthService.GoogleUserInfo googleUserInfo = googleAuthService.verifyToken(googleRequest.getToken());

            if (!googleUserInfo.isEmailVerified()) {
                throw new RuntimeException("Email de Google no verificado");
            }

            // Registrar o autenticar usuario con Google
            Usuario usuario = usuarioService.registrarConGoogle(
                    googleUserInfo.getSub(),
                    googleUserInfo.getEmail(),
                    googleUserInfo.getName()
            );

            // Generar token JWT para nuestra app
            UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
            String token = jwtService.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("tipo", "Bearer");
            response.put("expiraEn", jwtService.getRemainingTime(token));
            response.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "nombre", usuario.getNombre(),
                    "email", usuario.getEmail(),
                    "rol", usuario.getRol(),
                    "plan", usuario.getPlan(),
                    "googleId", usuario.getGoogleId()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Error en autenticación con Google",
                    "message", e.getMessage()
            ));
        }
    }

    // Registro de nuevo usuario
    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registro(@RequestBody RegistroRequest registroRequest) {
        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(registroRequest.getNombre());
        nuevoUsuario.setEmail(registroRequest.getEmail());
        nuevoUsuario.setContrasena(registroRequest.getPassword());

        Usuario usuarioCreado = usuarioService.crearUsuario(nuevoUsuario);

        // Autenticar y generar token
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuarioCreado.getEmail());
        String token = jwtService.generateToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("tipo", "Bearer");
        response.put("expiraEn", jwtService.getRemainingTime(token));
        response.put("usuario", Map.of(
                "id", usuarioCreado.getId(),
                "nombre", usuarioCreado.getNombre(),
                "email", usuarioCreado.getEmail(),
                "rol", usuarioCreado.getRol(),
                "plan", usuarioCreado.getPlan()
        ));

        return ResponseEntity.ok(response);
    }

    // Verificar token
    @PostMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificarToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        boolean esValido = jwtService.validateToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valido", esValido);

        if (esValido) {
            String email = jwtService.extractUsername(token);
            Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);
            response.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "nombre", usuario.getNombre(),
                    "email", usuario.getEmail(),
                    "rol", usuario.getRol(),
                    "plan", usuario.getPlan()
            ));
            response.put("tiempoRestante", jwtService.getRemainingTime(token));
        }

        return ResponseEntity.ok(response);
    }

    // Refrescar token
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        try {
            String nuevoToken = jwtService.refreshToken(token);
            String email = jwtService.extractUsername(nuevoToken);
            Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("token", nuevoToken);
            response.put("tipo", "Bearer");
            response.put("expiraEn", jwtService.getRemainingTime(nuevoToken));
            response.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "nombre", usuario.getNombre(),
                    "email", usuario.getEmail(),
                    "rol", usuario.getRol(),
                    "plan", usuario.getPlan()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token inválido"));
        }
    }

    // LOGOUT básico
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Log para debugging
                System.out.println("Usuario hizo logout - Token: " + token.substring(0, 10) + "...");
            }

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Logout exitoso");
            response.put("instruccion", "Eliminar el token del almacenamiento del cliente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error en logout"));
        }
    }





    // Método para validar token de Google (implementación básica)
    private GoogleUserInfo validateGoogleToken(String token) {
        // En una implementación real, aquí harías una llamada a Google API
        // para verificar el token y obtener la información del usuario

        // Por ahora, simulemos la validación (debes implementar esto correctamente)
        try {
            // Esta es una simulación - implementa la validación real con Google API
            return new GoogleUserInfo(
                    "google_user_id_123", // sub
                    "usuario@google.com", // email
                    "Usuario Google",     // name
                    true                  // email_verified
            );
        } catch (Exception e) {
            throw new RuntimeException("Token de Google inválido");
        }
    }

    // DTOs para requests
    public static class LoginRequest {
        private String email;
        private String password;

        // Getters y Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegistroRequest {
        private String nombre;
        private String email;
        private String password;

        // Getters y Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class GoogleLoginRequest {
        private String token;

        // Getters y Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    // Clase para representar la información del usuario de Google
    public static class GoogleUserInfo {
        private String sub; // ID único de Google
        private String email;
        private String name;
        private boolean emailVerified;

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