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

package com.nokia.carbide.cpp.internal.pi.power.actions;

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.power.PwrTrace;


public class PowerSettingsDialog {

	private Shell shell;
	private GridData gridData;
	private PwrTrace trace;
	private int batterySize;

	private Text voltageText;
	private Text batteryText;
	private DecimalFormat voltageFormat = new DecimalFormat(Messages.getString("PowerSettingsDialog.voltageFormat")); //$NON-NLS-1$
//	private DecimalFormat batteryFormat = new DecimalFormat("###0");
	private float newVoltage;
	private float newBatterySize;
	
	public PowerSettingsDialog(Display display)
	{
		shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText(Messages.getString("PowerSettingsDialog.powerSettingsTitle")); //$NON-NLS-1$
		shell.setLayout(new GridLayout(4, false));
		
		trace = (PwrTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.power"); //$NON-NLS-1$
		
		float voltage = trace.getVoltage();
		newVoltage = voltage;

		Label voltLabel = new Label(shell, SWT.LEFT);
		voltLabel.setFont(PIPageEditor.helvetica_9);
		voltLabel.setText(Messages.getString("PowerSettingsDialog.voltageLabel")); //$NON-NLS-1$
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		voltLabel.setLayoutData(gridData);

		voltageText = new Text(shell, SWT.BORDER | SWT.RIGHT);
		voltageText.setFont(PIPageEditor.helvetica_9);
		voltageText.setText(voltageFormat.format(voltage));
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumWidth = 60;
		voltageText.setLayoutData(gridData);
		
		Label voltUnitsLabel = new Label(shell, SWT.LEFT);
		voltUnitsLabel.setFont(PIPageEditor.helvetica_9);
		voltUnitsLabel.setText(Messages.getString("PowerSettingsDialog.voltageUnits")); //$NON-NLS-1$

		batterySize = (int) trace.getBatterySize();
		newBatterySize = batterySize;

		Label batteryLabel = new Label(shell, SWT.LEFT);
		batteryLabel.setFont(PIPageEditor.helvetica_9);
		batteryLabel.setText(Messages.getString("PowerSettingsDialog.batteryLabel")); //$NON-NLS-1$
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		batteryLabel.setLayoutData(gridData);

		batteryText = new Text(shell, SWT.BORDER | SWT.RIGHT);
		batteryText.setFont(PIPageEditor.helvetica_9);
		batteryText.setText("" + batterySize);//batteryFormat.format(batterySize)); //$NON-NLS-1$
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumWidth = 60;
		batteryText.setLayoutData(gridData);

		Label batteryUnitsLabel = new Label(shell, SWT.LEFT);
		batteryUnitsLabel.setFont(PIPageEditor.helvetica_9);
		batteryUnitsLabel.setText(Messages.getString("PowerSettingsDialog.batteryUnits")); //$NON-NLS-1$

		// add the voltage listeners
		voltageText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				double currentVoltage = convert(voltageText, trace.getVoltage(), 0, Float.MAX_VALUE, voltageFormat);
				newVoltage = (float) (Math.ceil(currentVoltage * 1000) / 1000.0);
			}
		});

		voltageText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				double currentVoltage = convert(voltageText, trace.getVoltage(), 0, Float.MAX_VALUE, voltageFormat);
				newVoltage = (float) (Math.ceil(currentVoltage * 100) / 100.0);
			}
		});

		batteryText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				newBatterySize = convert(batteryText, (int)Math.ceil(trace.getBatterySize()), 0, Integer.MAX_VALUE); 
			}
		});

		batteryText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				newBatterySize = convert(batteryText, (int)Math.ceil(trace.getBatterySize()), 0, Integer.MAX_VALUE); 
			}
		});

		Label line = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 4;
		gridData.minimumHeight = 10;
		line.setLayoutData(gridData);

		// create the OK button
		Button ok = new Button(shell, SWT.NONE);
		ok.setText(Messages.getString("PowerSettingsDialog.ok")); //$NON-NLS-1$
		gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		gridData.minimumWidth = 60;
		gridData.horizontalSpan = 2;
		ok.setLayoutData(gridData);
		ok.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				// Update plugins with actions, if voltage of battery size has changed
				boolean changedVoltage  = trace.getVoltage() != newVoltage;
				boolean changedSize     = trace.getBatterySize() != newBatterySize;
				int uid = NpiInstanceRepository.getInstance().activeUid();

				if (changedVoltage) {
					trace.setVoltage(newVoltage);
					trace.getPowerGraph(PIPageEditor.THREADS_PAGE,   uid).action("changeVoltage"); //$NON-NLS-1$
					trace.getPowerGraph(PIPageEditor.BINARIES_PAGE,  uid).action("changeVoltage"); //$NON-NLS-1$
					trace.getPowerGraph(PIPageEditor.FUNCTIONS_PAGE, uid).action("changeVoltage"); //$NON-NLS-1$
				}

				if (changedSize) {
					trace.setBatterySize(newBatterySize);
					trace.getPowerGraph(PIPageEditor.THREADS_PAGE,   uid).action("changeBatterySize"); //$NON-NLS-1$
					trace.getPowerGraph(PIPageEditor.BINARIES_PAGE,  uid).action("changeBatterySize"); //$NON-NLS-1$
					trace.getPowerGraph(PIPageEditor.FUNCTIONS_PAGE, uid).action("changeBatterySize"); //$NON-NLS-1$
				}

				shell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// create the Cancel button
		Button cancel = new Button(shell, SWT.NONE);
		cancel.setText(Messages.getString("PowerSettingsDialog.cancel")); //$NON-NLS-1$
		gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		gridData.minimumWidth = 60;
		gridData.horizontalSpan = 2;
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

	private double convert(Text text, double currentDouble, double minimum, double maximum, DecimalFormat format)
	{
		double new_double;

		// convert, catch (NumberFormatException e1)
		try {
			new_double = Double.parseDouble(text.getText().replace(',','.'));
			if (new_double >= 0)
				currentDouble = new_double;
		} catch (NumberFormatException exc) {
			// just keep the old value
		}

		if (currentDouble > maximum)
			currentDouble = maximum;
		else if (currentDouble < minimum)
			currentDouble = minimum;

		text.setText(format.format(currentDouble));

		return currentDouble;
	}

	private int convert(Text text, int currentInt, int minimum, int maximum)
	{
		int new_int;

		// convert, catch (NumberFormatException e1)
		try {
			new_int = Integer.parseInt(text.getText().replace(',','.'));
			if (new_int >= 0)
				currentInt = new_int;
		} catch (NumberFormatException exc) {
			// just keep the old value
		}

		if (currentInt > maximum)
			currentInt = maximum;
		else if (currentInt < minimum)
			currentInt = minimum;

		text.setText("" + currentInt); //$NON-NLS-1$

		return currentInt;
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
