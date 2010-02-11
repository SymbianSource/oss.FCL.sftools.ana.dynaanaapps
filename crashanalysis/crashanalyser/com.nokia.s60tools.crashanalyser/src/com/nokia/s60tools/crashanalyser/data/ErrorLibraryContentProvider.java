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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 *  Error library content provider.
 *
 */
public class ErrorLibraryContentProvider implements IStructuredContentProvider {
	
	public static enum ContentTypes {PANIC, CATEGORY, ERROR}
	ErrorLibrary library = null;
	ContentTypes type; 
	
	/**
	 * Constructor
	 * @param contentType which types or errors this provider should provide 
	 * @param errorLibrary error library which contains all errors
	 */
	public ErrorLibraryContentProvider(ContentTypes contentType, ErrorLibrary errorLibrary) {
		type = contentType;
		library = errorLibrary;
	}
	
	public Object[] getElements(Object arg0) {
		switch (type) {
		case PANIC:
			return library.getPanics();
		case CATEGORY:
			return library.getCategories();
		case ERROR:
			return library.getErrors();
		default:
			return null;
		}
	}

	public void dispose() {
		// No implementation needed
	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// No implementation needed
	}

}
