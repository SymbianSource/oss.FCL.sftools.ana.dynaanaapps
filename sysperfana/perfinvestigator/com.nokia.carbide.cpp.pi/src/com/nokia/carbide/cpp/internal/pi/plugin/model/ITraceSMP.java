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
package com.nokia.carbide.cpp.internal.pi.plugin.model;

/**
 * Interface for SMP-specific code
 *
 */
public interface ITraceSMP {
	/** Returns array of trace Ids found for SMP trace files. Note, this may not be null. */
	public int[] getTraceIdsSMP();

}