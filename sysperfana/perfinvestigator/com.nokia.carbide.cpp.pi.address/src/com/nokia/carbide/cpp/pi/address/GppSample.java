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

import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampleWithFunctions;

public class GppSample extends GenericSampleWithFunctions
  {
	private static final long serialVersionUID = -4003790244732811550L;

	public int threadIndex;
	public int binaryIndex;
	public int functionIndex;
	
    public GppThread   thread;
    public GppFunction function;
    public long programCounter;
    public SomMapping somMapping;
    
    public Function currentFunctionSym;    
    public Function currentFunctionItt;
    
    public void resolveFunction(FunctionResolver res)
    {
    	if (res.getResolverName().equals("Symbol"))  //$NON-NLS-1$
    	{
    		this.currentFunctionSym = res.findFunctionForAddress(programCounter);
    	}
    	else if (res.getResolverName().equals("ITT"))  //$NON-NLS-1$
    	{
    		if (this.currentFunctionSym == null)
    			this.currentFunctionItt = res.findFunctionForAddress(programCounter);
    	}
    }

    public String toString()
	  {
	  	return "Gpp:#" + this.sampleSynchTime + " @0x"   //$NON-NLS-1$ //$NON-NLS-2$
	  							  + Long.toHexString(this.programCounter) + " fS:"   //$NON-NLS-1$
	  							  + this.currentFunctionSym != null ? this.currentFunctionSym.functionName : this.currentFunctionItt.functionName
	  							  + " pr:" + this.thread.process.name + " th:"+ this.thread.threadName;  //$NON-NLS-1$ //$NON-NLS-2$
	  }
  }
