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

public interface FunctionResolver 
{
	// find the function corresponding to an address
	public IFunction findFunctionForAddress(long address);
	
	// find the function corresponding to an address and a sample synch time
	public IFunction findFunctionForAddress(long address, long sampleSynchTime);

	// find the function name corresponding to an address
	public String findFunctionNameForAddress(long address);
	
	// find the binary corresponding to an address
	public IBinary findBinaryForAddress(long address);
	
	// find the binary corresponding to an address and a sample synch time
	public IBinary findBinaryForAddress(long address, long sampleSynchTime);

	// find the binary name corresponding to an address
	public String findBinaryNameForAddress(long address);
	
	// get the brief name of the resolver
	public String getResolverName();
	
	// get a long version of the name of the resolver
	public String getResolverString();
	
	// whether resolution will change anything (e.g., a symbol file resolver with no symbols should return false)
	public boolean canResolve();
}
