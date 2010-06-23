/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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
* Description: 
*
*/



package com.nokia.s60tools.traceanalyser.export;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.nokia.s60tools.traceanalyser.ui.dialogs.TraceSelectionDialog;
import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.activation.TraceActivationTraceItem;


/**
 * class TraceSelectionComposite.
 * Class that can be used for creating UI-components for selecting trace.
 * creates  
 */
public class TraceSelectionComposite implements SelectionListener {

	/* UI-components */
	private Text textTrace;
	private Composite compositeTraceSelection;
	private GridData gridDataTraceSelection;
	
	/* Selected Trace item */
	private TraceInfo traceItem;

	/* selection listener */
	ITraceSelectionCompositeListener listener;
	
	/**	
	 * TraceSelectionComposite.
	 * Constructor for Trace Selection component.
	 * @param composite composite where components are placed.
	 * @param traceName name for the label.
	 * @param listener trace item's change listener(optional).
	 */
	public TraceSelectionComposite(Composite composite, String traceName, ITraceSelectionCompositeListener listener ){
	
		this.listener = listener;
		
		// create Composite for trace selection components 
		compositeTraceSelection = new Composite(composite, SWT.NONE);
 		GridLayout layoutTraceSelection = new GridLayout();
 		layoutTraceSelection.numColumns = 2;
 		compositeTraceSelection.setLayout(layoutTraceSelection);
 		gridDataTraceSelection = new GridData(GridData.FILL_HORIZONTAL);
 		compositeTraceSelection.setLayoutData(gridDataTraceSelection);
 		
 		// create label 
		Label labelTrace = new Label(compositeTraceSelection, SWT.NONE);
		labelTrace.setText(traceName);
		GridData gridDataLabelTrace = new GridData(GridData.FILL_HORIZONTAL);
		gridDataLabelTrace.horizontalSpan = 2;
		labelTrace.setLayoutData(gridDataLabelTrace);
		
		// created trace name text field.
		textTrace = new Text(compositeTraceSelection, SWT.BORDER);
		GridData gridDataTextTrace = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textTrace.setToolTipText("Trace that is assigned for this rule.");
		textTrace.setLayoutData(gridDataTextTrace);
		textTrace.setEditable(false);
 		
		// Create select trace button.
		Button buttonSelectTrace = new Button(compositeTraceSelection, SWT.PUSH);
		buttonSelectTrace.setText("Select Trace");
		buttonSelectTrace.addSelectionListener(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent arg0) {
		// create dialog
		TraceSelectionDialog dialog = new TraceSelectionDialog( compositeTraceSelection.getShell() );
		int retVal = dialog.open();
		// open query dialog
		if( retVal == 0 ){
			//save trace;
			TraceActivationTraceItem selectedItem = dialog.getSelectedTrace();
			
			traceItem = new TraceInfo();
			TraceInformation idNumbers = new TraceInformation();
			idNumbers.setComponentId(selectedItem.getParent().getParent().getId());
			idNumbers.setGroupId(selectedItem.getParent().getId());
			idNumbers.setTraceId(selectedItem.getId());
			traceItem.setIdNumbers(idNumbers);
			traceItem.setTraceName(selectedItem.getName());
						
			this.updateTraceInfo();
			if(listener != null){
				listener.traceInfoUpdated();
			}
		}
		
		// Cancel pressed
		else if( retVal == 1 ){
				
		}
	}
	
	/**
	 * setVisible.
	 * Method that changes visibility of UI-components.
	 * @param value true, if components need to be shown.
	 */
	public void setVisible(boolean value){
		gridDataTraceSelection.exclude = !value;
		compositeTraceSelection.setVisible(value);
		
	}
	
	/**
	 * updateTraceInfo.
	 * updates trace info into trace text box.
	 */
	private void updateTraceInfo(){
		if( traceItem != null){
			String text = traceItem.getTraceName();
			
			// if trace name is null, set trace id numbers into text box
			if(text == null || text.equals("null")){
				text = "Trace name not found( ComponentID = ";
				text += Integer.toHexString(traceItem.getIdNumbers().getComponentId());
				text += ", GroupID = ";
				text += Integer.toHexString(traceItem.getIdNumbers().getGroupId());
				text += ", TraceID = ";
				text += Integer.toHexString(traceItem.getIdNumbers().getTraceId());
				text += " )";
			}
			this.textTrace.setText(text);
		}
	}
	
	/**
	 * getTraceInformation.
	 * @return selected trace item.
	 */
	public TraceInfo getTraceInformation(){
		return traceItem;

	}
	
	
	/**
	 * SetTraceItem.
	 * Sets trace item and updates 
	 * @param traceItem a new trace item.
	 */
	public void setTraceItem(TraceInfo traceItem) {
		this.traceItem = traceItem;
		updateTraceInfo();
	}
	
}
