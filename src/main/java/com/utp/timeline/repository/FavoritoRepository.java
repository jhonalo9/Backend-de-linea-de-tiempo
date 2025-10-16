package com.utp.timeline.repository;

import com.utp.timeline.entity.Favorito;
import com.utp.timeline.entity.Plantilla;
import com.utp.timeline.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    // Buscar favorito por usuario y plantilla
    Optional<Favorito> findByUsuarioAndPlantilla(Usuario usuario, Plantilla plantilla);

    // Verificar si existe favorito
    boolean existsByUsuarioAndPlantilla(Usuario usuario, Plantilla plantilla);

    // Obtener todos los favoritos de un usuario
    List<Favorito> findByUsuario(Usuario usuario);

    // Obtener favoritos de un usuario con paginación
    List<Favorito> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario, org.springframework.data.domain.Pageable pageable);

    // Contar favoritos de un usuario
    Long countByUsuario(Usuario usuario);

    // Contar favoritos de una plantilla
    Long countByPlantilla(Plantilla plantilla);

    // Eliminar favorito por usuario y plantilla
    @Modifying
    @Query("DELETE FROM Favorito f WHERE f.usuario = :usuario AND f.plantilla = :plantilla")
    void deleteByUsuarioAndPlantilla(@Param("usuario") Usuario usuario, @Param("plantilla") Plantilla plantilla);

    // Eliminar todos los favoritos de un usuario
    @Modifying
    @Query("DELETE FROM Favorito f WHERE f.usuario = :usuario")
    void deleteByUsuario(@Param("usuario") Usuario usuario);

    // Obtener favoritos recientes de un usuario (últimos 10)
    @Query("SELECT f FROM Favorito f WHERE f.usuario = :usuario ORDER BY f.fechaCreacion DESC LIMIT 10")
    List<Favorito> findRecentByUsuario(@Param("usuario") Usuario usuario);

    // Verificar si una plantilla está en favoritos del usuario
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorito f WHERE f.usuario = :usuario AND f.plantilla.id = :plantillaId")
    boolean existsByUsuarioIdAndPlantillaId(@Param("usuario") Usuario usuario, @Param("plantillaId") Long plantillaId);

    // Obtener plantillas más favoritas (global)


    // Buscar favoritos por nombre de plantilla
    @Query("SELECT f FROM Favorito f WHERE f.usuario = :usuario AND LOWER(f.plantilla.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Favorito> findByUsuarioAndPlantillaNombreContaining(@Param("usuario") Usuario usuario, @Param("nombre") String nombre);

    @Query("SELECT f FROM Favorito f JOIN FETCH f.plantilla WHERE f.usuario = :usuario")
    List<Favorito> findByUsuarioWithPlantilla(@Param("usuario") Usuario usuario);



    // Contar usuarios distintos que tienen favoritos
    @Query("SELECT COUNT(DISTINCT f.usuario) FROM Favorito f")
    Long countDistinctUsuariosConFavoritos();

    // Contar favoritos por plan de usuario
    @Query("SELECT u.plan, COUNT(f) FROM Favorito f JOIN f.usuario u GROUP BY u.plan")
    List<Object[]> countFavoritosPorPlanUsuario();

    // Contar favoritos de los últimos 30 días
    @Query(value = "SELECT COUNT(*) FROM favorito WHERE fecha_creacion >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)", nativeQuery = true)
    Long countFavoritosUltimos30Dias();

    // Estadísticas de favoritos por usuario (para ranking)
    @Query("SELECT f.usuario, COUNT(f) FROM Favorito f GROUP BY f.usuario ORDER BY COUNT(f) DESC")
    List<Object[]> findFavoriteStatsByUser();

    // Plantillas más favoritas con paginación
    @Query("SELECT f.plantilla, COUNT(f) as total FROM Favorito f GROUP BY f.plantilla ORDER BY total DESC")
    List<Object[]> findMostFavoritedTemplates(org.springframework.data.domain.Pageable pageable);


}