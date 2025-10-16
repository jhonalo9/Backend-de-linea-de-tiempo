package com.utp.timeline.dto;

import java.time.LocalDateTime;


public class FavoritoResponseDTO {
    private Long id;
    private Long plantillaId;
    private String plantillaNombre;
    private String plantillaDescripcion;
    private LocalDateTime fechaCreacion;

    // Constructores
    public FavoritoResponseDTO() {}

    public FavoritoResponseDTO(Long id, Long plantillaId, String plantillaNombre,
                               String plantillaDescripcion, LocalDateTime fechaCreacion) {
        this.id = id;
        this.plantillaId = plantillaId;
        this.plantillaNombre = plantillaNombre;
        this.plantillaDescripcion = plantillaDescripcion;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlantillaId() { return plantillaId; }
    public void setPlantillaId(Long plantillaId) { this.plantillaId = plantillaId; }

    public String getPlantillaNombre() { return plantillaNombre; }
    public void setPlantillaNombre(String plantillaNombre) { this.plantillaNombre = plantillaNombre; }

    public String getPlantillaDescripcion() { return plantillaDescripcion; }
    public void setPlantillaDescripcion(String plantillaDescripcion) { this.plantillaDescripcion = plantillaDescripcion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}