package com.utp.timeline.dto;

public class ProyectoDTO {
    private String titulo;
    private String descripcion;
    private String data;
    private Long plantillaBaseId;

    // Constructores
    public ProyectoDTO() {}

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public Long getPlantillaBaseId() { return plantillaBaseId; }
    public void setPlantillaBaseId(Long plantillaBaseId) { this.plantillaBaseId = plantillaBaseId; }
}
