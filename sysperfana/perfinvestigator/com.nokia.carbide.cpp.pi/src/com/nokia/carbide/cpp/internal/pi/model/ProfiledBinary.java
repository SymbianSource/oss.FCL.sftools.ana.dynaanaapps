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

public class ProfiledBinary extends ProfiledGeneric
{
    public ProfiledBinary(int cpuNumber, int graphCount)
    {
        super(cpuNumber, graphCount);
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
