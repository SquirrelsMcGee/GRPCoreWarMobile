/**
 * Created by james on 17/04/2018.
 */
package jMARS;

import frontend.*;
import marsVM.MarsVM;
import marsVM.WarriorObj;

import java.util.*;

import android.graphics.*;
import android.os.health.SystemHealthManager;
import android.view.*;

import android.content.Context;
import com.corewarmobile.corewarmobile.GameActivity;
import com.corewarmobile.corewarmobile.R;

import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import java.io.*;

public class jMARS implements Runnable, FrontEndManager {

    // constants
    static final int numDefinedColors = 4;
    static final int wColors[][] = {
            {Color.GREEN, Color.YELLOW},
            {Color.RED, Color.MAGENTA},
            {Color.CYAN, Color.BLUE},
            {Color.GRAY, Color.DKGRAY}};
    /*
    static final int wColors[][] = {
            {0x00FF00, 0xFFFF00},
            {0xFF0000, 0xFF00FF},
            {0x00FFFF, 0x0000FF},
            {0x808080, 0x696969},
    };*/

    public boolean Active = false;
    public boolean Paused = false;

    // Application specific variables
    String args[];
    //static Frame myFrame; // Unimplemented

    //static jMARS myApp;

    // Common variables
    int maxProc;
    int pSpaceSize;
    public int coreSize;
    int cycles;
    int rounds;
    int maxWarriorLength;
    int minWarriorDistance;
    int numWarriors;
    int minWarriors;

    WarriorObj warriors[];
    CoreDisplay coreDisplay;
    //RoundCycleCounter roundCycleCounter; // Unimplemented
    MarsVM MARS;

    int roundNum;
    int cycleNum;
    int warRun;
    int runWarriors;

    static Thread myThread;
    Handler handler;

    Vector<StepListener> stepListeners;
    Vector<CycleListener> cycleListeners;
    Vector<RoundListener> roundListeners;

    Date startTime;
    Date endTime;
    double totalTime;
    boolean roundOver = false;

    // To write to the screen we use
    // SurfaceView surface
    // SurfaceHolder surfaceHolder;
    // Canvas coreCanvas;
    // Canvas bufferCanvas
    // Rect canvasDimensions;
    Paint paint = new Paint();

    Context context;
    GameActivity activity;



    public SurfaceHolder surfaceHolder;
    public SurfaceView surfaceView;
    public Canvas coreCanvas;
    public Canvas bufferCanvas;
    public Matrix identityMatrix = new Matrix();
    public Bitmap bmp;

    public jMARS(GameActivity superActivity)
    {
        activity = superActivity;

        //surfaceHolder = activity.surfaceHolder;
        //bufferCanvas = activity.bufferCanvas;
        //context = activity.context;


        stepListeners = new Vector<>();
        cycleListeners = new Vector<>();
        roundListeners = new Vector<>();

        //frameCanvas = bufferCanvas; // Updated to use Android canvas
    }

    public void test() {

        for (int i = 0; i < 1000; i+=100) {

            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            Rect rectangle = new Rect(i, 500, i+100, 600);
            paint.setColor(color);

            //activity.bufferCanvas.drawRect(rectangle, paint);
        }


    }

    public void application_init()
    {
        boolean pspaceChanged = false;
        Vector<Integer> wArgs = new Vector<Integer>();

        // Set defaults for various constants
        maxWarriorLength = 100;
        minWarriorDistance = 100;
        maxProc = 8000;
        coreSize = 5000;
        cycles = 1000;
        rounds = 1;
        numWarriors = 2;


        if (!pspaceChanged)
            pSpaceSize = coreSize / 16;

        if (numWarriors == 0)
            System.out.println("ERROR: no warrior files specified");

        warriors = new WarriorObj[numWarriors];

        String filenames[] = {GameActivity.WarriorName,GameActivity.WarriorName2};
        //String filenames[] = {"imp.red", "clp.red"};

        InputStream iS;

        //if (true)return;

        for (int i=0; i < numWarriors; i++)
        {
            try
            {
                File file = new File(activity.getFilesDir(), filenames[i]);
                InputStream inputStream = new FileInputStream(file);
                //InputStream inputStream = activity.getAssets().open(filenames[i]);


                Integer aColor = wColors[i % numDefinedColors][0];
                Integer dColor = wColors[i % numDefinedColors][1];

                warriors[i] = new WarriorObj(inputStream, maxWarriorLength, aColor, dColor);
                warriors[i].initPSpace(pSpaceSize);
                warriors[i].setPCell(0, -1);

                System.out.println("Warrior ["+i+"] name = " + warriors[i].getName());

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        //activity.WarriorName = warriors[0].getName();
        //activity.WarriorName2 = warriors[1].getName();
        //activity.TextChanger(activity.WarriorName, activity.WarriorName2);


        warriors[0].Alive = true;
        warriors[1].Alive = true;

        coreDisplay = activity.coreDisplay;

        MARS = new MarsVM(coreSize, maxProc);

        loadWarriors();

        runWarriors = numWarriors;
        minWarriors = (numWarriors == 1) ? 0 : 1;
        roundNum = 0;
        cycleNum = 0;
        warRun = 0;


    }
    public void startThread() {

        Active = true;

        myThread = new Thread(this);
        myThread.start();
    }

    public static String getStringFromFile (File file) throws Exception {
        File fl = file;
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public void run() {

        Button runButton = activity.findViewById(R.id.runButton);
        if (Active) runButton.setText("Pause");
        startTime = new Date();
        roundNum = 0;
        rounds = 1;

        activity.coreDisplay.clear();


        final int delayMillis = 5;

        for (; roundNum < rounds; roundNum++) {
            roundOver = false;

            //System.out.println("Round #"+roundNum);

            handler = new Handler(Looper.getMainLooper());

            final Runnable cycleLoop = new Runnable() {
                @Override
                public void run() {
                    if (cycleNum < cycles && roundOver == false) {
                        //if (Paused) System.out.println("Attempted to cycle but was paused");
                        if (Active && !Paused) {
                            for (; warRun < runWarriors; warRun++) {

                                StepReport stats = MARS.executeInstr();

                                WarriorObj war = stats.warrior();

                                war.numProc = stats.numProc();

                                if (stats.wDeath()) {
                                    war.Alive = false;
                                    runWarriors--;
                                }

                                notifyStepListeners(stats);
                            }

                            notifyCycleListeners(cycleNum);

                            warRun = 0;

                            if (runWarriors <= minWarriors) {
                                roundOver = true;
                            }

                            cycleNum++;

                        }
                        handler.postDelayed(this, delayMillis);

                    } else {
                        if (Active && !Paused) {
                            //notifyRoundListeners(roundNum);
                            if (activity.coreCanvas == null) {
                                System.out.println("For some reason coreCanvas is null");
                                return;
                            }

                            endTime = new Date();
                            totalTime = ((double) endTime.getTime() - (double) startTime.getTime()) / 1000;
                            System.out.println("Total time=" + totalTime + " Cycles=" + cycleNum + " avg. time/cycle=" + (totalTime / cycleNum));

                            paint.setColor(Color.WHITE);
                            paint.setTextSize(40);

                            activity.coreCanvas = activity.surfaceHolder.lockCanvas();
                            activity.bufferCanvas.drawText("Total time=" + totalTime + " Cycles=" + cycleNum + " avg. time/cycle=" + (totalTime / cycleNum),
                                    10, -100 + activity.coreCanvas.getHeight() / 2, paint);

                            paint.setColor(Color.LTGRAY);
                            activity.bufferCanvas.drawText("Total time=" + totalTime + " Cycles=" + cycleNum + " avg. time/cycle=" + (totalTime / cycleNum),
                                    12, -98 + activity.coreCanvas.getHeight() / 2, paint);

                            activity.coreCanvas.drawBitmap(activity.bmp, activity.identityMatrix, null);
                            activity.surfaceHolder.unlockCanvasAndPost(activity.coreCanvas);

                            startTime = new Date();

                            MARS.reset();
                            loadWarriors();
                            runWarriors = numWarriors;
                            //activity.coreDisplay.clear();

                            Active = false;
                            Button runButton = activity.findViewById(R.id.runButton);
                            runButton.setText("Replay");

                            cycleNum = 0;
                        }
                    }
                }
            };

            handler.postDelayed(cycleLoop, delayMillis);
        }
    }


    public void loadWarriors()
    {
        int[] location = new int[warriors.length];

        if (!MARS.loadWarrior(warriors[0], 0))
        {
            System.out.println("ERROR: could not load warrior 1.");
        }

        for (int i=1, r=0; i<numWarriors; i++)
        {
            boolean validSpot;
            do
            {
                validSpot = true;
                r = (int) (Math.random() * coreSize);
                //System.out.println(r);
                if (r < minWarriorDistance || r > (coreSize - minWarriorDistance))
                    validSpot = false;

                for (int j=0; j < location.length; j++)
                    if (r < (minWarriorDistance + location[j]) && r > (minWarriorDistance + location[j]))
                        validSpot = false;

            } while (!validSpot);

            if (!MARS.loadWarrior(warriors[i], r))
            {
                System.out.println("ERROR: could not load warrior " + (i+1) + ".");
            }
        }
    }


    public void registerStepListener(StepListener l) {
        stepListeners.addElement(l);
    }

    public void registerCycleListener(CycleListener c) {
        cycleListeners.addElement(c);
    }

    public void registerRoundListener(RoundListener r) {
        roundListeners.addElement(r);
    }

    private void notifyStepListeners(StepReport step) {
        for (Enumeration e = stepListeners.elements(); e.hasMoreElements(); ) {
            StepListener j = (StepListener) e.nextElement();
            if (Active) {
                //System.out.println("Stepping Process");
                j.stepProcess(step);
            }
        }
    }

    private void notifyCycleListeners(int cycle) {
        for (Enumeration e = cycleListeners.elements(); e.hasMoreElements(); ) {
            CycleListener j = (CycleListener) e.nextElement();
            if (Active) {
                //System.out.println("Cycle Finished");
                j.cycleFinished(cycle);
            }
        }
    }

    private void notifyRoundListeners(int round) {
        for (Enumeration e = roundListeners.elements(); e.hasMoreElements(); ) {
            RoundListener j = (RoundListener) e.nextElement();
            if (Active) {
                //System.out.println("Round Finished");
                j.roundResults(round);
            }
        }
    }

    public void screenClose() {
        Active = false;
        System.out.println("handler exists" + (handler != null) + " thread exists" + (myThread != null));
        if (myThread != null) myThread.interrupt();
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }

    public void togglePause() {
        Button runButton = activity.findViewById(R.id.runButton);
        if (Paused) {
            runButton.setText("Pause");
            Paused = false;
        } else {
            runButton.setText("Run");
            Paused = true;
        }

        System.out.println("Paused=" +Paused);



    }

}
