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



package com.nokia.s60tools.memspy.ui.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.nokia.s60tools.memspy.interfaces.INewMemSpyFilesObserver;
import com.nokia.s60tools.memspy.model.MemSpyFileManager;

/**
 * This class provides content for main view
 */
class MemSpyMainViewContentProvider implements IStructuredContentProvider {
	
	private final MemSpyFileManager memSpyFileManager;
	
	/**
	 * Create new content provider
	 * @param observer
	 */
	public MemSpyMainViewContentProvider( INewMemSpyFilesObserver observer ){
		memSpyFileManager = new MemSpyFileManager( observer );
	}
	/**
	 * Refresh file manager
	 * @see MemSpyFileManager#refresh()
	 */
	public void refresh() {
		memSpyFileManager.refresh();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		return memSpyFileManager.getmemSpyFiles();
	}
	
}
