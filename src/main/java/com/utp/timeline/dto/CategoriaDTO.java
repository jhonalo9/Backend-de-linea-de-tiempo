package com.utp.timeline.dto;

public class CategoriaDTO {
    private Integer idCategoria;
    private String nombre;
    private String descripcion;
    private String estado;

    // Constructores
    public CategoriaDTO() {}

    public CategoriaDTO(Integer idCategoria, String nombre, String descripcion, String estado) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    // Getters y Setters
    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}