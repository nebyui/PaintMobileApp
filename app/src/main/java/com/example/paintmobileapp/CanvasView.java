package com.example.paintmobileapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.util.AttributeSet;

public class CanvasView extends View {
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paintBrush;
    private float lastX;
    private float lastY;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeCanvas();
    }

    private void initializeCanvas() {
        paintBrush = new Paint();
        paintBrush.setColor(Color.BLACK);
        paintBrush.setStrokeWidth(10);
        paintBrush.setStyle(Paint.Style.STROKE);

        bitmap = Bitmap.createBitmap(1500, 1000, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                drawLine(lastX, lastY, x, y);
                lastX = x;
                lastY = y;
                break;
            default:
                return false;
        }
        return true;
    }

    private void drawLine(float startX, float startY, float endX, float endY) {
        canvas.drawLine(startX, startY, endX, endY, paintBrush);
        invalidate();
    }
}
