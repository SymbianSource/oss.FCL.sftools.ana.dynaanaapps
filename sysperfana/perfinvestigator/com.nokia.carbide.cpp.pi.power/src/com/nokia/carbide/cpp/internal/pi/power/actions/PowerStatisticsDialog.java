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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;
import com.nokia.carbide.cpp.pi.power.PowerTraceGraph;
import com.nokia.carbide.cpp.pi.power.PwrTrace;


public class PowerStatisticsDialog {

	private Shell shell;
	private GridData gridData;
	private DecimalFormat voltageFormat     = new DecimalFormat(Messages.getString("PowerStatisticsDialog.voltageFormat")); //$NON-NLS-1$
	private DecimalFormat powerFormat       = new DecimalFormat(Messages.getString("PowerStatisticsDialog.powerFormat")); //$NON-NLS-1$

	private PwrTrace trace;
	private double startTime;
	private double endTime;
	
	private double levelMaxPower;
	private double level90Power;
	private double level75Power;
	private double level50Power;
	private double level25Power;
	private double level10Power;
	private double levelMinPower;
	private double levelVarPower;

	public PowerStatisticsDialog(Display display)
	{
		int batterySize;
		float voltage;
		Group group;
		
		shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText(Messages.getString("PowerStatisticsDialog.statsTitle")); //$NON-NLS-1$
		shell.setLayout(new GridLayout(4, false));

    	startTime = PIPageEditor.currentPageEditor().getStartTime();
    	endTime   = PIPageEditor.currentPageEditor().getEndTime();

		trace = (PwrTrace)NpiInstanceRepository.getInstance().activeUidGetTrace("com.nokia.carbide.cpp.pi.power"); //$NON-NLS-1$
	
		int uid = NpiInstanceRepository.getInstance().activeUid();

		PowerTraceGraph graph = trace.getPowerGraph(0, uid); // since they are in lockstep, any graph will do

		voltage = trace.getVoltage();
		batterySize = (int) trace.getBatterySize();

		calculateStats();
		
		group = new Group(shell, SWT.SHADOW_NONE);
		group.setText(Messages.getString("PowerStatisticsDialog.interval")); //$NON-NLS-1$
		group.setFont(PIPageEditor.helvetica_9);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 4;
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(4, false));//new FillLayout());
		textGrid(group, showTimeInterval(startTime, endTime), SWT.CENTER, SWT.CENTER, 4);

		group = new Group(shell, SWT.NONE);
		group.setText(Messages.getString("PowerStatisticsDialog.battery")); //$NON-NLS-1$
		group.setFont(PIPageEditor.helvetica_9);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 4;
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(4, false));//new FillLayout());
		textGrid(group, batterySize + Messages.getString("PowerStatisticsDialog.capacityVoltage2"), SWT.LEFT, SWT.CENTER, 2); //$NON-NLS-1$ //$NON-NLS-2$
		textGrid(group, Messages.getString("PowerStatisticsDialog.capacityVoltage3") + voltageFormat.format(voltage) + Messages.getString("PowerStatisticsDialog.capacityVoltage4"), SWT.RIGHT, SWT.CENTER, 2); //$NON-NLS-1$ //$NON-NLS-2$
		
		group = new Group(shell, SWT.NONE);
		group.setText(Messages.getString("PowerStatisticsDialog.currentSelection")); //$NON-NLS-1$
		group.setFont(PIPageEditor.helvetica_9);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 4;
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(4, false));//new FillLayout());

		double meanPower;
		double meanEnergy;
		long   meanLife;

		meanPower  = graph.getAverageConsumption();
		meanEnergy = graph.getCumulativeConsumption();
		
		if (meanPower == 0)
			meanLife = 0;
		else
			meanLife = Math.round((float) graph.getBatterySize() * 3600 * graph.getVoltage() / meanPower);

		textGrid(group, Messages.getString("PowerStatisticsDialog.energy1"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, (int) (meanEnergy + 0.5)
						+ Messages.getString("PowerStatisticsDialog.energy2"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		long hours   = meanLife / 3600;
		long minutes = (meanLife - hours * 3600) / 60;
		textGrid(group, Messages.getString("PowerStatisticsDialog.life1"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, hours + Messages.getString("PowerStatisticsDialog.life2") //$NON-NLS-1$
						+ minutes + Messages.getString("PowerStatisticsDialog.life3"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		textGrid(group, "", SWT.RIGHT, SWT.RIGHT, 4); //$NON-NLS-1$

		textGrid(group, Messages.getString("PowerStatisticsDialog.avgPower1"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, powerFormat.format(meanPower) + Messages.getString("PowerStatisticsDialog.avgPower2"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		textGrid(group, Messages.getString("PowerStatisticsDialog.maxa"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, powerFormat.format(levelMaxPower / 1000) + Messages.getString("PowerStatisticsDialog.maxb"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		textGrid(group, Messages.getString("PowerStatisticsDialog.90a"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, powerFormat.format(level90Power / 1000) + Messages.getString("PowerStatisticsDialog.90b"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		textGrid(group, Messages.getString("PowerStatisticsDialog.75a"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, powerFormat.format(level75Power / 1000) + Messages.getString("PowerStatisticsDialog.75b"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		textGrid(group, Messages.getString("PowerStatisticsDialog.50a"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, powerFormat.format(level50Power / 1000) + Messages.getString("PowerStatisticsDialog.50b"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		textGrid(group, Messages.getString("PowerStatisticsDialog.25a"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, powerFormat.format(level25Power / 1000) + Messages.getString("PowerStatisticsDialog.25b"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		textGrid(group, Messages.getString("PowerStatisticsDialog.10a"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, powerFormat.format(level10Power / 1000) + Messages.getString("PowerStatisticsDialog.10b"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		textGrid(group, Messages.getString("PowerStatisticsDialog.mina"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, powerFormat.format(levelMinPower / 1000) + Messages.getString("PowerStatisticsDialog.minb"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		textGrid(group, Messages.getString("PowerStatisticsDialog.variancea"), SWT.RIGHT, SWT.RIGHT, 2); //$NON-NLS-1$
		textGrid(group, powerFormat.format(levelVarPower / 1000) + Messages.getString("PowerStatisticsDialog.varianceb"), SWT.CENTER, SWT.CENTER, 2); //$NON-NLS-1$

		// create the Close button
		Button close = new Button(shell, SWT.NONE);
		close.setText(Messages.getString("PowerStatisticsDialog.close")); //$NON-NLS-1$
		gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		gridData.minimumWidth = 60;
		gridData.horizontalSpan = 4;
		close.setLayoutData(gridData);
		close.addSelectionListener(new SelectionListener(){

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
	
	private void textGrid(Composite parent, String text, int labelStyle, int gridStyle, int gridSpan)
	{
		Label label = new Label(parent, labelStyle);
		label.setFont(PIPageEditor.helvetica_9);
		label.setText(text);
		gridData = new GridData(SWT.FILL, gridStyle, true, true);
		gridData.horizontalSpan = gridSpan;
		label.setLayoutData(gridData);		
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

	private static String showTimeInterval(double startTime, double endTime)
	{
		return ProfileVisualiser.timeFormat.format(startTime)
		     + Messages.getString("PowerStatisticsDialog.interval1") + ProfileVisualiser.timeFormat.format(endTime) //$NON-NLS-1$
		     + Messages.getString("PowerStatisticsDialog.interval2")  + ProfileVisualiser.timeFormat.format(endTime - startTime) //$NON-NLS-1$
		     + Messages.getString("PowerStatisticsDialog.interval3"); //$NON-NLS-1$
	}

	private class PowerStat {
		public double power;
		public int count;
		
		PowerStat(double power) {
			this.power = power;
			this.count = 0;
		}
		
		PowerStat(double power, int count) {
			this.power = power;
			this.count = count;
		}
	}
	
	private void calculateStats()
	{
		PowerStat powerStat;
		Object[] powerLevelArray;
		ArrayList<PowerStat> powerLevel = new ArrayList<PowerStat>();
		Hashtable<Double,PowerStat> powerStatHash = new Hashtable<Double,PowerStat>();
		double sumOfSquares = 0.0;
		double sum = 0.0;
		int count = 0;

		int selStart = (int)(this.startTime * 1000 + 0.0005) + 1;
		int selEnd   = (int)(this.endTime * 1000 + 0.0005);
		int totalCount = selEnd - selStart + 1;

		levelVarPower = 0.0;

		if (selEnd < trace.getFirstSampleNumber() || selStart > selEnd)
		{
			powerLevelArray = new PowerStat[1];
			powerLevelArray[0] = new PowerStat(0.0, 1);
			totalCount = 1;
		}
		else
		{
			PowerTraceGraph graph = trace.getPowerGraph(0, NpiInstanceRepository.getInstance().activeUid()); // since graphs are in lockstep, any will do

			int index = graph.timeIndex(selStart);

	        // count time before the first sample as a bunch of zeros
	        if (selStart < trace.getFirstSampleNumber()) {
	        	count = trace.getFirstSampleNumber() - selStart;
				powerStat = new PowerStat(0, count);
				powerLevel.add(powerStat);
				powerStatHash.put(0.0, powerStat);
	        	index = 0;
	        }
	        
	        int[] sampleTimes = trace.getSampleTimes();
	        int[] ampValues   = trace.getAmpValues();
	        
			for (int j = index; j < sampleTimes.length; j++)
			{	
				int time = sampleTimes[j] + 1;
				if (time < selStart)
					time = selStart;
				
				int nextTime = j == sampleTimes.length - 1 ? Integer.MAX_VALUE : sampleTimes[j + 1];
				
				count = Math.min(nextTime - time + 1, selEnd - time + 1);
				double power = ampValues[j] * this.trace.getVoltage() * 1000.0;
				sumOfSquares += power * power * count;
				sum += power * count;
				time += count;
				
				powerStat = powerStatHash.get(power);
				if (powerStat == null) {
					powerStat = new PowerStat(power,count);
					powerLevel.add(powerStat);
					powerStatHash.put(power, powerStat);
				} else {
					powerStat.count += count;
				}
				
				if (time > selEnd)
					break;
			}

			powerLevelArray = powerLevel.toArray();
			Arrays.sort(powerLevelArray, new Comparator<Object>() {

				public int compare(Object o1, Object o2) {
					if (!(o1 instanceof PowerStat) || !(o2 instanceof PowerStat))
						return 0;

					double powerDiff = ((PowerStat)o1).power - ((PowerStat)o2).power;
					
					return powerDiff == 0.0 ? 0	: (powerDiff < 0.0 ? -1 : 1);
				}
			});
		}
		
		levelMaxPower = ((PowerStat) powerLevelArray[powerLevelArray.length - 1]).power;

		level90Power  = getPowerLevel(powerLevelArray, (totalCount * 90) / 100);
		level75Power  = getPowerLevel(powerLevelArray, (totalCount * 75) / 100);
		level50Power  = getPowerLevel(powerLevelArray, (totalCount * 50) / 100);
		level25Power  = getPowerLevel(powerLevelArray, (totalCount * 25) / 100);
		level10Power  = getPowerLevel(powerLevelArray, (totalCount * 10) / 100);

		levelMinPower = ((PowerStat) powerLevelArray[0]).power;

		levelVarPower = (sumOfSquares - (sum * sum)/totalCount)/totalCount;
	}
	
	// given array powerLevelArray of PowerStat elements (each representing PowerStat.count entries), find
	// power value of entry number index
	private double getPowerLevel(Object[] powerLevelArray, int index)
	{
		if (index == 0 || powerLevelArray == null || powerLevelArray.length == 0)
			return 0.0;
		
		for (int i = 0; (i < powerLevelArray.length) && (powerLevelArray[i] instanceof PowerStat); i++) {
			PowerStat powerStat = (PowerStat) powerLevelArray[i];
			if (index <= powerStat.count)
				return powerStat.power;
			index -= powerStat.count;
		}
		
		return 0.0;
	}
}
