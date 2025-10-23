package com.utp.timeline.dto;


import com.utp.timeline.entity.Plantilla;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlantillaMapper {

    public PlantillaResponseDTO toDto(Plantilla plantilla) {
        PlantillaResponseDTO dto = new PlantillaResponseDTO();
        dto.setId(plantilla.getId());
        dto.setNombre(plantilla.getNombre());
        dto.setDescripcion(plantilla.getDescripcion());
        dto.setData(plantilla.getData());
        dto.setEstado(plantilla.getEstado());
        dto.setEsPublica(plantilla.getEsPublica());
        dto.setFechaCreacion(plantilla.getFechaCreacion());

        // Información del creador
        if (plantilla.getCreadoPor() != null) {
            dto.setCreadoPorId(plantilla.getCreadoPor().getId());
            dto.setCreadoPorNombre(plantilla.getCreadoPor().getNombre());
        }

        // Información de la categoría - AGREGAR ESTO
        if (plantilla.getCategoria() != null) {
            dto.setCategoriaId(plantilla.getCategoria().getIdCategoria());
            dto.setCategoriaNombre(plantilla.getCategoria().getNombre());
        }

        return dto;
    }

    public List<PlantillaResponseDTO> toDtoList(List<Plantilla> plantillas) {
        return plantillas.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }



    public PlantillaEstadisticaDTO toEstadisticaDto(Plantilla plantilla, Long conteo, String tipoEstadistica) {
        PlantillaEstadisticaDTO dto = new PlantillaEstadisticaDTO();
        dto.setId(plantilla.getId());
        dto.setNombre(plantilla.getNombre());
        dto.setDescripcion(plantilla.getDescripcion());
        dto.setConteo(conteo);
        dto.setTipoEstadistica(tipoEstadistica);

        if (plantilla.getCreadoPor() != null) {
            dto.setCreadoPorId(plantilla.getCreadoPor().getId());
            dto.setCreadoPorNombre(plantilla.getCreadoPor().getNombre());
        }

        // Información de la categoría para estadísticas - AGREGAR ESTO
        if (plantilla.getCategoria() != null) {
            dto.setCategoriaId(plantilla.getCategoria().getIdCategoria());
            dto.setCategoriaNombre(plantilla.getCategoria().getNombre());
        }

        return dto;
    }
}