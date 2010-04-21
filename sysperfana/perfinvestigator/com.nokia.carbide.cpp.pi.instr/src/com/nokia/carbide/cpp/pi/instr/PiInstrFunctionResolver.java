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

import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.IBinary;
import com.nokia.carbide.cpp.internal.pi.model.IFunction;
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
	
	public IBinary findBinaryForAddress(long address, long sampleSynchTime) {
		IFunction f = this.findFunctionForAddress(address, sampleSynchTime);
		
		if (f != null)
			return f.getFunctionBinary();
		else 
			return null;
	}
	
	public IBinary findBinaryForAddress(long address) {
		return findBinaryForAddress(address, -1);
	}

	public String findBinaryNameForAddress(long address) {
		IFunction f = this.findFunctionForAddress(address);
		if (f != null)
		{
			return f.getFunctionBinary().getBinaryName();
		}
		else
		{
			return Messages.getString("PiInstrFunctionResolver.binaryNotFound"); //$NON-NLS-1$
		}
	}

	public IFunction findFunctionForAddress(long address, long sampleSynchTime) {
		IFunction f;
		f = this.rofsResolver.findFunctionForAddress(address);
		if (f == null) {	//$NON-NLS-1$
			f = this.ittTrace122.getFunctionForAddress(address, sampleSynchTime, this.binaryReader122);
		}
		return f;
	}

	public IFunction findFunctionForAddress(long address) {
		return findFunctionForAddress(address, -1);
	}

	
	public String findFunctionNameForAddress(long address) {
		IFunction f = this.findFunctionForAddress(address);
		if (f != null)
		{
			return f.getFunctionName();
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
