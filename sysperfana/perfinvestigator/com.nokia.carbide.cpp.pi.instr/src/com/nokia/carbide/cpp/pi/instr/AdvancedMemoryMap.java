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

import java.util.Enumeration;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.IBinary;


public class AdvancedMemoryMap implements FunctionResolver
{
    private boolean debug = false;
	private UndecidedPool unDecided;
	private DecidedPool decided;
	private BinaryReader binReader;
	
	private IttTrace122 ittTrace122;
	private BinaryReader122 binaryReader122;
	
	// whether the resolver has enough information to resolve anything
	protected boolean ableToResolve = true;
	
	public AdvancedMemoryMap(BinaryReader122 binaryReader122,IttTrace122 ittTrace122)
	{
		this.binaryReader122 = binaryReader122;
		this.ittTrace122 = ittTrace122;
	}
	
	public AdvancedMemoryMap(BinaryReader myBinaryReader)
	{
		this.binReader = myBinaryReader;
		this.decided = new DecidedPool(this);
		this.unDecided = new UndecidedPool(this);
	}
	
	public UndecidedPool getUndecidedPool()
	{
		return this.unDecided;
	}

	public DecidedPool getDecidedPool()
	{
		return this.decided;
	}

	public BinaryReader getBinaryReader()
	{
		return this.binReader;
	}
	
	public void insertSample(IttSample sample)
	{
		if (this.ittTrace122 == null)
		{
			// first check whether there is any binary in the location
			// the sample is
			boolean memStat = this.decided.isThereAnyBinaryInMemoryIn(sample.programCounter);
			
			if (memStat == true)
			{
				// there is a binary / binaries
				// in the memory at the location
				// where the sample is located
				
				this.decided.insertSample(sample);
				// result tells whether the sample was binary compatible
				// with any of the binaries. It is not important here though, as
				// the sample should have a match also with its correct binary if
				// it is present - consequent samples will then strenghten the 
				// position of the binary
			}
			
			else if (memStat == false)
			{
				// no, this sample has to be inserted to the
				// undecided pool
				
				this.unDecided.insertSample(sample,10);
			}
			
			if (this.unDecided.getUnprocessed().size() > 0)
			{
				//System.out.println("Unprocessed "+this.unDecided.getUnprocessed().size());
				Vector unProcVec = this.unDecided.getUnprocessed();
				Enumeration enumer = unProcVec.elements();
				while(enumer.hasMoreElements())
				{
					IttSample s = (IttSample)enumer.nextElement();
					// try the undecided samples again
					this.decided.insertSample(s);
				}
			}
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException(Messages.getString("AdvancedMemoryMap.notSupportedInV.1.22"));			 //$NON-NLS-1$
		}
	}
	
	public void postProcess()
	{
		if (this.ittTrace122 == null)
		{
			
			for (int i=9;i>0;i--)
			{		
				Vector undecided = unDecided.getUndecided();
				if (debug) System.out.println(Messages.getString("AdvancedMemoryMap.unmatchedSize")+undecided.size()); //$NON-NLS-1$
				
				Enumeration enumer = undecided.elements();
				while(enumer.hasMoreElements())
				{
					IttSample is = (IttSample)enumer.nextElement();
					
					if (this.decided.insertSample(is) == false)
						this.unDecided.insertSample(is,i);
					
				}
			}
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException(Messages.getString("AdvancedMemoryMap.notSupportedInV.1.22"));			 //$NON-NLS-1$
		}
	}
	
	public Binary findBinaryForSample(IttSample sample)
	{
		if (this.ittTrace122 == null)
		{
			Binary b = this.decided.findBinaryForSample(sample);
			if (b != null)
			{
				return b;
			}
			else
			{
				b = new Binary(Messages.getString("AdvancedMemoryMap.binaryForAddressNotFound1")+Long.toHexString(sample.programCounter)+Messages.getString("AdvancedMemoryMap.binaryForAddressNotFound2")); //$NON-NLS-1$ //$NON-NLS-2$
				b.setLength(0);
				b.setOffsetToCodeStart(0);
				b.setStartAddress( sample.programCounter+4);
				b.setType(null);
				return b;
			}
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException(Messages.getString("AdvancedMemoryMap.notSupportedInV.1.22"));			 //$NON-NLS-1$
		}
	}
	
	public Function findFunctionForSample(IttSample sample)
	{
		if (this.ittTrace122 == null)
		{
			Function f;
			ProcessedBinary pb = null;
			
			Binary b = this.decided.findBinaryForSample(sample);
			if (b != null)
			{
				int offset = (int)(sample.programCounter+4-(b.getStartAddress()+b.getOffsetToCodeStart()));
				pb = this.binReader.getProcessedBinaryForName(b.getBinaryName());
				if (pb != null)
				{
					f = pb.getFunctionForOffset(offset);
					if (f != null) 
					{
						f.setStartAddress(Long.valueOf(b.getStartAddress()+f.getOffsetFromBinaryStart()+pb.getOffsetToCodeStart()));
						// function found ok
						// System.out.println(f.toString());
						return f;
					}
					
					// function not found in processed binary
					f = new Function(Messages.getString("AdvancedMemoryMap.functionForAddressNotFound1")+Long.toHexString(sample.programCounter)+Messages.getString("AdvancedMemoryMap.functionForAddressNotFound2"), //$NON-NLS-1$ //$NON-NLS-2$
							Long.valueOf(pb.getStartAddress()),
							pb.getBinaryName());
					return f;
				}
			}
			
			// in all other cases
			f = new Function(Messages.getString("AdvancedMemoryMap.functionForAddressNotFound1")+Long.toHexString(sample.programCounter)+Messages.getString("AdvancedMemoryMap.functionForAddressNotFound2"), //$NON-NLS-1$ //$NON-NLS-2$
					Long.valueOf(sample.programCounter),
					Messages.getString("AdvancedMemoryMap.binaryForAddressNotFound1")+Long.toHexString(sample.programCounter)+Messages.getString("AdvancedMemoryMap.binaryForAddressNotFound2")); //$NON-NLS-1$ //$NON-NLS-2$
			return f;
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException(Messages.getString("AdvancedMemoryMap.notSupportedInV.1.22"));			 //$NON-NLS-1$
		}
	}
	
	public Function findFunctionForAddress(long address, long sampleSynchTime)
	{
		if (this.ittTrace122 == null)
		{
			return this.decided.getFunctionForAddress(address);
		}
		else
		{
			return this.ittTrace122.getFunctionForAddress(address, sampleSynchTime, this.binaryReader122);
		}
	}
	
	public Function findFunctionForAddress(long address){
		return findFunctionForAddress(address, -1);
	}
	
	public String findFunctionNameForAddress(long address)
	{
		Function f = this.findFunctionForAddress(address);
		if (f != null)
		{
			return f.getFunctionName();
		}
		else
		{
			return Messages.getString("AdvancedMemoryMap.functionNotFound"); //$NON-NLS-1$
		}
	}
	
	public String findBinaryNameForAddress(long address)
	{
		Function f = this.findFunctionForAddress(address);
		if (f != null)
		{
			return f.getFunctionBinary().getBinaryName();
		}
		else
		{
			return Messages.getString("AdvancedMemoryMap.binaryNotFound"); //$NON-NLS-1$
		}
	}
	
	public String getResolverName()
	{
		return "ITT"; //$NON-NLS-1$
	}

	public String getResolverString()
	{
		return Messages.getString("AdvancedMemoryMap.resolverITT"); //$NON-NLS-1$
	}
	
	public IBinary findBinaryForAddress(long address)
	{
		return findBinaryForAddress(address, -1);
	}
	
	public IBinary findBinaryForAddress(long address, long sampleSynchTime)
	{
		if (this.ittTrace122 == null)
		{
			Function f = this.findFunctionForAddress(address);
			
			if (f != null)
				return f.getFunctionBinary();
			else 
				return null;
		}
		else
		{
			Function f = this.ittTrace122.getFunctionForAddress(address, sampleSynchTime, this.binaryReader122);
			
			if (f != null)
				return f.getFunctionBinary();
			else 
				return null;
		}
	}

	public String findFunctionNameForSample(IttSample sample)
	{
		if (this.ittTrace122 == null)
		{
			Binary b = this.decided.findBinaryForSample(sample);
			if (b != null)
			{
				int offset = (int)(sample.programCounter+4-(b.getStartAddress()+b.getOffsetToCodeStart()));
				return this.binReader.getFunctionName(b.getBinaryName(),offset);
			}
			else 
			{
				return Messages.getString("AdvancedMemoryMap.functionForAddressNotFound1")+Long.toHexString(sample.programCounter)+Messages.getString("AdvancedMemoryMap.functionForAddressNotFound2"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException(Messages.getString("AdvancedMemoryMap.notSupportedInV.1.22")); //$NON-NLS-1$
		}
	}
	
	public Enumeration getSupportingSamples(Binary b)
	{
		if (this.ittTrace122 == null)
		{
			
			return this.decided.getSupportingSamplesForBinary(b);
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException(Messages.getString("AdvancedMemoryMap.notSupportedInV.1.22"));			 //$NON-NLS-1$
		}
		
	}
	
	public Enumeration getNonSupportingSamples(Binary b)
	{
		if (this.ittTrace122 == null)
		{
			
			return this.decided.getNonSupportingSamplesForBinary(b);
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException(Messages.getString("AdvancedMemoryMap.notSupportedInV.1.22"));			 //$NON-NLS-1$
		}
		
	}
	
	public Binary getBinary(String binaryName)
	{		
		if (this.ittTrace122 == null)
		{
			return this.decided.getBinaryWithName(binaryName);
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException(Messages.getString("AdvancedMemoryMap.notSupportedInV.1.22"));			 //$NON-NLS-1$
		}
	}

	public boolean canResolve() {
		return this.ableToResolve;
	}
}
