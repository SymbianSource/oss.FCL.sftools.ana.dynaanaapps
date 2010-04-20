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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.ui.PlatformUI;
import com.nokia.s60tools.ui.wizards.S60ToolsWizardPage;
import com.nokia.s60tools.crashanalyser.export.ICrashFileProvider;
import com.nokia.s60tools.crashanalyser.files.CrashAnalyserFile;
import com.nokia.s60tools.crashanalyser.model.*;
import com.nokia.s60tools.crashanalyser.resources.*;
import com.nokia.s60tools.crashanalyser.data.*;
import java.io.*;

/**
 * This 1st Wizard page queries the user for a crash file or folder from which Crash 
 * files are found. 
 *
 */
public class FileOrPathSelectionPage extends S60ToolsWizardPage implements SelectionListener,
																			ModifyListener,
																			DropTargetListener {

	Combo comboFileOrPath;
	Button buttonBrowseFolder;
	Button buttonBrowseFile;
	Button buttonReadFilesFromDevice;
	Label labelComboTitle;
	Label labelOr;
	DecoderEngine engine;
	ErrorLibrary errorLibrary;
	String providedFileOrFolder;
	CrashAnalyserWizard parentWizard;
	ICrashFileProvider fileProvider = null;
	
	/**
	 * Constructor
	 * @param decEng decoder engine 
	 * @param library error library
	 */
	public FileOrPathSelectionPage(DecoderEngine decEng, 
									ErrorLibrary library,
									String prefilledFileOrFolder,
									CrashAnalyserWizard parent,
									ICrashFileProvider crashFileProvider) {
		super("");
			
		setTitle("Crash Files Selection");
			
		setDescription("MobileCrash File (*.bin), D_EXC File (*.txt), Decoded File (*.crashxml), ELF Core Dump File (*.elf)");
		
		engine = decEng;
		errorLibrary = library;
		providedFileOrFolder = prefilledFileOrFolder;
		parentWizard = parent;
		fileProvider = crashFileProvider;

		// User cannot finish the page before some valid 
		// selection is made.
		setPageComplete(false);
		
	 }
	
	
	@Override
	public void recalculateButtonStates() {
		// no implementation needed
	}

	@Override
	public void setInitialFocus() {
		comboFileOrPath.setFocus();
	}
	

	/**
	 * Creates all UI controls
	 */
	public void createControl(Composite parent) {
		Composite composite =  new Composite(parent, SWT.NULL);
		
	    // create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		composite.setLayout(gl);
		
		GridData titleGD = new GridData(GridData.FILL_HORIZONTAL);
		titleGD.horizontalSpan = 3;

		// Combo title
		labelComboTitle = new Label(composite, SWT.LEFT);
		labelComboTitle.setText("A Crash File or a Folder Containing Crash Files:");
		titleGD.horizontalSpan = 3;
		labelComboTitle.setLayoutData(titleGD);
		
		// MobileCrash path combo
		comboFileOrPath = new Combo(composite, SWT.BORDER);
		GridData comboGD = new GridData(GridData.FILL_HORIZONTAL);
		comboFileOrPath.setLayoutData(comboGD);
		comboFileOrPath.addModifyListener(this);
		

		// Restore previous values to MobileCrash path combo
		UserEnteredData data = new UserEnteredData();
		String[] lastUsed = data.getPreviousValues(UserEnteredData.ValueTypes.CRASH_FILE_OR_PATH);
		if (lastUsed != null) {
			comboFileOrPath.setItems(lastUsed);
			comboFileOrPath.select(0);
		}
		
		// Browse folder button
		buttonBrowseFolder = new Button(composite, SWT.PUSH);
		buttonBrowseFolder.setText("Folder...");
		buttonBrowseFolder.addSelectionListener(this);

		// Browse file button
		buttonBrowseFile = new Button(composite, SWT.PUSH);
		buttonBrowseFile.setText("File...");
		buttonBrowseFile.addSelectionListener(this);
		
		// OR label
		labelOr = new Label(composite, SWT.LEFT);
		labelOr.setText("OR");
		labelOr.setLayoutData(titleGD);
		if (fileProvider == null)
			labelOr.setVisible(false);
		
		// Read Files from Device button
		buttonReadFilesFromDevice = new Button(composite, SWT.PUSH);
		if (fileProvider == null) {
			buttonReadFilesFromDevice.setVisible(false);
		} else {
			buttonReadFilesFromDevice.setText(fileProvider.getFunctionalityName());
			buttonReadFilesFromDevice.setToolTipText(fileProvider.getFunctionalityDescription());
		}
		buttonReadFilesFromDevice.addSelectionListener(this);
		
		setHelps();

		setInitialFocus();
		
		setControl(composite);
		
		if (!"".equals(providedFileOrFolder)) {
			comboFileOrPath.setText(providedFileOrFolder);
		}
		
		Transfer[] types = new Transfer[] { FileTransfer.getInstance() };
	    DropTarget dropTarget = new DropTarget(composite, DND.DROP_COPY);
	    dropTarget.setTransfer(types);
	    dropTarget.addDropListener(this);		
	}
	
	
	public void widgetDefaultSelected(SelectionEvent e)	{
		// no implementation needed
	}
	
	public void widgetSelected(SelectionEvent e) {
		
		// browse directory button
		if (e.widget == buttonBrowseFolder) {
			// open directory dialog for selecting directory which contains crash files
			DirectoryDialog dialog = new DirectoryDialog(this.getShell());
			dialog.setText("Select crash files path");
			String result = dialog.open();
			
			if (result != null)
				comboFileOrPath.setText(result);
			
		// browse file button
		} else if (e.widget == buttonBrowseFile){
			// open file dialog for selecting a crash file
			FileDialog dialog = new FileDialog(this.getShell());
			dialog.setText("Select a crash file");
			String[] filterExt = { "*."+CrashAnalyserFile.MOBILECRASH_FILE_EXTENSION, 
									"*."+CrashAnalyserFile.OUTPUT_FILE_EXTENSION,
									"*."+CrashAnalyserFile.D_EXC_FILE_EXTENSION,
									"*."+CrashAnalyserFile.ELF_CORE_DUMP_FILE_EXTENSION};
	        dialog.setFilterExtensions(filterExt);			
			String result = dialog.open();
			
			if(result != null)
				comboFileOrPath.setText(result);
			
		// Read Files from Device button
		} else if (e.widget == buttonReadFilesFromDevice) {
			parentWizard.moveToSecondPageRequest(true);
		}
	}
	
	public void modifyText(ModifyEvent event) {
		if(event.widget.equals(comboFileOrPath)){
			try {
				getWizard().getContainer().updateButtons();
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * Checks if user has selected a valid directory or file
	 */
	public boolean canFlipToNextPage() {
		try {
			// nothing is selected
			if (comboFileOrPath.getText().length() <= 0) {
				this.setErrorMessage(null);
				return false;
			}
			
			File file = new File(comboFileOrPath.getText());
			
			// user has selected a directory
			if (file.isDirectory() && file.exists()) {
				// if selected path has crash files, we can proceed to next page
				if (engine.isPathValid(comboFileOrPath.getText(), DecoderEngine.PathTypes.CRASH)) {
					this.setErrorMessage(null);
					return true;
				}
				// selected path does not contain crash files, show error
				else {
					this.setErrorMessage("Selected path does not contain any crash files");
					return false;
				}
			// user has selected a single file
			} else if (file.isFile() && file.exists()) {
					this.setErrorMessage(null);
					return true;
			// user has not selected a folder nor a file
			} else {
				this.setErrorMessage("Invalid file or folder.");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	
	/**
	 * Returns the user provided crash file directory if it is a directory
	 * @return the user provided crash file directory if it is a directory, otherwise ""
	 */
	public String getSelectedFolder() {
		if (comboFileOrPath.getText().length() <= 0) {
			return "";
		}
		
		File file = new File(comboFileOrPath.getText());
		
		// user has selected a directory
		if (file.isDirectory() && file.exists()) {
			return comboFileOrPath.getText();
		}
		
		return "";
	}
	
	/**
	 * Returns the user provided crash file or crash file directory
	 * @return the user provided crash file or crash file directory
	 */
	public String getSelectedFileOrFolder() {
		return comboFileOrPath.getText();
	}
	
	/**
	 * When next press has been processed, this should be called to inform
	 * if any errors were found during next press processing
	 * @param error
	 */
	public void errorInProcessingNextPress(boolean error) {
		if (error)
			this.setErrorMessage("Could not read files");
		else
			this.setErrorMessage(null);
	}
	
	/**
	 * Saves current combo value so that it will be the default value
	 * next time this wizard is shown.
	 */
	public void saveUserEnteredData() {
		UserEnteredData userData = new UserEnteredData();
		userData.saveValue(UserEnteredData.ValueTypes.CRASH_FILE_OR_PATH, comboFileOrPath.getText());			
	}
	
	/**
	 * Sets this page's context sensitive helps
	 *
	 */
	protected void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comboFileOrPath,
				HelpContextIDs.CRASH_ANALYSER_HELP_IMPORT_CRASH_FILES);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(buttonBrowseFolder,
				HelpContextIDs.CRASH_ANALYSER_HELP_IMPORT_CRASH_FILES);		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(buttonBrowseFile,
				HelpContextIDs.CRASH_ANALYSER_HELP_IMPORT_CRASH_FILES);		
	}
	
	/**
	 * Executes drop functionality when files are drag&dropped to wizard page.
	 * Checks whether dropped files are supported and starts "presses next automatically"
	 * @param files drag&dropped files (paths)
	 */
	void executeDrop(String[] files) {
		// just one (supported) file dropped
		if (files.length == 1 && DecoderEngine.isFileValidCrashFile(files[0])) {
			comboFileOrPath.setText(files[0]);
			parentWizard.moveToSecondPageRequest(files);
		// multiple files dropped
		} else if (files.length > 1) {
			String path = "";
			// go through all dropped files, and check that they are from same folder
			// (wizard doesn't know how to handle multiple files from multiple locations)
			for (int i = 0; i < files.length; i++) {
				if (DecoderEngine.isFileValidCrashFile(files[i])) {
					if ("".equals(path)) {
						path = FileOperations.getFolder(files[i]);
					} else if (!FileOperations.getFolder(files[i]).equalsIgnoreCase(path)){
						showMessage("Multiple files from different folders are not supported");
						break;
					}
				} else {
					showMessage("Unsupported file type");
					break;
				}
			}
			comboFileOrPath.setText(path);
			parentWizard.moveToSecondPageRequest(files);
		} else {
			showMessage("Unsupported file type");
		}
	}
	
	/**
	 * Shows a message box with given message
	 * @param message
	 */
	private void showMessage(String message) {
		MessageDialog.openInformation(
			comboFileOrPath.getShell(),
			"Crash Analyser",
			message);
	}
	
	public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}

	public void dragLeave(DropTargetEvent event) {
		// nothing to be done
	}

	public void dragOperationChanged(DropTargetEvent event) {
		// nothing to be done
	}

	public void dragOver(DropTargetEvent event) {
		event.feedback = DND.FEEDBACK_NONE;
	}

	public void drop(DropTargetEvent event) {
		// we accept only file drops
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			if (event.data != null) {
				String[] files = (String[])event.data;
				executeDrop(files);
			}
		}
	}

	public void dropAccept(DropTargetEvent event) {
		// nothing to be done
	}
	
}
