/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class PropertiesPage
 *
 */

package com.nokia.s60tools.analyzetool.properties;

import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PropertyPage;

import com.nokia.s60tools.analyzetool.global.Constants;

/**
 * Implements AnalyzeTool properties page
 * 
 * @author kihe
 * 
 */
@SuppressWarnings("restriction")
public class PropertiesPage extends PropertyPage implements Listener {
	/** Add button to open file selection dialog. */
	private Button addSymbolFile;

	/** Remove button to remove items from the list. */
	private Button removeSymbolFile;

	/** Rom symbol file. */
	private List romSymbolDirText;

	QualifiedName romSymbols;

	QualifiedName useRom;
	/**
	 * Button to use default rom symbol file.
	 */
	private Button useRomSymbol;

	/**
	 * Constructor.
	 */
	public PropertiesPage() {
		super();
		useRom = new QualifiedName(Constants.USE_ROM_SYMBOL, Constants.USE_ROM);
		romSymbols = new QualifiedName(Constants.USE_ROM_SYMBOL_LOCATION,
				Constants.ROM_LOC);
	}

	/**
	 * Checks that is selected symbol already in the list. If symbol file is not
	 * added => add it to list
	 * 
	 * @param selectedFile
	 *            Select symbol file name and location
	 */
	private void checkGivenSymbolFile(String selectedFile) {
		String[] items = romSymbolDirText.getItems();
		boolean found = false;
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(selectedFile)) {
				found = true;
				break;
			}
		}
		if (!found) {
			romSymbolDirText.add(selectedFile);
		}
	}

	/**
	 * Updates this preference page values to match stored values
	 */
	private final void checkInitValues() {
		try {
			IProject project = getProject();
			if (project == null) {
				useRomSymbol.setEnabled(false);
				romSymbolDirText.setEnabled(false);
				return;
			}

			String useRomSymbolProperty = project.getPersistentProperty(useRom);
			if (useRomSymbolProperty == null
					|| useRomSymbolProperty.equalsIgnoreCase("false")) {
				useRomSymbol.setSelection(false);
			} else {
				useRomSymbol.setSelection(true);
			}

			// get defined rom symbol(s)
			// if user has definend more than one rom symbol
			// parse those symbol files and add to list
			// each symbol file name is divided by ";" char
			// e.g. c:\temp\symbol.symbol;c:\temp\secondsymbol.symbol
			String symbolLoc = project.getPersistentProperty(romSymbols);
			if (symbolLoc != null && !("").equals(symbolLoc)) {
				String[] split = symbolLoc.split(";");
				if (split != null) {
					for (int i = 0; i < split.length; i++) {
						romSymbolDirText.add(split[i]);
					}
				}
			}
			handleRomSymbolState();
		} catch (CoreException ce) {
			ce.printStackTrace();
		}

	}

	/**
	 * Creates this preference page content.
	 * 
	 * @param parent
	 *            This preference page parent
	 */
	@Override
	protected final Control createContents(final Composite parent) {

		// create new composite
		final Composite composite = new Composite(parent, SWT.TOP);

		// create griddata for view
		final GridData gridData = new GridData();
		composite.setLayoutData(gridData);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 1;

		// set layoyt of this view
		composite.setLayout(gridLayout);

		// new group for symbol file definition
		final Group groupUseRomSymbol = new Group(composite, SWT.NONE);
		groupUseRomSymbol.setText(Constants.PREFS_ADVANCED);

		// layout for useRomSymbol button
		final GridLayout layoutSymbol = new GridLayout();
		layoutSymbol.numColumns = 1;
		groupUseRomSymbol.setLayout(layoutSymbol);

		// checkbox item to enable rom symbol selection
		useRomSymbol = new Button(groupUseRomSymbol, SWT.CHECK);
		useRomSymbol.setText(Constants.PREFS_USE_ROM_SYMBOL);
		useRomSymbol.addListener(SWT.Selection, this);

		// grid data for use useRomSymbol
		final GridData gridDataAtool = new GridData();
		gridDataAtool.horizontalSpan = 3;
		groupUseRomSymbol.setLayoutData(gridDataAtool);

		// new composite for rom symbol location
		final Composite romSymbolLocation = new Composite(groupUseRomSymbol,
				SWT.TOP);

		// romSymbolLocation layout
		final GridLayout dirSettingsLayout = new GridLayout();
		dirSettingsLayout.numColumns = 2;
		romSymbolLocation.setLayout(dirSettingsLayout);

		// grid data for romSymbolLocation
		final GridData dirGridData = new GridData();
		dirGridData.horizontalSpan = 3;
		romSymbolLocation.setLayoutData(dirGridData);

		// new composite for symbol file list
		Composite listComposite = new Composite(romSymbolLocation, SWT.NULL);
		GridLayout listCompLayout = new GridLayout();
		listCompLayout.numColumns = 1;
		listComposite.setLayout(listCompLayout);

		GridData listData = new GridData(250, SWT.DEFAULT);
		listData.minimumHeight = 350;
		listData.heightHint = 150;
		romSymbolDirText = new List(listComposite, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		romSymbolDirText.setLayoutData(listData);

		Composite buttonComposite = new Composite(romSymbolLocation, SWT.SIMPLE);
		GridLayout buttonLayoyt = new GridLayout();
		buttonLayoyt.numColumns = 1;
		buttonComposite.setLayout(buttonLayoyt);

		GridData buttonGridData = new GridData();
		buttonGridData.horizontalAlignment = SWT.FILL;
		buttonGridData.grabExcessVerticalSpace = true;

		// button which opens the folder selection dialog
		addSymbolFile = new Button(buttonComposite, SWT.NONE);
		addSymbolFile.setToolTipText(Constants.PREFS_SELECT_ROM_SYMBOL);
		addSymbolFile.setText("Add");
		addSymbolFile.setLayoutData(buttonGridData);
		addSymbolFile.addListener(SWT.Selection, this);

		removeSymbolFile = new Button(buttonComposite, SWT.NONE);
		removeSymbolFile.setToolTipText(Constants.PREFS_SELECT_ROM_SYMBOL);
		removeSymbolFile.setText("Remove");
		removeSymbolFile.setLayoutData(buttonGridData);
		removeSymbolFile.addListener(SWT.Selection, this);

		// UI elements are created => adjust right values
		checkInitValues();
		return composite;
	}

	/**
	 * Returns selected project reference
	 * 
	 * @return Project reference
	 */
	private IProject getProject() {
		// get project
		
		IAdaptable adap = super.getElement();
		
		IProject project = null;
		if( adap instanceof IProject ) {
			project = (IProject)adap;
		}
		else if( adap instanceof CProject ) {
			CProject cproj = (CProject)adap;
			project = cproj.getProject();
		}
			
		if (project != null && project.exists() && project.isOpen()) {
			return project;
		}
		return null;
	}

	/**
	 * Handles events.
	 * 
	 * @param event
	 *            Preference page event
	 */
	public final void handleEvent(final Event event) {
		if (event.widget == addSymbolFile) {
			openFolderDialog();
		} else if (event.widget == useRomSymbol) {
			handleRomSymbolState();
		} else if (event.widget == removeSymbolFile) {
			romSymbolDirText.remove(romSymbolDirText.getSelectionIndex());
		}
	}

	/**
	 * Updates rom symbol file selection state
	 */
	private final void handleRomSymbolState() {
		if (useRomSymbol.getSelection()) {
			romSymbolDirText.setEnabled(true);
			addSymbolFile.setEnabled(true);
			removeSymbolFile.setEnabled(true);
		} else {
			romSymbolDirText.setEnabled(false);
			addSymbolFile.setEnabled(false);
			removeSymbolFile.setEnabled(false);
		}
	}

	/**
	 * Opens file selection dialog.
	 */
	private final void openFolderDialog() {
		final FileDialog fileDialog = new FileDialog(romSymbolDirText
				.getShell(), SWT.MULTI);
		fileDialog.setText(Constants.PREFS_SELECT_ROM_SYMBOL);
		String[] extensions = { "*.symbol", "*.*" };
		fileDialog.setFilterExtensions(extensions);
		final String fileLocation = fileDialog.open();
		String[] fileNames = fileDialog.getFileNames();

		// if user has pressed "cancel"
		if (fileLocation == null || ("").equals(fileLocation)) {
			return;
		}
		// if user has selected only one file
		else if (fileNames != null && fileNames.length == 1) {
			checkGivenSymbolFile(fileLocation);
		}
		// user has selected multiple file
		else if (fileNames != null && fileNames.length > 1) {

			// get selected folder.
			String folder = fileDialog.getFilterPath();

			// go thru selected items
			for (int i = 0; i < fileNames.length; i++) {
				// get one selected file
				String fileName = fileNames[i];

				// check selected file
				checkGivenSymbolFile(folder + "\\" + fileName);
			}
		}
	}

	/**
	 * Stores selected values When user press "Ok" or "apply" button this method
	 * is called
	 */
	@Override
	public final boolean performOk() {
		try {

			// get selected project
			IProject project = getProject();
			if (project == null) {
				return false;
			}

			// get useRomSymbel selection
			if (useRomSymbol.getSelection()) {
				project.setPersistentProperty(useRom, "true");

			} else {
				project.setPersistentProperty(useRom, "false");
			}

			// store user defined rom symbols
			// divided by ";" char
			StringBuffer symbolFiles = new StringBuffer();
			String[] items = romSymbolDirText.getItems();
			for (int i = 0; i < items.length; i++) {
				symbolFiles.append(items[i]);
				symbolFiles.append(';');
			}

			// store symbol files
			project.setPersistentProperty(romSymbols, symbolFiles.toString());
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
		return super.performOk();
	}

}