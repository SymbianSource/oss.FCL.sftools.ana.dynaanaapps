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

import java.util.Enumeration;
import java.util.Vector;

public abstract class GenericSampledTrace extends GenericTrace
{
	private static final long serialVersionUID = -5030416972125713453L;
	
	public Vector samples;
	
	public GenericSampledTrace()
	{
	  this.samples = new Vector();
	}
	
	public void addSample(GenericSample sample)
	{
	  this.samples.add(sample);
	}
	
	public Enumeration getSamples()
	{
	  return samples.elements();
	}
	
	public GenericSample getSample(int number)
	{
	  return (GenericSample)this.samples.elementAt(number);
	}
	
	public Vector<GenericSample> getSamplesInsideTimePeriod(long start, long end)
	{
		Enumeration sEnum = samples.elements();
		Vector<GenericSample> okSamples = new Vector<GenericSample>();
		
		while(sEnum.hasMoreElements())
		{
			GenericSample s = (GenericSample)sEnum.nextElement();
			if (s.sampleSynchTime >= start)
			{
				if (s.sampleSynchTime <= end)
				{
					okSamples.add(s);
				}
			}
		}
		
		return okSamples;
	}
	
	public GenericSample[] getSamplesForTime(long time)
	{
		// start the search from the middle of the sample set
		long location = this.samples.size() / 2;
		
		// next, add or remove 1/4 of the length of the sample set
		int div = 4;
		
		// boolean value indicating that the search proceeds now one step at a time
		boolean saturated = false;
		
		// search algorithm, proceed until a value is found or 
		// reached the boundaries of the search area

		// note that the sample numbers MUST increase with growing
		// indices within the samples vector in order for this 
		// algorithm to work!
		while (true)
		{
			// take the sample from the location
			GenericSample s = (GenericSample)samples.elementAt((int)location);
			
			// if the searched value is larger than the value at the proposed location
			// increment the proposed location with the length of the data set divided
			// by the divisor
			if (s.sampleSynchTime < time)
			{
				if (!saturated)
				{
					int change = this.samples.size() / div;
					if (change >= 1)
					{
						location += change;
						div *= 2;
					}
					else 
					{
						saturated = true;
					}
				}
				else
				{
					// already saturated, go one step at a time
					location++;
				}				
			}
			// similarly, decrement the proposed location 
			// if the value is smaller than the searched value
			else if (s.sampleSynchTime > time)
			{
				if (!saturated)
				{					
					int change = this.samples.size() / div;
					if (change >= 1)
					{
						location -= change;
						div *= 2;
					}
					else 
					{
						saturated = true;
					}
				}
				else
				{
					// already saturated, go one step at a time
					location--;
				}
			}
			else if (s.sampleSynchTime == time)
			{
				return this.getSamplesWithSameTimeFrom(location);
			}
			
			// reached the end of the sample set without a match
			if (location < 0 || location >= this.samples.size())
				return null;
		}
		
	}
	
	private GenericSample[] getSamplesWithSameTimeFrom(long location)
	{
		long sameTime = ((GenericSample)samples.elementAt((int)location)).sampleSynchTime;
		int lowBound = (int)location;
		int highBound = (int)location;
		
		if (lowBound-1 >=0)
		{	
			// go backwards as long as there are samples with the same samplenumber
			while ( ((GenericSample)samples.elementAt(lowBound - 1)).sampleSynchTime == sameTime 
					&& (lowBound - 1) >= 0)
			{
				lowBound--;
			}
		}
		
		if (highBound + 1 < this.samples.size())
		{	
			// go backwards as long as there are samples with the same samplenumber
			while ( ((GenericSample)samples.elementAt(highBound + 1)).sampleSynchTime == sameTime 
					&& (highBound + 1) < this.samples.size())
			{
				highBound++;
			}
		}
		
		GenericSample[] sa = new GenericSample[highBound - lowBound + 1];
		for (int i = 0; i < sa.length; i++)
		{
			sa[i] = (GenericSample)samples.elementAt(lowBound + i);
		}
		
		return sa;
	}
	
	public int getSampleAmount()
	{
	  return this.samples.size();
	}
	
	public int getFirstSampleNumber()
	{
		if (this.samples.size() > 0)
			return (int)this.getSample(0).sampleSynchTime;
		else
			return 0;
	}
	
	public int getLastSampleNumber()
	{
		if (this.samples.size() > 0)
			return (int)((GenericSample)this.samples.lastElement()).sampleSynchTime;
		else
			return 0;
	}
	
	public String toString()
	{
		Enumeration sEnum = this.getSamples();
	  	Vector strings = new Vector();
	  	int totalLength = 0;
	  	while(sEnum.hasMoreElements())
	  	{	
	  		GenericSample s = (GenericSample)sEnum.nextElement();
	  		String uus = s.toString() + "\n"; //$NON-NLS-1$
	  		totalLength += uus.getBytes().length;
	  		strings.add(uus);
	  	}
	  	
	  	System.out.println(Messages.getString("GenericSampledTrace.totalLength") + totalLength); //$NON-NLS-1$
	  	byte[] bytes = new byte[totalLength];
	  	sEnum = strings.elements();
	  	int index = 0;
	  	while(sEnum.hasMoreElements())
	  	{
	  		String s = (String)sEnum.nextElement();
	  		byte[] sB = s.getBytes();
	  		for (int i = index; i < index + sB.length; i++)
	  		{
	  			bytes[i] = sB[i - index];
	  		}
	  		index += sB.length;
	  	}
	  	
	  	String total = new String(bytes);
	  	return total;
	  }
}
