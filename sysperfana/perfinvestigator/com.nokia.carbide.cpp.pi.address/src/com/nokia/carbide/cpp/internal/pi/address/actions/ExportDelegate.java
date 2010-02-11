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

package com.nokia.carbide.cpp.internal.pi.address.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.utils.PIUtilities;
import com.nokia.carbide.cpp.pi.address.AddressPlugin;
import com.nokia.carbide.cpp.pi.address.GppTrace;
import com.nokia.carbide.cpp.pi.address.GppTraceCsvPrinter;


public class ExportDelegate implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	
	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		// get the active page
		if (window == null)
			return;
		
		ParsedTraceData ptd = TraceDataRepository.getInstance().getTrace(NpiInstanceRepository.getInstance().activeUid(),
								AddressPlugin.getDefault().getTraceClass());
		if (ptd != null)
		{
			final GppTrace gppTraceTmp = (GppTrace)ptd.traceData;
							
			new Thread()
			{
				public void run()
				{   
				    try 
					{
						File report = PIUtilities.getAFile(true,"csv"); //$NON-NLS-1$
						if (report != null)
						{
						    GppTraceCsvPrinter csvPrinter = new GppTraceCsvPrinter(gppTraceTmp);
						    String csvPrint = csvPrinter.getCsvPrint();
						    if (csvPrint != null)
						    {
						        PIUtilities.saveCsvPrint(report, csvPrint);
						    }
						}
					} 
					catch (Exception e) 
					{
					    e.printStackTrace();
					}
				}
			}.start();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
