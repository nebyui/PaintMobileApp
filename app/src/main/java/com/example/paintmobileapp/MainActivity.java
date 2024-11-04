package com.example.paintmobileapp;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class MainActivity extends AppCompatActivity {
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paintBrush;
    private float lastTouchX;
    private float lastTouchY;
    private float totalTranslationX = 0f;
    private float totalTranslationY = 0f;
    private boolean tap = true;
    private int buttonWidth = 150;
    private int buttonHeight = 100;
    private Button eraserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeCanvas();

        FrameLayout rootLayout = new FrameLayout(this);

        View canvasView = new View(this) {

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
        };

        canvasView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        Button saveButton = new Button(this);
        saveButton.setText("Save");
        FrameLayout.LayoutParams saveButtonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        saveButtonParams.topMargin = 15;
        saveButtonParams.leftMargin = 15;
        saveButtonParams.width = buttonWidth;
        saveButtonParams.height = buttonHeight;
        saveButton.setLayoutParams(saveButtonParams);
        saveButton.setOnClickListener(v -> saveImage());


        Button loadButton = new Button(this);
        loadButton.setText("Load");
        FrameLayout.LayoutParams loadButtonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        loadButtonParams.topMargin = 15;
        loadButtonParams.leftMargin = 175;
        loadButtonParams.width = buttonWidth;
        loadButtonParams.height = buttonHeight;
        loadButton.setLayoutParams(loadButtonParams);
        loadButton.setOnClickListener(v -> loadImage());

        eraserButton = new Button(this);
        eraserButton.setText("Erase");
        FrameLayout.LayoutParams eraserButtonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        eraserButtonParams.topMargin = 15;
        eraserButtonParams.leftMargin = 335;
        eraserButtonParams.width = buttonWidth;
        eraserButtonParams.height = buttonHeight;
        eraserButton.setLayoutParams(eraserButtonParams);
        eraserButton.setOnClickListener(v -> eraserToggle());

        rootLayout.addView(canvasView);
        rootLayout.addView(saveButton);
        rootLayout.addView(loadButton);
        rootLayout.addView(eraserButton);
        setContentView(rootLayout);
    }

    private void initializeCanvas() {
        paintBrush = new Paint();
        paintBrush.setColor(Color.BLACK);
        paintBrush.setStrokeWidth(50);
        paintBrush.setStyle(Paint.Style.FILL);

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = (displayMetrics.widthPixels);
        int screenHeight = (displayMetrics.heightPixels);

        bitmap = Bitmap.createBitmap(screenWidth - 50, screenHeight - buttonHeight - 50, Bitmap.Config.ARGB_8888);
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

    private void saveImage() {
        try {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (directory != null) {

                if (!directory.exists()) {
                    directory.mkdirs();
                }

                File file = new File(directory, "canvas_image.png");

                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

            }
        } catch (IOException exception) {
            return;
        }
    }

    private void loadImage() {
        try {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(directory, "canvas_image.png");

            if (file.exists()) {

                Bitmap loadedBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(loadedBitmap, 0, 0, null);
                findViewById(android.R.id.content).invalidate();

            }
        } catch (Exception exception) {
            return;
        }
    }

    private void eraserToggle() {
        if (paintBrush.getColor() == Color.WHITE) {
            paintBrush.setColor(Color.BLACK);
            eraserButton.setText("Erase");
        } else {
            paintBrush.setColor(Color.WHITE);
            eraserButton.setText("Draw");
        }
    }
}