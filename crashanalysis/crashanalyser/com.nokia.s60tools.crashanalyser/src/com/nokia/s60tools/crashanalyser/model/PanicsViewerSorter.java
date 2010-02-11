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

import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.viewers.Viewer;

import com.nokia.s60tools.crashanalyser.containers.ErrorLibraryError;

/**
 * Sorts errors correctly in ErrorDialog's panics list.
 * 
 *  E.g.
 *  
 *  USER 1
 *  USER 2
 *  USER 34
 *  W32 1
 *  W32 2
 *
 */
public class PanicsViewerSorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		String panicName1 = ((ErrorLibraryError)e1).toString().trim();
		String panicName2 = ((ErrorLibraryError)e2).toString().trim();
		
		// separate panic id from panic name
		// panic name is category + panic id e.g. "W32 4"
		int index1 = panicName1.lastIndexOf(' ');
		int index2 = panicName2.lastIndexOf(' ');
		if (index1 > -1 && index2 > -1) {
			String category1 = panicName1.substring(0, index1).trim();
			String category2 = panicName2.substring(0, index2).trim();
			String panicId1 = panicName1.substring(index1).trim();
			String panicId2 = panicName2.substring(index2).trim();
			int res = category1.compareTo(category2);
			if (res == 0) {
				try {
					int panic1 = Integer.valueOf( panicId1 ).intValue();
					int panic2 = Integer.valueOf( panicId2 ).intValue();
					if (panic1 < panic2)
						return -1;
					else if (panic1 > panic2)
						return 1;
					else
						return 0;
				} catch (Exception e) {
					return panicName1.compareTo(panicName2);
				}
			} else {
				return panicName1.compareTo(panicName2);
			}
				
		} else {
			return panicName1.compareTo(panicName2);
		}
	}
}
