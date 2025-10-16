package com.utp.timeline.dto;


import com.utp.timeline.entity.Favorito;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FavoritoMapper {

    public FavoritoResponseDTO toDto(Favorito favorito) {
        if (favorito == null) {
            return null;
        }

        FavoritoResponseDTO dto = new FavoritoResponseDTO();
        dto.setId(favorito.getId());
        dto.setFechaCreacion(favorito.getFechaCreacion());

        // Información de la plantilla
        if (favorito.getPlantilla() != null) {
            dto.setPlantillaId(favorito.getPlantilla().getId());
            dto.setPlantillaNombre(favorito.getPlantilla().getNombre());
            dto.setPlantillaDescripcion(favorito.getPlantilla().getDescripcion());
        }

        return dto;
    }

    public List<FavoritoResponseDTO> toDtoList(List<Favorito> favoritos) {
        if (favoritos == null) {
            return List.of();
        }

        return favoritos.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}