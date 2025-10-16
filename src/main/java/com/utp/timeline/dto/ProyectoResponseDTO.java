package com.utp.timeline.dto;

import java.time.LocalDateTime;

public class ProyectoResponseDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String data;
    private Long usuarioId; // Solo el ID, no la entidad completa
    private String usuarioNombre;
    private Long plantillaBaseId;
    private String plantillaBaseNombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    // Constructores, getters y setters
    public ProyectoResponseDTO() {}

    public ProyectoResponseDTO(Long id, String titulo, String descripcion, String data,
                               Long usuarioId, String usuarioNombre, Long plantillaBaseId,
                               String plantillaBaseNombre, LocalDateTime fechaCreacion,
                               LocalDateTime fechaModificacion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.data = data;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.plantillaBaseId = plantillaBaseId;
        this.plantillaBaseNombre = plantillaBaseNombre;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public Long getPlantillaBaseId() { return plantillaBaseId; }
    public void setPlantillaBaseId(Long plantillaBaseId) { this.plantillaBaseId = plantillaBaseId; }

    public String getPlantillaBaseNombre() { return plantillaBaseNombre; }
    public void setPlantillaBaseNombre(String plantillaBaseNombre) { this.plantillaBaseNombre = plantillaBaseNombre; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
