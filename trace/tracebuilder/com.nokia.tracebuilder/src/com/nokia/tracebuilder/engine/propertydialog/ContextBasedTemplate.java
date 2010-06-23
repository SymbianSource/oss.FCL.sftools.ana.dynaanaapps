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
* Interface that can be implemented by property dialog templates to support context-specific availability
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.source.SourceContext;

/**
 * Interface that can be implemented by property dialog templates to support
 * context-specific availability. The code instrumenter will not instrument
 * functions where this template is not available and this template will not be
 * shown in "Add Trace" dialog if not available in the function
 * 
 */
public interface ContextBasedTemplate {

	/**
	 * Checks if this template can be used in the given source context
	 * 
	 * @param context
	 *            the context to be checked
	 * @return true if context is supported, false if not
	 */
	public boolean isAvailableInContext(SourceContext context);

}
