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

package com.nokia.carbide.cpp.internal.pi.manager;

public class RegisterEntry 
{
	private String pluginName;
	private String id;
	private String pluginClassName;
	private String libraryName;
	private String pluginPath;
	private String[] requiredLibrarys;
	
	public RegisterEntry(String pluginName, String id, String version, String providerName, String pluginClassName,
			String libraryName, String pluginPath, String[] requiredLibrarys)
	{
		this.pluginName = pluginName;
		this.id = id;
		this.pluginClassName = pluginClassName;
		this.libraryName = libraryName;
		this.pluginPath = pluginPath;
		this.requiredLibrarys = requiredLibrarys;
	}
	
	public String getId() 
	{
		return id;
	}
	public String getLibraryName() 
	{
		return libraryName;
	}
	public String getPluginClassName() 
	{
		return pluginClassName;
	}
	public String getPluginName() 
	{
		return pluginName;
	}
	public String getPluginPath() 
	{
		return pluginPath;
	}

	public String[] getReguiredLibrarys() 
	{
		return requiredLibrarys;
	}
}
