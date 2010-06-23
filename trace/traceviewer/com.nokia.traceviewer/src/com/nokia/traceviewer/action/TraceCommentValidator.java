/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Trace comment validator
 *
 */
package com.nokia.traceviewer.action;

import org.eclipse.jface.dialogs.IInputValidator;

/**
 * Trace comment validator
 */
public class TraceCommentValidator implements IInputValidator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
	 */
	public String isValid(String str) {
		String ret = null;
		if (str == null || str.equals("")) { //$NON-NLS-1$
			ret = ""; //$NON-NLS-1$
		}
		return ret;
	}

}
