/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
 * which accompanies this distribution, and is available
 * at the URL "http://www.eclipse.org/legal/epl-v10.html".
 *
 * Initial Contributors:
 * Nokia Corporation - initial contribution.
 *
 * Contributors:
 *
 * Description: 
 *
 */

package com.nokia.carbide.cpp.pi.power;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.nokia.carbide.cpp.internal.pi.actions.SaveSamples;
import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IContextMenu;
import com.nokia.carbide.cpp.internal.pi.power.actions.PowerSettingsDialog;
import com.nokia.carbide.cpp.internal.pi.power.actions.PowerStatisticsDialog;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.internal.pi.visual.GraphComposite;
import com.nokia.carbide.cpp.internal.pi.visual.PICompositePanel;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.internal.pi.visual.PIEventListener;
import com.nokia.carbide.cpp.pi.address.GppSample;
import com.nokia.carbide.cpp.pi.address.GppTrace;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.util.ColorPalette;


public class PowerTraceGraph extends GenericTraceGraph implements ActionListener,
																  PIEventListener,
																  MouseMotionListener,
																  IContextMenu
{
	
    private int[] DTrace; // used for synchronizing the power and gpptraces.
	private int[] DPower;
	
	// 3 tabs can share the same trace, but they need different graphs
    private PwrTrace trace;
    
	private FigureCanvas leftFigureCanvas;
	
	// used to draw the text for the power line in the graph window.
	private int mPowerLineY = 0;
	
    public int x = 400;
    public int y = 800;

    private double averageConsumption = (float)0.0;
	private int averageComsumptionLastOffset = Integer.MAX_VALUE;
    private double cumulative = (float)0.0;
    private double energy = 0.0f;
    private int[] sampleTimes;
    private int[] ampValues;
    private int[] voltValues;
//    private int[] capaValues;
    private double maxAmps = Integer.MIN_VALUE;
	private double minAmps = Integer.MAX_VALUE;
	private double maxPower = 0.0;
    private boolean mSelecting = false;
	
	// used when determining when to draw the text over the power bar.
	private int leftBorder = 0;
	
	private static int xLegendHeight = 20;
	
	boolean mShowPowerLine = true;
	
	private static DecimalFormat powerFormat   = new DecimalFormat(Messages.getString("powerFormat")); //$NON-NLS-1$
	private static DecimalFormat voltageFormat = new DecimalFormat(Messages.getString("voltageFormat")); //$NON-NLS-1$
	
	private int uid;
	
	protected static int SAMPLES_AT_ONE_TIME = 1000;
	protected int stringTime;

	
	// class to pass sample data to the save wizard
    public class SaveSampleString implements ISaveSamples {
		int startTime;
		int endTime;
    	
    	public SaveSampleString() {
		}

    	public String getData() {
    		return getSampleString(SAMPLES_AT_ONE_TIME, this.startTime, this.endTime);
		}

		public int getIndex() {
			if (stringTime == (int) (getSelectionStart() + 0.0005))
				return 0;

			return stringTime;
		}

		public void clear() {
			this.startTime = (int) (getSelectionStart() + 0.0005);
			this.endTime   = (int) (getSelectionEnd() + 0.0005);
			stringTime = startTime;
		}
    }

	/*
	 * return the power samples selected in the interval 
	 */
	protected String getSampleString(int count, int startTime, int endTime)
	{
		Vector sampleVector = ((PwrTrace) this.getTrace()).samples;
		PwrSample lastSample = (PwrSample) sampleVector.get(sampleVector.size() - 1);
		
		String returnString = null;
		
		if (this.stringTime == startTime) {
			returnString = Messages.getString("PowerTraceGraph.saveSamplesHeading"); //$NON-NLS-1$
		}

		this.stringTime++;

		// check if we have returned everything
		if ((this.stringTime > lastSample.sampleSynchTime) || (this.stringTime > endTime)) {
			this.stringTime = endTime + 1;
			return returnString;
		}
		
		double oldCurrent  = -1;
		double oldVoltage  = -1;
		double oldCapacity = -1;
		String string = ""; //$NON-NLS-1$

		if (this.trace.isComplete()) {
			for ( ;
				 (this.stringTime < endTime + 1) && (count > 0) && (this.stringTime < sampleVector.size());
				 this.stringTime++) {
				PwrSample sample = (PwrSample) sampleVector.get(stringTime);
				double current  = sample.current;
				double voltage  = sample.voltage;
				double capacity = sample.capacity;
				
				if ((oldCurrent != current) || (oldVoltage != voltage) || (oldCapacity != capacity)) {
					string = Messages.getString("PowerTraceGraph.comma") + (int) current + Messages.getString("PowerTraceGraph.comma")  + (int) voltage + Messages.getString("PowerTraceGraph.comma") + (int) capacity + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					oldCurrent  = current;
					oldVoltage  = voltage;
					oldCapacity = capacity;
				}
	
				returnString += sample.sampleSynchTime + string;
				count--;
			}
		} else {
			int i = 0;
			
			//find least sampling time greater than or equal to the start index
			for ( ; i < sampleVector.size(); i++) {
				if (((PwrSample) sampleVector.get(i)).sampleSynchTime > this.stringTime)
					break;
			}

			if (i != 0)
				i--;

			for ( ; i < sampleVector.size() && (count > 0); i++) {
				PwrSample sample = (PwrSample) sampleVector.get(i);
				
				this.stringTime = (int) sample.sampleSynchTime;
				if (sample.sampleSynchTime > endTime)
					break;
				
				double current  = sample.current;
				double voltage  = sample.voltage;
				double capacity = sample.capacity;
				
				returnString += sample.sampleSynchTime + Messages.getString("PowerTraceGraph.comma") + (int) current + Messages.getString("PowerTraceGraph.comma")  + (int) voltage + Messages.getString("PowerTraceGraph.comma") + (int) capacity + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				count--;
			}
		}

		// check if we have returned everything
		if (this.stringTime >= lastSample.sampleSynchTime) {
			this.stringTime = endTime + 1;
		}

		return returnString;
	}
	
	protected void actionSaveSamples(ISaveSamples saveSamples)
	{
		new SaveSamples(saveSamples);
	}

	protected MenuItem getSaveSamplesItem(Menu menu, boolean enabled) {
	    MenuItem saveSamplesItem = new MenuItem(menu, SWT.PUSH);

		saveSamplesItem.setText(Messages.getString("PowerTraceGraph.saveSamplesForInterval")); //$NON-NLS-1$
		saveSamplesItem.setEnabled(enabled);
		
		if (enabled) {
			saveSamplesItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
					saveEventSamples();
				}
			});
		}
	
		return saveSamplesItem;
	}
	
	public void saveEventSamples() {
    	SaveSampleString saveSampleString = new SaveSampleString();
    	actionSaveSamples(saveSampleString); //$NON-NLS-1$
	}

    public PowerTraceGraph( int graphIndex, int uid, PwrTrace trace )
    {
        super((GenericSampledTrace)trace);
		this.graphIndex     = graphIndex;
		this.uid			= uid;
       
        if ( trace != null ) 
		{
            this.trace = trace;

            if (this.trace.getSampleTimes() == null)
            	this.trace.initData();

			this.sampleTimes = this.trace.getSampleTimes();
			this.ampValues   = this.trace.getAmpValues();
			this.voltValues  = this.trace.getVoltValues();
//			this.capaValues  = this.trace.getCapaValues();
			this.maxAmps  = this.trace.getMaxAmps();
			this.minAmps  = this.trace.getMinAmps();
			this.maxPower = this.trace.getMaxPower();
        }
    }
    
	public int getUid() {
		return this.uid;
	}
    
	// This method is called when widgets in the PowerInfoPanel are
	// manipulated.
	//
	public void action(String actionString)
	{   
		if (actionString.equals("show_average")) //$NON-NLS-1$
		{
			// show average power as a line
			mShowPowerLine = true;
		}
		else if (actionString.equals("hide_average")) //$NON-NLS-1$
		{
			// do not show average power as a line
			mShowPowerLine = false;
		}
		else if (actionString.equals("changeVoltage")) //$NON-NLS-1$
		{
			// voltage changed, so compute new maximum power
			this.trace.scaleMaxPower();
			this.maxPower = this.trace.getMaxPower();
        }
		else if (actionString.equals("changeBatterySize"))  //$NON-NLS-1$
		{
			// battery size changed, so repaint
        }
		else
			return;

        this.repaint();
		if (this.leftFigureCanvas != null)
			this.leftFigureCanvas.redraw();
    }

    public void piEventReceived(PIEvent be)
	{
	    switch (be.getType())
	    {
	    case (PIEvent.SELECTION_AREA_CHANGED):

			// send this message to the 2 other graphs
			PIEvent be2 = new PIEvent(be.getValueObject(),
					PIEvent.SELECTION_AREA_CHANGED2);

	    	for (int i = 0; i < 3; i++)
	    	{
	    		PowerTraceGraph graph = trace.getPowerGraph(i, getUid());
	    		
	    		if (graph != this) {
					graph.piEventReceived(be2);
	    		}
	    	}

		// FALL THROUGH
		case PIEvent.SELECTION_AREA_CHANGED2:
			double[] values = (double[])be.getValueObject();
			this.setSelectionStart(values[0]);
			this.setSelectionEnd(values[1]);
			this.calcValues();
			mSelecting = true;
            break;

		case (PIEvent.MOUSE_PRESSED):
			this.calcValues();	
        	this.parentComponent.getSashForm().redraw();
			break;

		case PIEvent.SCROLLED:
			Event event = ((Event)be.getValueObject());
			this.parentComponent.setScrolledOrigin(event.x, event.y);
			this.repaint();
			break;

		default:
        	break;
	    }
	}
	
	public void refreshDataFromTrace()
	{
	}

	public void enablePowerLine( boolean state )
	{
		mShowPowerLine = state;
	}

	public void actionPerformed ( ActionEvent ae ) 
	{
        if( ae.getActionCommand().equals("switch") )  //$NON-NLS-1$
		{
            parentComponent.piEventReceived( new PIEvent( "switch_gpp", PIEvent.PLUGIN_STRING_MESSAGE) ); //$NON-NLS-1$
        } 
        this.repaint();
    }	
    
    public PICompositePanel getParentComponent() {
        return this.parentComponent;
    }
    
    public float getVoltage() 
	{
        if ( trace != null )
            return trace.getVoltage();
            
        return 0.0f;
    }
    
    public void setVoltage( float newVal ) 
	{
        if ( trace != null )
            trace.setVoltage(newVal);
    }
    
    public double getAverageConsumption() 
	{	
        if ( trace != null ) 
		{
			if( averageComsumptionLastOffset != trace.getOffset() )
			{
				averageComsumptionLastOffset = trace.getOffset();
				// not calculated yet
				calcValues();
			}
			
            return this.averageConsumption * trace.getVoltage();
        } 
		
		return 0.0f;
    }
    
    public void setOffset( int newOffset ) 
	{   
	    if ( trace != null )
			trace.setOffset( newOffset );
	}
    
    public void increaseOffset() 
	{
        if ( trace != null )
		{
            trace.setOffset( trace.getOffset() + 50 );
		}
    }
    
    public void decreaseOffset() 
	{
        if ( trace != null )
		{
            trace.setOffset( trace.getOffset() - 50 );
		}
    }
    
    public void setSize(int x, int y)
	{
	    this.x = x;
	    this.y = y;
	}

    public Dimension getSize()
	{
	    return new Dimension(x, y);
	}

	public void paint(Panel panel, Graphics graphics)
	{
		this.setSize(this.getSize().width, getVisualSize().height);
	    this.drawDottedLineBackground(graphics, PowerTraceGraph.xLegendHeight);
	    this.drawPowerData(graphics, PowerTraceGraph.xLegendHeight);

	    // draws the same selection as the Address/Thread trace
		this.drawSelectionSection(graphics, PowerTraceGraph.xLegendHeight);

		if (mShowPowerLine)
			this.drawPowerLine(graphics);		
	}

    public void repaint()
	{   
	    this.parentComponent.repaintComponent();
	}
    	
	private void showPowerValueAtX(MouseEvent me)
	{
		if (me.y > this.getVisualSizeY() - PowerTraceGraph.xLegendHeight) {
			this.setToolTipText(null);
			return;
		}
		
		// scale the value of x
		int x = (int) (me.x * this.getScale() + 0.5);
		
		if (x > (int) (PIPageEditor.currentPageEditor().getMaxEndTime() * 1000 + 0.0005)) {
			this.setToolTipText(null);
			return;
		}
		
		if (x > trace.getLastSampleNumber())
			x = trace.getLastSampleNumber();

		String textToShow = Double.toString(x / 1000.0)
							+ Messages.getString("tooltip1"); //$NON-NLS-1$

		// We could instead use the measured voltage: voltValues[x +/- movement]/1000.0 rather than the user-specified voltage?
		double voltage = trace.getVoltage();

		int movement = trace.getOffset();
		int ampValue = 0;

		// find the amperage for x
		int index = -1;
		if (movement == 0)
		{
			index = timeIndex(x);
			if (index != -1)
				ampValue = ampValues[index];
		}
		else if (movement < 0)
		{
			// make it positive
			movement *= -1;
			
			// eat the first N=offset values.
			if (x < sampleTimes.length - movement)
			{
				index = timeIndex(x + movement);
				if (index != -1)
					ampValue = ampValues[index];
			}
		}
		else // movement > 0
		{
			if (x > movement)
			{
				index = timeIndex(x - movement);
				if (index != -1)
					ampValue = ampValues[index];
			}
		}

		// determine the power = amps * voltage
		textToShow += (int) ((ampValue * voltage) + 0.5);

		this.setToolTipText(textToShow + Messages.getString("tooltip2") //$NON-NLS-1$
							+ PowerTraceGraph.voltageFormat.format(voltage));
	}
	
	public int timeIndex(int time) {
		if (time < sampleTimes[0])
			return -1;
		
		if (time >= sampleTimes[sampleTimes.length - 1])
			return sampleTimes.length - 1;

		int i = 0;
		for ( ; sampleTimes[i] <= time; i++)
			;
		
		if (sampleTimes[i] == time)
			return i;
		else
			return i - 1;
	}

	private void drawPowerData(Graphics graphics, int yLegendSpace)
	{
		int visY = this.getVisualSize().height - yLegendSpace;
		if (visY < 0)
			visY = 0;
		int sampleCount = this.sampleTimes.length;
				
		// arrays of values to draw
		int points[] = new int[sampleCount * 4];
		
		// the offset changes when the user moves the graph or the traces are synched
		int movement = trace.getOffset();
		
		// look for a move to the right ( > 0)
		// the y value is in thisValue is the milliAmps	
		// it doesn't matter if we render the milliAmps or mW, the graphs have the same
		// form, it only matters when you show the values associated with the graph.
		double maxAmps = this.maxPower / this.trace.getVoltage();

		double cachedScale = this.getScale();
		// or no movement (==0)
		if( movement == 0 )
		{
			for( int i = 0, k = 0; i < sampleTimes.length; i++ )
			{
				points[k++] = (int)(sampleTimes[i] / cachedScale);
				points[k++] = (int)(visY - (ampValues[i] / maxAmps) * visY);
				if (i < sampleTimes.length - 1)
				{
					points[k++] = (int)(sampleTimes[i + 1] / cachedScale);
				} else {
					long lastTime = (int) (PIPageEditor.currentPageEditor().getMaxEndTime() * 1000 + 0.0005);
					points[k++] = (int) (lastTime / cachedScale);
				}
				points[k++] = (int)(visY - (ampValues[i] / maxAmps) * visY);
			}
		} 
		else if( movement > 0 )
		{
			// set the first N=offset values to be 0
			for (int i = 0, k = movement * 2; i < sampleCount - movement; i++) {
				points[k++] = sampleTimes[i];
				points[k++] = ampValues[i];
			}

			for( int i = 0, k = 0; i < sampleCount; i++ )
			{
				if( i < movement )
				{
					points[k++] = (int)(i / cachedScale);
					points[k++] = (int)(visY - ((float)1 / maxAmps) * visY);
				}
				else
				{
					points[k++] = (int)((points[2 * i] + movement)/cachedScale);
					points[k++] = (int)(visY - ((float)points[1 + 2 * i] / maxAmps) * visY);
				}
			}
		}
		else
		{
			// make it positive
			movement *= -1;
			
			// eat the first N=offset values.
			int cutoff = sampleTimes.length - movement;
			
			for (int i = 0, k = movement * 2; i < sampleCount - movement; i++) {
				points[k++] = sampleTimes[i];
				points[k++] = ampValues[i];
			}

			for( int i = 0, k = 0; i < sampleCount; i++ )
			{
				if( i >= cutoff )
				{
					points[k++] = (int)(i / cachedScale);
					points[k++] = (int)(visY - ((float)1 / maxAmps) * visY);
				}
				else
				{
					points[k++] = (int)((points[2 * i] - movement) / cachedScale);
					points[k++] = (int)(visY - ((float)points[1 + 2 * i] / maxAmps) * visY);
				}
			}
		}

		graphics.setForegroundColor(ColorConstants.red);
		// draw all the points at once.
		graphics.drawPolyline(points);
		points = null;
	}

	public double calcValueForY( int y )
	{
		double visY = this.getVisualSize().height - PowerTraceGraph.xLegendHeight;
		if (visY <= 0)
			return 0.0;
		double maxAmps = this.maxPower / this.trace.getVoltage();
		double yScalingFactor = maxAmps/(double)visY;
		// visY-y to compensate for the transpose, * voltage to compenstate for the mA in the samples, we 
		// want to show the mW values here.
		return ( (double)(visY - (double)y) * yScalingFactor * (double)trace.getVoltage());
	}
	
	public int calcYforValue( double value )
	{
		double visY = this.getVisualSize().height - PowerTraceGraph.xLegendHeight;
		if ((visY <= 0) || (this.trace.getVoltage() == 0))
			return 0;
		double maxAmps = this.maxPower / this.trace.getVoltage();
		double yScalingFactor = maxAmps / visY;

		// visY-y to compensate for the transpose, * voltage to compenstate for the mA in the samples, we 
		// want to show the mW values here.
		if (yScalingFactor == 0)
			return 0;

		double tmp = value / (double)trace.getVoltage() / yScalingFactor;
		
		return (int)Math.ceil(visY - tmp);
	}

	private void drawPowerLine( Graphics graphics )
	{
		//mPowerLineY == 0  on startup.
		boolean startup = (mPowerLineY == 0);

		if( mPowerLineY == 0 || mSelecting )
		{
			if( mSelecting )
			{
				// still selecting?
				if( ((super.getSelectionStart() == -1)  && (super.getSelectionEnd() == -1)) )
				{
					mSelecting = false;
					// need to turn them back on. mouse drag events for drawing the power line.
				}
				else
				{
					mPowerLineY = calcYforValue( getAverageConsumption() );
				}
			}
			
			if( mPowerLineY == 0 )
			{
				mPowerLineY = calcYforValue( getAverageConsumption() );
			}
		}
		
		double powerValue = 0.0;
		
		String strValue = null;
		
		// requirement, if dragging power bar then print true power value. If
		// selecting then print average of area not the true value of the y coordinate.
		//
		if( !mSelecting && !startup )
			powerValue = calcValueForY(mPowerLineY);
		else if( mSelecting || startup )
			powerValue = getAverageConsumption();

		strValue = PowerTraceGraph.powerFormat.format((int)(powerValue + 0.5));

		// this does cause some overhead and could be removed since it only provides some aesthetics.
		updateVisibleBorders();
		leftBorder = this.parentComponent.getScrolledOrigin().x;
//		leftBorder = this.getVisibleLeftBorder();
		if( leftBorder < 0 )
			leftBorder = 0;	
		
		// draw the average power line with a width of 3
		int lineWidth = graphics.getLineWidth();

		graphics.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		graphics.setLineWidth(2);
		graphics.drawLine(leftBorder, mPowerLineY, this.getSize().width, mPowerLineY );

		graphics.setLineWidth(lineWidth);

		// figure out how big the box behind the text should be.
		GC gc = new GC(PIPageEditor.currentPageEditor().getSite().getShell());
		Point point = gc.stringExtent(strValue);
		gc.dispose();

		// clear the text rectangle
		graphics.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		graphics.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		graphics.fillRectangle(leftBorder + 20, mPowerLineY - (point.y / 2), point.x + 6, point.y );

		graphics.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));

		// make the font bold 16, draw the text, and restore the font
		int oldStyle  = graphics.getFont().getFontData()[0].getStyle();
		int oldHeight = graphics.getFont().getFontData()[0].getHeight();

		graphics.getFont().getFontData()[0].setStyle((oldStyle & SWT.NORMAL) | (oldStyle & SWT.ITALIC) | SWT.BOLD);
		graphics.getFont().getFontData()[0].setHeight(16);
		
		graphics.drawString(strValue, leftBorder + 23, mPowerLineY - (point.y / 2));

		graphics.getFont().getFontData()[0].setStyle(oldStyle);
		graphics.getFont().getFontData()[0].setHeight(oldHeight);

		if ( startup )
			mPowerLineY = 0;
	}

    public void drawPwrGraphSelection(Graphics g)
	{	
	    int selectionStart = (int)super.getSelectionStart();
	    int selectionEnd = (int)super.getSelectionEnd();
	    double scale = super.getScale();
		
	    if(selectionStart != -1 && selectionEnd != -1)
	    {
	    	Color selectColor = new Color(Display.getCurrent(),123,156,178);
	    	
	    	g.setForegroundColor(selectColor);
	    	g.fillRectangle((int)(selectionStart / scale + 0.5),
	    					0,
	    					(int)((selectionEnd - selectionStart) / scale + 0.5),
	    					this.getVisualSize().height - PowerTraceGraph.xLegendHeight);
	    	selectColor.dispose();
	    }
	}
	
	// synchronize the power and software traces
	// logically:
	// 1) digitize the GPP and power traces
	// 2) align the traces so that the tail of the software trace is aligned with 
	//	  the tail of the power trace.
	// 3) slowly move software trace to the left ADDing the software and power trace
	// 	  values together and performing a sum until the highest total is found.
	// 4) using the index of the highest total, either add dummy values to the power trace or
	//    remove values from the power trace.
	// 5) finally update the samples and refresh turn off the wait cursor.
    
	public void doSynchronize( GppTrace gpp, PwrTrace power )
	{
//		System.err.println( "Starting synchronization process" );
		digitizeGPPFile(gpp);
		digitizePowerFile(power);
		
		int MAX = 0;
		int MAX_POWER_INDEX = 0;
		int MAX_TRACE_INDEX = 0;
		int accum = 0;

		int TOTAL_MOVEMENT = (DPower.length-DTrace.length) + (int)(0.25*DTrace.length) + 1;

		// TI = trace index
		int TI = 0;
		// PI = power trace index
		int PI = DPower.length-DTrace.length;

		for( int j = 0; j < TOTAL_MOVEMENT; j++ )
		{
			for( int i = TI; i < DTrace.length; i++ )
			{
				if( (DTrace[i] & DPower[PI+i]) == 0 )
				{
					accum++;
				}
			}
			if( accum > MAX )
			{
				MAX = accum;
				MAX_POWER_INDEX = PI;
				MAX_TRACE_INDEX = TI;
			}
			accum = 0;
			PI--;
			if( PI < 0 )
			{
				PI=0;
				TI++;
			}
		}
		// one more pass through the data to update the graphs.
		//

		if( MAX_TRACE_INDEX > 0 )
		{
			trace.setOffset( trace.getOffset() - MAX_TRACE_INDEX );
		}
		else
		{
			trace.setOffset( trace.getOffset() + MAX_POWER_INDEX );
		}
		parentComponent.getActiveGraph().getCompositePanel().getSashForm()
								.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT) );
	}
	// digitize the power trace
	// basically:
	// 1) find the min and max power values in the trace
	// 2) for each value in the trace if the value is > Half the average range
	//    then set that value to 1 else 0.
	//
	public void digitizePowerFile( PwrTrace power )
	{
		double MIN_Value = (double)minAmps/(double)1000;
		double MAX_Value = (double)maxAmps/(double)1000;
		
		// find the min and max in the power
		//
		// we already have this data so we can simply divide by 1000 for the scale we need.

		float Half_Avg_Range = (float)((MAX_Value - MIN_Value)/2);		
		
		DPower = new int[power.samples.size()];
		int DPowerIndex = 0;

		// as an aside, this tracks well with the application states.
		//
		for( Enumeration e = power.getSamples(); e.hasMoreElements() ; )
		{
			PwrSample current = (PwrSample)e.nextElement();
			if( current.current > Half_Avg_Range )
			{
				DPower[DPowerIndex] = 1;
			}
			else
			{
				DPower[DPowerIndex] = 0;
			}
			DPowerIndex++;
		}
	}
	
	// digitize the gpp file
	// basically:
	// 1a) divide the run into windows and for each window:
	// 1b) count the number of transitions i.e. different functions called in the windows by putting the process
	//    name into a hashtable, if the add returns a null value then the insert 
	//    did not collide and we transitioned.
	// 1c) find the min and max number of transistions for all windows
	//
	// 2a) for each window if the number of transitions for that window are greater
	//	   than the average number of transtitions then set the digitized version to be 1 else 0.
	//
	public void digitizeGPPFile( GppTrace thisTrace )
	{		
		int WINDOW_SIZE = 20;

		int numSamples = thisTrace.samples.size();
		int count = 1;
		Hashtable<String,String> histogram = new Hashtable<String,String>();
		int windowSizes[] = new int[numSamples/WINDOW_SIZE];
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		int numTransitions = 1;

		int total = 0;
//		String lastTrace = null;
		for( int i = 0; i < numSamples; i++ )
		{
			GppSample sample = thisTrace.getGppSample(i);
			if( count % WINDOW_SIZE == 0 )
			{
				windowSizes[i/WINDOW_SIZE] = histogram.size() + numTransitions;
				if( windowSizes[i/WINDOW_SIZE] < min )
					min = windowSizes[i/WINDOW_SIZE];
				else if( windowSizes[i/WINDOW_SIZE] > max )
					max = windowSizes[i/WINDOW_SIZE];

				total += windowSizes[i/WINDOW_SIZE];

				numTransitions = 1;
				histogram.clear();
				count = 0;
			}
			count++;
			if( histogram.put( new String(sample.thread.process.name + Messages.getString("PowerTraceGraph.histogram1") //$NON-NLS-1$
						                  + (sample.thread.threadName != null ? sample.thread.threadName
											   : Messages.getString("PowerTraceGraph.11")) + Messages.getString("PowerTraceGraph.12") //$NON-NLS-1$ //$NON-NLS-2$
					   							   + sample.thread.threadId + Messages.getString("PowerTraceGraph.13") //$NON-NLS-1$
					   							   + sample.currentFunctionSym.functionName + Messages.getString("PowerTraceGraph.14") //$NON-NLS-1$
					   							   + Long.toHexString(sample.currentFunctionSym.startAddress.longValue()) ),
					   			Messages.getString("PowerTraceGraph.15") ) == null ) //$NON-NLS-1$
			{
				numTransitions++;
			}
		}
		
		int average = (max - min) /2 ;

		DTrace = new int[windowSizes.length * WINDOW_SIZE];
		int DTraceIndex = 0;
		for( int i = 0; i < windowSizes.length; i++ )
		{
			for( int j = 0; j < WINDOW_SIZE; j++ )
			{
				// okay, so it seems that when there is more variablity in the trace there is more constant variablity in the power.
				if( windowSizes[i] <= average /*halfAvgRange */ )	
				{
					DTrace[DTraceIndex] = 0;
				}
				else
				{
					DTrace[DTraceIndex] = 1;
				}
				DTraceIndex++;
			}
		}
	}

	// calculates the average power numbers of a selection.
    private void calcValues() 
	{
        int selStart = (int)super.getSelectionStart(); 
        int selEnd   = (int)super.getSelectionEnd();
        int sum = 0;
        int offset = trace.getOffset();
        this.cumulative = 0;
        this.averageConsumption = 0;
        this.energy = 0;
        
        if (selStart < offset)
        	selStart = offset;

        if (selEnd < trace.getFirstSampleNumber() || selStart > selEnd)
        	return;
        
        // find the first sample greater than or equal to selStart
        int index = timeIndex(selStart);

        // count time before the first sample as a bunch of zeros
        if (selStart < trace.getFirstSampleNumber()) {
        	sum = trace.getFirstSampleNumber() - selStart;
        	index = 0;
        }
        
		for (int j = index; j < sampleTimes.length; j++)
		{	
			int time = sampleTimes[j] + 1;
			if (time < selStart)
				time = selStart;
			
			int nextTime = j == sampleTimes.length - 1 ? Integer.MAX_VALUE : sampleTimes[j + 1];
			
			int count = Math.min(nextTime - time + 1, selEnd - time + 1);
			this.energy += ((double)ampValues[j] * (double)this.voltValues[j])/(float)1000000.0 * count;
			this.cumulative += ampValues[j] * count;
			sum += count;
			time += count;
			
			if (time > selEnd)
				break;
		}
		
		if (sum > 0)
			this.averageConsumption = (double)this.cumulative / sum ;     		
    }
    
    public double getCumulativeConsumption() 
	{
        return this.energy;
    }
     
    public float getBatterySize() {
        if ( trace != null ) {
            return trace.getBatterySize();
        } else
            return 0.0f;
    }
    
    public void setBatterySize( float newVal ) {
        if ( trace != null )
            trace.setBatterySize( newVal );
    }
	
	public void mouseDragged(MouseEvent me)  
	{
		int tmpY = me.y;
		int tmpX = me.x;

		if( (tmpY < (this.getVisualSize().height - PowerTraceGraph.xLegendHeight)) && (tmpY > 0) && (!mSelecting))
		{
			mPowerLineY = tmpY;
		}

		this.repaint();
	}

	public void mouseMoved(MouseEvent me)  
	{
		showPowerValueAtX( me );
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mouseHover(MouseEvent arg0) {
	}

	public void addContextMenuItems(Menu menu, org.eclipse.swt.events.MouseEvent me) {
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		Boolean showLine   = Boolean.TRUE;	// by default, show the interval average power as a line

		// if there is a show average power line value associated with the current Analyser tab, then use it
		Object obj = NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.power.showLine");  //$NON-NLS-1$
		if ((obj != null) && (obj instanceof Boolean))
			// retrieve the current value
			showLine = (Boolean)obj;
		else
			// set the initial value
			NpiInstanceRepository.getInstance().setPersistState(uid, "com.nokia.carbide.cpp.pi.power.showLine", showLine);  //$NON-NLS-1$

		final Boolean showLineFinal = showLine;

		MenuItem showLineItem = new MenuItem(menu, SWT.CHECK);
		showLineItem.setText(Messages.getString("PowerTraceGraph.18")); //$NON-NLS-1$
		showLineItem.setSelection(showLine);
		showLineItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String action;
				NpiInstanceRepository.getInstance().setPersistState(uid, "com.nokia.carbide.cpp.pi.power.showLine", !showLineFinal);  //$NON-NLS-1$
				if (!showLineFinal)
				{
					action = "show_average";  //$NON-NLS-1$
				} else {
					action = "hide_average";  //$NON-NLS-1$
				}

		    	for (int i = 0; i < 3; i++)
		    	{
		    		PowerTraceGraph graph = trace.getPowerGraph(i, getUid());
					graph.action(action);
		    	}
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);

		int startTime = (int) this.getSelectionStart();
		int endTime   = (int) this.getSelectionEnd();

		// save raw samples
		getSaveSamplesItem(menu, (startTime != -1) && (endTime != -1) && (startTime != endTime));
		
		new MenuItem(menu, SWT.SEPARATOR);

		MenuItem powerSettingsItem = new MenuItem(menu, SWT.PUSH);
		powerSettingsItem.setText(Messages.getString("PowerTraceGraph.22")); //$NON-NLS-1$
		powerSettingsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new PowerSettingsDialog(Display.getCurrent());
			}
		});
		
		MenuItem powerStatsItem = new MenuItem(menu, SWT.PUSH);
		powerStatsItem.setText(Messages.getString("PowerTraceGraph.23")); //$NON-NLS-1$
		powerStatsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new PowerStatisticsDialog(Display.getCurrent());
			}
		});
	}

	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc)
	{
		GC localGC = gc;
		
		if (gc == null)
			gc = new GC(PIPageEditor.currentPageEditor().getSite().getShell());
		
		if (this.leftFigureCanvas == null)
			this.leftFigureCanvas = figureCanvas;

		Rectangle rect = ((GraphComposite) figureCanvas.getParent()).figureCanvas.getClientArea();
		
		double visY = rect.height - PowerTraceGraph.xLegendHeight;
		if (visY < 0)
			visY = 0;
		double yIncrement = visY / 10;
		
		// this assumes maxPower is evenly divisible by 10
		int maxPower = (int) this.maxPower;
		int powerIncrement = maxPower / 10;

		gc.setForeground(ColorPalette.getColor(new RGB(100, 100, 100)));
		gc.setBackground(ColorPalette.getColor(new RGB(255, 255, 255)));

		int previousBottom = 0;		// bottom of the previous legend drawn
		String legend;

		// draw 11 value indicators (0..10) to the scale  
		int i = 0;
		for (double y = 0; i < 11; i++, y += yIncrement, maxPower -= powerIncrement)
		{
			// construct the text for each scale
			legend = (int)maxPower + Messages.getString("PowerTraceGraph.24"); //$NON-NLS-1$
			
			Point extent = gc.stringExtent(legend);
			
			gc.drawLine(GenericTraceGraph.yLegendWidth - 3, (int)y + 1, GenericTraceGraph.yLegendWidth, (int)y + 1);

			if (y >= previousBottom)
			{
				gc.drawString(legend, GenericTraceGraph.yLegendWidth - extent.x - 4, (int)y);
				previousBottom = (int)y + extent.y;
			}
		}

		if (localGC == null) {
			gc.dispose();
			figureCanvas.redraw();
		}
	}
}
