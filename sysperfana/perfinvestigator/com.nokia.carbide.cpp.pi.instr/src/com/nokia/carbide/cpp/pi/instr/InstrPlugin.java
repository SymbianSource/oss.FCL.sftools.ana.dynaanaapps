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

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IClassReplacer;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;


/**
 * The main plugin class to be used in the desktop.
 */
public class InstrPlugin extends AbstractPiPlugin
			implements ITrace, IClassReplacer {

	//The shared instance.
	private static InstrPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public InstrPlugin() {
		plugin = this;
	}

	public Class getReplacedClass(String className)
	{
		if (   className.indexOf("com.nokia.carbide.cpp.pi.instr.IttTrace122") != -1 //$NON-NLS-1$
			|| className.indexOf("com.nokia.carbide.pi.instr.IttTrace122") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.model.IttTrace122") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.ittTracePlugin.IttTrace122") != -1) //$NON-NLS-1$
		{
			return IttTrace122.class;
		}
		if (   className.indexOf("com.nokia.carbide.cpp.pi.instr.IttEvent122") != -1 //$NON-NLS-1$
			|| className.indexOf("com.nokia.carbide.pi.instr.IttEvent122") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.model.IttEvent122") != -1 //$NON-NLS-1$
			|| className.indexOf("fi.vtt.bappea.ittTracePlugin.IttEvent122") != -1) //$NON-NLS-1$
		{
			return IttEvent122.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.instr.IttTrace") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.instr.IttTrace") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.model.IttTrace") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.ittTracePlugin.IttTrace") != -1) //$NON-NLS-1$
		{
			return IttTrace.class;
		}
		else if (   className.indexOf("com.nokia.carbide.cpp.pi.instr.IttSample") != -1 //$NON-NLS-1$
				 || className.indexOf("com.nokia.carbide.pi.instr.IttSample") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.model.IttSample") != -1 //$NON-NLS-1$
				 || className.indexOf("fi.vtt.bappea.ittTracePlugin.IttSample") != -1) //$NON-NLS-1$
		{
			return IttSample.class;
		}
		else
		{
			return null;
		}
	}
	
	public Class getTraceClass() 
	{
		//return IttTrace.class;
		// may be bad, but we should only support the new ITT
		return IttTrace122.class;
	}

	public void initialiseTrace(GenericTrace trace) 
	{
		//no action required at the moment - maybe some event casting in future
	}

	public String getTraceName() {
		return "Dynamic Binary Support"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#getTraceTitle()
	 */
	public String getTraceTitle() {
		return Messages.getString("InstrPlugin.0"); //$NON-NLS-1$
	}

	public int getTraceId() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace#parseTraceFiles(java.io.File[])
	 */
	public ParsedTraceData parseTraceFiles(File[] files) throws Exception {
		throw new UnsupportedOperationException();
	}

	public ParsedTraceData parseTraceFile(File file /*, ProgressBar progressBar*/) throws Exception 
	{
		try
        {
            IttTraceParser ittParser;
            
            ittParser = new IttTraceParser();
            
            ParsedTraceData ptd = ittParser.parse(file/*,progressBar*/);
     		return ptd;
           
        } catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static InstrPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractPiPlugin.imageDescriptorFromPlugin("com.nokia.carbide.cpp.pi.instr", path); //$NON-NLS-1$
	}
}
