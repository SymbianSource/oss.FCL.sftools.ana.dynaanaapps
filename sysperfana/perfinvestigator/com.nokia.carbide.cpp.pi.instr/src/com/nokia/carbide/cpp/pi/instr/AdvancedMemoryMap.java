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
	
	public void InsertSample(IttSample sample)
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
				b.length = 0;
				b.offsetToCodeStart = 0;
				b.startAddress = sample.programCounter+4;
				b.type = null;
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
				int offset = (int)(sample.programCounter+4-(b.startAddress+b.offsetToCodeStart));
				pb = this.binReader.getProcessedBinaryForName(b.binaryName);
				if (pb != null)
				{
					f = pb.getFunctionForOffset(offset);
					if (f != null) 
					{
						f.startAddress = new Long(b.startAddress+f.offsetFromBinaryStart+pb.offsetToCodeStart);
						// function found ok
						// System.out.println(f.toString());
						return f;
					}
					
					// function not found in processed binary
					f = new Function(Messages.getString("AdvancedMemoryMap.functionForAddressNotFound1")+Long.toHexString(sample.programCounter)+Messages.getString("AdvancedMemoryMap.functionForAddressNotFound2"), //$NON-NLS-1$ //$NON-NLS-2$
							new Long(pb.startAddress),
							pb.binaryName);
					return f;
				}
			}
			
			// in all other cases
			f = new Function(Messages.getString("AdvancedMemoryMap.functionForAddressNotFound1")+Long.toHexString(sample.programCounter)+Messages.getString("AdvancedMemoryMap.functionForAddressNotFound2"), //$NON-NLS-1$ //$NON-NLS-2$
					new Long(sample.programCounter),
					Messages.getString("AdvancedMemoryMap.binaryForAddressNotFound1")+Long.toHexString(sample.programCounter)+Messages.getString("AdvancedMemoryMap.binaryForAddressNotFound2")); //$NON-NLS-1$ //$NON-NLS-2$
			return f;
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException(Messages.getString("AdvancedMemoryMap.notSupportedInV.1.22"));			 //$NON-NLS-1$
		}
	}
	
	public Function findFunctionForAddress(long address)
	{
		if (this.ittTrace122 == null)
		{
			return this.decided.getFunctionForAddress(address);
		}
		else
		{
			return this.ittTrace122.getFunctionForAddress(address,this.binaryReader122);
		}
	}
	
	public String findFunctionNameForAddress(long address)
	{
		Function f = this.findFunctionForAddress(address);
		if (f != null)
		{
			return f.functionName;
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
			return f.functionBinary.binaryName;
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
	
	public Binary findBinaryForAddress(long address)
	{
		if (this.ittTrace122 == null)
		{
			Function f = this.findFunctionForAddress(address);
			
			if (f != null)
				return f.functionBinary;
			else 
				return null;
		}
		else
		{
			Function f = this.ittTrace122.getFunctionForAddress(address,this.binaryReader122);
			
			if (f != null)
				return f.functionBinary;
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
				int offset = (int)(sample.programCounter+4-(b.startAddress+b.offsetToCodeStart));
				return this.binReader.getFunctionName(b.binaryName,offset);
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
