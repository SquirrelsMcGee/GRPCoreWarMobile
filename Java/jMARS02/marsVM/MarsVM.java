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
 * This class implements the virtual machine (MARS) for corewars
 */
 
package marsVM;

import frontend.StepReport;

public class MarsVM
{
	public Memory core[];
	protected WarriorRT currentW;		// current warrior
	int numWarriors;
	protected int maxProc;
	protected int coreSize;
	

	public MarsVM(int coreS, int maxP)
	{
		maxProc = maxP;
		coreSize = coreS;
		
		core = new Memory[coreSize];
		
		for (int i=0; i<core.length; i++)
		{
			core[i] = new Memory();
		}
		
	}
	
	public void reset()
	{
		currentW = null;
		numWarriors = 0;
		
		for (int i=0; i<core.length; i++)
		{
			core[i] = new Memory();
		}
	}
	
	/**
	 * Load a warrior into the core.
	 * @param WarriorObj warrior - warrior to load
	 * @param int startPosition - first memory location occupied by warrior
	 * @returns boolean - false if couldn't load warrior
	 */
	public boolean loadWarrior(WarriorObj warrior, int startPosition)
	{
		WarriorRT newWarrior;
		Memory wMemory[] = warrior.getMemory(coreSize);
		if ((startPosition + wMemory.length-1) > coreSize) return false;	// check that warrior fits in memory

		numWarriors++;
		
		for (int i=0; i<wMemory.length; i++)
			core[startPosition+i].copy(wMemory[i]);
		
		warrior.normalizePSpace(coreSize);
		newWarrior = new WarriorRT(warrior, startPosition+warrior.getOffset(), warrior.getPSpace());
		
		if (currentW == null)
		{
			currentW = newWarrior;
		} else {
			newWarrior.Insert(currentW, currentW.getNextWarrior());
		}
		
		return true;
	}


	/**
	 * get the warrior object of the current warrior
	 * @returns marsVM.WarriorObj - current warrior's object
	 */
	public WarriorObj getCurrentWarrior()
	{
		return currentW.warrior();
	}
	
	/**
	 * The heart of the VM it executes on instruction in core each time it is called
	 * @returns marsVM.StepReport - report of what happened this instuction.
	 */
	public StepReport executeInstr()
	{
		Memory instr = new Memory();	// copy of current instruction
		int tempAddr;		// temporary address for use in mode evaluation
		int addrA = 0;			// A's address
		Memory instrA = new Memory();
		int addrAAValue = 0;	// address A's A Value
		int addrABValue = 0;	// address B's B Value
		int addrB = 0;			// address B
		Memory instrB = new Memory();
		int addrBAValue = 0;	// address B's A Value
		int addrBBValue = 0;	// address B's B Value
		StepReport report = new StepReport();
		
		// get instruction pointer
		int IP = currentW.getProc();
		report.warrior(currentW.warrior());
		report.numProc(currentW.numProc());
		
		// Get a Pointer to the current instruction
		instr.copy(core[IP]);
		
		// evaluate A operand
		tempAddr = (instr.aValue + IP) % coreSize; // temporary address stuffed with the direct value to evaluate actions and help with indirect mode
		// do Pre timed actions
		if (instr.aTiming == instr.PRE && instr.aAction != instr.NONE)
		{
			if (instr.aAction == instr.DECREMENT)
			{
				if (instr.aTarget == instr.A)
				{
					if (--core[tempAddr].aValue < 0)
						core[tempAddr].aValue = coreSize - 1;
				} else {
					if (--core[tempAddr].bValue < 0)
						core[tempAddr].bValue = coreSize - 1;
				}
				report.decrement(tempAddr);
			} else {
				if (instr.aTarget == instr.A)
					core[tempAddr].aValue = (++core[tempAddr].aValue) % coreSize;
				else
					core[tempAddr].bValue = (++core[tempAddr].bValue) % coreSize;
				report.increment(tempAddr);
			}
		}
		
		// evaluate indirection
		switch (instr.aIndir)
		{
			case instr.IMMEDIATE:
				addrA = IP;
				instrA.copy(core[IP]);
				addrAAValue = instr.aValue;
				addrABValue = instr.bValue;
				break;
				
			case instr.DIRECT:
				addrA = (IP + instr.aValue) % coreSize;
				instrA.copy(core[(IP + instr.aValue) % coreSize]);
				addrAAValue = core[addrA].aValue;
				addrABValue = core[addrA].bValue;
				break;
				
			case instr.INDIRECT:
				if (instr.aTarget == instr.A)
				{
					addrA = (core[tempAddr].aValue + tempAddr) % coreSize;
				} else {
					addrA = (core[tempAddr].bValue + tempAddr) % coreSize;
				}

				instrA.copy(core[addrA]);
				addrAAValue = core[addrA].aValue;
				addrABValue = core[addrA].bValue;
				
				report.increment(tempAddr);
				break;
		}
		
		// do Post actions
		if (instr.aTiming == instr.POST && instr.aAction != instr.NONE)
		{
			if (instr.aAction == instr.DECREMENT)
			{
				if (instr.aTarget == instr.A)
				{
					if (--core[tempAddr].aValue < 0)
						core[tempAddr].aValue = coreSize - 1;
				} else {
					if (--core[tempAddr].bValue < 0)
						core[tempAddr].bValue = coreSize - 1;
				}
				report.decrement(tempAddr);
			} else {
				if (instr.aTarget == instr.A)
					core[tempAddr].aValue = (++core[tempAddr].aValue) % coreSize;
				else
					core[tempAddr].bValue = (++core[tempAddr].bValue) % coreSize;
				report.increment(tempAddr);
			}
		}
		
		// evaluate B operand
		tempAddr = (instr.bValue + IP) % coreSize; // temporary address stuffed with the direct value to evaluate actions and help with indirect mode
		// do Pre timed actions
		if (instr.bTiming == instr.PRE && instr.bAction != instr.NONE)
		{
			if (instr.bAction == instr.DECREMENT)
			{
				if (instr.bTarget == instr.A)
				{
					if (--core[tempAddr].aValue < 0)
						core[tempAddr].aValue = coreSize - 1;
				} else {
					if (--core[tempAddr].bValue < 0)
						core[tempAddr].bValue = coreSize - 1;
				}
				report.decrement(tempAddr);
			} else {
				if (instr.bTarget == instr.A)
					core[tempAddr].aValue = (++core[tempAddr].aValue) % coreSize;
				else
					core[tempAddr].bValue = (++core[tempAddr].bValue) % coreSize;
				report.increment(tempAddr);
			}
		}
		
		// evaluate indirection
		switch (instr.bIndir)
		{
			case instr.IMMEDIATE:
				addrB = IP;
				instrB.copy(core[IP]);
				addrBAValue = instr.aValue;
				addrBBValue = instr.bValue;
				break;
				
			case instr.DIRECT:
				addrB = (IP + instr.bValue) % coreSize;
				instrB.copy(core[(IP + instr.bValue) % coreSize]);
				addrBAValue = core[addrB].aValue;
				addrBBValue = core[addrB].bValue;
				break;
				
			case instr.INDIRECT:
				if (instr.bTarget == instr.A)
				{
					addrB = (core[tempAddr].aValue + tempAddr) % coreSize;
				} else {
					addrB = (core[tempAddr].bValue + tempAddr) % coreSize;
				}

				instrB.copy(core[addrB]);
				addrBAValue = core[addrB].aValue;
				addrBBValue = core[addrB].bValue;
				
				report.increment(tempAddr);

				break;
		}
		
		// do Post actions
		if (instr.bTiming == instr.POST && instr.bAction != instr.NONE)
		{
			if (instr.bAction == instr.DECREMENT)
			{
				if (instr.bTarget == instr.A)
				{
					if (--core[tempAddr].aValue < 0)
						core[tempAddr].aValue = coreSize - 1;
				} else {
					if (--core[tempAddr].bValue < 0)
						core[tempAddr].bValue = coreSize - 1;
				}
				report.decrement(tempAddr);
			} else {
				if (instr.bTarget == instr.A)
					core[tempAddr].aValue = (++core[tempAddr].aValue) % coreSize;
				else
					core[tempAddr].bValue = (++core[tempAddr].bValue) % coreSize;
				report.increment(tempAddr);
			}
		}
		
		// execute instruction
		report.execute(IP);
		switch (instr.opcode)
		{
			case instr.DAT:
			killProc(report);
			return report;

			case instr.MOV:
				switch (instr.modifier)
				{
					case instr.mI:
						core[addrB].copy(instrA);
						break;
					
					case instr.mA:
						core[addrB].aValue = addrAAValue;
						break;
					
					case instr.mF:
						core[addrB].aValue = addrAAValue;
						// fallthrough for rest
					case instr.mB:
						core[addrB].bValue = addrABValue;
						break;
					
					case instr.mAB:
						core[addrB].bValue = addrAAValue;
						break;
						
					case instr.mX:
						core[addrB].bValue = addrAAValue;
						// fallthrough for rest
					case instr.mBA:
						core[addrB].aValue = addrABValue;
						break;
				}
				report.read(addrA);
				report.write(addrB);
				break;
				
			case instr.ADD:
				switch (instr.modifier)
				{
					case instr.mA:
						core[addrB].aValue = (addrAAValue + addrBAValue) % coreSize;
						break;
						
					case instr.mI:
					case instr.mF:
						core[addrB].aValue = (addrAAValue + addrBAValue) % coreSize;
						// fallthrough for rest
					case instr.mB:
						core[addrB].bValue = (addrABValue + addrBBValue) % coreSize;
						break;
						
					case instr.mAB:
						core[addrB].bValue = (addrAAValue + addrBBValue) % coreSize;
						break;
						
					case instr.mX:
						core[addrB].bValue = (addrAAValue + addrBBValue) % coreSize;
						// fallthrough for rest
					case instr.mBA:
						core[addrB].aValue = (addrABValue + addrBAValue) % coreSize;
						break;
				}
				report.read(addrA);
				report.write(addrB);
				break;
				
			case instr.SUB:
				switch (instr.modifier)
				{
					case instr.mA:
						if ((core[addrB].aValue = addrBAValue - addrAAValue) < 0)
							core[addrB].aValue += coreSize;
						break;
						
					case instr.mI:
					case instr.mF:
						if ((core[addrB].aValue = addrBAValue - addrAAValue) < 0)
							core[addrB].aValue += coreSize;
						// fallthrough for rest
					case instr.mB:
						if ((core[addrB].bValue = addrBBValue - addrABValue) < 0)
							core[addrB].bValue += coreSize;
						break;
						
					case instr.mAB:
						if ((core[addrB].bValue = addrBBValue - addrAAValue) < 0)
							core[addrB].bValue += coreSize;
						break;
					
					case instr.mX:
						if ((core[addrB].bValue = addrBBValue - addrAAValue) < 0)
							core[addrB].aValue += coreSize;
						// falthrough for rest
					case instr.mBA:
						if ((core[addrB].aValue = addrBAValue - addrABValue) < 0)
							core[addrB].aValue += coreSize;
						break;
				}
				report.read(addrA);
				report.write(addrB);
				break;
			
			case instr.MUL:
				switch (instr.modifier)
				{
					// the cast prevents overflow, i hope ;)
					case instr.mA:
						core[addrB].aValue = (int) ((long) addrBAValue * addrAAValue % coreSize);
						break;
						
					case instr.mI:
					case instr.mF:
						core[addrB].aValue = (int) ((long) addrBAValue * addrAAValue % coreSize);
						// fallthrough for rest
					case instr.mB:
						core[addrB].bValue = (int) ((long) addrBBValue * addrABValue % coreSize);
						break;
						
					case instr.mAB:
						core[addrB].bValue = (int) ((long) addrBBValue * addrAAValue % coreSize);
						break;
						
					case instr.mX:
						core[addrB].bValue = (int) ((long) addrBBValue * addrAAValue % coreSize);
						// fallthrough for rest
					case instr.mBA:
						core[addrB].aValue = (int) ((long) addrBAValue * addrABValue % coreSize);
						break;
				}
				report.read(addrA);
				report.write(addrB);
				break;
				
			case instr.DIV:
				report.read(addrA);
				switch (instr.modifier)
				{
					case instr.mA:
						if (addrAAValue == 0)
						{
							killProc(report);
							return report;
						}
						core[addrB].aValue = addrBAValue / addrAAValue;
						break;
						
					case instr.mB:
						if (addrABValue == 0)
						{
							killProc(report);
							return report;
						}
						core[addrB].bValue = addrBBValue / addrABValue;
						break;
						
					case instr.mAB:
						if (addrAAValue == 0)
						{
							killProc(report);
							return report;
						}
						core[addrB].bValue = addrBBValue / addrAAValue;
						break;
						
					case instr.mBA:
						if (addrABValue == 0)
						{
							killProc(report);
							return report;
						}
						core[addrB].aValue = addrBAValue / addrABValue;
						break;
						
					case instr.mI:
					case instr.mF:
						if (addrAAValue != 0)
						{
							core[addrB].aValue = addrBAValue / addrAAValue;
							if (addrABValue == 0)
							{
								killProc(report);
								return report;
							}
							core[addrB].bValue = addrBBValue / addrABValue;
							break;
						} else {
							if (addrABValue == 0)
							{
								killProc(report);
								return report;
							}
							core[addrB].bValue = addrBBValue / addrABValue;
							report.write(addrB);
							killProc(report);
							return report;
						}
						
					case instr.mX:
						if (addrABValue != 0)
						{
							core[addrB].aValue = addrBAValue / addrABValue;
							if (addrAAValue == 0)
							{
								killProc(report);
								return report;
							}
							core[addrB].bValue = addrBBValue / addrAAValue;
							break;
						} else {
							if (addrAAValue == 0)
							{
								killProc(report);
								return report;
							}
							core[addrB].bValue = addrBBValue / addrAAValue;
							report.write(addrB);
							killProc(report);
							return report;
						}
				}
				report.write(addrB);
				break;
			
			case instr.MOD:
				report.read(addrA);
				switch (instr.modifier)
				{
					case instr.mA:
						if (addrAAValue == 0)
						{
							killProc(report);
							return report;
						}
						core[addrB].aValue = addrBAValue % addrAAValue;
						break;
						
					case instr.mB:
						if (addrABValue == 0)
						{
							killProc(report);
							return report;
						}
						core[addrB].bValue = addrBBValue % addrABValue;
						break;
						
					case instr.mAB:
						if (addrAAValue == 0)
						{
							killProc(report);
							return report;
						}
						core[addrB].bValue = addrBBValue % addrAAValue;
						break;
						
					case instr.mBA:
						if (addrABValue == 0)
						{
							killProc(report);
							return report;
						}
						core[addrB].aValue = addrBAValue % addrABValue;
						break;
						
					case instr.mI:
					case instr.mF:
						if (addrAAValue != 0)
						{
							core[addrB].aValue = addrBAValue % addrAAValue;
							if (addrABValue == 0)
							{
								killProc(report);
								return report;
							}
							core[addrB].bValue = addrBBValue % addrABValue;
							break;
						} else {
							if (addrABValue == 0)
							{
								killProc(report);
								return report;
							}
							core[addrB].bValue = addrBBValue % addrABValue;
							report.write(addrB);
							killProc(report);
							return report;
						}
						
					case instr.mX:
						if (addrABValue != 0)
						{
							core[addrB].aValue = addrBAValue % addrABValue;
							if (addrAAValue == 0)
							{
								killProc(report);
								return report;
							}
							core[addrB].bValue = addrBBValue % addrAAValue;
							break;
						} else {
							if (addrAAValue == 0)
							{
								killProc(report);
								return report;
							}
							core[addrB].bValue = addrBBValue % addrAAValue;
							report.write(addrB);
							killProc(report);
							return report;
						}
				}
				report.write(addrB);
				break;
				
			case instr.JMZ:
				report.read(addrB);
				switch (instr.modifier)
				{
					case instr.mA:
					case instr.mBA:
						if (addrBAValue != 0)
							break;
						currentW.addProc(addrA);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mF:
					case instr.mX:
					case instr.mI:
						if (addrBAValue != 0)
							break;
						// fallthrough
					case instr.mB:
					case instr.mAB:
						if (addrBBValue != 0)
							break;
						currentW.addProc(addrA);
						currentW = currentW.getNextWarrior();
						return report;
				}
				break;
				
			case instr.JMN:
				report.read(addrB);
				switch (instr.modifier)
				{
					case instr.mA:
					case instr.mBA:
						if (addrBAValue == 0)
							break;
						currentW.addProc(addrA);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mF:
					case instr.mX:
					case instr.mI:
						if ((addrBAValue == 0) && (addrBBValue == 0))
							break;
						currentW.addProc(addrA);
						break;
					
					case instr.mB:
					case instr.mAB:
						if (addrBBValue == 0)
							break;
						currentW.addProc(addrA);
						currentW = currentW.getNextWarrior();
						return report;
				}
				break;
				
			case instr.DJN:
				report.decrement(addrB);
				switch (instr.modifier)
				{
					case instr.mA:
					case instr.mBA:
						if (--core[addrB].aValue < 0)
							core[addrB].aValue = coreSize - 1;
						if (addrBAValue == 1)
							break;
						currentW.addProc(addrA);
						currentW = currentW.getNextWarrior();
						return report;

						
					case instr.mB:
					case instr.mAB:
						if (--core[addrB].bValue < 0)
							core[addrB].bValue = coreSize - 1;
						if (addrBBValue == 1)
							break;
						currentW.addProc(addrA);
						currentW = currentW.getNextWarrior();
						return report;

						
					case instr.mF:
					case instr.mI:
					case instr.mX:
						if (--core[addrB].bValue < 0)
							core[addrB].bValue = coreSize - 1;
						if (--core[addrB].aValue < 0)
							core[addrB].aValue = coreSize - 1;
						if ((addrBAValue == 1) && (addrBBValue == 1))
							break;
						currentW.addProc(addrA);
						currentW = currentW.getNextWarrior();
						return report;
				}
				break;
				
			case instr.SEQ:
				report.read(addrA);
				report.read(addrB);
				switch (instr.modifier)
				{
					case instr.mA:
						if (addrBAValue != addrAAValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mI:
						if (!core[addrB].equals(core[addrA]))
								break;
						// fallthrough for rest
					case instr.mF:
						if (addrBAValue != addrAAValue)
							break;
						// fallthrough for rest
					case instr.mB:
						if (addrBBValue != addrABValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mAB:
						if (addrBBValue != addrAAValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mX:
						if (addrBBValue != addrAAValue)
							break;
						// fallthrough for rest
					case instr.mBA:
						if (addrBAValue != addrABValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
				}
				break;
				
			case instr.SNE:
				report.read(addrA);
				report.read(addrB);
				switch (instr.modifier)
				{
					case instr.mA:
						if (addrBAValue == addrAAValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mI:
						if (core[addrB].equals(core[addrA]))
								break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mF:
						if ((addrBAValue == addrAAValue) &&
							(addrBBValue == addrABValue))
								break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;


					case instr.mB:
						if (addrBBValue == addrABValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mAB:
						if (addrBBValue == addrAAValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mX:
						if ((addrBBValue == addrAAValue) &&
							(addrBAValue == addrABValue))
								break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;

					case instr.mBA:
						if (addrBAValue == addrABValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
				}
				break;

			case instr.SLT:
				report.read(addrA);
				report.read(addrB);
				switch (instr.modifier)
				{
					case instr.mA:
						if (addrBAValue <= addrAAValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mF:
					case instr.mI:
						if (addrBAValue <= addrAAValue)
							break;
						// fallthrough for rest
					case instr.mB:
						if (addrBBValue <= addrABValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mAB:
						if (addrBBValue <= addrAAValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
						
					case instr.mX:
						if (addrBBValue <= addrAAValue)
							break;
						// fallthrough for rest
					case instr.mBA:
						if (addrBAValue <= addrABValue)
							break;
						currentW.addProc((IP + 2) % coreSize);
						currentW = currentW.getNextWarrior();
						return report;
				}
				break;
				
			case instr.JMP:
				currentW.addProc(addrA);
				currentW = currentW.getNextWarrior();
				return report;
				
			case instr.SPL:
				currentW.addProc((IP+1) % coreSize);
				if (currentW.numProc() >= maxProc)
				{
					currentW = currentW.getNextWarrior();
					return report;
				}
				currentW.addProc(addrA);
				report.numProc(currentW.numProc());
				currentW = currentW.getNextWarrior();
				return report;
				
			case instr.NOP:
				break;
				
			case instr.LDP:
				report.read(addrA);
				switch (instr.modifier)
				{
					case instr.mA:
						core[addrB].aValue = currentW.getPCell(addrAAValue);
						break;
						
					case instr.mF:
					case instr.mX:
					case instr.mI:
					case instr.mB:
						core[addrB].bValue = currentW.getPCell(addrABValue);
						break;
						
					case instr.mAB:
						core[addrB].bValue = currentW.getPCell(addrAAValue);
						break;
						
					case instr.mBA:
						core[addrB].aValue = currentW.getPCell(addrABValue);
						break;
				}
				report.write(addrB);
				break;
				
			case instr.STP:
				report.read(addrA);
				switch (instr.modifier)
				{
					case instr.mA:
						currentW.setPCell(addrBAValue, addrAAValue);
						break;
						
					case instr.mF:
					case instr.mX:
					case instr.mI:
					case instr.mB:
						currentW.setPCell(addrBBValue, addrABValue);
						break;
						
					case instr.mAB:
						currentW.setPCell(addrBBValue, addrAAValue);
						break;
						
					case instr.mBA:
						currentW.setPCell(addrBAValue, addrABValue);
						break;
				}
				break;
				
			default:
				return report;
				
		}
		
		// point the IP to the next instruction
		currentW.addProc((IP + 1) % coreSize);
		
		currentW = currentW.getNextWarrior();
		
		return report;
	}


	protected void killProc(StepReport report)
	{
		// delete the current process
		report.pDie();
		report.numProc(currentW.numProc());
		if (currentW.numProc() > 0)
		{
			currentW = currentW.getNextWarrior();
			return;
		}
		
		// else if that was the last process in that warrior kill it
		report.wDie();
		numWarriors--;
		currentW.setPCell(0, numWarriors);
		currentW = currentW.getNextWarrior();
		currentW.getPrevWarrior().Remove();
		return;
	}
}

