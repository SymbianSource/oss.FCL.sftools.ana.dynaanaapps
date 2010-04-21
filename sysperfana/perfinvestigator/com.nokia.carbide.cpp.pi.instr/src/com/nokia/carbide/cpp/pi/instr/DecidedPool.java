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
import java.util.Hashtable;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.Function;


public class DecidedPool 
{
    private boolean debug = false;
	private MemoryLayout memLayout;
	private AdvancedMemoryMap amm;
	
	public DecidedPool(AdvancedMemoryMap amm)
	{
		this.memLayout = new MemoryLayout();
		this.amm = amm;
	}
	
	public boolean insertSample(IttSample sample)
	{
		Vector binaries = this.memLayout.findBinariesForAddress(sample.programCounter);
		Enumeration binEnum = binaries.elements();
		
		Vector okBins = new Vector();
		while (binEnum.hasMoreElements())
		{
			Binary b = (Binary)binEnum.nextElement();
			if (this.trySampleWithBinary(sample,b,4) == true)
			{
				okBins.add(b);
			}
		}

		if (okBins.size() == 0)
		{
			// no matching binaries
			return false;
		}
		else if (okBins.size() == 1)
		{
			// homma bueno
			return true;
		}
		else if (okBins.size() > 1)
		{
			Vector okOkBins = new Vector();
			Enumeration okBinEnum = binaries.elements();
			while (okBinEnum.hasMoreElements())
			{
				Binary b = (Binary)okBinEnum.nextElement();
				if (this.trySampleWithBinary(sample,b,0) == true)
				{
					okOkBins.add(b);
				}
			}
			
			if (okOkBins.size() > 1)
			{
				Enumeration okOkBinEnum = okOkBins.elements();
				int winnerSupSamples = 0;
				while (okOkBinEnum.hasMoreElements())
				{
					Binary b = (Binary)okOkBinEnum.nextElement();
					DecidedLocation dl = this.memLayout.getDecidedLocationForBinary(b);
					if (dl.getSupportingSamples().size() > winnerSupSamples) 
					{
						winnerSupSamples = dl.getSupportingSamples().size();
					}
				}
			}
						
			return true;
		}
		else
		{
			if (debug)System.out.println(Messages.getString("DecidedPool.debugDecidedPool")); //$NON-NLS-1$
			return false;
		}
	}
	
	public boolean isThereAnyBinaryInMemoryIn(long address)
	{
		if (this.memLayout.findBinariesForAddress(address).size() == 0)
		{
			return false;
		}
		else 
		{
			return true;
		}
	}
	
	public Function getFunctionForAddress(long address)
	{
		Vector binaries = this.memLayout.findBinariesForAddress(address);
		Enumeration binEnum = binaries.elements();
		
		Binary winner = null;
		int winnerSupport = -100000;
		
		while (binEnum.hasMoreElements())
		{
			Binary b = (Binary)binEnum.nextElement();
			DecidedLocation dl = this.memLayout.getDecidedLocationForBinary(b);
			int support = dl.supportingSamples.size()-dl.nonSupportingSamples.size();
			if (support > winnerSupport)
			{
				winner = b;
				winnerSupport = support;
			}
		}
		if (winner != null)
		{
			ProcessedBinary pb = this.amm.getBinaryReader().
									getProcessedBinaryForName(winner.getBinaryName());
			
			
			String fName = pb.getFunctionNameForOffset(
							address-(winner.getStartAddress()+winner.getOffsetToCodeStart()));
			
			String bName = winner.getBinaryName();
			long fOffset = pb.getOffsetFromBinaryStartForFunction(fName);
			Long fStart = Long.valueOf(winner.getStartAddress()+winner.getOffsetToCodeStart()+fOffset);
			
			Function f = new Function(fName,fStart,bName);
			f.setLength(pb.getFunctionLengthForOffset(fOffset));
			
			return f;

		}
		
		return null;
	}
	
	public boolean trySampleWithBinary(IttSample sample, Binary b,int allowErrors)
	{
		if (b != null)
		{
			if (amm.getBinaryReader().checkSampleInBinary(sample,b,allowErrors) == true)
			{
				//System.out.println("Located 0x"+Long.toHexString(sample.programCounter)+" to "+b.binaryName);
				DecidedLocation dl = this.memLayout.getDecidedLocationForBinary(b);
				dl.insertSupportingSample(sample);

				return true;
			}
			else
			{
				// binary is not a match with the sample, register the sample as
				// a non-supporting sample. These will be processed at a later time

				DecidedLocation dl = this.memLayout.getDecidedLocationForBinary(b);
				dl.insertNonSupportingSample(sample);
				
				//System.out.println("Binary "+b.binaryName+" might be in a wrong place - n:"+
				//					dl.getNonSupportingSamples().size()+" s:"+dl.getSupportingSamples().size());

				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	public void insertDecidedBinary(Binary binary,Vector samples)
	{		
		Vector currentBins = this.memLayout.isOccupiedBy(binary.getStartAddress(),binary.getLength());
		
		if (currentBins.size() == 0)
		{
			if (debug) System.out.println(Messages.getString("DecidedPool.analysisResolved1")+binary.getBinaryName()+Messages.getString("DecidedPool.analysisResolved2")+Long.toHexString(binary.getStartAddress()+binary.getOffsetToCodeStart())+ //$NON-NLS-1$ //$NON-NLS-2$
					Messages.getString("DecidedPool.analysisResolved3")+Long.toHexString(binary.getStartAddress()+binary.getOffsetToCodeStart()+binary.getLength())); //$NON-NLS-1$
			
			DecidedLocation dl = new DecidedLocation(binary);
			dl.insertSupportingSamples(samples);
			
			this.memLayout.insertArea(dl);
		}
		else
		{
			Enumeration curEnum = currentBins.elements();
			while (curEnum.hasMoreElements())
			{
				Binary b = (Binary)curEnum.nextElement();

				boolean okForThis = true;
				int okCount = 0;
				Enumeration sampEnum = samples.elements();
				
				while (sampEnum.hasMoreElements())
				{
					IttSample is = (IttSample)sampEnum.nextElement();

					if (is.programCounter >= b.getStartAddress() && is.programCounter <= b.getStartAddress()+b.getLength())
					{
						if (this.trySampleWithBinary(is,b,4) == false) 
							okForThis=false;
						else
							okCount++;
					}
				}
				
				if (okForThis == true)
				{
					//System.out.println(okCount+" samples in "+binary.binaryName+" match to "+b.binaryName);
				}
			}
						
			DecidedLocation dl = new DecidedLocation(binary);
			dl.insertSupportingSamples(samples);
			
			this.memLayout.insertArea(dl);			
			
			/*
			System.out.println(overLap+" Area at "+Long.toHexString(currentBin.startAddress)+"-"
								+Long.toHexString(currentBin.startAddress+currentBin.length)
								+" is occupied by "
								+currentBin.binaryName+" - unable to locate "+binary.binaryName+" to "
								+Long.toHexString(binary.startAddress)+"-"
								+Long.toHexString(binary.startAddress+binary.length));
			*/
		}
	}
	
	public Enumeration getSupportingSamplesForBinary(Binary b)
	{
		DecidedLocation dl = this.memLayout.getDecidedLocationForBinary(b);
		if (dl != null)
		{
			return dl.supportingSamples.elements();			
		}
		else
		{
			return new Vector().elements();
		}
	}

	public Enumeration getNonSupportingSamplesForBinary(Binary b)
	{
		DecidedLocation dl = this.memLayout.getDecidedLocationForBinary(b);
		if (dl != null)
		{
			return dl.nonSupportingSamples.elements();			
		}
		else
		{
			return new Vector().elements();
		}
	}
	
	public Binary getBinaryWithName(String binaryName)
	{
		return this.memLayout.getBinaryWithName(binaryName);
	}
	
	public Binary findBinaryForSample(IttSample sample)
	{
		Vector binaries = this.memLayout.findBinariesForAddress(sample.programCounter+4);
		if (binaries == null) return null;
		if (binaries.size() == 0) return null;
		
		Vector okBins = new Vector();
		Enumeration enumer = binaries.elements();
		
		while (enumer.hasMoreElements())
		{
			Binary b = (Binary)enumer.nextElement();
			if (this.trySampleWithBinary(sample,b,6))
			{
				okBins.add(b);
			}
		}	
		
		if (okBins.size() <= 0)
		{
			if (binaries.size() == 1)
			{
				// there is only one binary at this location in memory,
				// guess that it is the one we are looking for even though
				// the samples don't match
				Binary binary = (Binary)binaries.firstElement();
				//System.out.println("Guessing that "+binary.binaryName+" is at 0x"+Long.toHexString(sample.programCounter));
				return binary;
			}
			else if (binaries.size() > 1)
			{
				Enumeration binEnum = binaries.elements();
				DecidedLocation bestGuess = this.memLayout.getDecidedLocationForBinary(
												(Binary)binEnum.nextElement());
				while (binEnum.hasMoreElements())
				{
					DecidedLocation dl = this.memLayout.getDecidedLocationForBinary(
											(Binary)binEnum.nextElement());
					
					if ( (dl.supportingSamples.size()-dl.nonSupportingSamples.size()) >
							(bestGuess.supportingSamples.size()-bestGuess.nonSupportingSamples.size()) )
					{
						bestGuess = dl;
					}
				}
				// return the binary that has most support for its location
				return bestGuess.binary;
			}
		}
		else if (okBins.size() == 1)
		{
			return (Binary)okBins.firstElement();
		}
		else if (okBins.size() > 1)
		{
			Enumeration okEnum = okBins.elements();
			
			Vector okOkVec = new Vector();
			
			while (okEnum.hasMoreElements())
			{
				Binary okBin = (Binary)okEnum.nextElement();
				if (this.trySampleWithBinary(sample,okBin,0) == true)
				{
					okOkVec.add(okBin);
				}
			}
			
			if (okOkVec.size() == 1) return (Binary)okOkVec.firstElement();
			
			else if (okOkVec.size() <= 0) return null;
			
			else if (okOkVec.size() > 1)
			{
					Enumeration okOkEnum = okOkVec.elements();
					
					Binary winner = null;
					int winnerSupSamples = 0;
					//System.out.println("Conflict: "+okOkVec.size()+" binaries");

					while (okOkEnum.hasMoreElements())
					{
						Binary b = (Binary)okOkEnum.nextElement();
						DecidedLocation dl = this.memLayout.getDecidedLocationForBinary(b);
						if (dl.getSupportingSamples().size() > winnerSupSamples) 
						{
							winnerSupSamples = dl.getSupportingSamples().size();
							winner = b;
							//System.out.println("bin: "+b.binaryName+""+dl.getSupportingSamples().size());
						}
					}
					/*
					if (winner != null)
						System.out.println("Winner: "+winner.binaryName+"\n");
					else
						System.out.println("NO WINNER???");
					*/
					return winner;
			}
		}
		
		return null;
	}
	
	private static class DecidedLocation
	{
		private Binary binary;
		private Vector supportingSamples;
		private Vector nonSupportingSamples;
		
		public DecidedLocation(Binary binary)
		{
			this.binary = binary;
			this.supportingSamples = new Vector();
			this.nonSupportingSamples = new Vector();
		}
		
		public void insertSupportingSample(IttSample sample)
		{
			this.supportingSamples.add(sample);
		}
		
		public void insertSupportingSamples(Vector samples)
		{
			Enumeration enumer = samples.elements();
			while (enumer.hasMoreElements())
			{
				IttSample s = (IttSample)enumer.nextElement();
				if (!this.supportingSamples.contains(s))
				{
					this.insertSupportingSample(s);
				}
			}
		}
		
		public void insertNonSupportingSample(IttSample sample)
		{
			if (!this.nonSupportingSamples.contains(sample))
			{
				this.nonSupportingSamples.add(sample);
			}
		}
		
		public Vector getSupportingSamples()
		{
			return this.supportingSamples;
		}

		public Vector getNonSupportingSamples()
		{
			return this.nonSupportingSamples;
		}
		
	}
	
	private static class MemoryLayout
	{
		public Hashtable decidedLocations;
		
		public MemoryLayout()
		{
			// keys are the binaries
			// decided locations are the elements
			this.decidedLocations = new Hashtable();
		}
		
		public void insertArea(DecidedLocation dl)
		{
			this.decidedLocations.put(dl.binary,dl);
		}
		
		public Vector isOccupiedBy(long start, long length)
		{
			Vector binaries = new Vector();
			
			Enumeration enumer = this.decidedLocations.keys();
			while (enumer.hasMoreElements())
			{
				Binary b = (Binary)enumer.nextElement();
				long bStart = b.getStartAddress();
				long bEnd = b.getStartAddress()+b.getLength();
				long end = start+length;
				
				// starts within the area
				if (start <= bEnd && start >= bStart)
				{
					binaries.add(b);
				}
					
				// ends within the area
				else if (end <= bEnd && end >= bStart)
				{
					binaries.add(b);
				}
				
				// is over the area
				else if (start <= bStart && end >= bEnd)
				{
					binaries.add(b);
				}

			}
			return binaries;
		}
		
		public DecidedLocation getDecidedLocationForBinary(Binary b)
		{
			if (this.decidedLocations.containsKey(b))
			{
				return (DecidedLocation)this.decidedLocations.get(b);
			}
			else
			{
				return null;
			}
		}
		
		public Vector findBinariesForAddress(long address)
		{
			Enumeration enumer = this.decidedLocations.keys();
			Vector binaries = new Vector();
			
			while (enumer.hasMoreElements())
			{
				Binary b = (Binary)enumer.nextElement();
				long bStart = b.getStartAddress();
				long bEnd = b.getStartAddress()+b.getLength();
				
				if (address >= bStart && address <= bEnd)
				{
					binaries.add(b);
				}
				
			}
			return binaries;
		}
		
		public Binary getBinaryWithName(String binaryName)
		{
			Enumeration dls = this.decidedLocations.keys();
			while (dls.hasMoreElements())
			{
				Binary b = (Binary)dls.nextElement();
				if (b.getBinaryName().toLowerCase().equals(binaryName.toLowerCase())) return b;
			}
			return null;
		}
		
	}
	
}
