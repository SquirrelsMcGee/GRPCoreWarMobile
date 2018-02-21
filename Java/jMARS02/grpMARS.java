import marsVM.*;

public class grpMARS {
	
	/* Constant values */
	static final int numberOfColours = 4;
	
	static final Color warriorColors[][] = {
		{Color.green, Color.yellow},
		{Color.red, Color.magenta},
		{Color.cyan, Color.blue},
		{Color.gray, Color.darkGray}
	};
	
	String args[];
	//static grpMARS application;
	
	// Common variables
	int maxProcesses;
	int pSpaceSize;
	int coreSize;
	int numCycles;
	int numRounds;
	int maxWarriorLength;
	int minWarriorDistance;
	int numWarriors;
	int minWarriors;
	
	WarriorObj warriors[];
	RoundCycleCounter roundCycleCounter;
	MarsVM MARS;
	
	int roundNum;
	int cycleNum;
	int warRun;
	int runWarriors;
	
	Vector stepListeners;
	Vector cycleListeners;
	Vector roundListeners;

	Date startTime;
	Date endTime;
	double totalTime;
	
	/**
	 * Constructor
	 * @param String[] a - array of "command line" arguments
	 * 					   used for changing how the MARS is run, including warrior files
	 * @return -1 if empty string
	 *
	 * example [options] warrior1.red [warrior2.red ...]
	 */
	public int grpMARS(String a[]) {
		stepListeners = new Vector();
		cycleListeners = new Vector();
		roundListeners = new Vector();
		
		if (a.length == 0) {
			return -1;
		}
		
		initialise();
	}
	
	private void initialise() {
		
		boolean pspaceChanged = false;
		Vector wArgs = new Vector();

		// Set defaults for various constants
		maxWarriorLength = 100;
		minWarriorDistance = 100;
		maxProc = 8000;
		coreSize = 8000;
		cycles = 80000;
		rounds = 10;
		
		for (int i=0; i< args.length; i++)
		{
			if (args[i].charAt(0) == '-')
			{
				if (args[i].equals("-r"))
				{
					numRounds = Integer.parseInt(args[++i]);
				} else if (args[i].equals("-s"))
				{
					coreSize = Integer.parseInt(args[++i]);
				} else if (args[i].equals("-c"))
				{
					numCycles = Integer.parseInt(args[++i]);
				} else if (args[i].equals("-p"))
				{
					maxProcesses = Integer.parseInt(args[++i]);
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
			} else {
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
		
		//roundCycleCounter = new RoundCycleCounter(this, this);
		
		//validate();
		//repaint();
		
		MARS = new MarsVM(coreSize, maxProc);
		
		loadWarriors();
		
		runWarriors = numWarriors;
		minWarriors = (numWarriors == 1) ? 0 : 1;
		roundNum = 0;
		cycleNum = 0;
		warRun = 0;

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
}