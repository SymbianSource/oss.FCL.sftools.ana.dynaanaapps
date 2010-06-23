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
* Interface used by property dialog manager to configure the property dialog
*
*/
package com.nokia.tracebuilder.engine;

import java.util.List;

/**
 * Interface used by property dialog manager to configure the property dialog
 * 
 */
public interface TraceObjectPropertyDialogConfiguration {

	/**
	 * Adds the templates to be shown in the "Add" dialog
	 * 
	 * @param templates
	 *            the list where the templates are added
	 * @param dialogType
	 *            the dialog type
	 */
	public void addViewTemplates(
			List<TraceObjectPropertyDialogTemplate> templates, int dialogType);

	/**
	 * Adds the property flags that are shown in the view for given object
	 * 
	 * @param flags
	 *            the list where the flags are added
	 * @param dialogType
	 *            the dialog type
	 */
	public void addViewFlags(List<TraceObjectPropertyDialogFlag> flags,
			int dialogType);

}
