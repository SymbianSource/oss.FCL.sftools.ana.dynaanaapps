/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Import page
 *
 */
package com.nokia.traceviewer.engine.preferences.wizard;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

/**
 * Page in import wizard
 * 
 */
public class ImportPage extends AbstractImportExportPage {

	/**
	 * File
	 */
	private File file;

	/**
	 * Filter extensions
	 */
	static final String[] FILTER_EXTS = { "*.xml", //$NON-NLS-1$
			"*.*" //$NON-NLS-1$
	};

	/**
	 * Filter extension names
	 */
	static final String[] FILTER_EXT_NAMES = {
			Messages.getString("ImportPage.ConfigurationFileName"), //$NON-NLS-1$
			Messages.getString("ImportPage.AllFilesName") }; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	protected ImportPage() {
		super("ImportPage"); //$NON-NLS-1$
		setTitle(Messages.getString("ImportPage.PageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("ImportPage.PageDescription")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.preferences.wizard.AbstractImportExportPage
	 * #createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite composite = (Composite) getControl();

		// Create browse group
		createBrowseGroup(composite, Messages
				.getString("ImportPage.BrowseGroupText")); //$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
				fileDialog.setText(Messages
						.getString("ImportPage.FileDialogText")); //$NON-NLS-1$
				fileDialog.setFilterExtensions(FILTER_EXTS);
				fileDialog.setFilterNames(FILTER_EXT_NAMES);
				String pathstr = fileDialog.open();
				if (pathstr != null) {
					pathText.setText(pathstr);
				}
			}
		});

		setPageComplete(validatePage(true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.preferences.wizard.AbstractImportExportPage
	 * #validatePage(boolean)
	 */
	@Override
	protected boolean validatePage(boolean validateFile) {
		setErrorMessage(null);
		boolean valid = !validateFile;
		if (validateFile) {
			IPath path = new Path(pathText.getText());
			file = path.toFile();
			if (file.exists()) {
				valid = true;
			}
		}
		return valid;
	}

	/**
	 * Get file
	 * 
	 * @return given file
	 */
	public File getFile() {
		return file;
	}
}
