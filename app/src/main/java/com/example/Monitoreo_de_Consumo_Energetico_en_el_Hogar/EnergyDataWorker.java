package com.example.Monitoreo_de_Consumo_Energetico_en_el_Hogar;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Random;
import android.util.Log;

public class EnergyDataWorker extends Worker {

    private static final String PREFS_NAME = "EnergyData";
    private static final String KEY_LUZ = "luz";
    private static final String KEY_ELECTRODOMESTICOS = "electrodomesticos";
    private static final String KEY_CALefaccion = "calefaccion";

    public EnergyDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Obtener SharedPreferences
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Generar nuevos valores aleatorios
        Random random = new Random();
        int luz = random.nextInt(101); // Nuevo valor aleatorio de 0 a 100
        int electrodomesticos = random.nextInt(101);
        int calefaccion = random.nextInt(101);

        // Guardar los nuevos valores
        editor.putInt(KEY_LUZ, luz);
        editor.putInt(KEY_ELECTRODOMESTICOS, electrodomesticos);
        editor.putInt(KEY_CALefaccion, calefaccion);
        editor.apply();

        // Log de los valores
        Log.d("EnergyDataWorker", "Luz: " + luz + ", Electrodomésticos: " + electrodomesticos + ", Calefacción: " + calefaccion);

        // Crear los datos de salida
        Data outputData = new Data.Builder()
                .putInt("luz", luz)
                .putInt("electrodomesticos", electrodomesticos)
                .putInt("calefaccion", calefaccion)
                .build();

        // Retornar los datos
        return Result.success(outputData);
    }
}
