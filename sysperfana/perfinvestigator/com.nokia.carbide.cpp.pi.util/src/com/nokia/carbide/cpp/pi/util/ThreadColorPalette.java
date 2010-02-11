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

public class ThreadColorPalette extends TableColorPalette {

	@Override
	public RGB getConstantRGB(Object entry) {
		
		assert (entry instanceof String);
		//threadName   = sample.thread.process.name + "::" + sample.thread.threadName + "_" + sample.thread.threadId;

		String string = (String)entry;
		
		int lastIndexOf = string.lastIndexOf("_");
		
		
		String threadName[] = string.substring(0, lastIndexOf).split("::"); //$NON-NLS-1$ //$NON-NLS-2$
		
		int length = threadName.length;
		
		if(length == 1){
			String newArray[] = new String[2];
			newArray[0] = threadName[0];
			newArray[1] = string.substring(lastIndexOf);
			threadName = newArray;
		}
		
		assert (threadName.length == 2);
		
		String pro_string = threadName[0];
		String thr_string = threadName[1];
		int mark;
		if (pro_string.indexOf(".") != -1) //$NON-NLS-1$
		    mark = pro_string.indexOf(".");  //EKA2 //$NON-NLS-1$
		else if (pro_string.indexOf('[') != -1)
		    mark = pro_string.indexOf('[');  //EKA1
		else
			return null;
		
		pro_string = pro_string.substring(0, mark);
		
	    if (pro_string.equalsIgnoreCase("EKern") && thr_string.equalsIgnoreCase("NULL")) { //$NON-NLS-1$ //$NON-NLS-2$
	    	return new RGB(192,192,192); // AWT Color.lightGray
	    }
	    if (pro_string.equalsIgnoreCase("CarbideProf") && thr_string.equalsIgnoreCase("Profiler")) { //$NON-NLS-1$ //$NON-NLS-2$
	    	return new RGB(0, 255, 255);	// AWT Color.cyan
	    }
	    if (pro_string.equalsIgnoreCase("C32exe") && thr_string.equalsIgnoreCase("EtelServer")) { //$NON-NLS-1$ //$NON-NLS-2$
	        return new RGB(255, 200, 0);	// AWT Color.orange
	    }
	    if (pro_string.equalsIgnoreCase("C32exe") && thr_string.equalsIgnoreCase("SocketServer")) { //$NON-NLS-1$ //$NON-NLS-2$
	        return new RGB(255, 175, 175);	// AWT Color.pink
	    }
	    if (pro_string.equalsIgnoreCase("DosServer") && thr_string.equalsIgnoreCase("DosServer")) { //$NON-NLS-1$ //$NON-NLS-2$
	        return new RGB(255, 0, 255);	// AWT Color magenta
	    }
	    if (pro_string.equalsIgnoreCase("EFile") && thr_string.equalsIgnoreCase("FileServer")) { //$NON-NLS-1$ //$NON-NLS-2$
	        return new RGB(0, 0, 255);	// AWT Color.blue
	    }
	    if (pro_string.equalsIgnoreCase("EKern") && thr_string.equalsIgnoreCase("Supervisor")) { //$NON-NLS-1$ //$NON-NLS-2$
	        return new RGB(0, 0, 0);	// AWT Color.black
	    }
	    if (pro_string.equalsIgnoreCase("EikSrvs") && thr_string.equalsIgnoreCase("ViewServerThread")) { //$NON-NLS-1$ //$NON-NLS-2$
	        return new RGB(64, 64, 64);	// AWT Color.darkGray
	    }
	    if (pro_string.equalsIgnoreCase("FbServ") && thr_string.equalsIgnoreCase("Fontbitmapserver")) { //$NON-NLS-1$ //$NON-NLS-2$
	        return new RGB(255, 0, 0);	// AWT Color.red
	    }
	    if (pro_string.equalsIgnoreCase("Phone") && thr_string.equalsIgnoreCase("Phone")) { //$NON-NLS-1$ //$NON-NLS-2$
	        return new RGB(128, 128, 128);	// AWT Color.gray
	    }
	    
	    return null;
	}

}
