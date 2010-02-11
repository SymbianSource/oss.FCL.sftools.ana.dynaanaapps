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

package com.nokia.carbide.cpp.pi.instr;

import java.io.File;

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.resolvers.RofsSymbolFileFunctionResolver;
import com.nokia.carbide.cpp.pi.importer.SampleImporter;


public class PiInstrFunctionResolver implements FunctionResolver {
	private IttTrace122 ittTrace122;
	private BinaryReader122 binaryReader122;
	private RofsSymbolFileFunctionResolver rofsResolver;
	
	boolean ableToResolve = false;
	
	String notFound = com.nokia.carbide.cpp.internal.pi.resolvers.Messages.getString("CachedFunctionResolver.notFound"); //$NON-NLS-1$
	
	public PiInstrFunctionResolver(BinaryReader122 binaryReader122,IttTrace122 ittTrace122,int parsedMapFileCount)
	{
		this.binaryReader122 = binaryReader122;
		this.ittTrace122 = ittTrace122;
		this.rofsResolver = new RofsSymbolFileFunctionResolver();

		if (ittTrace122.getEvents().size() > 0 || parsedMapFileCount > 0)
			this.ableToResolve = true;
		
		//reads the symbol name path from configuration data
		String[] list = SampleImporter.getInstance().getRofsSymbolFileList();
		for (String filename : list) {
            File rofsSymbolFile = new File(filename);
            if (rofsSymbolFile.exists()) {
            	rofsResolver.parseAndProcessSymbolFile(rofsSymbolFile);
            	this.ableToResolve = true;
            }
		}
		rofsResolver.adjustRuntimeBinary(binaryReader122.getHostNameToBinary());		
	}
	
	public Binary findBinaryForAddress(long address) {
		Function f = this.findFunctionForAddress(address);
		
		if (f != null)
			return f.functionBinary;
		else 
			return null;
	}

	public String findBinaryNameForAddress(long address) {
		Function f = this.findFunctionForAddress(address);
		if (f != null)
		{
			return f.functionBinary.binaryName;
		}
		else
		{
			return Messages.getString("PiInstrFunctionResolver.binaryNotFound"); //$NON-NLS-1$
		}
	}

	public Function findFunctionForAddress(long address) {
		Function f;
		f = this.rofsResolver.findFunctionForAddress(address);
		if (f == null) {	//$NON-NLS-1$
			f = this.ittTrace122.getFunctionForAddress(address,this.binaryReader122);
		}
		return f;
	}

	public String findFunctionNameForAddress(long address) {
		Function f = this.findFunctionForAddress(address);
		if (f != null)
		{
			return f.functionName;
		}
		else
		{
			return Messages.getString("PiInstrFunctionResolver.functionNotFound"); //$NON-NLS-1$
		}
	}

	public String getResolverName() {
		return "ITT"; //$NON-NLS-1$
	}

	public String getResolverString() {
		return Messages.getString("PiInstrFunctionResolver.resolverITT"); //$NON-NLS-1$
	}
	
	public boolean canResolve() {
		return this.ableToResolve;
	}
}
