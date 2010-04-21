/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.pi.perfcounters;

import com.nokia.carbide.cpp.internal.pi.interfaces.IReturnPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;

/**
 * Implements the extension point piPluginData to register this plugin as a PI plugin
 */
public class PecReturnPlugin implements IReturnPlugin {
	public AbstractPiPlugin getPlugin() {
		return PecPlugin.getDefault();
	}
}
