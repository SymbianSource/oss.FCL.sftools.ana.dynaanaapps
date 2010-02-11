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

package com.nokia.s60tools.crashanalyser.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.nokia.s60tools.crashanalyser.ui.views.MainView;
import com.nokia.s60tools.crashanalyser.files.PanicFile;

/**
 * This class listens emulator for panics. 
 *
 */
public class EmulatorListener extends Job {

	static final int POLL_INTERVAL = 10000; // 10 seconds
	boolean listening = false;
	String epocWindOutPath = "";
	long previousFileSize = -1;
	int readLines = 0;
	Pattern panicParserPattern = Pattern.compile("^\\s*(\\S*)\\s*Thread\\s*(\\S*)\\s*Panic\\s*(\\S*)\\s*(\\S*).*");
	
	/**
	 * Constructor
	 */
	public EmulatorListener() {
		super("Crash Analyser - Emulator Listener");
		epocWindOutPath = FileOperations.addSlashToEnd(System.getenv("TEMP"));
		epocWindOutPath += "epocwind.out";
	}
	
	/**
	 * Starts listening emulator
	 */
	public void start() {
		if (listening)
			return;		
		listening = true;
		setPriority(Job.LONG);
		setSystem(true);
		setUser(false);
		schedule();		
	}
	
	/**
	 * Stops listening emulator
	 */
	public void stop() {
		listening = false;
		previousFileSize = -1;
		readLines = 0;
		cancel();
	}
	
	void reSchedule() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				cancel();
				schedule(POLL_INTERVAL);
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);        		
	}
	
	@Override
	protected IStatus run(IProgressMonitor arg0) {
		try {
			boolean filesAdded = false;
			File epocwind = new File(epocWindOutPath);
			if (epocwind.exists()) {
				// epocwind.out has not yet been read
				if (previousFileSize == -1) {
					previousFileSize = epocwind.length();
				// epocwind.out has changes
				} else if (epocwind.length() > previousFileSize) {
					FileInputStream fis = new FileInputStream(epocwind);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					br.skip(readLines);
					
					String line = "";
					// read all new lines in epocwind.out
					while ((line = br.readLine()) != null) {
						readLines++;
						if (handleLine(line))
							filesAdded = true;
					}
					previousFileSize = epocwind.length();
				// new epocwind.out file
				} else if (epocwind.length() < previousFileSize){
					previousFileSize = 0;
					readLines = 0;
				}
				
				// if panics were found, update main view
				if (filesAdded)
					MainView.showOrRefresh();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		reSchedule();
		return Status.OK_STATUS;
	}
	
	/**
	 * If line contains a panic, writes it to xml file
	 * @param line epocwind.out file line
	 * @return true if line contained a panic, false if not
	 */
	boolean handleLine(String line) {
		Matcher matcher = panicParserPattern.matcher(line);
		if (matcher.find()) {
			PanicFile.WritePanicFile(matcher.group(1),
									 matcher.group(2),
									 matcher.group(3),
									 matcher.group(4));
			return true;
		}	
		return false;
	}
	
}
