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

/**
 * 
 */
package com.nokia.carbide.cpp.internal.pi.actions;

import java.text.DecimalFormat;
import java.util.Enumeration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.carbide.cpp.internal.pi.analyser.PIChangeEvent;
import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;


public class TimeSetDialog {

	private Shell shell;
	private Label label;
	private Text startTimeText;
	private Text endTimeText;
	private Label labelMsg;
	private Button ok;
	private Button cancel;
	private GridData gridData;
	private double startTime;
	private double endTime;
	private DecimalFormat timeFormat = new DecimalFormat(Messages.getString("TimeSetDialog.decimalFormat")); //$NON-NLS-1$
	
	public TimeSetDialog(Display display, double start, double end)
	{
		this.startTime = start;
		this.endTime   = end;
		
		shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText(Messages.getString("TimeSetDialog.selectInterval")); //$NON-NLS-1$
		shell.setLayout(new GridLayout(2, true));
		
		// create the start time label and input box
		label = new Label(shell, SWT.LEFT);
		label.setText(Messages.getString("TimeSetDialog.startTime")); //$NON-NLS-1$
		label.setFont(PIPageEditor.helvetica_9);
		
		startTimeText = new Text(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		startTimeText.setLayoutData(gridData);
		startTimeText.setFont(PIPageEditor.helvetica_9);
		startTimeText.setData(Messages.getString("TimeSetDialog.startMustBeFloat")); //$NON-NLS-1$
		startTimeText.setText(timeFormat.format(startTime));
		
		// add the listeners
		startTimeText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				startTime = convert(startTimeText, startTime);
				startTimeText.setText(timeFormat.format(startTime));
			}
		});
		startTimeText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				startTime = convert(startTimeText, startTime);
				startTimeText.setText(timeFormat.format(startTime));
			}
		});
		startTimeText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				startTime = convert(startTimeText, startTime);
				checkOk();
			}
		});
		
		// create the start time label and input box
		label = new Label(shell, SWT.LEFT);
		label.setText(Messages.getString("TimeSetDialog.endTime")); //$NON-NLS-1$
		label.setFont(PIPageEditor.helvetica_9);

		endTimeText = new Text(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		endTimeText.setLayoutData(gridData);
		endTimeText.setData(Messages.getString("TimeSetDialog.endMustBeFloat")); //$NON-NLS-1$
		endTimeText.setFont(PIPageEditor.helvetica_9);
		endTimeText.setText(timeFormat.format(endTime));
		
		// add the listeners
		endTimeText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				endTime = convert(endTimeText, endTime);
				endTimeText.setText(timeFormat.format(endTime));
			}
		});
		endTimeText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				endTime = convert(endTimeText, endTime);
				endTimeText.setText(timeFormat.format(endTime));
			}
		});
		endTimeText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				endTime = convert(endTimeText, endTime);
				checkOk();
			}
		});

		label = new Label(shell, SWT.LEFT);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		gridData.verticalAlignment = GridData.CENTER;
		label.setLayoutData(gridData);

		double maxEndTime = PIPageEditor.currentPageEditor().getMaxEndTime(); 
		labelMsg = new Label(shell, SWT.LEFT);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		gridData.verticalAlignment = GridData.CENTER;
		labelMsg.setLayoutData(gridData);
		labelMsg.setFont(PIPageEditor.helvetica_9);
		labelMsg.setText(Messages.getString("TimeSetDialog.maxEndTime1") + maxEndTime + Messages.getString("TimeSetDialog.maxEndTime2")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// create the OK button
		ok = new Button(shell, SWT.NONE);
		ok.setText(Messages.getString("TimeSetDialog.ok")); //$NON-NLS-1$
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		ok.setLayoutData(gridData);
		ok.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				PIPageEditor.currentPageEditor().setLocalTime(startTime, endTime);
				PIChangeEvent.action("changeSelection"); //$NON-NLS-1$
				
				// after the graphs have been updated, notify plugins that might have tables but no graphs
        		Enumeration enu = PluginInitialiser.getPluginInstances(
        									"com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
        		if (enu != null) {
        			Event event = new Event();
        			event.start = (int) (startTime * 1000);
        			event.end   = (int) (endTime   * 1000);
        			
            		while (enu.hasMoreElements())
            		{
            			IEventListener plugin = (IEventListener)enu.nextElement();
            			plugin.receiveEvent("changeSelection", event); //$NON-NLS-1$
            		}
        		}

				shell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		// create the Cancel button
		cancel = new Button(shell, SWT.NONE);
		cancel.setText(Messages.getString("TimeSetDialog.cancel")); //$NON-NLS-1$
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		cancel.setLayoutData(gridData);
		cancel.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		shell.pack();
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}
	}

	private void checkOk() {
		double maxEndTime = PIPageEditor.currentPageEditor().getMaxEndTime(); 
		boolean isOk = false;
		
		if (startTime > endTime) {
			labelMsg.setText(Messages.getString("TimeSetDialog.endTimeMustBeGreater")); //$NON-NLS-1$
			labelMsg.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
		} else if (!isValidPositive(startTimeText) || !isValidPositive(endTimeText)) {
			labelMsg.setText(Messages.getString("TimeSetDialog.timesMustBePositiveValues")); //$NON-NLS-1$
			labelMsg.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
		} else if (startTime > maxEndTime || endTime > maxEndTime) {
			labelMsg.setText(Messages.getString("TimeSetDialog.maxEndTime1") + maxEndTime + Messages.getString("TimeSetDialog.maxEndTime2")); //$NON-NLS-1$ //$NON-NLS-2$
			labelMsg.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
		} else {
			isOk = true;
			labelMsg.setText(Messages.getString("TimeSetDialog.maxEndTime1") + maxEndTime + Messages.getString("TimeSetDialog.maxEndTime2")); //$NON-NLS-1$ //$NON-NLS-2$
			labelMsg.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		}
		ok.setEnabled(isOk);
	}
	
	private double convert(Text text, double currentTime)
	{
		double new_double;
		
		// convert, catch (NumberFormatException e1)
		try {
			new_double = Double.parseDouble(text.getText().replace(',','.'));
			if (new_double == -0.0)
				currentTime = 0.0f;
			else if (new_double >= 0)
				currentTime = new_double;
		} catch (NumberFormatException exc) {
			// just keep the old value
		}
		
		return currentTime;
	}
	
	private boolean isValidPositive(Text text) {
		try {
			return Double.parseDouble(text.getText().replace(',','.')) >= 0;
		} catch (NumberFormatException exc) {
			return false;
		}
	}
	
	public void dispose()
	{
		if (this.shell != null) {
			if (!this.shell.isDisposed()) {
				this.shell.close();				
			}
			this.shell.dispose();
		}
		
		this.shell = null;
	}
}
