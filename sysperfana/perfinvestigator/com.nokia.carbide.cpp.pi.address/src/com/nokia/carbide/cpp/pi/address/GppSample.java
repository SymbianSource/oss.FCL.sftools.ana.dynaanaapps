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

package com.nokia.carbide.cpp.pi.address;

import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampleWithFunctions;
import com.nokia.carbide.cpp.internal.pi.model.IFunction;
import com.nokia.carbide.cpp.internal.pi.model.UnresolvedFunction;

public class GppSample extends GenericSampleWithFunctions
  {
	private static final long serialVersionUID = -4003790244732811550L;

	public int threadIndex;
	public int binaryIndex;
	public int functionIndex;
	
    public GppThread   thread;
    public GppFunction function;
    public long programCounter;
    //public SomMapping somMapping;
    
    private IFunction currentFunctionSym;    
    private IFunction currentFunctionItt;
    
    /** CPU core number */
    public int cpuNumber;

    public void resolveFunction(FunctionResolver res)
    {
    	if (res.getResolverName().equals("Symbol"))  //$NON-NLS-1$
    	{
    		this.currentFunctionSym = res.findFunctionForAddress(programCounter);
    	}
    	else if (res.getResolverName().equals("ITT"))  //$NON-NLS-1$
    	{
    		if (this.currentFunctionSym == null)
    			this.currentFunctionItt = res.findFunctionForAddress(programCounter, sampleSynchTime);
    	}
    }


	/**
	 * @return the function resolved from symbol file
	 */
	public IFunction getCurrentFunctionSym() {
		return currentFunctionSym;
	}

	/**
	 * Setter for the function resolved from symbol file
	 * @param currentFunctionSym the function to set
	 */
	public void setCurrentFunctionSym(IFunction currentFunctionSym) {
		this.currentFunctionSym = currentFunctionSym;
	}

	/**
	 * Getter
	 * @return the function resolved from dynamic binary trace
	 */
	public IFunction getCurrentFunctionItt() {
		if (currentFunctionItt == null && currentFunctionSym == null){
			return new UnresolvedFunction(programCounter);
		}
		
		return currentFunctionItt;
	}

	/**
	 * Setter the function resolved from dynamic binary trace
	 * @param currentFunctionItt the function to set
	 */
	public void setCurrentFunctionItt(IFunction currentFunctionItt) {
		this.currentFunctionItt = currentFunctionItt;
	}
    
	@Override
    public String toString()    
	  {
  	StringBuilder sb = new StringBuilder("Gpp:#").append(this.sampleSynchTime);
  	if (cpuNumber != -1){
  		sb.append(" CPU: ").append(cpuNumber);
  	}
  	sb.append(" @0x").append(Long.toHexString(this.programCounter));   //$NON-NLS-1$ //$NON-NLS-2$
  	if (currentFunctionSym != null){
  		sb.append(" fS:").append(currentFunctionSym.getFunctionName());
  	} else if (this.currentFunctionItt != null){
  		sb.append(" fS:").append(currentFunctionItt.getFunctionName());
  	}
  	if (thread != null){
  		if (thread.process != null && thread.process.name != null){
  			sb.append(" pr:").append(this.thread.process.name);
  		}
  		if (thread.threadName != null){
  			sb.append(" th:").append(this.thread.threadName);
  		}
  	}
  	return sb.toString();
	  }
    
  }
