package com.example.monitoreo_de_consumo_energtico_en_el_hogar.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HomeDataWorker extends Worker {
    private FirebaseFirestore db;

    public HomeDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        String nombre = getInputData().getString("nombre");
        int consumoEnergetico = getInputData().getInt("consumoEnergetico", 0);
        String color = getInputData().getString("color");

        Map<String, Object> habitacionData = new HashMap<>();
        habitacionData.put("nombre", nombre);
        habitacionData.put("consumoEnergetico", consumoEnergetico);
        habitacionData.put("color", color);

        db.collection("habitaciones").document(nombre)
                .set(habitacionData)
                .addOnSuccessListener(aVoid -> {
                    // Éxito
                })
                .addOnFailureListener(e -> {
                    // Error
                });

        return Result.success();
    }
}



