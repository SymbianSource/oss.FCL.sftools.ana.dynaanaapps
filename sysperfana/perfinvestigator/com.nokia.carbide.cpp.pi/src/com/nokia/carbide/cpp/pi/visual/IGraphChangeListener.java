/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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
package com.nokia.carbide.cpp.pi.visual;

/**
 * Listener to changes in a graph component
 */
public interface IGraphChangeListener {
	
	/**
	 * Indicates that the subject is now either hidden or shown. 
	 * @param visible true if revealed, false if hidden
	 */
	public void onVisiblityChanged(boolean visible);
	
	/**
	 * Indicates that the graph title has changed
	 * @param newTitle the new title
	 */
	public void onTitleChange(final String newTitle);
}
