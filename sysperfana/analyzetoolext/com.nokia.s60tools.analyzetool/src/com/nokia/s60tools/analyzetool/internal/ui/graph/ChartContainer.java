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
 * Description:  Definitions for the class ChartContainer
 *
 */

package com.nokia.s60tools.analyzetool.internal.ui.graph;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.AnalyzeToolHelpContextIDs;
import com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel;
import com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener;
import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;

/**
 *  Container for the complete AnalyzeTool chart (x and y axis as well as graph)
 *
 */
public class ChartContainer extends Composite implements IMemoryActivityModelChangeListener{
	
	private static final String ICON_PROPVIEW = "icons/properties.gif";//$NON-NLS-1$
	private static final String ICON_HELP = "icons/linkto_help.gif";//$NON-NLS-1$
	
	private AnalyzeToolGraph graphCanvas;
	private YAxis yAxis;
	private Combo processCombo;
	private IMemoryActivityModel model;
	private UIJob iRefreshUIJob;
	
	/**
	 * Constructor
	 * 
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot
	 *            be null)
	 * @param style
	 *            the style of widget to construct
	 */
	public ChartContainer(Composite parent, int style) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		constructChartArea();
	}
	
	/**
	 * Builds up the main composite when opening for the first time
	 */
	private void constructChartArea(){
		setLayout(new FormLayout());
			
		Label processLabel = new Label(this, SWT.CENTER);
		processLabel.setText("Process:");
		processCombo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);

		
		ToolBar toolBar = new ToolBar (this, SWT.RIGHT | SWT.FLAT);
		
		//toolbar button to open the Eclipse Properties view
		ToolItem propViewItem = new ToolItem (toolBar, SWT.PUSH | SWT.FLAT );
		propViewItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				//do nothing by design
			}

			public void widgetSelected(SelectionEvent e) {
                try {
                    PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage()
                    .showView("org.eclipse.ui.views.PropertySheet");//$NON-NLS-1$
            } catch (PartInitException ex) {
                    Activator.getDefault().log(IStatus.ERROR, ex.getMessage(), ex);
            }
			}
			
		});
		propViewItem.setImage(Activator.getDefault().getImage(ICON_PROPVIEW));
		propViewItem.setToolTipText("Show Properties View");

		//toolbar button to show some navigation help and open the F1 help
		ToolItem helpItem = new ToolItem (toolBar, SWT.PUSH | SWT.FLAT );
		helpItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				//do nothing by design
			}

			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(AnalyzeToolHelpContextIDs.ANALYZE_GRAPH);
			}
			
		});
		helpItem.setImage(Activator.getDefault().getImage(ICON_HELP));
		helpItem.setToolTipText("Quick navigtion help\n-------------------------\nZoom in: select a region of the graph (mouse left-click and drag).\nZoom out: mouse right-click.\n\nSelect memory operation: mouse left-click in the graph.\nMove selection to next or previous using arrow left and arrow right keys.\n\nFor more help press F1");
		
		yAxis = new YAxis(this);
		yAxis.createContent();
		graphCanvas = new AnalyzeToolGraph(this);
		graphCanvas.setSize(500, 450);
		
		FormData formData = new FormData();
		formData.top = new FormAttachment(0,2);
		formData.left   = new FormAttachment(0, 2);
		formData.height = processCombo.getBounds().height;
		processLabel.setLayoutData(formData);
		
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left   = new FormAttachment(processLabel, 10, SWT.BOTTOM);
		processCombo.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 2);
		formData.right   = new FormAttachment(100, 2);
		toolBar.setLayoutData(formData);
		
		formData = new FormData();
		formData.top = new FormAttachment(processCombo, 5, SWT.BOTTOM);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(0);
		formData.width  = 60;
		yAxis.setLayoutData(formData);
		
		formData = new FormData();
		formData.top = new FormAttachment(processCombo, 5, SWT.BOTTOM);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(yAxis, 0, SWT.RIGHT);
		formData.right  = new FormAttachment(100);
		graphCanvas.setLayoutData(formData);
		
		yAxis.setBackground(ColorConstants.white);
		yAxis.addPaintListener(new PaintListener()
		{

			public void paintControl(PaintEvent event) {
				
				if(yAxis != null)
					yAxis.paintYAxis(event.gc);
			}		
		});
		
		graphCanvas.setBackground(new Color(Display.getDefault(), new RGB(255,255,255)));
		graphCanvas.createContent();
		graphCanvas.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				//the client area of the graph panel might change if a scrollbar gets added/removed 
				//this needs to be passed on to the y axis
				if (yAxis.getHeight() !=  graphCanvas.getClientArea().height){
					yAxis.setHeight(graphCanvas.getClientArea().height);
					yAxis.redraw();
				}
			}
			
		});
		
		iRefreshUIJob = new UIJob("Updating process combo box"){

			@Override
			public IStatus runInUIThread(IProgressMonitor arg0) {
				processCombo.removeAll();
				// Update combo box
				if (model.getProcesses().size() == 0){
					processCombo.add("No data available");
					processCombo.select(0);
				} else {
					for (ProcessInfo p : model.getProcesses()) {
						processCombo.add(String.format("%s with %d leak(s)",p.getProcessName() != null ? p.getProcessName() : p.getProcessID(), p.getMemLeaksNumber()));
					}
					processCombo.setVisibleItemCount(10);
				}
				processCombo.pack();
				return Status.OK_STATUS;
			}
			
		};
		iRefreshUIJob.setSystem(true);
		
		processCombo.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				//do nothing by design
			}

			public void widgetSelected(SelectionEvent e) {
				int sel = processCombo.getSelectionIndex();
				if (model.getProcesses().size()>sel){
					ProcessInfo process = model.getProcesses().get(processCombo.getSelectionIndex());
					model.setSelectedProcess(process);
					processCombo.setSize(processCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			}
			
		});
		
				
		layout();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				this,
				AnalyzeToolHelpContextIDs.ANALYZE_GRAPH);
	}

	/**
	 * Sets a new model. Note the redraw of the new content only happens on 
	 * callbacks to model change listeners.
	 * @param aProject The currently selected project in the IDE, used for pinpointing
	 * 
	 * @param newModel the new IMemoryActivityModel to set
	 */
	public void setInput(IProject aProject, IMemoryActivityModel newModel) {
		if (this.model != null){
			this.model.removeListener(this);
		}
		this.model = newModel;
		this.model.addListener(this);

		graphCanvas.setInput(model);
		yAxis.setInput(model);
		if (aProject != null) {
			graphCanvas.setProject(aProject);
		}
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener#onProcessSelected(com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo)
	 */
	public void onProcessSelected(ProcessInfo p) {
		//do nothing by design
	}

	/* (non-Javadoc)
	 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener#onProcessesAdded()
	 */
	public void onProcessesAdded() {
		iRefreshUIJob.cancel();
		iRefreshUIJob.schedule();
		
	}	
	
	@Override
	public void update(){
		graphCanvas.zoomGraph();
		graphCanvas.redraw();
		yAxis.redraw();
	}
}
