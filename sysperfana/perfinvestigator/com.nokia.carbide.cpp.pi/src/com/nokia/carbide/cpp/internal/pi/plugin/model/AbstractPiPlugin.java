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

package com.nokia.carbide.cpp.internal.pi.plugin.model;

import org.eclipse.ui.plugin.AbstractUIPlugin;

//Every user plug-in has to extend this class

public abstract class AbstractPiPlugin extends AbstractUIPlugin
{
	protected String pluginName;
	protected int pluginSystemId;
	
	private boolean initialised = false;
	private boolean pluginSystemIdSet = false;
	
	public AbstractPiPlugin()
	{}

	public String getPluginName() 
	{
		return pluginName;
	}

	public boolean isInitialised() 
	{
		return initialised;
	}
	
	public int getPluginSystemId()
	{
		if (pluginSystemIdSet)
			return pluginSystemId;
		else
			return -1;
	}
	
	//	plugin system id can be set only once for each plugin instances
	public void setPluginSystemId(int id)
	{
		if (pluginSystemIdSet)
			return;
		this.pluginSystemId = id;
		pluginSystemIdSet = true;
	}
	
	public void initialise(String name)
	{
		this.pluginName = name;
		this.initialised = true;
	}
}
