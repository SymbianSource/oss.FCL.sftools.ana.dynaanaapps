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

import java.io.Serializable;

import org.eclipse.core.runtime.Platform;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.pi.PiPlugin;


public class ProfiledThread extends ProfiledGeneric implements Serializable, ICPUScaleAdapter
{
	private static final long serialVersionUID = 20150633093396772L;

	private char name; //char symbol of the thread

	private int threadId; //thread's real id
	
	private float[] calculatedActivity;
 
	public ProfiledThread(int cpuCount, int graphCount)
	{
		super(cpuCount, graphCount);
	}
  
	public void setNameValues(char symbol,String nameString)
	{
		this.name = symbol;
		setNameString(nameString);
	}
 
	//unused?
//	public String toString(int graphIndex)
//	{
//		if (this.isEnabled(graphIndex))
//		{
//			return "true  " + this.getAverageLoadValueString(graphIndex) + getNameString(); //$NON-NLS-1$
//		}
//		else
//		{
//			return "false " + this.getAverageLoadValueString(graphIndex) + getNameString(); //$NON-NLS-1$
//		}
//	}
  
	public Character getName(){
		return new Character(this.name);
	}

	public int getThreadId() 
	{
		return threadId;
	}

	public void setThreadId(int threadId) 
	{
		this.threadId = threadId;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric#getActivityList()
	 */
	@Override
	public float[] getActivityList() {
		float[] activityList = super.getActivityList();
	
		boolean scaleCPU = isScaledCpu();
		if(scaleCPU){
			Object object = getAdapter(ICPUScale.class);		
			if(object instanceof ICPUScale){		
				ICPUScale  cpuScale = (ICPUScale) object;
				if(calculatedActivity != null && calculatedActivity.length == activityList.length){
					activityList =  calculatedActivity;					
				}else{				
					int[] sampleList = getSampleList();			
					for (int i = 0; i < sampleList.length; i++) {
						float value = activityList[i] / 100;
						value = value * cpuScale.calculateScale(sampleList[i]) * 100;
						activityList[i] = value;				
					}
					calculatedActivity = activityList;
				}	
			}
		}

		return activityList;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if(adapter == null){
			return null;
		}
		return Platform.getAdapterManager().loadAdapter(this, adapter.getName());
	}
	
	/**
	 * Check are threads scaled or not
	 * 
	 * @return
	 */
	public boolean isScaledCpu(){
		boolean scaleCPU = false;

		Object obj = NpiInstanceRepository.getInstance().activeUidGetPersistState(
				PiPlugin.ACTION_SCALE_CPU);
		if ((obj != null) && (obj instanceof Boolean)){
			// retrieve the current value
			scaleCPU = (Boolean) obj;
		}else{
			// set the initial value
			NpiInstanceRepository.getInstance().activeUidSetPersistState(
					PiPlugin.ACTION_SCALE_CPU, scaleCPU);
		}
		return scaleCPU;
	}
}
