package com.utp.timeline.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.utp.timeline.entity.Usuario;
import com.utp.timeline.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Rol principal basado en el campo 'rol'
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

        // Authority adicional basado en el plan
        authorities.add(new SimpleGrantedAuthority(usuario.getPlan().name()));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getContrasena())
                .authorities(authorities)
                .build();
    }

    public Usuario loadUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}