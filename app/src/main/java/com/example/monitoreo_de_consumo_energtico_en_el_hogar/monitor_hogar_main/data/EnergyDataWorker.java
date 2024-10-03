package com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class EnergyDataWorker extends Worker {

    private FirebaseFirestore db;

    public EnergyDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance();  // Inicializar Firestore
    }

    @NonNull
    @Override
    public Result doWork() {
        // Obtenemos los datos pasados desde la Activity
        int luz = getInputData().getInt("luz", 0);
        int electrodomesticos = getInputData().getInt("electrodomesticos", 0);
        int calefaccion = getInputData().getInt("calefaccion", 0);
        String token = getInputData().getString("token");

        // Creamos un mapa de datos para actualizar en Firestore
        Map<String, Object> energyData = new HashMap<>();
        energyData.put("luz", luz);
        energyData.put("electrodomesticos", electrodomesticos);
        energyData.put("calefaccion", calefaccion);
        energyData.put("timestamp", System.currentTimeMillis());

        // Comprobamos si el documento ya existe
        db.collection("energyUsage").document(token)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Si el documento ya existe, actualiza los datos
                        db.collection("energyUsage").document(token)
                                .update(energyData)
                                .addOnSuccessListener(aVoid -> Log.d("EnergyDataWorker", "Datos actualizados con éxito"))
                                .addOnFailureListener(e -> Log.e("EnergyDataWorker", "Error al actualizar los datos", e));
                    } else {
                        // Si no existe, se crea el documento
                        db.collection("energyUsage").document(token)
                                .set(energyData)
                                .addOnSuccessListener(aVoid -> Log.d("EnergyDataWorker", "Documento creado y datos guardados"))
                                .addOnFailureListener(e -> Log.e("EnergyDataWorker", "Error al crear documento y guardar los datos", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("EnergyDataWorker", "Error al verificar la existencia del documento", e));

        return Result.success();
    }
}


