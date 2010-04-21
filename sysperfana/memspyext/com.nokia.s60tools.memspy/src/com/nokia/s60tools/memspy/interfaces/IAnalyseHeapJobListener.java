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
 * IAnalyseHeapJobListener.
 * interface for Heap Analyser launcher listener.
 */
public interface IAnalyseHeapJobListener {
	
	/**
	 * heapAnalyserFinished.
	 * Methot that is called when Heap Aanalyser finishes
	 * @param returnValue return value of Heap Analyser
	 */
	public void heapAnalyserFinished(final int returnValue);
	
	/**
	 * heapAnalyserFinished.
	 * Methot that is called when Heap Aanalyser finishes
	 * @param returnValue return value of Heap Analyser
	 * @param outputFilePath file path of output file
	 */
	public void heapAnalyserFinished(final int returnValue, String outputFilePath);

}
