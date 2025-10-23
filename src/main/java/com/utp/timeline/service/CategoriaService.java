package com.utp.timeline.service;

import com.utp.timeline.entity.Categoria;
import com.utp.timeline.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {


    private final CategoriaRepository categoriaRepository;

    @Autowired
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Crear nueva categoría
    public Categoria crearCategoria(Categoria categoria) {
        // Verificar que no exista una categoría con el mismo nombre
        if (categoriaRepository.findByNombre(categoria.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        // Establecer valores por defecto
        if (categoria.getEstado() == null) {
            categoria.setEstado("ACTIVA");
        }

        return categoriaRepository.save(categoria);
    }

    // Obtener todas las categorías activas
    public List<Categoria> obtenerTodasCategorias() {
        return categoriaRepository.findByEstado("ACTIVA");
    }

    // Obtener categoría por ID
    public Categoria obtenerCategoriaPorId(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
    }

    // Actualizar categoría
    public Categoria actualizarCategoria(Integer id, Categoria categoriaActualizada) {
        Categoria categoriaExistente = obtenerCategoriaPorId(id);

        // Verificar nombre único si se está cambiando
        if (categoriaActualizada.getNombre() != null &&
                !categoriaActualizada.getNombre().equals(categoriaExistente.getNombre())) {
            if (categoriaRepository.findByNombre(categoriaActualizada.getNombre()).isPresent()) {
                throw new RuntimeException("Ya existe una categoría con ese nombre");
            }
            categoriaExistente.setNombre(categoriaActualizada.getNombre());
        }

        if (categoriaActualizada.getDescripcion() != null) {
            categoriaExistente.setDescripcion(categoriaActualizada.getDescripcion());
        }

        return categoriaRepository.save(categoriaExistente);
    }

    // Eliminar categoría (soft delete)
    public void eliminarCategoria(Integer id) {
        Categoria categoria = obtenerCategoriaPorId(id);

        // Verificar que la categoría no tenga plantillas asociadas
        if (categoria.getPlantillas() != null && !categoria.getPlantillas().isEmpty()) {
            throw new RuntimeException("No se puede eliminar la categoría porque tiene plantillas asociadas");
        }

        categoria.setEstado("INACTIVA");
        categoriaRepository.save(categoria);
    }

    // Buscar categorías por nombre
    public List<Categoria> buscarCategorias(String query) {
        return categoriaRepository.findByNombreContainingIgnoreCase(query);
    }

    // Obtener categorías con plantillas públicas
    public List<Categoria> obtenerCategoriasConPlantillasPublicas() {
        return categoriaRepository.findCategoriasConPlantillasPublicas();
    }

    // Obtener estadísticas de categorías
    public List<Object[]> obtenerEstadisticasCategorias() {
        return categoriaRepository.countPlantillasByCategoria();
    }

    // Verificar si categoría existe y está activa
    public boolean existeCategoriaActiva(Integer id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);
        return categoria.isPresent() && "ACTIVA".equals(categoria.get().getEstado());
    }
}
