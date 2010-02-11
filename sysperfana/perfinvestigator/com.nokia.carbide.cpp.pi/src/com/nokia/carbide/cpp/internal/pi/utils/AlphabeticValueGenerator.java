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

package com.nokia.carbide.cpp.internal.pi.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

public class AlphabeticValueGenerator 
{
	public static void generateAlphabeticValues(Vector alphabeticItems)
	{
		ArrayList<Long> created = new ArrayList<Long>();
		
		Enumeration ae = alphabeticItems.elements();
		while(ae.hasMoreElements())
		{
			AlphabeticItem ai = (AlphabeticItem)ae.nextElement();

			Enumeration ae2 = alphabeticItems.elements();
			int tempOrder = 0;
			while(ae2.hasMoreElements())
			{
				AlphabeticItem ai2 = (AlphabeticItem)ae2.nextElement();
				if (ai.toString().compareTo(ai2.toString()) > 0)
				{
					tempOrder+=1000;
				}
			}

			while (created.contains(new Long(tempOrder)))
			{
				tempOrder++;
			}
			
			created.add(new Long(tempOrder));
			
			ai.order = tempOrder;
		}
	}
}
