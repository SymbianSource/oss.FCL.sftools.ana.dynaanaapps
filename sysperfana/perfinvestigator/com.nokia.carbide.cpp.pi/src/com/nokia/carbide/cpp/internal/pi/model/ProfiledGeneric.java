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

import java.text.DecimalFormat;

import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.nokia.carbide.cpp.pi.util.ColorPalette;

/**
 * Generic class for a profiled element, such as a thread, a binary or a function
 *
 */
public abstract class ProfiledGeneric
{
	/** number of graphs */
	protected static final int GRAPH_COUNT = 3;
	
	/**
	 * Unique ordinal for this profiled element, given on creation. The first created profiled element
	 * of this kind has an index of zero, each following is incremented by one.
	 */
    protected int index = -1;
    /** Name of this profiled element */
    private String nameString;
    /** Colour with which this profiled element is represented in any graph or legend view*/
    protected Color color;
    /** overall sample count of this profiled element in the trace */
	protected int totalSampleCount;
	/** timestamp of the first sample for this profiled element */
    protected int firstSample = -1;
	/** timestamp of the last sample for this profiled element */
    protected int lastSample = -1;

	// since each instance may be used by table viewers for multiple graphs,
	// keep per graph (for a maximum of 3 graphs):
    //	whether the thread/binary/function is currently enabled
    //	sample count in the selected graph area
    //	% load in the selected graph area
	//  string version of graph percent load
    
    /**
  	 * The cumulative list contains for each bucket the 
  	 * cumulative sample percentage of the same bucket of all ProfiledGenerics
  	 * so far processed (not including the values of the current ProfiledGeneric).
  	 * 
  	 * In other words it's containing the bottom or start value of this
  	 * profiled element to draw on the graph (the graph drawn just underneath the 
  	 * graph of this profiled element)
  	 */
    private float[][] cumulativeList;
    /** checkbox enabled state of this profiled element for each of the graphs */
    protected boolean[] enableValue;
    /** current sample count of this profiled element for each of the graphs, changes with enabled state, selected time frame, master filtering */
	protected int[]   graphSampleCount;
    /** current sample percentage load of this profiled element for each of the graphs, changes with enabled state, selected time frame, master filtering */
	protected float[] graphPercentLoad;
    /** current sample average load of this profiled element for each of the graphs, changes with enabled state, selected time frame, master filtering */
    private String[]  averageLoadValueString; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	
    /** PointList for each graphs polyline */
    private PointList[] pointList;
    
    //  bucket values 
    //
    /** holds the sample count value for all buckets, calculated as one-off when trace is first processed */
    private int[] activityN = null;
    /** holds percentage value for all buckets, calculated as one-off when trace is first processed */
    protected float[] activityP = null;
    /** holds start timestamp of all buckets, calculated as one-off when trace is first processed */
    private int[] activityT = null;
    /** the index of the bucket currently updated */
    protected int activityIndx = 0;
    
    /** total number of CPUs in the system; 1 for non-SMP trace */
	private int cpuCount;

    //for SMP
    protected boolean isSMP;
	protected int[] totalSampleCountSMP;
    
    protected float[][] activityPSMP;
    private int[][] activityNSMP;
    
	/**
	 * Constructor
	 * @param cpuCount number of CPUs present in the trace
	 * @param graphCount number of graphs to display
	 */
	public ProfiledGeneric(int cpuCount, int graphCount) {
		if (graphCount < 0){
			throw new IllegalArgumentException("graphCount must be greater than 0.");
		}
		// default to light gray
		this.color = ColorPalette.getColor(new RGB(192, 192, 192));
		this.cpuCount = cpuCount;
		
		// init variables for values per graph
		cumulativeList = new float[graphCount][];
		enableValue = new boolean[graphCount];
		graphSampleCount = new int[graphCount];
		graphPercentLoad = new float[graphCount];
		averageLoadValueString = new String[graphCount];
		pointList = new PointList[graphCount];
		for (int graph = 0; graph < graphCount; graph++) {
		    enableValue[graph] = true;
			averageLoadValueString[graph] = ""; //$NON-NLS-1$
			pointList[graph] = new PointList();
		}

		if (cpuCount > 1) { // SMP

			isSMP = true;
			totalSampleCountSMP = new int[cpuCount];

			activityPSMP = new float[cpuCount][];
			activityNSMP = new int[cpuCount][];
		}
	}
    
    /**
     * Setter for the name of the profiled element
     * @param nameString
     */
    public void setNameString(String nameString)
    {
    	this.nameString = nameString;
    }
    
    /**
     * Adds a point to the PointList of the given graph
     * @param graphIndex the graph to use
     * @param x the X-coordinate of the point
     * @param y the Y-coordinate of the point
     */
    public void addPointToPolyline(int graphIndex, int x, int y)
    {
    	pointList[graphIndex].addPoint(x, y);
    }
    
    public void resetPolyline(int graphIndex)
    {
    	pointList[graphIndex].removeAllPoints();
    }
    
    public PointList getPointList(int graphIndex)
    {
    	return pointList[graphIndex];
    }
    
    public int getIndex()
    {
    	return this.index;
    }
    
    /**
     * Creates bucket arrays. This also propagates to SMP bucket arrays.  
     * @param numberOfBuckets The total number of buckets to use.
     */
    public void createBuckets(int numberOfBuckets){
    	this.activityP = new float[numberOfBuckets];
    	this.activityT = new int[numberOfBuckets];
    	this.activityN = new int[numberOfBuckets];
    	if (isSMP){
			for (int cpu = 0; cpu < cpuCount; cpu++) {
		    	this.activityPSMP[cpu] = new float[numberOfBuckets];
		    	this.activityNSMP[cpu] = new int[numberOfBuckets];
			}						    		
    	}
    }
    
    /**
     * this is intended to initialise the newly created bucket arrays
     * @param duration length of time each bucket represents 
     */
    public void initialiseBuckets(int duration){
    	for (int i = 0; i < activityT.length; i++) {
			//this is important for calculating the x values later, so use mid-bucket values
    		activityT[i] = duration*i + duration / 2;
		}
    }
    
    /**
     * Updates the bucket percentage values. This requires that sample counts have
     * already been calculated for each bucket.
     * @param bucketTotalArr Total number of samples per bucket
     * @param bucketTotalArrSMP total number of samples per cpu and bucket. May be null for non-SMP systems.
     */
    public void calculateBucketPercentages(int[] bucketTotalArr, int[][] bucketTotalArrSMP){
    	if (activityN != null){ 
        	for (int b = 0; b < activityN.length; b++) {//loop through sample counts per bucket
        		if (bucketTotalArr[b] > 0){
            		activityP[b] = (float)activityN[b]*100/bucketTotalArr[b];
        			
        		}
        		
        		activityIndx = activityP.length; //TODO: temporarily needed, remove later
        		
    			if (isSMP){
    				for (int cpu = 0; cpu < bucketTotalArrSMP.length; cpu++) {
    					if (bucketTotalArrSMP[cpu][b]>0){
    						activityPSMP[cpu][b] = (float)activityNSMP[cpu][b]*100/bucketTotalArrSMP[cpu][b];
    					}
					}
    			}
    		}    		
    	}	    		
    }
    
    /**
     * Increases total sample count as well as sample count for the given bucket. Takes care of SMP values
     * if SMP trace.
     * @param bucketIndex The index of the bucket to use
     * @param timestamp the timestamp of the sample
     * @param cpu the CPU number of the sample
     */
    public void increaseSampleCount(int bucketIndex, int timestamp, int cpu){
    	//note, activityT (bucket times) is constant and has been set during initialisation
    	//activityP (bucket percentages) will be calculated after all samples have been processed
    	
		incTotalSampleCount();
		if (isSMP){
			incTotalSampleCountForSMP(cpu);				
		}
		
    	this.activityN[bucketIndex]++; //increase sample count in bucket
    	if (firstSample == -1){
    		firstSample = timestamp;
    	}
    	lastSample = timestamp;
    	if (isSMP){
    		this.activityNSMP[cpu][bucketIndex]++;
    	}
    }

  	public void setupCumulativeList(int graphIndex)
  	{
  		if (this.activityT != null && activityT.length != 0)
  		{	
  			if (cumulativeList[graphIndex] == null)
  				cumulativeList[graphIndex] = new float[this.activityT.length];
  			else
  			{
  				for (int i = 0; i < cumulativeList[graphIndex].length; i++)
  				{
  					cumulativeList[graphIndex][i] = 0;
  				}
  			}
  		}
  	}

  	/**
  	 * The cumulative list contains for each bucket the 
  	 * cumulative sample percentage of the same bucket of all ProfiledGenerics
  	 * so far processed (not including the values of the current ProfiledGeneric).
  	 * 
  	 * Typically used for drawing a stacked-area chart
  	 * @param graphIndex the graph ordinal to use
  	 * @return
  	 */
  	public float[] getCumulativeList(int graphIndex)
  	{
  		return this.cumulativeList[graphIndex];
  	}

  	public void setCumulativeValue(int graphIndex, int index, float value)
  	{
  		this.cumulativeList[graphIndex][index] = value;
  	}
  	
  	/**
  	 * returns x-coordinates
  	 * @return
  	 */
  	public int[] getSampleList()
  	{
  		if (activityT != null){
  			//Arrays.copyOf() is only supported from Java 6
  			//return Arrays.copyOf(this.activityT, this.activityT.length);
  			int[] ret = new int[activityT.length];
  			for (int i = 0; i < ret.length; i++) {
				ret[i] = activityT[i];
			}
  			return ret;
  		}
  		return null;
  	}
  	
  	/**
  	 *  returns y-coordinates
  	 * @return
  	 */
  	public float[] getActivityList(){
  		if (activityP != null){
  			//Arrays.copyOf() is only supported from Java 6
  			//return Arrays.copyOf(this.activityP, this.activityP.length);
  			float[] ret = new float[activityP.length];
  			for (int i = 0; i < ret.length; i++) {
				ret[i] = activityP[i];
			}
  			return ret;
  		}
  		return null;
   	}
  	
  	/**
  	 *  returns y-coordinates (percentage values of activity)
  	 * @param cpu 
  	 * @return
  	 */
  	public float[] getActivityListForSMP(int cpu)
  	{
  		if (isSMP && this.activityPSMP[cpu] != null){
  			//Arrays.copyOf() is only supported from Java 6
  			//return Arrays.copyOf(activityPSMP[cpu], activityPSMP[cpu].length);
  			float[] ret = new float[activityPSMP[cpu].length];
  			for (int i = 0; i < ret.length; i++) {
				ret[i] = activityPSMP[cpu][i];
			}
  			return ret;
  		}
  		return null;
  	}

    public int getFirstSample()
    {
      if (this.firstSample == -1)
    	  return 0;
      else
    	  return this.firstSample;
    }


    public int getLastSample()
    {
      if (this.lastSample == -1)
    	  return 0;
      else
    	  return this.lastSample;
    }

    public int getRealLastSample()
    {
   	  return this.lastSample;
    }
    
    public void setLastSample(int lastSample)
    {
    	this.lastSample = lastSample;
    }
    
    /**
     * Setter for this ordinal of this profiled element
     * @param index the ordinal to set
     */
    public void setIndex(int index)
    {
    	this.index = index;
    }
    
    public void setAverageLoadValueString(int graphIndex, String valueString)
    {
    	this.averageLoadValueString[graphIndex] = valueString;
    }

    public void setAverageLoadValueString(int graphIndex, float value)
    {
    	this.averageLoadValueString[graphIndex] = (new DecimalFormat(Messages.getString("ProfiledGeneric.decimalFormat"))).format(value); //$NON-NLS-1$
    }


    public String getAverageLoadValueString(int graphIndex)
    {
    	return this.averageLoadValueString[graphIndex];
    }

    /**
     * Setter for this colour of this profiled element
     * @param c the colour to set
     */
    public void setColor(Color c)
    {
    	this.color = c;
    }

    /**
     * Sets enabled state for this profiled element in the given graph
     * @param graphIndex the graph index to use
     * @param enableValue true for enabled, false for disabled
     */
    public void setEnabled(int graphIndex, boolean enableValue)
    {
    	this.enableValue[graphIndex] = enableValue;
    }

    /**
     * Checks enabled state for this profiled element in the given graph
     * @param graphIndex the graph index to use
     * @return true if enabled, false if disabled
     */
    public boolean isEnabled(int graphIndex)
    {
    	return enableValue[graphIndex];
    }

    /**
     * @return the colour of this profiled element
     */
    public Color getColor()
    {
    	return this.color;
    }
    
    /**
     * @return the name of this profiled element
     */
    public String getNameString()
    {
    	return this.nameString;
    }
    
    /**
     * @return the total sample count of this profiled element over the entire trace data
     */
    public int getTotalSampleCount()
    {
    	return this.totalSampleCount;
    }
    
    /**
     * Increased the total sample count of this profiled element by one
     */
   public void incTotalSampleCount()
    {
    	this.totalSampleCount++;
    }
    
    /**
     * returns the total sample count over the entire trace data for the given CPU
     * @param cpu the CPU to use
     * @return the total sample count for given CPU
     */
    public int getTotalSampleCountForSMP(int cpu)
    {
    	return this.totalSampleCountSMP[cpu];
    }
    
    /**
     * Increases the total sample count for the given CPU by one
     * @param cpu the CPU to use
     */
    public void incTotalSampleCountForSMP(int cpu)
    {
    	this.totalSampleCountSMP[cpu]++;
    }

	/**
	 * Getter for the given graph's sample count
	 * @param graphIndex
	 * @return
	 */
	public int getSampleCount(int graphIndex)
	{
		return this.graphSampleCount[graphIndex];
	}
	
	public void setSampleCount(int graphIndex, int sampleCount)
	{
		this.graphSampleCount[graphIndex] = sampleCount;
	}
	
	public void incSampleCount(int graphIndex)
	{
		this.graphSampleCount[graphIndex]++;
	}
	
	public float getPercentLoad(int graphIndex)
	{
		return this.graphPercentLoad[graphIndex];
	}
	
	public void setLoadAndString(int graphIndex, float percentLoad)
	{
		this.graphPercentLoad[graphIndex] = percentLoad;

		// doesn't hurt to set the string here, too.
		// you may not need to set the string when you set the float
		if (percentLoad >= 0.005f)
			this.averageLoadValueString[graphIndex] = (new DecimalFormat(Messages.getString("ProfiledGeneric.decimalFormat"))).format(percentLoad); //$NON-NLS-1$
		else
			this.averageLoadValueString[graphIndex] = Messages.getString("ProfiledGeneric.zeroFormat"); //$NON-NLS-1$
	}

	
}
