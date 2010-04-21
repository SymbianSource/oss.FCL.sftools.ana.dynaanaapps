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
package com.nokia.carbide.cpp.internal.pi.address;

import org.eclipse.swt.widgets.Composite;

import com.nokia.carbide.cpp.pi.address.AddrThreadTable;
import com.nokia.carbide.cpp.pi.address.GppTraceGraph;

/**
 * 
 * AddrThreadTable implementation for SMP
 */
public class AddrThreadTableSMP extends AddrThreadTable{

	/**
	 * Constructor
	 * @param myGraph the parent graph
	 * @param parent the parent composite
	 * @param adapter a GppModelAdapter to ensure calls into the trace model are SMP specific
	 */
	public AddrThreadTableSMP(GppTraceGraph myGraph, Composite parent, GppModelAdapter adapter) {
		super(myGraph, parent, adapter);
	}

}
