package com.utp.timeline.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "publico")
public class Publico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_publico")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_proyecto", nullable = false)
    private Proyecto proyecto;

    @Column(name = "token", nullable = false, unique = true, length = 120)
    private String token;

    @Column(name = "expira_en")
    private LocalDateTime expiraEn;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Constructores
    public Publico() {}

    public Publico(Proyecto proyecto, String token) {
        this.proyecto = proyecto;
        this.token = token;
        this.fechaCreacion = LocalDateTime.now();
        this.expiraEn = LocalDateTime.now().plusDays(30); // Expira en 30 días
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Proyecto getProyecto() { return proyecto; }
    public void setProyecto(Proyecto proyecto) { this.proyecto = proyecto; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiraEn() { return expiraEn; }
    public void setExpiraEn(LocalDateTime expiraEn) { this.expiraEn = expiraEn; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}