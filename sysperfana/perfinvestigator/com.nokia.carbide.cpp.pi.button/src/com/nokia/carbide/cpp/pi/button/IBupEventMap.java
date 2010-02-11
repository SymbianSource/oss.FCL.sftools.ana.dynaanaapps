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

package com.nokia.carbide.cpp.pi.button;

import java.util.Set;

import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileType;

public interface IBupEventMap {
	public IBupEventMapProfile getProfile ();
	public Set<Integer> getKeyCodeSet();
	public String getLabel(int keyCode);
	public String getEnum(int keyCode);
	public void addMapping(int keyCode, String enumString, String label);
	public void removeMapping(int keyCode);
	public ButtonEventProfileType toEmfModel();
}
