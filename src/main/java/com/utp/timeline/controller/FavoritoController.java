package com.utp.timeline.controller;

import com.utp.timeline.dto.FavoritoMapper;
import com.utp.timeline.dto.FavoritoResponseDTO;
import com.utp.timeline.entity.Favorito;
import com.utp.timeline.entity.Usuario;
import com.utp.timeline.service.CurrentUserService;
import com.utp.timeline.service.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;
    private final CurrentUserService currentUserService;
    private final FavoritoMapper favoritoMapper;

    @Autowired
    public FavoritoController(FavoritoService favoritoService, CurrentUserService currentUserService, FavoritoMapper favoritoMapper) {
        this.favoritoMapper = favoritoMapper;
        this.favoritoService = favoritoService;
        this.currentUserService = currentUserService;
    }

    // GET /api/favoritos - Obtener todos los favoritos del usuario
    @GetMapping
    public ResponseEntity<List<FavoritoResponseDTO>> obtenerFavoritos() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Favorito> favoritos = favoritoService.obtenerFavoritosUsuario(usuario);
            List<FavoritoResponseDTO> favoritosDTO = favoritoMapper.toDtoList(favoritos);
            return ResponseEntity.ok(favoritosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // POST /api/favoritos/toggle - Alternar favorito (RECOMENDADO - método principal)
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleFavorito(@RequestBody Map<String, Long> request) {
        try {
            Long plantillaId = request.get("plantillaId");
            Usuario usuario = currentUserService.getCurrentUser();

            // Verificar si ya es favorito
            boolean eraFavorito = favoritoService.esFavorito(plantillaId, usuario);

            if (eraFavorito) {
                // Si ya era favorito, quitarlo
                favoritoService.eliminarDeFavoritos(plantillaId, usuario);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Plantilla removida de favoritos");
                response.put("esFavorito", false);
                return ResponseEntity.ok(response);
            } else {
                // Si no era favorito, agregarlo
                favoritoService.agregarAFavoritos(plantillaId, usuario);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Plantilla agregada a favoritos");
                response.put("esFavorito", true);
                return ResponseEntity.ok(response);
            }

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // GET /api/favoritos/con-info - Obtener favoritos con información completa
    @GetMapping("/con-info")
    public ResponseEntity<List<Map<String, Object>>> obtenerFavoritosConInfo() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Map<String, Object>> favoritos = favoritoService.obtenerFavoritosConInfo(usuario);
            return ResponseEntity.ok(favoritos);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // GET /api/favoritos/recientes - Obtener favoritos recientes
    @GetMapping("/recientes")
    public ResponseEntity<List<FavoritoResponseDTO>> obtenerFavoritosRecientes() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Favorito> favoritos = favoritoService.obtenerFavoritosRecientes(usuario);
            List<FavoritoResponseDTO> favoritosDTO = favoritoMapper.toDtoList(favoritos);
            return ResponseEntity.ok(favoritosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // GET /api/favoritos/plantilla/{plantillaId} - Verificar si plantilla es favorita
    @GetMapping("/plantilla/{plantillaId}")
    public ResponseEntity<?> verificarFavorito(@PathVariable Long plantillaId) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            boolean esFavorita = favoritoService.esFavorita(plantillaId, usuario);
            Long totalFavoritos = favoritoService.contarFavoritosPlantilla(plantillaId);

            Map<String, Object> response = new HashMap<>();
            response.put("esFavorita", esFavorita);
            response.put("totalFavoritos", totalFavoritos);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // DELETE /api/favoritos/plantilla/{plantillaId} - Eliminar de favoritos
    @DeleteMapping("/plantilla/{plantillaId}")
    public ResponseEntity<?> eliminarFavorito(@PathVariable Long plantillaId) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            favoritoService.eliminarDeFavoritos(plantillaId, usuario);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Plantilla eliminada de favoritos");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // GET /api/favoritos/contador - Obtener contador de favoritos del usuario
    @GetMapping("/contador")
    public ResponseEntity<?> obtenerContadorFavoritos() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Long totalFavoritos = favoritoService.contarFavoritosUsuario(usuario);

            Map<String, Long> response = new HashMap<>();
            response.put("totalFavoritos", totalFavoritos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // GET /api/favoritos/buscar - Buscar en favoritos
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarFavoritos(@RequestParam String q) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            List<Favorito> favoritos = favoritoService.buscarEnFavoritos(q, usuario);
            List<FavoritoResponseDTO> favoritosDTO = favoritoMapper.toDtoList(favoritos);
            return ResponseEntity.ok(favoritosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // DELETE /api/favoritos/limpiar - Limpiar todos los favoritos del usuario
    @DeleteMapping("/limpiar")
    public ResponseEntity<?> limpiarFavoritos() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            favoritoService.limpiarFavoritos(usuario);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Todos los favoritos han sido eliminados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // GET /api/favoritos/{id} - Obtener favorito específico
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerFavorito(@PathVariable Long id) {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            Map<String, Object> favorito = favoritoService.obtenerInfoFavorito(id, usuario);
            return ResponseEntity.ok(favorito);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ===== ENDPOINTS SOLO PARA ADMIN =====

    // GET /api/favoritos/admin/estadisticas - Estadísticas de favoritos
    @GetMapping("/admin/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasFavoritos() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();

            if (usuario.getRol() != Usuario.Rol.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Acceso denegado. Se requiere rol ADMIN");
                return ResponseEntity.status(403).body(error);
            }

            Map<String, Object> estadisticas = favoritoService.obtenerEstadisticasFavoritos();
            return ResponseEntity.ok(estadisticas);

        } catch (RuntimeException e) {
            // Error de autenticación (usuario no encontrado, etc.)
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error de autenticación: " + e.getMessage());
            return ResponseEntity.status(401).body(error);
        } catch (Exception e) {
            // Error general del servidor
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            e.printStackTrace(); // Para debugging
            return ResponseEntity.status(500).body(error);
        }
    }

    // GET /api/favoritos/admin/populares - Plantillas más populares (global)
    @GetMapping("/admin/populares")
    public ResponseEntity<?> obtenerPlantillasPopulares() {
        try {
            Usuario usuario = currentUserService.getCurrentUser();
            if (usuario.getRol() != Usuario.Rol.ADMIN) {
                return ResponseEntity.status(403).build();
            }

            List<Map<String, Object>> populares = favoritoService.obtenerPlantillasMasPopulares();
            return ResponseEntity.ok(populares);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}