package com.utp.timeline.service;

import com.utp.timeline.entity.Plantilla;
import com.utp.timeline.entity.Proyecto;
import com.utp.timeline.entity.Usuario;
import com.utp.timeline.repository.PlantillaRepository;
import com.utp.timeline.repository.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final PlantillaRepository plantillaRepository;

    @Autowired
    public ProyectoService(ProyectoRepository proyectoRepository, PlantillaRepository plantillaRepository) {
        this.proyectoRepository = proyectoRepository;
        this.plantillaRepository = plantillaRepository;
    }

    // Crear nuevo proyecto
    public Proyecto crearProyecto(Proyecto proyecto, Usuario usuario) {
        // Validar plantilla base si se proporciona
        if (proyecto.getPlantillaBase() != null && proyecto.getPlantillaBase().getId() != null) {
            Plantilla plantilla = plantillaRepository.findById(proyecto.getPlantillaBase().getId())
                    .orElseThrow(() -> new RuntimeException("Plantilla no encontrada"));

            // Verificar que la plantilla esté disponible para el usuario
            if (!plantilla.getEsPublica() && !plantilla.getCreadoPor().getId().equals(usuario.getId())) {
                throw new RuntimeException("No tienes acceso a esta plantilla");
            }

            proyecto.setPlantillaBase(plantilla);
        }

        proyecto.setUsuario(usuario);
        proyecto.setFechaCreacion(LocalDateTime.now());
        proyecto.setFechaModificacion(LocalDateTime.now());

        return proyectoRepository.save(proyecto);
    }

    // Obtener todos los proyectos del usuario
    public List<Proyecto> obtenerProyectosUsuario(Usuario usuario) {
        return proyectoRepository.findByUsuario(usuario);
    }

    // Obtener proyecto por ID (con verificación de permisos)
    public Proyecto obtenerProyectoPorId(Long id, Usuario usuario) {
        return proyectoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado o no tienes permisos"));
    }

    // Actualizar proyecto
    public Proyecto actualizarProyecto(Long id, Proyecto proyectoActualizado, Usuario usuario) {
        Proyecto proyectoExistente = obtenerProyectoPorId(id, usuario);

        // Actualizar campos permitidos
        proyectoExistente.setTitulo(proyectoActualizado.getTitulo());
        proyectoExistente.setDescripcion(proyectoActualizado.getDescripcion());
        proyectoExistente.setData(proyectoActualizado.getData());
        proyectoExistente.setFechaModificacion(LocalDateTime.now());

        // Actualizar plantilla base si se proporciona
        if (proyectoActualizado.getPlantillaBase() != null) {
            Plantilla plantilla = plantillaRepository.findById(proyectoActualizado.getPlantillaBase().getId())
                    .orElseThrow(() -> new RuntimeException("Plantilla no encontrada"));
            proyectoExistente.setPlantillaBase(plantilla);
        }

        return proyectoRepository.save(proyectoExistente);
    }

    // Eliminar proyecto
    public void eliminarProyecto(Long id, Usuario usuario) {
        Proyecto proyecto = obtenerProyectoPorId(id, usuario);
        proyectoRepository.delete(proyecto);
    }

    // Buscar proyectos por título
    public List<Proyecto> buscarProyectosPorTitulo(String titulo, Usuario usuario) {
        return proyectoRepository.findByTituloContainingIgnoreCaseAndUsuario(titulo, usuario);
    }

    // Obtener proyectos recientes (últimos 5)
    public List<Proyecto> obtenerProyectosRecientes(Usuario usuario) {
        return proyectoRepository.findRecentByUsuario(usuario, PageRequest.of(0, 5));
    }

    // Contar proyectos del usuario
    public Long contarProyectosUsuario(Usuario usuario) {
        return proyectoRepository.countByUsuario(usuario);
    }

    // Duplicar proyecto
    public Proyecto duplicarProyecto(Long id, Usuario usuario) {
        Proyecto proyectoOriginal = obtenerProyectoPorId(id, usuario);

        Proyecto proyectoDuplicado = new Proyecto();
        proyectoDuplicado.setUsuario(usuario);
        proyectoDuplicado.setTitulo(proyectoOriginal.getTitulo() + " (Copia)");
        proyectoDuplicado.setDescripcion(proyectoOriginal.getDescripcion());
        proyectoDuplicado.setData(proyectoOriginal.getData());
        proyectoDuplicado.setPlantillaBase(proyectoOriginal.getPlantillaBase());
        proyectoDuplicado.setFechaCreacion(LocalDateTime.now());
        proyectoDuplicado.setFechaModificacion(LocalDateTime.now());

        return proyectoRepository.save(proyectoDuplicado);
    }

    // Verificar permisos sobre proyecto
    public boolean tienePermisosProyecto(Long proyectoId, Usuario usuario) {
        if (usuario.getRol() == Usuario.Rol.ADMIN) {
            return proyectoRepository.existsById(proyectoId);
        }
        return proyectoRepository.existsByIdAndUsuarioId(proyectoId, usuario.getId());
    }

    // Actualizar solo los datos del proyecto (para guardado automático)
    public void actualizarDatosProyecto(Long id, String data, Usuario usuario) {
        Proyecto proyecto = obtenerProyectoPorId(id, usuario);
        proyecto.setData(data);
        proyecto.setFechaModificacion(LocalDateTime.now());
        proyectoRepository.save(proyecto);
    }


    //  Estadísticas para administradores
    public Map<String, Object> obtenerEstadisticasAdmin() {
        Long totalProyectos = proyectoRepository.count(); // Usando el método de JpaRepository

        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        Long proyectosRecientes = proyectoRepository.countProyectosDesde(hace30Dias);

        Double promedioPorUsuario = proyectoRepository.promedioProyectosPorUsuario();
        if (promedioPorUsuario == null) {
            promedioPorUsuario = 0.0;
        }

        // Calcular crecimiento (proyectos últimos 30 días)
        Long crecimiento = proyectosRecientes;

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalProyectos", totalProyectos);
        estadisticas.put("proyectosRecientes", proyectosRecientes);
        estadisticas.put("proyectosPorUsuario", Math.round(promedioPorUsuario * 10.0) / 10.0);
        estadisticas.put("crecimientoProyectos", crecimiento);

        return estadisticas;
    }
}