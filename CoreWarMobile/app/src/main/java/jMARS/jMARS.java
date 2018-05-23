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
    static final int numDefinedColors = 4; // Number of colour groups

    static final int wColors[][] = { // Colour groups for each warrior
            {Color.GREEN, Color.YELLOW},
            {Color.RED, Color.MAGENTA},
            {Color.CYAN, Color.BLUE},
            {Color.GRAY, Color.DKGRAY}};


    public boolean Active = false; // If the game is running and has not finished
    public boolean Paused = false; // if the game is running, has not finished, and is paused
    public boolean roundOver = false; // if the round is over

    // Common variables
    int maxProc; // maximum process
    int pSpaceSize; //program space size
    public int coreSize; // core size (determines how long the array is)
    int cycles; // number of cycles to run in each round
    int rounds; // number of rounds to play
    int maxWarriorLength;
    int minWarriorDistance;
    int numWarriors; // number of active warriors
    int minWarriors; // minimum number of active warriors before forceful end

    WarriorObj warriors[]; // array containing warrior objects

    GameActivity activity; // reference to the GameActivity, for data access and screen drawing
    MarsVM MARS; // reference to the virtual machine class

    CoreDisplay coreDisplay; // reference to front end class
    RoundCycleCounter roundCycleCounter; // Unimplemented

    int roundNum; // current round number
    int cycleNum; // current cycle number
    int warRun; // current warrior within cycle
    int runWarriors; // number of warriors to run

    static Thread myThread; // self thread
    Handler handler; // handler for screen drawing/cycling processes

    Vector<StepListener> stepListeners;
    Vector<CycleListener> cycleListeners;
    Vector<RoundListener> roundListeners;

    Date startTime;
    Date endTime;
    double totalTime;

    Paint paint = new Paint();

    // constructor
    public jMARS(GameActivity superActivity)
    {
        activity = superActivity; // set a reference to the GameActivity this was created from

        stepListeners = new Vector<>();
        cycleListeners = new Vector<>();
        roundListeners = new Vector<>();

    }

    public void application_init()
    {
        boolean pspaceChanged = false;

        // Set defaults for various constants
        maxWarriorLength = 100;
        minWarriorDistance = 100;
        maxProc = 8000;
        coreSize = 8000;
        cycles = 8000;
        rounds = 1;
        numWarriors = 2;


        if (!pspaceChanged)
            pSpaceSize = coreSize / 16;

        if (numWarriors == 0)
            System.out.println("ERROR: no warrior files specified");

        warriors = new WarriorObj[numWarriors];

        //String filenames[] = {GameActivity.WarriorName,GameActivity.WarriorName2};
        String filenames[] = {"clp.red", "dwarf.red", "ElectricHead.red", "imp.red", "imp2.red", "rave.red", "rose.red", "twister.red"};


        for (int i=0; i < numWarriors; i++)
        {
            try
            {
                Random r = new Random();
                int randomIndex = r.nextInt(filenames.length);
                int index = randomIndex;

                //File file = new File(activity.getFilesDir(), filenames[index]);
                InputStream inputStream = activity.getAssets().open(filenames[index]);


                Integer aColor = wColors[i % numDefinedColors][0];
                Integer dColor = wColors[i % numDefinedColors][1];

                warriors[i] = new WarriorObj(inputStream, maxWarriorLength, aColor, dColor);
                warriors[i].initPSpace(pSpaceSize);
                warriors[i].setPCell(0, -1);

                System.out.println("Warrior ["+i+"] name = " + warriors[i].getName());

                if (warriors[i].getName() ==  null) warriors[i].setName(filenames[index]);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        activity.WarriorName = "Warrior 1: \n" + warriors[0].getName();
        activity.WarriorName2 = "Warrior 2: \n" + warriors[1].getName();

        activity.TextChanger(activity.WarriorName, activity.WarriorName2);

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
                        activity.progress.setText("Cycle: " + cycleNum + "/" +cycles);
                        handler.postDelayed(this, delayMillis);

                    } else {
                        if (Active && !Paused) {
                            //notifyRoundListeners(roundNum);
                            activity.progress.setText("Finished");

                            if (activity.coreCanvas == null) {
                                System.out.println("For some reason coreCanvas is null");
                                return;
                            }

                            endTime = new Date();
                            totalTime = ((double) endTime.getTime() - (double) startTime.getTime()) / 1000;
                            System.out.println("Total time=" + totalTime + " Cycles=" + cycleNum + " avg. time/cycle=" + (totalTime / cycleNum));


                            paint.setTextSize(40);


                            /* Draw final information to the canvas */
                            activity.coreCanvas = activity.surfaceHolder.lockCanvas();

                            paint.setColor(Color.BLACK);
                            activity.bufferCanvas.drawRect(0, ((activity.coreCanvas.getHeight() / 3)), activity.coreCanvas.getWidth(), 100 + activity.coreCanvas.getHeight() / 2, paint);

                            paint.setColor(Color.WHITE);
                            activity.bufferCanvas.drawText("Total time=" + totalTime + " Cycles=" + cycleNum + " avg. time/cycle=" + (totalTime / cycleNum),
                                    10, -100 + activity.coreCanvas.getHeight() / 2, paint);

                            activity.bufferCanvas.drawText(warriors[0].getName() + " - Alive=" +warriors[0].Alive,
                                    100, -40 + activity.coreCanvas.getHeight() / 2, paint);
                            activity.bufferCanvas.drawText(warriors[1].getName() + " - Alive=" +warriors[1].Alive,
                                    100, activity.coreCanvas.getHeight() / 2, paint);

                            paint.setColor(Color.LTGRAY);
                            activity.bufferCanvas.drawText("Total time=" + totalTime + " Cycles=" + cycleNum + " avg. time/cycle=" + (totalTime / cycleNum),
                                    12, -98 + activity.coreCanvas.getHeight() / 2, paint);

                            activity.bufferCanvas.drawText(warriors[0].getName() + " - Alive=" +warriors[0].Alive,
                                    102, -38 + activity.coreCanvas.getHeight() / 2, paint);
                            activity.bufferCanvas.drawText(warriors[1].getName() + " - Alive=" +warriors[1].Alive,
                                    102, 2+ activity.coreCanvas.getHeight() / 2, paint);


                            activity.coreCanvas.drawBitmap(activity.bmp, activity.identityMatrix, null);
                            activity.surfaceHolder.unlockCanvasAndPost(activity.coreCanvas);

                            startTime = new Date(); // used for calculating run time

                            MARS.reset();
                            loadWarriors();
                            runWarriors = numWarriors;

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

                if (r < minWarriorDistance || r > (coreSize - minWarriorDistance))
                    validSpot = false;

                for (int j=0; j<location.length; j++)
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
        //System.out.println("handler exists" + (handler != null) + " thread exists" + (myThread != null));
        if (myThread != null) myThread.interrupt();
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }

    public void togglePause() {
        Button runButton = activity.findViewById(R.id.runButton);
        if (cycleNum == cycles) return;
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
