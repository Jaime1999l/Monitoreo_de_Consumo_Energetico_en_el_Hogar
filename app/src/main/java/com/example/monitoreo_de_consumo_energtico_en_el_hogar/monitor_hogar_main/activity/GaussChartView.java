package com.example.monitoreo_de_consumo_energtico_en_el_hogar.monitor_hogar_main.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.List;

public class GaussChartView extends View {

    private List<Integer> consumos;
    private Paint linePaint;
    private Paint axisPaint;
    private int maxConsumo;

    public GaussChartView(Context context) {
        super(context);
        init();
    }

    public GaussChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(5);
        linePaint.setStyle(Paint.Style.STROKE);

        axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(3);
    }

    public void setData(List<Integer> consumos) {
        if (consumos != null && !consumos.isEmpty()) {
            this.consumos = consumos;
            maxConsumo = consumos.stream().max(Integer::compare).orElse(1);
            invalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (consumos == null || consumos.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        canvas.drawLine(0, (float) height / 2, width, (float) height / 2, axisPaint); // Eje X
        canvas.drawLine(50, 0, 50, height, axisPaint); // Eje Y

        int prevX = 0, prevY = 0;
        for (int i = 0; i < consumos.size(); i++) {
            int x = 50 + (i * (width - 100) / consumos.size());
            int consumo = consumos.get(i);
            int y = height - (consumo * height / maxConsumo);

            if (i > 0) {
                canvas.drawLine(prevX, prevY, x, y, linePaint);
            }
            prevX = x;
            prevY = y;
        }
    }
}
