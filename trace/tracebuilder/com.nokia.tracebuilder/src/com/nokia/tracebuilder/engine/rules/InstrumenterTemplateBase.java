/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
* Base class for instrumenter templates
*
*/
package com.nokia.tracebuilder.engine.rules;

import com.nokia.tracebuilder.engine.TracePropertyDialogTemplate;

/**
 * Base class for instrumenter templates
 * 
 */
public abstract class InstrumenterTemplateBase implements
		TracePropertyDialogTemplate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#isIDEnabled()
	 */
	public boolean isIDEnabled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#isNameEnabled()
	 */
	public boolean isNameEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#isValueEnabled()
	 */
	public boolean isValueEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#isTargetEnabled()
	 */
	public boolean isTargetEnabled() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TracePropertyDialogTemplate#getText(java.lang.String)
	 */
	public String getText(String groupName) {
		return getValue();
	}

}
