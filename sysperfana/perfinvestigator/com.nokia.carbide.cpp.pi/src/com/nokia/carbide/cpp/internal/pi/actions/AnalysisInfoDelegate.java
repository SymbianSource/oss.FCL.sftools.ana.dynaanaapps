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

package com.nokia.carbide.cpp.internal.pi.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.test.AnalysisInfoHandler;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;


public class AnalysisInfoDelegate implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	
	public void dispose() {
		window = null;
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		// get the active page
		if (window == null)
			return;

		AnalysisInfoHandler analysisInfoHandler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();
		
		if (analysisInfoHandler != null)
		{
			Shell shell = new Shell(PIPageEditor.currentPageEditor().getEditorSite().getShell().getDisplay());
			analysisInfoHandler.getAnalysisInfoTable(shell);
			shell.setLayout(new FillLayout());
			shell.pack();
			shell.open();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
