package com.example.Monitoreo_de_Consumo_Energetico_en_el_Hogar;



import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.monitoreo_de_consumo_energtico_en_el_hogar.R;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView textViewLuz, textViewElectrodomesticos, textViewCalefaccion, textViewConsumoTotal, textViewConsumoMedioTotal;
    private ProgressBar progressBarLuz, progressBarElectrodomesticos, progressBarCalefaccion;
    private Handler handler = new Handler();
    private Random random = new Random();
    private Runnable updateRunnable;

    // Variables para el consumo total
    private int totalLuz = 0;
    private int totalElectrodomesticos = 0;
    private int totalCalefaccion = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar las vistas
        textViewLuz = findViewById(R.id.textViewLuz);
        textViewElectrodomesticos = findViewById(R.id.textViewElectrodomesticos);
        textViewCalefaccion = findViewById(R.id.textViewCalefaccion);
        textViewConsumoTotal = findViewById(R.id.textViewConsumoTotal);
        textViewConsumoMedioTotal = findViewById(R.id.textViewConsumoMedio);

        progressBarLuz = findViewById(R.id.progressBarLuz);
        progressBarElectrodomesticos = findViewById(R.id.progressBarElectrodomesticos);
        progressBarCalefaccion = findViewById(R.id.progressBarCalefaccion);

        // Inicializar imágenes
        ImageView imageViewLuz = findViewById(R.id.imageViewLuz);
        ImageView imageViewElectrodomesticos = findViewById(R.id.imageViewElectrodomesticos);
        ImageView imageViewCalefaccion = findViewById(R.id.imageViewCalefaccion);

        imageViewLuz.setImageResource(R.drawable.bombilla);
        imageViewElectrodomesticos.setImageResource(R.drawable.lavadora);
        imageViewCalefaccion.setImageResource(R.drawable.fuego);

        // Inicializar con valores aleatorios entre 30 y 120
        int luzInicial = random.nextInt(91) + 30; // Rango de 30 a 120
        int electrodomesticosInicial = random.nextInt(91) + 30;
        int calefaccionInicial = random.nextInt(91) + 30;

        // Actualizar la UI inicialmente
        updateUI(luzInicial, electrodomesticosInicial, calefaccionInicial);

        // Iniciar actualizaciones
        startUpdates();
    }

    private void startUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                // Cambios menores: generamos un valor aleatorio que se suma o resta
                int luz = getUpdatedValue(progressBarLuz.getProgress());
                int electrodomesticos = getUpdatedValue(progressBarElectrodomesticos.getProgress());
                int calefaccion = getUpdatedValue(progressBarCalefaccion.getProgress());

                // Actualizar la UI
                updateUI(luz, electrodomesticos, calefaccion);

                // Reprogramar el runnable cada 6 segundos
                handler.postDelayed(this, 6000);
            }
        };

        handler.post(updateRunnable);
    }

    private int getUpdatedValue(int current) {
        // Cambios menores: incrementamos o decrementamos en un rango pequeño
        int change = random.nextInt(11) - 5; // Rango de -5 a +5
        int newValue = current + change;
        return Math.max(30, Math.min(120, newValue)); // Asegurar que esté en el rango 30 a 120
    }

    private void updateUI(int luz, int electrodomesticos, int calefaccion) {
        textViewLuz.setText("Luz: " + luz + " kWh");
        textViewElectrodomesticos.setText("Electrodomésticos: " + electrodomesticos + " kWh");
        textViewCalefaccion.setText("Calefacción: " + calefaccion + " kWh");

        progressBarLuz.setProgress(luz);
        progressBarElectrodomesticos.setProgress(electrodomesticos);
        progressBarCalefaccion.setProgress(calefaccion);

        setProgressBarColor(progressBarLuz, luz);
        setProgressBarColor(progressBarElectrodomesticos, electrodomesticos);
        setProgressBarColor(progressBarCalefaccion, calefaccion);

        // Actualizar el consumo total y resetear el medio
        updateTotalConsumption(luz, electrodomesticos, calefaccion);
    }

    private void updateTotalConsumption(int luz, int electrodomesticos, int calefaccion) {
        // Resetear totales antes de actualizar
        totalLuz = luz;
        totalElectrodomesticos = electrodomesticos;
        totalCalefaccion = calefaccion;

        int total = totalLuz + totalElectrodomesticos + totalCalefaccion;
        textViewConsumoTotal.setText("Consumo Total: " + total + " kWh");

        // Reiniciar y calcular el consumo medio total
        int consumoMedioTotal = (totalLuz + totalElectrodomesticos + totalCalefaccion) / 3; // Calcular media
        textViewConsumoMedioTotal.setText("Consumo Medio : " + consumoMedioTotal + " kWh");
    }

    private void setProgressBarColor(ProgressBar progressBar, int value) {
        if (value < 40) { // 33% de 120 es 40
            progressBar.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.holo_green_light), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (value < 80) { // 66% de 120 es 80
            progressBar.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.holo_orange_light), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            progressBar.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.holo_red_light), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}