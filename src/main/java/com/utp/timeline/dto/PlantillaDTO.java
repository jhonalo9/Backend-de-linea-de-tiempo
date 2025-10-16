package com.utp.timeline.dto;

public class PlantillaDTO {
    private String nombre;
    private String descripcion;
    private String data;
    private Boolean esPublica;

    // Constructores
    public PlantillaDTO() {}

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public Boolean getEsPublica() { return esPublica; }
    public void setEsPublica(Boolean esPublica) { this.esPublica = esPublica; }
}