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

import android.annotation.TargetApi;
import android.graphics.*;
import android.os.Build;
import android.view.SurfaceView;

import com.corewarmobile.corewarmobile.GameActivity;

/**
 * A pMARS style core display
 */
public class CoreDisplay extends Canvas implements StepListener
{
	/* core background color */
	//protected Color background;
	protected int backgroundInt;
	
	/* size of core */
	protected int coreSize;

	protected int width;
	protected int height;
	
	//protected Image offScreen;
	//protected Graphics buffer;

	GameActivity activity;
	protected Paint paint = new Paint();
	protected int aliveColor;
	protected int deathColor;

	protected int colorInt;
	protected int dcolorInt;
	
	/**
	 * Create a new core display for a specified core size and width.
	 * //@param FrontEndManager man - Object managing the front end components.
	 * //@param SurfaceView surface - Surface to draw on
	 * //@param int coreS - Size of core to be displayed.
	 * //@param int w - desired width of display.
	 */
	public CoreDisplay(GameActivity superActivity, FrontEndManager man, SurfaceView surface, int coreS, int w, int h)
	{
		activity = superActivity;

		if (activity == null) {
			System.out.println("Failed to get activity");
		}
		coreSize = coreS;
		width = w;
		height = h;
		
		backgroundInt = Color.WHITE;

		man.registerStepListener(this);
	}
	
	/**
	 * Update display with info from a round.
	 * //@param marsVM.StatReport report - info from round
	 */
	@TargetApi(Build.VERSION_CODES.O)
	public void stepProcess(StepReport report)
	{
		int i;
		int x = 0, y = 0;
		int addr[];

		aliveColor = report.warrior().getColor();
		deathColor = report.warrior().getDColor();

		System.out.printf("[Step Report] Alive colour=%d  Death Color=%d \n ",aliveColor, deathColor);

		paint.setColor(aliveColor);
		paint.setStrokeWidth(10);

		//activity.coreDisplay

		addr = report.addrRead();
		//System.out.printf("[Step Process] [addrRead] Address Length: %d", addr.length);

		for (i=0; i < addr.length; i++)
		{
			y = (addr[i] / (width /3)) * 3;
			x = (addr[i] % (width /3)) * 3;

			activity.bufferCanvas.drawLine(x, y, x, y, paint);
		}
		
		addr = report.addrWrite();
		//System.out.printf("[Step Process] [addrWrite] Address Length: %d", addr.length);

		for (i=0; i < addr.length; i++)
		{
			y = (addr[i] / (width /3)) * 3;
			x = (addr[i] % (width /3)) * 3;

			activity.bufferCanvas.drawLine(x+1, y, x, y+1, paint);
		}

		addr = report.addrDec();
		//System.out.printf("[Step Process] [addrDec] Address Length: %d", addr.length);

		for (i=0; i < addr.length; i++)
		{
			y = (addr[i] / (width /3)) * 3;
			x = (addr[i] % (width /3)) * 3;

			activity.bufferCanvas.drawLine(x, y, x+1, y, paint);
		}
		
		addr = report.addrInc();
		//System.out.printf("[Step Process] [addrInc] Address Length: %d", addr.length);

		for (i=0; i < addr.length; i++)
		{
			y = (addr[i] / (width /3)) * 3;
			x = (addr[i] % (width /3)) * 3;

			activity.bufferCanvas.drawLine(x, y, x+1, y, paint);
		}

		System.out.printf("[Step Process] x=%d y=%d\n", x, y);



		if ((i = report.addrExec()) != -1)
		{
			y = (i / (width /3)) * 3;
			x = (i % (width /3)) * 3;
			
			if (report.pDeath()) paint.setColor(deathColor);
			activity.bufferCanvas.drawLine(x, y, x+1, y, paint);
			activity.bufferCanvas.drawLine(x, y+1, x+1, y+1, paint);
		}
		if (activity == null) System.out.println("Error: Activity is null");
		if (activity.coreCanvas == null) System.out.println("Error: Activity.coreCanvas is null");

		paint.setColor(aliveColor);


		activity.bufferCanvas.drawRect(x, y, x+10, y+10, paint);

		activity.coreCanvas = activity.surfaceHolder.lockCanvas();

		if (activity != null && activity.coreCanvas != null) {
			activity.coreCanvas.drawBitmap(activity.bmp, activity.identityMatrix, null);
			System.out.println("[Graphics Update] Updated Canvas");
		}

		try {
			activity.surfaceHolder.unlockCanvasAndPost(activity.coreCanvas);
		} catch (Exception e) {
			System.out.println("Error: Canvas Already unlocked");
		}

		return;
	}
	
	/**
	 * clear the display
	 */
	public void clear()
	{
		//if (offScreen == null)
		//	return;
		
		//buffer.setColor(background);
		paint.setColor(backgroundInt);

		activity.coreCanvas = activity.surfaceHolder.lockCanvas();

		activity.bufferCanvas.drawRect(0, 0, width, height, paint);

		if (activity != null && activity.coreCanvas != null)
			activity.coreCanvas.drawBitmap(activity.bmp, activity.identityMatrix, null);

		try {
			activity.surfaceHolder.unlockCanvasAndPost(activity.coreCanvas);
		} catch (Exception e) {
			System.out.println("Already released");
		}
	}

	/**
	 * paint the display on the screen
	 * @param android.graphics screen - Graphics context to paint to
	 */
	/*
	public void paint(Graphics screen)
	{
		if (offScreen == null)
		{
			offScreen = createImage(width, height);
			buffer = offScreen.getGraphics();
			buffer.setColor(background);
			buffer.fillRect(0, 0, width, height);
		}


		//screen.drawImage(offScreen, 0, 0, this);
		
		System.out.println("paint size =" + getSize());
		return;
	}
	*/

	/**
	 * Get the maximum size for the display
	 * @return java.awt.Dimension - maximum size
	 */
	/*
	public Dimension getMaximumSize()
	{
		return new Dimension(width, height);
	}
	*/
	/**
	 * Get the preffered size for the display
	 * @return java.awt.Dimension - preferred size
	 */
	/*
	public Dimension getPreferredSize()
	{
		return new Dimension(width, height);
	}
	*/
	/**
	 * Get the minimum size for the display
	 * @return java.awt.Dimension - minimum size
	 */
	/*
	public Dimension getMinimumSize()
	{
		return new Dimension(width, height);
	}
	*/
	/**
	 * Get X alignment the display wants in the layout. Asks for a center alignment.
	 * @return float - X alignment (0.5)
	 */
	/*
	public float getAlignmentX()
	{
		return 0.5F;
	}
	*/
	/**
	 * Get Y alignment the display wants in the layout. Asks for a center alignment.
	 * @return float - Y alignment (0.5)
	 */
	/*
	public float getAlignmentY()
	{
		return 0.5F;
	}
	*/

	public int getIntFromColor(float Red, float Green, float Blue){
		int R = Math.round(255 * Red);
		int G = Math.round(255 * Green);
		int B = Math.round(255 * Blue);

		R = (R << 16) & 0x00FF0000;
		G = (G << 8) & 0x0000FF00;
		B = B & 0x000000FF;

		return 0xFF000000 | R | G | B;
	}
}
