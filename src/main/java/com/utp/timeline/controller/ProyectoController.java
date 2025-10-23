package com.utp.timeline.controller;

import com.utp.timeline.dto.ProyectoDTO;
import com.utp.timeline.dto.ProyectoMapper;
import com.utp.timeline.dto.ProyectoResponseDTO;
import com.utp.timeline.entity.Plantilla;
import com.utp.timeline.entity.Proyecto;
import com.utp.timeline.entity.Usuario;
import com.utp.timeline.service.CurrentUserService;
import com.utp.timeline.service.ProyectoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    private final ProyectoService proyectoService;
    private final CurrentUserService currentUserService;
    private final ProyectoMapper proyectoMapper;

    @Autowired
    public ProyectoController(ProyectoService proyectoService,
                              CurrentUserService currentUserService,
                              ProyectoMapper proyectoMapper) {
        this.proyectoService = proyectoService;
        this.currentUserService = currentUserService;
        this.proyectoMapper = proyectoMapper;
    }

    // GET /api/proyectos - Obtener todos los proyectos del usuario
    @GetMapping
    public ResponseEntity<List<ProyectoResponseDTO>> obtenerProyectosUsuario() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Proyecto> proyectos = proyectoService.obtenerProyectosUsuario(usuario);
            List<ProyectoResponseDTO> proyectosDTO = proyectoMapper.toDtoList(proyectos);
            return ResponseEntity.ok(proyectosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // GET /api/proyectos/recientes - Obtener proyectos recientes
    @GetMapping("/recientes")
    public ResponseEntity<List<ProyectoResponseDTO>> obtenerProyectosRecientes() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Proyecto> proyectos = proyectoService.obtenerProyectosRecientes(usuario);
            List<ProyectoResponseDTO> proyectosDTO = proyectoMapper.toDtoList(proyectos);
            return ResponseEntity.ok(proyectosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // GET /api/proyectos/{id} - Obtener proyecto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponseDTO> obtenerProyecto(@PathVariable Long id) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Proyecto proyecto = proyectoService.obtenerProyectoPorId(id, usuario);
            ProyectoResponseDTO responseDTO = proyectoMapper.toDto(proyecto);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/proyectos - Crear nuevo proyecto
    @PostMapping
    public ResponseEntity<?> crearProyecto(@RequestBody ProyectoDTO proyectoDTO) {
        try {
            Usuario usuarioActual = currentUserService.getCurrentUser();
            System.out.println("Usuario autenticado ID: " + usuarioActual.getId());

            Proyecto proyecto = convertirDtoAEntidad(proyectoDTO);
            Proyecto nuevoProyecto = proyectoService.crearProyecto(proyecto, usuarioActual);

            ProyectoResponseDTO responseDTO = proyectoMapper.toDto(nuevoProyecto);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear proyecto: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // PUT /api/proyectos/{id} - Actualizar proyecto
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProyecto(@PathVariable Long id, @RequestBody ProyectoDTO proyectoDTO) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Proyecto proyectoActualizado = convertirDtoAEntidad(proyectoDTO);
            Proyecto proyecto = proyectoService.actualizarProyecto(id, proyectoActualizado, usuario);
            ProyectoResponseDTO responseDTO = proyectoMapper.toDto(proyecto);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH /api/proyectos/{id}/data - Actualizar solo los datos
    @PatchMapping("/{id}/data")
    public ResponseEntity<?> actualizarDatosProyecto(@PathVariable Long id, @RequestBody Map<String, String> data) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            proyectoService.actualizarDatosProyecto(id, data.get("data"), usuario);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Datos actualizados correctamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/proyectos/{id} - Eliminar proyecto
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProyecto(@PathVariable Long id) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            proyectoService.eliminarProyecto(id, usuario);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Proyecto eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/proyectos/buscar?titulo=xxx - Buscar proyectos por título
    @GetMapping("/buscar")
    public ResponseEntity<List<ProyectoResponseDTO>> buscarProyectos(@RequestParam String titulo) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Proyecto> proyectos = proyectoService.buscarProyectosPorTitulo(titulo, usuario);
            List<ProyectoResponseDTO> proyectosDTO = proyectoMapper.toDtoList(proyectos);
            return ResponseEntity.ok(proyectosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // POST /api/proyectos/{id}/duplicar - Duplicar proyecto
    @PostMapping("/{id}/duplicar")
    public ResponseEntity<?> duplicarProyecto(@PathVariable Long id) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Proyecto proyectoDuplicado = proyectoService.duplicarProyecto(id, usuario);
            ProyectoResponseDTO responseDTO = proyectoMapper.toDto(proyectoDuplicado);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasAdmin() {
        try {
            Map<String, Object> estadisticas = proyectoService.obtenerEstadisticasAdmin();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener estadísticas: " + e.getMessage());
        }
    }

    // GET /api/proyectos/estadisticas - Estadísticas del usuario
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Long totalProyectos = proyectoService.contarProyectosUsuario(usuario);
            List<Proyecto> proyectosRecientes = proyectoService.obtenerProyectosRecientes(usuario);

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalProyectos", totalProyectos);
            estadisticas.put("proyectosRecientes", proyectosRecientes.size());
            estadisticas.put("ultimosProyectos", proyectoMapper.toDtoList(proyectosRecientes));

            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // GET /api/proyectos/{id}/permisos - Verificar permisos
    @GetMapping("/{id}/permisos")
    public ResponseEntity<?> verificarPermisos(@PathVariable Long id) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            boolean tienePermisos = proyectoService.tienePermisosProyecto(id, usuario);
            Map<String, Boolean> respuesta = new HashMap<>();
            respuesta.put("tienePermisos", tienePermisos);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    private Proyecto convertirDtoAEntidad(ProyectoDTO dto) {
        Proyecto proyecto = new Proyecto();
        proyecto.setTitulo(dto.getTitulo());
        proyecto.setDescripcion(dto.getDescripcion());
        proyecto.setData(dto.getData());

        if (dto.getPlantillaBaseId() != null) {
            Plantilla plantilla = new Plantilla();
            plantilla.setId(dto.getPlantillaBaseId());
            proyecto.setPlantillaBase(plantilla);
        }

        return proyecto;
    }
}