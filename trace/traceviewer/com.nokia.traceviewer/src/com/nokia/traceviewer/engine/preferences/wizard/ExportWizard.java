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
 * Export wizard
 *
 */
package com.nokia.traceviewer.engine.preferences.wizard;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.nokia.traceviewer.engine.TraceViewerGlobals;
import com.nokia.traceviewer.engine.preferences.XMLColorConfigurationExporter;
import com.nokia.traceviewer.engine.preferences.XMLFilterConfigurationExporter;
import com.nokia.traceviewer.engine.preferences.XMLLineCountConfigurationExporter;
import com.nokia.traceviewer.engine.preferences.XMLTriggerConfigurationExporter;
import com.nokia.traceviewer.engine.preferences.XMLVariableTracingConfigurationExporter;

/**
 * Wizard for exporting TraceViewer configurations to a file
 */
public class ExportWizard extends Wizard implements IExportWizard {

	/**
	 * Export page
	 */
	private ExportPage exportPage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		boolean success = false;
		File file = exportPage.getFile();
		if (file != null) {
			String fn = file.getAbsolutePath();

			// Export Filter rules to XML file
			XMLFilterConfigurationExporter filterExporter = new XMLFilterConfigurationExporter(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getFilterProcessor()
							.getRoot(), fn, false);
			filterExporter.export();

			// Export Color rules to XML file
			XMLColorConfigurationExporter colorExporter = new XMLColorConfigurationExporter(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getColorer().getRoot(),
					fn, false);
			colorExporter.export();

			// Export LineCount rules to XML file
			XMLLineCountConfigurationExporter countExporter = new XMLLineCountConfigurationExporter(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getLineCountProcessor()
							.getRoot(), fn, false);
			countExporter.export();

			// Export Variable Tracing rules to XML file
			XMLVariableTracingConfigurationExporter var = new XMLVariableTracingConfigurationExporter(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess()
							.getVariableTracingProcessor().getRoot(), fn, false);
			var.export();

			// Export Trigger rules to XML file
			XMLTriggerConfigurationExporter triggerExporter = new XMLTriggerConfigurationExporter(
					TraceViewerGlobals.getTraceViewer()
							.getDataProcessorAccess().getTriggerProcessor()
							.getRoot(), fn, false);
			triggerExporter.export();

			// Inform about export succesful
			String exported = Messages.getString("ExportWizard.Emsg"); //$NON-NLS-1$
			TraceViewerGlobals.getTraceViewer().getDialogs()
					.showInformationMessage(exported);
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
		setWindowTitle(Messages.getString("ExportWizard.ExportTitle")); //$NON-NLS-1$
		exportPage = new ExportPage();
		addPage(exportPage);
	}

}
