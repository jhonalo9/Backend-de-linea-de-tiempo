package com.utp.timeline.dto;

import java.time.LocalDateTime;

public class PlantillaResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String data;
    private String estado;
    private Boolean esPublica;
    private Long creadoPorId;
    private String creadoPorNombre;
    private Integer categoriaId;        // Nuevo campo
    private String categoriaNombre;
    private LocalDateTime fechaCreacion;

    // Constructores, getters y setters
    public PlantillaResponseDTO() {}

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Boolean getEsPublica() { return esPublica; }
    public void setEsPublica(Boolean esPublica) { this.esPublica = esPublica; }

    public Long getCreadoPorId() { return creadoPorId; }
    public void setCreadoPorId(Long creadoPorId) { this.creadoPorId = creadoPorId; }

    public String getCreadoPorNombre() { return creadoPorNombre; }
    public void setCreadoPorNombre(String creadoPorNombre) { this.creadoPorNombre = creadoPorNombre; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

}
