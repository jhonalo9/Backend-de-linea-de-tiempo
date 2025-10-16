package com.utp.timeline.dto;

public class PlantillaEstadisticaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Long creadoPorId;
    private String creadoPorNombre;
    private Long conteo;
    private String tipoEstadistica;

    // Constructores, getters y setters
    public PlantillaEstadisticaDTO() {}

    public PlantillaEstadisticaDTO(Long id, String nombre, String descripcion,
                                   Long creadoPorId, String creadoPorNombre,
                                   Long conteo, String tipoEstadistica) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creadoPorId = creadoPorId;
        this.creadoPorNombre = creadoPorNombre;
        this.conteo = conteo;
        this.tipoEstadistica = tipoEstadistica;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getCreadoPorId() { return creadoPorId; }
    public void setCreadoPorId(Long creadoPorId) { this.creadoPorId = creadoPorId; }

    public String getCreadoPorNombre() { return creadoPorNombre; }
    public void setCreadoPorNombre(String creadoPorNombre) { this.creadoPorNombre = creadoPorNombre; }

    public Long getConteo() { return conteo; }
    public void setConteo(Long conteo) { this.conteo = conteo; }

    public String getTipoEstadistica() { return tipoEstadistica; }
    public void setTipoEstadistica(String tipoEstadistica) { this.tipoEstadistica = tipoEstadistica; }
}