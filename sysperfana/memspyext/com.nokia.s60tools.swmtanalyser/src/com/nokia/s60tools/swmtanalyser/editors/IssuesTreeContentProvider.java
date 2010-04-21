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
package com.nokia.s60tools.swmtanalyser.editors;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.nokia.s60tools.swmtanalyser.analysers.ResultsParentNodes;

/**
 * ContentProvider for table viewer in the Analysis tab.
 *
 */
public class IssuesTreeContentProvider implements ITreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		ArrayList<Object> total_children = new ArrayList<Object>();
		
		if(parent instanceof ResultsParentNodes) {
			return ((ResultsParentNodes)parent).getChildren();
		}
		
		return total_children.toArray();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object arg0) {
		if(arg0 instanceof ResultsParentNodes)
			return arg0;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object arg0) {
		return getChildren(arg0)!=null && getChildren(arg0).length > 0;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Object[] getElements(Object arg0) {
		return ((ArrayList)arg0).toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
	
	}

}
