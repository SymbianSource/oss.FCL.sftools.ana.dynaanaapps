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

import java.io.Serializable;

public class GppFunction implements Serializable
  {
	private static final long serialVersionUID = -4412767529422953949L;
	
    public int appearances;
    public String name;
    public long address;
    public String dllName;
    public int popularityIndex;
  }
