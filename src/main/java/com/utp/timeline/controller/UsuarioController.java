package com.utp.timeline.controller;

import com.utp.timeline.entity.Usuario;
import com.utp.timeline.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
//@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // GET /api/usuarios/perfil - Obtener perfil del usuario actual
    @GetMapping("/perfil")
    public ResponseEntity<Usuario> obtenerPerfil(@AuthenticationPrincipal Usuario usuario) {
        Usuario perfil = usuarioService.obtenerPerfilActual(usuario);
        return ResponseEntity.ok(perfil);
    }

    // PUT /api/usuarios/perfil - Actualizar perfil del usuario actual
    @PutMapping("/perfil")
    public ResponseEntity<Usuario> actualizarPerfil(@RequestBody Usuario usuarioActualizado,
                                                    @AuthenticationPrincipal Usuario usuario) {
        Usuario usuarioActualizadoObj = usuarioService.actualizarUsuario(usuario.getId(), usuarioActualizado, usuario);
        return ResponseEntity.ok(usuarioActualizadoObj);
    }

    // POST /api/usuarios/{id}/cambiar-contrasena - Cambiar contraseña
    @PostMapping("/{id}/cambiar-contrasena")
    public ResponseEntity<?> cambiarContrasena(@PathVariable Long id,
                                               @RequestBody Map<String, String> contrasenas,
                                               @AuthenticationPrincipal Usuario usuario) {
        String contrasenaActual = contrasenas.get("contrasenaActual");
        String nuevaContrasena = contrasenas.get("nuevaContrasena");

        usuarioService.cambiarContrasena(id, contrasenaActual, nuevaContrasena, usuario);
        return ResponseEntity.ok().body(Map.of("message", "Contraseña actualizada correctamente"));
    }

    // ===== ENDPOINTS SOLO PARA ADMIN =====

    // GET /api/usuarios - Obtener todos los usuarios (admin only)
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodosUsuarios(@AuthenticationPrincipal Usuario usuario) {
        if (!usuario.getRol().equals(Usuario.Rol.ADMIN)) {
            return ResponseEntity.status(403).build();
        }
        List<Usuario> usuarios = usuarioService.obtenerTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    // GET /api/usuarios/{id} - Obtener usuario por ID (admin only)
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        if (!usuario.getRol().equals(Usuario.Rol.ADMIN)) {
            return ResponseEntity.status(403).build();
        }
        Usuario usuarioEncontrado = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuarioEncontrado);
    }

    // PUT /api/usuarios/{id}/plan - Actualizar plan de usuario (admin only)
    @PutMapping("/{id}/plan")
    public ResponseEntity<Usuario> actualizarPlan(@PathVariable Long id,
                                                  @RequestBody Map<String, String> planData,
                                                  @AuthenticationPrincipal Usuario usuario) {
        if (!usuario.getRol().equals(Usuario.Rol.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        Usuario.Plan plan = Usuario.Plan.valueOf(planData.get("plan"));
        Usuario usuarioActualizado = usuarioService.actualizarPlanUsuario(id, plan, usuario);
        return ResponseEntity.ok(usuarioActualizado);
    }

    // PUT /api/usuarios/{id}/rol - Actualizar rol de usuario (admin only)
    @PutMapping("/{id}/rol")
    public ResponseEntity<Usuario> actualizarRol(@PathVariable Long id,
                                                 @RequestBody Map<String, String> rolData,
                                                 @AuthenticationPrincipal Usuario usuario) {
        if (!usuario.getRol().equals(Usuario.Rol.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        Usuario.Rol rol = Usuario.Rol.valueOf(rolData.get("rol"));
        Usuario usuarioActualizado = usuarioService.actualizarRolUsuario(id, rol, usuario);
        return ResponseEntity.ok(usuarioActualizado);
    }

    // DELETE /api/usuarios/{id} - Eliminar usuario (admin o propio usuario)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        usuarioService.eliminarUsuario(id, usuario);
        return ResponseEntity.ok().body(Map.of("message", "Usuario eliminado correctamente"));
    }

    // GET /api/usuarios/estadisticas - Estadísticas de usuarios (admin only)
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(@AuthenticationPrincipal Usuario usuario) {
        if (!usuario.getRol().equals(Usuario.Rol.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Object> estadisticas = usuarioService.obtenerEstadisticasUsuarios();
        return ResponseEntity.ok(estadisticas);
    }

    // GET /api/usuarios/buscar?nombre=xxx - Buscar usuarios por nombre (admin only)
    @GetMapping("/buscar")
    public ResponseEntity<List<Usuario>> buscarUsuarios(@RequestParam String nombre,
                                                        @AuthenticationPrincipal Usuario usuario) {
        if (!usuario.getRol().equals(Usuario.Rol.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        List<Usuario> usuarios = usuarioService.buscarUsuariosPorNombre(nombre);
        return ResponseEntity.ok(usuarios);
    }

    // POST /api/usuarios/upgrade-premium - Upgrade a premium (usuario actual)
    //cambiar un usuario de free a primium
    @PostMapping("/upgrade-premium")
    public ResponseEntity<Usuario> upgradeAPremium(@AuthenticationPrincipal Usuario usuario) {
        Usuario usuarioActualizado = usuarioService.upgradeAPremium(usuario.getId());
        return ResponseEntity.ok(usuarioActualizado);
    }

    // POST /api/usuarios/{id}/downgrade-free - Downgrade a free (admin only)
    //sirve para degradar a un usuario Premium a Free.
    @PostMapping("/{id}/downgrade-free")
    public ResponseEntity<Usuario> downgradeAFree(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        if (!usuario.getRol().equals(Usuario.Rol.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        Usuario usuarioActualizado = usuarioService.downgradeAFree(id, usuario);
        return ResponseEntity.ok(usuarioActualizado);
    }

    // GET /api/usuarios/{id}/permisos - Verificar permisos sobre usuario
    @GetMapping("/{id}/permisos")
    public ResponseEntity<Map<String, Boolean>> verificarPermisos(@PathVariable Long id,
                                                                  @AuthenticationPrincipal Usuario usuario) {
        boolean tienePermisos = usuarioService.verificarPermisosUsuario(id, usuario);
        return ResponseEntity.ok(Map.of("tienePermisos", tienePermisos));
    }



}