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
package com.nokia.s60tools.swmtanalyser.ui.actions;

import org.eclipse.jface.viewers.ISelection;

/**
 * Interface for provide selection that User has been made in UI 
 */
public interface ISelectionProvider {
	

	/**
	 * Get selection that user has made in UI
	 * @return selection made in UI
	 */
	public ISelection getUserSelection();
	

		
}
