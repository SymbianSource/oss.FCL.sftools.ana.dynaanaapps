/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of "Eclipse Public License v1.0"
 * which accompanies this distribution, and is available
 * at the URL "http://www.eclipse.org/legal/epl-v10.html".
 *
 * Initial Contributors:
 * Nokia Corporation - initial contribution.
 *
 * Contributors:
 *
 * Description:  Definitions for the class YAxis
 *
 */
package com.nokia.s60tools.analyzetool.internal.ui.graph;
import java.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

import com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel;
import com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener;
import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;
import com.nokia.s60tools.analyzetool.internal.ui.util.GraphUtils;

/**
 * FigureCanvas containing the Y axis of the AnalyzeTool graph
 *
 */
public class YAxis extends Canvas implements IMemoryActivityModelChangeListener {
	private static final int YLEGENDSPACE = 60;
	private static final DecimalFormat MB_FORMAT = new DecimalFormat("#####.0");
	private IMemoryActivityModel model;
	protected int visualSizeY = 0;
	private UIJob iRefreshUIJob;
	private Image bytesImage;

	/**
	 * Constructor
	 * @param parent
	 */
	public YAxis(Composite parent) {
		super(parent, SWT.NONE);
	}
	/**
	 * To be called once after construction.
	 */
	public void createContent() {
		iRefreshUIJob = new UIJob("Updating Memory Activity Graph"){

			@Override
			public IStatus runInUIThread(IProgressMonitor arg0) {
				// Refresh the graph
				redraw();
				return Status.OK_STATUS;
			}
			
		};
		iRefreshUIJob.setSystem(true);		
	}

	/**
	 * Sets a new model as input
	 * @param newModel
	 */
	public void setInput(IMemoryActivityModel newModel) {
		if (this.model != null){
			this.model.removeListener(this);
		}
		this.model = newModel;
		this.model.addListener(this);
	}
	
	/**
	 * Called when paint event occurs. 
	 * @param gc The GC to draw on
	 */
	@SuppressWarnings("cast")
	public void paintYAxis(GC gc){
		
		
		double visY = visualSizeY - 50;
		//max value influences unit of measure on graph
		int prettyBytes = GraphUtils.prettyMaxBytes(model.getHighestCumulatedMemoryAlloc());
		double multiplier = prettyBytes / visY;
		
		double yIncrement = visY / 10;
		int previousBottom = 0;
		
		for (double k = visY; k >= 0; k-=yIncrement)
		{
			// location for the value indicator is k * 1/10 the height of the display
			int y = (int) (visY - k);
		
			int bytes = (int)(Math.ceil(k * multiplier));

			String legend = "";
			
			if (prettyBytes < 10*1024)
			{
				legend += bytes + " B"; 
			}
			else if (prettyBytes <= 500 * 1024)
			{
				legend += (bytes / 1024) + " KB"; 
			}
			else
			{
				legend +=  MB_FORMAT.format(((float) bytes / (1024 * 1024)))  + " MB";
			}
			org.eclipse.swt.graphics.Point extent = gc.stringExtent(legend);
			
			gc.drawLine(YLEGENDSPACE - 3, y + 1, YLEGENDSPACE, y + 1);
			
			if (y >= previousBottom)
			{
				gc.drawString(legend, YLEGENDSPACE - extent.x -2, y);
				previousBottom = y + extent.y;
			}
		}
	
		if (bytesImage == null){
			bytesImage = GraphUtils.getVerticalLabel("Bytes");			
		}
		gc.setAdvanced(true);
	    final org.eclipse.swt.graphics.Rectangle rect2 = bytesImage.getBounds();
        Transform transform = new Transform(Display.getDefault());

        transform.translate(rect2.height / 2f, rect2.width / 2f);
        transform.rotate(-90);
        transform.translate(-rect2.width / 2f, -rect2.height / 2f);

        gc.setTransform(transform);
        gc.drawImage(bytesImage, -(int)visY/3, 1);
        
        transform.dispose();
	}
	
	/**
	 * @return the visible height of the control
	 */
	public int getHeight() {
		return visualSizeY;
	}
	/**
	 * Sets the visible height of the control
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		visualSizeY = height;
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener#onProcessesAdded()
	 */
	public void onProcessesAdded() {
		iRefreshUIJob.cancel();
		iRefreshUIJob.schedule();
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener#onProcessSelected(com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo)
	 */
	public void onProcessSelected(ProcessInfo p) {
		iRefreshUIJob.cancel();
		iRefreshUIJob.schedule();
	}
	
//	public void dispose() {
//	//TODO cancel ui jobs here and remove listeners
//	//there is no dispose() entry point in this class; we may have to call this via MainView.dispose()
//}

}
