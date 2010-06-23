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
* Interface to the list of trace project API's registered to TraceBuilder
*
*/
package com.nokia.tracebuilder.project;

import java.util.Iterator;

import com.nokia.tracebuilder.model.TraceModelExtension;

/**
 * Interface to the list of trace project API's registered to TraceBuilder
 * 
 */
public interface TraceProjectAPIList extends TraceModelExtension {

	/**
	 * Gets the list of API's
	 * 
	 * @return the list
	 */
	public Iterator<TraceProjectAPI> getAPIs();

}
