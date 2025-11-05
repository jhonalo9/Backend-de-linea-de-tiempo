package com.utp.timeline.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.utp.timeline.entity.Usuario;
import com.utp.timeline.repository.UsuarioRepository;

@Service
public class CurrentUserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Usuario) {
            return (Usuario) principal;
        } else {
            // Fallback: buscar por email si no es la entidad Usuario
            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }
            return usuarioRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        }
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}