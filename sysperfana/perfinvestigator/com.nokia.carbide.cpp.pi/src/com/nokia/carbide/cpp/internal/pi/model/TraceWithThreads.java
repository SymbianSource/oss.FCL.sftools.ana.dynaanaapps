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

/*
 * TraceWithThreads.java
 */
package com.nokia.carbide.cpp.internal.pi.model;

public interface TraceWithThreads 
{
	  public GenericThread[] getThreads();
//	  public long getLastSynchTimeForThread(GenericThread t);
//	  public long getFirstSynchTimeForThread(GenericThread t);
}
