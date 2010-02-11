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

package com.nokia.s60tools.crashanalyser.ui.console;

import org.eclipse.jface.resource.ImageDescriptor;

import com.nokia.s60tools.util.console.AbstractProductSpecificConsole;

/**
 * A console for Crash Analyser.
 *
 */
public final class CrashAnalyserEditorConsole extends AbstractProductSpecificConsole {

	/**
	 * Private constructor forcing Singleton usage of the class.
	 */
	private CrashAnalyserEditorConsole() {		
	}
	
	/**
	 * Singleton instance of the class.
	 */
	static private CrashAnalyserEditorConsole instance = null;
	
	/**
	 * Public accessor method.
	 * @return Singleton instance of the class.
	 */
	static public CrashAnalyserEditorConsole getInstance(){
		if(instance == null ){
			instance = new CrashAnalyserEditorConsole();
		}
		return instance;
	}	

	@Override
	protected ImageDescriptor getProductConsoleImageDescriptor() {
		return null;
	}

	@Override
	protected String getProductConsoleName() {
		return "Crash Analyser";
	}

}
