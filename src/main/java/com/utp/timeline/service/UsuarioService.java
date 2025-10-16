package com.utp.timeline.service;

import com.utp.timeline.entity.Usuario;
import com.utp.timeline.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Crear nuevo usuario
    public Usuario crearUsuario(Usuario usuario) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Encriptar contraseña
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setFechaRegistro(LocalDateTime.now());

        // Por defecto, nuevo usuario es FREE y USUARIO
        if (usuario.getRol() == null) {
            usuario.setRol(Usuario.Rol.USUARIO);
        }
        if (usuario.getPlan() == null) {
            usuario.setPlan(Usuario.Plan.FREE);
        }

        return usuarioRepository.save(usuario);
    }

    // Registrar usuario con Google
    public Usuario registrarConGoogle(String googleId, String email, String nombre) {
        // Verificar si ya existe usuario con este Google ID
        Optional<Usuario> usuarioExistente = usuarioRepository.findByGoogleId(googleId);
        if (usuarioExistente.isPresent()) {
            return usuarioExistente.get();
        }

        // Verificar si el email ya está registrado (sin Google)
        Optional<Usuario> usuarioPorEmail = usuarioRepository.findByEmail(email);
        if (usuarioPorEmail.isPresent()) {
            // Actualizar usuario existente con Google ID
            Usuario usuario = usuarioPorEmail.get();
            usuario.setGoogleId(googleId);
            return usuarioRepository.save(usuario);
        }

        // Crear nuevo usuario con Google
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setGoogleId(googleId);
        nuevoUsuario.setContrasena(passwordEncoder.encode("google_auth_" + System.currentTimeMillis()));
        nuevoUsuario.setRol(Usuario.Rol.USUARIO);
        nuevoUsuario.setPlan(Usuario.Plan.FREE);
        nuevoUsuario.setFechaRegistro(LocalDateTime.now());

        return usuarioRepository.save(nuevoUsuario);
    }

    // Obtener usuario por ID
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // Obtener usuario por email
    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // Obtener todos los usuarios (solo para admin)
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    // Actualizar información del usuario
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado, Usuario usuarioSolicitante) {
        Usuario usuarioExistente = obtenerUsuarioPorId(id);

        // Verificar permisos (solo admin o el propio usuario puede actualizar)
        if (!usuarioSolicitante.getRol().equals(Usuario.Rol.ADMIN) &&
                !usuarioSolicitante.getId().equals(id)) {
            throw new RuntimeException("No tienes permisos para actualizar este usuario");
        }

        // Actualizar campos permitidos
        if (usuarioActualizado.getNombre() != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }

        // Verificar email único si se está actualizando
        if (usuarioActualizado.getEmail() != null &&
                !usuarioActualizado.getEmail().equals(usuarioExistente.getEmail())) {
            if (!usuarioRepository.isEmailAvailable(usuarioActualizado.getEmail(), id)) {
                throw new RuntimeException("El email ya está en uso");
            }
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
        }

        return usuarioRepository.save(usuarioExistente);
    }

    // Actualizar plan de usuario (solo admin)
    public Usuario actualizarPlanUsuario(Long id, Usuario.Plan plan, Usuario administrador) {
        if (!administrador.getRol().equals(Usuario.Rol.ADMIN)) {
            throw new RuntimeException("Solo los administradores pueden actualizar planes");
        }

        Usuario usuario = obtenerUsuarioPorId(id);
        usuario.setPlan(plan);
        return usuarioRepository.save(usuario);
    }

    // Actualizar rol de usuario (solo admin)
    public Usuario actualizarRolUsuario(Long id, Usuario.Rol rol, Usuario administrador) {
        if (!administrador.getRol().equals(Usuario.Rol.ADMIN)) {
            throw new RuntimeException("Solo los administradores pueden actualizar roles");
        }

        Usuario usuario = obtenerUsuarioPorId(id);
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    // Cambiar contraseña
    public void cambiarContrasena(Long id, String contrasenaActual, String nuevaContrasena, Usuario usuarioSolicitante) {
        Usuario usuario = obtenerUsuarioPorId(id);

        // Verificar permisos
        if (!usuarioSolicitante.getRol().equals(Usuario.Rol.ADMIN) &&
                !usuarioSolicitante.getId().equals(id)) {
            throw new RuntimeException("No tienes permisos para cambiar esta contraseña");
        }

        // Verificar contraseña actual (solo si no es admin cambiando otra contraseña)
        if (!usuarioSolicitante.getRol().equals(Usuario.Rol.ADMIN) ||
                usuarioSolicitante.getId().equals(id)) {
            if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
                throw new RuntimeException("La contraseña actual es incorrecta");
            }
        }

        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
    }

    // Eliminar usuario (solo admin o el propio usuario)
    public void eliminarUsuario(Long id, Usuario usuarioSolicitante) {
        Usuario usuario = obtenerUsuarioPorId(id);

        // Verificar permisos
        if (!usuarioSolicitante.getRol().equals(Usuario.Rol.ADMIN) &&
                !usuarioSolicitante.getId().equals(id)) {
            throw new RuntimeException("No tienes permisos para eliminar este usuario");
        }

        // Prevenir que un admin se elimine a sí mismo
        if (usuarioSolicitante.getId().equals(id) && usuarioSolicitante.getRol().equals(Usuario.Rol.ADMIN)) {
            // Contar cuántos admins hay
            long totalAdmins = usuarioRepository.findByRol(Usuario.Rol.ADMIN).size();
            if (totalAdmins <= 1) {
                throw new RuntimeException("No puedes eliminar el único administrador del sistema");
            }
        }

        usuarioRepository.delete(usuario);
    }

    // Buscar usuarios por nombre
    public List<Usuario> buscarUsuariosPorNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // Obtener estadísticas de usuarios (para admin)
    public Map<String, Object> obtenerEstadisticasUsuarios() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalUsuarios", usuarioRepository.count());
        estadisticas.put("usuariosPremium", usuarioRepository.countPremiumUsers());
        estadisticas.put("usuariosFree", usuarioRepository.countFreeUsers());

        // Usuarios por plan
        List<Object[]> usuariosPorPlan = usuarioRepository.countUsersByPlan();
        Map<String, Long> distribucionPlan = new HashMap<>();
        for (Object[] resultado : usuariosPorPlan) {
            Usuario.Plan plan = (Usuario.Plan) resultado[0];
            Long cantidad = (Long) resultado[1];
            distribucionPlan.put(plan.toString(), cantidad);
        }
        estadisticas.put("distribucionPorPlan", distribucionPlan);

        // Usuarios por rol
        List<Object[]> usuariosPorRol = usuarioRepository.countUsersByRol();
        Map<String, Long> distribucionRol = new HashMap<>();
        for (Object[] resultado : usuariosPorRol) {
            Usuario.Rol rol = (Usuario.Rol) resultado[0];
            Long cantidad = (Long) resultado[1];
            distribucionRol.put(rol.toString(), cantidad);
        }
        estadisticas.put("distribucionPorRol", distribucionRol);

        // Usuarios recientes (últimos 30 días)
        Long usuariosRecientes = usuarioRepository.countUsersSince(LocalDateTime.now().minusDays(30));
        estadisticas.put("usuariosUltimos30Dias", usuariosRecientes);

        return estadisticas;
    }

    // Obtener perfil del usuario actual
    public Usuario obtenerPerfilActual(Usuario usuario) {
        return obtenerUsuarioPorId(usuario.getId());
    }

    // Verificar si un usuario existe y tiene permisos
    public boolean verificarPermisosUsuario(Long usuarioId, Usuario usuarioSolicitante) {
        if (usuarioSolicitante.getRol().equals(Usuario.Rol.ADMIN)) {
            return usuarioRepository.existsById(usuarioId);
        }
        return usuarioSolicitante.getId().equals(usuarioId);
    }

    // Upgrade a premium (simulación de proceso de pago)
    public Usuario upgradeAPremium(Long usuarioId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        usuario.setPlan(Usuario.Plan.PREMIUM);
        return usuarioRepository.save(usuario);
    }

    // Downgrade a free
    public Usuario downgradeAFree(Long usuarioId, Usuario administrador) {
        if (!administrador.getRol().equals(Usuario.Rol.ADMIN)) {
            throw new RuntimeException("Solo los administradores pueden realizar downgrade");
        }

        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        usuario.setPlan(Usuario.Plan.FREE);
        return usuarioRepository.save(usuario);
    }
}