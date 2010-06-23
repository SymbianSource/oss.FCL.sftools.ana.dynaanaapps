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
* Interface to add text to the object name shown in view
*
*/
package com.nokia.tracebuilder.engine;

import com.nokia.tracebuilder.model.TraceModelExtension;

/**
 * Interface to add text to the object name shown in view
 * 
 */
public interface TraceViewNameExtension extends TraceModelExtension {

	/**
	 * Returns the name extension that is shown in parenthesis after the actual
	 * object name
	 * 
	 * @return the name extension
	 */
	public String getNameExtension();

}
