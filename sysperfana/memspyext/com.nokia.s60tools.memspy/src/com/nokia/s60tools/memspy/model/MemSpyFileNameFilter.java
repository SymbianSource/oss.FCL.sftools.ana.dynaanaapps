/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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



package com.nokia.s60tools.memspy.model;

import java.io.File;
import java.io.FilenameFilter;

/**
 * File name filter for MemSpy log files(*.log, *.txt)
 */
public class MemSpyFileNameFilter implements FilenameFilter{
	
        /* (non-Javadoc)
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        public boolean accept(File dir, String name) {
        	if( name.endsWith(".txt") || name.endsWith(".log") ){
        		return true;
        	}
        	else{
        		return false;
        	}
        }
}

