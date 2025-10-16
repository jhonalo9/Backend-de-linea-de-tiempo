package com.utp.timeline.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorito",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_plantilla"}))
public class Favorito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plantilla", nullable = false)
    private Plantilla plantilla;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Constructores
    public Favorito() {}

    public Favorito(Usuario usuario, Plantilla plantilla) {
        this.usuario = usuario;
        this.plantilla = plantilla;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Plantilla getPlantilla() { return plantilla; }
    public void setPlantilla(Plantilla plantilla) { this.plantilla = plantilla; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}