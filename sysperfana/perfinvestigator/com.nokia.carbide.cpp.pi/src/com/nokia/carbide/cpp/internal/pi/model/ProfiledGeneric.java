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

public abstract class ProfiledGeneric
{
    protected int index = -1;
    private String nameString;
    protected Color color;
    protected int totalSampleCount;
    protected int firstSample = -1;
    protected int lastSample = -1;
    private int   recentSample = -1;
    private int   recentSampleCount = 0;
    private int   recentPercentage = -1;
    protected int[] activityList;
    private int[] sampleList;
    private int[][] cumulativeList = new int[3][];
    
	// since each instance may be used by table viewers for multiple graphs,
	// keep per graph (for a maximum of 3 graphs):
    //	whether the thread/binary/function is currently enabled
    //	sample count in the selected graph area
    //	% load in the selected graph area
	//  string version of graph percent load
    protected boolean[] enableValue = {true, true, true};
	protected int[]   graphSampleCount = { 0, 0, 0 };
	protected float[] graphPercentLoad = { 0.0f, 0.0f, 0.0f };
    private String[]  averageLoadValueString = { "", "", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
    private PointList[] pointList  = {new PointList(), new PointList(), new PointList()};
    protected int[] activityP = null;
    private int[] activityT = null;
    protected int activityIndx = 0;
    
    public ProfiledGeneric()
    {
      // default to light gray
      this.color = ColorPalette.getColor(new RGB(192, 192, 192));
     }
    
    public void setNameString(String nameString)
    {
    	this.nameString = nameString;
    }
    
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
    
    public void setActivityMarkCount(int size)
    {
    	this.activityP = new int[size];
    	this.activityT = new int[size];
    }
    
    // zero the samples of this profiled generic in the time range starting at the time stamp
    public void zeroActivityMarkValues(int timeStamp)
    {
        addActivityMarkValues(timeStamp, 0, 0);
    }

    // set the samples of of this profiled generic in the time range starting at the time stamp
    public void addActivityMarkValues(int timeStamp, int percentage, int sampleCount)
    {
      this.activityT[this.activityIndx] = timeStamp;
      this.activityP[this.activityIndx] = percentage;

      if (this.firstSample == -1 && percentage != 0)
      {
    	  this.firstSample = this.recentSample;
      }

   	  if (this.recentSampleCount > 0)
   		  this.lastSample = timeStamp;

      this.recentSample      = timeStamp;
      this.recentSampleCount = sampleCount;
      this.recentPercentage  = percentage;
      this.activityIndx++;
    }

    public void addActivityMark(int timeStamp, int percentage)
    {
      this.activityT[this.activityIndx] = timeStamp;
      this.activityP[this.activityIndx] = percentage;
      this.activityIndx++;
    }

  	public void setupCumulativeList(int graphIndex)
  	{
  		if (this.activityIndx != 0)
  		{	
  			if (cumulativeList[graphIndex] == null)
  				cumulativeList[graphIndex] = new int[this.activityIndx];
  			else
  			{
  				for (int i = 0; i < cumulativeList[graphIndex].length; i++)
  				{
  					cumulativeList[graphIndex][i] = 0;
  				}
  			}
  		}
  	}

  	public int[] getCumulativeList(int graphIndex)
  	{
  		return this.cumulativeList[graphIndex];
  	}

  	public void setCumulativeValue(int graphIndex, int index, int value)
  	{
  		this.cumulativeList[graphIndex][index] = value;
  	}
  	
  	//returns x-coordinates
  	public int[] getSampleList()
  	{
  		if (sampleList == null && this.activityIndx != 0)
  		{
  			sampleList = new int[this.activityIndx];
 
  			for (int i = 0; i < this.activityIndx; i++)
  			{
  				this.sampleList[i] = this.activityT[i];
  			}
  			this.activityT = null;
  		}
  		return this.sampleList;
  	}
  	
  	// returns y-coordinates
  	public int[] getActivityList()
  	{
  		if (activityList == null && this.activityIndx != 0)
  		{
  			activityList = new int[this.activityIndx];

  			for (int i = 0; i < this.activityIndx; i++)
  			{
  			  activityList[i] = this.activityP[i];
  			}
  			this.activityP = null;
  		}

  		return this.activityList;
  	}

    public int getFirstSample()
    {
      if (this.firstSample == -1)
    	  return 0;
      else
    	  return this.firstSample;
    }

    public int getRealFirstSample()
    {
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

    private int findNearestSampleIndexFromStart(int sampleNum)
    {
    	int i = 0;
    	while (i < this.sampleList.length && this.sampleList[i] < sampleNum)
    	{
    		i++;
    	}    
    	return i;
    }

    private int findNearestSampleIndexFromEnd(int sampleNum)
    {
    	int i = sampleList.length - 1;
    	while (this.sampleList[i] > sampleNum && i > 0)
    	{
    		i--;
    	}
    	return i;
    }

    public float getAverageLoad(double startTime, double endTime)
    {
        return getAverageLoad((int) startTime,(int) endTime);
    }
    
    public float getAverageLoad(int startSample,int endSample)
    {
    	if (startSample == -1 || endSample == -1) return -666;
    	if (endSample < startSample) return -777;

    	if (this.activityList == null || this.sampleList == null) return -888;

    	int firstSampleIndx = 0;
    	int lastSampleIndx = 0;

    	firstSampleIndx = this.findNearestSampleIndexFromStart(startSample) + 1;
    	lastSampleIndx  = this.findNearestSampleIndexFromEnd(endSample) + 1;
   
    	if (firstSampleIndx < 0)
    		firstSampleIndx = 0;
    	if (firstSampleIndx >= activityList.length)
    		firstSampleIndx = activityList.length - 1;
    	
    	if (lastSampleIndx < 0)
    		lastSampleIndx = 0;
    	if (lastSampleIndx >= activityList.length)
    		lastSampleIndx = activityList.length - 1;
      
    	if (firstSampleIndx > lastSampleIndx)
    	{
    		int temp = firstSampleIndx;
    		firstSampleIndx = lastSampleIndx;
    		lastSampleIndx = temp;
    	}
    	
    	int totalTime = sampleList[lastSampleIndx - 1] - sampleList[firstSampleIndx - 1];
    	int totalLoad = 0;
    	
    	for (int i = firstSampleIndx; i < lastSampleIndx; i++)
    	{
    		totalLoad += this.activityList[i];
    	}
     	
    	totalLoad *= (sampleList[1] - sampleList[0]);

    	if (totalTime == 0)
    	{
    		return 0;
    	}
    	else
    	{
    		return (float)(((float)totalLoad) / ((float)totalTime));
    	}
    }
    
    public int getTotalLoad(int startSample,int endSample)
    {
    	if (startSample == -1 || endSample == -1) return -666;
    	if (endSample < startSample) return -777;

    	if (this.activityList == null || this.sampleList == null) return -888;

    	int firstSampleIndx = 0;
    	int lastSampleIndx = 0;

    	firstSampleIndx = this.findNearestSampleIndexFromStart(startSample) + 1;
    	lastSampleIndx  = this.findNearestSampleIndexFromEnd(endSample) + 1;
  
    	if (firstSampleIndx < 0)
    		firstSampleIndx = 0;
    	if (firstSampleIndx >= activityList.length)
    		firstSampleIndx = activityList.length - 1;
    	
    	if (lastSampleIndx < 0)
    		lastSampleIndx = 0;
    	if (lastSampleIndx >= activityList.length)
    		lastSampleIndx = activityList.length - 1;
      
    	if (firstSampleIndx > lastSampleIndx)
    	{
    		int temp = firstSampleIndx;
    		firstSampleIndx = lastSampleIndx;
    		lastSampleIndx = temp;
    	}
    	
    	int totalTime = sampleList[lastSampleIndx - 1] - sampleList[firstSampleIndx - 1];
    	int totalLoad = 0;
   	
    	for (int i = firstSampleIndx; i < lastSampleIndx; i++)
    	{
    		totalLoad += this.activityList[i];
    	}
     	
    	if (totalTime == 0)
    	{
    		return 0;
    	}
    	else
    	{
    		return (int) totalLoad;
    	}
    }
    
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

    public void setAverageLoadValueString(int graphIndex)
    {
    	this.averageLoadValueString[graphIndex] = (new DecimalFormat(Messages.getString("ProfiledGeneric.decimalFormat"))).format(this.graphPercentLoad[graphIndex]); //$NON-NLS-1$
    }

    public String getAverageLoadValueString(int graphIndex)
    {
    	return this.averageLoadValueString[graphIndex];
    }

    public void setColor(Color c)
    {
    	this.color = c;
    }

    public void setEnabled(int graphIndex, boolean enableValue)
    {
    	this.enableValue[graphIndex] = enableValue;
    }

    public boolean isEnabled(int graphIndex)
    {
    	return enableValue[graphIndex];
    }

    public Color getColor()
    {
    	return this.color;
    }
    
    public String getNameString()
    {
    	return this.nameString;
    }
    
    public int getTotalSampleCount()
    {
    	return this.totalSampleCount;
    }
    
    public void incTotalSampleCount()
    {
    	this.totalSampleCount++;
    }

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
	
	public void setSampleCounts(int sampleCount0, int sampleCount1, int sampleCount2)
	{
		this.graphSampleCount[0] = sampleCount0;
		this.graphSampleCount[1] = sampleCount1;
		this.graphSampleCount[2] = sampleCount2;
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
	
	public void setPercentLoads(float percentLoad0, float percentLoad1, float percentLoad2)
	{
		this.graphPercentLoad[0] = percentLoad0;
		this.graphPercentLoad[1] = percentLoad1;
		this.graphPercentLoad[2] = percentLoad2;

		// doesn't hurt to set the strings here, too
		// you may not need to set the strings when you set the float
		if (percentLoad0 >= 0.005f)
			this.averageLoadValueString[0] = (new DecimalFormat(Messages.getString("ProfiledGeneric.decimalFormat"))).format(percentLoad0); //$NON-NLS-1$
		else
			this.averageLoadValueString[0] = Messages.getString("ProfiledGeneric.zeroFormat"); //$NON-NLS-1$
		if (percentLoad1 >= 0.005f)
			this.averageLoadValueString[1] = (new DecimalFormat(Messages.getString("ProfiledGeneric.decimalFormat"))).format(percentLoad1); //$NON-NLS-1$
		else
			this.averageLoadValueString[1] = Messages.getString("ProfiledGeneric.zeroFormat"); //$NON-NLS-1$
		if (percentLoad2 >= 0.005f)
			this.averageLoadValueString[2] = (new DecimalFormat(Messages.getString("ProfiledGeneric.decimalFormat"))).format(percentLoad2); //$NON-NLS-1$
		else
			this.averageLoadValueString[2] = Messages.getString("ProfiledGeneric.zeroFormat"); //$NON-NLS-1$
	}
}
