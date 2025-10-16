package com.utp.timeline.repository;

import com.utp.timeline.entity.Plantilla;
import com.utp.timeline.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;

import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface PlantillaRepository extends JpaRepository<Plantilla, Long> {

    // Buscar plantillas por creador
    List<Plantilla> findByCreadoPor(Usuario creadoPor);

    // Buscar plantillas por creador y estado
    List<Plantilla> findByCreadoPorAndEstado(Usuario creadoPor, String estado);

    // Buscar plantillas públicas
    List<Plantilla> findByEsPublicaTrue();

    // Buscar plantillas públicas por estado
    List<Plantilla> findByEsPublicaTrueAndEstado(String estado);

    // Buscar plantillas por estado
    List<Plantilla> findByEstado(String estado);

    // Buscar plantilla por nombre (exacto)
    Optional<Plantilla> findByNombre(String nombre);

    Optional<Plantilla> findByNombreAndCreadoPor(String nombre, Usuario creadoPor);

    // Buscar plantillas por nombre (parcial, case insensitive)
    List<Plantilla> findByNombreContainingIgnoreCase(String nombre);

    // Buscar plantillas disponibles para un usuario
    @Query("SELECT p FROM Plantilla p WHERE p.estado = 'ACTIVA' AND (p.esPublica = true OR p.creadoPor = :usuario)")
    List<Plantilla> findDisponiblesParaUsuario(@Param("usuario") Usuario usuario);

    // Buscar plantillas públicas activas para usuarios free
    @Query("SELECT p FROM Plantilla p WHERE p.estado = 'ACTIVA' AND p.esPublica = true")
    List<Plantilla> findPublicasActivas();

    // Plantillas más populares (con más favoritos)
    @Query("SELECT p, COUNT(f) as favoritosCount FROM Plantilla p LEFT JOIN Favorito f ON p.id = f.plantilla.id " +
            "WHERE p.estado = 'ACTIVA' GROUP BY p.id ORDER BY favoritosCount DESC")
    List<Object[]> findMostPopularTemplates();

    // Plantillas más recientes
    @Query("SELECT p FROM Plantilla p WHERE p.estado = 'ACTIVA' ORDER BY p.fechaCreacion DESC")
    List<Plantilla> findRecentTemplates(org.springframework.data.domain.Pageable pageable);

    // Plantillas destacadas (más usadas en proyectos)
    @Query("SELECT p, COUNT(proy) as usoCount FROM Plantilla p LEFT JOIN Proyecto proy ON p.id = proy.plantillaBase.id " +
            "WHERE p.estado = 'ACTIVA' GROUP BY p.id ORDER BY usoCount DESC")
    List<Object[]> findMostUsedTemplates();

    // Contar plantillas por creador
    Long countByCreadoPor(Usuario creadoPor);

    // Contar plantillas por estado
    Long countByEstado(String estado);

    // Contar plantillas públicas/privadas
    @Query("SELECT COUNT(p) FROM Plantilla p WHERE p.esPublica = :esPublica AND p.estado = 'ACTIVA'")
    Long countByEsPublicaAndEstadoActiva(@Param("esPublica") boolean esPublica);

    // Actualizar estado de plantilla
    @Modifying
    @Query("UPDATE Plantilla p SET p.estado = :estado WHERE p.id = :id")
    void actualizarEstado(@Param("id") Long id, @Param("estado") String estado);

    // Actualizar visibilidad (pública/privada)
    @Modifying
    @Query("UPDATE Plantilla p SET p.esPublica = :esPublica WHERE p.id = :id")
    void actualizarVisibilidad(@Param("id") Long id, @Param("esPublica") boolean esPublica);

    // Buscar plantillas por categoría (ejemplo usando like en descripción)
    @Query("SELECT p FROM Plantilla p WHERE p.estado = 'ACTIVA' AND LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :categoria, '%'))")
    List<Plantilla> findByCategoria(@Param("categoria") String categoria);

    // Verificar si el usuario es propietario de la plantilla
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Plantilla p WHERE p.id = :plantillaId AND p.creadoPor.id = :usuarioId")
    boolean existsByIdAndCreadoPorId(@Param("plantillaId") Long plantillaId, @Param("usuarioId") Long usuarioId);

    // Plantillas creadas por un usuario específico (para admin)
    @Query("SELECT p FROM Plantilla p WHERE p.creadoPor.id = :usuarioId AND p.estado = 'ACTIVA'")
    List<Plantilla> findByCreadoPorId(@Param("usuarioId") Long usuarioId);

    // Estadísticas de uso de plantillas
    @Query("SELECT p.creadoPor.plan, COUNT(p) FROM Plantilla p WHERE p.estado = 'ACTIVA' GROUP BY p.creadoPor.plan")
    List<Object[]> countPlantillasByUserPlan();



    // Para plantillas populares (más favoritas)
    @Query("SELECT p, COUNT(f) as conteo FROM Plantilla p LEFT JOIN Favorito f ON p.id = f.plantilla.id GROUP BY p ORDER BY conteo DESC")
    List<Object[]> findPlantillasPopulares();

    // Para plantillas más usadas
    @Query("SELECT p, COUNT(proy) as conteo FROM Plantilla p LEFT JOIN Proyecto proy ON p.id = proy.plantillaBase.id GROUP BY p ORDER BY conteo DESC")
    List<Object[]> findPlantillasMasUsadas();

    Long countByEsPublicaTrue();
    Long countByEsPublicaFalse();
}