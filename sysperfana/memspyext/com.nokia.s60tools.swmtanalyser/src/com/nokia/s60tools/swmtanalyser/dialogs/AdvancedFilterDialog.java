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
package com.nokia.s60tools.swmtanalyser.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.s60tools.swmtanalyser.analysers.AnalyserConstants;
import com.nokia.s60tools.swmtanalyser.dialogs.SaveFilterOptions.ValueTypes;

/**
 * UI Dialog class for Advanced Filter Options.
 *
 */
public class AdvancedFilterDialog extends Dialog implements SelectionListener{
	
	private Button[] severity_buttons;
	private Combo delta_dropdown;
	private Combo delta_dropdown2;
	private Button filter_button;
	private Button cancel_button;
	private Text start_text;
	private Text end_text;
	private Text start_text2;
	private Text end_text2;
	private String[] delta_filter_type = {"Doesn't matter", "Between", "Equals", "Greater than", "Less than"};
	private Combo startwith_dropdown;
	private Text item_name_text;
	private Button[] ram_and_disk_buttons;
	private Button[] thread_event_buttons;
	private Button[] system_events_buttons;
	private TextVerifyListener verify_listener = new TextVerifyListener();
	private FilterInput input;
	private Button reset_button;
	
	/**
	 * Construction
	 * @param parent shell
	 */
	public AdvancedFilterDialog(Shell parent) {
		super(parent);
		setShellStyle(getShellStyle()|SWT.RESIZE);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.RESIZE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setSize(400, 500);
		composite.getShell().setText("Custom filter");
		
		Group item_name_grp = new Group(composite, SWT.NONE);
		item_name_grp.setText("Item name");
		item_name_grp.setLayout(new GridLayout(3,false));
		item_name_grp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label item_label = new Label(item_name_grp, SWT.WRAP);
		item_label.setText("Item name:");
		
		startwith_dropdown = new Combo(item_name_grp, SWT.BORDER|SWT.READ_ONLY);
		startwith_dropdown.setItems(new String[]{"Start with", "Contains"});
		startwith_dropdown.select(0);
		
		item_name_text = new Text(item_name_grp, SWT.BORDER);
		item_name_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
		
		Group event_name_grp = new Group(composite, SWT.NONE);
		event_name_grp.setText("Event name");
		event_name_grp.setLayout(new GridLayout(3,true));
		event_name_grp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label eventLabel = new Label(event_name_grp, SWT.WRAP|SWT.BOLD);
		eventLabel.setText("RAM and Disk memory events:");
		GridData eventLabelGD = new GridData(GridData.FILL_HORIZONTAL);
		eventLabelGD.horizontalSpan = 4;
		eventLabel.setLayoutData(eventLabelGD);
		
		String[] ram_and_disk_event_names = {"RAM used", "Disk used"};
		ram_and_disk_buttons = new Button[2];
		for(int i=0; i<ram_and_disk_buttons.length; i++)
		{
			ram_and_disk_buttons[i] = new Button(event_name_grp, SWT.CHECK);
			ram_and_disk_buttons[i].setText(ram_and_disk_event_names[i]);
		}
		
		Label thread_events_label = new Label(event_name_grp, SWT.WRAP|SWT.BOLD);
		thread_events_label.setText("Thread events:");
		thread_events_label.setLayoutData(eventLabelGD);
		
		String[] thread_event_names = {"Heap size", "Heap allocated space", "Heap allocated cell count", "No of Files", "No of PS Handles"};
		thread_event_buttons = new Button[thread_event_names.length];
		for (int i = 0; i < thread_event_buttons.length; i++) {
			thread_event_buttons[i] = new Button(event_name_grp, SWT.CHECK);
			thread_event_buttons[i].setText(thread_event_names[i]);
		}
		
		Label system_events_label = new Label(event_name_grp, SWT.WRAP|SWT.BOLD);
		system_events_label.setText("System data events:");
		system_events_label.setLayoutData(eventLabelGD);
		
		String[] system_event_names = {"System Data"};
		system_events_buttons = new Button[system_event_names.length];
		for (int i = 0; i < system_events_buttons.length; i++) {
			system_events_buttons[i] = new Button(event_name_grp, SWT.CHECK);
			system_events_buttons[i].setText(system_event_names[i]);
		}
		
		Group severity_grp = new Group(composite, SWT.NONE);
		severity_grp.setText("Severity");
		severity_grp.setLayout(new GridLayout(4, true));
		severity_grp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		String[] severity_names = {"All", "Critical", "High", "Normal"};
		severity_buttons = new Button[4];
		for (int i = 0; i < severity_buttons.length; i++) {
			severity_buttons[i] = new Button(severity_grp, SWT.CHECK);
			severity_buttons[i].setText(severity_names[i]);
			severity_buttons[i].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			severity_buttons[i].addSelectionListener(this);
			severity_buttons[i].setEnabled(false);
		}
		severity_buttons[0].setEnabled(true);
		severity_buttons[0].setSelection(true);
		
		Group delta_grp = new Group(composite, SWT.NONE);
		delta_grp.setText("Delta");
		delta_grp.setLayout(new GridLayout(5, false));
		delta_grp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label delta_label = new Label(delta_grp, SWT.WRAP);
		delta_label.setText("Delta (bytes):");
		
		delta_dropdown = new Combo(delta_grp, SWT.BORDER|SWT.READ_ONLY);
		delta_dropdown.setItems(delta_filter_type);
		delta_dropdown.select(0);
		delta_dropdown.addSelectionListener(this);
		
		GridData textGD = new GridData(GridData.FILL);
		textGD.widthHint = 100;
		start_text = new Text(delta_grp, SWT.BORDER);
		start_text.setText("0");
		start_text.setLayoutData(textGD);
		start_text.setEnabled(false);
		start_text.addVerifyListener(verify_listener);
		
		Label and_label = new Label(delta_grp, SWT.WRAP);
		and_label.setText("and");
		
		end_text = new Text(delta_grp, SWT.BORDER);
		end_text.setText("0");
		end_text.setLayoutData(textGD);
		end_text.setEnabled(false);
		end_text.addVerifyListener(verify_listener);
		
		Label delta_label2 = new Label(delta_grp, SWT.WRAP);
		delta_label2.setText("Delta (count):");
		
		delta_dropdown2 = new Combo(delta_grp, SWT.BORDER|SWT.READ_ONLY);
		delta_dropdown2.setItems(delta_filter_type);
		delta_dropdown2.select(0);
		delta_dropdown2.addSelectionListener(this);
		
		start_text2 = new Text(delta_grp, SWT.BORDER);
		start_text2.setText("0");
		start_text2.setLayoutData(textGD);
		start_text2.setEnabled(false);
		start_text2.addVerifyListener(verify_listener);
		
		Label and_label2 = new Label(delta_grp, SWT.WRAP);
		and_label2.setText("and");
		
		end_text2 = new Text(delta_grp, SWT.BORDER);
		end_text2.setText("0");
		end_text2.setLayoutData(textGD);
		end_text2.setEnabled(false);
		end_text2.addVerifyListener(verify_listener);
		
		restorePreviousValues();
		return super.createContents(composite);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		filter_button = this.createButton(parent, IDialogConstants.OK_ID, "Filter", true);
		filter_button.addSelectionListener(this);
		reset_button = this.createButton(parent, IDialogConstants.DESELECT_ALL_ID, "Reset", false);
		reset_button.addSelectionListener(this);
		cancel_button = this.createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		cancel_button.addSelectionListener(this);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		if(e.widget == severity_buttons[0])
		{
			for (int i = 1; i < severity_buttons.length; i++) {
				severity_buttons[i].setEnabled(false);
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if(e.widget == severity_buttons[0])
		{
			for (int i = 1; i < severity_buttons.length; i++) {
				severity_buttons[i].setEnabled(!severity_buttons[0].getSelection());
			}
		}
		else if(e.widget == delta_dropdown)
		{
			changeTextBoxStatus(delta_dropdown, start_text, end_text);
		}
		else if(e.widget == delta_dropdown2)
		{
			changeTextBoxStatus(delta_dropdown2, start_text2, end_text2);
		}
	}
	
	/**
	 * Get filter options
	 * @return options
	 */
	public FilterInput getFilterOptions()
	{
		return input;
	}
	
	/**
	 * Enable/Disable the To and From text boxes based on the selection
	 * @param drop_down Combo
	 * @param start To Text box
	 * @param end From Text box
	 */
	private void changeTextBoxStatus(Combo drop_down, Text start, Text end)
	{
		int i = drop_down.getSelectionIndex();
		switch (i) {
		case 0:
			start.setEnabled(false);
			end.setEnabled(false);
			break;
		case 1:
			start.setEnabled(true);
			end.setEnabled(true);
			break;
		case 2:
			start.setEnabled(true);
			end.setEnabled(false);
			break;
		case 3:
			start.setEnabled(true);
			end.setEnabled(false);
			break;
		case 4:
			start.setEnabled(true);
			end.setEnabled(false);
			break;
						
		default:
			break;
		}
	}
	
	/**
	 * Helper class to store all settings in the Advanced Dialog
	 *
	 */
	public class FilterInput
	{
		int filter_option;
		String filter_text;
		String[] events;
		AnalyserConstants.Priority[] severities;
		int delta_bytes_option;
		long start_size;
		long end_size;
		int delta_count_option;
		long start_count;
		long end_count;
		public int getDelta_count_option() {
			return delta_count_option;
		}
		public void setDelta_count_option(int delta_count_option) {
			this.delta_count_option = delta_count_option;
		}
		public int getDelta_bytes_option() {
			return delta_bytes_option;
		}
		public void setDelta_kb_option(int delta_kb_option) {
			this.delta_bytes_option = delta_kb_option;
		}
		public long getEnd_count() {
			return end_count;
		}
		public void setEnd_count(long end_count) {
			this.end_count = end_count;
		}
		public long getEnd_size() {
			return end_size;
		}
		public void setEnd_size(long end_size) {
			this.end_size = end_size;
		}
		public String[] getEvents() {
			return events;
		}
		public void setEvents(String[] events) {
			this.events = events;
		}
		public int getName_Filter_option() {
			return filter_option;
		}
		private void setFilter_option(int filter_option) {
			this.filter_option = filter_option;
		}
		public String getFilter_text() {
			return filter_text;
		}
		public void setFilter_text(String filter_text) {
			this.filter_text = filter_text;
		}
		public AnalyserConstants.Priority[] getSeverities() {
			return severities;
		}
		public void setSeverities(AnalyserConstants.Priority[] severities) {
			this.severities = severities;
		}
		public long getStart_count() {
			return start_count;
		}
		public void setStart_count(long start_count) {
			this.start_count = start_count;
		}
		public long getStart_size() {
			return start_size;
		}
		public void setStart_size(long start_size) {
			this.start_size = start_size;
		}
		
	}
	
	/**
	 * Verify Listener to stop typing alphabetics in the To and From text boxes.
	 *
	 */
	private class TextVerifyListener implements VerifyListener
	{
		public void verifyText(VerifyEvent event) {
			switch (event.keyCode) {  
            case SWT.BS:           // Backspace  
            case SWT.DEL:          // Delete  
            case SWT.HOME:         // Home  
            case SWT.END:          // End  
            case SWT.ARROW_LEFT:   // Left arrow  
            case SWT.ARROW_RIGHT:  // Right arrow  
                return;  
			}
			if (!Character.isDigit(event.character)) {  
	            event.doit = false;  // disallow the action  
	        }  
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		saveValues();
		input = new FilterInput();
		input.setFilter_option(startwith_dropdown.getSelectionIndex());
		input.setFilter_text(item_name_text.getText());
		
		input.setEvents(getSelectedEvents());
		
		ArrayList<AnalyserConstants.Priority> severities = new ArrayList<AnalyserConstants.Priority>();
		for (int i = 0; i < severity_buttons.length; i++) {
			if(i==0 && severity_buttons[i].getSelection())
				break;
			else if(severity_buttons[i].getSelection())
				severities.add(AnalyserConstants.Priority.valueOf(severity_buttons[i].getText().toUpperCase()));
		}
		input.setSeverities(severities.toArray(new AnalyserConstants.Priority[0]));
		
		input.setDelta_kb_option(delta_dropdown.getSelectionIndex());
		input.setStart_size(Long.parseLong(start_text.getText()));
		input.setEnd_size(Long.parseLong(end_text.getText()));
		
		input.setDelta_count_option(delta_dropdown2.getSelectionIndex());
		input.setStart_count(Long.parseLong(start_text2.getText()));
		input.setEnd_count(Long.parseLong(end_text2.getText()));			
		
		if(getShell()!=null)
			getShell().dispose();
		setReturnCode(Dialog.OK);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		
		if(buttonId == IDialogConstants.DESELECT_ALL_ID)
		{
			startwith_dropdown.select(0);
			item_name_text.setText("");
			for (int i = 0; i < ram_and_disk_buttons.length; i++) {
				ram_and_disk_buttons[i].setSelection(false);
			}
			for (int i = 0; i < thread_event_buttons.length; i++) {
				thread_event_buttons[i].setSelection(false);
			}
			for (int i = 0; i < system_events_buttons.length; i++) {
				system_events_buttons[i].setSelection(false);
			}
			severity_buttons[0].setSelection(true);
			severity_buttons[0].notifyListeners(SWT.Selection, new Event());
			delta_dropdown.select(0);
			delta_dropdown.notifyListeners(SWT.Selection, new Event());
			delta_dropdown2.select(0);
			delta_dropdown2.notifyListeners(SWT.Selection, new Event());
		}
		super.buttonPressed(buttonId);
	}
	
	/**
	 * Returns string array of all selected events
	 * @return events
	 */
	public String[] getSelectedEvents()
	{
		ArrayList<String> events = new ArrayList<String>();
		for (int i = 0; i < ram_and_disk_buttons.length; i++) {
			if(ram_and_disk_buttons[i].getSelection())
				events.add(ram_and_disk_buttons[i].getText());
		}
		for (int i = 0; i < thread_event_buttons.length; i++) {
			if(thread_event_buttons[i].getSelection())
				events.add(thread_event_buttons[i].getText());
		}
		for (int i = 0; i < system_events_buttons.length; i++) {
			if(system_events_buttons[i].getSelection())
				events.add(system_events_buttons[i].getText());
		}
		return events.toArray(new String[events.size()]);
	}
	
	/**
	 * Returns array of selected severities. If the array size is 0, then assume that all severities selected.
	 * @return severities array
	 */
	public String[] getSelectedSeverities()
	{
		ArrayList<String> severities = new ArrayList<String>();
		for (int i = 1; i < severity_buttons.length; i++) {
			if(severity_buttons[i].getSelection())
				severities.add(severity_buttons[i].getText());
		}
		return severities.toArray(new String[0]);
	}
	
	/**
	 * Returns start and end values for the delta to search.
	 * @param drop_down
	 * @param start
	 * @param end
	 * @return
	 */
	private String[] getToFromValues(Combo drop_down, Text start, Text end)
	{
		int i = drop_down.getSelectionIndex();
		String[] values = null;
		switch (i) {
		case 0:
			break;
		case 1:
			values = new String[2];
			values[0] = start.getText();
			values[1] = end.getText();
			break;
		case 2:
			values = new String[1];
			values[0] = start.getText();
			break;
		case 3:
			values = new String[1];
			values[0] = start.getText();
			break;
		case 4:
			values = new String[1];
			values[0] = start.getText();
			break;
		default:
			break;
		}
		return values;
	}
	
	/**
	 * Saves values to dialog settings.
	 *
	 */
	private void saveValues() {
		SaveFilterOptions save = new SaveFilterOptions();
		save.saveDropdownOption(ValueTypes.FILTER_TYPE, startwith_dropdown.getText());
		save.saveDropdownOption(ValueTypes.ITEM_TEXT, item_name_text.getText());
		String[] events = getSelectedEvents();
		save.saveValues(ValueTypes.EVENTS, events);
		String[] severities = getSelectedSeverities();
		save.saveValues(ValueTypes.SEVERITIES, severities);
		save.saveDropdownOption(ValueTypes.SIZE_TYPE, delta_dropdown.getText());
		save.saveValues(ValueTypes.SIZES, getToFromValues(delta_dropdown,start_text, end_text));
		save.saveDropdownOption(ValueTypes.COUNT_TYPE, delta_dropdown2.getText());
		save.saveValues(ValueTypes.COUNTS, getToFromValues(delta_dropdown2,start_text2, end_text2));
	}

	/**
	 * Restore privious values and shows in advanced dialog.
	 *
	 */
	private void restorePreviousValues() {
		SaveFilterOptions restore = new SaveFilterOptions();
		
		String filter_option = restore.getPreviousDropdownOption(ValueTypes.FILTER_TYPE);
		if(filter_option!=null)
			startwith_dropdown.select(startwith_dropdown.indexOf(filter_option));
		String filter_text = restore.getPreviousDropdownOption(ValueTypes.ITEM_TEXT);
		if(filter_text!=null)
			item_name_text.setText(filter_text);
		String[] events = restore.getValues(ValueTypes.EVENTS);
		String[] severities = restore.getValues(ValueTypes.SEVERITIES);
		String delta_size_option = restore.getPreviousDropdownOption(ValueTypes.SIZE_TYPE);
		String[] sizes = restore.getValues(ValueTypes.SIZES);
		String delta_count_option = restore.getPreviousDropdownOption(ValueTypes.COUNT_TYPE);
		String[] counts = restore.getValues(ValueTypes.COUNTS);
		
		List<String> events_list = new ArrayList<String>();
		if(events!=null)
			events_list = Arrays.asList(events);
		List<String> severity_list = new ArrayList<String>();
		if(severities!=null)
			severity_list = Arrays.asList(severities);
		
		for (int i = 0; i < ram_and_disk_buttons.length; i++) {
			if(events_list.contains(ram_and_disk_buttons[i].getText()))
				ram_and_disk_buttons[i].setSelection(true);
		}
		for (int i = 0; i < thread_event_buttons.length; i++) {
			if(events_list.contains(thread_event_buttons[i].getText()))
				thread_event_buttons[i].setSelection(true);
		}
		for (int i = 0; i < system_events_buttons.length; i++) {
			if(events_list.contains(system_events_buttons[i].getText()))
				system_events_buttons[i].setSelection(true);
		}
		for (int i = 1; i < severity_buttons.length; i++) {
			if(severity_list.contains(severity_buttons[i].getText()))
			{
				severity_buttons[0].setSelection(false);
				severity_buttons[0].notifyListeners(SWT.Selection, new Event());
				severity_buttons[i].setSelection(true);
			}
		}
		if(delta_size_option!=null)
		{
			delta_dropdown.select(delta_dropdown.indexOf(delta_size_option));
			delta_dropdown.notifyListeners(SWT.Selection, new Event());
			
			//setText will not work if the verify listener is there.
			//So, remove it for temporary
			start_text.removeVerifyListener(verify_listener);
			end_text.removeVerifyListener(verify_listener);
			if(sizes!=null && sizes.length == 2)
			{
				start_text.setText(sizes[0]);
				end_text.setText(sizes[1]);
			}
			else if(sizes!=null && sizes.length == 1)
			{
				start_text.setText(sizes[0]);
			}
			//Add listeners again
			start_text.addVerifyListener(verify_listener);
			end_text.addVerifyListener(verify_listener);
		}
		
		if(delta_count_option!=null)
		{
			delta_dropdown2.select(delta_dropdown2.indexOf(delta_count_option));
			delta_dropdown2.notifyListeners(SWT.Selection, new Event());
			
			//setText will not work if the verify listener is there.
			//So, remove it for temporary
			start_text2.removeVerifyListener(verify_listener);
			end_text2.removeVerifyListener(verify_listener);
			if(counts!=null && counts.length == 2)
			{
				start_text2.setText(counts[0]);
				end_text2.setText(counts[1]);
			}
			else if(counts!=null && counts.length == 1)
				start_text2.setText(counts[0]);
			
			//Add listeners again
			start_text2.addVerifyListener(verify_listener);
			end_text2.addVerifyListener(verify_listener);
		}		
	}	
}
