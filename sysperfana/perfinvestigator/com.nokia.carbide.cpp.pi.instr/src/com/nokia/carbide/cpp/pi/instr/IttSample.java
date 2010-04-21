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

import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampleWithFunctions;
import com.nokia.carbide.cpp.internal.pi.model.IBinary;
import com.nokia.carbide.cpp.internal.pi.model.IFunction;


public class IttSample extends GenericSampleWithFunctions
{
	private static final long serialVersionUID = -6451379240317856526L;
	
  public long[] instructions;
  public long checksum;
  public long programCounter;

  private IFunction currentFunctionItt;
  private IFunction currentFunctionSym;
  
  public IttSample(int size)
  {
    instructions = new long[size];
    sampleSynchTime = 0;
  }

  public long[] reversedInstructions()
  {
    long[] revs = new long[instructions.length];
    for (int i=0;i<instructions.length;i++)
    {
      revs[i] |= (((instructions[i] & 0x000000ff) << 24) & 0xff000000);
      revs[i] |= (((instructions[i] & 0x0000ff00) << 8) & 0x00ff0000);
      revs[i] |= (((instructions[i] & 0x00ff0000) >> 8) & 0x0000ff00);
      revs[i] |= (((instructions[i] & 0xff000000) >> 24) & 0x000000ff);

      //System.out.print("\nOriginal "+Integer.toHexString((int)instructions[i]));

      String temp = Long.toHexString(revs[i]);
      String nulls = ""; //$NON-NLS-1$
      for (int n=0;n<8-temp.length();n++) nulls=nulls+"0"; //$NON-NLS-1$
      temp = nulls+temp;
      //System.out.println(" reversed "+temp);
    }
    return revs;
  }

  public void resolveFunction(FunctionResolver res)
  {
  	if (res.getResolverName().equals("Symbol"))  //$NON-NLS-1$
  	{
  		this.currentFunctionSym = res.findFunctionForAddress(programCounter);
  	}
  	else if (res.getResolverName().equals("ITT"))  //$NON-NLS-1$
  	{
  		if (currentFunctionSym == null) {
	  		this.currentFunctionItt = res.findFunctionForAddress(programCounter, sampleSynchTime);
  		}
  	}
  }
   
  public String toReversedString()
  {
    String s = ""; //$NON-NLS-1$
    long[] revs = this.reversedInstructions();
    
    for (int i=0;i<instructions.length;i++)
    {
      String nulls = ""; //$NON-NLS-1$
      String temp = ""; //$NON-NLS-1$
      temp = Integer.toHexString((int)revs[i]);
      for (int t = 0;t<(8-temp.length());t++) nulls = nulls+"0"; //$NON-NLS-1$
      s = s+(nulls+temp+Messages.getString("IttSample.space")); //$NON-NLS-1$
    }
    return s;
  }
	  
	 /**
	  * Getter for current function resolved from dynamic binary trace
	 * @return the currentFunctionItt
	 */
	public IFunction getCurrentFunctionItt() {
		return currentFunctionItt;
	}
	
	/**
	  * Setter for current function resolved from dynamic binary trace
	 * @param currentFunctionItt the currentFunctionItt to set
	 */
	public void setCurrentFunctionItt(Function currentFunctionItt) {
		this.currentFunctionItt = currentFunctionItt;
	}
	
	/**
	  * Getter for current function resolved from symbols
	 * @return the currentFunctionSym
	 */
	public IFunction getCurrentFunctionSym() {
		return currentFunctionSym;
	}
	
	/**
	  * Setter for current function resolved from symbols
	 * @param currentFunctionSym the currentFunctionSym to set
	 */
	public void setCurrentFunctionSym(Function currentFunctionSym) {
		this.currentFunctionSym = currentFunctionSym;
	}

	/**
	 * Getter for the current binary resolved from dynamic binary trace
	 * @return the currentBinaryItt
	 */
	public IBinary getCurrentBinaryItt() {
		IBinary ret = null;
  		if (this.currentFunctionItt != null){
  			ret = this.currentFunctionItt.getFunctionBinary();
  		}
  		return ret;
	}

	/**
	 * Getter for the current binary resolved from symbols
	 * @return the currentBinarySym
	 */
	public IBinary getCurrentBinarySym() {
		IBinary ret = null;
  		if (this.currentFunctionSym != null){
  			ret = this.currentFunctionSym.getFunctionBinary();
  		}
		return ret;
	}


	@Override
	public String toString()
  {
  	long diff = 0;
  	
  	if (this.getCurrentBinaryItt() != null && this.getCurrentBinarySym() != null)
  		diff = this.getCurrentBinaryItt().getStartAddress()+this.getCurrentBinaryItt().getOffsetToCodeStart()-
			   this.getCurrentBinarySym().getStartAddress();
  	else
  	{
  		System.out.println("NULL"); //$NON-NLS-1$
  	}
  	
  	String result = "Itt:#"+this.sampleSynchTime+ //$NON-NLS-1$
			" @0x"+Long.toHexString(this.programCounter)+ //$NON-NLS-1$
			" fS:"+this.currentFunctionSym.getFunctionName()+ //$NON-NLS-1$
			" oS:"+Long.toHexString(this.currentFunctionSym.getStartAddress().longValue())+" +"+(this.programCounter-this.currentFunctionSym.getStartAddress().longValue())+ //$NON-NLS-1$ //$NON-NLS-2$
			" fI:"+this.currentFunctionItt.getFunctionName()+ //$NON-NLS-1$
			" oI:"+Long.toHexString(this.currentFunctionItt.getStartAddress().longValue())+" +"+(this.programCounter-this.currentFunctionItt.getStartAddress().longValue()); //$NON-NLS-1$ //$NON-NLS-2$
  	
  	if (diff != 0) result=" DIFF: "+diff+" "+result; //$NON-NLS-1$ //$NON-NLS-2$
  	
  	return result;
  }

  
}
