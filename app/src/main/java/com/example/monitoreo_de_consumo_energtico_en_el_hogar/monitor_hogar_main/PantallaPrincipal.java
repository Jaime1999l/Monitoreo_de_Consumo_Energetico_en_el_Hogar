package com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoreo_de_consumo_energtico_en_el_hogar.R;
import com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main.activity.DisenarHogarActivity;
import com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main.activity.EnergyMonitorActivity;
import com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main.activity.RoomMonitorActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PantallaPrincipal extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ExecutorService executorService; //Para el manejo de hilos
    private FirebaseFirestore db;
    private Map<String, Integer> habitacionesConsumo;
    private Map<String, Map<String, Integer>> energyUsage;  // Para almacenar consumos de luz, electrodomésticos, calefacción

    private Random random;

    // Flags para controlar los hilos
    private volatile boolean isUpdatingConsumo = true; // Variables volatiles para que los hilos tengan una visibilidad inmediata de esta variable, esto se hace posible debido a que la lectura/escritura de esta variable se realiza sobre la memoria principal direcamente y no son guardadas en la cache del procesador
    private volatile boolean isUpdatingEnergyUsage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializamos Firebase y los Mapas de datos
        db = FirebaseFirestore.getInstance();
        habitacionesConsumo = new HashMap<>();
        energyUsage = new HashMap<>();
        random = new Random();

        // Crear un pool de hilos para ejecutar tareas en segundo plano
        executorService = Executors.newFixedThreadPool(2);

        // Ejecutamos las tareas en segundo plano
        runBackgroundTasksConsumo();
        runBackgroundTasksEnergy();

        TextView greetingTextView = findViewById(R.id.greeting_text_view);
        greetingTextView.setText("¡Bienvenido a la aplicación de monitoreo del hogar!");

        // Imagen
        ImageView imageView = findViewById(R.id.home_image);
        imageView.setImageResource(R.drawable.hogar);

        // Menu desplegable
        drawerLayout = findViewById(R.id.drawer_layout);

        // Menu
        Button openMenuButton = findViewById(R.id.open_menu_button);
        openMenuButton.setOnClickListener(v -> drawerLayout.openDrawer(findViewById(R.id.menu_layout)));

        // Configurar redirección a otras actividades
        setupNavigation();
    }

    // Método para configurar la redirección a otras actividades
    private void setupNavigation() {
        // Opción del menú: Diseñar Hogar
        TextView navDesignHome = findViewById(R.id.nav_design_home);
        navDesignHome.setOnClickListener(v -> {
            detenerHiloCosumo(); // Detenemos el hilo correspondiente al ejecutarse otra actividad, de esta manera cedemos el manejo de los datos a la propia actividad
            Intent intent = new Intent(PantallaPrincipal.this, DisenarHogarActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers(); // Cerrar el menú después de la selección
        });

        // Opción del menú: Monitorear Consumo por Habitación
        TextView navRoomMonitoring = findViewById(R.id.nav_room_monitoring);
        navRoomMonitoring.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipal.this, RoomMonitorActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers(); // Cerrar el menú después de la selección
        });

        // Opción del menú: Monitorear Dispositivos Consumidores
        TextView navDeviceMonitoring = findViewById(R.id.nav_device_monitoring);
        navDeviceMonitoring.setOnClickListener(v -> {
            detenerHiloEnergy();
            Intent intent = new Intent(PantallaPrincipal.this, EnergyMonitorActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers(); // Cerrar el menú después de la selección
        });
    }

    private void runBackgroundTasksConsumo() {
        executorService.execute(this::collectDesignHomeData);
    }
    private void runBackgroundTasksEnergy() {
        executorService.execute(this::collectEnergyMonitorData);
    }

    private void collectDesignHomeData() {
        // Obtenemos los datos de consumo para las habitaciones desde Firebase (colección "habitaciones")
        db.collection("habitaciones").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nombreHabitacion = document.getString("nombre");
                    Long consumo = document.getLong("consumoEnergetico");
                    if (nombreHabitacion != null && consumo != null) {
                        habitacionesConsumo.put(nombreHabitacion, consumo.intValue());
                    }
                }
                // Iniciar la actualización periódica del consumo después de obtener los datos
                startConsumoUpdate();
            } else {
                Log.d("Firebase", "Error al obtener datos de habitaciones: ", task.getException());
            }
        });
    }

    private void collectEnergyMonitorData() {
        db.collection("energyUsage").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Long consumoLuz = document.getLong("luz");
                    Long consumoElectrodomesticos = document.getLong("electrodomesticos");
                    Long consumoCalefaccion = document.getLong("calefaccion");

                    if (consumoLuz != null && consumoElectrodomesticos != null && consumoCalefaccion != null) {
                        String dispositivo = document.getId();  // Usamos el ID del documento como identificador

                        // Creamos un mapa para almacenar los consumos por dispositivo
                        Map<String, Integer> consumos = new HashMap<>();
                        consumos.put("luz", consumoLuz.intValue());
                        consumos.put("electrodomesticos", consumoElectrodomesticos.intValue());
                        consumos.put("calefaccion", consumoCalefaccion.intValue());

                        // Almacenamos los datos en el mapa energyUsage
                        energyUsage.put(dispositivo, consumos);
                    } else {
                        Log.e("Firebase", "Error: faltan datos en un documento de energyUsage.");
                    }
                }
                // Actualizacion periodica de energyUsage
                startEnergyUsageUpdate();
            } else {
                Log.e("Firebase", "Error al obtener datos de energyUsage: ", task.getException());
            }
        });
    }



    private void startConsumoUpdate() {
        executorService.execute(() -> {
            while (isUpdatingConsumo) {
                for (Map.Entry<String, Integer> entry : habitacionesConsumo.entrySet()) {
                    int nuevoConsumo = entry.getValue() + random.nextInt(10) - 5; // Cambios aleatorios
                    habitacionesConsumo.put(entry.getKey(), Math.max(nuevoConsumo, 0)); // Asegurar que no sea negativo

                    Log.d("Consumo Habitacion", entry.getKey() + ": " + nuevoConsumo + " kWh");

                    // Actualizacion de los datos en Firebase
                    Map<String, Object> dataUpdate = new HashMap<>();
                    dataUpdate.put("consumoEnergetico", nuevoConsumo);
                    db.collection("habitaciones").document(entry.getKey()).update(dataUpdate)
                            .addOnSuccessListener(aVoid -> Log.d("Firebase", "Consumo actualizado para " + entry.getKey()))
                            .addOnFailureListener(e -> Log.e("Firebase", "Error al actualizar el consumo", e));
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void startEnergyUsageUpdate() {
        // Inicia una tarea en segundo plano
        executorService.execute(() -> {
            try {
                while (isUpdatingEnergyUsage) {

                    // Recorre cada entrada del mapa de energyUsage. Cada entrada contiene un dispositivo como clave de tipo String
                    // y un mapa de consumos (Map<String, Integer>) como valor. Este mapa de consumos guarda los valores de
                    // luz, electrodomesticos y calefaccion
                    for (Map.Entry<String, Map<String, Integer>> entry : energyUsage.entrySet()) {
                        String dispositivo = entry.getKey();
                        Map<String, Integer> consumos = entry.getValue();

                        // Actualizacion de cada campo de consumo
                        int nuevoConsumoLuz = consumos.get("luz") + random.nextInt(10) - 5;
                        int nuevoConsumoElectrodomesticos = consumos.get("electrodomesticos") + random.nextInt(10) - 5;
                        int nuevoConsumoCalefaccion = consumos.get("calefaccion") + random.nextInt(10) - 5;

                        // Asegurar que los consumos no sean negativos
                        consumos.put("luz", Math.max(nuevoConsumoLuz, 0));
                        consumos.put("electrodomesticos", Math.max(nuevoConsumoElectrodomesticos, 0));
                        consumos.put("calefaccion", Math.max(nuevoConsumoCalefaccion, 0));

                        // Convertiremos el mapa a Map<String, Object> para Firebase
                        Map<String, Object> consumosObject = new HashMap<>();
                        consumosObject.put("luz", consumos.get("luz"));
                        consumosObject.put("electrodomesticos", consumos.get("electrodomesticos"));
                        consumosObject.put("calefaccion", consumos.get("calefaccion"));

                        // Actualizar los datos en Firebase
                        db.collection("energyUsage").document(dispositivo).update(consumosObject)
                                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Consumo actualizado para " + dispositivo))
                                .addOnFailureListener(e -> Log.e("Firebase", "Error al actualizar el consumo", e));
                    }
                    // Pausamos 5 segundos antes de la proxima actualizacion
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                Log.e("EnergyUsageUpdate", "Error en la actualización periódica: " + e.getMessage());
            }
        });
    }

    private void detenerHiloCosumo() {
        isUpdatingConsumo = false;
    }
    private void detenerHiloEnergy() {
        isUpdatingEnergyUsage = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cerrar el ExecutorService cuando ya no se necesiten los hilos
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

