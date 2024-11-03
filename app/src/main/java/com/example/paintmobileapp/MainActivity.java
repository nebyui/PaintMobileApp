package com.example.paintmobileapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paintBrush;
    private float lastX;
    private float lastY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeCanvas();

        setContentView(new View(this) {

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
                        invalidate();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    private void initializeCanvas() {
        paintBrush = new Paint();
        paintBrush.setColor(Color.BLACK);
        paintBrush.setStrokeWidth(50);
        paintBrush.setStyle(Paint.Style.FILL);
        paintBrush.setAntiAlias(true);

        bitmap = Bitmap.createBitmap(1500, 1000, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
    }

    private void drawLine(float startX, float startY, float endX, float endY) {
        float distance = (float) Math.ceil(Math.hypot(endX - startX, endY - startY));
        for (int i = 0; i <= distance; i++) {
            float interpolatedPosition = (float) i / distance;
            float x = startX + interpolatedPosition * (endX - startX);
            float y = startY + interpolatedPosition * (endY - startY);
            canvas.drawCircle(x, y, 10, paintBrush);
        }
    }


}