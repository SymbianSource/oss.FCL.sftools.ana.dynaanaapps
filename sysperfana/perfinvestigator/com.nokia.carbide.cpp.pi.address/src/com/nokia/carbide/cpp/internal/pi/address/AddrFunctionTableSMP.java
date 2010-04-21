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

import com.nokia.carbide.cpp.pi.address.AddrFunctionTable;
import com.nokia.carbide.cpp.pi.address.GppTraceGraph;

/**
 * 
 * AddrFunctionTable implementation for SMP
 */
public class AddrFunctionTableSMP extends AddrFunctionTable{

	/**
	 * Constructor
	 * @param myGraph the parent graph
	 * @param parent the parent composite
	 */
	public AddrFunctionTableSMP(GppTraceGraph myGraph, Composite parent, GppModelAdapter adapter) {
		super(myGraph, parent, adapter);
	}

}
