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

package com.nokia.carbide.cpp.pi.power;

import java.util.ArrayList;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;


public class PwrTrace extends GenericSampledTrace
{
	private static final long serialVersionUID = -2398791759386296139L;
	
	transient private PowerTraceGraph[] graphs = null;
	transient private boolean complete = false;
	transient private ArrayList<PwrSample> powerChangePoints;	// times when power samples change
	transient private long maxEndTime;
	
    private float voltage = 3.7f;
	
    // used when manually positioning the trace
    public int offset = 0; 
	
    public float batterySize = 1500f;
    
    private double scale = 0.0;
	private double min = 0.0;
	private double max = 0.0;
	private double synchValue = 0.0;
	
	transient private int[] sampleTimes;
    transient private int[] ampValues;
    transient private int[] voltValues;
    transient private int[] capaValues;
    transient private double maxAmps = Double.MIN_VALUE;
	transient private double minAmps = Double.MAX_VALUE;
	transient private double maxPower = 0.0;

    public GenericTraceGraph getTraceGraph(int graphIndex, int uid)
	{		
		return getPowerGraph(graphIndex, uid);
	}
	
	public PowerTraceGraph getPowerGraph(int graphIndex, int uid)
	{
		if (graphs == null) {
			graphs = new PowerTraceGraph[3];
			maxEndTime = (long) ((PIPageEditor.currentPageEditor().getMaxEndTime() + .0005) * 1000); 
		}

		// note that graphIndex needs not match the index sent to GppTraceGraph
		if (   (graphIndex == PIPageEditor.THREADS_PAGE)
			|| (graphIndex == PIPageEditor.BINARIES_PAGE)
			|| (graphIndex == PIPageEditor.FUNCTIONS_PAGE)) {
			if (graphs[graphIndex] == null)
				graphs[graphIndex] = new PowerTraceGraph(graphIndex, uid, this);
			return graphs[graphIndex];
		}
	
		return null;
	}

    public float getBatterySize() 
	{
        return batterySize;
    }

    public void setBatterySize(float newVal) 
	{
        batterySize = newVal;
    }

    public int getOffset() 
	{
        return offset;
    }

    public float getVoltage()
    {
    	return voltage;
    }

	public int[] getSampleTimes()
    {
    	return sampleTimes;
    }

	public int[] getAmpValues()
    {
    	return ampValues;
    }

    public int[] getVoltValues()
    {
    	return voltValues;
    }

    public int[] getCapaValues()
    {
    	return capaValues;
    }

    public double getMaxAmps()
    {
    	return maxAmps;
    }

    public double getMinAmps()
    {
    	return minAmps;
    }

	public double getMaxPower()
    {
    	return maxPower;
    }

	public void setVoltage(float voltage)
    {
    	this.voltage = voltage;
    }
    
    public void setOffset( int newOffset ) 
	{
		int mySize = getSampleAmount();
		
		if( newOffset > mySize )
			offset = mySize;
		else if( (newOffset * -1) > mySize )
			offset = (-1)*mySize;
		else
			offset = newOffset;
    }
    
	public double getScale()
	{
		return scale;
	}
	
	public double getMin()
	{
		return min;
	}
	
	public double getMax()
	{
		return max;
	}
	
	public long getMaxEndTime()
	{
		return maxEndTime;
	}
	
	public double getSynchValue()
	{
		return synchValue;
	}
	
	public void setSynchValue(double aSynchValue)
	{
		synchValue = aSynchValue;
	}
	
	public void addSample(PwrSample sample)
	{
		this.samples.add(sample);
	}

	public PwrSample getPwrSample(int number)
	{
		return (PwrSample)this.samples.elementAt(number);
	}

	// compensate for different sampling speeds. 
	// the software traces are done at 1KHz and the 
	// power traces are typically done at 250Khz.
	//
	public void initData()
	{
		ArrayList<PwrSample> powerChangePoints = new ArrayList<PwrSample>();
		PwrSample sample = (PwrSample) this.samples.get(0);
			
		long   currentTime     = sample.sampleSynchTime;
		double currentCurrent  = sample.current;
		double currentVoltage  = sample.voltage;
		double currentCapacity = sample.capacity;
				
		for (int i = 1; i < this.samples.size(); i++) {
			sample = (PwrSample) this.samples.get(i);
			if (   sample.current  != currentCurrent
				|| sample.voltage  != currentVoltage
				|| sample.capacity != currentCapacity) {
				PwrSample addSample = new PwrSample(currentTime, currentCurrent, currentVoltage, currentCapacity);
				powerChangePoints.add(addSample);
				currentTime     = sample.sampleSynchTime;
				currentCurrent  = sample.current;
				currentVoltage  = sample.voltage;
				currentCapacity = sample.capacity;
			}
		}
		
		sample = powerChangePoints.get(powerChangePoints.size() - 1);
		if (   sample.current  != currentCurrent
			|| sample.voltage  != currentVoltage
			|| sample.capacity != currentCapacity) {
			powerChangePoints.add(new PwrSample(currentTime, currentCurrent, currentVoltage, currentCapacity));
		}

		this.powerChangePoints = powerChangePoints;
		
		sampleTimes = new int[this.powerChangePoints.size()];
        ampValues   = new int[this.powerChangePoints.size()];
        voltValues  = new int[this.powerChangePoints.size()];
        capaValues  = new int[this.powerChangePoints.size()];

		int localMaxAmps = Integer.MIN_VALUE;
		int localMinAmps = Integer.MAX_VALUE;

		for (int i = 0; i < this.powerChangePoints.size(); i++)
	    {
			PwrSample tmp = this.powerChangePoints.get(i);
			sampleTimes[i] = (int)Math.abs(tmp.sampleSynchTime);		
			ampValues[i]   = (int)Math.abs(tmp.current);	// in milliamps
			voltValues[i]  = (int)Math.abs(tmp.voltage);	// in millivolts
			capaValues[i]  = (int)Math.abs(tmp.capacity);

			if (ampValues[i] > localMaxAmps)
			{
				localMaxAmps = ampValues[i];
			}
			else if (ampValues[i] < minAmps )
			{
				localMinAmps = ampValues[i];
			}
		}
	    
	    maxAmps = localMaxAmps;
	    minAmps = localMinAmps;

	    scaleMaxPower();
	}
	
	public ArrayList<PwrSample> getPowerChangePoints()
	{
		return this.powerChangePoints;
	}
	
	public void scaleMaxPower()
	{
		maxPower = maxAmps * this.voltage;

		if (maxPower < 10)
	    	maxPower =     10;
		else if (maxPower <     20)
	    	maxPower =     20;
	    else if (maxPower <     50)
	    	maxPower =     50;
	    else if (maxPower <    100)
	    	maxPower =    100;
	    else if (maxPower <    200)
	    	maxPower =    200;
	    else if (maxPower <    500)
	    	maxPower =    500;
	    else if (maxPower <   1000)
	    	maxPower =   1000;
	    else if (maxPower <   2000)
	    	maxPower =   2000;
	    else if (maxPower <   5000)
	    	maxPower =   5000;
	    else if (maxPower <  10000)
	    	maxPower =  10000;
	    else if (maxPower <  20000)
	    	maxPower =  20000;
	    else if (maxPower <  50000)
	    	maxPower =  50000;
	    else if (maxPower < 100000)
	    	maxPower = 100000;
	}

  	/*
  	 * Check if the power trace is complete (first sample is at time 1, sample N is at time N)
  	 */
  	public void setComplete()
  	{
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
  		int size;
  		
  		this.complete = true;
  		
   		size = this.samples.size();
  		for (int i = 0; i < size; i++) {
  			PwrSample element = (PwrSample)this.samples.get(i);
  			if (element.sampleSynchTime != i*samplingInterval) {
  				this.complete = false;
  				break;
  			}
  		}
  	}
  	
  	public boolean isComplete() {
  		return this.complete;
  	}
}
