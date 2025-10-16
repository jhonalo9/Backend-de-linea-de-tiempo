package com.utp.timeline.service;


import com.utp.timeline.entity.Favorito;
import com.utp.timeline.entity.Plantilla;
import com.utp.timeline.entity.Usuario;
import com.utp.timeline.repository.FavoritoRepository;
import com.utp.timeline.repository.PlantillaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final PlantillaRepository plantillaRepository;
    private final PlantillaService plantillaService;

    @Autowired
    public FavoritoService(FavoritoRepository favoritoRepository, PlantillaRepository plantillaRepository, PlantillaService plantillaService) {
        this.favoritoRepository = favoritoRepository;
        this.plantillaRepository = plantillaRepository;
        this.plantillaService = plantillaService;
    }

    // Agregar plantilla a favoritos
    public Favorito agregarAFavoritos(Long plantillaId, Usuario usuario) {
        // Verificar que la plantilla existe y es accesible
        Plantilla plantilla = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada"));

        if (!plantilla.getEstado().equals("ACTIVA")) {
            throw new RuntimeException("La plantilla no está disponible");
        }

        // Verificar si ya es favorito - de manera más eficiente
        Optional<Favorito> favoritoExistente = favoritoRepository.findByUsuarioAndPlantilla(usuario, plantilla);

        if (favoritoExistente.isPresent()) {
            // En lugar de lanzar excepción, puedes:
            // Opción 1: Devolver el favorito existente
            return favoritoExistente.get();

            // Opción 2: Lanzar excepción más específica
            // throw new RuntimeException("La plantilla ya está en tus favoritos");
        }

        // Crear nuevo favorito
        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setPlantilla(plantilla);
        favorito.setFechaCreacion(LocalDateTime.now());

        return favoritoRepository.save(favorito);
    }


    public boolean agregarAFavoritosBoolean(Long plantillaId, Usuario usuario) {
        try {
            Plantilla plantilla = plantillaRepository.findById(plantillaId)
                    .orElseThrow(() -> new RuntimeException("Plantilla no encontrada"));

            if (!plantilla.getEstado().equals("ACTIVA")) {
                throw new RuntimeException("La plantilla no está disponible");
            }

            Optional<Favorito> favoritoExistente = favoritoRepository.findByUsuarioAndPlantilla(usuario, plantilla);

            if (favoritoExistente.isPresent()) {
                return false; // Ya existía
            }

            Favorito favorito = new Favorito();
            favorito.setUsuario(usuario);
            favorito.setPlantilla(plantilla);
            favorito.setFechaCreacion(LocalDateTime.now());

            favoritoRepository.save(favorito);
            return true; // Se agregó nuevo

        } catch (Exception e) {
            throw new RuntimeException("Error al agregar a favoritos: " + e.getMessage());
        }
    }


    public boolean esFavorito(Long plantillaId, Usuario usuario) {
        Plantilla plantilla = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada"));

        return favoritoRepository.findByUsuarioAndPlantilla(usuario, plantilla).isPresent();
    }


    // Eliminar plantilla de favoritos
    public void eliminarDeFavoritos(Long plantillaId, Usuario usuario) {
        Plantilla plantilla = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada"));

        Favorito favorito = favoritoRepository.findByUsuarioAndPlantilla(usuario, plantilla)
                .orElseThrow(() -> new RuntimeException("La plantilla no está en tus favoritos"));

        favoritoRepository.delete(favorito);
    }

    // Alternar favorito (agregar/eliminar)
    public Map<String, Object> toggleFavorito(Long plantillaId, Usuario usuario) {
        Plantilla plantilla = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada"));

        Optional<Favorito> favoritoExistente = favoritoRepository.findByUsuarioAndPlantilla(usuario, plantilla);

        Map<String, Object> resultado = new HashMap<>();

        if (favoritoExistente.isPresent()) {
            // Eliminar de favoritos
            favoritoRepository.delete(favoritoExistente.get());
            resultado.put("accion", "eliminado");
            resultado.put("esFavorito", false);
        } else {
            // Agregar a favoritos
            if (!plantillaService.tieneAccesoAPlantilla(plantilla, usuario)) {
                throw new RuntimeException("No tienes acceso a esta plantilla");
            }

            Favorito nuevoFavorito = new Favorito();
            nuevoFavorito.setUsuario(usuario);
            nuevoFavorito.setPlantilla(plantilla);
            nuevoFavorito.setFechaCreacion(LocalDateTime.now());
            favoritoRepository.save(nuevoFavorito);

            resultado.put("accion", "agregado");
            resultado.put("esFavorito", true);
        }

        resultado.put("totalFavoritos", favoritoRepository.countByPlantilla(plantilla));
        return resultado;
    }

    // Obtener favoritos del usuario
    public List<Favorito> obtenerFavoritosUsuario(Usuario usuario) {
        return favoritoRepository.findByUsuario(usuario);
    }

    // Obtener favoritos del usuario con información completa de plantilla
    public List<Map<String, Object>> obtenerFavoritosConInfo(Usuario usuario) {
        List<Favorito> favoritos = favoritoRepository.findByUsuarioWithPlantilla(usuario);

        return favoritos.stream().map(favorito -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", favorito.getId());
            info.put("fechaCreacion", favorito.getFechaCreacion());

            if (favorito.getPlantilla() != null) {
                Map<String, Object> plantillaInfo = new HashMap<>();
                plantillaInfo.put("id", favorito.getPlantilla().getId());
                plantillaInfo.put("nombre", favorito.getPlantilla().getNombre());
                plantillaInfo.put("descripcion", favorito.getPlantilla().getDescripcion());
                plantillaInfo.put("esPublica", favorito.getPlantilla().getEsPublica());
                info.put("plantilla", plantillaInfo);
            }

            return info;
        }).collect(Collectors.toList());
    }

    // Verificar si una plantilla es favorita del usuario
    public boolean esFavorita(Long plantillaId, Usuario usuario) {
        return favoritoRepository.existsByUsuarioIdAndPlantillaId(usuario, plantillaId);
    }

    // Obtener contador de favoritos para una plantilla
    public Long contarFavoritosPlantilla(Long plantillaId) {
        Plantilla plantilla = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada"));
        return favoritoRepository.countByPlantilla(plantilla);
    }

    // Obtener contador de favoritos del usuario
    public Long contarFavoritosUsuario(Usuario usuario) {
        return favoritoRepository.countByUsuario(usuario);
    }

    // Obtener favoritos recientes del usuario
    public List<Favorito> obtenerFavoritosRecientes(Usuario usuario) {
        return favoritoRepository.findRecentByUsuario(usuario);
    }

    // Buscar en favoritos del usuario
    public List<Favorito> buscarEnFavoritos(String query, Usuario usuario) {
        return favoritoRepository.findByUsuarioAndPlantillaNombreContaining(usuario, query);
    }

    // Obtener plantillas más populares (global)
    public List<Map<String, Object>> obtenerPlantillasMasPopulares() {
        try {
            List<Object[]> resultados = favoritoRepository.findMostFavoritedTemplates(
                    org.springframework.data.domain.PageRequest.of(0, 10)
            );

            return resultados.stream().map(result -> {
                Plantilla plantilla = (Plantilla) result[0];
                Long totalFavoritos = (Long) result[1];

                Map<String, Object> plantillaInfo = new HashMap<>();
                plantillaInfo.put("plantillaId", plantilla.getId());
                plantillaInfo.put("nombre", plantilla.getNombre());
                plantillaInfo.put("descripcion", plantilla.getDescripcion());
                plantillaInfo.put("esPublica", plantilla.getEsPublica());
                plantillaInfo.put("totalFavoritos", totalFavoritos);

                if (plantilla.getCreadoPor() != null) {
                    plantillaInfo.put("creadoPor", plantilla.getCreadoPor().getNombre());
                }

                return plantillaInfo;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            return List.of();
        }
    }

    // Obtener estadísticas de favoritos para admin
    public Map<String, Object> obtenerEstadisticasFavoritos() {
        Map<String, Object> estadisticas = new HashMap<>();

        try {
            // Total de favoritos en el sistema
            Long totalFavoritos = favoritoRepository.count();
            estadisticas.put("totalFavoritos", totalFavoritos);

            // Total de usuarios que tienen favoritos
            Long totalUsuariosConFavoritos = favoritoRepository.countDistinctUsuariosConFavoritos();
            estadisticas.put("totalUsuariosConFavoritos", totalUsuariosConFavoritos);

            // Promedio de favoritos por usuario
            Double promedioFavoritosPorUsuario = totalUsuariosConFavoritos > 0 ?
                    (double) totalFavoritos / totalUsuariosConFavoritos : 0.0;
            estadisticas.put("promedioFavoritosPorUsuario", Math.round(promedioFavoritosPorUsuario * 100.0) / 100.0);

            // Favoritos por plan de usuario - CORREGIDO
            List<Object[]> favoritosPorPlan = favoritoRepository.countFavoritosPorPlanUsuario();
            Map<String, Long> favoritosPorPlanMap = new HashMap<>();
            for (Object[] resultado : favoritosPorPlan) {
                Usuario.Plan plan = (Usuario.Plan) resultado[0]; // Obtener el Enum
                Long cantidad = (Long) resultado[1];
                favoritosPorPlanMap.put(plan.name(), cantidad); // Usar .name() para convertir a String
            }
            estadisticas.put("favoritosPorPlan", favoritosPorPlanMap);

            // Favoritos de los últimos 30 días
            Long favoritosUltimos30Dias = favoritoRepository.countFavoritosUltimos30Dias();
            estadisticas.put("favoritosUltimos30Dias", favoritosUltimos30Dias);

            // Plantillas más populares (top 10)
            List<Map<String, Object>> plantillasPopulares = obtenerPlantillasMasPopulares();
            estadisticas.put("plantillasMasPopulares", plantillasPopulares);

            // Usuarios más activos en favoritos (top 5)
            List<Map<String, Object>> usuariosMasActivos = obtenerUsuariosMasActivosEnFavoritos();
            estadisticas.put("usuariosMasActivos", usuariosMasActivos);

        } catch (Exception e) {
            estadisticas.put("error", "Error al calcular estadísticas: " + e.getMessage());
            // Poner valores por defecto para evitar errores en el frontend
            estadisticas.put("totalFavoritos", 0L);
            estadisticas.put("totalUsuariosConFavoritos", 0L);
            estadisticas.put("promedioFavoritosPorUsuario", 0.0);
            estadisticas.put("favoritosUltimos30Dias", 0L);
            estadisticas.put("favoritosPorPlan", new HashMap<>());
            estadisticas.put("plantillasMasPopulares", List.of());
            estadisticas.put("usuariosMasActivos", List.of());
        }

        return estadisticas;
    }

    private List<Map<String, Object>> obtenerUsuariosMasActivosEnFavoritos() {
        try {
            List<Object[]> resultados = favoritoRepository.findFavoriteStatsByUser();

            return resultados.stream()
                    .limit(5) // Top 5
                    .map(result -> {
                        Usuario usuario = (Usuario) result[0];
                        Long totalFavoritos = (Long) result[1];

                        Map<String, Object> usuarioInfo = new HashMap<>();
                        usuarioInfo.put("usuarioId", usuario.getId());
                        usuarioInfo.put("nombre", usuario.getNombre());
                        usuarioInfo.put("email", usuario.getEmail());
                        usuarioInfo.put("plan", usuario.getPlan().name()); // CORREGIDO: usar .name()
                        usuarioInfo.put("totalFavoritos", totalFavoritos);

                        return usuarioInfo;
                    }).collect(Collectors.toList());

        } catch (Exception e) {
            return List.of();
        }
    }



    // Limpiar favoritos del usuario (eliminar todos)
    public void limpiarFavoritos(Usuario usuario) {
        favoritoRepository.deleteByUsuario(usuario);
    }

    // Obtener información completa de favorito
    public Map<String, Object> obtenerInfoFavorito(Long favoritoId, Usuario usuario) {
        Favorito favorito = favoritoRepository.findById(favoritoId)
                .orElseThrow(() -> new RuntimeException("Favorito no encontrado"));

        // Verificar que el favorito pertenece al usuario
        if (!favorito.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para ver este favorito");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("favorito", favorito);
        info.put("plantilla", favorito.getPlantilla());
        info.put("usuario", Map.of(
                "id", favorito.getUsuario().getId(),
                "nombre", favorito.getUsuario().getNombre()
        ));

        return info;
    }
}