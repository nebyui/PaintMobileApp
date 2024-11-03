package com.example.paintmobileapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ScaleGestureDetector;

public class MainActivity extends AppCompatActivity {
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paintBrush;
    private float lastTouchX;
    private float lastTouchY;
    private float sizeScale = 1.0f;
    private float translateX = 0;
    private float translateY = 0;
    private float dx = 0;
    private float dy = 0;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private ScaleGestureDetector scaleGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeCanvas();

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
          @Override
          public boolean onScale(ScaleGestureDetector detector) {
              sizeScale *= detector.getScaleFactor();
              sizeScale = Math.max(0.1f, Math.min(sizeScale, 5.0f));
              getWindow().getDecorView().postInvalidate();
              return true;
          }
        });

        setContentView(new View(this) {

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                canvas.save();
                canvas.translate(translateX, translateY);
                canvas.scale(sizeScale, sizeScale);
                canvas.drawBitmap(bitmap, 0, 0, null);
                canvas.restore();
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);

                float scaledX = (event.getX() - translateX) / sizeScale;
                float scaledY = (event.getY() - translateY) / sizeScale;

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if (event.getPointerCount() == 1) {
                            lastTouchX = scaledX;
                            lastTouchY = scaledY;
                            drawLine(lastTouchX, lastTouchY, lastTouchX + 0.1f, lastTouchY + 0.1f);
                            invalidate();
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() == 1) {
                            drawLine(lastTouchX, lastTouchY, scaledX, scaledY);
                            lastTouchX = scaledX;
                            lastTouchY = scaledY;
                            invalidate();
                        } else if (event.getPointerCount() == 2) {
                            float newDX = event.getX(0) - (lastTouchX * sizeScale + translateX);
                            float newDY = event.getY(0) - (lastTouchY * sizeScale + translateY);
                            translateX += newDX;
                            translateY += newDY;
                            lastTouchX = scaledX; // Update last touch position for consistent dragging
                            lastTouchY = scaledY;
                            invalidate();
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
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
            canvas.drawCircle(x, y, 10, paintBrush);
        }
    }


}