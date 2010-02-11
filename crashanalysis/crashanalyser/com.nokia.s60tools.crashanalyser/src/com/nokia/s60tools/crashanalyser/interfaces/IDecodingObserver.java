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

import com.nokia.s60tools.crashanalyser.files.*;
/**
 * Observer which is used to inform when decoding of crash files is
 * finished.
 * 
 */
public interface IDecodingObserver {
	public void decodingFinished(String error, CrashAnalyserFile decodedFile);
}
