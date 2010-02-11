/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class EpocReader
 *
 */
package com.nokia.s60tools.analyzetool.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;
import com.nokia.s60tools.analyzetool.ui.MainView;

/**
 * Creates new job for polling epocwind.out file
 * @author kihe
 *
 */
public class EpocReader extends Job{

	/** Epocwind.out file location*/
	private final String emulatorFileLocation;

	/**Is file listening active*/
	boolean alreadyStarted = false;

	/**Interval to read file contents*/
	static final int POLL_INTERVAL = 1000; // 1 second

	/**Epocwind.out file size*/
	long fileSize = -1;

	/**
	 * How many characters we have read
	 * We can assume that long is big enough to store character count
	 * because max size of long is 9223372036854775807l
	 */
	long readCharacters = 0;

	/**Project reference*/
	IProject project;

	/**Parent class reference*/
	MainView main;

	/**Flag to determinate do we still need to read changes from epocwind.out file*/
	boolean stillRun = true;

	/**
	 * Constructor
	 * @param projectRef Project reference
	 * @param parentClass Parent class reference
	 */
	public EpocReader(IProject projectRef, MainView parentClass)
	{
		super(Constants.OUTPUT_READER_TITLE);
		emulatorFileLocation = System.getenv("TEMP") + File.separator+ "epocwind.out";
		project = projectRef;
		main = parentClass;

	}

	/**
	 * Starts epocwind.out file reading
	 */
	public void start(){

		//if we are already listening epocwindout file => no need create new job
		if( alreadyStarted ) {
			return;
		}

		setPriority(Job.LONG);
		setUser(false);

		//start as soon as possible
		schedule();

		//update listening flag
		alreadyStarted = true;

		//open streams
		main.parser.openStreams(Util.getBldInfFolder(
				project, true));
	}


	/**
	 * Stops listening emulator
	 */
	public void stop() {
		alreadyStarted = false;
		fileSize = -1;
		readCharacters = 0;
		stillRun = false;
		cancel();

	}


	/**
	 * Reads the epocwind.out file if it has been changed since last time
	 */
	@Override
	protected IStatus run(IProgressMonitor arg0) {

		//stream which are used to read file content
		FileInputStream fis = null;
		BufferedReader br = null;

		//run while user stops the data capturing
		while(stillRun) {
			try {
				//create new file
				File epocFile = new File(emulatorFileLocation);

				//if file exists
				if (epocFile.exists()) {

					// emulator output is not read
					// so we need to thru existing data of emulator output,
					// because we are only interested of new data.
					if (fileSize == -1) {
						fileSize = epocFile.length();

						//open epocwind.out file
						fis = new FileInputStream(epocFile);
						br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

						//skip lines what we have already read
						br.skip(readCharacters);

						String line = "";
						// read all new lines of epocwind.out file
						while ((line = br.readLine()) != null) {
							//update read count
							readCharacters+=line.length()+2; //<== line feed character
						}
					// emulator output contains new/more information than what we are read
					} else if (epocFile.length() > fileSize) {

						//open epocwind.out file
						fis = new FileInputStream(epocFile);
						br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

						//skip lines what we have already read
						br.skip(readCharacters);

						String line = "";
						// read all new lines in epocwind.out
						while ((line = br.readLine()) != null) {
							//update read count
							readCharacters+=line.length()+2; //<== line feed character

							//transfer line to parser
							main.parser.parse(line);

						}

						//update size
						fileSize = epocFile.length();

						//tell AT UI to update online allocation count value
						main.updateAllocNumber();
					}else if (epocFile.length() < fileSize){
						fileSize = 0;
						readCharacters = 0;
					}
				}
				if( br != null ) {
					br.close();
					br = null;
				}

				if( fis != null ) {
					fis.close();
					fis = null;
				}

				//sleep this thread
				Thread.sleep(POLL_INTERVAL);
			} catch (InterruptedException ie) {
				//we can shallow the exception
				//because when this exception is raised we can exit
				cancel();
			} catch (Exception e) {
				e.printStackTrace();
				cancel();
			}
			finally{
				try{
					if(br != null ) {
						br.close();
					}
				}catch(IOException ioe ) {
					ioe.printStackTrace();
				}

				try{
					if(fis != null ) {
						fis.close();
					}
				}catch(IOException ioe ) {
					ioe.printStackTrace();
				}
			}
		}

		//parsing is finished
		return Status.OK_STATUS;
	}
}
