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

import java.util.Vector;

public class ProfiledFunction extends ProfiledGeneric
{
	long   functionAddress;
    String functionBinaryName;
//	String functionBinaryPathName;
    Vector<Integer> threadIds;
    
    public ProfiledFunction(int cpuNumber, int graphCount)
    {
        super(cpuNumber, graphCount);
        this.threadIds = new Vector<Integer>();
        //setEnabled(false);
    }
    /**
     * @return Returns the functionAddress.
     */
    public long getFunctionAddress()
    {
        return functionAddress;
    }

    /**
     * @param functionAddress The functionAddress to set.
     */
    public void setFunctionAddress(long functionAddress)
    {
        this.functionAddress = functionAddress;
    }

    /**
     * @return Returns the functionBinaryName.
     */
    public String getFunctionBinaryName()
    {
        //return this.nameString;
        return functionBinaryName;
    }

    /**
     * @param functionBinaryName The functionBinaryName to set.
     */
    public void setFunctionBinaryName(String functionBinaryName)
    {
        //this.nameString = functionBinaryName;
        this.functionBinaryName = functionBinaryName;
    }
//
//    public String getFunctionBinaryPathName()
//    {
//        //return this.nameString;
//        return functionBinaryPathName;
//    }
//
//    public void setFunctionBinaryPathName(String functionBinaryPathName)
//    {
//        //this.nameString = functionBinaryName;
//        this.functionBinaryPathName = functionBinaryPathName;
//    }
    
    public void addThreadId(int id)
    {
    	Integer integer = Integer.valueOf(id);
    	if (!this.threadIds.contains(integer))
    	{
    		this.threadIds.add(integer);
    	}
    }
    
    public boolean containsThreadId(int id)
    {
    	return this.threadIds.contains(Integer.valueOf(id));
    }

    @Override
	public boolean equals(Object anObject)
    {
        if (anObject == null)
            return false;
        if (!(anObject instanceof ProfiledFunction))
            return false;
        ProfiledFunction pfTmp = (ProfiledFunction)anObject;
        if (this.getNameString().equals(pfTmp.getNameString()))
            return true;
        else
            return false;
    }
    
    @Override
	public int hashCode()
    {
    	return this.getNameString().hashCode();
    }

    //unused?
//    public String toString(int graphIndex)
//    {
//    	if (this.isEnabled(graphIndex))
//    	{
//    		return "true  " + this.getAverageLoadValueString(graphIndex) + getNameString(); //$NON-NLS-1$
//	  	}
//		else
//		{
//	      	return "false " + this.getAverageLoadValueString(graphIndex) + getNameString(); //$NON-NLS-1$
//		}
//    }   
}
