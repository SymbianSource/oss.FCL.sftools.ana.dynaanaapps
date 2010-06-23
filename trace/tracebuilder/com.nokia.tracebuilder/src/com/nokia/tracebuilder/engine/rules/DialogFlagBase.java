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
* Base class for instrumenter dialog flags
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag;

/**
 * Base class for dialog flags
 * 
 */
abstract class DialogFlagBase implements TraceObjectPropertyDialogFlag {

	/**
	 * Enabled flag
	 */
	private boolean enabled;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.InstrumenterDialogFlag#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled && isVisible();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.InstrumenterDialogFlag#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogFlag#isVisible()
	 */
	public boolean isVisible() {
		return true;
	}

}
