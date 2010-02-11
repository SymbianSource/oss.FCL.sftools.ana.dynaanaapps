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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.nokia.s60tools.crashanalyser.containers.ErrorLibraryError;

/**
 * Sorts errors correctly in ErrorDialog's errors list 
 *
 * E.g.
 * 
 * -1
 * -2
 * -3
 * KErrBadHandle
 * KErrNone
 * KErrNotFound
 *
 */
public class ErrorsViewerSorter extends ViewerSorter {
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		String errorName1 = ((ErrorLibraryError)e1).toString().trim();
		String errorName2 = ((ErrorLibraryError)e2).toString().trim();
		
		// both are numbers
		if (isParsableToInt(errorName1) && isParsableToInt(errorName2)) {
			int error1 = Integer.parseInt(errorName1);
			int error2 = Integer.parseInt(errorName2);
			if (error1 > error2)
				return -1;
			else if (error1 < error2)
				return 1;
			else
				return 0;
		// errorName1 is number, errorName2 is not
		} else if (isParsableToInt(errorName1)) {
			return -1;
		// errorName1 is not a number, errorName2 is a number
		} else if (isParsableToInt(errorName2)) {
			return 1;
		// errorName1 nor errorName2 is a number
		} else {
			return errorName1.compareTo(errorName2);
		}
	}
	
	/**
	 * Checks whether given string is a number
	 * @param i string
	 * @return true if given string is a number, false if not
	 */
	boolean isParsableToInt(String i)
	{
		try
		{
			Integer.parseInt(i);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}	
}
