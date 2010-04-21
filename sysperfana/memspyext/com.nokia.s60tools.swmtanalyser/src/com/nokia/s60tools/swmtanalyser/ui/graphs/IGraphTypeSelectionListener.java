/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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
package com.nokia.s60tools.swmtanalyser.ui.graphs;

/**
 * Implementors of this interface can be notified when selected graph type changes
 * and implementor can do necessary actions based on the changed selection. 
 */
public interface IGraphTypeSelectionListener {

	/**
	 * Listeners are notified when user changes the selection of threads, 
	 */
	public void notifyThreadsSelection();
	/**
	 * Listeners are notified when user changes the selection of chunks.
	 */
	public void notifyChunksSelection();
	/**
	 * Listeners are notified when user changes the selection of disks.
	 */
	public void notifyDisksSelection();
	/**
	 * Listeners are notified when user changes the selection of elements to be shown.
	 */
	public void notifySysElementsSelection();
	
}
