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

package com.nokia.carbide.cpp.pi.util;

// Sometime Symbian files are referring everything as root relative,
// sometime there is reference to $(EPOCROOT) too
// let's try to guess what it was. I hate windows drive letter

public class GuessAndFixPath {
	public static String fixPath(String path, String epocroot, String fileInPath) {
		if (path.charAt(0) != '\\')
			return path;

		// currently what we get from the final .PKG seems to referring root directory
		// to the root of the drive that EPOCROOT is on
		if (path.substring(0, 11).equalsIgnoreCase("$(EPOCROOT)")) { //$NON-NLS-1$
			return path.replace("\\$(EPOCROOT)", epocroot); //$NON-NLS-1$
		}

		String rootOfEpocRoot;

		if (epocroot.indexOf('\\') > 0) {
			// remove trailling '\\'
			if (epocroot.endsWith("\\")) //$NON-NLS-1$
			{
				rootOfEpocRoot = epocroot.substring(0, epocroot.lastIndexOf('\\'));
			}
			else {
				rootOfEpocRoot = epocroot;
			}
		} else {
			rootOfEpocRoot = ""; //$NON-NLS-1$
		}
		return rootOfEpocRoot + path;
	}
}
