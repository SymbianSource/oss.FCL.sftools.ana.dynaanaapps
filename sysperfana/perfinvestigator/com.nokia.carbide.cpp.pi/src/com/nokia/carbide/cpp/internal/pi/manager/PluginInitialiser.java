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
import java.util.Hashtable;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.utils.PluginClassLoader;
import com.nokia.carbide.cpp.pi.PiPlugin;


public class PluginInitialiser 
{
	/*
	 * contains vectors which are each mapped to hashcodes of analysis instances
	 * vectors contains analysis specific plugin instances
	 * also one vector for top level plugin instances is included which is mapped to 0 
	 */ 
	private static Hashtable<Integer,Vector<AbstractPiPlugin>> pluginInstanceReferences;
	
	/*
	 * Contains class loaders for the plugins
	 */
	private static ClassLoader pluginClassLoader;
	
	private PluginInitialiser(){}
	
	/**
	 * This method takes a plugin interface name as a parameter (e.g. com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace)
	 * and returns instances of the plugins which implement that interface
	 */
	public static Enumeration<AbstractPiPlugin> invokePluginInstances(int id, String pluginInterfaceName)
	{
		Vector<AbstractPiPlugin> pluginInstances = new Vector<AbstractPiPlugin>();
		AbstractPiPlugin plugin = null;
		java.lang.Class matchClass = null;

		// make sure interface name is a valid class
		try {
			matchClass = java.lang.Class.forName(pluginInterfaceName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// find all plugins that are instances of the interface
		Enumeration<AbstractPiPlugin> enu = PluginRegistry.getInstance().getRegistryEntries();
		while (enu.hasMoreElements())
		{
			// make sure the cast is valid
			try {
				plugin = (AbstractPiPlugin) enu.nextElement();
			} catch (ClassCastException e) {
				e.printStackTrace();
			}

			// record and return matches
			if (matchClass.isInstance(plugin)) {
				pluginInstances.add(plugin);
				addInstanceReference(id, plugin);
			}
		}

		return pluginInstances.elements();
	}
	
	public static ClassLoader getPluginClassLoader()
	{
		if (pluginClassLoader != null)
			return pluginClassLoader;
		
		AbstractPiPlugin plugin = null;
		Vector<AbstractPiPlugin> vec = new Vector<AbstractPiPlugin>();
		
		Enumeration<AbstractPiPlugin> e = PluginRegistry.getInstance().getRegistryEntries();
		while(e.hasMoreElements())
		{
			// make sure the cast is valid
			try {
				plugin = (AbstractPiPlugin) e.nextElement();
			} catch (ClassCastException e1) {
				e1.printStackTrace();
			}
			vec.add(plugin);
		}
		
		AbstractPiPlugin[] array = vec.toArray(new AbstractPiPlugin[vec.size()]);
		pluginClassLoader = new PluginClassLoader(array);
		return pluginClassLoader;
	}
	
	//returns all invoked plugin instances mapped to given id number
	public static Enumeration<AbstractPiPlugin> getPluginInstances(int id)
	{
		Vector<AbstractPiPlugin> tmp = new Vector<AbstractPiPlugin>();

		if (pluginInstanceReferences != null)
		{
			// make sure the cast is valid
			try {
				tmp = pluginInstanceReferences.get(Integer.valueOf(id));
			} catch (ClassCastException e1) {
				e1.printStackTrace();
			}
		}
		return tmp.elements();
	}
	
	// returns invoked plugin instances mapped to the given id number which implement an interface with the given name
	public static Enumeration<AbstractPiPlugin> getPluginInstances(int id, String pluginInterfaceName)
	{
		Vector<AbstractPiPlugin> instances = new Vector<AbstractPiPlugin>();

		if (pluginInstanceReferences == null)
			return instances.elements();

		Vector<AbstractPiPlugin> tmp = pluginInstanceReferences.get(Integer.valueOf(id));
		if (tmp != null)
		{
			AbstractPiPlugin plugin = null;
			java.lang.Class matchClass = null;

			// make sure the interface name is a valid class
			try {
				matchClass = java.lang.Class.forName(pluginInterfaceName);
			} catch (ClassNotFoundException e1) {
				System.out.println(Messages.getString("PluginInitialiser.pluginInterfaces")); //$NON-NLS-1$
				e1.printStackTrace();
			}
			
			// find all plugins that are instances of the interface
			Enumeration<AbstractPiPlugin> enu = tmp.elements();
			while (enu.hasMoreElements())
			{
				// make sure the cast is valid
				try {
					plugin = (AbstractPiPlugin) enu.nextElement();
				} catch (ClassCastException e1) {
					e1.printStackTrace();
				}
				
				if (matchClass.isInstance(plugin))
				{
					instances.add(plugin);
				}
			}
		}
		return instances.elements();
	}
	
	// returns all invoked plugin instances which implement an interface with the given name
	public static Enumeration<AbstractPiPlugin> getPluginInstances(String pluginInterfaceName)
	{
		Vector<AbstractPiPlugin> instances = new Vector<AbstractPiPlugin>();

		if (pluginInstanceReferences == null)
			return instances.elements();

		AbstractPiPlugin plugin = null;
		java.lang.Class matchClass = null;

		// make sure the interface name is a valid class
		try {
			matchClass = java.lang.Class.forName(pluginInterfaceName);
		} catch (ClassNotFoundException e1) {
			System.out.println(Messages.getString("PluginInitialiser.pluginInterfaces")); //$NON-NLS-1$
			e1.printStackTrace();
		}
		
		Enumeration<Vector<AbstractPiPlugin>> enu = pluginInstanceReferences.elements();
		while (enu.hasMoreElements())
		{
			// make sure the cast is valid
			Vector<AbstractPiPlugin> tmp = null;
			try {
				tmp = enu.nextElement();
			} catch (ClassCastException e1) {
				e1.printStackTrace();
			}

			if (tmp != null)
			{
				Enumeration<AbstractPiPlugin> e = tmp.elements();
				while (e.hasMoreElements())
				{
					// make sure the cast is valid
					try {
						plugin = (AbstractPiPlugin)e.nextElement();
					} catch (ClassCastException e1) {
						e1.printStackTrace();
					}

					if (matchClass.isInstance(plugin))
					{
						instances.add(plugin);
					}
				}
			}
		}

		return instances.elements();
	}
	
	public static Class getPluginClass(String className)
	{
		// because only trace types will be returned by getPluginInstances(),
		// and PiPlugin is not a trace type, call its class replacer explicitly 
		Class c = PiPlugin.getReplacedClass(className);
		if (c != null)
			return c;

		Enumeration<AbstractPiPlugin> e = getPluginInstances("com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer"); //$NON-NLS-1$
		while(e.hasMoreElements())
		{
			IClassReplacer rep = (IClassReplacer)e.nextElement();
			c = rep.getReplacedClass(className);
			if (c != null)
				return c;
		}
		
		return null;
	}
	
	public static void removeTraceInstances(int id) 
	{
		if (id == 0) return; //not removing top level plugin instances
		if (pluginInstanceReferences != null)
		{
			pluginInstanceReferences.remove(Integer.valueOf(id));
		}
	}
	
	public static void removeAllTraceInstances()
	{
		if (pluginInstanceReferences != null)
		{
			Enumeration<Integer> e = pluginInstanceReferences.keys();
			while (e.hasMoreElements())
			{
				Integer i = (Integer)e.nextElement();
				if (i != 0)
					pluginInstanceReferences.remove(i);
			}
		}
	}
	
	private static void addInstanceReference(int id, AbstractPiPlugin plugin)
	{
		if (pluginInstanceReferences == null)
			pluginInstanceReferences = new Hashtable<Integer, Vector<AbstractPiPlugin>>();
		Vector<AbstractPiPlugin> tmp = pluginInstanceReferences.get(Integer.valueOf(id));
		if (tmp != null)
		{
			// do not allow duplicate plugins
			if (!tmp.contains(plugin))
				tmp.add(plugin);
		}
		else
		{
			Vector<AbstractPiPlugin> tmp2 = new Vector<AbstractPiPlugin>();
			tmp2.add(plugin);
			pluginInstanceReferences.put(Integer.valueOf(id), tmp2);
		}
	}
}
