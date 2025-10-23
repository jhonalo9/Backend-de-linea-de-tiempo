package com.utp.timeline.repository;

import com.utp.timeline.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findByNombre(String nombre);
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);
    List<Categoria> findByEstado(String estado);
    // Contar plantillas por categoría
    @Query("SELECT c, COUNT(p) FROM Categoria c LEFT JOIN c.plantillas p WHERE c.estado = 'ACTIVA' GROUP BY c")
    List<Object[]> countPlantillasByCategoria();

    // Obtener categorías con plantillas públicas
    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.plantillas p WHERE p.estado = 'ACTIVA' AND p.esPublica = true")
    List<Categoria> findCategoriasConPlantillasPublicas();


}
