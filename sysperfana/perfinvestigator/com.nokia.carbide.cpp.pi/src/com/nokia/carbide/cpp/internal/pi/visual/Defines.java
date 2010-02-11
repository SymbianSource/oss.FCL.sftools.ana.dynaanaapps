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

package com.nokia.carbide.cpp.internal.pi.visual;

public class Defines {

   
    // what to show, based on the ordering of tables
    // used as drawing mode and for determining table order
    // The last item in a name is what is shown in the graph.
    // E.g., THREADS_FUNCTIONS means show functions that match
    // the selected threads in the selected graph area.
    
    // Threads
	public static final int THREADS                    =  1;
	// Threads -> Functions
	public static final int THREADS_FUNCTIONS          =  2;
	// Threads -> Functions -> Binaries
	public static final int THREADS_FUNCTIONS_BINARIES =  3;
	// Threads -> Binaries
	public static final int THREADS_BINARIES           =  4;
	// Threads -> Binaries -> Functions
	public static final int THREADS_BINARIES_FUNCTIONS =  5;
	// Binaries
	public static final int BINARIES                   =  6;
	// Binaries -> Threads
	public static final int BINARIES_THREADS           =  7;
	// Binaries -> Threads -> Functions
	public static final int BINARIES_THREADS_FUNCTIONS =  8;
	// Binaries -> Functions
	public static final int BINARIES_FUNCTIONS         =  9;
	// Binaries -> Functions -> Threads
	public static final int BINARIES_FUNCTIONS_THREADS = 10;
	// Functions
	public static final int FUNCTIONS                  = 11;
	// Functions -> Threads
	public static final int FUNCTIONS_THREADS          = 12;
	// Functions -> Threads -> Binaries
	public static final int FUNCTIONS_THREADS_BINARIES = 13;
	// Functions -> Binaries
	public static final int FUNCTIONS_BINARIES         = 14;
	// Functions -> Binaries -> Threads
	public static final int FUNCTIONS_BINARIES_THREADS = 15;
	
	// In function analysis there are no graphs. Functions to
	// examine are chosen by selecting threads then binaries,
	// or binaries then threads.
	
	// In function analysis: Threads -> Binaries -> Functions
	public static final int ANALYSIS_THREADS_BINARIES = 101;
	// In function analysis: Binaries -> Threads -> Functions
	public static final int ANALYSIS_BINARIES_THREADS = 102;
}
