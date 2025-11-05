package com.utp.timeline.config_seguridad;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface RateLimited {
     /**
     * Número máximo de peticiones permitidas
     */
    int maxRequests() default 10;
    
    /**
     * Duración de la ventana de tiempo
     */
    int duration() default 1;
    
    /**
     * Unidad de tiempo
     */
    TimeUnit unit() default TimeUnit.MINUTES;
    
    /**
     * Mensaje de error personalizado
     */
    String message() default "Demasiadas peticiones. Intenta más tarde.";
    
    /**
     * Tipo de limitación:
     * - IP: Por dirección IP
     * - USER: Por usuario autenticado
     * - GLOBAL: Global para todos
     */
    LimitType type() default LimitType.IP;
    
    enum LimitType {
        IP, USER, GLOBAL
    }
    
}
