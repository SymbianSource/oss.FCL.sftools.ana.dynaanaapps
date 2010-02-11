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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

public class EventDialog
{
	private String eventName;
	private String eventComment;
	private long   eventTime;

	private String newEventName;
	private String newEventComment;
	private long   newEventTime;

	private Shell shell;
	private Label label;
	private GridData gridData;
	DecimalFormat timeFormat = new DecimalFormat(Messages.getString("EventDialog.timeFormat")); //$NON-NLS-1$

	private Text nameText;
//	private Text typeText;
	private Text commentText;
	private Text timeText;

	public EventDialog(Display display, String eventType, String name, String comment, long time)
	{
		showDialog(display, eventType, name, comment, time, false);
	}

	public EventDialog(Display display, String eventType, String name, String comment, long time, boolean timeChange)
	{
		showDialog(display, eventType, name, comment, time, timeChange);
	}

	private void showDialog(Display display, String type, String name, String comment, long time,
							boolean timeChange)
	{
		// store original values
		this.eventName    = name;
		this.eventComment = comment;
		this.eventTime    = time;

		this.newEventName    = name;
		this.newEventComment = comment;
		this.newEventTime    = time;

		// create the shell
		shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText(type + Messages.getString("EventDialog.event1") + eventName + Messages.getString("EventDialog.event2") + eventTime/1000d + Messages.getString("EventDialog.event3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		shell.setLayout(new GridLayout(2, true));

		// do let them change the name
		label = new Label(shell, SWT.LEFT);
		label.setText(Messages.getString("EventDialog.eventName")); //$NON-NLS-1$
		label.setFont(PIPageEditor.helvetica_9);

		nameText = new Text(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gridData);
		nameText.setFont(PIPageEditor.helvetica_9);
		nameText.setData(Messages.getString("EventDialog.typeName")); //$NON-NLS-1$

		if (eventName == null)
			nameText.setText(""); //$NON-NLS-1$
		else
			nameText.setText(eventName);
		nameText.setEditable(true);

		// add the listener(s)
		nameText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newEventName = nameText.getText();
			}
		});
		nameText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				newEventName = nameText.getText();
			}
		});

//		// do not let them change the event type
//		label = new Label(shell, SWT.LEFT);
//		label.setText("Type:");
//		label.setFont(PIPageEditor.helvetica_9);
//
//		typeText = new Text(shell, SWT.NONE);
//		gridData = new GridData(GridData.FILL_HORIZONTAL);
//		typeText.setLayoutData(gridData);
//		typeText.setFont(PIPageEditor.helvetica_9);
//		typeText.setData("Type a type");
//		typeText.setText(type);
//		typeText.setEditable(false);

		// do let them change the comment
		label = new Label(shell, SWT.LEFT);
		label.setText(Messages.getString("EventDialog.comment")); //$NON-NLS-1$
		label.setFont(PIPageEditor.helvetica_9);

		commentText = new Text(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		commentText.setLayoutData(gridData);
		commentText.setFont(PIPageEditor.helvetica_9);
		commentText.setData(Messages.getString("EventDialog.typeComment")); //$NON-NLS-1$

		if (eventComment == null)
			commentText.setText(""); //$NON-NLS-1$
		else
			commentText.setText(eventComment);
		commentText.setEditable(true);

		// add the listener(s)
		commentText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newEventComment = commentText.getText();
			}
		});
		commentText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				newEventComment = commentText.getText();
			}
		});

		// maybe let them change the time
		label = new Label(shell, SWT.LEFT);
		label.setText(Messages.getString("EventDialog.time")); //$NON-NLS-1$
		label.setFont(PIPageEditor.helvetica_9);

		timeText = new Text(shell, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		timeText.setLayoutData(gridData);
		timeText.setFont(PIPageEditor.helvetica_9);
		timeText.setData(Messages.getString("EventDialog.typeTime")); //$NON-NLS-1$
		timeText.setText(timeFormat.format(eventTime/1000d));
		timeText.setEditable(false);

		// add the listener(s)
		timeText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				newEventTime = convert(timeText, newEventTime);
			}
		});
		timeText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				newEventTime = convert(timeText, newEventTime);
			}
		});

		// create the OK button
		Button okButton = new Button(shell, SWT.NONE);
		okButton.setText(Messages.getString("EventDialog.ok")); //$NON-NLS-1$
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		okButton.setLayoutData(gridData);

		// add the listener(s)
		okButton.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				if (   ((eventName == null) && (newEventName != null))
					|| ((eventName != null) && !eventName.equals(newEventName))
					|| ((eventComment == null) && (newEventComment != null))
					|| ((eventComment != null) && !eventComment.equals(newEventComment))
					|| eventTime != newEventTime)
				{
					// the file has changed
					PIPageEditor.currentPageEditor().setDirty();
				}
				shell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// create the Cancel button
		Button cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setText(Messages.getString("EventDialog.cancel")); //$NON-NLS-1$
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		cancelButton.setLayoutData(gridData);

		// add the listener(s)
		cancelButton.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				newEventName    = eventName;
				newEventComment = eventComment;
				newEventTime    = eventTime;
				shell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		shell.pack();
		shell.open();
		
		GC gc = new GC(shell);
		Point point = gc.stringExtent(shell.getText());
		gc.dispose();

		Rectangle bounds = shell.getBounds();
		bounds.width = point.x + 100;
		shell.setBounds(bounds);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private long convert(Text text, long currentTime)
	{
		double new_double;

		// convert, catch (NumberFormatException e1)
		try {
			new_double = Double.parseDouble(text.getText().replace(',','.'));
			if (new_double >= 0)
				currentTime = (long) (new_double * 1000);
		} catch (NumberFormatException exc) {
			// just keep the old value
		}

		text.setText(timeFormat.format(currentTime/10/100d));

		return currentTime;
	}

	public String getName()
	{
		return this.eventName;
	}
	public String getComment()
	{
		return this.eventComment;
	}

	public long   getTime()
	{
		return this.eventTime;
	}

	public String getNewName()
	{
		return this.newEventName;
	}

	public String getNewComment()
	{
		return this.newEventComment;
	}

	public long   getNewTime()
	{
		return this.newEventTime;
	}
}

