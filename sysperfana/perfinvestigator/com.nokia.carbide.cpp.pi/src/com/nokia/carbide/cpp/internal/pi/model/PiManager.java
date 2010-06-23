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

package com.nokia.carbide.cpp.internal.pi.model;

import com.nokia.carbide.cpp.internal.pi.analyser.PIChangeEvent;



public final class PiManager {
	private static PiManager manager;
	
	private PiManager() {
	}
	
   ///////////// IPiItem accessors ///////////////////////

	public static PiManager getManager() {
		if (manager == null)
			manager = new PiManager();
		return manager;
	}
	
	public void synchronizePI() {
		PIChangeEvent.action("synchronise"); //$NON-NLS-1$
	}

	

	

	

   
}
