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
package com.nokia.carbide.cpp.internal.pi.address;

import java.awt.event.ActionListener;
import java.awt.event.FocusListener;

import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.visual.Defines;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.pi.address.GppSample;
import com.nokia.carbide.cpp.pi.address.GppTrace;
import com.nokia.carbide.cpp.pi.address.GppTraceGraph;
import com.nokia.carbide.cpp.pi.address.IGppTraceGraph;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

/**
 * 
 * Main class to draw charts for SMP traces. Manages one SMP graph for a single CPU and its associated
 * legend checkbox table. 
 * 
 */
public class GppTraceGraphSMP extends GppTraceGraph implements IGppTraceGraph, 
		ActionListener, FocusListener, MouseMotionListener,
		MouseListener, MouseMoveListener {


	/** the index of the CPU for this graph */
	private int cpuIndex;
	/** type of graph, must be one of PIPageEditor.THREADS_PAGE, PIPageEditor.BINARIES_PAGE, PIPageEditor.FUNCTIONS_PAGE */
	private int graphType;

	/**
	 * Constructor
	 * @param graphIndex The graph index
	 * @param gppTrace The trace model data to use
	 * @param uid general Uid of this editor session
	 * @param cpuIndex Index of the CPU
	 * @param graphType The type of graph, one of PIPageEditor.THREADS_PAGE, PIPageEditor.BINARIES_PAGE, PIPageEditor.FUNCTIONS_PAGE
	 */
	public GppTraceGraphSMP(int graphIndex, GppTrace gppTrace, int uid, int cpuIndex, int graphType) {
		super(graphIndex, gppTrace, uid);
		this.graphIndex = graphIndex;
		this.cpuIndex = cpuIndex;
		this.graphType = graphType;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.address.GppTraceGraph#doCreateAdapter()
	 */
	@Override
	protected void doCreateAdapter() {
		adapter = new GppModelAdapter(this.cpuIndex, graphType);
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.address.GppTraceGraph#createLegendTables(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createLegendTables(Composite holdTablesComposite) {
		this.threadTable   = new AddrThreadTableSMP(this, holdTablesComposite, adapter);
		this.binaryTable   = new AddrBinaryTableSMP(this, holdTablesComposite, adapter);
		this.functionTable = new AddrFunctionTableSMP(this, holdTablesComposite, adapter);
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.visual.PIEventListener#piEventReceived(com.nokia.carbide.cpp.internal.pi.visual.PIEvent)
	 */
	@Override
	public void piEventReceived(PIEvent be) {
		switch (be.getType())
		{
				
			// when the selection area changes, change the percent loads
			// and the sample counts in all tables of this GPP graph
			case PIEvent.SELECTION_AREA_CHANGED:
				// this is the first GPP graph to be told of the selection area change,
				// so it gathers the overall trace information
				double startTime = PIPageEditor.currentPageEditor().getStartTime();
				double endTime   = PIPageEditor.currentPageEditor().getEndTime();
				GppTrace trace = (GppTrace)this.getTrace();
				trace.setSelectedArea(startTime, endTime);
				
				// recalculate threshold items
				//CH: why? doesn't look like it takes the selected timeframe into account
				//TODO: CH: do we need to recalculate the thresholds for binaries and functions too?
				int sampleCount = 0;
				int thresholdCount = (Integer)NpiInstanceRepository.getInstance().getPersistState(uid, "com.nokia.carbide.cpp.pi.address.thresholdCountThread"); //$NON-NLS-1$
				if (thresholdCount > 0) {
					for (ProfiledGeneric pt : profiledThreads) {
						if (pt.getTotalSampleCountForSMP(cpuIndex) < thresholdCount){
							sampleCount += pt.getTotalSampleCountForSMP(cpuIndex);							
						}
					}
				}
				thresholdThread.setSampleCount(this.graphIndex, sampleCount);

				
				// send this message to the other GPP graphs
				PIEvent be2 = new PIEvent(be.getValueObject(), PIEvent.SELECTION_AREA_CHANGED2);
				
				// update the selection area shown
				for (int i = 0; i < trace.getGraphCount(); i++)
				{
					IGppTraceGraph graph = trace.getGppGraph(i, getUid());

					if (graph != this) {
						graph.piEventReceived(be2);
						// once per graph, update the selection interval shown
						graph.getCompositePanel().getVisualiser().updateStatusBarTimeInterval(startTime, endTime);
					}
					
					// change the graph's selected time interval
					graph.setSelectionStart(startTime * 1000);
					graph.setSelectionEnd(endTime * 1000);
					graph.getCompositePanel().setSelectionFields((int)(startTime * 1000), (int)(endTime * 1000));
				}

				this.parentComponent.getSashForm().redraw();
				be = be2;
				// FALL THROUGH
			case PIEvent.SELECTION_AREA_CHANGED2:
			{
				// this code lets each graph's base thread/binary/function table update the other tables
		        this.threadTable.piEventReceived(be);

				this.vPanel.refreshCumulativeThreadTable();
				this.setGraphImageChanged(true);	// any selection change to drill down will change graph
				this.repaint();
				break;
			}
				
			default:{
				super.piEventReceived(be);
				break;				
			}
				
		}
	}
	
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.address.GppTraceGraph#doCheckSampleMatch(com.nokia.carbide.cpp.pi.address.GppSample)
	 */
	@Override
	protected boolean doCheckSampleMatch(GppSample sample){
		return sample.cpuNumber == this.cpuIndex;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.address.GppTraceGraph#sampleInChart(com.nokia.carbide.cpp.pi.address.GppSample)
	 */
	@Override
	protected boolean sampleInChart(GppSample sample) {
		return sample.cpuNumber == this.cpuIndex;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.address.GppTraceGraph#refreshColoursFromTrace()
	 */
	@Override
	public void refreshColoursFromTrace() {
		//assuming that the SMP graphs are on the Threads page
		getThreadTable().addColor(Defines.THREADS);
	}
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.address.GppTraceGraph#doSetTitle()
	 */
	@Override
	protected void doSetTitle() {
		title = String.format(com.nokia.carbide.cpp.internal.pi.address.Messages.GppTraceGraphSMP_0, this.cpuIndex);
		shortTitle = String.format(com.nokia.carbide.cpp.internal.pi.address.Messages.GppTraceGraphSMP_1, this.cpuIndex);
	}

}
