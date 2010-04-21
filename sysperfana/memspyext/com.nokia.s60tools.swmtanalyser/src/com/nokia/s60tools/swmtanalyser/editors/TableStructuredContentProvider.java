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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for chunk/thread/disk etc.. tables 
 *
 */
public class TableStructuredContentProvider implements IStructuredContentProvider {

	FilterTextTable table;
	/**
	 * Constructor
	 * @param table
	 */
	public TableStructuredContentProvider(FilterTextTable table) {
		this.table = table;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object arg0) {
		ArrayList<TableViewerInputObject> items  = new ArrayList<TableViewerInputObject>();
		for(String name: table.input)
		{
			TableViewerInputObject obj = new TableViewerInputObject(name, null);
			items.add(obj);
		}
		return items.toArray();
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
