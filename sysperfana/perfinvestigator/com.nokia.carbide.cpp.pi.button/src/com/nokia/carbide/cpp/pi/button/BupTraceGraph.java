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

package com.nokia.carbide.cpp.pi.button;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Panel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.nokia.carbide.cpp.internal.pi.actions.SaveSamples;
import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples;
import com.nokia.carbide.cpp.internal.pi.model.GenericSample;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IContextMenu;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;


public class BupTraceGraph extends GenericTraceGraph implements MouseMoveListener,
																MouseListener,
																IContextMenu
{
	/*
	 * Draw button text in existing Address plugin panel
	 *
	 * For each button press, draw a line from the top to below the thread/binary/function start and end
	 * indicators, but above the x-axis label that tells seconds
	 */
	
	// whether to display events
	private boolean display_events = true;

	// constants used in drawing event: line coming down with a rectangle at the end and a name underneath
	private static final int EVENT_TOPOFLINE    = 50;
	private static final int EVENT_BOTTOMOFLINE = 30;
	private static final int EVENT_RECTWIDTH    = 5;
	private static final int EVENT_RECTHEIGHT   = 5;

	private ArrayList<BupSample> matchingSamples;
	private Button eventButtons[];
	private boolean selectedEventButtons;
	
	// class to pass sample data to the save wizard
    public class SaveSampleString implements ISaveSamples {
    	boolean done = false;
    	
    	public SaveSampleString() {
		}

    	public String getData() {
    		if (done)
    			return null;
    		
			String returnString = getSampleString();
			done = true;
			return returnString;
		}

		public String getData(int size) {
 			return getData();
		}

		public int getIndex() {
			return done ? 1 : 0;
		}

		public void clear() {
			done = false;
		}
   }

	/*
	 * return the button samples selected in the interval 
	 */
	protected String getSampleString()
	{
		int startTime = (int) this.getSelectionStart();
		int endTime   = (int) this.getSelectionEnd();
		
		Vector sampleVector = ((GenericSampledTrace) this.getTrace()).samples;
		
		int i = 0;
		while (i < sampleVector.size() && ((BupSample) sampleVector.get(i)).sampleSynchTime < startTime)
			i++;
	
		String returnString = Messages.getString("BupTraceGraph.saveSamplesHeading"); //$NON-NLS-1$

		while (i < sampleVector.size() && ((BupSample) sampleVector.get(i)).sampleSynchTime <= endTime) {
			BupSample sample = (BupSample) sampleVector.get(i);
			returnString += sample.sampleSynchTime + "," + sample.getLabel() + ",\""; //$NON-NLS-1$ //$NON-NLS-2$

			String comment = sample.getComment();
			if (comment != null) {
				for (int j = 0; j < comment.length(); j++) {
					if (comment.charAt(j) == '"')
						returnString += '"';
					returnString += comment.charAt(j);
				}
			}
			returnString += "\"\n"; //$NON-NLS-1$
			i++;
		}

		return returnString;
	}

	protected void actionSaveSamples(ISaveSamples saveSamples)
	{
		new SaveSamples(saveSamples);
	}

	protected MenuItem getSaveSamplesItem(Menu menu, boolean enabled) {
	    MenuItem saveSamplesItem = new MenuItem(menu, SWT.PUSH);

		saveSamplesItem.setText(Messages.getString("BupTraceGraph.saveSamplesForInterval")); //$NON-NLS-1$
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

	public BupTraceGraph(int graphIndex, BupTrace trace)
	{
		super(trace);
		this.graphIndex = graphIndex;
	}

	public void paint(Panel panel, Graphics graphics)
	{
		if (!display_events)
			return;

		Enumeration samples = ((GenericSampledTrace)this.getTrace()).samples.elements();
		graphics.setForegroundColor(ColorConstants.red);
		double scale = this.getScale();
		int height = this.getVisualSize().height;
		String eventName;
		int lastXLabelEnd = -1; //the x coordinate at which the last drawn label ended

		while (samples.hasMoreElements())
		{
			BupSample sa = (BupSample)samples.nextElement();
			int x = (int)(sa.sampleSynchTime/scale);

			graphics.setForegroundColor(ColorConstants.red);
			graphics.drawLine(x, height - EVENT_TOPOFLINE, x, height - EVENT_BOTTOMOFLINE);

			// center the text under vertical line
			eventName = sa.getLabel();

			graphics.setFont(PIPageEditor.helvetica_8);

			GC gc = new GC(PIPageEditor.currentPageEditor().getSite().getShell());
			Point point = gc.stringExtent(eventName);
			gc.dispose();

			int xLabelStart = x - (point.x / 2);
			if (xLabelStart > lastXLabelEnd){ //only draw label if it doesn't overlap with previous
				if (sa.isLabelModified()) {
					graphics.setForegroundColor(ColorConstants.blue);
				} else {
					graphics.setForegroundColor(ColorConstants.red);
				}
				graphics.drawString(eventName, xLabelStart, height - EVENT_BOTTOMOFLINE + 3 + EVENT_RECTHEIGHT);
				lastXLabelEnd = x + (point.x / 2);				
			}

			// events with comments will have blue rectangles; otherwise, red triangles
			if ((sa.getComment() != null) && (!sa.getComment().equals(""))) //$NON-NLS-1$
			{
				graphics.setForegroundColor(ColorConstants.blue);
				graphics.setBackgroundColor(ColorConstants.blue);
			}
			else
			{
				graphics.setBackgroundColor(ColorConstants.red);
			}
			graphics.drawLine(x , height - EVENT_BOTTOMOFLINE + 1, x, height - EVENT_BOTTOMOFLINE + 2);
			graphics.fillRectangle(x - (EVENT_RECTWIDTH / 2), height - EVENT_BOTTOMOFLINE + 3,
									EVENT_RECTWIDTH, EVENT_RECTHEIGHT);
		}
	}

	public void repaint()
	{
	}

	public void action(String action)
	{
		// do/don't show events in graph
		if (   action.equals("button_events_on") //$NON-NLS-1$
			|| action.equals("button_events_off")) //$NON-NLS-1$
  		{
  			this.display_events = action.equals(Messages.getString("BupTraceGraph.button.events.on")); //$NON-NLS-1$
  			this.parentComponent.repaintComponent();
  		} else if (action.equals("button_map_switch")){ //$NON-NLS-1$
  			this.parentComponent.repaintComponent();
  		}
  	}

	public void mouseMove(MouseEvent me)
//	public void mouseHover(MouseEvent me)
	{
		if (!display_events)
			return;

		long x = me.x;
		long y = me.y;

		int height = this.getVisualSize().height;

		// make sure we're in the right area of the graph window
		if ((y < height - EVENT_BOTTOMOFLINE) || (y > height - EVENT_BOTTOMOFLINE + EVENT_RECTHEIGHT + 2))
			return;

		Enumeration samples = ((GenericSampledTrace) this.getTrace()).samples.elements();
		double scale = this.getScale();
		
//		x += ((FigureCanvas)me.getSource()).getViewport().getViewLocation().x;

		String tooltip = ""; //$NON-NLS-1$

		while (samples.hasMoreElements())
		{
			BupSample sample = (BupSample)samples.nextElement();
			long xSample = (long)(sample.sampleSynchTime/scale + 0.5);
			
			// samples are in timestamp order, so stop when xSample + EVENT_RECTWIDTH is too high
			if (x < xSample - EVENT_RECTWIDTH)
				break;

			if ((x >= xSample - EVENT_RECTWIDTH) && (x <= xSample + EVENT_RECTWIDTH))
			{
				String parsedName = sample.getLabel();
				if (!tooltip.equals("")) //$NON-NLS-1$
					tooltip += Messages.getString("BupTraceGraph.newline"); //$NON-NLS-1$
				tooltip += Messages.getString("BupTraceGraph.tooltip1") + parsedName + Messages.getString("BupTraceGraph.tooltip2") + sample.sampleSynchTime/1000.0 + Messages.getString("BupTraceGraph.tooltip3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (sample.getComment() != null)
					 tooltip += Messages.getString("BupTraceGraph.tooltip4") + sample.getComment(); //$NON-NLS-1$
			}
		}
		
		if (!tooltip.equals("")) //$NON-NLS-1$
			((FigureCanvas)me.getSource()).setToolTipText(tooltip);
	}

	public void mouseDoubleClick(MouseEvent me)
	{
		if (!display_events)
			return;

		long x = me.x;
		long y = me.y;

		int height = this.getVisualSize().height;

		// make sure we're in the right area of the graph window
		if ((y < height - EVENT_BOTTOMOFLINE) || (y > height - EVENT_BOTTOMOFLINE + EVENT_RECTHEIGHT + 2))
			return;

		// repaint if the name or comment changes
		boolean redraw = false;

		Enumeration samples = ((GenericSampledTrace)this.getTrace()).samples.elements();
		double scale = this.getScale();

		// unlike mouseMove, the x coordinate is not relative to the FigureCanvas
		x += ((FigureCanvas)me.getSource()).getViewport().getViewLocation().x;

		matchingSamples = new ArrayList<BupSample>();
		
		while (samples.hasMoreElements())
		{
			BupSample sample = (BupSample)samples.nextElement();
			long xSample = (long)(sample.sampleSynchTime/scale + 0.5);

			// samples are in timestamp order, so stop when xSample + EVENT_RECTWIDTH is too high
			if (x < xSample - EVENT_RECTWIDTH)
				break;

			if ((x >= xSample - EVENT_RECTWIDTH) && (x <= xSample + EVENT_RECTWIDTH))
			{
				matchingSamples.add(sample);
			}
		}

		if (matchingSamples.size() == 0)
			return;
	
		// let them change some information
		Display display = PIPageEditor.currentPageEditor().getEditorSite().getShell().getDisplay();

		GridData gridData;

		if (matchingSamples.size() > 1) {
			selectedEventButtons = false;

			// query for which of several to display
			Shell shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
			shell.setText(Messages.getString("BupTraceGraph.chooseEvents")); //$NON-NLS-1$
			shell.setLayout(new GridLayout(2, true));
			final Shell shellFinal = shell;

			Label label = new Label(shell, SWT.LEFT);
			label.setText(Messages.getString("BupTraceGraph.selectEvents")); //$NON-NLS-1$
			label.setFont(PIPageEditor.helvetica_10);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;
			label.setLayoutData(gridData);

			eventButtons = new Button[matchingSamples.size()];

			for (int i = 0; i < matchingSamples.size(); i++) {
				eventButtons[i] = new Button(shell, SWT.CHECK);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				eventButtons[i].setLayoutData(gridData);
				
				BupSample sample = matchingSamples.get(i);
				
				String parsedName = sample.getLabel();
				eventButtons[i].setText(Messages.getString("BupTraceGraph.buttonString1") + parsedName + Messages.getString("BupTraceGraph.buttonString2") + sample.sampleSynchTime/1000d + Messages.getString("BupTraceGraph.buttonString3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				eventButtons[i].setFont(PIPageEditor.helvetica_10);
			}
			
			// create the OK button
			Button okButton = new Button(shell, SWT.NONE);
			okButton.setText(Messages.getString("BupTraceGraph.ok")); //$NON-NLS-1$
			gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
			okButton.setLayoutData(gridData);

			// add the listener(s)
			okButton.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					selectedEventButtons = true;
					for (int i = matchingSamples.size() - 1; i >= 0; i--)
						if (eventButtons[i].getSelection() == false)
							matchingSamples.remove(i);

					shellFinal.close();
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});

			// create the Cancel button
			Button cancelButton = new Button(shell, SWT.NONE);
			cancelButton.setText(Messages.getString("BupTraceGraph.cancel")); //$NON-NLS-1$
			gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
			cancelButton.setLayoutData(gridData);

			// add the listener(s)
			cancelButton.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					shellFinal.close();
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});

			shell.pack();
			shell.open();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}

			if (selectedEventButtons == false)
				matchingSamples.clear();
		}

		IBupEventMap map = ((BupTrace)this.getTrace()).getCurrentBupMapInUse();
		for (int i = 0; i < matchingSamples.size(); i++)
		{
			BupSample sample = matchingSamples.get(i);
			
			String enumString = map.getEnum(sample.getKeyCode());

			BupEventDialog eventDialog = new BupEventDialog(PIPageEditor.currentPageEditor().getEditorSite().getShell(), Messages.getString("BupTraceGraph.buttonDialog"), //$NON-NLS-1$
														sample.getKeyCode(), enumString, sample.getLabel(), sample.getComment(), false, sample.sampleSynchTime);
			eventDialog.open();

			if (   (eventDialog.getNewName() != null)
				&& (!eventDialog.getNewName().equals(sample.getLabel()))) {
				sample.setLabel(eventDialog.getNewName());
				redraw = true;
			}

			if (   (eventDialog.getNewComment() != null)
				&& (!eventDialog.getNewComment().equals(sample.getComment()))) {
				sample.setComment(eventDialog.getNewComment());
				redraw = true;
			}
			
			if (eventDialog.getNewSamePropagate()) {
				Vector<GenericSample> allSamples = ((GenericSampledTrace)this.getTrace()).samples;
				for (GenericSample genericSample : allSamples) {
					BupSample currentSample = (BupSample)genericSample;
					if (currentSample.getKeyCode() == sample.getKeyCode()) {
						currentSample.setLabel(eventDialog.getNewName());
					}
				}
				redraw = true;
			}
		}

		if (redraw)
			((FigureCanvas)me.getSource()).redraw();
	}

	public void mouseDragged(MouseEvent me) {}

	public void mouseEntered(MouseEvent me) {}

	public void mouseExited(MouseEvent me) {}

	public void mouseDown(MouseEvent me) {}

	public void mouseUp(MouseEvent me) {}

	public static boolean openButtonDialog(int buttonIndex)
	{
		boolean redraw = false;

		BupTrace bupTrace = (BupTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.button"); //$NON-NLS-1$

		if (buttonIndex > bupTrace.samples.size() || buttonIndex < 1)
			return false;

		BupSample sample = bupTrace.getBupSample(buttonIndex - 1);

		// let them change some information
		Display display = PIPageEditor.currentPageEditor().getEditorSite().getShell().getDisplay();

		GridData gridData;

		IBupEventMap map = bupTrace.getCurrentBupMapInUse();
		String enumString = map.getEnum(sample.getKeyCode());

		BupEventDialog eventDialog = new BupEventDialog(PIPageEditor.currentPageEditor().getEditorSite().getShell(), Messages.getString("BupTraceGraph.buttonDialog"), //$NON-NLS-1$
													sample.getKeyCode(), enumString, sample.getLabel(), sample.getComment(), false, sample.sampleSynchTime);
		eventDialog.open();

		if (   (eventDialog.getNewName() != null)
			&& (!eventDialog.getNewName().equals(sample.getLabel()))) {
			sample.setLabel(eventDialog.getNewName());
			redraw = true;
		}

		if (   (eventDialog.getNewComment() != null)
			&& (!eventDialog.getNewComment().equals(sample.getComment()))) {
			sample.setComment(eventDialog.getNewComment());
			redraw = true;
		}
			
		if (eventDialog.getNewSamePropagate()) {
			Vector<GenericSample> allSamples = bupTrace.samples;
			for (GenericSample genericSample : allSamples) {
				BupSample currentSample = (BupSample)genericSample;
				if (currentSample.getKeyCode() == sample.getKeyCode()) {
					currentSample.setLabel(eventDialog.getNewName());
				}
			}
			redraw = true;
		}
		
		return true;
	}

	public void addContextMenuItems(Menu menu, MouseEvent me)
	{
		if (!display_events)
			return;

		long x = me.x;
		long y = me.y;

		int height = this.getVisualSize().height;

		// make sure we're in the right area of the graph window
		if ((y < height - EVENT_BOTTOMOFLINE) || (y > height - EVENT_BOTTOMOFLINE + EVENT_RECTHEIGHT + 2))
			return;

		Enumeration samples = ((GenericSampledTrace) this.getTrace()).samples.elements();
		double scale = this.getScale();

		boolean found = false;

		// unlike mouseMove, the x coordinate is not relative to the FigureCanvas
		x += ((FigureCanvas)me.getSource()).getViewport().getViewLocation().x;

		while (samples.hasMoreElements())
		{
			BupSample sample = (BupSample)samples.nextElement();
			long xSample = (long)(sample.sampleSynchTime/scale + 0.5);

			// samples are in timestamp order, so stop when xSample + EVENT_RECTWIDTH is too high
			if (x < xSample - EVENT_RECTWIDTH)
				break;

			if ((x >= xSample - EVENT_RECTWIDTH) && (x <= xSample + EVENT_RECTWIDTH))
			{
				found = true;
				break;
			}
		}

		if (!found)
			return;

		new MenuItem(menu, SWT.SEPARATOR);

		final MouseEvent meFinal = me;

		MenuItem changeEventItem = new MenuItem(menu, SWT.PUSH);
		changeEventItem.setText(Messages.getString("BupTraceGraph.changeEvent")); //$NON-NLS-1$
		changeEventItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				mouseDoubleClick(meFinal);
			}
		});

		int startTime = (int) this.getSelectionStart();
		int endTime   = (int) this.getSelectionEnd();

		// save raw samples
		Vector sampleVector = ((GenericSampledTrace) this.getTrace()).samples;
		
		int i = 0;
		while (i < sampleVector.size() && ((BupSample) sampleVector.get(i)).sampleSynchTime < startTime)
			i++;
		
		// enable if we have at least one event in the interval
		getSaveSamplesItem(menu, (i < sampleVector.size() && ((BupSample) sampleVector.get(i)).sampleSynchTime <= endTime));
	}
	
	public void saveEventSamples() {
    	SaveSampleString saveSampleString = new SaveSampleString();
    	actionSaveSamples(saveSampleString); //$NON-NLS-1$
	}

	public void paintLeftLegend(FigureCanvas figureCanvas, GC gc) {
	}
}
