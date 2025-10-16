package com.utp.timeline.service;

import com.utp.timeline.dto.PlantillaEstadisticaDTO;
import com.utp.timeline.dto.PlantillaMapper;
import com.utp.timeline.entity.Plantilla;
import com.utp.timeline.entity.Usuario;
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
public class PlantillaService {

    private final PlantillaRepository plantillaRepository;
    private final PlantillaMapper plantillaMapper;

    @Autowired
    public PlantillaService(PlantillaRepository plantillaRepository,PlantillaMapper plantillaMapper) {
        this.plantillaRepository = plantillaRepository;
        this.plantillaMapper = plantillaMapper;
    }

    // Crear nueva plantilla
    public Plantilla crearPlantilla(Plantilla plantilla, Usuario creador) {
        // Verificar permisos: solo admin y premium pueden crear plantillas
        if (creador.getPlan() == Usuario.Plan.FREE && creador.getRol() != Usuario.Rol.ADMIN) {
            throw new RuntimeException("Los usuarios free no pueden crear plantillas");
        }

        // Verificar que no exista una plantilla con el mismo nombre del mismo usuario
        if (plantillaRepository.findByNombreAndCreadoPor(plantilla.getNombre(), creador).isPresent()) {
            throw new RuntimeException("Ya tienes una plantilla con ese nombre");
        }

        plantilla.setCreadoPor(creador);
        plantilla.setFechaCreacion(LocalDateTime.now());
        plantilla.setEstado("ACTIVA");

        // Por defecto, las plantillas de admin son públicas, las de usuarios privadas
        if (creador.getRol() == Usuario.Rol.ADMIN) {
            plantilla.setEsPublica(true);
        } else {
            plantilla.setEsPublica(plantilla.getEsPublica() != null ? plantilla.getEsPublica() : false);
        }

        return plantillaRepository.save(plantilla);
    }

    // Obtener plantillas disponibles para un usuario
    public List<Plantilla> obtenerPlantillasDisponibles(Usuario usuario) {
        if (usuario.getRol() == Usuario.Rol.ADMIN) {
            // Admin ve todas las plantillas activas
            return plantillaRepository.findByEstado("ACTIVA");
        } else if (usuario.getPlan() == Usuario.Plan.PREMIUM) {
            // Premium ve plantillas públicas + sus propias plantillas
            return plantillaRepository.findDisponiblesParaUsuario(usuario);
        } else {
            // Free solo ve plantillas públicas activas
            return plantillaRepository.findPublicasActivas();
        }
    }

    // Obtener plantilla por ID con verificación de permisos
    public Plantilla obtenerPlantillaPorId(Long id, Usuario usuario) {
        Plantilla plantilla = plantillaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada"));

        // Verificar permisos de acceso
        if (!tieneAccesoAPlantilla(plantilla, usuario)) {
            throw new RuntimeException("No tienes permisos para acceder a esta plantilla");
        }

        return plantilla;
    }

    // Actualizar plantilla
    public Plantilla actualizarPlantilla(Long id, Plantilla plantillaActualizada, Usuario usuario) {
        Plantilla plantillaExistente = obtenerPlantillaPorId(id, usuario);

        // Verificar que el usuario es el creador o admin
        if (!plantillaExistente.getCreadoPor().getId().equals(usuario.getId()) &&
                usuario.getRol() != Usuario.Rol.ADMIN) {
            throw new RuntimeException("Solo el creador o un administrador pueden editar esta plantilla");
        }

        // Actualizar campos permitidos
        if (plantillaActualizada.getNombre() != null) {
            // Verificar nombre único para el mismo usuario
            if (!plantillaActualizada.getNombre().equals(plantillaExistente.getNombre())) {
                if (plantillaRepository.findByNombreAndCreadoPor(plantillaActualizada.getNombre(), usuario).isPresent()) {
                    throw new RuntimeException("Ya tienes una plantilla con ese nombre");
                }
            }
            plantillaExistente.setNombre(plantillaActualizada.getNombre());
        }

        if (plantillaActualizada.getDescripcion() != null) {
            plantillaExistente.setDescripcion(plantillaActualizada.getDescripcion());
        }

        if (plantillaActualizada.getData() != null) {
            plantillaExistente.setData(plantillaActualizada.getData());
        }

        // Solo premium y admin pueden cambiar visibilidad
        if (plantillaActualizada.getEsPublica() != null &&
                (usuario.getPlan() == Usuario.Plan.PREMIUM || usuario.getRol() == Usuario.Rol.ADMIN)) {
            plantillaExistente.setEsPublica(plantillaActualizada.getEsPublica());
        }

        return plantillaRepository.save(plantillaExistente);
    }

    // Eliminar plantilla (archivar en lugar de eliminar)
    public void eliminarPlantilla(Long id, Usuario usuario) {
        Plantilla plantilla = obtenerPlantillaPorId(id, usuario);

        // Verificar que el usuario es el creador o admin
        if (!plantilla.getCreadoPor().getId().equals(usuario.getId()) &&
                usuario.getRol() != Usuario.Rol.ADMIN) {
            throw new RuntimeException("Solo el creador o un administrador pueden eliminar esta plantilla");
        }

        // Archivar en lugar de eliminar (soft delete)
        plantilla.setEstado("ARCHIVADA");
        plantillaRepository.save(plantilla);
    }

    // Obtener plantillas del usuario actual
    public List<Plantilla> obtenerMisPlantillas(Usuario usuario) {
        if (usuario.getPlan() == Usuario.Plan.FREE && usuario.getRol() != Usuario.Rol.ADMIN) {
            throw new RuntimeException("Los usuarios free no tienen plantillas propias");
        }
        return plantillaRepository.findByCreadoPorAndEstado(usuario, "ACTIVA");
    }



    public List<PlantillaEstadisticaDTO> obtenerPlantillasPopulares() {
        try {
            List<Object[]> resultados = plantillaRepository.findPlantillasPopulares();

            return resultados.stream()
                    .map(result -> {
                        Plantilla plantilla = (Plantilla) result[0];
                        Long conteoFavoritos = (Long) result[1];

                        // Verificar adicionalmente que la plantilla sea activa y pública
                        if (!"ACTIVA".equals(plantilla.getEstado()) || !Boolean.TRUE.equals(plantilla.getEsPublica())) {
                            return null;
                        }

                        return plantillaMapper.toEstadisticaDto(plantilla, conteoFavoritos, "FAVORITOS");
                    })
                    .filter(Objects::nonNull) // Filtrar nulos
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    // Obtener plantillas más usadas

    public List<PlantillaEstadisticaDTO> obtenerPlantillasMasUsadas() {
        try {
            List<Object[]> resultados = plantillaRepository.findPlantillasMasUsadas();

            return resultados.stream()
                    .map(result -> {
                        Plantilla plantilla = (Plantilla) result[0];
                        Long conteoUsos = (Long) result[1];

                        // Verificar adicionalmente que la plantilla sea activa y pública
                        if (!"ACTIVA".equals(plantilla.getEstado()) || !Boolean.TRUE.equals(plantilla.getEsPublica())) {
                            return null;
                        }

                        return plantillaMapper.toEstadisticaDto(plantilla, conteoUsos, "USOS");
                    })
                    .filter(Objects::nonNull) // Filtrar nulos
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }




    // Obtener plantillas recientes
    public List<Plantilla> obtenerPlantillasRecientes() {
        try {
            return plantillaRepository.findRecentTemplates(PageRequest.of(0, 10));
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Buscar plantillas por nombre
    public List<Plantilla> buscarPlantillas(String query, Usuario usuario) {
        try {
            List<Plantilla> plantillas = plantillaRepository.findByNombreContainingIgnoreCase(query);

            // Filtrar según permisos del usuario y estado ACTIVO
            return plantillas.stream()
                    .filter(plantilla -> "ACTIVA".equals(plantilla.getEstado()))
                    .filter(plantilla -> tieneAccesoAPlantilla(plantilla, usuario))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Cambiar visibilidad de plantilla
    public Plantilla cambiarVisibilidad(Long id, boolean esPublica, Usuario usuario) {
        Plantilla plantilla = obtenerPlantillaPorId(id, usuario);

        // Verificar permisos
        if (usuario.getPlan() == Usuario.Plan.FREE && usuario.getRol() != Usuario.Rol.ADMIN) {
            throw new RuntimeException("Los usuarios free no pueden cambiar la visibilidad de plantillas");
        }

        if (!plantilla.getCreadoPor().getId().equals(usuario.getId()) &&
                usuario.getRol() != Usuario.Rol.ADMIN) {
            throw new RuntimeException("Solo el creador puede cambiar la visibilidad");
        }

        plantilla.setEsPublica(esPublica);
        return plantillaRepository.save(plantilla);
    }

    // Duplicar plantilla
    public Plantilla duplicarPlantilla(Long id, Usuario usuario) {
        Plantilla plantillaOriginal = obtenerPlantillaPorId(id, usuario);

        // Verificar que el usuario puede crear plantillas
        if (usuario.getPlan() == Usuario.Plan.FREE && usuario.getRol() != Usuario.Rol.ADMIN) {
            throw new RuntimeException("Los usuarios free no pueden duplicar plantillas");
        }

        Plantilla plantillaDuplicada = new Plantilla();
        plantillaDuplicada.setNombre(plantillaOriginal.getNombre() + " (Copia)");
        plantillaDuplicada.setDescripcion(plantillaOriginal.getDescripcion());
        plantillaDuplicada.setData(plantillaOriginal.getData());
        plantillaDuplicada.setCreadoPor(usuario);
        plantillaDuplicada.setEsPublica(false); // Por defecto, duplicados son privados
        plantillaDuplicada.setEstado("ACTIVA");
        plantillaDuplicada.setFechaCreacion(LocalDateTime.now());

        return plantillaRepository.save(plantillaDuplicada);
    }

    // Verificar si usuario tiene acceso a plantilla
    public boolean tieneAccesoAPlantilla(Plantilla plantilla, Usuario usuario) {
        if (plantilla.getEstado().equals("ARCHIVADA")) {
            return false;
        }

        if (usuario.getRol() == Usuario.Rol.ADMIN) {
            return true;
        }

        if (plantilla.getEsPublica()) {
            return true;
        }

        return plantilla.getCreadoPor().getId().equals(usuario.getId());
    }

    // Obtener estadísticas para admin
    public Map<String, Object> obtenerEstadisticasAdmin() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalPlantillas", plantillaRepository.countByEstado("ACTIVA"));
        estadisticas.put("plantillasPublicas", plantillaRepository.countByEsPublicaAndEstadoActiva(true));
        estadisticas.put("plantillasPrivadas", plantillaRepository.countByEsPublicaAndEstadoActiva(false));
        estadisticas.put("plantillasArchivadas", plantillaRepository.countByEstado("ARCHIVADA"));

        // Plantillas por tipo de usuario creador
        List<Object[]> porPlanUsuario = plantillaRepository.countPlantillasByUserPlan();
        Map<String, Long> plantillasPorPlan = new HashMap<>();
        for (Object[] resultado : porPlanUsuario) {
            Usuario.Plan plan = (Usuario.Plan) resultado[0];
            Long cantidad = (Long) resultado[1];
            plantillasPorPlan.put(plan.toString(), cantidad);
        }
        estadisticas.put("plantillasPorPlanUsuario", plantillasPorPlan);

        // Plantillas populares
        estadisticas.put("plantillasPopulares", obtenerPlantillasPopulares());

        // Plantillas más usadas
        estadisticas.put("plantillasMasUsadas", obtenerPlantillasMasUsadas());

        return estadisticas;
    }

    // Método auxiliar para procesar resultados de estadísticas
    private List<Map<String, Object>> procesarResultadosEstadisticas(List<Object[]> resultados, String tipoMetrica) {
        List<Map<String, Object>> plantillasConMetrica = new ArrayList<>();

        for (Object[] resultado : resultados) {
            Plantilla plantilla = (Plantilla) resultado[0];
            Long metrica = (Long) resultado[1];

            Map<String, Object> item = new HashMap<>();
            item.put("plantilla", plantilla);
            item.put(tipoMetrica, metrica);
            plantillasConMetrica.add(item);
        }

        return plantillasConMetrica;
    }

    public List<Plantilla> obtenerTodasPlantillas() {
        return plantillaRepository.findAll();
    }

    // Método adicional para buscar por nombre y creador
    /*private Optional<Plantilla> findByNombreAndCreadoPor(String nombre, Usuario creadoPor) {
        return plantillaRepository.findByCreadoPor(creadoPor).stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }*/
}