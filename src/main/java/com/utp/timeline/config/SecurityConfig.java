package com.utp.timeline.config;

import com.utp.timeline.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/api/publico/public/**").permitAll()
                        .requestMatchers("/api/publico/info/**").permitAll()
                        .requestMatchers("/api/publico/validar/**").permitAll()
                        .requestMatchers("/api/plantillas/publicas").permitAll()
                        .requestMatchers("/api/archivos/**").permitAll()

                        // Endpoints de administración - solo ADMIN
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/plantillas/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/favoritos/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/publico/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/estadisticas/**").hasAuthority("ROLE_ADMIN")

                        // Endpoints que requieren ser PREMIUM o ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/plantillas").hasAnyAuthority("PREMIUM", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/plantillas/*").hasAnyAuthority("PREMIUM", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/plantillas/*").hasAnyAuthority("PREMIUM", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/plantillas/*/visibilidad").hasAnyAuthority("PREMIUM", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/plantillas/*/duplicar").hasAnyAuthority("PREMIUM", "ROLE_ADMIN")
                        .requestMatchers("/api/plantillas/mis-plantillas").hasAnyAuthority("PREMIUM", "ROLE_ADMIN")

                        // Endpoints de lectura para todos autenticados
                       // .requestMatchers(HttpMethod.GET, "/api/plantillas").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/plantillas/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/plantillas/populares").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/plantillas/mas-usadas").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/plantillas/recientes").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/plantillas/buscar").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/plantillas/*/permisos").authenticated()

                        // Otros endpoints
                        .requestMatchers("/api/proyectos/**").authenticated()
                        .requestMatchers("/api/favoritos/**").authenticated()
                        .requestMatchers("/api/publico/proyecto/**").authenticated()
                        .requestMatchers("/api/usuarios/perfil/**").authenticated()
                        .requestMatchers("/api/usuarios/upgrade-premium").authenticated()

                        // Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Para desarrollo
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }



}