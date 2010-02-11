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

package com.nokia.s60tools.crashanalyser.interfaces;

/**
 * Observer which is used to inform when there are new crash files available
 * or when a crash files have been updated. E.g. MainForm will get these notifications
 * so that it knows to update main grid.
 *
 */
public interface INewCrashFilesObserver {
	public void crashFilesUpdated();
}
