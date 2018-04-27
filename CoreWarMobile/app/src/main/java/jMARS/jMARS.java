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

import android.os.*;
import android.support.v7.app.AppCompatActivity;

import java.io.*;

public class jMARS implements Runnable, FrontEndManager {

    // constants
    static final int numDefinedColors = 4;
    /*static final int wColors[][] = {
            {Color.GREEN, Color.YELLOW},
            {Color.RED, Color.MAGENTA},
            {Color.CYAN, Color.BLUE},
            {Color.GRAY, Color.DKGRAY}};*/
    static final int wColors[][] = {
            {0x00FF00, 0xFFFF00},
            {0xFF0000, 0xFF00FF},
            {0x00FFFF, 0x0000FF},
            {0x808080, 0x696969},
    };

    public boolean Active = false;

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

    Vector<StepListener> stepListeners;
    Vector<CycleListener> cycleListeners;
    Vector<RoundListener> roundListeners;

    Date startTime;
    Date endTime;
    double totalTime;

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

        Active = true;


        //frameCanvas = bufferCanvas; // Updated to use Android canvas
    }

    public void test() {

        for (int i = 0; i < 1000; i+=100) {

            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            Rect rectangle = new Rect(i, 500, i+100, 600);
            paint.setColor(color);

            activity.bufferCanvas.drawRect(rectangle, paint);
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
        coreSize = 8000;
        cycles = 100;
        rounds = 1;
        numWarriors = 2;

        /*
        for (int i=0; i<args.length; i++)
        {
            if (args[i].charAt(0) == '-')
            {
                if (args[i].equals("-r"))
                {
                    rounds = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-s"))
                {
                    coreSize = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-c"))
                {
                    cycles = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-p"))
                {
                    maxProc = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-l"))
                {
                    maxWarriorLength = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-d"))
                {
                    minWarriorDistance = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-S"))
                {
                    pSpaceSize = Integer.parseInt(args[++i]);
                    pspaceChanged = true;
                }
            } else
            {
                numWarriors++;

                wArgs.addElement(i);
            }
        }*/
        //numWarriors++;

        if (!pspaceChanged)
            pSpaceSize = coreSize / 16;

        if (numWarriors == 0)
            System.out.println("ERROR: no warrior files specified");

        warriors = new WarriorObj[numWarriors];

        String filenames[] = {"dwarf.red","imp2.red"};

        InputStream iS;

        //if (true)return;

        for (int i=0; i < numWarriors; i++)
        {
            try
            {
                InputStream inputStream = activity.getAssets().open(filenames[i]);

                Integer aColor = wColors[i % numDefinedColors][0];
                Integer dColor = wColors[i % numDefinedColors][1];
                //System.out.println("Warrior [" + i + "] colours are:" + aColor + " " + dColor );
                warriors[i] = new WarriorObj(inputStream, maxWarriorLength, aColor, dColor);
                warriors[i].initPSpace(pSpaceSize);
                warriors[i].setPCell(0, -1);

                System.out.println("Warrior ["+i+"] name = " + warriors[i].getName());
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        warriors[0].Alive = true;
        warriors[1].Alive = true;

        coreDisplay = activity.coreDisplay;

        //roundCycleCounter = new RoundCycleCounter(this, this);

        //validate();
        //repaint();
//		update(getGraphics());

        MARS = new MarsVM(coreSize, maxProc);

        loadWarriors();

        runWarriors = numWarriors;
        minWarriors = (numWarriors == 1) ? 0 : 1;
        roundNum = 0;
        cycleNum = 0;
        warRun = 0;

        //if (true) return;

    }
    public void startThread() {

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
        startTime = new Date();
        roundNum = 0;
        rounds = 1;

        for (; roundNum < rounds; roundNum++)
        {
            System.out.println("Round #"+roundNum);

            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable loop = new Runnable() {
                @Override
                public void run() {

                    for (; warRun < runWarriors; warRun++) {
                        //System.out.println("warRun=" + warRun);

                        StepReport stats = MARS.executeInstr();

                        WarriorObj war = stats.warrior();

                        war.numProc = stats.numProc();

                        if (stats.wDeath())
                        {
                            //System.out.println("Warrior["+warRun+"] died");
                            war.Alive = false;
                            runWarriors--;
                        }
                        notifyStepListeners(stats);
                    }

                    if (cycleNum < cycles) {
                        System.out.println("Cycle #"+cycleNum);
                        cycleNum++;
                        handler.postDelayed(this, 10);

                    } else {

                        endTime = new Date();
                        totalTime = ((double) endTime.getTime() - (double) startTime.getTime()) / 1000;
                        System.out.println("Total time="+ totalTime +" Cycles="+ cycleNum +" avg. time/cycle="+ (totalTime/cycleNum));
                        startTime = new Date();

                        MARS.reset();
                        loadWarriors();
                        runWarriors = numWarriors;
                        activity.coreDisplay.clear();

                        cycleNum = 0;
                    }
                }
            };
            handler.postDelayed(loop, 10);

            if (false == true) // remember to delete
            for (; cycleNum < cycles; cycleNum++) {
                for (; warRun < runWarriors; warRun++) {
                    //System.out.println("warRun=" + warRun);

                    StepReport stats = MARS.executeInstr();

                    WarriorObj war = stats.warrior();

                    war.numProc = stats.numProc();

                    if (stats.wDeath())
                    {
                        //System.out.println("Warrior["+warRun+"] died");
                        war.Alive = false;
                        runWarriors--;
                    }
                    notifyStepListeners(stats);
                }

                notifyCycleListeners(cycleNum);

                if (runWarriors <= minWarriors)
                    break;

                warRun = 0;

            }

            endTime = new Date();
            totalTime = ((double) endTime.getTime() - (double) startTime.getTime()) / 1000;
            System.out.println("Total time="+ totalTime +" Cycles="+ cycleNum +" avg. time/cycle="+ (totalTime/cycleNum));
            startTime = new Date();

            MARS.reset();
            loadWarriors();
            runWarriors = numWarriors;
            activity.coreDisplay.clear();

            cycleNum = 0;
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
            if (Active) j.stepProcess(step);
        }
    }

    private void notifyCycleListeners(int cycle) {
        for (Enumeration e = cycleListeners.elements(); e.hasMoreElements(); ) {
            CycleListener j = (CycleListener) e.nextElement();
            if (Active) j.cycleFinished(cycle);
        }
    }

    private void notifyRoundListeners(int round) {
        for (Enumeration e = roundListeners.elements(); e.hasMoreElements(); ) {
            RoundListener j = (RoundListener) e.nextElement();
            if (Active) j.roundResults(round);
        }
    }

    public void screenClose() {
        Active = false;
        myThread.interrupt();

    }

}