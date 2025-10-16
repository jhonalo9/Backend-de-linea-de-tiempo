package com.utp.timeline.dto;


public class CambioContrasenaDTO {
    private String contrasenaActual;
    private String nuevaContrasena;

    // Getters y Setters
    public String getContrasenaActual() { return contrasenaActual; }
    public void setContrasenaActual(String contrasenaActual) { this.contrasenaActual = contrasenaActual; }

    public String getNuevaContrasena() { return nuevaContrasena; }
    public void setNuevaContrasena(String nuevaContrasena) { this.nuevaContrasena = nuevaContrasena; }
}