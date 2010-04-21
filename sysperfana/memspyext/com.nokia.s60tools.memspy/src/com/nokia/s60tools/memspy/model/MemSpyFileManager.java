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


package com.nokia.s60tools.memspy.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.nokia.s60tools.memspy.interfaces.INewMemSpyFilesObserver;


/**
 * This class is responsible for providing MemSpy files to MainView's content provider.
 * This class read workspace specific files from disk and prepares them for main view. 
 */
public class MemSpyFileManager extends Job {
	private List<MemSpyFileBundle> memSpyFiles = null;
	private INewMemSpyFilesObserver filesObserver = null;
	private ILock accessLock = null;
	private boolean restart = false;
	private boolean jobRunning = false;
	
	
	/**
	 * Constructor.
	 * @param observer, which is notified when reading is finished.
	 */
	public MemSpyFileManager( INewMemSpyFilesObserver observer ) {
		super("MemSpy - Reading Files");
		filesObserver = observer;
		memSpyFiles = null;
		accessLock = Job.getJobManager().newLock();
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		accessLock.acquire();
		try {
			jobRunning = true;
			if (memSpyFiles == null) {
				memSpyFiles = new ArrayList<MemSpyFileBundle>();
			} else {
				
				// remove no longer existing files
				for (int i = 0; i < memSpyFiles.size(); i++) {
					MemSpyFileBundle file = memSpyFiles.get(i);
					if (!file.exists()) {
						memSpyFiles.remove(i);
						i--;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			accessLock.release();
		}
		
		//get directory
		File cFilesFolder = new File( MemSpyFileOperations.getImportedDirectory() );
		
		if (cFilesFolder.exists() && cFilesFolder.isDirectory()) {
			
			// get folders and files in imported directory
			File[] folders = cFilesFolder.listFiles();
			if (folders != null) {
				for (int j = 0; j < folders.length; j++) {
					File cFolder = folders[j];
					if (cFolder.isDirectory()) {
						accessLock.acquire();
						try {

							MemSpyFileBundle dummy = MemSpyFileBundle.createDummyBundle(cFolder.getAbsolutePath());
							if (!memSpyFiles.contains(dummy)) {
								accessLock.release();
								MemSpyFileBundle bundle = new MemSpyFileBundle(cFolder.getAbsolutePath());
								if (bundle.hasFiles()) {
									accessLock.acquire();
									memSpyFiles.add(bundle);
								}
							} 
							else {
								accessLock.release();
								MemSpyFileBundle bundle = new MemSpyFileBundle(cFolder.getAbsolutePath());
								if (bundle.hasFiles()) {
									accessLock.acquire();
									memSpyFiles.remove(dummy);
									memSpyFiles.add(bundle);
								}
							}
						} 
						catch (Exception e) {
							e.printStackTrace();
						} 
						finally {
							accessLock.release();
						}
					}
				}
			}
		}
		
		filesObserver.memSpyFilesUpdated();
		
		accessLock.acquire();
		if (restart) {
			restart = false;
			accessLock.release();
			reSchedule();
		} 
		else {
			jobRunning = false;
			accessLock.release();
		}
		
		return Status.OK_STATUS;
	}

	/**
	 * Re schelude this job
	 * @see Job#schedule()
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
	 * Get all MemSpy files for main view to use
	 * @return files
	 */
	public MemSpyFileBundle[] getmemSpyFiles() {
		// files have not yet been read, start reading process
		if (memSpyFiles == null) {
			if(jobRunning == false){
				jobRunning = true;
				setPriority(Job.LONG);
				setUser(false);
				schedule(100);
			}
			MemSpyFileBundle[] cFiles = new MemSpyFileBundle[1];
			cFiles[0] = new MemSpyFileBundle(false);
			return cFiles;
		}
		else{
			return memSpyFiles.toArray(new MemSpyFileBundle[memSpyFiles.size()]);
		}
		
	}
	
	
	/**
	 * Refresh content. Reads files from disk again.
	 * @see MemSpyFileManager#run(IProgressMonitor)
	 */
	public void refresh() {
		
		accessLock.acquire();
		try {
		if (jobRunning) {
			restart = true;
		} else {
				setPriority(Job.LONG);
				setUser(false);
				schedule(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			accessLock.release();
		}
	}


}
