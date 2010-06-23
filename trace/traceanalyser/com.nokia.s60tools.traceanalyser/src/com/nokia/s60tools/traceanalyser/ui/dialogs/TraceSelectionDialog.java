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


package com.nokia.s60tools.traceanalyser.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


import com.nokia.s60tools.traceanalyser.model.UserEnteredData;
import com.nokia.s60tools.traceanalyser.model.UserEnteredData.ValueTypes;
import com.nokia.s60tools.traceanalyser.resources.ImageKeys;
import com.nokia.s60tools.traceanalyser.resources.ImageResourceManager;
import com.nokia.s60tools.traceanalyser.ui.views.MainView;
import com.nokia.traceviewer.api.TraceViewerAPI;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;
import com.nokia.traceviewer.engine.activation.TraceActivationGroupItem;
import com.nokia.traceviewer.engine.activation.TraceActivationTraceItem;


/**
 * class Trace selection dialog
 * Dialog that can be used for selecting one Trace item from imported trace dictionaries.
 */
public class TraceSelectionDialog extends TitleAreaDialog  implements SelectionListener, ModifyListener {
	
	
	/* UI-components */

	/* Dictionary-related components */
	private Composite compositeDictionaries;
	private Table tableGroups;
	private Label labelDictinaryFilter;
	private Text textDictionaryFilter;

	/* Group related components */
	private Composite compositeGroups;
	private Table tableDictionaries;
	private Label labelGroupFilter;
	private Text textGroupFilter;
	
	/* Trace related components */
	private Composite compositeTraces;
	private Table tableTraces;
	private Label labelTraceFilter;
	private Text textTraceFilter;
	
	
	/* List of trace dictionaries */
	private ArrayList<TraceActivationComponentItem> dictionaryList;
	
	/* Currently selected trace item */
	TraceActivationTraceItem selectedTrace;
	
	/* Strings */
	public final static String TEXT_FILTER = "Filter:";
	
	/* Column width's for each table */
	public final static int COLUMN_DICTIONARIES_NAME = 150;
	public final static int COLUMN_GROUPS_ID = 45;
	public final static int COLUMN_GROUPS_NAME = 200;
	public final static int COLUMN_TRACES_ID = 45;
	public final static int COLUMN_TRACES_NAME = 350;

	@SuppressWarnings("unchecked")
	public class TraceActivationComponentComparator implements Comparator {

		public final int compare(Object pFirst, Object pSecond) {
			String aFirstWeight = ((TraceActivationComponentItem) pFirst).getName();
			String aSecondWeight = ((TraceActivationComponentItem) pSecond).getName();
			return aFirstWeight.compareToIgnoreCase(aSecondWeight);
		} // end compare

	}
	   
	/**
	 * TraceSelectionDialog.
	 * Constructor.
	 * @param parentShell
	 */
	public TraceSelectionDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
	}

	@Override
	/**
	 * createDialogArea.
	 */
	protected Control createDialogArea(Composite parent) {


		getShell().setText("Select Trace");

		setTitle("Select Trace For Rule");
		setTitleImage(ImageResourceManager.getImage(ImageKeys.IMG_TRACE_ANALYSER_BANNER));

		// Set the minimum size for dialog
		getShell().setMinimumSize(new Point(600, 500));
		// create composite where all components are placed
		Composite contents = new Composite(parent, SWT.NONE);
 		GridLayout contentsLayout = new GridLayout();
 		contentsLayout.numColumns = 1;
 		contents.setLayout(contentsLayout);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
		// create group where all components are placed
		Group group = new Group(contents, SWT.NONE);
		GridLayout layoutContents = new GridLayout();
 		layoutContents.numColumns = 3;
 		group.setLayout(layoutContents);
 		group.setText("Select Trace");
		GridData contentsGridData = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(contentsGridData);
 		
		// create tables for trace selection
		this.createDictionaryTable( group );
 		this.createGroupsTable(group);
 		this.createTracesTable(group);
		loadUserEnteredData();

 		setHelps();
		return contents;
	}
	
	/**
	 * createDictionaryTable.
	 * Places dictionary table into given composite.
	 * @param composite composite where table is placed.
	 */
	private void createDictionaryTable( Composite composite ){
		
 		// Create dictionaries - composite
 		compositeDictionaries = new Composite( composite, SWT.NONE );
 		compositeDictionaries.setLayoutData(new GridData(GridData.FILL_BOTH));
 		GridLayout layoutDictionaries = new GridLayout();
 		layoutDictionaries.numColumns = 2;
 		compositeDictionaries.setLayout(layoutDictionaries);
		
		// Table that contains all traces from selected dictionary
		tableDictionaries = new Table(compositeDictionaries, SWT.BORDER | SWT.FULL_SELECTION );		
		GridData gridDataDictionaryTable = new GridData(GridData.FILL_BOTH);
		gridDataDictionaryTable.horizontalSpan = 2;
		gridDataDictionaryTable.heightHint = 250;
		tableDictionaries.setLayoutData(gridDataDictionaryTable);
		tableDictionaries.setHeaderVisible(true);
		tableDictionaries.addSelectionListener( this );
		
		// add columns into table
		TableColumn columnDictionary = new TableColumn(tableDictionaries, SWT.LEFT);
		columnDictionary.setText("Dictionary Name");
		columnDictionary.setWidth(COLUMN_DICTIONARIES_NAME);
		
		// Create dictionary filter label
		labelDictinaryFilter = new Label(compositeDictionaries, SWT.NONE);
		labelDictinaryFilter.setText(TEXT_FILTER);

		// Create dictionary filter text field
		textDictionaryFilter = new Text(compositeDictionaries, SWT.BORDER);
		GridData groupFilterTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textDictionaryFilter.setToolTipText("Filter for Dictionary");
		textDictionaryFilter.setLayoutData(groupFilterTextGridData);
		textDictionaryFilter.addModifyListener(this);
		
		// update dictionary list(load list from TraceViewer):
		updateDictionaryList();

		
	}
	
	
	/**
	 * createGroupsTable.
	 * Places group table into given composite.
	 * @param composite composite where table is placed.
	 */
	private void createGroupsTable( Composite composite ){
		
		// create composite for group table
		compositeGroups = new Composite( composite, SWT.NONE );
		compositeGroups.setLayoutData(new GridData(GridData.FILL_BOTH));
 		GridLayout layoutGroups = new GridLayout();
 		layoutGroups.numColumns = 2;
 		compositeGroups.setLayout(layoutGroups);
 		
		// Table that contains all groups from selected dictionary
		tableGroups = new Table(compositeGroups, SWT.BORDER | SWT.FULL_SELECTION );	
		GridData gridDataTableGroups = new GridData(GridData.FILL_BOTH);
		gridDataTableGroups.horizontalSpan = 2;
		gridDataTableGroups.heightHint = 250;
		tableGroups.setLayoutData(gridDataTableGroups);
		tableGroups.setHeaderVisible(true);
		tableGroups.addSelectionListener( this );
		
		// add columns into table
		TableColumn columnID = new TableColumn(tableGroups, SWT.LEFT);
		columnID.setText("ID");
		columnID.setWidth(COLUMN_GROUPS_ID);
		TableColumn columnGroup = new TableColumn(tableGroups, SWT.LEFT);
		columnGroup.setText("Group Name");
		columnGroup.setWidth(COLUMN_GROUPS_NAME);
		
		// Create Group filter label
		labelGroupFilter = new Label(compositeGroups, SWT.NONE);
		labelGroupFilter.setText(TEXT_FILTER);
		
		// Create trace filter text field
		textGroupFilter = new Text(compositeGroups, SWT.BORDER);
		GridData gridDataTextTraceFilter = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textGroupFilter.setToolTipText("Filter for Group");
		textGroupFilter.setLayoutData(gridDataTextTraceFilter);
		textGroupFilter.addModifyListener(this);
		
	}
	
	
	/**
	 * createTracesTable.
	 * Places group table into given composite.
	 * @param composite composite where table is placed.
	 */
	private void createTracesTable(Composite composite){

		// create composite for Traces table
		compositeTraces = new Composite( composite, SWT.NONE );
		compositeTraces.setLayoutData(new GridData(GridData.FILL_BOTH));
 		GridLayout layoutTraces = new GridLayout();
 		layoutTraces.numColumns = 2;
 		compositeTraces.setLayout(layoutTraces);
 		
		// Table that contains all traces from selected dictionary
		tableTraces = new Table(compositeTraces, SWT.BORDER | SWT.FULL_SELECTION );	
		GridData gridDataTableTraces = new GridData(GridData.FILL_BOTH);
		gridDataTableTraces.horizontalSpan = 2;
		gridDataTableTraces.heightHint = 250;
		tableTraces.setLayoutData(gridDataTableTraces);
		tableTraces.setHeaderVisible(true);
		tableTraces.addSelectionListener( this );
		
		// add columns into table
		TableColumn columnID = new TableColumn(tableTraces, SWT.LEFT);
		columnID.setText("ID");
		columnID.setWidth(COLUMN_TRACES_ID);
		TableColumn columnGroup = new TableColumn(tableTraces, SWT.LEFT);
		columnGroup.setText("Trace Name");
		columnGroup.setWidth(COLUMN_TRACES_NAME);
		
		// Create trace filter label
		labelTraceFilter = new Label(compositeTraces, SWT.NONE);
		labelTraceFilter.setText(TEXT_FILTER);
		
		// Create trace filter text field
		textTraceFilter = new Text(compositeTraces, SWT.BORDER);
		GridData gridDataTextTraceFilter = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textTraceFilter.setToolTipText("Filter for Group");
		textTraceFilter.setLayoutData(gridDataTextTraceFilter);
		textTraceFilter.addModifyListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent event) {
		
		// if doubleclick detected in trace-table, close dialog. 
		if(event.widget == tableTraces){
			okPressed();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent event) {
		if( event.widget == tableDictionaries ){
			
			// when selection in dictionary-table changes, update group list
			this.updateGroupList();
			this.updateTraceList();
			this.setSelectedTraceItem();
		}
		else if( event.widget == tableGroups ){
		
			// when selection in groups-table changes, update trace list
			this.updateTraceList();
			this.setSelectedTraceItem();

		}
		else if( event.widget == tableTraces ){

			this.setSelectedTraceItem();

		}
	}

	/**
	 * containsIgnoreCase.
	 * Contains-operation that ignores stings case.
	 * @param line string where another parameter is searched from
	 * @param searchPhrase String that is searched from another given parameter
	 * @return
	 */
	private boolean containsIgnoreCase( String line, String searchPhrase ){
		
		// convert both Strings to lowercase.
		line = line.toLowerCase();
		searchPhrase = searchPhrase.toLowerCase();
		
		// compare lowercase strings to each other.
		return line.contains(searchPhrase);
				
	}
	
	/**
	 * updateDictionaryList.
	 * Loads dictionary list from TraveViewer and updates dictionary-table
	 */
	@SuppressWarnings("unchecked")
	private void updateDictionaryList(){
		
		
		// remove all items from dictionary-table
		tableDictionaries.removeAll();
		
		
		// get dictionary components from TraceViewer
		dictionaryList = TraceViewerAPI.getDictionaryComponents();

		// Sort list into aphabetical order.
		Collections.sort(dictionaryList, new TraceActivationComponentComparator());

		if(dictionaryList.size() == 0){
			//TODO do something!
			MainView.showTraceViewer();
			MainView.showAndReturnYourself();
			dictionaryList = TraceViewerAPI.getDictionaryComponents();

		}
		
		int tableIndexNumber = 0;
		
		// go thru dictionary list and add dictionaries to table
		while( tableIndexNumber < dictionaryList.size() ){
			if( textDictionaryFilter.getText() != "" && 
				!this.containsIgnoreCase( dictionaryList.get(tableIndexNumber).getName(), textDictionaryFilter.getText() ) ){
					// if filter is set, delete dictionary name if it does not contain filter text.
					dictionaryList.remove(tableIndexNumber);
				}
			else{
				// create new table item into table
				TableItem newItem = new TableItem(tableDictionaries, SWT.NONE, tableIndexNumber);
				newItem.setText( new String[]{ dictionaryList.get(tableIndexNumber).getName() } );	
				tableIndexNumber++;
			}
		}
		
		if( dictionaryList.size() > 0){
			setMessage("Select dictionary where needed trace is found.", org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
		}
		
		if( dictionaryList.size() == 0){
			setMessage("No loaded trace dictionaries found from TraceViewer. See help for more information.", org.eclipse.jface.dialogs.IMessageProvider.WARNING);

		}


	}

	/**
	 * updateGroupList.
	 * Loads Group list from TraceViewer and updates Groups-table
	 */
	private void updateGroupList(){
	
		
		// Reset array
		tableGroups.removeAll();
		
		ArrayList<TraceActivationGroupItem> groupList = this.getSelectedDictionarysItems();
		
		if( groupList != null ){
			
			int tableIndexNumber = 0;
			// go thru dictionary list
			while( tableIndexNumber < groupList.size() ){
				if( textGroupFilter.getText() != "" && 
					!this.containsIgnoreCase( groupList.get(tableIndexNumber).getName(), textGroupFilter.getText() ) ){
					// if filter is set, delete dictionary name if it does not contain filter text.
					groupList.remove(tableIndexNumber);
					}
				else{
					// add new item into table
					TableItem newItem = new TableItem(tableGroups, SWT.NONE, tableIndexNumber);
					newItem.setText( new String[]{ "0x"+Integer.toHexString(groupList.get(tableIndexNumber).getId()), groupList.get(tableIndexNumber).getName() } );
					tableIndexNumber++;
				}
			}
		}
		
		if( groupList != null && groupList.size() > 0){
			setMessage("Select group where needed trace is found.",org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
		}
		


	}
	
	/**
	 * updateTraceList.
	 * Loads Trace list from TraceViewer and updates traces-table.
	 */
	private void updateTraceList(){
		
	
		// Reset array
		tableTraces.removeAll();
		
		ArrayList<TraceActivationTraceItem> traceList = this.getSelectedGroupsItems();
		
		boolean nullNamesFound = false;
		
		if( traceList != null ){
			int tableIndexNumber = 0;
			
			// go thru dictionary list
			while( tableIndexNumber < traceList.size() ){
				if( textTraceFilter.getText() != "" && 
				  
					!this.containsIgnoreCase( traceList.get(tableIndexNumber).getName(), textTraceFilter.getText() ) ){
					// if filter is set, delete dictionary name if it does not contain filter text.
					traceList.remove(tableIndexNumber);
					}
				else{
					// add new item into table
					TableItem newItem = new TableItem(tableTraces, SWT.NONE, tableIndexNumber);
					
					// get id and convert id to hex
					String id = "0x"+Integer.toHexString(traceList.get(tableIndexNumber).getId());
					String name = traceList.get(tableIndexNumber).getName();
					
					if(name == null){
						nullNamesFound = true;
					}
					
					newItem.setText(new String[]{id ,name});
					tableIndexNumber++;
				}
			}
		}
		
		if( traceList != null && traceList.size() > 0){
			setMessage("Select Trace.", org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);

		}
		
		
		
		if (nullNamesFound){
			setMessage("No trace names were found from trace definitions file. See help for more information.", org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
		}
	


	}

	
	
	/**
	* setSelectedGroupItem.
	* sets selected trace item into private variable called selectedTrace and enables of disables.	 
	*/
	private void setSelectedTraceItem(){
		
		ArrayList<TraceActivationTraceItem> traceList = this.getSelectedGroupsItems();
		
		if( traceList != null ){
		
			int index = tableTraces.getSelectionIndex();
			if( index != -1 == index < traceList.size()){
				this.selectedTrace = traceList.get(index);
				this.getButton(OK).setEnabled(true);
				setMessage(null);

				return;
			}		
		}
		this.getButton(OK).setEnabled(false);
		this.selectedTrace  = null;
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent event) {
		if( event.widget == textDictionaryFilter ){
			// if Dictionary filter is edited
			
			// update dictionary list
			this.updateDictionaryList();
			
			// update groups list
			this.updateGroupList();
			
			// update trace list.
			this.updateTraceList();
			
			// Update selected trece
			this.setSelectedTraceItem();

		}
		else if( event.widget == textGroupFilter ){
			// if Group filter is edited
			
			// update group list.
			this.updateGroupList();
			
			// update trace list.
			this.updateTraceList();

		}
		else if( event.widget == textTraceFilter ){
			// if Trace filter is edited
			
			// update trace list.
			this.updateTraceList();

		}
	}
	
	/**
	 * getSelectedDictionarysItems.
	 * @return currently selected dictionarys items and null is no dictionary is selected.
	 */
	private ArrayList<TraceActivationGroupItem> getSelectedDictionarysItems(){
		int index = tableDictionaries.getSelectionIndex();
		if(index != -1 && index < dictionaryList.size()){
			
			//Get selected dictionary
			TraceActivationComponentItem dictionary = dictionaryList.get(index);
			ArrayList<TraceActivationGroupItem> groupList = new ArrayList<TraceActivationGroupItem>( dictionary.getGroups() );
			return groupList;
		}
		else{
			return null;
		}
	}
	
	/**
	 * getSelectedGroupsItems.
	 * @return currently selected groups items and null if no group is selected.
	 */
	private ArrayList<TraceActivationTraceItem> getSelectedGroupsItems(){
		ArrayList<TraceActivationGroupItem> groupItems = this.getSelectedDictionarysItems();
		if( groupItems != null ){
			int index = tableGroups.getSelectionIndex();
			if(index != -1 && index < groupItems.size()){
				ArrayList<TraceActivationTraceItem> traceList = new ArrayList<TraceActivationTraceItem> (groupItems.get(index).getTraces());
				return traceList;
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	public void createButtonsForButtonBar( Composite composite ){
		super.createButtonsForButtonBar(composite);
		// OK button is always disabled when dialog is opened.
		this.setSelectedTraceItem();

	}

	/**
	 * getSelectedTrace
	 * @return Selected trace, null if no trace is selected.
	 */
	public TraceActivationTraceItem getSelectedTrace() {
		return selectedTrace;
	}
	
	

	/**
	 * loadUserEnteredData
	 * loads previous values into UI components
	 */
	private void loadUserEnteredData(){
		UserEnteredData data = new UserEnteredData();

		String previousDictionary = data.getString(ValueTypes.PREVIOUS_DICTIONARY);
		String previousGroup = data.getString(ValueTypes.PREVIOUS_GROUP);
		int ii = 0;
		while(ii < tableDictionaries.getItemCount()){
			if( tableDictionaries.getItem(ii).getText(0).equals(previousDictionary)){
				tableDictionaries.setSelection(ii);
				updateGroupList();
				ii = 0;
				while(ii < tableGroups.getItemCount()){
					if(tableGroups.getItem(ii).getText(0).equals(previousGroup)){
						tableGroups.setSelection(ii);
						updateTraceList();
						break;
					}
					ii++;
				}
				break;
			}
			ii++;
		}
		

		
 	}
	
	/**
	 * saveUserEnteredData
	 * Saves current user entered data from UI components
	 */
	public void saveUserEnteredData(){
		UserEnteredData data = new UserEnteredData();
				
		String item = tableDictionaries.getSelection()[0].getText(0);
		data.saveString(ValueTypes.PREVIOUS_DICTIONARY, item);

		item = tableGroups.getSelection()[0].getText(0);
		data.saveString(ValueTypes.PREVIOUS_GROUP, item);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	public void okPressed(){
		saveUserEnteredData();
		this.close();	
		
	}
	
	/**
	 * Set this page's context sensitive helps
	 */
	protected void setHelps() {
		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp( getShell(), com.nokia.s60tools.traceanalyser.resources.HelpContextIDs.TRACE_ANALYSER_TRACE_SELECTION_DIALOG);
	}

}
