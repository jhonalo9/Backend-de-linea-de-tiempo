package com.utp.timeline.controller;

import com.utp.timeline.dto.PublicoMapper;
import com.utp.timeline.dto.PublicoResponseDTO;
import com.utp.timeline.entity.Proyecto;
import com.utp.timeline.entity.Publico;
import com.utp.timeline.entity.Usuario;
import com.utp.timeline.service.CurrentUserService;
import com.utp.timeline.service.PublicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/publico")
public class PublicoController {

    private final PublicoService publicoService;
    private final CurrentUserService currentUserService;
    private final PublicoMapper publicoMapper;

    @Autowired
    public PublicoController(PublicoService publicoService,
                             CurrentUserService currentUserService,
                             PublicoMapper publicoMapper) {
        this.publicoService = publicoService;
        this.currentUserService = currentUserService;
        this.publicoMapper = publicoMapper;
    }

    // ===== ENDPOINTS PÚBLICOS =====

    @GetMapping("/public/proyecto/{token}")
    public ResponseEntity<Proyecto> obtenerProyectoPublico(@PathVariable String token) {
        Proyecto proyecto = publicoService.obtenerProyectoPublico(token);
        return ResponseEntity.ok(proyecto);
    }

    @GetMapping("/info/{token}")
    public ResponseEntity<Map<String, Object>> obtenerInfoProyectoPublico(@PathVariable String token) {
        Map<String, Object> info = publicoService.obtenerInfoCompleta(token);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/validar/{token}")
    public ResponseEntity<Map<String, Boolean>> validarToken(@PathVariable String token) {
        boolean esValido = publicoService.esTokenValido(token);
        return ResponseEntity.ok(Map.of("valido", esValido));
    }

    // ===== ENDPOINTS PRIVADOS =====

    @PostMapping("/proyecto/{proyectoId}/compartir")
    public ResponseEntity<?> compartirProyecto(@PathVariable Long proyectoId,
                                               @RequestBody(required = false) Map<String, Integer> request) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Integer diasExpiracion = request != null ? request.get("diasExpiracion") : null;

            Publico publico = publicoService.compartirProyecto(proyectoId, usuario, diasExpiracion);
            PublicoResponseDTO publicoDTO = publicoMapper.toDto(publico);

            Map<String, Object> respuesta = Map.of(
                    "publico", publicoDTO,
                    "url", publicoService.generarUrlPublica(publico.getToken()),
                    "mensaje", "Proyecto compartido exitosamente"
            );

            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/proyecto/{proyectoId}/compartir")
    public ResponseEntity<?> dejarDeCompartir(@PathVariable Long proyectoId) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            publicoService.dejarDeCompartir(proyectoId, usuario);
            return ResponseEntity.ok(Map.of("mensaje", "Proyecto dejó de ser compartido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<?> obtenerInfoCompartido(@PathVariable Long proyectoId) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Publico publico = publicoService.obtenerInfoCompartido(proyectoId, usuario);
            PublicoResponseDTO publicoDTO = publicoMapper.toDto(publico);
            return ResponseEntity.ok(publicoDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/proyecto/{proyectoId}/regenerar-token")
    public ResponseEntity<?> regenerarToken(@PathVariable Long proyectoId) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Publico publico = publicoService.regenerarToken(proyectoId, usuario);
            PublicoResponseDTO publicoDTO = publicoMapper.toDto(publico);

            Map<String, Object> respuesta = Map.of(
                    "publico", publicoDTO,
                    "url", publicoService.generarUrlPublica(publico.getToken()),
                    "mensaje", "Token regenerado exitosamente"
            );

            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/proyecto/{proyectoId}/expiracion")
    public ResponseEntity<?> actualizarExpiracion(@PathVariable Long proyectoId,
                                                  @RequestBody Map<String, Integer> request) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Integer diasExpiracion = request.get("diasExpiracion");
            Publico publico = publicoService.actualizarExpiracion(proyectoId, diasExpiracion, usuario);
            PublicoResponseDTO publicoDTO = publicoMapper.toDto(publico);
            return ResponseEntity.ok(publicoDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mis-compartidos")
    public ResponseEntity<?> obtenerMisCompartidos() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Publico> compartidos = publicoService.obtenerProyectosCompartidos(usuario);
            List<PublicoResponseDTO> compartidosDTO = publicoMapper.toDtoList(compartidos);
            return ResponseEntity.ok(compartidosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/mis-compartidos/activos")
    public ResponseEntity<?> obtenerMisCompartidosActivos() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Publico> compartidos = publicoService.obtenerProyectosCompartidosActivos(usuario);
            List<PublicoResponseDTO> compartidosDTO = publicoMapper.toDtoList(compartidos);
            return ResponseEntity.ok(compartidosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/proyecto/{proyectoId}/esta-compartido")
    public ResponseEntity<?> estaCompartido(@PathVariable Long proyectoId) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            boolean compartido = publicoService.estaCompartido(proyectoId, usuario);
            return ResponseEntity.ok(Map.of("compartido", compartido));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/contador")
    public ResponseEntity<?> contarCompartidos() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Long total = publicoService.contarProyectosCompartidos(usuario);
            return ResponseEntity.ok(Map.of("totalCompartidos", total));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // ===== ENDPOINTS ADMIN =====

    @GetMapping("/admin/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            if (usuario.getRol() != Usuario.Rol.ADMIN) {
                return ResponseEntity.status(403).build();
            }

            Map<String, Object> estadisticas = publicoService.obtenerEstadisticasCompartidos();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/admin/limpiar-expirados")
    public ResponseEntity<?> limpiarExpirados() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            if (usuario.getRol() != Usuario.Rol.ADMIN) {
                return ResponseEntity.status(403).build();
            }

            publicoService.limpiarCompartidosExpirados();
            return ResponseEntity.ok(Map.of("mensaje", "Compartidos expirados eliminados"));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/admin/todos")
    public ResponseEntity<?> obtenerTodosCompartidos() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            if (usuario.getRol() != Usuario.Rol.ADMIN) {
                return ResponseEntity.status(403).build();
            }

            List<Publico> todos = publicoService.obtenerTodosCompartidos();
            List<PublicoResponseDTO> todosDTO = publicoMapper.toDtoList(todos);
            return ResponseEntity.ok(todosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}