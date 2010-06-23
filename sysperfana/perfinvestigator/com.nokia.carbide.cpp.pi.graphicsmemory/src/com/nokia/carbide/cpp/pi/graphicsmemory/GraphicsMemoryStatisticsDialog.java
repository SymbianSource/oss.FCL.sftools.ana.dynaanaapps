/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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
package com.nokia.carbide.cpp.pi.graphicsmemory;

import java.text.DecimalFormat;

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

public class GraphicsMemoryStatisticsDialog {

	private Shell shell;
	private GridData gridData;
	private DecimalFormat formatKBytes = new DecimalFormat(Messages
			.getString("GraphicsMemoryStatisticsDialog.KBformat")); //$NON-NLS-1$
	private DecimalFormat formatBytes = new DecimalFormat(Messages
			.getString("GraphicsMemoryStatisticsDialog.BytesFormat")); //$NON-NLS-1$
	private GraphicsMemoryTrace trace;
	private double startTime;
	private double endTime;

	public GraphicsMemoryStatisticsDialog(Display display) {
		Group group;

		shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setText(Messages
				.getString("GraphicsMemoryStatisticsDialog.statistics")); //$NON-NLS-1$
		shell.setLayout(new GridLayout(3, false));

		startTime = PIPageEditor.currentPageEditor().getStartTime();
		endTime = PIPageEditor.currentPageEditor().getEndTime();

		trace = (GraphicsMemoryTrace) NpiInstanceRepository.getInstance()
				.activeUidGetTrace(GraphicsMemoryPlugin.PLUGIN_ID); //$NON-NLS-1$

		group = new Group(shell, SWT.SHADOW_NONE);
		group.setText(Messages
				.getString("GraphicsMemoryStatisticsDialog.interval")); //$NON-NLS-1$
		group.setFont(PIPageEditor.helvetica_9);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 3;
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(3, false));// new FillLayout());
		textGrid(group, showTimeInterval(startTime, endTime), SWT.CENTER,
				SWT.CENTER, 3);

		int pageIndex = PIPageEditor.currentPageIndex();

		GraphicsMemoryTraceGraph graph = (GraphicsMemoryTraceGraph) trace
				.getTraceGraph(pageIndex); // since graph intervals are in
											// lockstep, any pageIndex will do
		GraphicsMemoryProcessTable table = graph.getGraphicsMemoryProcessTable();

		MaxGraphicsMemoryItem systemUseByInterval = trace
				.getSystemUseByInterval((long) (startTime * 1000.0),
						(long) (endTime * 1000.0));

		group = new Group(shell, SWT.NONE);
		group.setText(Messages
				.getString("GraphicsMemoryStatisticsDialog.onDevice")); //$NON-NLS-1$
		group.setFont(PIPageEditor.helvetica_9);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 3;
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(3, false));// new FillLayout());

		String usedKB = Messages
				.getString("GraphicsMemoryStatisticsDialog.notRecorded"); //$NON-NLS-1$
		String usedBytes = ""; //$NON-NLS-1$
		String freeKB = usedKB;
		String freeBytes = usedBytes;
		String totalKB = usedKB;
		String totalBytes = usedBytes;

		if ((long) (endTime * 1000.0 + 0.5) >= trace.getFirstSampleNumber()) {
			long deviceTotalMemory = systemUseByInterval.maxTotal;
			long deviceUsedMemory = systemUseByInterval.maxPrivate;

			usedKB = formatKBytes.format(deviceUsedMemory / 1024.0);
			usedBytes = formatBytes.format(deviceUsedMemory);
			freeKB = formatKBytes
					.format((deviceTotalMemory - deviceUsedMemory) / 1024.0);
			freeBytes = formatBytes
					.format(deviceTotalMemory - deviceUsedMemory);
			totalKB = formatKBytes.format(deviceTotalMemory / 1024.0);
			totalBytes = formatBytes.format(deviceTotalMemory);
		}
		textGrid(
				group,
				Messages.getString("GraphicsMemoryStatisticsDialog.used"), SWT.LEFT, SWT.CENTER, 1); //$NON-NLS-1$
		textGrid(group, usedKB, SWT.RIGHT, SWT.CENTER, 1);
		textGrid(group, usedBytes, SWT.RIGHT, SWT.CENTER, 1);
		textGrid(
				group,
				Messages.getString("GraphicsMemoryStatisticsDialog.free"), SWT.LEFT, SWT.CENTER, 1); //$NON-NLS-1$
		textGrid(group, freeKB, SWT.RIGHT, SWT.CENTER, 1);
		textGrid(group, freeBytes, SWT.RIGHT, SWT.CENTER, 1);
		textGrid(
				group,
				Messages.getString("GraphicsMemoryStatisticsDialog.total"), SWT.LEFT, SWT.CENTER, 1); //$NON-NLS-1$
		textGrid(group, totalKB, SWT.RIGHT, SWT.CENTER, 1);
		textGrid(group, totalBytes, SWT.RIGHT, SWT.CENTER, 1);

		group = new Group(shell, SWT.NONE);
		group.setText(Messages
				.getString("GraphicsMemoryStatisticsDialog.currentSelection")); //$NON-NLS-1$
		group.setFont(PIPageEditor.helvetica_9);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 3;
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(3, false));// new FillLayout());

		String privateKB = Messages
				.getString("GraphicsMemoryStatisticsDialog.notRecorded"); //$NON-NLS-1$
		String privateBytes = ""; //$NON-NLS-1$
		String sharedKB = privateKB;
		String sharedBytes = privateBytes;

		if ((long) (endTime * 1000.0 + 0.5) >= trace.getFirstSampleNumber()) {
			float selectedMaxPrivate = 0;
			float selectedMaxShared = 0;

			Object[] selected = table.getTableViewer().getCheckedElements();

			for (int i = 0; i < selected.length; i++) {
				if (selected[i] instanceof GraphicsMemoryProcess) {
					GraphicsMemoryProcess checked = (GraphicsMemoryProcess) selected[i];
					selectedMaxPrivate += checked.maxMemoryItem.maxPrivate;
					selectedMaxShared += checked.maxMemoryItem.maxShared;
				}
			}

			privateKB = formatKBytes.format(selectedMaxPrivate / 1024.0);
			privateBytes = formatBytes.format((long) selectedMaxPrivate);
			sharedKB = formatKBytes.format(selectedMaxShared / 1024.0);
			sharedBytes = formatBytes.format((long) selectedMaxShared);
		}

		textGrid(
				group,
				Messages.getString("GraphicsMemoryStatisticsDialog.private"), SWT.LEFT, SWT.CENTER, 1); //$NON-NLS-1$
		textGrid(group, privateKB, SWT.RIGHT, SWT.CENTER, 1);
		textGrid(group, privateBytes, SWT.RIGHT, SWT.CENTER, 1);
		textGrid(
				group,
				Messages.getString("GraphicsMemoryStatisticsDialog.shared"), SWT.LEFT, SWT.CENTER, 1); //$NON-NLS-1$
		textGrid(group, sharedKB, SWT.RIGHT, SWT.CENTER, 1);
		textGrid(group, sharedBytes, SWT.RIGHT, SWT.CENTER, 1);

		// create the Close button
		Button close = new Button(shell, SWT.NONE);
		close.setText(Messages
				.getString("GraphicsMemoryStatisticsDialog.close")); //$NON-NLS-1$
		gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		gridData.minimumWidth = 60;
		gridData.horizontalSpan = 3;
		close.setLayoutData(gridData);
		close.addSelectionListener(new SelectionListener() {

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

	private void textGrid(Composite parent, String text, int labelStyle,
			int gridStyle, int gridSpan) {
		Label label = new Label(parent, labelStyle);
		label.setFont(PIPageEditor.helvetica_9);
		label.setText(text);
		gridData = new GridData(SWT.FILL, gridStyle, true, true);
		gridData.horizontalSpan = gridSpan;
		label.setLayoutData(gridData);
	}

	public void dispose() {
		if (this.shell != null) {
			if (!this.shell.isDisposed()) {
				this.shell.close();
			}
			this.shell.dispose();
		}

		this.shell = null;
	}

	private static String showTimeInterval(double startTime, double endTime) {
		return ProfileVisualiser.TIME_FORMAT.format(startTime)
				+ Messages
						.getString("GraphicsMemoryStatisticsDialog.interval1") + ProfileVisualiser.TIME_FORMAT.format(endTime) //$NON-NLS-1$
				+ Messages
						.getString("GraphicsMemoryStatisticsDialog.interval2") + ProfileVisualiser.TIME_FORMAT.format(endTime - startTime) //$NON-NLS-1$
				+ Messages
						.getString("GraphicsMemoryStatisticsDialog.interval3"); //$NON-NLS-1$
	}
}
