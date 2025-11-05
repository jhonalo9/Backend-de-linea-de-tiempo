package com.utp.timeline.config_seguridad;


import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
    // Estructura: key -> {timestamp, count}
    private final ConcurrentHashMap<String, RequestCounter> requests = new ConcurrentHashMap<>();

    /**
     * Verifica si una petición está permitida.
     * 
     * @param key Identificador único (IP, userId, etc.)
     * @param maxRequests Máximo de peticiones permitidas
     * @param durationMs Ventana de tiempo en milisegundos
     * @return true si la petición está permitida
     */
    public boolean isAllowed(String key, int maxRequests, long durationMs) {
        long now = System.currentTimeMillis();
        
        requests.compute(key, (k, counter) -> {
            // Primera petición o ventana expirada
            if (counter == null || now - counter.windowStart > durationMs) {
                return new RequestCounter(now, 1);
            }
            
            // Incrementar contador en la ventana actual
            counter.count++;
            return counter;
        });
        
        RequestCounter counter = requests.get(key);
        
        // Limpiar entradas antiguas periódicamente
        cleanupExpiredEntries(now, durationMs);
        
        return counter.count <= maxRequests;
    }

    /**
     * Obtiene el número de peticiones restantes.
     */
    public int getRemainingRequests(String key, int maxRequests, long durationMs) {
        long now = System.currentTimeMillis();
        RequestCounter counter = requests.get(key);
        
        if (counter == null || now - counter.windowStart > durationMs) {
            return maxRequests;
        }
        
        return Math.max(0, maxRequests - counter.count);
    }

    /**
     * Obtiene el tiempo restante hasta el reset (en segundos).
     */
    public long getResetTime(String key, long durationMs) {
        RequestCounter counter = requests.get(key);
        
        if (counter == null) {
            return 0;
        }
        
        long elapsed = System.currentTimeMillis() - counter.windowStart;
        return Math.max(0, (durationMs - elapsed) / 1000);
    }

    /**
     * Limpia entradas expiradas para evitar memory leaks.
     */
    private void cleanupExpiredEntries(long now, long durationMs) {
        if (requests.size() > 10000) { // Solo limpiar si hay muchas entradas
            requests.entrySet().removeIf(entry -> 
                now - entry.getValue().windowStart > durationMs * 2
            );
        }
    }

    /**
     * Resetea el contador para una key específica (útil para testing).
     */
    public void reset(String key) {
        requests.remove(key);
    }

    // Clase interna para almacenar contador
    private static class RequestCounter {
        long windowStart;
        int count;

        RequestCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
    
}
