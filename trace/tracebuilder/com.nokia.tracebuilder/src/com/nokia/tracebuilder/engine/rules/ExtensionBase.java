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
* Base class for model extensions
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Base class for model extensions
 * 
 */
public class ExtensionBase implements TraceModelExtension {

	/**
	 * The owning trace object
	 */
	private TraceObject owner;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#
	 *      setOwner(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void setOwner(TraceObject owner) {
		this.owner = owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#getOwner()
	 */
	public TraceObject getOwner() {
		return owner;
	}

}
