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
*/

package com.nokia.s60tools.crashanalyser.handlers;

import org.eclipse.core.commands.*;
import org.eclipse.swt.widgets.*;

import com.nokia.s60tools.crashanalyser.model.SourceSdkManager;

/**
 * A class for handling SDK selection. 
 *
 */
public class SdkSelectionHandler extends AbstractHandler {

	/**
	 * This method gets called when user selects an SDK from a pop-up menu.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Event e = (Event)event.getTrigger();
		MenuItem item = (MenuItem)e.widget;
		String sdkName = item.getText();
		SourceSdkManager.setSdk(sdkName);
		return null;
	}
}