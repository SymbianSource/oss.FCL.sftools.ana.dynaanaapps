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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.PIChangeEvent;
import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;


public class SetThresholdsDialog {

	private Shell shell;

	private Group threadGroup;
	private Label threadLoadLabel;
	private Label threadCountLabel;
	private Text  threadLoadText;
	private Text  threadCountText;

	private Button button;

	private Group binaryGroup;
	private Label binaryLoadLabel;
	private Label binaryCountLabel;
	private Text  binaryLoadText;
	private Text  binaryCountText;

	private Group functionGroup;
	private Label functionLoadLabel;
	private Label functionCountLabel;
	private Text  functionLoadText;
	private Text  functionCountText;

	private Button ok;
	private Button cancel;
	private GridData gridData;
	private DecimalFormat timeFormat = new DecimalFormat(Messages.getString("SetThresholdsDialog.decimalFormat")); //$NON-NLS-1$

	private double thresholdLoadThread;
	private double thresholdLoadBinary;
	private double thresholdLoadFunction;
	private int thresholdCountThread;
	private int thresholdCountBinary;
	private int thresholdCountFunction;

	public SetThresholdsDialog(Display display)
	{
		thresholdLoadThread    = (Double) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdLoadThread");			//$NON-NLS-1$
		thresholdLoadBinary    = (Double) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdLoadBinary");		//$NON-NLS-1$
		thresholdLoadFunction  = (Double) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdLoadFunction");		//$NON-NLS-1$
		thresholdCountThread   = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountThread");		//$NON-NLS-1$
		thresholdCountBinary   = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountBinary");		//$NON-NLS-1$
		thresholdCountFunction = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountFunction");		//$NON-NLS-1$

		shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText(Messages.getString("SetThresholdsDialog.thresholdTitle")); //$NON-NLS-1$
		shell.setLayout(new GridLayout(4, false));

		// thread threshold
		threadGroup = new Group(shell, SWT.NONE);
		threadGroup.setText(Messages.getString("SetThresholdsDialog.threadThreshold")); //$NON-NLS-1$
		threadGroup.setFont(PIPageEditor.helvetica_10);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gridData.horizontalSpan = 4;
		threadGroup.setLayoutData(gridData);
		threadGroup.setLayout(new FillLayout());

		threadLoadLabel = new Label(threadGroup, SWT.RIGHT);
		threadLoadLabel.setText(Messages.getString("SetThresholdsDialog.averageLoad")); //$NON-NLS-1$
		threadLoadLabel.setFont(PIPageEditor.helvetica_9);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 1;

		threadLoadText = new Text(threadGroup, SWT.BORDER | SWT.RIGHT);
		threadLoadText.setFont(PIPageEditor.helvetica_9);
		threadLoadText.setData(Messages.getString("SetThresholdsDialog.minimumPercent")); //$NON-NLS-1$
		threadLoadText.setText(timeFormat.format(thresholdLoadThread * 100.0));
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 1;

		threadCountLabel = new Label(threadGroup, SWT.LEFT);
		threadCountLabel.setText(Messages.getString("SetThresholdsDialog.totalSamples")); //$NON-NLS-1$
		threadCountLabel.setFont(PIPageEditor.helvetica_9);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 1;

		threadCountText = new Text(threadGroup, SWT.BORDER | SWT.RIGHT);
		threadCountText.setFont(PIPageEditor.helvetica_9);
		threadCountText.setData(Messages.getString("SetThresholdsDialog.minimumSamples")); //$NON-NLS-1$
		threadCountText.setText("" + thresholdCountThread); //$NON-NLS-1$
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 1;

		// add the thread listeners
		threadLoadText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdLoadThread  = convert(threadLoadText, thresholdLoadThread, 0, 100, timeFormat) / 100.0;
				thresholdCountThread = (int) ((totalSampleCount / samplingInterval) * thresholdLoadThread + 0.5);
				threadLoadText.setText(timeFormat.format(thresholdLoadThread * 100.0));
				threadCountText.setText("" + thresholdCountThread); //$NON-NLS-1$
				
				Boolean useOnlyThreadThresholds = (Boolean) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.useOnlyThreadThresholds");  //$NON-NLS-1$
				if (useOnlyThreadThresholds) {
					copyThreadThresholds();
				}
			}
		});

		threadLoadText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdLoadThread  = convert(threadLoadText, thresholdLoadThread, 0, 100, timeFormat) / 100.0;
				thresholdCountThread = (int) ((totalSampleCount / samplingInterval) * thresholdLoadThread + 0.5);
				threadLoadText.setText(timeFormat.format(thresholdLoadThread * 100.0));
				threadCountText.setText("" + thresholdCountThread); //$NON-NLS-1$

				Boolean useOnlyThreadThresholds = (Boolean) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.useOnlyThreadThresholds"); //$NON-NLS-1$
				if (useOnlyThreadThresholds) {
					copyThreadThresholds();
				}
			}
		});

		threadCountText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdCountThread = convert(threadCountText, thresholdCountThread, 0, totalSampleCount / samplingInterval);
				thresholdLoadThread  = ((double)thresholdCountThread * samplingInterval) / ((double)totalSampleCount);
				threadLoadText.setText(timeFormat.format(thresholdLoadThread * 100.0));
				threadCountText.setText("" + thresholdCountThread); //$NON-NLS-1$

				Boolean useOnlyThreadThresholds = (Boolean) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.useOnlyThreadThresholds");	//$NON-NLS-1$
				if (useOnlyThreadThresholds) {
					copyThreadThresholds();
				}
			}
		});

		threadCountText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdCountThread = convert(threadCountText, thresholdCountThread, 0, totalSampleCount / samplingInterval);
				thresholdLoadThread  = ((double)thresholdCountThread * samplingInterval) / ((double)totalSampleCount);
				threadLoadText.setText(timeFormat.format(thresholdLoadThread * 100.0));
				threadCountText.setText("" + thresholdCountThread); //$NON-NLS-1$

				Boolean useOnlyThreadThresholds = (Boolean) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.useOnlyThreadThresholds");	 //$NON-NLS-1$
				if (useOnlyThreadThresholds) {
					copyThreadThresholds();
				}
			}
		});

		threadGroup.pack();

		button = new Button(shell, SWT.LEFT | SWT.CHECK);
		button.setText(Messages.getString("SetThresholdsDialog.useThreadForAll")); //$NON-NLS-1$
		button.setFont(PIPageEditor.helvetica_8);
		button.setSelection((Boolean) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.useOnlyThreadThresholds"));	 //$NON-NLS-1$
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 10;
		button.setLayoutData(gridData);

		// add the button listeners
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.useOnlyThreadThresholds", button.getSelection()); //$NON-NLS-1$
				Boolean useOnlyThreadThresholds = (Boolean) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.useOnlyThreadThresholds"); //$NON-NLS-1$
				
				if (useOnlyThreadThresholds) {
					// disable the other numbers
					binaryLoadText.setEnabled(false);
					binaryCountText.setEnabled(false);
					functionLoadText.setEnabled(false);
					functionCountText.setEnabled(false);
					copyThreadThresholds();
				} else {
					binaryLoadText.setEnabled(true);
					binaryCountText.setEnabled(true);
					functionLoadText.setEnabled(true);
					functionCountText.setEnabled(true);
				}
			}

			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		});

		// binary threshold
		binaryGroup = new Group(shell, SWT.LEFT);
		binaryGroup.setText(Messages.getString("SetThresholdsDialog.binaryThreshold")); //$NON-NLS-1$
		binaryGroup.setFont(PIPageEditor.helvetica_10);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 4;
		binaryGroup.setLayoutData(gridData);
		binaryGroup.setLayout(new FillLayout());

		binaryLoadLabel = new Label(binaryGroup, SWT.RIGHT);
		binaryLoadLabel.setText(Messages.getString("SetThresholdsDialog.averageLoad")); //$NON-NLS-1$
		binaryLoadLabel.setFont(PIPageEditor.helvetica_9);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 1;

		binaryLoadText = new Text(binaryGroup, SWT.BORDER | SWT.RIGHT);
		binaryLoadText.setFont(PIPageEditor.helvetica_9);
		binaryLoadText.setData(Messages.getString("SetThresholdsDialog.minimumPercent")); //$NON-NLS-1$
		binaryLoadText.setText(timeFormat.format(thresholdLoadBinary * 100.0));
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 1;

		binaryCountLabel = new Label(binaryGroup, SWT.LEFT);
		binaryCountLabel.setText(Messages.getString("SetThresholdsDialog.totalSamples")); //$NON-NLS-1$
		binaryCountLabel.setFont(PIPageEditor.helvetica_9);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 1;

		binaryCountText = new Text(binaryGroup, SWT.BORDER | SWT.RIGHT);
		binaryCountText.setFont(PIPageEditor.helvetica_9);
		binaryCountText.setData(Messages.getString("SetThresholdsDialog.minimumSamples")); //$NON-NLS-1$
		binaryCountText.setText("" + thresholdCountBinary); //$NON-NLS-1$
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 1;

		// add the listeners
		binaryLoadText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdLoadBinary  = convert(binaryLoadText, thresholdLoadBinary, 0, 100, timeFormat) / 100.0;
				thresholdCountBinary = (int) ((totalSampleCount / samplingInterval) * thresholdLoadBinary + 0.5);
				binaryLoadText.setText(timeFormat.format(thresholdLoadBinary * 100.0));
				binaryCountText.setText("" + thresholdCountBinary); //$NON-NLS-1$
			}
		});

		binaryLoadText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdLoadBinary  = convert(binaryLoadText, thresholdLoadBinary, 0, 100, timeFormat) / 100.0;
				thresholdCountBinary = (int) ((totalSampleCount / samplingInterval) * thresholdLoadBinary + 0.5);
				binaryLoadText.setText(timeFormat.format(thresholdLoadBinary * 100.0));
				binaryCountText.setText("" + thresholdCountBinary); //$NON-NLS-1$
			}
		});

		binaryCountText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdCountBinary = convert(binaryCountText, thresholdCountBinary, 0, totalSampleCount / samplingInterval);
				thresholdLoadBinary = ((double)thresholdCountBinary * samplingInterval) / ((double)totalSampleCount);
				binaryLoadText.setText(timeFormat.format(thresholdLoadBinary * 100.0));
				binaryCountText.setText("" + thresholdCountBinary); //$NON-NLS-1$
			}
		});

		binaryCountText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdCountBinary = convert(binaryCountText, thresholdCountBinary, 0, totalSampleCount / samplingInterval);
				thresholdLoadBinary = ((double)thresholdCountBinary * samplingInterval) / ((double)totalSampleCount);
				binaryLoadText.setText(timeFormat.format(thresholdLoadBinary * 100.0));
				binaryCountText.setText("" + thresholdCountBinary); //$NON-NLS-1$
			}
		});

		binaryGroup.pack();

		// function threshold
		functionGroup = new Group(shell, SWT.LEFT);
		functionGroup.setText(Messages.getString("SetThresholdsDialog.functionThreshold")); //$NON-NLS-1$
		functionGroup.setFont(PIPageEditor.helvetica_10);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 4;
		functionGroup.setLayoutData(gridData);
		functionGroup.setLayout(new FillLayout());

		functionLoadLabel = new Label(functionGroup, SWT.RIGHT);
		functionLoadLabel.setText(Messages.getString("SetThresholdsDialog.averageLoad")); //$NON-NLS-1$
		functionLoadLabel.setFont(PIPageEditor.helvetica_9);

		functionLoadText = new Text(functionGroup, SWT.BORDER | SWT.RIGHT);
		functionLoadText.setFont(PIPageEditor.helvetica_9);
		functionLoadText.setData(Messages.getString("SetThresholdsDialog.minimumPercent")); //$NON-NLS-1$
		functionLoadText.setText(timeFormat.format(thresholdLoadFunction * 100.0));

		functionCountLabel = new Label(functionGroup, SWT.LEFT);
		functionCountLabel.setText(Messages.getString("SetThresholdsDialog.totalSamples")); //$NON-NLS-1$
		functionCountLabel.setFont(PIPageEditor.helvetica_9);

		functionCountText = new Text(functionGroup, SWT.BORDER | SWT.RIGHT);
		functionCountText.setFont(PIPageEditor.helvetica_9);
		functionCountText.setData(Messages.getString("SetThresholdsDialog.minimumSamples")); //$NON-NLS-1$
		functionCountText.setText("" + thresholdCountFunction); //$NON-NLS-1$

		// add the listeners
		functionLoadText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdLoadFunction  = convert(functionLoadText, thresholdLoadFunction, 0, 100, timeFormat) / 100.0;
				thresholdCountFunction = (int) ((totalSampleCount / samplingInterval) * thresholdLoadFunction + 0.5);
				functionLoadText.setText(timeFormat.format(thresholdLoadFunction * 100.0));
				functionCountText.setText("" + thresholdCountFunction); //$NON-NLS-1$
			}
		});

		functionLoadText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdLoadFunction  = convert(functionLoadText, thresholdLoadFunction, 0, 100, timeFormat) / 100.0;
				thresholdCountFunction = (int) ((totalSampleCount / samplingInterval) * thresholdLoadFunction + 0.5);
				functionLoadText.setText(timeFormat.format(thresholdLoadFunction * 100.0));
				functionCountText.setText("" + thresholdCountFunction); //$NON-NLS-1$
			}
		});

		functionCountText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdCountFunction = convert(functionCountText, thresholdCountFunction, 0, totalSampleCount / samplingInterval);
				thresholdLoadFunction = ((double)thresholdCountFunction * samplingInterval) / ((double)totalSampleCount);
				functionLoadText.setText(timeFormat.format(thresholdLoadFunction * 100.0));
				functionCountText.setText("" + thresholdCountFunction); //$NON-NLS-1$
			}
		});

		functionCountText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				int uid = NpiInstanceRepository.getInstance().activeUid();
				int totalSampleCount = NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.THREADS_PAGE).getLastSampleX();
				int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
				thresholdCountFunction = convert(functionCountText, thresholdCountFunction, 0, totalSampleCount / samplingInterval);
				thresholdLoadFunction = ((double)thresholdCountFunction * samplingInterval) / ((double)totalSampleCount);
				functionLoadText.setText(timeFormat.format(thresholdLoadFunction * 100.0));
				functionCountText.setText("" + thresholdCountFunction); //$NON-NLS-1$
			}
		});

		functionGroup.pack();

		// create the OK button
		ok = new Button(shell, SWT.NONE);
		ok.setEnabled(false);
		ok.setVisible(false);

		ok = new Button(shell, SWT.NONE);
		ok.setText(Messages.getString("SetThresholdsDialog.ok")); //$NON-NLS-1$
		gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		gridData.minimumWidth = 60;
		ok.setLayoutData(gridData);
		ok.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				boolean changedThread   = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountThread") != thresholdCountThread;	//$NON-NLS-1$
				boolean changedBinary   = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountBinary")  != thresholdCountBinary;	//$NON-NLS-1$
				boolean changedFunction = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountFunction") != thresholdCountFunction;	//$NON-NLS-1$

				if (changedThread) {
					NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.thresholdLoadThread", new Double(thresholdLoadThread)); //$NON-NLS-1$
					NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountThread", new Integer(thresholdCountThread)); //$NON-NLS-1$
					PIChangeEvent.action("changeThresholdThread"); //$NON-NLS-1$
					PIChangeEvent.action("changeSelection"); //$NON-NLS-1$
				}

				if (changedBinary) {
					NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.thresholdLoadBinary", new Double(thresholdLoadBinary)); //$NON-NLS-1$
					NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountBinary", new Integer(thresholdCountBinary)); //$NON-NLS-1$
					PIChangeEvent.action("changeThresholdBinary"); //$NON-NLS-1$
					PIChangeEvent.action("changeSelection"); //$NON-NLS-1$
				}

				if (changedFunction) {
					NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.thresholdLoadFunction", new Double(thresholdLoadFunction)); //$NON-NLS-1$
					NpiInstanceRepository.getInstance().activeUidSetPersistState("com.nokia.carbide.cpp.pi.address.thresholdCountFunction", new Integer(thresholdCountFunction)); //$NON-NLS-1$
					PIChangeEvent.action("changeThresholdFunction"); //$NON-NLS-1$
					PIChangeEvent.action("changeSelection"); //$NON-NLS-1$
				}

				// after the graphs have been updated, notify plugins that might have tables but no graphs
        		Enumeration enu = PluginInitialiser.getPluginInstances(
        									"com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
        		if (enu != null) {
        			Event event = new Event();

            		while (enu.hasMoreElements())
            		{
            			IEventListener plugin = (IEventListener)enu.nextElement();
            			if (changedThread) {
            				event.data  = new Double(thresholdLoadThread);
            				event.count = thresholdCountThread;
                			plugin.receiveEvent("changeThresholdThread", event); //$NON-NLS-1$
            			}

            			if (changedBinary) {
            				event.data  = new Double(thresholdLoadBinary);
            				event.count = thresholdCountBinary;
                			plugin.receiveEvent("changeThresholdBinary", event); //$NON-NLS-1$
            			}

            			if (changedFunction) {
            				event.data  = new Double(thresholdLoadFunction);
            				event.count = thresholdCountFunction;
                			plugin.receiveEvent("changeThresholdFunction", event); //$NON-NLS-1$
            			}
            		}
        		}

				shell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		ok = new Button(shell, SWT.NONE);
		ok.setEnabled(false);
		ok.setVisible(false);

		// create the Cancel button
		cancel = new Button(shell, SWT.NONE);
		cancel.setText(Messages.getString("SetThresholdsDialog.cancel")); //$NON-NLS-1$
		gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		gridData.minimumWidth = 60;
		cancel.setLayoutData(gridData);
		cancel.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		if ((Boolean)NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.useOnlyThreadThresholds") == true) { //$NON-NLS-1$
			// disable the other numbers
			binaryLoadText.setEnabled(false);
			binaryCountText.setEnabled(false);
			functionLoadText.setEnabled(false);
			functionCountText.setEnabled(false);
		}

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
	
	private void copyThreadThresholds() {
		thresholdLoadBinary    = thresholdLoadThread;
		thresholdCountBinary   = thresholdCountThread;
		thresholdLoadFunction  = thresholdLoadThread;
		thresholdCountFunction = thresholdCountThread;
		binaryLoadText.setText(timeFormat.format(thresholdLoadBinary * 100.0));
		binaryCountText.setText("" + thresholdCountBinary); //$NON-NLS-1$
		functionLoadText.setText(timeFormat.format(thresholdLoadFunction * 100.0));
		functionCountText.setText("" + thresholdCountFunction); //$NON-NLS-1$
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
