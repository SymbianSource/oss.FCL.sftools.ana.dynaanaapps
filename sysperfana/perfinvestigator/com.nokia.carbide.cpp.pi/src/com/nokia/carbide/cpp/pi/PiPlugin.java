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

package com.nokia.carbide.cpp.pi;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleContext;

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.CusSample;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.GenericEvent;
import com.nokia.carbide.cpp.internal.pi.model.GenericEventTrace;
import com.nokia.carbide.cpp.internal.pi.model.GenericSample;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampleWithFunctions;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTraceWithFunctions;
import com.nokia.carbide.cpp.internal.pi.model.GenericThread;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.test.BappeaAnalysisInfo;
import com.nokia.carbide.cpp.internal.pi.test.EnabledTrace;
import com.nokia.carbide.cpp.internal.pi.test.PIAnalysisInfo;
import com.nokia.carbide.cpp.internal.pi.test.TraceAdditionalInfo;


/**
 * The main plugin class to be used in the desktop.
 */
public class PiPlugin extends AbstractPiPlugin {

	//The shared instance.
	private static PiPlugin plugin;
	
	public final static String PLUGIN_ID = "com.nokia.carbide.cpp.pi"; //$NON-NLS-1$
	
	private static void setPlugin(PiPlugin localPlugin) {
		plugin = localPlugin;
	}

	/**
	 * The constructor.
	 */
	public PiPlugin() {
		setPlugin(this);
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
		setPlugin(null);
	}

	/**
	 * Returns the shared instance.
	 */
	public static PiPlugin getDefault() {
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
		ImageDescriptor descriptor = getDefault().getImageRegistry().getDescriptor(path);
		if (descriptor == null) {
			descriptor = ImageDescriptor.createFromURL(getDefault().getBundle().getEntry(path));
			getDefault().getImageRegistry().put(path, descriptor);
		}
		return descriptor;
	}
	/**
	 * Returns an Image for the image file at the given
	 * plug-in relative path. The Image is stored in the image 
	 * registry if not already there. 
	 * @param aLocation path of the image
	 * @return the Image for the given image location
	 */
	public static Image getImage(String aLocation){
		getImageDescriptor(aLocation); //make sure the ImageDescriptor gets created
		return getDefault().getImageRegistry().get(aLocation);
	}

	public static Class getReplacedClass(String className) {
        // handle legacy PI analysis file class names
    	if (!className.startsWith("fi.")) //$NON-NLS-1$
    		return null;
    	
    	if (className.equals("fi.vtt.bappea.model.GenericSample")) //$NON-NLS-1$
    		return GenericSample.class;
    	
        if (className.equals("fi.vtt.bappea.model.GenericSampledTrace")) { //$NON-NLS-1$
        	return GenericSampledTrace.class;
        }
        
        if (className.equals("fi.vtt.bappea.model.GenericEventTrace")) { //$NON-NLS-1$
        	return GenericEventTrace.class;
        }
        
        if (className.equals("fi.vtt.bappea.model.GenericEvent")) { //$NON-NLS-1$
        	return GenericEvent.class;
        }
        
        if (className.equals("fi.vtt.bappea.model.GenericTrace")) { //$NON-NLS-1$
        	return GenericTrace.class;
        }
        
        if (className.equals("fi.vtt.bappea.model.GenericThread")) { //$NON-NLS-1$
        	return GenericThread.class;
        }
        
        if (className.equals("fi.vtt.bappea.model.GenericSampledTraceWithFunctions")) { //$NON-NLS-1$
        	return GenericSampledTraceWithFunctions.class;
        }
        
        if (className.equals("fi.vtt.bappea.model.GenericSampleWithFunctions")) { //$NON-NLS-1$
        	return GenericSampleWithFunctions.class;
        }
        
        if (className.equals("fi.vtt.bappea.model.Function")) { //$NON-NLS-1$
        	return Function.class;
        }
        
        if (className.equals("fi.vtt.bappea.model.Binary")) { //$NON-NLS-1$
        	return Binary.class;
        }
        
        if (className.equals("fi.vtt.bappea.model.CusSample")) { //$NON-NLS-1$
        	return CusSample.class;
        }

        if (className.equals("fi.vtt.bappea.test.PIAnalysisInfo")) { //$NON-NLS-1$
        	return PIAnalysisInfo.class;
        }

        if (className.equals("fi.vtt.bappea.test.EnabledTrace")) { //$NON-NLS-1$
        	return EnabledTrace.class;
        }

        if (className.equals("fi.vtt.bappea.test.TraceAdditionalInfo")) { //$NON-NLS-1$
        	return TraceAdditionalInfo.class;
        }

        if (className.equals("fi.vtt.bappea.test.BappeaAnalysisInfo")) { //$NON-NLS-1$
        	return BappeaAnalysisInfo.class;
        }

		return null;
	}
}
