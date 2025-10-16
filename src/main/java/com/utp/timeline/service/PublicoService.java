package com.utp.timeline.service;


import com.utp.timeline.entity.Proyecto;
import com.utp.timeline.entity.Publico;
import com.utp.timeline.entity.Usuario;
import com.utp.timeline.repository.ProyectoRepository;
import com.utp.timeline.repository.PublicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PublicoService {

    private final PublicoRepository publicoRepository;
    private final ProyectoRepository proyectoRepository;
    private final ProyectoService proyectoService;

    @Value("${app.publico.expiracion-dias:30}")
    private int expiracionDias;

    @Autowired
    public PublicoService(PublicoRepository publicoRepository, ProyectoRepository proyectoRepository, ProyectoService proyectoService) {
        this.publicoRepository = publicoRepository;
        this.proyectoRepository = proyectoRepository;
        this.proyectoService = proyectoService;
    }

    // Compartir proyecto públicamente
    public Publico compartirProyecto(Long proyectoId, Usuario usuario, Integer diasExpiracion) {
        // Verificar que el proyecto existe y el usuario tiene permisos
        Proyecto proyecto = proyectoService.obtenerProyectoPorId(proyectoId, usuario);

        // Verificar si ya está compartido
        Optional<Publico> existente = publicoRepository.findByProyecto(proyecto);
        if (existente.isPresent()) {
            // Si ya existe, devolver el existente
            return existente.get();
        }

        // Generar token único
        String token = generarTokenUnico();

        // Calcular fecha de expiración
        LocalDateTime expiraEn = null;
        if (diasExpiracion != null && diasExpiracion > 0) {
            expiraEn = LocalDateTime.now().plusDays(diasExpiracion);
        } else if (expiracionDias > 0) {
            expiraEn = LocalDateTime.now().plusDays(expiracionDias);
        }

        // Crear registro público
        Publico publico = new Publico();
        publico.setProyecto(proyecto);
        publico.setToken(token);
        publico.setExpiraEn(expiraEn);
        publico.setFechaCreacion(LocalDateTime.now());

        return publicoRepository.save(publico);
    }

    // Obtener proyecto público por token
    public Proyecto obtenerProyectoPublico(String token) {
        Publico publico = publicoRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Enlace no encontrado o expirado"));

        // Verificar si ha expirado
        if (publico.getExpiraEn() != null && publico.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Este enlace ha expirado");
        }

        return publico.getProyecto();
    }

    // Dejar de compartir proyecto
    public void dejarDeCompartir(Long proyectoId, Usuario usuario) {
        Proyecto proyecto = proyectoService.obtenerProyectoPorId(proyectoId, usuario);

        Publico publico = publicoRepository.findByProyecto(proyecto)
                .orElseThrow(() -> new RuntimeException("El proyecto no está compartido"));

        publicoRepository.delete(publico);
    }

    // Obtener información de compartido por proyecto
    public Publico obtenerInfoCompartido(Long proyectoId, Usuario usuario) {
        Proyecto proyecto = proyectoService.obtenerProyectoPorId(proyectoId, usuario);

        return publicoRepository.findByProyecto(proyecto)
                .orElseThrow(() -> new RuntimeException("El proyecto no está compartido"));
    }

    // Generar nuevo token para un proyecto ya compartido
    public Publico regenerarToken(Long proyectoId, Usuario usuario) {
        Proyecto proyecto = proyectoService.obtenerProyectoPorId(proyectoId, usuario);

        Publico publico = publicoRepository.findByProyecto(proyecto)
                .orElseThrow(() -> new RuntimeException("El proyecto no está compartido"));

        // Generar nuevo token
        String nuevoToken = generarTokenUnico();
        publico.setToken(nuevoToken);
        publico.setFechaCreacion(LocalDateTime.now());

        return publicoRepository.save(publico);
    }

    // Actualizar fecha de expiración
    public Publico actualizarExpiracion(Long proyectoId, Integer diasExpiracion, Usuario usuario) {
        Proyecto proyecto = proyectoService.obtenerProyectoPorId(proyectoId, usuario);

        Publico publico = publicoRepository.findByProyecto(proyecto)
                .orElseThrow(() -> new RuntimeException("El proyecto no está compartido"));

        LocalDateTime nuevaExpiracion = null;
        if (diasExpiracion != null && diasExpiracion > 0) {
            nuevaExpiracion = LocalDateTime.now().plusDays(diasExpiracion);
        }

        publico.setExpiraEn(nuevaExpiracion);
        return publicoRepository.save(publico);
    }

    // Obtener todos los proyectos compartidos por un usuario
    public List<Publico> obtenerProyectosCompartidos(Usuario usuario) {
        return publicoRepository.findByUsuarioId(usuario.getId());
    }

    // Obtener proyectos compartidos activos de un usuario
    public List<Publico> obtenerProyectosCompartidosActivos(Usuario usuario) {
        List<Publico> todos = publicoRepository.findByUsuarioId(usuario.getId());
        return todos.stream()
                .filter(publico -> publico.getExpiraEn() == null ||
                        publico.getExpiraEn().isAfter(LocalDateTime.now()))
                .toList();
    }

    // Verificar si un proyecto está compartido
    public boolean estaCompartido(Long proyectoId, Usuario usuario) {
        try {
            Proyecto proyecto = proyectoService.obtenerProyectoPorId(proyectoId, usuario);
            return publicoRepository.existsByProyecto(proyecto);
        } catch (Exception e) {
            return false;
        }
    }

    // Obtener estadísticas de compartidos para admin
    public Map<String, Object> obtenerEstadisticasCompartidos() {
        Map<String, Object> estadisticas = new HashMap<>();

        // Total de compartidos activos
        Long totalActivos = publicoRepository.countActiveSharesByUsuario(null);
        estadisticas.put("totalCompartidosActivos", totalActivos);

        // Total de compartidos expirados
        List<Publico> expirados = publicoRepository.findExpiredShares(LocalDateTime.now());
        estadisticas.put("totalCompartidosExpirados", expirados.size());

        // Compartidos que expiran pronto
        List<Publico> expiranPronto = publicoRepository.findSharesExpiringSoon(
                LocalDateTime.now(), LocalDateTime.now().plusHours(24));
        estadisticas.put("expiranEn24Horas", expiranPronto.size());

        // Estadísticas por usuario
        List<Object[]> statsPorUsuario = publicoRepository.countSharesByUser();
        List<Map<String, Object>> porUsuario = statsPorUsuario.stream()
                .map(resultado -> {
                    Usuario usuario = (Usuario) resultado[0];
                    Long count = (Long) resultado[1];
                    Map<String, Object> userStats = new HashMap<>();
                    userStats.put("usuario", Map.of(
                            "id", usuario.getId(),
                            "nombre", usuario.getNombre(),
                            "email", usuario.getEmail()
                    ));
                    userStats.put("totalCompartidos", count);
                    return userStats;
                })
                .toList();
        estadisticas.put("compartidosPorUsuario", porUsuario);

        return estadisticas;
    }

    // Limpiar compartidos expirados (tarea programada)
    public void limpiarCompartidosExpirados() {
        publicoRepository.deleteExpiredShares(LocalDateTime.now());
    }

    // Generar URL pública
    public String generarUrlPublica(String token) {
        return "/public/proyecto/" + token;
    }

    // Generar token único
    private String generarTokenUnico() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        } while (publicoRepository.findByToken(token).isPresent());

        return token;
    }

    // Verificar validez de token
    public boolean esTokenValido(String token) {
        return publicoRepository.isTokenValid(token);
    }

    // Obtener información completa del compartido
    public Map<String, Object> obtenerInfoCompleta(String token) {
        Publico publico = publicoRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Enlace no encontrado"));

        Map<String, Object> info = new HashMap<>();
        info.put("publico", publico);
        info.put("proyecto", publico.getProyecto());
        info.put("url", generarUrlPublica(publico.getToken()));
        info.put("activo", publico.getExpiraEn() == null || publico.getExpiraEn().isAfter(LocalDateTime.now()));
        info.put("diasRestantes", publico.getExpiraEn() != null ?
                java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), publico.getExpiraEn()) : null);

        return info;
    }

    // Obtener contador de proyectos compartidos del usuario
    public Long contarProyectosCompartidos(Usuario usuario) {
        return publicoRepository.countActiveSharesByUsuario(usuario.getId());
    }

    public List<Publico> obtenerTodosCompartidos() {
        return publicoRepository.findAll();
    }
}