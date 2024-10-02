package com.example.monitoreo_de_consumo_energtico_en_el_hogar.domain;

public class Pasillo {

    private String nombre;
    private String color; // Hexadecimal color code
    private int ancho; // Optional: width of the hallway
    private int alto; // Optional: height of the hallway
    private boolean esHorizontal; // Para saber si es horizontal o vertical

    // Constructor para un pasillo por defecto
    public Pasillo(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
        this.ancho = 300; // Ancho por defecto
        this.alto = 100;  // Alto por defecto
        this.esHorizontal = true; // Por defecto horizontal
    }

    // Constructor que acepta todos los parámetros, incluyendo orientación
    public Pasillo(String nombre, int ancho, int alto, String color, boolean esHorizontal) {
        this.nombre = nombre;
        this.ancho = ancho;
        this.alto = alto;
        this.color = color;
        this.esHorizontal = esHorizontal;
    }

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean esHorizontal() {
        return esHorizontal;
    }

    public void setHorizontal(boolean esHorizontal) {
        this.esHorizontal = esHorizontal;
    }
}

