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
package com.nokia.carbide.cpp.pi.address;

import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

/**
 * Utility class for functionality associated with GppTraceGraph
 *
 */
public final class GppTraceUtil {
	static private final String STRING_NOT_FOUND = Messages.getString("GppTraceGraph.notFound"); //$NON-NLS-1$

	static private final String STRING_BINARY_AT = Messages.getString("GppTraceGraph.binaryAt"); //$NON-NLS-1$
	static private final String STRING_BINARY_FOR_ADDRESS = Messages.getString("GppTraceGraph.binaryForAddress"); //$NON-NLS-1$
	static private final String STRING_BINARY_NOT_FOUND = Messages.getString("GppTraceGraph.binaryNotFound"); //$NON-NLS-1$

	static private final String STRING_FUNCTION_AT = Messages.getString("GppTraceGraph.functionAt"); //$NON-NLS-1$
	static private final String STRING_FUNCTION_FOR_ADDRESS = Messages.getString("GppTraceGraph.functionForAddress"); //$NON-NLS-1$
	static private final String STRING_FUNCTION_NOT_FOUND = Messages.getString("GppTraceGraph.functionNotFound"); //$NON-NLS-1$

	/**
	 * Private constructor to avoid instantiation
	 */
	private GppTraceUtil() {
	}

	/**
	 * Returns the name of the binary from the given sample's function
	 * @param s the sample to use
	 * @return the binary's name
	 */
	public static String getBinaryName(GppSample s) {
		String name = null;

		if (s.getCurrentFunctionSym() != null)
			name = s.getCurrentFunctionSym().getFunctionBinary().getBinaryName();

		if ((s.getCurrentFunctionItt() != null)
				&& ((name == null) || name.endsWith(STRING_NOT_FOUND))) {
			name = s.getCurrentFunctionItt().getFunctionBinary().getBinaryName();
		}

		if (name == null || name.startsWith(STRING_BINARY_AT)
				|| name.startsWith(STRING_BINARY_FOR_ADDRESS)) {
			name = STRING_BINARY_NOT_FOUND;
		}
		return name;
	}

	/**
	 * Returns the function name found within the given sample
	 * @param s The sample to use
	 * @return the function name
	 */
	public static String getFunctionName(GppSample s) {
		String name = null;
		if (s.getCurrentFunctionSym() != null)
			name = s.getCurrentFunctionSym().getFunctionName();

		if ((s.getCurrentFunctionItt() != null)
				&& ((name == null) || name.endsWith(STRING_NOT_FOUND))) {
			name = s.getCurrentFunctionItt().getFunctionName();
		}

		if ((name == null) || (name.startsWith(STRING_FUNCTION_AT))
				|| (name.startsWith(STRING_FUNCTION_FOR_ADDRESS))) {
			name = STRING_FUNCTION_NOT_FOUND;
		}

		return name;
	}

	/**
	 * Returns the address of the function from the sample
	 * @param s the sample to use
	 * @return the function's address, or 0 if no function is given 
	 */
	public static long getFunctionAddress(GppSample s) {
		if (s.getCurrentFunctionSym() != null) {
			String name = s.getCurrentFunctionSym().getFunctionName();
			if ((s.getCurrentFunctionItt() != null)
					&& (name == null || name.endsWith(Messages
							.getString("GppTraceGraph.notFound")))) //$NON-NLS-1$
			{
				return s.getCurrentFunctionItt().getStartAddress().longValue();
			} else {
				return s.getCurrentFunctionSym().getStartAddress().longValue();
			}
		} else if (s.getCurrentFunctionItt() != null) {
			if (s.getCurrentFunctionItt().getFunctionName() == null) {
				if (s.getCurrentFunctionSym() != null)
					return s.getCurrentFunctionSym().getStartAddress().longValue();
			} else {
				return s.getCurrentFunctionItt().getStartAddress().longValue();
			}
		}

		return 0;
	}

	/**
	 * Calculates and returns the number of buckets required to accommodate all samples
	 * @param lastSampleTime highest-possible timestamp in the set of samples
	 * @param granularityValue number of samples per bucket
	 * @return number of buckets
	 */
	public static int calculateNumberOfBuckets(final long lastSampleTime, final int granularityValue){
		return (int)(lastSampleTime / granularityValue) +1; 
	}

	/**
	 * From the given graph index, works out which page it goes on. 
	 * In SMP, all single-CPU graphs go onto the threads page.
	 * @param graphIndex the graph index to use
	 * @return the page index the graph is on
	 */
	public static int getPageIndex(int graphIndex) {
		return graphIndex < 3 ? graphIndex : PIPageEditor.THREADS_PAGE;
	}
	
	


}
