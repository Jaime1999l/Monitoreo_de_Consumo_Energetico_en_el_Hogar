package com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main.domain;

public class Pasillo {

    private String nombre;
    private String color; // Hexadecimal color code
    private int ancho; // Ancho del pasillo
    private int alto; // Alto del pasillo
    private boolean esHorizontal; // Indica si el pasillo es horizontal o vertical
    private int posX; // Posición X en el lienzo
    private int posY; // Posición Y en el lienzo

    public Pasillo(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
        this.ancho = 300;
        this.alto = 100;
        this.esHorizontal = true;
        this.posX = 0;
        this.posY = 0;
    }

    public Pasillo(String nombre, int ancho, int alto, String color, boolean esHorizontal) {
        this.nombre = nombre;
        this.ancho = ancho;
        this.alto = alto;
        this.color = color;
        this.esHorizontal = esHorizontal;
        this.posX = 0;
        this.posY = 0;
    }

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
