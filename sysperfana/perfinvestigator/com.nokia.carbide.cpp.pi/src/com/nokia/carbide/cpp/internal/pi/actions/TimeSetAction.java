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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.nokia.carbide.cpp.internal.pi.analyser.PIChangeEvent;
import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;


public class TimeSetAction extends org.eclipse.jface.action.ControlContribution {

	double startTime = 0;
	double endTime   = 0;
	
	private Text  startTimeText;
	private Text  endTimeText;
	
	private DecimalFormat timeFormat = new DecimalFormat(Messages.getString("TimeSetAction.decimalFormat")); //$NON-NLS-1$
	
	public TimeSetAction(String id) {
		super(id);
	}
	
	public void setTime(double start, double end) {
		if (start < 0f)
			start = 0f;
		
		if (end < 0f)
			end = 0f;
		
		if (start <= end) {
			this.startTime = start;
			this.endTime   = end;
		} else {
			// swap start and end
			double temp = start;
			this.startTime = end;
			this.endTime   = temp;
		}

		if ((startTimeText == null) || (endTimeText == null))
			return;

		startTimeText.setText(timeFormat.format(startTime));
		endTimeText.setText(timeFormat.format(endTime));
	}
	
	public void setStartTime(double start) {
		setTime(start, this.endTime);
	}
	
	public void setEndTime(double end) {
		setTime(this.startTime, end);
	}
	
	public double getStartTime() {
		return this.startTime;
	}

	public double getEndTime() {
		return this.endTime;
	}

	protected Control createControl(Composite parent) {
		Composite control;
		Label label;

		// create a composite where the user can enter a start time and an end time
		control = new Composite(parent, SWT.BORDER);
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
		control.setLayout(new FillLayout(SWT.HORIZONTAL));

		label = new Label(control, SWT.RIGHT);
		label.setText(Messages.getString("TimeSetAction.startTime")); //$NON-NLS-1$
		label.pack();

		startTimeText = new Text(control, SWT.BORDER);
		
		if (startTime >= 0.0f)
			startTimeText.setText(timeFormat.format(startTime));
		else
			startTimeText.setText(""); //$NON-NLS-1$

		startTimeText.addFocusListener(new FocusListener() {

			double initialStartTime = -1;
			
			public void focusGained(FocusEvent e) {
				initialStartTime = startTime;
			}

			public void focusLost(FocusEvent e) {
				double new_double;
				
				// convert, catch (NumberFormatException e1)
				try {
					new_double = Double.parseDouble(startTimeText.getText().replace(',','.'));
					if (new_double >= 0)
						startTime = new_double;
				} catch (NumberFormatException exc) {
					// just keep the old value
				}					

				// redisplay the time
				setTime(startTime, endTime);
				
				if (startTime != initialStartTime) {
					PIChangeEvent.action("changeSelection"); //$NON-NLS-1$
					
					// after the graphs have been updated, notify plugins that might have tables but no graphs
            		Enumeration enu = PluginInitialiser.getPluginInstances(
            									"com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
            		if (enu != null) {
            			Event event = new Event();
            			event.start = (int) startTime;
            			event.end   = (int) endTime;
            			
	            		while (enu.hasMoreElements())
	            		{
	            			IEventListener plugin = (IEventListener)enu.nextElement();
	            			plugin.receiveEvent("changeSelection", event); //$NON-NLS-1$
	            		}
            		}
				}
			}

		});
		
		startTimeText.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				double new_double;

				// convert, catch (NumberFormatException e1)
				try {
					new_double = Double.parseDouble(startTimeText.getText().replace(',','.'));
					if (new_double >= 0)
						startTime = new_double;
				} catch (NumberFormatException exc) {
					// just keep the old value
				}					

				// redisplay the time
				setTime(startTime, endTime);
			}

		});

		label = new Label(control, SWT.RIGHT);
		label.setText(Messages.getString("TimeSetAction.endTime")); //$NON-NLS-1$
		label.pack();

		endTimeText   = new Text(control, SWT.BORDER);

		if (endTime >= 0.0f)
			endTimeText.setText(timeFormat.format(endTime));
		else
			endTimeText.setText(""); //$NON-NLS-1$

		endTimeText.addFocusListener(new FocusListener() {
			
			double initialEndTime   = -1;

			public void focusGained(FocusEvent e) {
				initialEndTime = endTime;
			}

			public void focusLost(FocusEvent e) {
				double new_double;
				
				// convert, catch (NumberFormatException e1)
				try {
					new_double = Double.parseDouble(endTimeText.getText().replace(',','.'));
					if (new_double >= 0)
						endTime = new_double;
				} catch (NumberFormatException exc) {
					// just keep the old value
				}					

				// redisplay the time
				setTime(startTime, endTime);
				if (endTime != initialEndTime) {
					PIChangeEvent.action("changeSelection"); //$NON-NLS-1$
					
					// after the graphs have been updated, notify plugins that might have tables but no graphs
            		Enumeration enu = PluginInitialiser.getPluginInstances(
            									"com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
            		if (enu != null) {
            			Event event = new Event();
            			event.start = (int) startTime;
            			event.end   = (int) endTime;
            			
	            		while (enu.hasMoreElements())
	            		{
	            			IEventListener plugin = (IEventListener)enu.nextElement();
	            			plugin.receiveEvent("changeSelection", event); //$NON-NLS-1$
	            		}
            		}
				}
			}

		});
		
		endTimeText.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				double new_double;
			
				// convert, catch (NumberFormatException e1)
				try {
					new_double = Double.parseDouble(endTimeText.getText().replace(',','.'));
					if (new_double >= 0)
						endTime = new_double;
				} catch (NumberFormatException exc) {
					// just keep the old value
				}					

				// redisplay the time
				setTime(startTime, endTime);
			}
			
		});

		control.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				if (startTime >= 0.0f)
					startTimeText.setText(timeFormat.format(startTime));
				else
					startTimeText.setText(""); //$NON-NLS-1$
				
				if (endTime >= 0.0f)
					endTimeText.setText(timeFormat.format(endTime));
				else
					endTimeText.setText(""); //$NON-NLS-1$
			}
			
		});

		return control;
	}
	
	public void dispose() {
	}
}

