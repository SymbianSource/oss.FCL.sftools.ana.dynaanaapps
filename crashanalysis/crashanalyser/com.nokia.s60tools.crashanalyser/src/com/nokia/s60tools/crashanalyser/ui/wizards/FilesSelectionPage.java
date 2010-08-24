/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.s60tools.crashanalyser.ui.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.*;
import org.eclipse.ui.PlatformUI;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;
import com.nokia.s60tools.crashanalyser.model.*;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;
import com.nokia.s60tools.crashanalyser.files.*;
import java.util.*;
import java.text.Collator;
import java.util.Locale;
import java.text.DateFormat;
import java.util.Date;

/**
 * This is the 2nd page in the wizard. This page contains a table from which
 * user selects the crash files he/she wants to import.
 */
public class FilesSelectionPage extends S60ToolsWizardPage implements SelectionListener{

	Button buttonSelectDeselect;
	Table tableFiles;
	TableColumn tableColumnCreated;
	TableColumn tableColumnType;
	TableColumn tableColumnRomId;
	TableColumn tableColumnFile;
	Label labelFileInfoTitle;
	Browser browserFileInfo;
	DecoderEngine engine;
	Composite composite;
	List<CrashFileBundle> files = null;
	String[] filesToBeShown = null;
	public boolean romIdsMatch = true;
	static final int TABLE_COLUMN_CREATED = 0;
	static final int TABLE_COLUMN_TYPE = 1;
	static final int TABLE_COLUMN_ROMID = 2;
	static final int TABLE_COLUMN_FILE = 3;
	static final int TABLE_COLUMN_COUNT = 4;
	
	/**
	 * Constructor
	 * @param decEng decoder engine
	 */
	public FilesSelectionPage(DecoderEngine decEng, String[] showOnlyTheseFiles) {
		super("");
			
		setTitle("Select Files");
			
		setDescription("Select crash files to be imported.");

		// User cannot finish the page before some valid 
		// selection is made.
		setPageComplete(false);
		engine = decEng;
		filesToBeShown = showOnlyTheseFiles;
	}

	public void showOnlyTheseFiles(String[] files) {
		filesToBeShown = files;
	}
	
	@Override
	public void recalculateButtonStates() {
		// no implementation needed
	}

	@Override
	public void setInitialFocus() {
		tableFiles.setFocus();
	}

	/**
	 * Creates all UI controls
	 */
	public void createControl(Composite parent) {
		composite =  new Composite(parent, SWT.NULL);
		
	    // create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		composite.setLayout(gl);

		// Table
		tableFiles = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL);
		tableFiles.setLinesVisible (true);
		tableFiles.setHeaderVisible (true);
		
		// Created column
		tableColumnCreated = new TableColumn (tableFiles, SWT.NONE);
		tableColumnCreated.setText("Created");
		tableColumnCreated.addSelectionListener(this);
		
		// Type column
		tableColumnType = new TableColumn (tableFiles, SWT.NONE);
		tableColumnType.setText("Type");
		tableColumnType.addSelectionListener(this);

		// ROM ID column
		tableColumnRomId = new TableColumn (tableFiles, SWT.NONE);
		tableColumnRomId.setText("ROM ID");
		tableColumnRomId.addSelectionListener(this);

		// File column
		tableColumnFile = new TableColumn (tableFiles, SWT.NONE);
		tableColumnFile.setText("File");
		tableColumnFile.addSelectionListener(this);
		
		autoSizeTableColumns();
		
		GridData treeGD = new GridData(GridData.FILL_BOTH);
		tableFiles.setLayoutData(treeGD);		
		tableFiles.addSelectionListener(this);
		
		// Select/Deselect button
		buttonSelectDeselect = new Button(composite, SWT.PUSH);
		buttonSelectDeselect.setText("Select/Unselect");
		buttonSelectDeselect.addSelectionListener(this);

		// File info title
		labelFileInfoTitle = new Label(composite, SWT.LEFT);
		labelFileInfoTitle.setText("File Info");
		
		// File info
		browserFileInfo = new Browser(composite, SWT.BORDER);
		GridData textGD = new GridData(GridData.FILL_HORIZONTAL);
		textGD.heightHint = 150;
		browserFileInfo.setLayoutData(textGD);
		browserFileInfo.setText("");
		
		setHelps();
		
		setInitialFocus();
		
		setControl(composite);		
		
		updateButtons();
	}
	
	/**
	 * Autos sizes all table columns
	 */
	void autoSizeTableColumns() {
		for (int i = 0; i < TABLE_COLUMN_COUNT; i++) {
			tableFiles.getColumn(i).pack();
		}		
	}
	
	/**
	 * Loads the table with files given by decoder engine
	 */
	public void loadTable() {
		tableFiles.removeAll();
		files = engine.getCrashFiles();
		if (files == null || files.size() < 1) {
			setErrorMessage("No crash files found.");
		} else {
			for (int i = 0; i < files.size(); i++) {
				CrashFileBundle file = files.get(i);
				String fileType = "";
				String fileName = "";
				// summary xml file
				if (file.isPartiallyDecoded()) {
					SummaryFile summaryFile = file.getSummaryFile();
					fileType = summaryFile.getSourceFileType();
					fileName = summaryFile.getSourceFileName();
				// .crashxml file
				} else {
					CrashFile crashfile = file.getCrashFile();
					fileType = crashfile.getSourceFileType();
					fileName = crashfile.getFileName();
				}
				// if user has drag&dropped multiple files, we have scanned the whole
				// folder, but we should show only the dragged ones
				if (!shouldCrashFileBeShown(fileName))
					continue;
				TableItem item = new TableItem (tableFiles, SWT.NONE);
				item.setText(0, file.getTime());
				item.setText(1, fileType);
				item.setText(2, file.getRomId());
				item.setText(3, fileName);
				item.setData(file);
				item.setChecked(true);
			}
			autoSizeTableColumns();
		}
		browserFileInfo.setText("");
		
		if (tableFiles.getItemCount() > 0) {
			tableFiles.select(0);
			showFileInfo();
		}
	}
	
	/**
	 * Checks whether given crash file should be shown in table. if user has drag&dropped 
	 * multiple files, we have scanned the whole folder, but we should show only the dragged ones. 
	 * @param fileName file to be checked
	 * @return true if file should be shown, false if not
	 */
	boolean shouldCrashFileBeShown(String fileName) {
		if (filesToBeShown != null && filesToBeShown.length > 0) {
			for (int i = 0; i < filesToBeShown.length; i++) {
				if (fileName.equalsIgnoreCase(FileOperations.getFileNameWithExtension(filesToBeShown[i])))
					return true;
			}
			return false;

		} else {
			return true;
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// no implementation needed
	}

	public void widgetSelected(SelectionEvent e) {
		// Select/Unselect button pressed
		if (e.widget == buttonSelectDeselect) {
			TableItem[] items = tableFiles.getItems();
			if (items != null) {
				boolean checkAll = true;
				// check whether items needs to be checked or unchecked
				for (int i = 0; i < items.length; i++) {
					if (items[i].getChecked()) {
						checkAll = false;
						break;
					}
				}
				
				// check/uncheck all items
				for (int i = 0; i < items.length; i++)
					items[i].setChecked(checkAll);
			}
			showFileInfo();
			updateButtons();
			
		// table item selected/checked/unchecked
		} else if (e.widget == tableFiles) {
			// item was checked/unchecked
			if (e.detail == SWT.CHECK) {
				tableFiles.setSelection((TableItem)e.item);
				updateButtons();
			}
			showFileInfo();
			
		// table column presses -> sort
		} else if (e.widget == tableColumnCreated ) {
			sortTableByColumn(TABLE_COLUMN_CREATED, tableColumnCreated);
		} else if (e.widget == tableColumnType) {
			sortTableByColumn(TABLE_COLUMN_TYPE, tableColumnType);
		} else if (e.widget == tableColumnRomId) {
			sortTableByColumn(TABLE_COLUMN_ROMID, tableColumnRomId);
		} else if (e.widget == tableColumnFile) {
			sortTableByColumn(TABLE_COLUMN_FILE, tableColumnFile);
		}
	}
	
	/**
	 * Sorts table by given column number
	 * @param column column number
	 * @param col column item
	 */
	void sortTableByColumn(int column, TableColumn col) {
		TableItem[] items = tableFiles.getItems();
		Collator collator = Collator.getInstance(Locale.getDefault());

		TableColumn sortColumn = tableFiles.getSortColumn();
		
		// column is sorted for the "first time" or column is sorted UP. Sort column DOWN.
		if (sortColumn == null || !col.equals(sortColumn) || tableFiles.getSortDirection() == SWT.UP) {
			for (int i = 1; i < items.length; i++) {
				String value1 = items[i].getText(column);
				for (int j = 0; j < i; j++) {
					String value2 = items[j].getText(column);
					// time compare
					if (column == TABLE_COLUMN_CREATED) {
						try {
							DateFormat date = DateFormat.getInstance();
							Date d1 = date.parse(value1);
							Date d2 = date.parse(value2);
							if (d1 != null && d2 != null && d1.compareTo(d2) < 0) {
								boolean checked = items[i].getChecked();
								String[] values = { items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3)};
								items[i].dispose();
								TableItem item = new TableItem(tableFiles, SWT.NONE, j);
								item.setText(values);
								item.setChecked(checked);
								items = tableFiles.getItems();
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					// string compare
					} else {
						if (collator.compare(value1, value2) < 0) {
							boolean checked = items[i].getChecked();
							String[] values = { items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3)};
							items[i].dispose();
							TableItem item = new TableItem(tableFiles, SWT.NONE, j);
							item.setText(values);
							item.setChecked(checked);
							items = tableFiles.getItems();
							break;
						}
					}
				}
			}
			tableFiles.setSortColumn(col);
			tableFiles.setSortDirection(SWT.DOWN);
		
		// Reverse rows
		} else {
			int index = items.length-1;
			for (int i = 0; i < items.length-1; i++) {
				boolean checked = items[index].getChecked();
				String[] values = { items[index].getText(0), items[index].getText(1), items[index].getText(2), items[index].getText(3)};
				items[index].dispose();
				TableItem item = new TableItem(tableFiles, SWT.NONE, i);
				item.setText(values);
				item.setChecked(checked);
				items = tableFiles.getItems();
			}
			
			tableFiles.setSortColumn(col);
			tableFiles.setSortDirection(SWT.UP);
		}
	}
	
	/**
	 * Updates next, finish button states
	 */
	void updateButtons() {
		try {
			getWizard().getContainer().updateButtons();
		} catch (Exception E) {
		}
	}
	
	/**
	 * Show information about the currently selected file
	 */
	void showFileInfo() {
		int selected = tableFiles.getSelectionIndex();
		if (selected >= 0) {
			CrashFileBundle cfb = (CrashFileBundle)tableFiles.getItem(selected).getData();
			browserFileInfo.setText(HtmlFormatter.formatHtmlStyle(labelFileInfoTitle.getFont(), 
																	cfb.getDescription(false)));
		} else {
			browserFileInfo.setText("");
		}
	}

	/**
	 * Checks whether we can press Next button
	 */
	public boolean canFlipToNextPage() {
		try {
			// decoder engine contains no files (shouldn't happen)
			if (files == null || files.size() < 1)
				return false;
			
			if (onlyCrashxmlFilesSelected())
			{
				// If there are just crashxml files then we do not need symbol files ->
				// only Finish button is enabled. 
				return false;
			}
			
			TableItem[] items = tableFiles.getItems();
			// no items in table (shouldn't happen)
			if (items == null) {
				return false;
			// table contains items
			} else {
				String romId = "";
				boolean selectedFiles = false;
				boolean hasMultipleRomIds = false;
				// check whether user is trying to import files which 
				// have different ROM IDs
				for (int i = 0; i < items.length; i++) {
					CrashFileBundle file = (CrashFileBundle)items[i].getData();
					if (!items[i].getChecked() || file == null)
						continue;

					selectedFiles = true;
					
					// ignore ROM ID of .crashxml file
					if (file.isFullyDecoded())
						continue;
					
					//ignore if file has no rom id
					if ("".equals(file.getRomId()))
						continue;
					
					if (!"".equals(romId) && !file.getRomId().equalsIgnoreCase(romId)) {
						hasMultipleRomIds = true;
					}
					romId = file.getRomId();
				}
				
				// don't allow to proceed when multiple different ROM ID file are selected
				if (hasMultipleRomIds) {
					this.setErrorMessage("ROM IDs of selected files do not match. Please select only files which have same ROM IDs.");
					return false;
				} else {
					this.setErrorMessage(null);
				}
				
				// no checked files
				if (!selectedFiles)
					return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}		
	
	/**
	 * Checks whether Finish button can be enabled. Finish button can be
	 * enabled if user is importing only already decoded files (.crashxml files).
	 * 
	 * @return true if Finish button can be enable, false if not
	 */
	public boolean canFinish() {
		try {
			// if no files in this page (shouldn't happen)
			if (files == null || files.size() < 1)
				return false;
			
			TableItem[] items = tableFiles.getItems();
			// if no files in table (shouldn't happen)
			if (items == null) {
				return false;
			} else {
				boolean hasAnalyzedFiles = false;
				boolean hasUndecodedFiles = false;
				// go through all checked files in the table
				for (int i = 0; i < items.length; i++) {
					if (!items[i].getChecked())
						continue;
					
					CrashFileBundle file = (CrashFileBundle)items[i].getData();
					if (file != null) {
						if (file.isPartiallyDecoded()) {
							hasUndecodedFiles = true;
						} else {
							hasAnalyzedFiles = true;
						}
					}
				}
				
				// only .crashxml files are selected, we can show Finish button
				if (hasAnalyzedFiles && !hasUndecodedFiles)
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Checks whether only .crashxml files are selected in the table 
	 * @return true if only .crashxml files are selected, false if not
	 */
	public boolean onlyCrashxmlFilesSelected() {
		try {
			// if no files in this page (shouldn't happen)
			if (files == null || files.size() < 1)
				return false;
			
			TableItem[] items = tableFiles.getItems();
			// if no files in table (shouldn't happen)
			if (items == null) {
				return false;
			} else {
				boolean onlyCrashxmlFiles = true;
				// go through all checked files in the table
				for (int i = 0; i < items.length; i++) {
					if (!items[i].getChecked())
						continue;
					CrashFileBundle file = (CrashFileBundle)items[i].getData();
					if (file.isPartiallyDecoded()) {
						onlyCrashxmlFiles = false;
						break;
					}
				}
				
				if (onlyCrashxmlFiles)
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	/**
	 * Returns indexes of those Crash files which are selected (checked) in the table
	 * @return indexes of those Crash files which are selected (checked) in the table
	 */
	public List<Integer> getFileIndexes() {
		List<Integer> indexes = new ArrayList<Integer>();
		
		TableItem[] items = tableFiles.getItems();
		// if no files in table (shouldn't happen)
		if (items == null) {
			return null;
		} else {
			// go through all checked files in the table
			for (int i = 0; i < items.length; i++) {
				if (items[i].getChecked())
					indexes.add(i);
			}
		}
		
		if (indexes.isEmpty()) {
			return null;
		} else {
			return indexes;
		}
	}
	
	/**
	 * Sets this page's context sensitive helps
	 *
	 */
	protected void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableFiles,
				HelpContextIDs.CRASH_ANALYSER_HELP_IMPORT_CRASH_FILES);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(browserFileInfo,
				HelpContextIDs.CRASH_ANALYSER_HELP_IMPORT_CRASH_FILES);		

		PlatformUI.getWorkbench().getHelpSystem().setHelp(buttonSelectDeselect,
				HelpContextIDs.CRASH_ANALYSER_HELP_IMPORT_CRASH_FILES);		
	}
}
