/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Interface to location parser
*
*/
package com.nokia.tracebuilder.project;

import com.nokia.tracebuilder.engine.TraceLocation;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.source.SourceParserRule.TraceConversionResult;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.source.SourceExcludedArea;

/**
 * Interface to location parser. The parser is implemented into the project API
 * 
 */
public interface TraceLocationParser {

	/**
	 * Processes a newly parsed location. The location has not yet been
	 * associated with a trace.
	 * 
	 * @param location
	 *            the new location
	 */
	public void processNewLocation(TraceLocation location);

	/**
	 * Gets the group where locations created by this parser belong
	 * 
	 * @return the location group
	 */
	public String getLocationGroup();

	/**
	 * Converts a location to trace
	 * 
	 * @param location
	 *            the location to be parsed
	 * @return properties for new trace
	 * @throws TraceBuilderException
	 *             if the location cannot be converted
	 */
	public TraceConversionResult convertLocation(TraceLocation location)
			throws TraceBuilderException;

	/**
	 * Finds the comment related to given location
	 * 
	 * @param location
	 *            the location to be checked
	 * @return the comment related to the location or null if not found
	 */
	public SourceExcludedArea findLocationComment(TraceLocation location);

	/**
	 * Determines if a location parsed from source should be automatically
	 * converted to a trace. This is called only if
	 * isLocationAutoConvertSupported has returned true
	 * 
	 * @param location
	 *            the location to be checked
	 * 
	 * @return true if automatically converted, false if not
	 */
	public boolean isLocationConverted(TraceLocation location);

	/**
	 * Checks if a location matches its trace
	 * 
	 * @param location
	 *            the location to be checked
	 * @return error code from TraceBuilderErrorCodes
	 */
	public TraceBuilderErrorCode checkLocationValidity(TraceLocation location);

}
