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

package com.nokia.s60tools.crashanalyser.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.nokia.s60tools.crashanalyser.model.*;
import com.nokia.s60tools.crashanalyser.containers.ErrorLibraryError;
import com.nokia.s60tools.crashanalyser.interfaces.IErrorLibraryObserver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;

/**
 * ErrorLibrary class contains all error descriptions. It contains
 * a list of errors, panics and panic categories. 
 *
 */
public final class ErrorLibrary extends Job {

	private static ErrorLibrary contentProvider = null;
	Map<String, ErrorLibraryError> errors = null;
	Map<String, ErrorLibraryError> panics = null;
	Map<String, ErrorLibraryError> categories = null;
	List<IErrorLibraryObserver> observers = null;
	boolean errorsRead = false;
	boolean running = false;
	ILock accessLock = null;
	ILock queryLock = null;
	
	/**
	 * a singleton constructor
	 * @param observer 
	 * @return a ErrorLibrary instance
	 */
	public static ErrorLibrary getInstance(IErrorLibraryObserver observer) {
		if (contentProvider == null) {
			contentProvider = new ErrorLibrary();
		}
		
		contentProvider.run(observer);
		
		return contentProvider;
	}
	
	/**
	 * private constructor
	 */
	private ErrorLibrary() {
		super("Errors container");
		accessLock = Job.getJobManager().newLock();
		queryLock = Job.getJobManager().newLock();
		setPriority(Job.LONG);
		setUser(false);
	}
	
	/**
	 * Starts the errors reading job
	 * @param observer
	 */
	void run(IErrorLibraryObserver observer) {
		accessLock.acquire();
		addObserver(observer);
		try {
			if (running)
				return;
			else
				running = true;
		} finally {
			accessLock.release();
		}
		schedule(100);		
	}

	public boolean isReady() {
		return errorsRead;
	}
	
	/**
	 * Returns list of errors
	 * @return list of errors
	 */
	public ErrorLibraryError[] getErrors() {
		queryLock.acquire();
		try {
			if (errors == null)
				return new ErrorLibraryError[0];
	
			return errors.values().toArray(new ErrorLibraryError[errors.size()]);
		} finally {
			queryLock.release();
		}
	}
	
	/**
	 * Returns list of panics
	 * @return list of panics
	 */
	public ErrorLibraryError[] getPanics() {
		queryLock.acquire();
		try {
			if (panics == null)
				return new ErrorLibraryError[0];
			
			return panics.values().toArray(new ErrorLibraryError[panics.size()]);
		} finally {
			queryLock.release();
		}
	}
	
	/**
	 * Returns list of panic categories
	 * @return list of panic categories
	 */
	public ErrorLibraryError[] getCategories() {
		queryLock.release();
		try {
			if (categories == null)
				return new ErrorLibraryError[0];
	
		return categories.values().toArray(new ErrorLibraryError[categories.size()]);
		} finally {
			queryLock.release();
		}
		
	}
	
	/**
	 * Searches a description for given panic code
	 * @param category e.g. USER
	 * @param id e.g. 46
	 * @return description for given panic. "" if not found.
	 */
	public String getPanicDescription(String category, String id) {
		queryLock.acquire();
		try {
			String retval = "";
			
			if (panics != null && panics.containsKey(category+id)) {
				retval = panics.get(category+id).getDescription();
			}
			
			return retval;
		} finally {
			queryLock.release();
		}
	}
	
	/**
	 * Searches a description for given error code
	 * @param value e.g. -1 or KErrNotFound etc.
	 * @return description for given error. "" if not found.
	 */
	public String getErrorDescription(String value) {
		queryLock.acquire();
		try {
			String retval = "";
			
			if (errors != null && errors.containsKey(value)) {
				retval = errors.get(value).getDescription();
			}
			
			return retval;
		} finally {
			queryLock.release();
		}
	}
	
	/**
	 * Adds a new error library observer
	 * @param observer
	 */
	void addObserver(IErrorLibraryObserver observer) {
		try {
			if (observers == null)
				observers = new ArrayList<IErrorLibraryObserver>();
			if (observer != null && !observers.contains(observer)) {
				try {
					observers.add(observer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Notifies observers that error library is ready to be used
	 */
	void errorReaderReady() {
		
		try {
			while (!observers.isEmpty()) {
				IErrorLibraryObserver observer = observers.get(0);
				observer.errorLibraryReady();
				observers.remove(observer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		running = false;
	}
	
	@Override
	protected IStatus run(IProgressMonitor arg0) {
		try {
			// if errors haven't already been read, read errors
			if (!errorsRead) {
				ErrorsXmlReader reader = new ErrorsXmlReader();
				reader.readAll();
				errors = reader.getErrorsOwnership();
				panics = reader.getPanicsOwnership();
				categories = reader.getCategoriesOwnership();
				errorsRead = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		accessLock.acquire();
		errorReaderReady();
		accessLock.release();
		
		return Status.OK_STATUS;
	}
}
