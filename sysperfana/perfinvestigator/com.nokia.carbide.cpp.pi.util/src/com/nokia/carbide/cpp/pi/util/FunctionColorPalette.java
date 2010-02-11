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

import org.eclipse.swt.graphics.RGB;

public class FunctionColorPalette extends TableColorPalette {

	@Override
	public RGB getConstantRGB(Object entry) {
		assert (entry instanceof String);
		String func_name = ((String)entry).toLowerCase();
		
		// Gray out HAL Idle and RestoreIrqs that most of the empty cycle are spun
		if (func_name.startsWith("imphal::idle")) { //$NON-NLS-1$
	    	return new RGB(192,192,192); // AWT Color.lightGray
		}
		if (func_name.startsWith("imphal::restoreirqs")) { //$NON-NLS-1$
	    	return new RGB(192,192,192); // AWT Color.lightGray
		}
		
		// EKA2
		if (func_name.startsWith("nkern::restoreinterrupts") || //$NON-NLS-1$
				func_name.startsWith("a::nullthread") || //$NON-NLS-1$
				func_name.startsWith("kernelmain") || //$NON-NLS-1$
				func_name.contains("::cpuidle()") || //$NON-NLS-1$
				func_name.startsWith("nkern::disableallinterrupts()") || //$NON-NLS-1$
				func_name.startsWith("ntimerq::timeraddress")) { //$NON-NLS-1$
	    	return new RGB(192,192,192); // AWT Color.lightGray			
		}
		
		// default function not found
		if (func_name.startsWith("function not found_")) { //$NON-NLS-1$
			return new RGB(64, 64, 64);	// AWT Color.darkGray
		}
		
	    return null;
	}

}
