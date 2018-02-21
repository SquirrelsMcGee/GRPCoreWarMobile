/*-
 * Copyright (c) 1998 Brian Haskin jr.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */

/**
 * jMARS is a corewars interpreter in which programs (warriors)
 * battle in the memory of a virtual machine (the MARS) and try to disable
 * the other program.
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.URL;
import java.io.*;

import marsVM.*;
import frontend.*;

public class jMARS extends java.applet.Applet implements Runnable, WindowListener, FrontEndManager
{
	// constants
	static final int numDefinedColors = 4;
	static final Color wColors[][] = {{Color.green, Color.yellow},
									   {Color.red, Color.magenta},
									   {Color.cyan, Color.blue},
									   {Color.gray, Color.darkGray}};

	// static variables
	static boolean inApplet = true;
	
	// Application specific variables
	String args[];
	static Frame myFrame;
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
	RoundCycleCounter roundCycleCounter;
	MarsVM MARS;
	
	int roundNum;
	int cycleNum;
	int warRun;
	int runWarriors;
	
	static Thread myThread;
	
	Vector stepListeners;
	Vector cycleListeners;
	Vector roundListeners;

	Date startTime;
	Date endTime;
	double totalTime;
	
	public jMARS()
	{
		stepListeners = new Vector();
		cycleListeners = new Vector();
		roundListeners = new Vector();
	}
	
	/**
	 * Starting function for the application. It sets up a frame and adds the applet to it.
	 * @param java.lang.String[] a - array of command line arguments
	 */
	public static void main(String a[])
	{
		if (a.length == 0)
		{
			System.out.println("usage: jMARS [options] warrior1.red [warrior2.red ...]");
			return;
		}
		
		inApplet = false;
		
		myFrame = new Frame("jMARS");
		myFrame.setSize(new Dimension(500, 300));
		
		myApp = new jMARS();
		myApp.args = a;
		
		myFrame.add(myApp);
		myFrame.addWindowListener(myApp);
		myFrame.show();
		
		myApp.application_init();
		
	}
	
	/**
	 * Initialization function for the applet, this automatically called by the browser.
	 */
	public void init()
	{
		// Set up various constants
		String temp;
		if ((temp = getParameter("max_length")) != null)
		{
			maxWarriorLength = Integer.parseInt(temp);
		} else {
			maxWarriorLength = 100;
		}
		
		if ((temp = getParameter("min_distance")) != null)
		{
			minWarriorDistance = Integer.parseInt(temp);
		} else {
			minWarriorDistance = 100;
		}
		
		if ((temp = getParameter("max_proc")) != null)
		{
			maxProc = Integer.parseInt(temp);
		} else {
			maxProc = 8000;
		}
		
		if ((temp = getParameter("core_size")) != null)
		{
			coreSize = Integer.parseInt(temp);
		} else {
			coreSize = 8000;
		}
		
		if ((temp = getParameter("pspace_size")) != null)
		{
			pSpaceSize = Integer.parseInt(temp);
		} else {
			pSpaceSize = coreSize / 16;
		}
		
		if ((temp = getParameter("num_cycles")) != null)
		{
			cycles = Integer.parseInt(temp);
		} else {
			cycles = 80000;
		}
		
		if ((temp = getParameter("num_rounds")) != null)
		{
			rounds = Integer.parseInt(temp);
		} else {
			rounds = 10;
		}
		
		if ((temp = getParameter("num_warriors")) != null)
		{
			numWarriors = Integer.parseInt(temp);
		} else {
			System.out.println("Number of warriors not specified");
			return;
		}
		
		warriors = new WarriorObj[numWarriors];
		
		for (int i=0; i<numWarriors; i++)
		{
			try
			{
				URL wURL = new URL(getCodeBase(), getParameter("warrior" + (i+1)));
				BufferedReader warRead = new BufferedReader(new InputStreamReader(wURL.openStream()));

				warriors[i] = new WarriorObj(warRead, maxWarriorLength, wColors[i % numDefinedColors][0], wColors[i % numDefinedColors][1]);
				warriors[i].initPSpace(pSpaceSize);
				warriors[i].setPCell(0, -1);
			} catch (IOException e)
			{
				System.out.println(e.toString());
				System.exit(0);
			}
		}
				
		coreDisplay = new CoreDisplay(this, this, coreSize, 402);
		roundCycleCounter = new RoundCycleCounter(this, this);
		
		validate();
		repaint();
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
	
	
	/**
	 * called by browser to shutdown the applet
	 */
	public void destroy()
	{
		myThread.stop();
	}
	
	/**
	 * Initialization function for the application.
	 */
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
				
		coreDisplay = new CoreDisplay(this, this, coreSize, 402);
		roundCycleCounter = new RoundCycleCounter(this, this);
		
		validate();
		repaint();
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
	
	/**
	 * main function and loop for jMARS. Runs the battles and handles display.
	 */
	public void run()
	{
		startTime = new Date();

		for (; roundNum<rounds; roundNum++)
		{
			for (; cycleNum<cycles; cycleNum++)
			{
				for (; warRun < runWarriors; warRun++)
				{
					StepReport stats = MARS.executeInstr();
					
					WarriorObj war = stats.warrior();
					war.numProc = stats.numProc();
									
					if (stats.wDeath())
					{
						war.Alive = false;
						runWarriors--;
					}
					
					notifyStepListeners(stats); 
					
				}
				
				notifyCycleListeners(cycleNum);
				repaint();
				
				if (runWarriors <= minWarriors)
					break;
					
				warRun = 0;

			}
			
			notifyRoundListeners(roundNum);
			
			endTime = new Date();
			totalTime = ((double) endTime.getTime() - (double) startTime.getTime()) / 1000;
			System.out.println("Total time="+ totalTime +" Cycles="+ cycleNum +" avg. time/cycle="+ (totalTime/cycleNum));
			startTime = new Date();
			
			MARS.reset();
			loadWarriors();
			runWarriors = numWarriors;
			coreDisplay.clear();
			
			
			cycleNum = 0;
		}
	}
	
	/**
	 * Load warriors into core
	 */
	void loadWarriors()
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
	
	/**
	 * update the display
	 * @param java.awt.Graphics g - Graphics context
	 */
	public void update(Graphics g)
	{
		paintComponents(g);
		
		return;
	}
	
	public void registerStepListener(StepListener l)
	{
		stepListeners.addElement(l);
	}
	
	public void registerCycleListener(CycleListener c)
	{
		cycleListeners.addElement(c);
	}
	
	public void registerRoundListener(RoundListener r)
	{
		roundListeners.addElement(r);
	}
	
	protected void notifyStepListeners(StepReport step)
	{
		for (Enumeration e = stepListeners.elements(); e.hasMoreElements(); )
		{
			StepListener j = (StepListener) e.nextElement();
			j.stepProcess(step);
		}
		
		return;
	}
	
	protected void notifyCycleListeners(int cycle)
	{
		for (Enumeration e = cycleListeners.elements(); e.hasMoreElements(); )
		{
			CycleListener j = (CycleListener) e.nextElement();
			j.cycleFinished(cycle);
		}
		
		return;
	}

	protected void notifyRoundListeners(int round)
	{
		for (Enumeration e = roundListeners.elements(); e.hasMoreElements(); )
		{
			RoundListener j = (RoundListener) e.nextElement();
			j.roundResults(round);
		}
		
		return;
	}


	
    /**
     * Invoked when a window is in the process of being closed.
     * The close operation can be overridden at this point.
     */
	public void windowClosing(WindowEvent e)
	{
		myApp.stop();
		System.exit(0);
	} 

    /**
     * Invoked when a window has been opened.
     */
    public void windowOpened(WindowEvent e)
    {
    	
    }

    /**
     * Invoked when a window has been closed.
     */
    public void windowClosed(WindowEvent e)
    {

    }

    /**
     * Invoked when a window is iconified.
     */
    public void windowIconified(WindowEvent e)
    {

    }

    /**
     * Invoked when a window is de-iconified.
     */
    public void windowDeiconified(WindowEvent e)
    {

    }

    /**
     * Invoked when a window is activated.
     */
    public void windowActivated(WindowEvent e)
	{
		
	}

    /**
     * Invoked when a window is de-activated.
     */
    public void windowDeactivated(WindowEvent e)
    {
    	
    }	
}

