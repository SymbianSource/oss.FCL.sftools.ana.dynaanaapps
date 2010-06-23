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
* Callback for select component dialog
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import java.util.Iterator;

import com.nokia.tracebuilder.engine.SoftwareComponent;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceObjectPropertyDialog;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceModel;

/**
 * Callback for select component dialog
 * 
 */
final class SelectComponentCallback extends PropertyDialogCallback {

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 */
	SelectComponentCallback(TraceModel model) {
		super(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.PropertyDialogManagerCallback#
	 *      okSelected(com.nokia.tracebuilder.engine.TraceObjectPropertyDialog)
	 */
	public void okSelected(TraceObjectPropertyDialog dialog)
			throws TraceBuilderException {
		int index = 0;
		String componentName = dialog.getTarget();
		Iterator<SoftwareComponent> components = TraceBuilderGlobals.getSoftwareComponents();
		while (components.hasNext()) {
			SoftwareComponent component = components.next();
			if (component.getName().equals(componentName)) {
				TraceBuilderGlobals.setCurrentSoftwareComponentIndex(index);
				break;
			}
			index++;
		}
	}

}
