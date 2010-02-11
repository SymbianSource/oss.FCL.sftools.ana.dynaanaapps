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

package com.nokia.carbide.cpp.internal.pi.wizards.ui.util;

public class RofsObySymbolPair {
	private String obyFile = "";	//$NON-NLS-1$
	private String symbolFile = "";	//$NON-NLS-1$
	
	public void setObyFile(String filename) {
		if (filename == null) {
			filename = "";	//$NON-NLS-1$
		}
		obyFile = filename;
	}
	
	public String getObyFile() {
		return obyFile;
	}
	
	public boolean haveObyFile() {
		return (obyFile.length() > 0);
	}
	
	public void setSymbolFile(String filename) {
		if (filename == null) {
			filename = "";	//$NON-NLS-1$
		}
		symbolFile = filename;
	}
	
	public String getSymbolFile() {
		return symbolFile;
	}
	
	public boolean haveSymbolFile() {
		return (symbolFile.length() > 0);
	}
	
	public String getDisplayString() {
		String displayString = "";	//$NON-NLS-1$
		if (obyFile.length() > 0) {
			displayString += obyFile;
		}
		if (symbolFile.length() > 0) {
			if (displayString.length() > 0) {
				displayString += '\n';	//$NON-NLS-1$
			}
			displayString += symbolFile;
		}
		return displayString;
	}

	public boolean equals(RofsObySymbolPair pair) {
		if (this.getObyFile().equals(pair.getObyFile()) == false) {
			return false;
		}
		if (this.getSymbolFile().equals(pair.getSymbolFile()) == false) {
			return false;
		}
		return true;
	}
}
