package com.utp.timeline.dto;

import com.utp.timeline.entity.Proyecto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProyectoMapper {

    public ProyectoResponseDTO toDto(Proyecto proyecto) {
        ProyectoResponseDTO dto = new ProyectoResponseDTO();
        dto.setId(proyecto.getId());
        dto.setTitulo(proyecto.getTitulo());
        dto.setDescripcion(proyecto.getDescripcion());
        dto.setData(proyecto.getData());
        dto.setFechaCreacion(proyecto.getFechaCreacion());
        dto.setFechaModificacion(proyecto.getFechaModificacion());

        // Información del usuario (sin proxy de Hibernate)
        if (proyecto.getUsuario() != null) {
            dto.setUsuarioId(proyecto.getUsuario().getId());
            dto.setUsuarioNombre(proyecto.getUsuario().getNombre());
        }

        // Información de la plantilla base
        if (proyecto.getPlantillaBase() != null) {
            dto.setPlantillaBaseId(proyecto.getPlantillaBase().getId());
            dto.setPlantillaBaseNombre(proyecto.getPlantillaBase().getNombre());
        }

        return dto;
    }

    public List<ProyectoResponseDTO> toDtoList(List<Proyecto> proyectos) {
        return proyectos.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}