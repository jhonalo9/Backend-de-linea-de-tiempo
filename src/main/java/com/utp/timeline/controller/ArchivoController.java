package com.utp.timeline.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/archivos")
@CrossOrigin(origins = {"http://localhost:4200"},
        allowCredentials = "true")
public class ArchivoController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // POST /api/archivos/subir
    @PostMapping("/subir")
    public ResponseEntity<?> subirArchivo(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam("proyectoId") Long proyectoId,
            @RequestParam("tipo") String tipo, // "portadas" o "assets"
            @RequestParam("tipoUsuario") String tipoUsuario, // "users", "admins", "users-premium"
            @RequestParam(value = "esPlantilla", defaultValue = "false") boolean esPlantilla) {

        try {
            // Validar tipo de usuario
            if (!isValidTipoUsuario(tipoUsuario)) {
                return ResponseEntity.badRequest().body("Tipo de usuario inválido");
            }

            // Validar tipo de archivo según si es plantilla o no
            if (!isValidTipoArchivo(tipo, esPlantilla)) {
                return ResponseEntity.badRequest().body("Tipo de archivo inválido para este contexto");
            }

            // Construir ruta según la nueva estructura
            Path directorioDestino = construirRutaDestino(tipoUsuario, usuarioId, proyectoId, tipo, esPlantilla);
            Files.createDirectories(directorioDestino);

            // Generar nombre único
            String nombreOriginal = archivo.getOriginalFilename();
            String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            String nombreArchivo = UUID.randomUUID().toString() + extension;

            Path rutaCompleta = directorioDestino.resolve(nombreArchivo);

            // Guardar archivo
            Files.copy(archivo.getInputStream(), rutaCompleta);

            // Devolver URL relativa según la nueva estructura
            String urlArchivo = construirUrlRelativa(tipoUsuario, usuarioId, proyectoId, tipo, nombreArchivo, esPlantilla);

            return ResponseEntity.ok().body(Map.of(
                    "url", urlArchivo,
                    "nombreOriginal", nombreOriginal,
                    "tamaño", archivo.getSize(),
                    "tipo", tipo,
                    "tipoUsuario", tipoUsuario,
                    "esPlantilla", esPlantilla
            ));

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error al subir archivo: " + e.getMessage());
        }
    }

    // GET /api/archivos/{tipoUsuario}/{usuarioId}/{proyectoId}/{tipo}/{nombreArchivo}
    @GetMapping("/{tipoUsuario}/{usuarioId}/{proyectoId}/{tipo}/{nombreArchivo}")
    public ResponseEntity<byte[]> obtenerArchivo(
            @PathVariable String tipoUsuario,
            @PathVariable Long usuarioId,
            @PathVariable Long proyectoId,
            @PathVariable String tipo,
            @PathVariable String nombreArchivo,
            @RequestParam(value = "esPlantilla", defaultValue = "false") boolean esPlantilla) {

        try {
            // Construir ruta según la nueva estructura
            Path rutaArchivo = construirRutaDestino(tipoUsuario, usuarioId, proyectoId, tipo, esPlantilla)
                    .resolve(nombreArchivo);

            if (!Files.exists(rutaArchivo)) {
                return ResponseEntity.notFound().build();
            }

            byte[] archivoBytes = Files.readAllBytes(rutaArchivo);
            String tipoContenido = Files.probeContentType(rutaArchivo);

            return ResponseEntity.ok()
                    .header("Content-Type", tipoContenido)
                    .body(archivoBytes);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Método para construir la ruta de destino según la nueva estructura
    private Path construirRutaDestino(String tipoUsuario, Long usuarioId, Long proyectoId, String tipo, boolean esPlantilla) {
        Path basePath = Paths.get(uploadDir, tipoUsuario);

        switch (tipoUsuario) {
            case "users":
                // Users solo pueden tener proyectos, no plantillas
                if (esPlantilla) {
                    throw new IllegalArgumentException("Users no pueden crear plantillas");
                }
                return basePath.resolve("user-" + usuarioId)
                        .resolve("proyectos")
                        .resolve("proyecto-" + proyectoId)
                        .resolve(tipo);

            case "admins":
                // Admins solo pueden tener plantillas
                if (!esPlantilla) {
                    throw new IllegalArgumentException("Admins solo pueden manejar plantillas");
                }
                return basePath.resolve("plantillas")
                        .resolve("plantilla-" + proyectoId) // proyectoId se usa como plantillaId aquí
                        .resolve(tipo);

            case "users-premium":
                if (esPlantilla) {
                    // Users premium pueden tener plantillas
                    return basePath.resolve("user-" + usuarioId)
                            .resolve("plantillas")
                            .resolve("plantilla-" + proyectoId) // proyectoId se usa como plantillaId aquí
                            .resolve(tipo);
                } else {
                    // Users premium pueden tener proyectos
                    return basePath.resolve("user-" + usuarioId)
                            .resolve("proyectos")
                            .resolve("proyecto-" + proyectoId)
                            .resolve(tipo);
                }

            default:
                throw new IllegalArgumentException("Tipo de usuario no válido: " + tipoUsuario);
        }
    }

    // Método para construir URL relativa
    private String construirUrlRelativa(String tipoUsuario, Long usuarioId, Long proyectoId, String tipo, String nombreArchivo, boolean esPlantilla) {
        // Para mantener compatibilidad con la URL, agregamos el parámetro esPlantilla como query param
        String baseUrl = String.format("/archivos/%s/%d/%d/%s/%s",
                tipoUsuario, usuarioId, proyectoId, tipo, nombreArchivo);

        if (esPlantilla) {
            return baseUrl + "?esPlantilla=true";
        }
        return baseUrl;
    }

    // Validar tipo de usuario
    private boolean isValidTipoUsuario(String tipoUsuario) {
        return tipoUsuario != null &&
                (tipoUsuario.equals("users") ||
                        tipoUsuario.equals("admins") ||
                        tipoUsuario.equals("users-premium"));
    }

    // Validar tipo de archivo según contexto
    private boolean isValidTipoArchivo(String tipo, boolean esPlantilla) {
        if (esPlantilla) {
            // Las plantillas solo pueden tener portadas
            return tipo != null && tipo.equals("portadas");
        } else {
            // Los proyectos pueden tener portadas y assets
            return tipo != null && (tipo.equals("portadas") || tipo.equals("assets"));
        }
    }

    // DELETE /api/archivos/eliminar
    @DeleteMapping("/eliminar")
    public ResponseEntity<?> eliminarArchivo(
            @RequestParam String tipoUsuario,
            @RequestParam Long usuarioId,
            @RequestParam Long proyectoId,
            @RequestParam String tipo,
            @RequestParam String nombreArchivo,
            @RequestParam(value = "esPlantilla", defaultValue = "false") boolean esPlantilla) {

        try {
            Path rutaArchivo = construirRutaDestino(tipoUsuario, usuarioId, proyectoId, tipo, esPlantilla)
                    .resolve(nombreArchivo);

            if (!Files.exists(rutaArchivo)) {
                return ResponseEntity.notFound().build();
            }

            Files.delete(rutaArchivo);
            return ResponseEntity.ok().body("Archivo eliminado correctamente");

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error al eliminar archivo: " + e.getMessage());
        }
    }

    // GET /api/archivos/listar/{tipoUsuario}/{usuarioId}/{proyectoId}/{tipo}
    @GetMapping("/listar/{tipoUsuario}/{usuarioId}/{proyectoId}/{tipo}")
    public ResponseEntity<?> listarArchivos(
            @PathVariable String tipoUsuario,
            @PathVariable Long usuarioId,
            @PathVariable Long proyectoId,
            @PathVariable String tipo,
            @RequestParam(value = "esPlantilla", defaultValue = "false") boolean esPlantilla) {

        try {
            Path directorio = construirRutaDestino(tipoUsuario, usuarioId, proyectoId, tipo, esPlantilla);

            if (!Files.exists(directorio)) {
                return ResponseEntity.ok().body(Map.of("archivos", java.util.Collections.emptyList()));
            }

            java.util.List<String> archivos = Files.list(directorio)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok().body(Map.of("archivos", archivos));

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error al listar archivos: " + e.getMessage());
        }
    }
}