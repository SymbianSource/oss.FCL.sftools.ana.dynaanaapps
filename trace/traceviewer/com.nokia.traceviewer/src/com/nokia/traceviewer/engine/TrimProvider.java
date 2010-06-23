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
 * TrimProvider shows messages in Trim area
 *
 */
package com.nokia.traceviewer.engine;

/**
 * TrimProvider shows messages in Trim area
 */
public interface TrimProvider {

	/**
	 * Updates text to TrimProvider
	 * 
	 * @param text
	 *            the new text
	 */
	public void updateText(String text);
}
