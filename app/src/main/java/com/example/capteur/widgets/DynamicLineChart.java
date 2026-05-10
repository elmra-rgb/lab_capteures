package com.example.capteur.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DynamicLineChart extends View {

    private final List<Float> dataBuffer = new ArrayList<>();
    private final int maxStoredPoints = 80;

    private final Paint axisPainter = new Paint();
    private final Paint curvePainter = new Paint();
    private final Paint textPainter = new Paint();

    public DynamicLineChart(Context context) {
        super(context);

        axisPainter.setColor(Color.LTGRAY);
        axisPainter.setStrokeWidth(3);

        curvePainter.setColor(Color.rgb(33, 150, 243));
        curvePainter.setStrokeWidth(5);
        curvePainter.setStyle(Paint.Style.STROKE);

        textPainter.setColor(Color.DKGRAY);
        textPainter.setTextSize(30);
    }

    public void pushMeasurement(float newValue) {
        if (dataBuffer.size() >= maxStoredPoints) {
            dataBuffer.remove(0);
        }
        dataBuffer.add(newValue);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas drawingCanvas) {
        super.onDraw(drawingCanvas);

        int canvasWidth = getWidth();
        int canvasHeight = getHeight();

        drawingCanvas.drawLine(40, canvasHeight - 40, canvasWidth - 20, canvasHeight - 40, axisPainter);
        drawingCanvas.drawLine(40, 20, 40, canvasHeight - 40, axisPainter);

        if (dataBuffer.size() < 2) {
            drawingCanvas.drawText("En attente des données...", 60, canvasHeight / 2, textPainter);
            return;
        }

        float minVal = Float.MAX_VALUE;
        float maxVal = -Float.MAX_VALUE;

        for (float val : dataBuffer) {
            minVal = Math.min(minVal, val);
            maxVal = Math.max(maxVal, val);
        }

        if (maxVal == minVal) {
            maxVal = minVal + 1;
        }

        Path linePath = new Path();

        for (int idx = 0; idx < dataBuffer.size(); idx++) {
            float currentX = 40 + idx * ((canvasWidth - 80f) / (maxStoredPoints - 1));
            float normalizedVal = (dataBuffer.get(idx) - minVal) / (maxVal - minVal);
            float currentY = canvasHeight - 40 - normalizedVal * (canvasHeight - 80);

            if (idx == 0) {
                linePath.moveTo(currentX, currentY);
            } else {
                linePath.lineTo(currentX, currentY);
            }
        }

        drawingCanvas.drawPath(linePath, curvePainter);
        drawingCanvas.drawText("Mini : " + minVal + " | Maxi : " + maxVal, 60, 40, textPainter);
    }
}
