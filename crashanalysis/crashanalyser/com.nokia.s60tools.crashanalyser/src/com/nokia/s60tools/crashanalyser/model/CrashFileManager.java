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

import java.io.*;
import java.util.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.swt.widgets.Display;
import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.interfaces.*;

/**
 * This class is responsible for providing crash files to MainView's content provider.
 * This class read workspace specific crash files from disk and prepares them for main view. 
 *
 */
public class CrashFileManager extends Job {
	List<CrashFileBundle> crashFiles = null;
	INewCrashFilesObserver filesObserver = null;
	ErrorLibrary errorLibrary = null;
	ILock accessLock = null;
	boolean restart = false;
	boolean jobRunning = false;
	
	/**
	 * Constructor
	 * @param observer observer for new files
	 */
	public CrashFileManager(INewCrashFilesObserver observer) {
		super("CrashAnalyser - Reading Files");
		filesObserver = observer;
		accessLock = Job.getJobManager().newLock();
		setPriority(Job.LONG);
		setUser(false);
	}
	
	protected IStatus run(IProgressMonitor monitor) {
		accessLock.acquire();
		try {
			jobRunning = true;
			// create array for crash files if it doesn't exist yet
			if (crashFiles == null) {
				crashFiles = new ArrayList<CrashFileBundle>();
			// array exists, remove non-existing files
			} else {
				
				// remove no longer existing files
				for (int i = 0; i < crashFiles.size(); i++) {
					CrashFileBundle file = crashFiles.get(i);
					if (!file.exists()) {
						crashFiles.remove(i);
						i--;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			accessLock.release();
		}
		
		// get crash files folder e.g. C:\My_Workspace\.metadata\.plugins\com.nokia.s60tools.crashanalyser\CrashFiles
		File cFilesFolder = new File(DecoderEngine.getCrashFilesFolder());
		if (cFilesFolder.exists() && cFilesFolder.isDirectory()) {
			// get crash file folders (CrashFiles are under numeric folders, e.g. ..\CrashFiles\1, ...\CrashFiles\2, etc.
			File[] folders = cFilesFolder.listFiles();
			if (folders != null) {
				// go through all folders
				for (int j = 0; j < folders.length; j++) {
					File cFolder = folders[j];
					if (cFolder.isDirectory()) {
						accessLock.acquire();
						try {
							// create dummy bundle for comparing purposes
							CrashFileBundle dummy = CrashFileBundle.createDummyBundle(cFolder.getAbsolutePath());
							// if array already contains this bundle, remove it and add it again, so that data is updated 
							if (crashFiles.contains(dummy)) {
								accessLock.release();
								CrashFileBundle bundle = new CrashFileBundle(cFolder.getAbsolutePath(), errorLibrary);
								if (bundle.hasFiles()) {
									accessLock.acquire();
									crashFiles.remove(dummy);
									crashFiles.add(bundle);
								}
							// array doesn't contain this bundle, add it
							} else {
								accessLock.release();
								CrashFileBundle bundle = new CrashFileBundle(cFolder.getAbsolutePath(), errorLibrary);
								if (bundle.hasFiles()) {
									accessLock.acquire();
									crashFiles.add(bundle);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							accessLock.release();
						}
					}
				}
			}
		}
		
		// notify observer to update it's view
		filesObserver.crashFilesUpdated();
		
		accessLock.acquire();
		// we have received new files while reading files, 
		// read files immediately again
		if (restart) {
			restart = false;
			accessLock.release();
			reSchedule();
		} else {
			jobRunning = false;
			accessLock.release();
		}
		
		return Status.OK_STATUS;
	}

	/**
	 * Schedules job asynchronously
	 */
	void reSchedule() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				schedule(100);
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);        		
	}
	
	/**
	 * Returns all read crash files. Can also return "No files"-bundle or "Waiting"-bundle.
	 * @param library error library
	 * @return list of crash files
	 */
	public CrashFileBundle[] getCrashFiles(ErrorLibrary library) {
		try {
			// do nothing until we get a valid ErrorLibrary, return "Waiting"-bundle
			if (library == null) {
				CrashFileBundle[] cFiles = new CrashFileBundle[1];
				cFiles[0] = new CrashFileBundle(false);
				return cFiles;
			}
			
			// crash files has not yet been read, start reading process,
			// and return "reading files"-bundle.
			if (crashFiles == null) {
				errorLibrary = library;
				reSchedule();
				CrashFileBundle[] cFiles = new CrashFileBundle[1];
				cFiles[0] = new CrashFileBundle(false);
				return cFiles;
			// files are up to date, return files
			} else {
				accessLock.acquire();
				try {
					// there are no files, return "No files"-bundle
					if (crashFiles.isEmpty()) {
						CrashFileBundle[] cFiles = new CrashFileBundle[1];
						cFiles[0] = new CrashFileBundle(true);
						return cFiles;
					// there are files, return them
					} else {
						return crashFiles.toArray(new CrashFileBundle[crashFiles.size()]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					accessLock.release();
				}
				
				// if something went wrong, return "No files"-bundle 
				CrashFileBundle[] cFiles = new CrashFileBundle[1];
				cFiles[0] = new CrashFileBundle(true);
				return cFiles;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Starts reading all files from files system
	 */
	public void refresh() {
		accessLock.acquire();
		try {
			// if we are currently reading files, 
			if (jobRunning) {
				restart = true;
			// we are not reading files at the moment, schedule job
			} else {
				if (errorLibrary != null) {
					reSchedule();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			accessLock.release();
		}
	}
}
