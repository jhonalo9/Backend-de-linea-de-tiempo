package com.utp.timeline.dto;

import com.utp.timeline.entity.Publico;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PublicoMapper {

    public PublicoResponseDTO toDto(Publico publico) {
        if (publico == null) {
            return null;
        }

        PublicoResponseDTO dto = new PublicoResponseDTO();
        dto.setId(publico.getId());
        dto.setToken(publico.getToken());
        dto.setExpiraEn(publico.getExpiraEn());
        dto.setFechaCreacion(publico.getFechaCreacion());

        // Información del proyecto
        if (publico.getProyecto() != null) {
            dto.setProyectoId(publico.getProyecto().getId());
            dto.setProyectoTitulo(publico.getProyecto().getTitulo());
        }

        return dto;
    }

    public List<PublicoResponseDTO> toDtoList(List<Publico> publicos) {
        if (publicos == null) {
            return List.of();
        }

        return publicos.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}