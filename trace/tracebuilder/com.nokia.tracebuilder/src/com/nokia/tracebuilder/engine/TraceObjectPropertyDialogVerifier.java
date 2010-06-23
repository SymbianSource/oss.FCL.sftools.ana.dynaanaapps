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
* Interface which can be used by the view to verify dialog contents
*
*/
package com.nokia.tracebuilder.engine;

import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * Interface which can be used by the view to verify dialog contents while user
 * is changing them
 * 
 */
public interface TraceObjectPropertyDialogVerifier {

	/**
	 * Verifies the contents of the property dialog. The exception thrown by
	 * this method can be used to generate the error message
	 * 
	 * @throws TraceBuilderException
	 *             if verification fails
	 */
	public void verifyContents() throws TraceBuilderException;

}
