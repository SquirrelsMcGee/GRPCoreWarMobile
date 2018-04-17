/**
 * Created by james on 17/04/2018.
 */
import marsVM.*;
import frontend.*;
import java.util.*;
import android.graphics.*;

public class jMARS implements Runnable, FrontEndManager {

    // constants
    static final int numDefinedColors = 4;
    static final int wColors[][] = {{Color.GREEN, Color.YELLOW},
            {Color.RED, Color.MAGENTA},
            {Color.CYAN, Color.BLUE},
            {Color.GRAY, Color.DKGRAY}};

    // static variables
    static boolean inApplet = true;

    // Application specific variables
    String args[];
    //static Frame myFrame; // Unimplemented
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

    public jMARS()
    {
        stepListeners = new Vector();
        cycleListeners = new Vector();
        roundListeners = new Vector();
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
