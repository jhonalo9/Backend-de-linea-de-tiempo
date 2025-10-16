package com.utp.timeline.repository;
import com.utp.timeline.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;

import org.springframework.data.repository.query.Param;

import java.util.List;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);

    // Verificar si existe un usuario por email
    boolean existsByEmail(String email);

    // Buscar usuario por Google ID
    Optional<Usuario> findByGoogleId(String googleId);

    // Buscar usuarios por rol
    List<Usuario> findByRol(Usuario.Rol rol);

    // Buscar usuarios por plan
    List<Usuario> findByPlan(Usuario.Plan plan);

    // Buscar usuarios por rol y plan
    List<Usuario> findByRolAndPlan(Usuario.Rol rol, Usuario.Plan plan);

    // Contar usuarios premium
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.plan = 'PREMIUM'")
    Long countPremiumUsers();

    // Contar usuarios free
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.plan = 'FREE'")
    Long countFreeUsers();

    // Contar usuarios activos por plan
    @Query("SELECT u.plan, COUNT(u) FROM Usuario u GROUP BY u.plan")
    List<Object[]> countUsersByPlan();

    // Contar usuarios por rol
    @Query("SELECT u.rol, COUNT(u) FROM Usuario u GROUP BY u.rol")
    List<Object[]> countUsersByRol();

    // Buscar usuarios por nombre (búsqueda parcial)
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);

    // Actualizar plan de usuario
    @Modifying
    @Query("UPDATE Usuario u SET u.plan = :plan WHERE u.id = :id")
    void actualizarPlanUsuario(@Param("id") Long id, @Param("plan") Usuario.Plan plan);

    // Actualizar rol de usuario
    @Modifying
    @Query("UPDATE Usuario u SET u.rol = :rol WHERE u.id = :id")
    void actualizarRolUsuario(@Param("id") Long id, @Param("rol") Usuario.Rol rol);

    // Actualizar contraseña
    @Modifying
    @Query("UPDATE Usuario u SET u.contrasena = :contrasena WHERE u.id = :id")
    void actualizarContrasena(@Param("id") Long id, @Param("contrasena") String contrasena);

    // Obtener usuarios recientes (ordenados por fecha de registro)
    @Query("SELECT u FROM Usuario u ORDER BY u.fechaRegistro DESC")
    List<Usuario> findRecentUsers(org.springframework.data.domain.Pageable pageable);

    // Verificar si email está disponible (excluyendo un usuario específico)
    @Query("SELECT CASE WHEN COUNT(u) = 0 THEN true ELSE false END FROM Usuario u WHERE u.email = :email AND u.id != :excludeId")
    boolean isEmailAvailable(@Param("email") String email, @Param("excludeId") Long excludeId);

    // Estadísticas de registro por período (ejemplo: últimos 30 días)
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.fechaRegistro >= :fechaInicio")
    Long countUsersSince(@Param("fechaInicio") java.time.LocalDateTime fechaInicio);
}