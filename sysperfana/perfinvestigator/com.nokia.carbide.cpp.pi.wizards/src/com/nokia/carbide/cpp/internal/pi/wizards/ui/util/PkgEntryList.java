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

import java.util.ArrayList;

import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;

public class PkgEntryList {
	private static ArrayList<IPkgEntry> entries = new ArrayList<IPkgEntry>();
	private static PkgEntryList instance = null;
	
	//PKG file and their SDK_ID or EPOCROOT
	private final class PkgEntry implements IPkgEntry {
		private String pkgFile;
		private ISymbianSDK sdk;
		
		private PkgEntry(String myPkg, ISymbianSDK mySdk) {
			pkgFile = myPkg;
			sdk = mySdk;
		}
		
		public String getPkgFile() {
			return pkgFile;
		}
			
		public ISymbianSDK getSdk() {
			return sdk;
		}
	}

	private PkgEntryList() {
		// singleton
	}
	
	public static PkgEntryList getInstance() {
		if (instance == null) {
			instance = new PkgEntryList();
		}
		return instance;
	}
	
	public IPkgEntry getPkgEntry(String myPkg, ISymbianSDK mySdk) {
		IPkgEntry found = null;
		for (IPkgEntry entry : entries) {
			if (entry.getPkgFile().equals(myPkg) &&
					entry.getSdk().equals(mySdk)) {
				found = entry;
				break;
			}
		}
		if (found == null) {
			found = new PkgEntry(myPkg, mySdk);
			entries.add(found);
		}
		return found;
	}
}
