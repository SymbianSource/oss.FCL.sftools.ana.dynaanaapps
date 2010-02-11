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

package com.nokia.s60tools.crashanalyser.search;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A filter class for a text viewer. Used e.g. in ErrorLibrary dialog to
 * filter list of text which match to what user has started to write.
 *
 */
public class TextViewerFilter extends ViewerFilter {

	String filterText = "";
	
	/**
	 * Constructor
	 * @param text text to match
	 */
	public TextViewerFilter(String text) {
		filterText = text.toLowerCase();
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element.toString().toLowerCase().startsWith(filterText)) 
			return true;
		return false;
	}
	
	public boolean isFilterProperty(Object element, String property) {
		return true;
	}

}
