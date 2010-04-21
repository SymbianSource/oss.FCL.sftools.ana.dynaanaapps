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

package com.nokia.carbide.cpp.pi.call;

import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampleWithFunctions;
import com.nokia.carbide.cpp.internal.pi.model.IFunction;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.model.UnresolvedFunction;
import com.nokia.carbide.cpp.pi.address.GppSample;
import com.nokia.carbide.cpp.pi.address.GppTrace;


public class GfcSample extends GenericSampleWithFunctions
{
  private static final long serialVersionUID =  4293607815033391377L;
  
  private static transient Vector gppSamples = null;
 
  public long programCounter;
  public long linkRegister;
  public long sampleNumber;

  private IFunction callerFunctionSym;
  private IFunction currentFunctionSym;
  
  private IFunction callerFunctionItt;
  private IFunction currentFunctionItt;
  
  public GfcSample(long programCounter, long linkRegister, long sampleNumber, long sampleSynchTime)
  {
    this.programCounter = programCounter;
    this.linkRegister = linkRegister;
    this.sampleNumber = sampleNumber;
    this.sampleSynchTime = sampleSynchTime;
  }

  @Override
  public void resolveFunction(FunctionResolver res)
  {
	  if (res.getResolverName().equals("Symbol")) //$NON-NLS-1$
	  {
		  this.callerFunctionSym  = res.findFunctionForAddress(linkRegister);
		  int index = (int)this.sampleNumber;
		  if (gppSamples != null && index < gppSamples.size()) {
			  GppSample gppSample = (GppSample)gppSamples.get(index);
			  if (gppSample.programCounter == programCounter)
				  this.currentFunctionSym = gppSample.getCurrentFunctionSym();
			  else
				  this.currentFunctionSym = res.findFunctionForAddress(programCounter);
		  } else
			  this.currentFunctionSym = res.findFunctionForAddress(programCounter);		  
	  }
	  else if (res.getResolverName().equals("ITT")) //$NON-NLS-1$
	  {
		  if (this.callerFunctionSym == null)
			  this.callerFunctionItt  = res.findFunctionForAddress(linkRegister);
		  if (this.currentFunctionSym == null) {
			  GppSample gppSample = (GppSample)gppSamples.get((int)this.sampleNumber);
			  if (gppSample.programCounter == programCounter){
				  IFunction function = gppSample.getCurrentFunctionItt();
				  
				  //if it's an unresolved function don't store it, we can generate it later
				  if (!(function instanceof UnresolvedFunction)){
					  this.currentFunctionItt = function;					  
				  }
			  }
			  else
				  this.currentFunctionItt = res.findFunctionForAddress(programCounter, sampleSynchTime);		  
		  }
	  }
  }
  
	 /**
	  * Getter for the caller function (resolved from Symbol file)
	 * @return the callerFunctionSym
	 */
	public IFunction getCallerFunctionSym() {
		return callerFunctionSym;
	}
	
	/**
	  * Setter for the caller function (resolved from Symbol file)
	 * @param callerFunctionSym the callerFunctionSym to set
	 */
	public void setCallerFunctionSym(IFunction callerFunctionSym) {
		this.callerFunctionSym = callerFunctionSym;
	}
	
	/**
	  * Getter for the current function (resolved from Symbol file)
	 * @return the currentFunctionSym
	 */
	public IFunction getCurrentFunctionSym() {
		return currentFunctionSym;
	}
	
	/**
	  * Setter for the current function (resolved from Symbol file)
	 * @param currentFunctionSym the currentFunctionSym to set
	 */
	public void setCurrentFunctionSym(IFunction currentFunctionSym) {
		this.currentFunctionSym = currentFunctionSym;
	}

	/**
	  * Getter for the caller function (resolved from dynamic binary trace)
	 * @return the callerFunctionItt
	 */
	public IFunction getCallerFunctionItt() {
		if (callerFunctionItt == null && callerFunctionSym == null){
			return new UnresolvedFunction(linkRegister);
		}
		
		return callerFunctionItt;
	}
	
	/**
	  * Setter for the caller function (resolved from dynamic binary trace)
	 * @param callerFunctionItt the callerFunctionItt to set
	 */
	public void setCallerFunctionItt(IFunction callerFunctionItt) {
		this.callerFunctionItt = callerFunctionItt;
	}
	
	/**
	  * Getter for the current function (resolved from dynamic binary trace)
	 * @return the currentFunctionItt
	 */
	public IFunction getCurrentFunctionItt() {
		if (currentFunctionItt == null && currentFunctionSym == null){
			return new UnresolvedFunction(programCounter);
		}
		
		return currentFunctionItt;
	}
	
	/**
	  * Getter for the current function (resolved from dynamic binary trace)
	 * @param currentFunctionItt the currentFunctionItt to set
	 */
	public void setCurrentFunctionItt(IFunction currentFunctionItt) {
		this.currentFunctionItt = currentFunctionItt;
	}

	@Override
public String toString()
  {
  	return "Gfc:#" + this.sampleSynchTime //$NON-NLS-1$
  			+ " @0x" + Long.toHexString(this.programCounter) //$NON-NLS-1$
  			+ " fS:" //$NON-NLS-1$
  			+ ((this.currentFunctionSym == null || (this.currentFunctionSym.getFunctionName() == null)) ? "" : this.currentFunctionSym.getFunctionName()) //$NON-NLS-1$
  			+ " cS:" //$NON-NLS-1$
  			+ ((this.callerFunctionSym  == null || (this.callerFunctionSym.getFunctionName()  == null)) ? "" : this.callerFunctionSym.getFunctionName()); //$NON-NLS-1$
  }
  
  public static void setAddressTraceSamples() {
	  ParsedTraceData traceData = TraceDataRepository.getInstance().getTrace(NpiInstanceRepository.getInstance().activeUid(), GppTrace.class);
	  if (traceData != null && traceData.traceData != null && (traceData.traceData instanceof GppTrace))
		  gppSamples = ((GppTrace)traceData.traceData).samples; //$NON-NLS-1$
  }
}
