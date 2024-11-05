package com.example.paintmobileapp;

import android.graphics.BitmapFactory; // imports all the classes that the program utilizes
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
    private Bitmap bitmap; // declaring all the variables that are used throughout the program
    private Canvas canvas;
    private Paint paintBrush;
    private float initialTouchX;
    private float initialTouchY;
    private float totalTranslationX = 0f;
    private float totalTranslationY = 0f;
    private boolean tap = true;
    private Button eraserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //onCreate runs once when the program first starts
        super.onCreate(savedInstanceState);
        initializeCanvas();  // calls a custom function that creates the canvas

        FrameLayout frameLayout = new FrameLayout(this); // FrameLayout is an Android class, it handles and stacks the child elements like the buttons

        View canvasView = new View(this) {
            // View is an Android class, it creates the visible UI elements like the canvas and buttons
            @Override
            protected void onDraw(Canvas canvas) { // This function runs every event loop
                canvas.save(); // this saves the current state of the canvas so it does not get messed up when the translations happen
                canvas.translate(totalTranslationX, totalTranslationY); // moves the canvas around the screen. the parameters are updated elsewhere
                canvas.drawBitmap(bitmap, 0, 0, null); // the bitmap holds the paint data, this restores the data when changes occur on the canvas
                canvas.restore(); // this brings back state of the canvas that was previously saved
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) { // provided by View class, runs when the contact between the finger and screen change

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: // when the first finger is placed
                        initialTouchX = event.getX(); // stores the coordinates of the initial touch to be used later
                        initialTouchY = event.getY();
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN: // when additional fingers are placed
                        tap = false; // tap is used to detect when the user whats to simply tap a dot. If more than one finger is detected, that tap event does not occur
                        break;
                    case MotionEvent.ACTION_MOVE: // whenever the finger is dragged across the screen
                        if (event.getPointerCount() == 1 && tap) { // when only one finger is detected and tap is true
                            float newTouchX = event.getX(); // updates the position of the finger as it moves
                            float newTouchY = event.getY();
                            drawLine(initialTouchX, initialTouchY, newTouchX, newTouchY); // calls drawing function, both initial and new positions are needed
                            initialTouchX = newTouchX; // updates the initial touch cordinates to prep for the next iteration onTouchEvent
                            initialTouchY = newTouchY;
                            invalidate(); // updates the image with new line
                        } else if (event.getPointerCount() == 2 && !tap) { // when two fingers are detected
                            float changeInX = event.getX(0) - initialTouchX; // gets the new position of the fingers
                            float changeInY = event.getY(0) - initialTouchY;
                            totalTranslationX += changeInX; // addes the new displacement to the total displacement of the canvas from 0,0
                            totalTranslationY += changeInY;
                            initialTouchX = event.getX(0); // updates finger position for next iteration of onTouchEvent
                            initialTouchY = event.getY(0);
                            invalidate(); // refreshes image
                        }
                        break;
                    case MotionEvent.ACTION_UP: { // when the last finger is lifted up
                        if (tap) { // if tap is true, then it draws a dot. Prevents dots from being created when user uses two fingers to drag
                            drawLine(initialTouchX, initialTouchY, initialTouchX + 0.1f, initialTouchY + 0.1f);
                            invalidate();
                        }
                        tap = true; // tap becomes false whenever two fingers are detected, so it resets after all fingers are removed
                        break;
                    }
                }
                return true;
            }
        };

        canvasView.setLayoutParams(new FrameLayout.LayoutParams( // FrameLayout class allow customization of placement and size of different UI elements
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        Button saveButton = new Button(this); // creates save button, Button class provided by Android
        saveButton.setText("Save");
        FrameLayout.LayoutParams saveButtonLayout = new FrameLayout.LayoutParams( // applies FrameLayout's properties and functions to button
                FrameLayout.LayoutParams.WRAP_CONTENT, // makes sure the button is large enough to contain its text contents
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        saveButtonLayout.topMargin = 15; // distance from edges of screen
        saveButtonLayout.leftMargin = 15;
        saveButton.setLayoutParams(saveButtonLayout); // applies the properties defined in saveButtonLayout to button
        saveButton.setOnClickListener(v -> saveImage());

        Button loadButton = new Button(this); // same thing but with load button
        loadButton.setText("Load");
        FrameLayout.LayoutParams loadButtonLayout = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        loadButtonLayout.topMargin = 15;
        loadButtonLayout.leftMargin = 175;
        loadButton.setLayoutParams(loadButtonLayout);
        loadButton.setOnClickListener(v -> loadImage());

        eraserButton = new Button(this); // same thing but with eraser button
        eraserButton.setText("Erase");
        FrameLayout.LayoutParams eraserButtonLayout = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        eraserButtonLayout.topMargin = 15;
        eraserButtonLayout.leftMargin = 335;
        eraserButton.setLayoutParams(eraserButtonLayout);
        eraserButton.setOnClickListener(v -> eraserToggle());

        frameLayout.addView(canvasView);  // adds the canvas and buttons to frameLayout to be added to the UI
        frameLayout.addView(saveButton);
        frameLayout.addView(loadButton);
        frameLayout.addView(eraserButton);
        setContentView(frameLayout);
    }

    private void initializeCanvas() { // Creates the canvas and its attributes
        paintBrush = new Paint(); // Paint class provided by Android, creates brush
        paintBrush.setColor(Color.BLACK);
        paintBrush.setStrokeWidth(50);
        paintBrush.setStyle(Paint.Style.FILL);

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE); // WindowManager manages the layout and display of the Android device
        DisplayMetrics displayMetrics = new DisplayMetrics(); // DisplayMetrics handles information about the phone display
        windowManager.getDefaultDisplay().getMetrics(displayMetrics); // WindowMangager tells DisplayMetrics the details of the display
        int screenWidth = (displayMetrics.widthPixels); // gets width and height from displayMetrics
        int screenHeight = (displayMetrics.heightPixels);

        bitmap = Bitmap.createBitmap(screenWidth - 50, screenHeight - 100, Bitmap.Config.ARGB_8888); // defines size and properties of the bitmap
        canvas = new Canvas(bitmap); // applies the bitmap to the canvas.
        canvas.drawColor(Color.WHITE); // sets canvas color to white
    }
    private void drawLine(float startX, float startY, float endX, float endY) { // draws line, uses initialTouch and newTouch coordinates
        float distance = (float) Math.ceil(Math.hypot(endX - startX, endY - startY)); // calculates how far apart the two touch positions are
        for (float i = 0; i <= distance; i++) { // iterates through every point between the two touch positions
            float progressAlongDistance = i / distance; // gets the point along distance relative to how many iterations have occured
            float currentX = startX + progressAlongDistance * (endX - startX);
            float currentY = startY + progressAlongDistance * (endY - startY);
            canvas.drawCircle(currentX - totalTranslationX, currentY - totalTranslationY, 10, paintBrush); // draws circle at every point, making it appear as a continuous stroke
                   // the total translation is calculated as well so the line always appears at the finger when the canvas has been moved
        }
    }

    private void saveImage() {
        try {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); // finds the Pictures folder in Android device
            if (directory != null) { // checks if directory variable was properly initialized, does not check if Pictures folder exists

                if (!directory.exists()) { // checks if Pictures folder exists, creates one if not
                    directory.mkdirs(); //
                }

                File file = new File(directory, "painting.png"); // names the image file

                FileOutputStream outputStream = new FileOutputStream(file); // creates a stream instance to send data
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream); // streams the bitmap information to the image
                outputStream.flush();
                outputStream.close(); // clears and ends the stream

            }
        } catch (IOException exception) {
            return;
        }
    }

    private void loadImage() {
        try {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); // locates Pictures folder
            File file = new File(directory, "painting.png"); // finds image with that name

            if (file.exists()) {
                Bitmap loadedBitmap = BitmapFactory.decodeFile(file.getAbsolutePath()); // turns the data of the image into a bitmap
                canvas.drawColor(Color.WHITE); // clears and preps the canvas by filling it with white
                canvas.drawBitmap(loadedBitmap, 0, 0, null); // applies the bitmap data to the canvas
                findViewById(android.R.id.content).invalidate(); // updates the entire UI

            }
        } catch (Exception exception) {
            return;
        }
    }

    private void eraserToggle() {
        if (paintBrush.getColor() == Color.WHITE) { // inverts the color and text on button when it is pressed
            paintBrush.setColor(Color.BLACK);
            eraserButton.setText("Erase");
        } else {
            paintBrush.setColor(Color.WHITE);
            eraserButton.setText("Draw");
        }
    }
}