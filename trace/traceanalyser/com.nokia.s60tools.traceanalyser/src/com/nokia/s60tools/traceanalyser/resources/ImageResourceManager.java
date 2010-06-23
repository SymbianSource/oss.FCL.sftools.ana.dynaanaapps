/*
* ============================================================================
*  Revision    : $Rev:$ 
*
*  Copyright © 2009 Nokia.  All rights reserved.
*  This material, including documentation and any related computer
*  programs, is protected by copyright controlled by Nokia.  All
*  rights are reserved.  Copying, including reproducing, storing,
*  adapting or translating, any or all of this material requires the
*  prior written consent of Nokia.  This material also contains
*  confidential information which may not be disclosed to others
*  without the prior written consent of Nokia.
============================================================================
*/

package com.nokia.s60tools.traceanalyser.resources;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


/**
 * Class for handling Trace Analyser icons.
 *
 */
public class ImageResourceManager {

	public static void loadImages(String imagesPath){
		
    	Display disp = Display.getCurrent();
    	
    	ImageRegistry imgReg = JFaceResources.getImageRegistry();
    	
    	//
    	// Storing images to image registry
    	//
    	
    	/*******************************************************************************
    	 * Copyright for the following group of images.
    	 * Copyright ©2009 Nokia Corporation. All rights reserved.
    	 *******************************************************************************/
    	Image img = new Image( disp, imagesPath + "\\TraceAnalyser.png" );
        imgReg.put( ImageKeys.IMG_APP_ICON, img );

        img = new Image( disp, imagesPath + "\\ClearAllCounters.png" );
        imgReg.put( ImageKeys.IMG_CLEAR_ALL, img );

        img = new Image( disp, imagesPath + "\\CreateNewRule.png" ); 
        imgReg.put( ImageKeys.IMG_CREATE_NEW, img );
        
        img = new Image( disp, imagesPath + "\\FailReceived.png" ); 
        imgReg.put( ImageKeys.IMG_FAIL_RECEIVED, img );
        
        img = new Image( disp, imagesPath + "\\ClearFailLog.png" ); 
        imgReg.put( ImageKeys.IMG_CLEAR_FAIL_LOG, img );

        img = new Image( disp, imagesPath + "\\TraceAnalyser_banner.png" ); 
        imgReg.put( ImageKeys.IMG_TRACE_ANALYSER_BANNER, img );

	}
	
	public static ImageDescriptor getImageDescriptor( String key ){
    	ImageRegistry imgReg = JFaceResources.getImageRegistry();
    	return  imgReg.getDescriptor( key );		
	}	

	public static Image getImage( String key ){
    	ImageRegistry imgReg = JFaceResources.getImageRegistry();    	
    	return  imgReg.get(key);		
	}	
}
