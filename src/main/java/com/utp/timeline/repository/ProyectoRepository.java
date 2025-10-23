package com.utp.timeline.repository;

import com.utp.timeline.entity.Proyecto;
import com.utp.timeline.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    // Buscar todos los proyectos de un usuario
    List<Proyecto> findByUsuario(Usuario usuario);

    // Buscar proyecto por ID y usuario (para verificar permisos)
    Optional<Proyecto> findByIdAndUsuario(Long id, Usuario usuario);

    // Buscar proyectos por título (búsqueda parcial)
    List<Proyecto> findByTituloContainingIgnoreCaseAndUsuario(String titulo, Usuario usuario);

    // Contar proyectos de un usuario
    Long countByUsuario(Usuario usuario);

    // Buscar proyectos por plantilla base
    List<Proyecto> findByPlantillaBaseId(Long plantillaId);

    // Actualizar fecha de modificación
    @Modifying
    @Query("UPDATE Proyecto p SET p.fechaModificacion = CURRENT_TIMESTAMP WHERE p.id = :id")
    void actualizarFechaModificacion(@Param("id") Long id);

    // Proyectos recientes de un usuario (ordenados por fecha de modificación)
    @Query("SELECT p FROM Proyecto p WHERE p.usuario = :usuario ORDER BY p.fechaModificacion DESC")
    List<Proyecto> findRecentByUsuario(@Param("usuario") Usuario usuario, org.springframework.data.domain.Pageable pageable);

    // Verificar si un usuario es propietario del proyecto
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Proyecto p WHERE p.id = :proyectoId AND p.usuario.id = :usuarioId")
    boolean existsByIdAndUsuarioId(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId);

    // ✅ NUEVO: Contar TODOS los proyectos (para admin)
    @Query("SELECT COUNT(p) FROM Proyecto p")
    Long countAllProyectos();


    // ✅ NUEVO: Contar proyectos creados en los últimos N días
    @Query("SELECT COUNT(p) FROM Proyecto p WHERE p.fechaCreacion >= :fecha")
    Long countProyectosDesde(@Param("fecha") LocalDateTime fecha);

    // ✅ NUEVO: Promedio de proyectos por usuario
    @Query("SELECT AVG(proyectosPorUsuario) FROM " +
            "(SELECT COUNT(p) as proyectosPorUsuario FROM Proyecto p GROUP BY p.usuario.id)")
    Double promedioProyectosPorUsuario();


}