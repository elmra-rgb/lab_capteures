package com.example.capteur.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class DynamicPlotView extends View {

    private final List<Float> dataBuffer = new ArrayList<>();
    private final int maxCapacity = 70;

    private final Paint axisPainter = new Paint();
    private final Paint curvePainter = new Paint();
    private final Paint labelPainter = new Paint();

    public DynamicPlotView(Context ctx) {
        super(ctx);
        initPaints();
    }

    private void initPaints() {
        axisPainter.setColor(Color.LTGRAY);
        axisPainter.setStrokeWidth(2);

        curvePainter.setColor(Color.rgb(0, 150, 136));
        curvePainter.setStrokeWidth(4);
        curvePainter.setStyle(Paint.Style.STROKE);
        curvePainter.setAntiAlias(true);

        labelPainter.setColor(Color.DKGRAY);
        labelPainter.setTextSize(28);
    }

    public void feedData(float newValue) {
        if (dataBuffer.size() >= maxCapacity) {
            dataBuffer.remove(0);
        }
        dataBuffer.add(newValue);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        // Dessiner les axes
        canvas.drawLine(45, h - 45, w - 25, h - 45, axisPainter);
        canvas.drawLine(45, 25, 45, h - 45, axisPainter);

        if (dataBuffer.size() < 2) {
            canvas.drawText("⏳ En attente des données...", 60, h / 2, labelPainter);
            return;
        }

        float minVal = Float.MAX_VALUE;
        float maxVal = -Float.MAX_VALUE;
        for (float v : dataBuffer) {
            minVal = Math.min(minVal, v);
            maxVal = Math.max(maxVal, v);
        }
        if (maxVal == minVal) maxVal = minVal + 1;

        Path linePath = new Path();
        for (int idx = 0; idx < dataBuffer.size(); idx++) {
            float xPos = 45 + idx * ((w - 70f) / (maxCapacity - 1));
            float norm = (dataBuffer.get(idx) - minVal) / (maxVal - minVal);
            float yPos = h - 45 - norm * (h - 80);
            if (idx == 0) linePath.moveTo(xPos, yPos);
            else linePath.lineTo(xPos, yPos);
        }

        canvas.drawPath(linePath, curvePainter);
        canvas.drawText(String.format("Min: %.2f | Max: %.2f", minVal, maxVal), 60, 50, labelPainter);
    }
}
