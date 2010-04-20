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

package com.nokia.s60tools.crashanalyser.ui.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.interfaces.INewCrashFilesObserver;
import com.nokia.s60tools.crashanalyser.model.CrashFileBundle;
import com.nokia.s60tools.crashanalyser.model.CrashFileManager;

/**
 * This class provides all rows to MainView's table. This class 
 * contains CrashFileManager which is responsible for handling
 * Crash files on hard drive. This class just gets all files from
 * CrashFileManager and provides them to MainView's table in correct
 * format. 
 *
 */
public class MainViewContentProvider implements ITreeContentProvider { // IStructuredContentProvider {

	private final CrashFileManager crashFileManager;	
	private ErrorLibrary errorLibrary = null;
	
	public MainViewContentProvider(INewCrashFilesObserver observer) {
		crashFileManager = new CrashFileManager(observer);
	}
	
	public void refresh() {
		crashFileManager.refresh();
	}
	
	public void setErrorLibrary(ErrorLibrary library) {
		errorLibrary = library;
	}

	public Object[] getElements(Object arg0) {
		return crashFileManager.getCrashFiles(errorLibrary);
	}

	public void dispose() {
		// No implementation needed
	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// No implementation needed
	}

	public Object[] getChildren(Object crashFile) {
		return crashFileManager.getThreads((CrashFileBundle) crashFile);
	}

	public Object getParent(Object arg0) {
		return null;
	}

	public boolean hasChildren(Object arg0) {
		if (crashFileManager.getTotalThreadCount((CrashFileBundle) arg0) > 1)
			return true;
		return false;
	}
}
