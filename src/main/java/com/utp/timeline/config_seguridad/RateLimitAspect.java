package com.utp.timeline.config_seguridad;


import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RateLimitAspect {

    
    @Autowired
    private RateLimitService rateLimitService;

    @Around("@annotation(com.utp.timeline.config_seguridad.RateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        // Obtener la anotación
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimited rateLimited = method.getAnnotation(RateLimited.class);

        // Construir la key según el tipo de limitación
        String key = buildKey(rateLimited.type(), method.getName());
        
        // Convertir duración a milisegundos
        long durationMs = rateLimited.unit().toMillis(rateLimited.duration());
        
        // Verificar si está permitido
        if (!rateLimitService.isAllowed(key, rateLimited.maxRequests(), durationMs)) {
            long resetTime = rateLimitService.getResetTime(key, durationMs);
            
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                rateLimited.message() + " Intenta de nuevo en " + resetTime + " segundos."
            );
        }

        // Agregar headers informativos
        addRateLimitHeaders(key, rateLimited.maxRequests(), durationMs);
        
        // Continuar con la ejecución del método
        return joinPoint.proceed();
    }

    /**
     * Construye la key según el tipo de limitación.
     */
    private String buildKey(RateLimited.LimitType type, String methodName) {
        switch (type) {
            case IP:
                return "ratelimit:ip:" + getClientIP() + ":" + methodName;
            case USER:
                return "ratelimit:user:" + getCurrentUserId() + ":" + methodName;
            case GLOBAL:
                return "ratelimit:global:" + methodName;
            default:
                return "ratelimit:unknown:" + methodName;
        }
    }

    /**
     * Obtiene la IP del cliente.
     */
    private String getClientIP() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Obtiene el ID del usuario autenticado.
     */
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "anonymous";
    }

    /**
     * Agrega headers de rate limiting a la respuesta.
     */
    private void addRateLimitHeaders(String key, int maxRequests, long durationMs) {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            
            int remaining = rateLimitService.getRemainingRequests(key, maxRequests, durationMs);
            long resetTime = rateLimitService.getResetTime(key, durationMs);
            
            attributes.getResponse().setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
            attributes.getResponse().setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            attributes.getResponse().setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
        } catch (Exception e) {
            // Si no podemos agregar headers, continuamos sin problema
        }
    }
    
}
