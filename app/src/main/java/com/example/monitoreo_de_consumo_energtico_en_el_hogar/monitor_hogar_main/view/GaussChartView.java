package com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.PaintDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.List;

public class GaussChartView extends View {

    private Paint linePaint;
    private Paint axisPaint;
    private Paint pointPaint; // Variable agregada para los puntos destacados
    private int maxConsumo;
    private List<Integer> consumos;

    public GaussChartView(Context context) {
        super(context);
        init();
    }

    public GaussChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Pintura para las líneas del gráfico
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#4CAF50")); // Color verde claro
        linePaint.setStrokeWidth(6);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true); // Suavizado de bordes
        linePaint.setPathEffect(new CornerPathEffect(50)); // Líneas suavizadas

        // Pintura para los ejes
        axisPaint = new Paint();
        axisPaint.setColor(Color.parseColor("#757575")); // Gris claro
        axisPaint.setStrokeWidth(3);
        axisPaint.setTextSize(36);
        axisPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)); // Fuente moderna

        // Pintura para los puntos destacados
        pointPaint = new Paint(); // Inicialización faltante
        pointPaint.setColor(Color.RED); // Color rojo para los puntos
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        // Uso de GradientDrawable para establecer un fondo de gradiente
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] { Color.parseColor("#E0F7FA"), Color.WHITE } // Colores del gradiente
        );
        gradientDrawable.setCornerRadius(0f); // Si quieres bordes redondeados, ajusta esto
        setBackground(gradientDrawable); // Aplicamos el GradientDrawable como fondo
    }

    public void setData(List<Integer> consumos) {
        if (consumos != null && !consumos.isEmpty()) {
            this.consumos = consumos; // Guardamos la lista de consumos
            maxConsumo = consumos.stream().max(Integer::compare).orElse(1); // Calculamos el valor máximo
            invalidate(); // Redibujamos el gráfico con los nuevos datos
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (consumos == null || consumos.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();

        // Dibujar cuadrícula de fondo (opcional)
        drawGrid(canvas, width, height);

        // Eje X
        canvas.drawLine(0, height - 50, width, height - 50, axisPaint);
        // Eje Y
        canvas.drawLine(50, 0, 50, height, axisPaint);

        int prevX = 0, prevY = 0;
        for (int i = 0; i < consumos.size(); i++) {
            int x = 50 + (i * (width - 100) / consumos.size());
            int consumo = consumos.get(i);
            int y = height - (consumo * height / maxConsumo);

            if (i > 0) {
                // Dibujar línea suavizada
                canvas.drawLine(prevX, prevY, x, y, linePaint);
            }

            // Dibujar puntos destacados
            canvas.drawCircle(x, y, 8, pointPaint); // Puntos rojos en cada consumo

            prevX = x;
            prevY = y;
        }
    }

    private void drawGrid(Canvas canvas, int width, int height) {
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#BDBDBD")); // Color gris suave
        gridPaint.setStrokeWidth(1);

        // Dibujar líneas verticales
        for (int i = 50; i < width; i += 100) {
            canvas.drawLine(i, 0, i, height, gridPaint);
        }

        // Dibujar líneas horizontales
        for (int i = 50; i < height; i += 100) {
            canvas.drawLine(50, i, width, i, gridPaint);
        }
    }
}