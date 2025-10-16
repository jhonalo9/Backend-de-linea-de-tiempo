package com.utp.timeline.dto;

public class FavoritoDTO {
    private Long plantillaId;

    // Constructores
    public FavoritoDTO() {}

    public FavoritoDTO(Long plantillaId) {
        this.plantillaId = plantillaId;
    }

    // Getters y Setters
    public Long getPlantillaId() { return plantillaId; }
    public void setPlantillaId(Long plantillaId) { this.plantillaId = plantillaId; }
}