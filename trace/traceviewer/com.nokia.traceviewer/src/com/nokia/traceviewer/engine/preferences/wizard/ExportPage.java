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
 * Export page
 *
 */
package com.nokia.traceviewer.engine.preferences.wizard;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

import java.io.File;

/**
 * Page in export wizard
 * 
 */
public class ExportPage extends AbstractImportExportPage {

	/**
	 * File
	 */
	private File file;

	/**
	 * Save as parent
	 */
	private static String saveAsParent;

	/**
	 * Constructor
	 */
	protected ExportPage() {
		super("ExportPage"); //$NON-NLS-1$
		setTitle(Messages.getString("ExportPage.PageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("ExportPage.PageDescription")); //$NON-NLS-1$
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
				.getString("ExportPage.BrowseGroupText")); //$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setText(Messages.getString("ExportPage.FileDialogText")); //$NON-NLS-1$
				if (saveAsParent == null)
					saveAsParent = System.getProperty("user.home"); //$NON-NLS-1$
				dialog.setFilterPath(saveAsParent);
				dialog.setFileName("TVConfigurations.xml"); //$NON-NLS-1$
				String path = dialog.open();
				if (path != null) {
					IPath saveAsPath = new Path(path);
					saveAsParent = saveAsPath.removeLastSegments(1).toString();
					pathText.setText(saveAsPath.toOSString());
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
			valid = file.isAbsolute();
		}

		if (!valid) {
			setErrorMessage(Messages.getString("ExportPage.NoFileSelectedMsg")); //$NON-NLS-1$
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
