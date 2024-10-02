 package com.example.monitoreo_de_consumo_energtico_en_el_hogar.domain;

public class Habitacion {

    private String nombre;
    private int consumoEnergetico;
    private String color;

    public Habitacion() {
        // Constructor vacío necesario para Firebase
    }

    public Habitacion(String nombre, int consumoEnergetico, String color) {
        this.nombre = nombre;
        this.consumoEnergetico = consumoEnergetico;
        this.color = color;
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
}



