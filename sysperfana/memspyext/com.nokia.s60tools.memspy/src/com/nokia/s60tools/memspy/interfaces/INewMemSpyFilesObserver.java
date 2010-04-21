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



package com.nokia.s60tools.memspy.interfaces;

/**
 * interface INewMemSpyFilesObserver.
 * interface for MemSpy file observer.
 *
 */

public interface INewMemSpyFilesObserver {
	
		/**
		 * memSpyFilesUpdated.
		 * Method that is called when MemSPy file scan is complete.
		 */
		public void memSpyFilesUpdated();
		
		/**
		 * setOutputFilePath.
		 * sets output file's path
		 * @param outputFilePath new outputFilePath
		 */
		public void setOutputFilePath(String outputFilePath);
	
}
