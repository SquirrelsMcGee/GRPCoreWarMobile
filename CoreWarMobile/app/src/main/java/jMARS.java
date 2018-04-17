/**
 * Created by james on 17/04/2018.
 */
import marsVM.*;
import frontend.*;
import java.util.*;
import android.graphics.*;
import android.view.*;

import java.io.*;

public class jMARS implements Runnable, FrontEndManager {

    // constants
    static final int numDefinedColors = 4;
    static final int wColors[][] = {
            {Color.GREEN, Color.YELLOW},
            {Color.RED, Color.MAGENTA},
            {Color.CYAN, Color.BLUE},
            {Color.GRAY, Color.DKGRAY}};

    // static variables
    static boolean inApplet = true;

    // Application specific variables
    String args[];
    //static Frame myFrame; // Unimplemented

    SurfaceView surfaceView;
    Canvas frameCanvas;

    static jMARS myApp;

    // Common variables
    int maxProc;
    int pSpaceSize;
    int coreSize;
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

    Vector stepListeners;
    Vector cycleListeners;
    Vector roundListeners;

    public jMARS(SurfaceHolder arg_holder, SurfaceView arg_surfaceView)
    {
        stepListeners = new Vector();
        cycleListeners = new Vector();
        roundListeners = new Vector();

        //frameCanvas = bufferCanvas; // Updated to use Android canvas
    }

    void application_init()
    {
        boolean pspaceChanged = false;
        Vector wArgs = new Vector();

        // Set defaults for various constants
        maxWarriorLength = 100;
        minWarriorDistance = 100;
        maxProc = 8000;
        coreSize = 8000;
        cycles = 80000;
        rounds = 10;

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

                wArgs.addElement(new Integer(i));
            }
        }

        if (!pspaceChanged)
            pSpaceSize = coreSize / 16;

        if (numWarriors == 0)
            System.out.println("ERROR: no warrior files specified");

        warriors = new WarriorObj[numWarriors];

        for (int i=0; i<numWarriors; i++)
        {
            try
            {
                FileReader wFile = new FileReader(args[(((Integer) wArgs.elementAt(i)).intValue())]);
                warriors[i] = new WarriorObj(wFile, maxWarriorLength, wColors[i % numDefinedColors][0], wColors[i % numDefinedColors][1]);
                warriors[i].initPSpace(pSpaceSize);
                warriors[i].setPCell(0, -1);
            } catch (FileNotFoundException e)
            {
                System.out.println("Could not find warrior file " + args[i]);
                System.exit(0);
            }
        }

        coreDisplay = new CoreDisplay(this, surfaceView, coreSize, 100, 100);
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

        myThread = new Thread(this);

        myThread.setPriority(Thread.NORM_PRIORITY-1);

        myThread.start();
        return;
    }

    public void run() {

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

    protected void notifyStepListeners(StepReport step) {
        for (Enumeration e = stepListeners.elements(); e.hasMoreElements(); ) {
            StepListener j = (StepListener) e.nextElement();
            j.stepProcess(step);
        }
        return;
    }

    protected void notifyCycleListeners(int cycle) {
        for (Enumeration e = cycleListeners.elements(); e.hasMoreElements(); ) {
            CycleListener j = (CycleListener) e.nextElement();
            j.cycleFinished(cycle);
        }
        return;
    }

    protected void notifyRoundListeners(int round) {
        for (Enumeration e = roundListeners.elements(); e.hasMoreElements(); ) {
            RoundListener j = (RoundListener) e.nextElement();
            j.roundResults(round);
        }
        return;
    }

}
