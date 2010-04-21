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

package com.nokia.carbide.cpp.internal.pi.resolvers;

import java.util.Hashtable;

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.IFunction;


public class RofsSymbolFileFunctionResolver extends SymbolFileFunctionResolver {
	
	@Override
	public IFunction findFunctionForAddress(long address) {
		return super.findFunctionForAddress(address);
	}
	
	public void adjustRuntimeBinary(Hashtable<String, Binary> hostNameToBinary) {
		if (dllList == null)
			return;
		
		for (SymbolFileDllItem dllItem : dllList) {
			if (dllItem.name != null) {
				Binary binary = hostNameToBinary.get(dllItem.name);
				if (binary != null) {
					dllItem.start += binary.getStartAddress();
					dllItem.end += binary.getStartAddress();
					for (SymbolFileFunctionItem funcItem : dllItem.data) {
						funcItem.address += binary.getStartAddress();
					}
				}
			}
		}
	}
}
