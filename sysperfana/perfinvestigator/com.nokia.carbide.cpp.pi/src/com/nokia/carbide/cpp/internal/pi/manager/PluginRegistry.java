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

import java.util.Enumeration;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


public final class PluginRegistry 
{
	private static PluginRegistry pluginRegistry = null;
	private Vector<AbstractPiPlugin> plugins;

	private PluginRegistry()
	{
		plugins = new Vector<AbstractPiPlugin>();
	}
	
	public static PluginRegistry getInstance()
	{
		if (pluginRegistry != null)
			return pluginRegistry;
		else
		{
			pluginRegistry = new PluginRegistry();
			return pluginRegistry;
		}
	}
	
	protected void addRegistryEntry(AbstractPiPlugin plugin)
	{
		if (plugin instanceof ITrace)
		{
			ITrace testPlugin = (ITrace) plugin;
			if (testPlugin.getTraceId() == 1)
			{
				// This is a workaround.
				// must have GPP run first because ITT
				// parsing assume the symbolparser of GPP
				// is already registered, which in turn 
				// register during GPP parsing
				plugins.add(0, plugin);
				return;
			} else {
				int index = plugins.size() > 0 ? plugins.size() - 1 : 0;
				for (int i = 0; i < plugins.size(); i++) {
					AbstractPiPlugin thisPlugin = plugins.get(i);
					if (thisPlugin instanceof ITrace) {
						if (testPlugin.getTraceId() > ((ITrace)thisPlugin).getTraceId()) {
							index = i + 1;
							continue;
						} else if (testPlugin.getTraceId() < ((ITrace)thisPlugin).getTraceId()) {
							index = i;	// found insert location
							break;
						} else if (testPlugin.getTraceId() == 4) {
							// priority before memory for 4
							if (((ITrace)thisPlugin).getTraceName().equals("Memory")) {	//$NON-NLS-1$
								index = i;
								break;
							} else {
								index = i + 1;
								break;
							}
						} else {
							// only priority and memory share ID = 4
							GeneralMessages.showErrorMessage(Messages.getString("PluginRegistry.DuplicatedTraceID")); //$NON-NLS-1$
						}
					}
				}
				plugins.insertElementAt(plugin, index);
				return;
			}
		}
		plugins.add(plugin);
	}

	public Enumeration<AbstractPiPlugin> getRegistryEntries()
	{
		return plugins.elements();
	}
	
	public int getRegistrySize()
	{
		return plugins.size();
	}
}
