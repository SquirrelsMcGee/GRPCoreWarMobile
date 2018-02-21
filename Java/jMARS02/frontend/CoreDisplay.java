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

package frontend;

import java.awt.*;

/**
 * A pMARS style core display
 */
public class CoreDisplay extends Canvas implements StepListener
{
	/* core background color */
	protected Color background;
	
	/* size of core */
	protected int coreSize;

	protected int width;
	protected int height;
	
	protected Image offScreen;
	protected Graphics buffer;
	
	/**
	 * Create a new core display for a specified core size and width.
	 * @param FrontEndManager man - Object managing the front end components.
	 * @param Container con - Display container for this component.
	 * @param int coreS - Size of core to be displayed.
	 * @param int w - desired width of display.
	 */
	public CoreDisplay(FrontEndManager man, Container con, int coreS, int w)
	{
		coreSize = coreS;
		width = w;
		height = ((coreSize / (width /3)) +1) *3;
		
		background = Color.black;
		
		man.registerStepListener(this);
		con.add(this);
		System.out.println("Constructor end size=" + getSize());
	}
	
	/**
	 * Update display with info from a round.
	 * @param marsVM.StatReport report - info from round
	 */
	public void stepProcess(StepReport report)
	{
		int i;
		int x,y;
		int addr[];
		
		if (offScreen == null)
			return;

		buffer.setColor(report.warrior().getColor());
		
		addr = report.addrRead();
		for (i=0; i < addr.length; i++)
		{
			y = (addr[i] / (width /3)) * 3;
			x = (addr[i] % (width /3)) * 3;
			
			buffer.drawLine(x, y, x, y);
		}
		
		addr = report.addrWrite();
		for (i=0; i < addr.length; i++)
		{
			y = (addr[i] / (width /3)) * 3;
			x = (addr[i] % (width /3)) * 3;
			
			buffer.drawLine(x+1, y, x, y+1);
		}

		addr = report.addrDec();
		for (i=0; i < addr.length; i++)
		{
			y = (addr[i] / (width /3)) * 3;
			x = (addr[i] % (width /3)) * 3;
			
			buffer.drawLine(x, y, x+1, y);
		}
		
		addr = report.addrInc();
		for (i=0; i < addr.length; i++)
		{
			y = (addr[i] / (width /3)) * 3;
			x = (addr[i] % (width /3)) * 3;
			
			buffer.drawLine(x, y, x+1, y);
		}
		
		if ((i = report.addrExec()) != -1)
		{
			y = (i / (width /3)) * 3;
			x = (i % (width /3)) * 3;
			
			if (report.pDeath()) buffer.setColor(report.warrior().getDColor());
			buffer.drawLine(x, y, x+1, y);
			buffer.drawLine(x, y+1, x+1, y+1);
		}
		
		return;
	}
	
	/**
	 * clear the display
	 */
	public void clear()
	{
		if (offScreen == null)
			return;
		
		buffer.setColor(background);
		buffer.fillRect(0, 0, width, height);
	}

	/**
	 * paint the display on the screen
	 * @param java.awt.Graphics screen - Graphics context to paint to
	 */
	public void paint(Graphics screen)
	{
		if (offScreen == null)
		{
			offScreen = createImage(width, height);
			buffer = offScreen.getGraphics();
			buffer.setColor(background);
			buffer.fillRect(0, 0, width, height);
		}


		screen.drawImage(offScreen, 0, 0, this);
		
		System.out.println("paint size =" + getSize());
		return;
	}
	
	/**
	 * Get the maximum size for the display
	 * @return java.awt.Dimension - maximum size
	 */
	public Dimension getMaximumSize()
	{
		return new Dimension(width, height);
	}
	
	/**
	 * Get the preffered size for the display
	 * @return java.awt.Dimension - preferred size
	 */
	public Dimension getPreferredSize()
	{
		return new Dimension(width, height);
	}
	
	/**
	 * Get the minimum size for the display
	 * @return java.awt.Dimension - minimum size
	 */
	public Dimension getMinimumSize()
	{
		return new Dimension(width, height);
	}
	
	/**
	 * Get X alignment the display wants in the layout. Asks for a center alignment.
	 * @return float - X alignment (0.5)
	 */
	public float getAlignmentX()
	{
		return 0.5F;
	}
	
	/**
	 * Get Y alignment the display wants in the layout. Asks for a center alignment.
	 * @return float - Y alignment (0.5)
	 */
	public float getAlignmentY()
	{
		return 0.5F;
	}
}
