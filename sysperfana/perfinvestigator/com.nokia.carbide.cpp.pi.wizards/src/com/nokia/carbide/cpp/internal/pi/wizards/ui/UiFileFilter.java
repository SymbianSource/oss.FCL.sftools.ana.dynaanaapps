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
 * UiFileFilter.java
 */
package com.nokia.carbide.cpp.internal.pi.wizards.ui;
//package com.nokia.carbide.cpp.internal.pi.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class UiFileFilter extends FileFilter
{
	static final String AXF_EXTENSION= "axf"; //$NON-NLS-1$
	static final String ELF_EXTENSION= "elf"; //$NON-NLS-1$
	static final String DOT_C_EXTENSION= ".c"; //$NON-NLS-1$
	static final String DOT_CPP_EXTENSION= ".cpp"; //$NON-NLS-1$
	static final String DOT_H_EXTENSION= ".h"; //$NON-NLS-1$
	static final String EXE_EXTENSION= "exe"; //$NON-NLS-1$
	static final String DLL_EXTENSION= "dll"; //$NON-NLS-1$
	static final String PDD_EXTENSION= "pdd"; //$NON-NLS-1$
	static final String SYMBOL_EXTENSION= "symbol"; //$NON-NLS-1$
	static final String OBY_EXTENSION= "oby"; //$NON-NLS-1$
	static final String BAP_EXTENSION= "bap"; //$NON-NLS-1$
	static final String DAT_EXTENSION= "dat"; //$NON-NLS-1$
	static final String CSV_EXTENSION= "csv"; //$NON-NLS-1$
	static final String TXT_EXTENSION= "txt"; //$NON-NLS-1$

	String myFilter;
	public UiFileFilter(String filter)
	{
		this.myFilter = filter;
	}
	public boolean accept(File f)
	{
		if(f.isDirectory()) return true;
		
		if(f.getName().endsWith(myFilter)) 
			return true;
		else
			return false;
	}
	
	public String getDescription()
	{
		// Symbian build system file types
		if(this.myFilter.equals(AXF_EXTENSION))
		{
			return Messages.getString("UiFileFilter.object.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(ELF_EXTENSION))
		{
			return Messages.getString("UiFileFilter.object.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(DOT_C_EXTENSION))
		{
			return Messages.getString("UiFileFilter.source.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(DOT_CPP_EXTENSION))
		{
			return Messages.getString("UiFileFilter.source.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(DOT_H_EXTENSION))
		{
			return Messages.getString("UiFileFilter.source.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(EXE_EXTENSION))
		{
			return Messages.getString("UiFileFilter.executable.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(DLL_EXTENSION))
		{
			return Messages.getString("UiFileFilter.executable.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(PDD_EXTENSION))
		{
			return Messages.getString("UiFileFilter.executable.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(SYMBOL_EXTENSION))
		{
			return Messages.getString("UiFileFilter.symbol.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(OBY_EXTENSION))
		{
			return Messages.getString("UiFileFilter.build.file.extensions"); //$NON-NLS-1$
		}

		// analyser file types
		else if(this.myFilter.equals(BAP_EXTENSION))
		{
			return Messages.getString("UiFileFilter.pi.analysis.file.extensions");			 //$NON-NLS-1$
		}		
		else if(this.myFilter.equals(DAT_EXTENSION))
		{
			return Messages.getString("UiFileFilter.raw.pi.measurement.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(CSV_EXTENSION))
		{
			return Messages.getString("UiFileFilter.comma.separated.text.file.extensions"); //$NON-NLS-1$
		}
		else if(this.myFilter.equals(TXT_EXTENSION))
		{
			return Messages.getString("UiFileFilter.file.extensions"); //$NON-NLS-1$
		}
		else 
		{
			return Messages.getString("UiFileFilter.no.description.error1")+myFilter+Messages.getString("UiFileFilter.no.description.error2"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
