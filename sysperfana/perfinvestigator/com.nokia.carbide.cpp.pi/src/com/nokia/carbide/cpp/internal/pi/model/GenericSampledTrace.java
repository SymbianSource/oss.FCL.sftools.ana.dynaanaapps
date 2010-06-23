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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public abstract class GenericSampledTrace extends GenericTrace
{
	private static final long serialVersionUID = 5248402326692863890L;
	
	/** highest sync time within samples */
	private long lastSampleTime;
	
	/** the samples */
	public Vector<GenericSample> samples;
	
	/**
	 * Constructor
	 */
	public GenericSampledTrace()
	{
	  this.samples = new Vector<GenericSample>();
	}
	
	/**
	 * Adds the given sample to the samples collection
	 * @param sample the sample to add
	 */
	public void addSample(GenericSample sample)
	{
	  this.samples.add(sample);
	  if (sample.sampleSynchTime > lastSampleTime){
		  lastSampleTime = sample.sampleSynchTime;
	  }
	}
	
	/**
	 * Returns the highest sampleSyncTime found in the set of samples.
	 * 
	 * @return last sample time
	 */
	public long getLastSampleTime(){
		if (lastSampleTime == 0 && samples.size()>0){
			//someone has added samples without going through the addSample() API in this class
			//really, this.samples should be private
			//let's try to recalculate lastSampleTime
			for (GenericSample s : samples) {
				  if (s.sampleSynchTime > lastSampleTime){
					  lastSampleTime = s.sampleSynchTime;
				  }
			}
		}
		return lastSampleTime;
	}
	
	/**
	 * returns all samples as an enumeration
	 * @return
	 */
	public Enumeration<GenericSample> getSamples()
	{
	  return samples.elements();
	}
	
	/**
	 * Gets the nth element of the collection of samples  
	 * @param number the index to look for
	 * @return the found sample (or ArrayIndexOutOfBoundsException)
	 */
	public GenericSample getSample(int number)
	{
	  return (GenericSample)this.samples.elementAt(number);
	}
	
	/**
	 * Returns samples inside the given time period (including the boundaries).
	 * @param start the start time at which to include samples
	 * @param end the end time at which to include samples
	 * @return samples included in the time period
	 */
	public Vector<GenericSample> getSamplesInsideTimePeriod(long start, long end)
	{
		Enumeration<GenericSample> sEnum = samples.elements();
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

	/**
	 * Returns all samples with the given sync time
	 * @param time the time to look for
	 * @return array of samples for given sync time
	 */
	public GenericSample[] getSamplesForTime(long time)	{
		List<GenericSample> resList = new ArrayList<GenericSample>();
		
		//find the sample just before the search result
		GenericSample sample = new MockGenericSample();
		sample.sampleSynchTime = time-1;
		int pos = Collections.binarySearch(samples, sample, new Comparator<GenericSample>(){
			public int compare(GenericSample s1, GenericSample s2) {
				return (int)(s1.sampleSynchTime - s2.sampleSynchTime);
			}
		});
		
		//go to the first match
		pos = pos < 0  ? (-(pos)-1) : pos+1;
		
		if (pos < samples.size()){
			//collect matches
			for (int i = pos; i < samples.size(); i++) {
				GenericSample found = samples.elementAt(i);
				if (found.sampleSynchTime > time){
					break;
				} else if (found.sampleSynchTime == time){
					resList.add(found);
				}
			}
		}
		
		return resList.toArray(new GenericSample[resList.size()]);
	}	
	
	/**
	 * Returns the overall number of samples
	 * @return
	 */
	public int getSampleAmount()
	{
	  return this.samples.size();
	}
	
	/**
	 * Returns the sync time of the first sample, or 0.
	 * @return
	 */
	public int getFirstSampleNumber()
	{
		if (this.samples.size() > 0)
			return (int)this.getSample(0).sampleSynchTime;
		else
			return 0;
	}
	
	/**
	 * Returns the highest sample time in the trace. Note, on SMP systems,
	 * this may not be the time of the last sample.
	 * @return
	 */
	public int getLastSampleNumber()
	{
		return (int)getLastSampleTime();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		Enumeration<GenericSample> sEnum = this.getSamples();
	  	Vector<String> strings = new Vector<String>();
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
	  	Enumeration<String> sEnumString = strings.elements();
	  	int index = 0;
	  	while(sEnumString.hasMoreElements())
	  	{
	  		String s = (String)sEnumString.nextElement();
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
	
	// this class is only needed for binary search
	@SuppressWarnings("serial")
	class MockGenericSample extends GenericSample {
	}
	
}
