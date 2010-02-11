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

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.RGB;

public class BinaryColorPalette extends TableColorPalette {

	@Override
	public RGB getConstantRGB(Object entry) {
		assert (entry instanceof String);
		String bin_name = new Path((String)entry).lastSegment().toLowerCase();

		// EKA1 kernel
		// Gray out Symbian kernel so user can look at their apps without Idle
		if (bin_name.equals("ekern") || bin_name.equals("ekern.exe")) { //$NON-NLS-1$ //$NON-NLS-2$
	    	return new RGB(192, 192, 192); // AWT Color.lightGray
		}

		// EKA2 kernel
		// Gray out Symbian ekern so user can look at their apps without Idle
		if (bin_name.startsWith("_reka2_")) { //$NON-NLS-1$
			return new RGB(192, 192, 192); // AWT Color.lightGray
		}

		// CarbideProf
		// Gray out Profiler so user can look at their apps without Idle
		if (bin_name.startsWith("carbideprof")) { //$NON-NLS-1$
			return new RGB(192, 192, 192); // AWT Color.lightGray
		}

		// CarbidePI
		// Gray out Profiler so user can look at their apps without Idle
		if (bin_name.startsWith("carbidepi")) { //$NON-NLS-1$
			return new RGB(192, 192, 192); // AWT Color.lightGray
		}

		// default binary not found
		if (bin_name.startsWith("binary not found")) { //$NON-NLS-1$
			return new RGB(64, 64, 64);	// AWT Color.darkGray
		}

		return null;
	}

}
