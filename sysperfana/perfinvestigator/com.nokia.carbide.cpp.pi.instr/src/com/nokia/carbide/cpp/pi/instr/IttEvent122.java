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
import com.nokia.carbide.cpp.internal.pi.model.GenericEvent;

public class IttEvent122 extends GenericEvent 
  {
	private static final long serialVersionUID = -5460562756042454539L;

	public String binaryName;
	public long binaryLength;
	public long binaryLocation;
	public Binary binary;

	public IttEvent122()
	{
		super(0);
	}
	
	public void createBinary()
	{
		binary = new Binary(this.binaryName);
		binary.length = (int)this.binaryLength;
		binary.offsetToCodeStart = 0;
		binary.startAddress = this.binaryLocation;
		binary.type = Messages.getString("IttEvent122.unknownBinaryType"); //$NON-NLS-1$
	}
  }
