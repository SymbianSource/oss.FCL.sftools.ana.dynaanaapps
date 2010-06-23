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
 * Import wizard
 *
 */
package com.nokia.traceviewer.engine.preferences.wizard;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.XMLColorConfigurationImporter;
import com.nokia.traceviewer.engine.preferences.XMLFilterConfigurationImporter;
import com.nokia.traceviewer.engine.preferences.XMLLineCountConfigurationImporter;
import com.nokia.traceviewer.engine.preferences.XMLTriggerConfigurationImporter;
import com.nokia.traceviewer.engine.preferences.XMLVariableTracingConfigurationImporter;

/**
 * Wizard for importing TraceViewer configurations from a file
 */
public class ImportWizard extends Wizard implements IImportWizard {

	/**
	 * Import page
	 */
	private ImportPage importPage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		boolean success = false;
		File file = importPage.getFile();
		if (file != null) {
			String fn = file.getAbsolutePath();

			// Import Filter rules
			XMLFilterConfigurationImporter filterImporter = new XMLFilterConfigurationImporter(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getFilterProcessor()
							.getRoot(), fn, false);
			filterImporter.importData();

			// Import Color rules
			XMLColorConfigurationImporter colorImporter = new XMLColorConfigurationImporter(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getColorer().getRoot(),
					fn, false);
			colorImporter.importData();

			// Import LineCount rules
			XMLLineCountConfigurationImporter countImporter = new XMLLineCountConfigurationImporter(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getLineCountProcessor()
							.getRoot(), fn, false);
			countImporter.importData();

			// Import Variable Tracing rules
			XMLVariableTracingConfigurationImporter var = new XMLVariableTracingConfigurationImporter(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess()
							.getVariableTracingProcessor().getRoot(), fn, false);
			var.importData();

			// Import Trigger rules
			XMLTriggerConfigurationImporter triggerImporter = new XMLTriggerConfigurationImporter(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getTriggerProcessor()
							.getRoot(), fn, false);
			triggerImporter.importData();

			// Inform about import succesful
			String imported = Messages.getString("ImportWizard.Imsg"); //$NON-NLS-1$
			TraceViewerGlobals.getTraceViewer().getDialogs()
					.showInformationMessage(imported);

			success = true;
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.getString("ImportWizard.ImportTitle")); //$NON-NLS-1$
		importPage = new ImportPage();
		addPage(importPage);
	}

}
