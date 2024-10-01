package com.example.monitoreo_de_consumo_energtico_en_el_hogar.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class EnergyDataWorker extends Worker {

    private FirebaseFirestore db;

    public EnergyDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Obtener los datos pasados desde la Activity
        int luz = getInputData().getInt("luz", 0);
        int electrodomesticos = getInputData().getInt("electrodomesticos", 0);
        int calefaccion = getInputData().getInt("calefaccion", 0);
        String token = getInputData().getString("token"); // Obtener el token único

        // Crear un mapa de datos para guardar en Firestore
        Map<String, Object> energyData = new HashMap<>();
        energyData.put("luz", luz);
        energyData.put("electrodomesticos", electrodomesticos);
        energyData.put("calefaccion", calefaccion);
        energyData.put("token", token); // Guardar el token
        energyData.put("timestamp", System.currentTimeMillis());

        // Guardar los datos en Firestore en la colección 'energyUsage'
        db.collection("energyUsage").document(token) // Usamos el token como ID del documento
                .set(energyData)
                .addOnSuccessListener(aVoid -> Log.d("EnergyDataWorker", "Datos guardados con éxito"))
                .addOnFailureListener(e -> Log.e("EnergyDataWorker", "Error al guardar los datos", e));

        // Retornar el éxito del Worker
        return Result.success();
    }
}

