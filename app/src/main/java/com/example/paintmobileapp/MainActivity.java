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
    private float lastTouchX;
    private float lastTouchY;
    private float totalTranslationX = 0f;
    private float totalTranslationY = 0f;
    private boolean tap = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeCanvas();



        setContentView(new View(this) {

            @Override
            protected void onDraw(Canvas canvas) {
                canvas.save();
                canvas.translate(totalTranslationX, totalTranslationY);
                canvas.drawBitmap(bitmap, 0, 0, null);
                canvas.restore();
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        lastTouchX = event.getX();
                        lastTouchY = event.getY();

                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        tap = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() == 1 && tap) {
                            float newTouchX = event.getX();
                            float newTouchY = event.getY();
                            drawLine(lastTouchX, lastTouchY, newTouchX, newTouchY);
                            lastTouchX = newTouchX;
                            lastTouchY = newTouchY;
                            invalidate();
                        } else if (event.getPointerCount() == 2 && !tap) {
                            float changeInX = event.getX(0) - lastTouchX;
                            float changeInY = event.getY(0) - lastTouchY;
                            totalTranslationX += changeInX;
                            totalTranslationY += changeInY;
                            lastTouchX = event.getX(0);
                            lastTouchY = event.getY(0);
                            invalidate();
                        }
                        break;
                    case MotionEvent.ACTION_UP: {
                        if (tap) {
                            drawLine(lastTouchX, lastTouchY, lastTouchX + 0.1f, lastTouchY + 0.1f);
                            invalidate();
                        }
                        tap = true;
                        break;
                    }
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
            canvas.drawCircle(x - totalTranslationX, y - totalTranslationY, 10, paintBrush);
        }
    }


}