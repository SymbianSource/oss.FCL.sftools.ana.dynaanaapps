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
* Label provider for CheckListEntry
*
*/
package com.nokia.tracebuilder.view;

import org.eclipse.jface.viewers.LabelProvider;

import com.nokia.tracebuilder.engine.CheckListDialogEntry;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Label provider for CheckListEntry
 * 
 */
final class CheckListSelectionDialogLabelProvider extends LabelProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		String retval;
		if (element instanceof CheckListDialogEntry) {
			Object o = ((CheckListDialogEntry) element).getObject();
			if (o instanceof TraceObject) {
				retval = ((TraceObject) o).getName();
			} else {
				retval = o.toString();
			}
		} else {
			retval = ""; //$NON-NLS-1$
		}
		return retval;
	}
}
