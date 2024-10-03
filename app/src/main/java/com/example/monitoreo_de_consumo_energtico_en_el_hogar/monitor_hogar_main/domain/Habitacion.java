package com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main.domain;

public class Habitacion {

    private String nombre;
    private int consumoEnergetico;
    private String color;
    private int ancho; // Ancho de la habitación
    private int alto;  // Alto de la habitación
    private int posX;  // Posición X en el lienzo
    private int posY;  // Posición Y en el lienzo

    // Constructor vacío necesario para Firebase
    public Habitacion() {
    }

    public Habitacion(String nombre, int consumoEnergetico, String color) {
        this.nombre = nombre;
        this.consumoEnergetico = consumoEnergetico;
        this.color = color;
    }

    public Habitacion(String nombre, int consumoEnergetico, String color, int ancho, int alto, int posX, int posY) {
        this.nombre = nombre;
        this.consumoEnergetico = consumoEnergetico;
        this.color = color;
        this.ancho = ancho;
        this.alto = alto;
        this.posX = posX;
        this.posY = posY;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getConsumoEnergetico() {
        return consumoEnergetico;
    }

    public void setConsumoEnergetico(int consumoEnergetico) {
        this.consumoEnergetico = consumoEnergetico;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getAncho() {
        return ancho;
    }

    public void setAncho(int ancho) {
        this.ancho = ancho;
    }

    public int getAlto() {
        return alto;
    }

    public void setAlto(int alto) {
        this.alto = alto;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }
}




