/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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

/*
 * PIFileFilter.java
 */
package com.nokia.carbide.cpp.internal.pi.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class PIFileFilter extends FileFilter
{
	String myFilter;
	public PIFileFilter(String filter)
	{
		this.myFilter = filter;
	}
	
	public boolean accept(File f)
	{
		if (f.isDirectory())
			return true;
		
		if (f.getName().endsWith(myFilter)) 
			return true;
		else
			return false;
	}
	
	public String getDescription()
	{
		if (this.myFilter.equals(Messages.getString("PIFileFilter.bapFilter"))) //$NON-NLS-1$
		{
			return Messages.getString("PIFileFilter.bapDescription");			 //$NON-NLS-1$
		}
		else if (this.myFilter.equals(Messages.getString("PIFileFilter.datFilter"))) //$NON-NLS-1$
		{
			return Messages.getString("PIFileFilter.datDescription"); //$NON-NLS-1$
		}
		else if (this.myFilter.equals(Messages.getString("PIFileFilter.csvFilter"))) //$NON-NLS-1$
		{
			return Messages.getString("PIFileFilter.csvDescription"); //$NON-NLS-1$
		}
		else if (this.myFilter.equals(Messages.getString("PIFileFilter.txtFilter"))) //$NON-NLS-1$
		{
			return Messages.getString("PIFileFilter.txtDescription"); //$NON-NLS-1$
		}
		else 
		{
			return Messages.getString("PIFileFilter.noDescription1") + myFilter + Messages.getString("PIFileFilter.noDescription2"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
