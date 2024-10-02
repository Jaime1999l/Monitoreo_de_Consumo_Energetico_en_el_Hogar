package com.example.monitoreo_de_consumo_energtico_en_el_hogar.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.monitoreo_de_consumo_energtico_en_el_hogar.R;
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
    private FrameLayout layoutSeleccionado;
    private static final int MIN_WIDTH_HEIGHT = 150;
    private float dX, dY;

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
        iniciarActualizacionConsumo();
    }

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

        int[] colores = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN};

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
                guardarHabitacionEnFirebase(nuevaHabitacion);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void mostrarDialogoNuevoPasillo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo Pasillo");

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 10);

        EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre del pasillo");

        Button btnHorizontal = new Button(this);
        btnHorizontal.setText("Horizontal");

        Button btnVertical = new Button(this);
        btnVertical.setText("Vertical");

        final boolean[] esHorizontal = {true};

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
                guardarPasilloEnFirebase(nuevoPasillo);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void agregarHabitacionVisual(Habitacion habitacion) {
        FrameLayout habitacionLayout = new FrameLayout(this);

        String colorString = habitacion.getColor();
        habitacionLayout.setBackgroundColor(Color.parseColor(colorString));

        habitacionLayout.setId(View.generateViewId());

        TextView nombreHabitacion = new TextView(this);
        nombreHabitacion.setText(habitacion.getNombre());
        nombreHabitacion.setTextColor(Color.BLACK);
        nombreHabitacion.setTextSize(16);
        nombreHabitacion.setGravity(Gravity.CENTER);
        habitacionLayout.addView(nombreHabitacion);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(300, 300);
        params.leftMargin = habitacion.getPosX();
        params.topMargin = habitacion.getPosY();
        habitacionLayout.setLayoutParams(params);
        layoutHogar.addView(habitacionLayout);
        mapaHabitaciones.put(habitacionLayout, habitacion);

        habilitarRedimensionadoYMovimiento(habitacionLayout, habitacion);
        mostrarDatosHabitacion(habitacion);

        habitacionLayout.setOnTouchListener((v, event) -> {
            layoutSeleccionado = habitacionLayout;
            if (!scaleGestureDetector.isInProgress()) {
                moverLayoutConUnDedo(v, event, habitacion);
            }
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void agregarPasilloVisual(Pasillo pasillo) {
        FrameLayout pasilloLayout = new FrameLayout(this);

        pasilloLayout.setBackgroundColor(Color.parseColor(pasillo.getColor()));

        pasilloLayout.setId(View.generateViewId());

        TextView nombrePasillo = new TextView(this);
        nombrePasillo.setText(pasillo.getNombre());
        nombrePasillo.setTextColor(Color.WHITE);
        nombrePasillo.setTextSize(16);
        nombrePasillo.setGravity(Gravity.CENTER);
        pasilloLayout.addView(nombrePasillo);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(pasillo.getAncho(), pasillo.getAlto());
        params.leftMargin = pasillo.getPosX();
        params.topMargin = pasillo.getPosY();
        pasilloLayout.setLayoutParams(params);
        layoutHogar.addView(pasilloLayout);
        mapaPasillos.put(pasilloLayout, pasillo);

        habilitarRedimensionadoYMovimiento(pasilloLayout, pasillo);

        pasilloLayout.setOnTouchListener((v, event) -> {
            layoutSeleccionado = pasilloLayout;
            if (!scaleGestureDetector.isInProgress()) {
                moverLayoutConUnDedo(v, event, pasillo);
            }
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void moverLayoutConUnDedo(View v, MotionEvent event, Object objeto) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dX = v.getX() - event.getRawX();
                dY = v.getY() - event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float newX = event.getRawX() + dX;
                float newY = event.getRawY() + dY;

                newX = Math.max(0, Math.min(newX, layoutHogar.getWidth() - v.getWidth()));
                newY = Math.max(0, Math.min(newY, layoutHogar.getHeight() - v.getHeight()));

                v.animate().x(newX).y(newY).setDuration(0).start();

                if (objeto instanceof Habitacion) {
                    Habitacion habitacion = (Habitacion) objeto;
                    habitacion.setPosX((int) newX);
                    habitacion.setPosY((int) newY);
                    guardarHabitacionEnFirebase(habitacion);
                } else if (objeto instanceof Pasillo) {
                    Pasillo pasillo = (Pasillo) objeto;
                    pasillo.setPosX((int) newX);
                    pasillo.setPosY((int) newY);
                    guardarPasilloEnFirebase(pasillo);
                }
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void habilitarRedimensionadoYMovimiento(FrameLayout layout, Object objeto) {
        layout.setOnTouchListener((v, event) -> {
            layoutSeleccionado = layout;
            if (!scaleGestureDetector.isInProgress()) {
                moverLayoutConUnDedo(v, event, objeto);
            }
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
    }

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

                if (mapaHabitaciones.containsKey(layoutSeleccionado)) {
                    Habitacion habitacion = mapaHabitaciones.get(layoutSeleccionado);
                    habitacion.setAncho(newWidth);
                    habitacion.setAlto(newHeight);
                    guardarHabitacionEnFirebase(habitacion);
                } else if (mapaPasillos.containsKey(layoutSeleccionado)) {
                    Pasillo pasillo = mapaPasillos.get(layoutSeleccionado);
                    pasillo.setAncho(newWidth);
                    pasillo.setAlto(newHeight);
                    guardarPasilloEnFirebase(pasillo);
                }
            }
            return true;
        }
    }

    private void mostrarDatosHabitacion(Habitacion habitacion) {
        TextView datosHabitacion = new TextView(this);
        datosHabitacion.setText("Habitación: " + habitacion.getNombre() + " - Consumo: " + habitacion.getConsumoEnergetico() + " kWh");
        layoutDatosHabitaciones.addView(datosHabitacion);
    }

    private void guardarHabitacionEnFirebase(Habitacion habitacion) {
        Map<String, Object> habitacionData = new HashMap<>();
        habitacionData.put("nombre", habitacion.getNombre());
        habitacionData.put("consumoEnergetico", habitacion.getConsumoEnergetico());
        habitacionData.put("color", habitacion.getColor());
        habitacionData.put("posX", habitacion.getPosX());
        habitacionData.put("posY", habitacion.getPosY());
        habitacionData.put("ancho", habitacion.getAncho());
        habitacionData.put("alto", habitacion.getAlto());

        db.collection("habitaciones").document(habitacion.getNombre()).set(habitacionData);
    }

    private void guardarPasilloEnFirebase(Pasillo pasillo) {
        Map<String, Object> pasilloData = new HashMap<>();
        pasilloData.put("nombre", pasillo.getNombre());
        pasilloData.put("ancho", pasillo.getAncho());
        pasilloData.put("alto", pasillo.getAlto());
        pasilloData.put("color", pasillo.getColor());
        pasilloData.put("esHorizontal", pasillo.esHorizontal());
        pasilloData.put("posX", pasillo.getPosX());
        pasilloData.put("posY", pasillo.getPosY());

        db.collection("pasillos").document(pasillo.getNombre()).set(pasilloData);
    }

    private void cargarElementosDesdeFirebase() {
        db.collection("habitaciones").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nombre = document.getString("nombre");
                    Long consumoLong = document.getLong("consumoEnergetico");
                    String color = document.getString("color");
                    Long posX = document.getLong("posX");
                    Long posY = document.getLong("posY");
                    Long anchoLong = document.getLong("ancho");
                    Long altoLong = document.getLong("alto");

                    int consumoEnergetico = (consumoLong != null) ? consumoLong.intValue() : 0;
                    int ancho = (anchoLong != null) ? anchoLong.intValue() : 300;
                    int alto = (altoLong != null) ? altoLong.intValue() : 300;

                    if (nombre != null && color != null && posX != null && posY != null) {
                        Habitacion habitacion = new Habitacion(nombre, consumoEnergetico, color);
                        habitacion.setPosX(posX.intValue());
                        habitacion.setPosY(posY.intValue());
                        habitacion.setAncho(ancho);
                        habitacion.setAlto(alto);
                        habitaciones.add(habitacion);
                        agregarHabitacionVisual(habitacion);
                    }
                }
            }
        });

        db.collection("pasillos").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nombre = document.getString("nombre");
                    Long anchoLong = document.getLong("ancho");
                    Long altoLong = document.getLong("alto");
                    String color = document.getString("color");
                    Boolean esHorizontal = document.getBoolean("esHorizontal");
                    Long posX = document.getLong("posX");
                    Long posY = document.getLong("posY");

                    int ancho = (anchoLong != null) ? anchoLong.intValue() : 100;
                    int alto = (altoLong != null) ? altoLong.intValue() : 500;

                    if (nombre != null && color != null && esHorizontal != null && posX != null && posY != null) {
                        Pasillo pasillo = new Pasillo(nombre, ancho, alto, color, esHorizontal);
                        pasillo.setPosX(posX.intValue());
                        pasillo.setPosY(posY.intValue());
                        pasillos.add(pasillo);
                        agregarPasilloVisual(pasillo);
                    }
                }
            }
        });
    }

    private void iniciarActualizacionConsumo() {
        Handler handler = new Handler();
        Runnable actualizarConsumo = new Runnable() {
            @Override
            public void run() {
                for (Habitacion habitacion : habitaciones) {
                    habitacion.setConsumoEnergetico(generarConsumoSimulado());
                    guardarHabitacionEnFirebase(habitacion);
                }
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(actualizarConsumo);
    }

    private int generarConsumoSimulado() {
        Random random = new Random();
        return random.nextInt(500) + 100;
    }
}


