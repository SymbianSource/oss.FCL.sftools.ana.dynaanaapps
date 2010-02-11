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

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampleWithFunctions;


public class IttSample extends GenericSampleWithFunctions
{
  private static final long serialVersionUID = 31446918621985951L;
	
  public long[] instructions;
  public long checksum;
  public long programCounter;

  public Function currentFunctionItt;
  public Binary currentBinaryItt;
  public Function currentFunctionSym;
  public Binary currentBinarySym;
  
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
  		
  		if (this.currentFunctionSym != null)
  			this.currentBinarySym = this.currentFunctionSym.functionBinary;
  	}
  	else if (res.getResolverName().equals("ITT"))  //$NON-NLS-1$
  	{
  		if (currentFunctionSym == null) {
	  		this.currentFunctionItt = res.findFunctionForAddress(programCounter);
	  		
	  		if (this.currentFunctionItt != null)
	  			this.currentBinaryItt = this.currentFunctionItt.functionBinary;
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
  
  public String toString()
  {
  	long diff = 0;
  	
  	if (this.currentBinaryItt != null && this.currentBinarySym != null)
  		diff = this.currentBinaryItt.startAddress+this.currentBinaryItt.offsetToCodeStart-
			   this.currentBinarySym.startAddress;
  	else
  	{
  		System.out.println("NULL"); //$NON-NLS-1$
  	}
  	
  	String result = "Itt:#"+this.sampleSynchTime+ //$NON-NLS-1$
			" @0x"+Long.toHexString(this.programCounter)+ //$NON-NLS-1$
			" fS:"+this.currentFunctionSym.functionName+ //$NON-NLS-1$
			" oS:"+Long.toHexString(this.currentFunctionSym.startAddress.longValue())+" +"+(this.programCounter-this.currentFunctionSym.startAddress.longValue())+ //$NON-NLS-1$ //$NON-NLS-2$
			" fI:"+this.currentFunctionItt.functionName+ //$NON-NLS-1$
			" oI:"+Long.toHexString(this.currentFunctionItt.startAddress.longValue())+" +"+(this.programCounter-this.currentFunctionItt.startAddress.longValue()); //$NON-NLS-1$ //$NON-NLS-2$
  	
  	if (diff != 0) result=" DIFF: "+diff+" "+result; //$NON-NLS-1$ //$NON-NLS-2$
  	
  	return result;
  }

  
}
