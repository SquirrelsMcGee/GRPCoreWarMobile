/*


    GameActivity.java


 */

package com.corewarmobile.corewarmobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import java.lang.Runnable;
import java.util.Random;
import android.util.DisplayMetrics;
import android.widget.ProgressBar;
import android.widget.TextView;

import frontend.*;
import marsVM.*;
import assembler.*;
import jMARS.*;

public class GameActivity extends AppCompatActivity {
    public static Context context;


    SurfaceView surface;
    TextView progress;

    public SurfaceHolder surfaceHolder;
    public Canvas coreCanvas;
    public Canvas bufferCanvas;
    public Rect canvasDimensions;
    static public String WarriorName = "Warrior 1";
    static public String WarriorName2 = "Warrior 2";
    public Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
    public Bitmap bmp;


    int count;
    int id;
    public Paint paint = new Paint();
    public Rect rectangle;
    public Matrix identityMatrix;

    int canvasWidth, canvasHeight;
    public CoreDisplay coreDisplay;

    GameActivity gameActivity = this;
    jMARS jmars;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameActivity.context = getApplicationContext();
        setContentView(R.layout.activity_game);
        
        TextChanger(WarriorName, WarriorName2);
        surface = (SurfaceView) findViewById(R.id.CoreSurface);
        progress = (TextView) findViewById(R.id.progressLabel);
        identityMatrix = new Matrix();

        final Handler handler = new Handler();
        final Runnable loop = new Runnable() {
            @Override
            public void run() {

                id = count;
                progress.setText("Start " + id);

                coreCanvas = surfaceHolder.lockCanvas();

                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

                rectangle = new Rect(count, 100, count+100, 200);
                count+=100;
                paint.setColor(color);

                bufferCanvas.drawRect(rectangle, paint);
                coreCanvas.drawBitmap(bmp, identityMatrix, null);

                progress.setText("End " + id);
                surfaceHolder.unlockCanvasAndPost(coreCanvas);

                if (count < 1000) {
                    handler.postDelayed(this, 500);

                }
            }
        };

        jmars = new jMARS(gameActivity);

        surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                if (!jmars.Active) jmars = new jMARS(gameActivity);

                jmars.application_init();

                surfaceHolder = holder;
                canvasDimensions = holder.getSurfaceFrame();



                canvasWidth = canvasDimensions.width();
                canvasHeight = canvasDimensions.height();

                coreDisplay = new CoreDisplay(gameActivity, jmars, surface, jmars.coreSize, canvasWidth, canvasHeight);

                bmp = Bitmap.createBitmap(canvasWidth, canvasHeight, conf); // this creates a MUTABLE bitmap
                bufferCanvas = new Canvas(bmp);
                // Do some drawing when surface is ready

                progress.setText("Post");

                jmars.test();
                //bufferCanvas = jmars.bufferCanvas;

                //coreCanvas = surfaceHolder.lockCanvas();
                //coreCanvas.drawBitmap(bmp, identityMatrix, null);
                //surfaceHolder.unlockCanvasAndPost(coreCanvas);

                coreCanvas = surfaceHolder.lockCanvas();
                paint.setColor(Color.BLACK);
                bufferCanvas.drawRect(0,0,canvasWidth,canvasHeight, paint);

                for (int i = 0; i < 1000; i+=100) {

                    Random rnd = new Random();
                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    Rect rectangle = new Rect(i, 0, i+100, 100);
                    paint.setColor(color);

                    //bufferCanvas.drawRect(rectangle, paint);

                }
                coreCanvas.drawBitmap(bmp, identityMatrix, null);
                surfaceHolder.unlockCanvasAndPost(coreCanvas);

                count = 0;
                //handler.postDelayed(loop, 500);
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                surface.getHolder().removeCallback(this);
                //if (handler != null) handler.removeCallbacks(loop);
                jmars.screenClose();
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

        });
    }


    public void NewWarrior(View view){
        Intent NewW = new Intent(this, NewWarriorActivity.class);
        startActivity(NewW);
    }
    public void Add(View view){
        if(WarriorName != "Warrior 1" && WarriorName2 != "Warrior 2") {
            WarriorName = "Warrior 1";
            WarriorName2 = "Warrior 2";
        }
        Intent Barracks = new Intent(this, BarrackActivity.class);
        startActivity(Barracks);
    }
    public void Compile(View view){
        if(WarriorName == "Warrior 1" || WarriorName2 == "Warrior 2") {
            Toast.makeText(getBaseContext(), "Add Warriors first", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getBaseContext(), "Compiled", Toast.LENGTH_SHORT).show();
        }

    }
    public void RunGame(View view) {
        jmars.startThread();
        //jmars.run();
    }
    public void TextChanger(String WarriorName, String WarriorName2) {
        TextView WarriorOne = (TextView)findViewById(R.id.warriorOneLabel);
        TextView WarriorTwo = (TextView)findViewById(R.id.warriorTwoLabel);
        WarriorOne.setText(WarriorName);
        WarriorTwo.setText(WarriorName2);
    }
}