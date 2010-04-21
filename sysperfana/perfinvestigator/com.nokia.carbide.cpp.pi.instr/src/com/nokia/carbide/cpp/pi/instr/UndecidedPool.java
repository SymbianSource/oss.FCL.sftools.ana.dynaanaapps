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


public class UndecidedPool 
{	
	private Hashtable undecidedLocations;
	private Vector unprocessed;
	private AdvancedMemoryMap amm;
	private Vector unDecided;
	
	public UndecidedPool(AdvancedMemoryMap amm)
	{
		this.amm = amm;
		// keys will be the binary names, and values wile be
		// their corresponding UndecidedLocation objects
		this.undecidedLocations = new Hashtable();
		
		this.unprocessed = new Vector();
		this.unDecided = new Vector();
	}
	
	public Vector getUnprocessed()
	{
		return this.unprocessed;
	}
	
	public void clearUnprocessed()
	{
		this.unprocessed = new Vector();
	}
	
	public Vector getUndecided()
	{
		return this.unDecided;
	}

	public void insertSample(IttSample sample,int decisionBoundary)
	{
		if (!this.unDecided.contains(sample))
			this.unDecided.add(sample);
		
		BinaryReaderResult brr = this.amm.getBinaryReader().findSequence(sample);
		for (int i=0;i<brr.possibleBinaries.length;i++)
		{
			Binary possibleBinary = brr.possibleBinaries[i];
			UndecidedLocation ul = 
				(UndecidedLocation)undecidedLocations.get(possibleBinary.getBinaryName());
			
			if (ul != null)
			{
				ul.addSample(sample,possibleBinary.getStartAddress());
			}
			else
			{
				ul = new UndecidedLocation(possibleBinary); 
				ul.addSample(sample,possibleBinary.getStartAddress());
				this.undecidedLocations.put(possibleBinary.getBinaryName(),ul);
			}
			
			// the value of decisionBoundary has to be reduced in the future
			// when no binaries can be resolved with the current value
			if (ul.getDecidedAddress(decisionBoundary) != -1)
			{
			 	// location of the binary has been resolved
				
				// refresh the samples that belong to the decision
				ul.refreshDecidedAndUndecidedSamples();
				
				// add all the samples that didn't fit to this binary, to
				// the pool of unprocessed samples				
				Vector undecided = ul.getUndecidedSamples();
				// add only those samples that are not present in the undecided
				// vector already
				Enumeration unEnum = undecided.elements();
				while(unEnum.hasMoreElements())
				{
					IttSample us = (IttSample)unEnum.nextElement();
					if (!this.unprocessed.contains(us)) this.unprocessed.add(us);
				}
				
				// then, pass the decision to the decidedPool
				Vector decided = ul.getDecidedSamples();
				this.amm.getDecidedPool().insertDecidedBinary(possibleBinary,decided);
				
				// then, remove the decided samples from all other possible binaries
				Enumeration renum = decided.elements();
				while(renum.hasMoreElements())
				{
					IttSample sampleToRemove = (IttSample)renum.nextElement();
					this.removeSampleFromAllPossibleLocations(sampleToRemove);
				}
			}
		}
	}
	
	private void removeSampleFromAllPossibleLocations(IttSample sample)
	{	
		Enumeration renum = this.undecidedLocations.keys();
		Vector removeList = new Vector();
		
		this.unDecided.remove(sample);
		
		while(renum.hasMoreElements())
		{
			String name = (String)renum.nextElement();
			
			UndecidedLocation ul = (UndecidedLocation)this.undecidedLocations.get(name);
			ul.removeSample(sample);
			
			if (ul.samplesForLocations.size() == 0) removeList.add(name);
		}
		
		if (removeList.size() > 0)
		{
			Enumeration enumer = removeList.elements();
			while(enumer.hasMoreElements())
			{
				this.undecidedLocations.remove(enumer.nextElement());
			}
		}
		
		if (this.unprocessed.contains(sample))
		{
			this.unprocessed.remove(sample);
		}
	} 
	
	private static class UndecidedLocation
	{
		private Hashtable samplesForLocations;

		private Binary binary;
		
		private int processedMostHits = 0;
		private Long addressWithMostHits = null;
		private Vector samplesWithMostHits = null;
		
		private boolean processedUndecided = false;
		private Vector undecidedSamples = null;
		private Vector decidedSamples = null; 
		
		public UndecidedLocation(Binary binary)
		{
			// key will be the address, and the value will be a
			// vector with all samples that support that location 
			this.samplesForLocations = new Hashtable();
			this.binary = binary;
		}
		
		public Binary getBinary()
		{
			return this.binary;
		}
		
		public void addSample(IttSample sample,long startAddress)
		{
			// clear the flags so that the values will be recalculated if needed
			this.processedMostHits = 0;
			this.processedUndecided = false;
			
			Object o = this.samplesForLocations.get(new Long(startAddress));
			
			if (o != null)
			{
				// this location has already a vector associated with it
				Vector sampleVec = (Vector)o;
				
				Enumeration sampleEnum = sampleVec.elements();
				boolean isAlready = false;
				
				while(sampleEnum.hasMoreElements())
				{
					IttSample testSample = (IttSample)sampleEnum.nextElement();
					if (testSample.programCounter == sample.programCounter && 
							testSample.checksum == sample.checksum)
						{
							isAlready = true;
							break;
						}
				}
				// add the sample only if it is 
				// different from the other samples
				if (isAlready == false) 
				{
					//System.out.println("Added unique sample "+sample.programCounter+" to "+this.binary.binaryName);
					sampleVec.add(sample);
				}
				else
				{
					//System.out.println("Discarded "+sample.programCounter+" to "+this.binary.binaryName);
				}
				
			}
			else
			{
				//System.out.println("Added unique sample "+sample.programCounter+" to "+this.binary.binaryName);
				Vector sampleVec = new Vector();
				sampleVec.add(sample);
				samplesForLocations.put(new Long(startAddress),sampleVec);
			}
		}
		
		public void removeSample(IttSample sample)
		{
			// clear the flags so that the values will be recalculated if needed
			this.processedMostHits = 0;
			this.processedUndecided = false;

			// remove this sample from all vectors, remove the vectors if they
			// become empty
			Enumeration enumer = this.samplesForLocations.keys();
			Vector removeList = new Vector();
			
			while(enumer.hasMoreElements())
			{
				Long address = (Long)enumer.nextElement();
				Vector v = (Vector)this.samplesForLocations.get(address);
				
				if (v.contains(sample)) 
				{
					v.remove(sample);
					// do not remove the vector yet because
					// it would mess the hashtable iteration
					if (v.size() == 0) removeList.add(address);
				}
			}
			
			// finally, remove the vectors that 
			// were emptied after removing the last sample
			if (removeList.size() > 0) 
			{
				Enumeration renum = removeList.elements();
				while(renum.hasMoreElements())
				{
					this.samplesForLocations.remove(renum.nextElement());
				}
			}
		
		}
		
		public long getDecidedAddress(int minSampleAmount)
		{
			if (minSampleAmount <= 0) return -1;
			
			if (this.processedMostHits == minSampleAmount)
			{
				// the function has been run with this value
				return this.addressWithMostHits.longValue();
			}
			
			Enumeration addresses = samplesForLocations.keys();
			
			int amountOfMaxValues = 0;
			
			while(addresses.hasMoreElements())
			{
				Long address = (Long)addresses.nextElement();
				Vector samples = (Vector)samplesForLocations.get(address);
				if (samples.size() >= minSampleAmount)
				{
					// there are at least minSampleAmount different samples
					// that support this binary being in this place
					if (samplesWithMostHits != null)
					{
						// there is already a value, check do we exceed it
						if (samples.size() > samplesWithMostHits.size())
						{
							samplesWithMostHits = samples;
							addressWithMostHits = address;
							amountOfMaxValues = 1;
						}
						else if (samples.size() == samplesWithMostHits.size())
						{
							amountOfMaxValues++;
						}
					}
					else
					{
						// there is no max value yet
						samplesWithMostHits = samples;
						addressWithMostHits = address;
						amountOfMaxValues = 1;
					}
				}
			}
			
			// this function does not need to be run again
			// if the values do not change
			this.processedMostHits = minSampleAmount;

			// there are two or more max values
			// or no max values at all
			if (amountOfMaxValues != 1) return -1;
				else return addressWithMostHits.longValue();
		}
		
		public void refreshDecidedAndUndecidedSamples()
		{
			if (this.processedUndecided == true) return;
			
			this.undecidedSamples = new Vector();
			this.decidedSamples = new Vector();
			
			Enumeration enumer = this.samplesForLocations.elements();
			while(enumer.hasMoreElements())
			{
				Vector v = (Vector)enumer.nextElement();
				
				if (!v.equals(this.samplesWithMostHits))
				{
					// count all samples but the one with 
					undecidedSamples.addAll(v);
				}
				else
				{
					// add the decided samples
					this.decidedSamples.addAll(v);
				}
			}
	
			this.processedUndecided = true;
		}
		
		public Vector getDecidedSamples()
		{
			// there is no value according to which the decision
			// could base on
			if (this.processedMostHits == 0) return new Vector();

			this.refreshDecidedAndUndecidedSamples();
			
			return this.decidedSamples;
		}
		
		public Vector getUndecidedSamples()
		{
			// there is no value according to which the decision
			// could base on
			if (this.processedMostHits == 0) return new Vector();

			this.refreshDecidedAndUndecidedSamples();			
			
			return this.undecidedSamples;
		}
	}
	
}
