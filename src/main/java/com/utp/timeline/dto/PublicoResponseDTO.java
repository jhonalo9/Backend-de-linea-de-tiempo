package com.utp.timeline.dto;

import java.time.LocalDateTime;

public class PublicoResponseDTO {
    private Long id;
    private String token;
    private LocalDateTime expiraEn;
    private LocalDateTime fechaCreacion;
    private Long proyectoId;
    private String proyectoTitulo;

    // Constructores, getters y setters
    public PublicoResponseDTO() {}

    public PublicoResponseDTO(Long id, String token, LocalDateTime expiraEn,
                              LocalDateTime fechaCreacion, Long proyectoId, String proyectoTitulo) {
        this.id = id;
        this.token = token;
        this.expiraEn = expiraEn;
        this.fechaCreacion = fechaCreacion;
        this.proyectoId = proyectoId;
        this.proyectoTitulo = proyectoTitulo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiraEn() { return expiraEn; }
    public void setExpiraEn(LocalDateTime expiraEn) { this.expiraEn = expiraEn; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Long getProyectoId() { return proyectoId; }
    public void setProyectoId(Long proyectoId) { this.proyectoId = proyectoId; }

    public String getProyectoTitulo() { return proyectoTitulo; }
    public void setProyectoTitulo(String proyectoTitulo) { this.proyectoTitulo = proyectoTitulo; }
}