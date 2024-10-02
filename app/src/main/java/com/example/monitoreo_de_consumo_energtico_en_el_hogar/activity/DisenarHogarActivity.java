package com.example.monitoreo_de_consumo_energtico_en_el_hogar.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.monitoreo_de_consumo_energtico_en_el_hogar.R;
import com.example.monitoreo_de_consumo_energtico_en_el_hogar.data.HomeDataWorker;
import com.example.monitoreo_de_consumo_energtico_en_el_hogar.domain.Habitacion;
import com.example.monitoreo_de_consumo_energtico_en_el_hogar.domain.Pasillo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DisenarHogarActivity extends AppCompatActivity {

    private FrameLayout layoutHogar;
    private LinearLayout layoutDatosHabitaciones;
    private ArrayList<Habitacion> habitaciones;
    private ArrayList<Pasillo> pasillos;
    private Map<FrameLayout, Habitacion> mapaHabitaciones;
    private Map<FrameLayout, Pasillo> mapaPasillos;
    private ScaleGestureDetector scaleGestureDetector;
    private FirebaseFirestore db;
    private int colorSeleccionado = Color.TRANSPARENT;
    private FrameLayout layoutSeleccionado; // Para saber qué layout está seleccionado para escalar o mover
    private static final int MIN_WIDTH_HEIGHT = 150;
    private float dX, dY; // Para mover el layout

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disenar_hogar);

        layoutHogar = findViewById(R.id.layout_hogar);
        layoutDatosHabitaciones = findViewById(R.id.layout_datos_habitaciones);
        habitaciones = new ArrayList<>();
        pasillos = new ArrayList<>();
        mapaHabitaciones = new HashMap<>();
        mapaPasillos = new HashMap<>();

        db = FirebaseFirestore.getInstance();
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        findViewById(R.id.btn_agregar_habitacion).setOnClickListener(v -> mostrarDialogoNuevaHabitacion());
        findViewById(R.id.btn_agregar_pasillo).setOnClickListener(v -> mostrarDialogoNuevoPasillo());

        cargarElementosDesdeFirebase();

        layoutHogar.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
    }

    // Diálogo para agregar una nueva habitación
    private void mostrarDialogoNuevaHabitacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nueva Habitación");

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 10);

        EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre de la habitación");

        LinearLayout coloresLayout = new LinearLayout(this);
        coloresLayout.setOrientation(LinearLayout.HORIZONTAL);
        coloresLayout.setGravity(Gravity.CENTER);
        coloresLayout.setPadding(0, 20, 0, 20);

        int[] colores = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN };

        Button[] botonesColores = new Button[colores.length];

        for (int i = 0; i < colores.length; i++) {
            botonesColores[i] = new Button(this);
            botonesColores[i].setBackgroundColor(colores[i]);
            botonesColores[i].setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            final int color = colores[i];
            botonesColores[i].setOnClickListener(v -> {
                colorSeleccionado = color;
                for (Button btn : botonesColores) {
                    btn.setAlpha(0.5f);
                }
                v.setAlpha(1.0f);
            });
            coloresLayout.addView(botonesColores[i]);
        }

        dialogLayout.addView(inputNombre);
        dialogLayout.addView(coloresLayout);
        builder.setView(dialogLayout);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombreHabitacion = inputNombre.getText().toString().trim();

            if (!nombreHabitacion.isEmpty()) {
                if (colorSeleccionado == Color.TRANSPARENT) {
                    colorSeleccionado = Color.WHITE;
                }
                String colorHex = String.format("#%06X", (0xFFFFFF & colorSeleccionado));
                Habitacion nuevaHabitacion = new Habitacion(nombreHabitacion, generarConsumoSimulado(), colorHex);
                habitaciones.add(nuevaHabitacion);
                agregarHabitacionVisual(nuevaHabitacion);
                enviarDatosHabitacionAWorker(nuevaHabitacion);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Diálogo para agregar un nuevo pasillo
    private void mostrarDialogoNuevoPasillo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo Pasillo");

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 10);

        EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre del pasillo");

        // Botones para elegir la orientación del pasillo
        Button btnHorizontal = new Button(this);
        btnHorizontal.setText("Horizontal");

        Button btnVertical = new Button(this);
        btnVertical.setText("Vertical");

        final boolean[] esHorizontal = {true}; // Por defecto, horizontal

        btnHorizontal.setOnClickListener(v -> esHorizontal[0] = true);
        btnVertical.setOnClickListener(v -> esHorizontal[0] = false);

        dialogLayout.addView(inputNombre);
        dialogLayout.addView(btnHorizontal);
        dialogLayout.addView(btnVertical);
        builder.setView(dialogLayout);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombrePasillo = inputNombre.getText().toString().trim();

            if (!nombrePasillo.isEmpty()) {
                int ancho = esHorizontal[0] ? 500 : 100;
                int alto = esHorizontal[0] ? 100 : 500;
                Pasillo nuevoPasillo = new Pasillo(nombrePasillo, ancho, alto, "#A9A9A9", esHorizontal[0]);
                pasillos.add(nuevoPasillo);
                agregarPasilloVisual(nuevoPasillo);
                enviarDatosPasilloAWorker(nuevoPasillo);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void agregarHabitacionVisual(Habitacion habitacion) {
        FrameLayout habitacionLayout = new FrameLayout(this);

        String colorString = habitacion.getColor();
        if (colorString.isEmpty()) {
            habitacionLayout.setBackgroundColor(Color.WHITE);
        } else {
            try {
                habitacionLayout.setBackgroundColor(Color.parseColor(colorString));
            } catch (IllegalArgumentException e) {
                habitacionLayout.setBackgroundColor(Color.WHITE);
            }
        }

        habitacionLayout.setId(View.generateViewId());

        TextView nombreHabitacion = new TextView(this);
        nombreHabitacion.setText(habitacion.getNombre());
        nombreHabitacion.setTextColor(Color.BLACK);
        nombreHabitacion.setTextSize(16);
        nombreHabitacion.setGravity(Gravity.CENTER);
        habitacionLayout.addView(nombreHabitacion);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(300, 300);
        params.leftMargin = 50;
        params.topMargin = 50;
        habitacionLayout.setLayoutParams(params);
        layoutHogar.addView(habitacionLayout);
        mapaHabitaciones.put(habitacionLayout, habitacion);

        habilitarRedimensionadoYMovimiento(habitacionLayout);
        mostrarDatosHabitacion(habitacion);

        habitacionLayout.setOnTouchListener((v, event) -> {
            layoutSeleccionado = habitacionLayout;
            if (!scaleGestureDetector.isInProgress()) {
                moverLayoutConUnDedo(v, event); // Movimiento con un dedo
            }
            scaleGestureDetector.onTouchEvent(event); // Redimensionar con dos dedos
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void agregarPasilloVisual(Pasillo pasillo) {
        FrameLayout pasilloLayout = new FrameLayout(this);

        String colorString = pasillo.getColor();
        if (colorString == null || colorString.isEmpty()) {
            colorString = "#A9A9A9"; // Color gris oscuro por defecto
        }

        try {
            pasilloLayout.setBackgroundColor(Color.parseColor(colorString));
        } catch (IllegalArgumentException e) {
            pasilloLayout.setBackgroundColor(Color.GRAY); // Si el color no es válido, usa un gris por defecto
        }

        pasilloLayout.setId(View.generateViewId());

        TextView nombrePasillo = new TextView(this);
        nombrePasillo.setText(pasillo.getNombre());
        nombrePasillo.setTextColor(Color.WHITE);
        nombrePasillo.setTextSize(16);
        nombrePasillo.setGravity(Gravity.CENTER);
        pasilloLayout.addView(nombrePasillo);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(pasillo.getAncho(), pasillo.getAlto());
        params.leftMargin = 50;
        params.topMargin = 50;
        pasilloLayout.setLayoutParams(params);
        layoutHogar.addView(pasilloLayout);
        mapaPasillos.put(pasilloLayout, pasillo);

        habilitarRedimensionadoYMovimiento(pasilloLayout);

        pasilloLayout.setOnTouchListener((v, event) -> {
            layoutSeleccionado = pasilloLayout;
            if (!scaleGestureDetector.isInProgress()) {
                moverLayoutConUnDedo(v, event); // Movimiento con un dedo
            }
            scaleGestureDetector.onTouchEvent(event); // Redimensionar con dos dedos
            return true;
        });
    }

    // Método para habilitar el movimiento con un dedo
    @SuppressLint("ClickableViewAccessibility")
    private void moverLayoutConUnDedo(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dX = v.getX() - event.getRawX();
                dY = v.getY() - event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                v.animate()
                        .x(event.getRawX() + dX)
                        .y(event.getRawY() + dY)
                        .setDuration(0)
                        .start();
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void habilitarRedimensionadoYMovimiento(FrameLayout layout) {
        layout.setOnTouchListener((v, event) -> {
            layoutSeleccionado = layout;
            if (!scaleGestureDetector.isInProgress()) {
                moverLayoutConUnDedo(v, event); // Movimiento con un dedo
            }
            scaleGestureDetector.onTouchEvent(event); // Redimensionar con dos dedos
            return true;
        });
    }

    // Clase para gestionar el redimensionado con dos dedos
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (layoutSeleccionado != null) {
                float scaleFactor = detector.getScaleFactor();
                int newWidth = Math.max(MIN_WIDTH_HEIGHT, (int) (layoutSeleccionado.getWidth() * scaleFactor));
                int newHeight = Math.max(MIN_WIDTH_HEIGHT, (int) (layoutSeleccionado.getHeight() * scaleFactor));
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layoutSeleccionado.getLayoutParams();
                params.width = newWidth;
                params.height = newHeight;
                layoutSeleccionado.setLayoutParams(params);
            }
            return true;
        }
    }

    private void mostrarDatosHabitacion(Habitacion habitacion) {
        TextView datosHabitacion = new TextView(this);
        datosHabitacion.setText("Habitación: " + habitacion.getNombre() + " - Consumo: " + habitacion.getConsumoEnergetico() + " kWh");
        layoutDatosHabitaciones.addView(datosHabitacion);
    }

    private void enviarDatosHabitacionAWorker(Habitacion habitacion) {
        Data inputData = new Data.Builder()
                .putString("nombre", habitacion.getNombre())
                .putInt("consumoEnergetico", habitacion.getConsumoEnergetico())
                .putString("color", habitacion.getColor())
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(HomeDataWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);

    }

    private void enviarDatosPasilloAWorker(Pasillo pasillo) {
        Data inputData = new Data.Builder()
                .putString("nombre", pasillo.getNombre())
                .putString("color", pasillo.getColor())
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(HomeDataWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);
        guardarPasilloEnFirebase(pasillo);
    }

    private void cargarElementosDesdeFirebase() {
        // Cargar habitaciones
        db.collection("habitaciones").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nombre = document.getString("nombre");
                    Long consumoLong = document.getLong("consumoEnergetico");
                    String color = document.getString("color");

                    int consumoEnergetico = (consumoLong != null) ? consumoLong.intValue() : 0;

                    if (nombre != null && color != null) {
                        Habitacion habitacion = new Habitacion(nombre, consumoEnergetico, color);
                        habitaciones.add(habitacion);
                        agregarHabitacionVisual(habitacion);
                    }
                }
            }
        });

        // Cargar pasillos
        db.collection("pasillos").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nombre = document.getString("nombre");
                    Long anchoLong = document.getLong("ancho");
                    Long altoLong = document.getLong("alto");
                    String color = document.getString("color");
                    Boolean esHorizontal = document.getBoolean("esHorizontal");

                    int ancho = (anchoLong != null) ? anchoLong.intValue() : 100;
                    int alto = (altoLong != null) ? altoLong.intValue() : 500;

                    if (nombre != null && color != null && esHorizontal != null) {
                        Pasillo pasillo = new Pasillo(nombre, ancho, alto, color, esHorizontal);
                        pasillos.add(pasillo);
                        agregarPasilloVisual(pasillo);
                    }
                }
            }
        });
    }


    private int generarConsumoSimulado() {
        Random random = new Random();
        return random.nextInt(500) + 100;
    }

    private void guardarPasilloEnFirebase(Pasillo pasillo) {
        Map<String, Object> pasilloData = new HashMap<>();
        pasilloData.put("nombre", pasillo.getNombre());
        pasilloData.put("ancho", pasillo.getAncho());
        pasilloData.put("alto", pasillo.getAlto());
        pasilloData.put("color", pasillo.getColor());
        pasilloData.put("esHorizontal", pasillo.esHorizontal());

        db.collection("pasillos")
                .add(pasilloData)
                .addOnSuccessListener(documentReference -> {
                    // Puedes manejar una acción cuando se guarde exitosamente
                    System.out.println("Pasillo guardado con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Maneja el error si falla el guardado
                    System.out.println("Error guardando pasillo: " + e.getMessage());
                });
    }

    private void guardarHabitacionEnFirebase(Habitacion habitacion) {
        Map<String, Object> habitacionData = new HashMap<>();
        habitacionData.put("nombre", habitacion.getNombre());
        habitacionData.put("consumoEnergetico", habitacion.getConsumoEnergetico());
        habitacionData.put("color", habitacion.getColor());

        db.collection("habitaciones")
                .add(habitacionData)
                .addOnSuccessListener(documentReference -> {
                    // Acción después de guardar la habitación
                    System.out.println("Habitación guardada con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Maneja el error si falla el guardado
                    System.out.println("Error guardando habitación: " + e.getMessage());
                });
    }

}



