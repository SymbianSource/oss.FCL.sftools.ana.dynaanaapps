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
package com.nokia.s60tools.swmtanalyser.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.swmtanalyser.SwmtAnalyserPlugin;
import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.OverviewData;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;
import com.nokia.s60tools.swmtanalyser.editors.SWMTEditorInput;
import com.nokia.s60tools.swmtanalyser.exception.SwmtFormatException;
import com.nokia.s60tools.swmtanalyser.model.SWMTLogReaderUtils;
import com.nokia.s60tools.swmtanalyser.model.SwmtParser;
import com.nokia.s60tools.util.console.IConsolePrintUtility;

/**
 * Interface to start analysis.
 *
 */
public class SwmtAnalyser {

	private static final String SWMT_EDITOR_ID = "com.nokia.s60tools.swmtanalyser.editors.SWMTEditor";
	private ArrayList<String> inputs;
	private ArrayList<CycleData> cycleData;
	private String status = null;
	private boolean isCancelled;
	private SWMTLogReaderUtils logReader = new SWMTLogReaderUtils();
	private String parserError;
	
	/**
	 * Construction for creating SWMT Analyser to editor area.
	 * @param console
	 */
	public SwmtAnalyser(IConsolePrintUtility console) {
		SwmtAnalyserPlugin.getDefault().setConsole(console);
	}

	/**
	 * Analyse given logs files.
	 * @param swmtFiles list of swmt log files to be analysed
	 */
	public void analyse(ArrayList<String> swmtFilePaths)
	{
		if(swmtFilePaths != null)
		{
			inputs = swmtFilePaths;
			cycleData = new ArrayList<CycleData>();
			
			isCancelled = false;
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask("Reading log files..", 10);
					status = logReader.getCycleDataArrayFromLogFiles(inputs, cycleData, monitor);
					if(monitor.isCanceled())
						isCancelled = true;
					
					monitor.done();
				}
			};
			
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			
			Shell shell = win != null ? win.getShell() : null;
			try {
				new ProgressMonitorDialog(shell).run(true, true, op);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}			
			
			if(isCancelled)
				return;
			
			if(status != null)
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "SWMT Analyser", status);
				return;
			}
			else if(cycleData.size()>1)
			{
				cycleData = logReader.checkCycleOrder(cycleData);
				if(cycleData==null)
				{
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "SWMT Analyser", "Invalid order of the log files. The selected files must be in consecutive order and must start from the first cycle.");
					return;
				}
				
				if(!logReader.checkRomInfo(cycleData))
				{
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "SWMT Analyser", "Selected logs do not have common ROM Checksum and Version. Hence, they cannot be compared.");
					return;
				}
				int ret = logReader.checkTimeStamp(cycleData);
				if(ret!= 0)
				{
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "SWMT Analyser", "In selected logs, the time stamp of log cycle "+ ret +" is lesser than log cycle "+ (ret -1)+". Hence, they cannot be analysed together.");
					return;
				}
			}
			else if(cycleData.size() == 1 && cycleData.get(0).getCycleNumber() != 1)
			{
				boolean ok = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "SWMT Analyser", "This is a delta file. It does not contain the complete information.  Do you still want to continue ?");
				if(!ok)
					return;
			}
			
			//Files are valid and the cycle numbers are in order.
			//So, get overview information to be displayed in the editor view
			OverviewData ov = logReader.getOverviewInformationFromCyclesData(cycleData.toArray(new CycleData[0]),cycleData.size());
			
			Runnable runnable = new Runnable(){
				public void run() {
					for(int i=0; i<cycleData.size(); i++)
					{
						CycleData cycle = cycleData.get(i);
						cycle.clear();
						
						try {
							SwmtParser.parseSwmtLog(cycle.getFileName(), cycle);
						} catch (SwmtFormatException e) {
							parserError = e.getMessage();
							Runnable runnable = new Runnable(){
								public void run() {
									MessageDialog.openError(Display.getCurrent().getActiveShell(), "SWMT Analyser", "Error while parsing the log file " + parserError );
								}
							};
							Display.getDefault().asyncExec(runnable);
							return;
						}
					}
				}
			};
			Display.getDefault().syncExec(runnable);
			
			IWorkbenchPage page=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
			try 
			{ 
				IEditorDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().findEditor(SWMT_EDITOR_ID);
				if(descriptor == null)
				{
					MessageDialog.openError(Display.getDefault().getActiveShell(),"SWMT Analyser", "SWMT Editor is not found");
					return;
				}
				
				ParsedData logData = new ParsedData();
				logData.setParsedData(cycleData);
				page.openEditor(new SWMTEditorInput(logData,ov), descriptor.getId(), true,IWorkbenchPage.MATCH_INPUT);

			} catch (PartInitException e) { 
				e.printStackTrace(); 
			} 
		}
		else
		{
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "SWMT Analyser", "Invalid input. Unable to open SWMT Editor");
			return;
		}
	}
		
}
