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
 * Trim Updater
 *
 */
package com.nokia.traceviewer.eventhandler;

import com.nokia.traceviewer.engine.TrimProvider;

/**
 * Trim Updater
 */
public final class TrimUpdater implements TrimProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.TrimProvider#updateText(java.lang.String)
	 */
	public void updateText(String text) {
		TrimInformation.setTextToLabel(text);
	}
}
