package com.utp.timeline.repository;

import com.utp.timeline.entity.Proyecto;
import com.utp.timeline.entity.Publico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PublicoRepository extends JpaRepository<Publico, Long> {

    // Buscar por token único
    Optional<Publico> findByToken(String token);

    // Buscar por proyecto
    Optional<Publico> findByProyecto(Proyecto proyecto);

    // Buscar por ID de proyecto
    Optional<Publico> findByProyectoId(Long proyectoId);

    // Verificar si existe compartido para un proyecto
    boolean existsByProyecto(Proyecto proyecto);

    // Obtener todos los compartidos activos (no expirados)
    @Query("SELECT p FROM Publico p WHERE p.expiraEn IS NULL OR p.expiraEn > :fechaActual")
    List<Publico> findActiveShares(@Param("fechaActual") LocalDateTime fechaActual);

    // Obtener compartidos expirados
    @Query("SELECT p FROM Publico p WHERE p.expiraEn IS NOT NULL AND p.expiraEn <= :fechaActual")
    List<Publico> findExpiredShares(@Param("fechaActual") LocalDateTime fechaActual);

    // Obtener compartidos de un usuario específico
    @Query("SELECT p FROM Publico p WHERE p.proyecto.usuario.id = :usuarioId")
    List<Publico> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Contar compartidos activos por usuario
    @Query("SELECT COUNT(p) FROM Publico p WHERE p.proyecto.usuario.id = :usuarioId AND (p.expiraEn IS NULL OR p.expiraEn > CURRENT_TIMESTAMP)")
    Long countActiveSharesByUsuario(@Param("usuarioId") Long usuarioId);

    // Eliminar compartidos expirados
    @Modifying
    @Query("DELETE FROM Publico p WHERE p.expiraEn IS NOT NULL AND p.expiraEn <= :fechaActual")
    void deleteExpiredShares(@Param("fechaActual") LocalDateTime fechaActual);

    // Eliminar por proyecto
    @Modifying
    @Query("DELETE FROM Publico p WHERE p.proyecto = :proyecto")
    void deleteByProyecto(@Param("proyecto") Proyecto proyecto);

    // Eliminar por ID de proyecto
    @Modifying
    @Query("DELETE FROM Publico p WHERE p.proyecto.id = :proyectoId")
    void deleteByProyectoId(@Param("proyectoId") Long proyectoId);

    // Actualizar fecha de expiración
    @Modifying
    @Query("UPDATE Publico p SET p.expiraEn = :expiraEn WHERE p.token = :token")
    void actualizarExpiracion(@Param("token") String token, @Param("expiraEn") LocalDateTime expiraEn);

    // Buscar compartidos que expiran pronto (en las próximas 24 horas)
    @Query("SELECT p FROM Publico p WHERE p.expiraEn IS NOT NULL AND p.expiraEn BETWEEN :ahora AND :en24Horas")
    List<Publico> findSharesExpiringSoon(@Param("ahora") LocalDateTime ahora,
                                         @Param("en24Horas") LocalDateTime en24Horas);

    // Estadísticas de compartidos por usuario
    @Query("SELECT p.proyecto.usuario, COUNT(p) FROM Publico p GROUP BY p.proyecto.usuario")
    List<Object[]> countSharesByUser();

    // Verificar si token es válido (activo y no expirado)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Publico p WHERE p.token = :token AND (p.expiraEn IS NULL OR p.expiraEn > CURRENT_TIMESTAMP)")
    boolean isTokenValid(@Param("token") String token);
}