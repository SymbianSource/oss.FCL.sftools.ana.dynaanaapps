/*
* Copyright (c) 2006 Nokia Corporation and/or its subsidiary(-ies). 
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
* Base class for dynamic flags
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogDynamicFlag;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate;

/**
 * Base class for dynamic flags
 * 
 */
abstract class DialogDynamicFlagBase extends DialogFlagBase implements
		TraceObjectPropertyDialogDynamicFlag {

	/**
	 * Available flag
	 */
	private boolean available = true;

	/**
	 * Always enabled flag
	 */
	private boolean alwaysEnabled;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogDynamicFlag#
	 *      isAlwaysEnabled()
	 */
	public boolean isAlwaysEnabled() {
		return alwaysEnabled;
	}

	/**
	 * Sets the always enabled flag
	 * 
	 * @param flag
	 *            new flag value
	 */
	protected void setAlwaysEnabled(boolean flag) {
		alwaysEnabled = flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogDynamicFlag#
	 *      isAvailable()
	 */
	public boolean isAvailable() {
		return available;
	}

	/**
	 * Sets the available flag
	 * 
	 * @param flag
	 *            new flag value
	 */
	protected void setAvailable(boolean flag) {
		available = flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogDynamicFlag#
	 *      templateChanged(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate)
	 */
	public boolean templateChanged(TraceObjectPropertyDialogTemplate template) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogDynamicFlag#
	 *      flagChanged(com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag)
	 */
	public boolean flagChanged(TraceObjectPropertyDialogFlag flag) {
		return false;
	}

}
