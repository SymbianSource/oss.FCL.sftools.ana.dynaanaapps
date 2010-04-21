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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.GenericEvent;
import com.nokia.carbide.cpp.internal.pi.model.GenericEventTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.pi.address.GppTrace;


public class IttTrace122 extends GenericEventTrace
  {
	private static final long serialVersionUID = -3670942351731061113L;
	private boolean debug = false;
	private transient boolean sortedEvents = false;
	private transient Object[] sorted;
	private transient Hashtable<String,Function> knownFunctions = new Hashtable<String,Function>();
	private transient Hashtable<String,Binary> knownBinaries = new Hashtable<String,Binary>();
	
	public IttTrace122()
	{
	}
	
	public Binary getBinaryForAddress(long address)
	{
		Enumeration<GenericEvent> enr = this.getEvents().elements();
		while(enr.hasMoreElements())
		{
			IttEvent122 ev = (IttEvent122)enr.nextElement();
			if (   (address >= ev.binaryLocation)
				&& (address < (ev.binaryLocation + ev.binaryLength)) )
			{
				return ev.getBinary();			
			}
		}
		
		return null;
	}

	
	public Binary getBinaryForAddressNew(long address, float sampleTime)
	{
		List<IttEvent122> foundedEvents  = new ArrayList<IttEvent122>();
		if (!sortedEvents) {
			sorted = this.getEvents().toArray();
			Arrays.sort(sorted, new Comparator<Object>() {
				
				public int compare(Object arg0, Object arg1)
				{
					if (arg0 instanceof IttEvent122 && arg1 instanceof IttEvent122)
						return (int) (((IttEvent122)arg0).binaryLocation - ((IttEvent122)arg1).binaryLocation);
					else
						return 0;
				}
			});
			sortedEvents = true;
		}
		
		int high = sorted.length;
		int low = -1;
		int next;
		
		// find a match using binary search
		while (high - low > 1) {
			next = (low + high) >>> 1;
			IttEvent122 event = (IttEvent122)sorted[next];
			if (   (address >= event.binaryLocation)
				&& (address < (event.binaryLocation + event.binaryLength)) ) {
				// first suitable event of the list is found and let's find also next ones
				for( int i=next ; i < sorted.length ; i++ ){
					IttEvent122 ie = (IttEvent122)sorted[i];
					if (   (address >= ie.binaryLocation)
							&& (address < (ie.binaryLocation + ie.binaryLength)) ) {
						foundedEvents.add(ie);
					}else{
						break;
					}
				}
				break;
			}

			if (event.binaryLocation >= address) {
				high = next;
			} else {
				low = next;
			}
		}
		
		if(!foundedEvents.isEmpty()){
			if(foundedEvents.size() == 1){
				return foundedEvents.get(0).getBinary();
			}else{
				if(sampleTime <= 0){
					// if sample time is not known just pick up first event of the list
					return foundedEvents.get(0).getBinary();
				}
				for (IttEvent122 event : foundedEvents) {
					if (sampleTime >= event.eventTime
							&& (sampleTime < event.eventEndTime)) {
						// event is in the time frame
						return event.getBinary();
					} else if (sampleTime >= event.eventTime
							&& event.eventEndTime <= 0.0) {
						// event is started in the time frame
						return event.getBinary();
					}
				}
			}
		}
		return null;
	}
	
	public Function getFunctionForAddress(long address, long sampleSynchTime, BinaryReader122 br)
	{		
		float sampleTime = calculateSampleTime(sampleSynchTime);
		Binary b = this.getBinaryForAddressNew(address, sampleTime);
		if (b != null)
		{	
			MapFile mf = br.getMapFileForBinary(b);
			Function f = null;
			
			if (mf != null)
			{
				f = mf.getFunctionForOffset(address-b.getStartAddress());
				if (f != null)
				{
					f.setStartAddress(Long.valueOf(b.getStartAddress()+mf.getOffsetFromBinaryStartForFunction(f.getFunctionName())));
					if (f.getStartAddress() != null)
					{
						if (f.getFunctionBinary() != null) 
						{
							f.setFunctionBinary(b);
						}
						//System.out.println("Resolved function to "+f.functionName+" "+Long.toHexString(f.startAddress.longValue()));
						return f;
					}
				}
				
				if (debug)System.out.println(Messages.getString("IttTrace122.couldNotResolveFunction")); //$NON-NLS-1$
			}
			else
			{
				if (debug)System.out.println(Messages.getString("IttTrace122.mapfileNotFound1")+b.getBinaryName()+Messages.getString("IttTrace122.mapfileNotFound2")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			if (f != null)
			{
				return f;
			}
			else
			{
				String fName = Messages.getString("IttTrace122.functionForAddressNotFound1")+Long.toHexString(address)+Messages.getString("IttTrace122.functionForAddressNotFound2"); //$NON-NLS-1$ //$NON-NLS-2$

				f = this.knownFunctions.get(fName);
				
				if (f == null) {
					f = new Function(fName, Long.valueOf(address), b.getBinaryName());
					f.setFunctionBinary(b);
					f.setLength(1);
					f.setOffsetFromBinaryStart(0);
					
					this.knownFunctions.put(fName, f);
				}

				return f;
			}
		}
		else
			return null;
	}

	/**
	 * Calculate an event time from given sample synch time
	 * 
	 * @param sampleSynchTime
	 * @return the sample time
	 */
	@SuppressWarnings("restriction")
	protected float calculateSampleTime(long sampleSynchTime) {
		if (sampleSynchTime <= 0) {
			// sampleSynchTime is not used
			return sampleSynchTime;
		}
		int cpuCount = 1;
		int samplingInterval = 1;
		try {
			ParsedTraceData ptd = TraceDataRepository.getInstance().getTrace(
					NpiInstanceRepository.getInstance().activeUid(),
					GppTrace.class);

			final GppTrace gppTraceTmp = (GppTrace) ptd.traceData;
			cpuCount = gppTraceTmp.getCPUCount();
			samplingInterval = (Integer) NpiInstanceRepository
					.getInstance()
					.activeUidGetPersistState(
							"com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		} catch (Exception e) {
			// use default values for the calculation
		}
		
		return (((sampleSynchTime * samplingInterval) / cpuCount) + 1)
				* (1 / 1000.0f) - 0.0005f;
	}
	
	  public Binary getBinaryForFileName(String fileName)
	  {
		  Enumeration<GenericEvent> en = this.getEvents().elements();
		  while (en.hasMoreElements())
		  {
			  IttEvent122 e = (IttEvent122)en.nextElement();
			  if (e.binaryName.toLowerCase().indexOf(fileName.toLowerCase()) != -1)
				  return e.getBinary();
		  }
		  return null;
	  }
	 
	/**
	 * Add given event into the event table. If version 2.x is used updates
	 * event's end time for the same event which is founded earlier otherwise
	 * event is added into the table
	 * 
	 * @param ge
	 * @param isVersion2x
	 *            is used 2.x version or earlier 1.22 version
	 */
	public void addEvent(GenericEvent ge, boolean isVersion2x) {
		if (!isVersion2x) {
			super.addEvent(ge);
			return;
		}

		IttEvent122 e = (IttEvent122) ge;
		Vector<GenericEvent> eventVector = getEvents();
		for (int i = (eventVector.size() - 1) ; i >= 0 ; i--) {
			IttEvent122 ie = (IttEvent122) eventVector.get(i);
			if (ie.getBinary().getLength() == e.getBinary().getLength()
					&& ie.binaryLocation == e.binaryLocation
					&& ie.binaryName.equals(e.binaryName)) {
				if (ie.eventTime > 0.0 && ie.eventEndTime <= 0.0) {	
					// set event's end time
					ie.eventEndTime = e.eventTime;
					return;
				} else {
					break;
				}
			}
		}
		// Given event is a new one or it is created again
		super.addEvent(ge);
	}
}
