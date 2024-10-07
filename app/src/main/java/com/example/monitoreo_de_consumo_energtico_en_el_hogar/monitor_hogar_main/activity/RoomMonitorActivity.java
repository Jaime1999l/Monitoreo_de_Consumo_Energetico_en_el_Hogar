package com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monitoreo_de_consumo_energtico_en_el_hogar.R;
import com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main.view.GaussChartView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RoomMonitorActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private GaussChartView gaussChartView;
    private TextView promedioTextView;
    private Handler handler;
    private final List<Integer> consumosList = new ArrayList<>();
    private int tokenCounter = 1; // Inicialización del token
    private Runnable updateTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_monitor);

        gaussChartView = findViewById(R.id.gaussChartView);
        promedioTextView = findViewById(R.id.promedioTextView);

        db = FirebaseFirestore.getInstance();

        // Obtener los datos iniciales de habitaciones y guardarlos en "historial_consumo"
        obtenerDatosHabitaciones();


        handler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                obtenerDatosHabitaciones();
                obtenerDatosDeHistorial();
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(updateTask); // Iniciar las actualizaciones
    }


    private void obtenerDatosHabitaciones() {
        CollectionReference habitacionesRef = db.collection("habitaciones");

        habitacionesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    Long consumo = document.getLong("consumoEnergetico");

                    if (consumo != null) {
                        // Tokenizado para evitar sobrescribir
                        String token = "TOKEN_" + tokenCounter++;
                        guardarConsumoEnHistorial(consumo.intValue(), token);
                    }
                }
            } else {
                Log.e("Firebase", "Error al obtener datos de habitaciones", task.getException());
            }
        });
    }

    private void guardarConsumoEnHistorial(int consumo, String token) {
        Map<String, Object> consumoData = new HashMap<>();
        consumoData.put("consumo", consumo);
        consumoData.put("fecha", System.currentTimeMillis());

        DocumentReference historialRef = db.collection("historial_consumo").document(token);
        historialRef.set(consumoData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Consumo guardado con éxito en historial"))
                .addOnFailureListener(e -> Log.e("Firebase", "Error al guardar el consumo en historial", e));
    }

    @SuppressLint("SetTextI18n")
    private void obtenerDatosDeHistorial() {
        CollectionReference historialConsumoRef = db.collection("historial_consumo");

        historialConsumoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Integer> consumos = new ArrayList<>();
                int totalConsumo = 0;
                int contador = 0;

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Long consumo = document.getLong("consumo");
                    if (consumo != null) {
                        consumos.add(consumo.intValue());
                        totalConsumo += consumo;
                        contador++;
                    }
                }

                if (contador > 0) {
                    int promedio = totalConsumo / contador;
                    promedioTextView.setText("Promedio de Consumo: " + promedio + " kWh");
                    gaussChartView.setData(consumos);
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTask);}
}