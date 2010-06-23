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
* Template for "this" pointer parameter.
*
*/
package com.nokia.tracebuilder.engine.rules;

import java.util.List;

import com.nokia.tracebuilder.engine.TraceParameterPropertyDialogTemplate;
import com.nokia.tracebuilder.model.TraceModelExtension;
import com.nokia.tracebuilder.model.TraceParameter;

/**
 * Template for "this" pointer parameter.
 * 
 */
final class ThisPointerParameterTemplate implements
		TraceParameterPropertyDialogTemplate {

	/**
	 * Parameter name
	 */
	static final String PARAMETER_NAME = "this"; //$NON-NLS-1$

	/**
	 * Title for the template
	 */
	static final String UI_TITLE = Messages
			.getString("ThisPointerParameterTemplate.Title"); //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#getTitle()
	 */
	public String getTitle() {
		return UI_TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#getName()
	 */
	public String getName() {
		return PARAMETER_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#getValue()
	 */
	public String getValue() {
		return PARAMETER_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#getType()
	 */
	public String getType() {
		return TraceParameter.HEX32;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceParameterPropertyDialogTemplate#isTypeEnabled()
	 */
	public boolean isTypeEnabled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#isIDEnabled()
	 */
	public boolean isIDEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#isNameEnabled()
	 */
	public boolean isNameEnabled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#isValueEnabled()
	 */
	public boolean isValueEnabled() {
		return false;
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
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogTemplate#
	 *      createExtensions(java.util.List)
	 */
	public void createExtensions(List<TraceModelExtension> list) {
	}

}
