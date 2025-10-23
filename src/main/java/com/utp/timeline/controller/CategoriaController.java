package com.utp.timeline.controller;

import com.utp.timeline.entity.Categoria;
import com.utp.timeline.service.CategoriaService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")

public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<Categoria> listarCategorias() {
        return categoriaService.obtenerTodasCategorias();
    }

    @PostMapping("/guardar")
    public Categoria guardarCategoria(@RequestBody Categoria categoria) {
        return categoriaService.crearCategoria(categoria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCategoriaPorId(@PathVariable Integer id) {
        try {
            Categoria categoria = categoriaService.obtenerCategoriaPorId(id);
            return ResponseEntity.ok(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCategoria(@PathVariable Integer id, @RequestBody Categoria categoria) {
        try {
            Categoria categoriaActualizada = categoriaService.actualizarCategoria(id, categoria);
            return ResponseEntity.ok(categoriaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Integer id) {
        try {
            categoriaService.eliminarCategoria(id);
            return ResponseEntity.ok().body("Categoría eliminada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public List<Categoria> buscarCategorias(@RequestParam String query) {
        return categoriaService.buscarCategorias(query);
    }

    @GetMapping("/publicas")
    public List<Categoria> obtenerCategoriasConPlantillasPublicas() {
        return categoriaService.obtenerCategoriasConPlantillasPublicas();
    }

    @GetMapping("/estadisticas")
    public List<Object[]> obtenerEstadisticasCategorias() {
        return categoriaService.obtenerEstadisticasCategorias();
    }

    @GetMapping("/{id}/existe")
    public ResponseEntity<?> verificarCategoriaActiva(@PathVariable Integer id) {
        boolean existe = categoriaService.existeCategoriaActiva(id);
        return ResponseEntity.ok(existe);
    }



}
