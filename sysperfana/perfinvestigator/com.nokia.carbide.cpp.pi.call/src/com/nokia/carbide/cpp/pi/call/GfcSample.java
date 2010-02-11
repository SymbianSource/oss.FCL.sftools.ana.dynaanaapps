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
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampleWithFunctions;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.pi.address.GppSample;
import com.nokia.carbide.cpp.pi.address.GppTrace;


public class GfcSample extends GenericSampleWithFunctions
{
  private static final long serialVersionUID =  4293607815033391377L;
  
  private static transient Vector gppSamples = null;
 
  public long programCounter;
  public long linkRegister;
  public long sampleNumber;

  public Function callerFunctionSym;
  public Function currentFunctionSym;
  
  public Function callerFunctionItt;
  public Function currentFunctionItt;
  
  public GfcSample(long programCounter, long linkRegister, long sampleNumber, long sampleSynchTime)
  {
    this.programCounter = programCounter;
    this.linkRegister = linkRegister;
    this.sampleNumber = sampleNumber;
    this.sampleSynchTime = sampleSynchTime;
  }

  public void resolveFunction(FunctionResolver res)
  {
	  if (res.getResolverName().equals("Symbol")) //$NON-NLS-1$
	  {
		  this.callerFunctionSym  = res.findFunctionForAddress(linkRegister);
		  int index = (int)this.sampleNumber;
		  if (gppSamples != null && index < gppSamples.size()) {
			  GppSample gppSample = (GppSample)gppSamples.get(index);
			  if (gppSample.programCounter == programCounter)
				  this.currentFunctionSym = gppSample.currentFunctionSym;
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
			  if (gppSample.programCounter == programCounter)
				  this.currentFunctionItt = gppSample.currentFunctionItt;
			  else
				  this.currentFunctionItt = res.findFunctionForAddress(programCounter);		  
		  }
	  }
  }
  
  public String toString()
  {
  	return "Gfc:#" + this.sampleSynchTime //$NON-NLS-1$
  			+ " @0x" + Long.toHexString(this.programCounter) //$NON-NLS-1$
  			+ " fS:" //$NON-NLS-1$
  			+ ((this.currentFunctionSym == null || (this.currentFunctionSym.functionName == null)) ? "" : this.currentFunctionSym.functionName) //$NON-NLS-1$
  			+ " cS:" //$NON-NLS-1$
  			+ ((this.callerFunctionSym  == null || (this.callerFunctionSym.functionName  == null)) ? "" : this.callerFunctionSym.functionName); //$NON-NLS-1$
  }
  
  public static void setAddressTraceSamples() {
	  ParsedTraceData traceData = TraceDataRepository.getInstance().getTrace(NpiInstanceRepository.getInstance().activeUid(), GppTrace.class);
	  if (traceData != null && traceData.traceData != null && (traceData.traceData instanceof GppTrace))
		  gppSamples = ((GppTrace)traceData.traceData).samples; //$NON-NLS-1$
  }
}
