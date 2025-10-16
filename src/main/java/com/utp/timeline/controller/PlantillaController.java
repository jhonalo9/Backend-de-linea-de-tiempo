package com.utp.timeline.controller;

import com.utp.timeline.dto.PlantillaEstadisticaDTO;
import com.utp.timeline.entity.Plantilla;
import com.utp.timeline.entity.Usuario;
import com.utp.timeline.repository.PlantillaRepository;
import com.utp.timeline.service.PlantillaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


import com.utp.timeline.dto.PlantillaMapper;
import com.utp.timeline.dto.PlantillaResponseDTO;
import com.utp.timeline.service.CurrentUserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plantillas")
public class PlantillaController {

    private final PlantillaService plantillaService;
    private final CurrentUserService currentUserService;
    private final PlantillaMapper plantillaMapper;
    private final PlantillaRepository plantillaRepository;

    @Autowired
    public PlantillaController(PlantillaService plantillaService,
                               CurrentUserService currentUserService,
                               PlantillaMapper plantillaMapper,PlantillaRepository plantillaRepository) {
        this.plantillaService = plantillaService;
        this.currentUserService = currentUserService;
        this.plantillaMapper = plantillaMapper;
        this.plantillaRepository = plantillaRepository;
    }

    @GetMapping("/publicas")
    public ResponseEntity<List<PlantillaResponseDTO>> obtenerPlantillasPublicas() {
        List<Plantilla> plantillas = plantillaRepository.findPublicasActivas();
        List<PlantillaResponseDTO> plantillasDTO = plantillaMapper.toDtoList(plantillas);
        return ResponseEntity.ok(plantillasDTO);
    }

    @GetMapping
    public ResponseEntity<List<PlantillaResponseDTO>> obtenerPlantillasParaUsuario() {
        Usuario usuario = currentUserService.getCurrentUser();
        List<Plantilla> plantillas = plantillaService.obtenerPlantillasDisponibles(usuario);
        List<PlantillaResponseDTO> plantillasDTO = plantillaMapper.toDtoList(plantillas);
        return ResponseEntity.ok(plantillasDTO);
    }



    // GET /api/plantillas/mis-plantillas - Obtener plantillas del usuario actual
    @GetMapping("/mis-plantillas")
    public ResponseEntity<List<PlantillaResponseDTO>> obtenerMisPlantillas() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Plantilla> plantillas = plantillaService.obtenerMisPlantillas(usuario);
            List<PlantillaResponseDTO> plantillasDTO = plantillaMapper.toDtoList(plantillas);
            return ResponseEntity.ok(plantillasDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // GET /api/plantillas/populares - Plantillas más populares (por favoritos)
    @GetMapping("/populares")
    public ResponseEntity<List<PlantillaEstadisticaDTO>> obtenerPlantillasPopulares() {
        List<PlantillaEstadisticaDTO> plantillas = plantillaService.obtenerPlantillasPopulares();
        return ResponseEntity.ok(plantillas);
    }

    // GET /api/plantillas/mas-usadas - Plantillas más usadas en proyectos
    @GetMapping("/mas-usadas")
    public ResponseEntity<List<PlantillaEstadisticaDTO>> obtenerPlantillasMasUsadas() {
        List<PlantillaEstadisticaDTO> plantillas = plantillaService.obtenerPlantillasMasUsadas();
        return ResponseEntity.ok(plantillas);
    }

    // GET /api/plantillas/recientes - Plantillas más recientes
    @GetMapping("/recientes")
    public ResponseEntity<List<PlantillaResponseDTO>> obtenerPlantillasRecientes() {
        List<Plantilla> plantillas = plantillaService.obtenerPlantillasRecientes();
        List<PlantillaResponseDTO> plantillasDTO = plantillaMapper.toDtoList(plantillas);
        return ResponseEntity.ok(plantillasDTO);
    }

    // GET /api/plantillas/{id} - Obtener plantilla por ID
    @GetMapping("/{id}")
    public ResponseEntity<PlantillaResponseDTO> obtenerPlantilla(@PathVariable Long id) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Plantilla plantilla = plantillaService.obtenerPlantillaPorId(id, usuario);
            PlantillaResponseDTO plantillaDTO = plantillaMapper.toDto(plantilla);
            return ResponseEntity.ok(plantillaDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/plantillas - Crear nueva plantilla
    @PostMapping
    public ResponseEntity<PlantillaResponseDTO> crearPlantilla(@RequestBody Plantilla plantilla) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Plantilla nuevaPlantilla = plantillaService.crearPlantilla(plantilla, usuario);
            PlantillaResponseDTO plantillaDTO = plantillaMapper.toDto(nuevaPlantilla);
            return ResponseEntity.ok(plantillaDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/plantillas/{id} - Actualizar plantilla
    @PutMapping("/{id}")
    public ResponseEntity<PlantillaResponseDTO> actualizarPlantilla(@PathVariable Long id, @RequestBody Plantilla plantilla) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Plantilla plantillaActualizada = plantillaService.actualizarPlantilla(id, plantilla, usuario);
            PlantillaResponseDTO plantillaDTO = plantillaMapper.toDto(plantillaActualizada);
            return ResponseEntity.ok(plantillaDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/plantillas/{id} - Eliminar (archivar) plantilla
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPlantilla(@PathVariable Long id) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            plantillaService.eliminarPlantilla(id, usuario);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Plantilla eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/plantillas/buscar?q=query - Buscar plantillas
    @GetMapping("/buscar")
    public ResponseEntity<List<PlantillaResponseDTO>> buscarPlantillas(@RequestParam String q) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Plantilla> plantillas = plantillaService.buscarPlantillas(q, usuario);
            List<PlantillaResponseDTO> plantillasDTO = plantillaMapper.toDtoList(plantillas);
            return ResponseEntity.ok(plantillasDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // PATCH /api/plantillas/{id}/visibilidad - Cambiar visibilidad
    @PatchMapping("/{id}/visibilidad")
    public ResponseEntity<PlantillaResponseDTO> cambiarVisibilidad(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            boolean esPublica = request.get("esPublica");
            Plantilla plantilla = plantillaService.cambiarVisibilidad(id, esPublica, usuario);
            PlantillaResponseDTO plantillaDTO = plantillaMapper.toDto(plantilla);
            return ResponseEntity.ok(plantillaDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/plantillas/{id}/duplicar - Duplicar plantilla
    @PostMapping("/{id}/duplicar")
    public ResponseEntity<PlantillaResponseDTO> duplicarPlantilla(@PathVariable Long id) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Plantilla plantillaDuplicada = plantillaService.duplicarPlantilla(id, usuario);
            PlantillaResponseDTO plantillaDTO = plantillaMapper.toDto(plantillaDuplicada);
            return ResponseEntity.ok(plantillaDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/plantillas/{id}/permisos - Verificar permisos sobre plantilla
    @GetMapping("/{id}/permisos")
    public ResponseEntity<Map<String, Boolean>> verificarPermisos(@PathVariable Long id) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Plantilla plantilla = plantillaService.obtenerPlantillaPorId(id, usuario);
            boolean puedeEditar = plantilla.getCreadoPor().getId().equals(usuario.getId()) ||
                    usuario.getRol() == Usuario.Rol.ADMIN;

            return ResponseEntity.ok(Map.of(
                    "tieneAcceso", true,
                    "puedeEditar", puedeEditar,
                    "esCreador", plantilla.getCreadoPor().getId().equals(usuario.getId())
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of(
                    "tieneAcceso", false,
                    "puedeEditar", false,
                    "esCreador", false
            ));
        }
    }

    // ===== ENDPOINTS SOLO PARA ADMIN =====

    // GET /api/plantillas/admin/estadisticas - Estadísticas para admin
    @GetMapping("/admin/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasAdmin() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            if (usuario.getRol() != Usuario.Rol.ADMIN) {
                return ResponseEntity.status(403).build();
            }

            Map<String, Object> estadisticas = plantillaService.obtenerEstadisticasAdmin();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // GET /api/plantillas/admin/todas - Obtener todas las plantillas (admin only)
    @GetMapping("/admin/todas")
    public ResponseEntity<?> obtenerTodasPlantillas() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            if (usuario.getRol() != Usuario.Rol.ADMIN) {
                return ResponseEntity.status(403).build();
            }

            List<Plantilla> plantillas = plantillaService.obtenerTodasPlantillas();
            List<PlantillaResponseDTO> plantillasDTO = plantillaMapper.toDtoList(plantillas);
            return ResponseEntity.ok(plantillasDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}