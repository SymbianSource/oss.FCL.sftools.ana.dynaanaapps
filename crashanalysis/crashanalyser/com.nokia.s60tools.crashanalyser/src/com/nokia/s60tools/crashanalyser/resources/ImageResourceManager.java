/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
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
 
package com.nokia.s60tools.crashanalyser.resources;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


/**
 * Class for handling Crash Analyser icons.
 *
 */
public final class ImageResourceManager {
	
	private ImageResourceManager() {
		// not meant to be instantiated
	}

	/**
	 * Loads CrashAnalyser images into image registry
	 * @param imagesPath path where images are
	 */
	public static void loadImages(String imagesPath){
		
    	Display disp = Display.getCurrent();
    	
    	ImageRegistry imgReg = JFaceResources.getImageRegistry();
    	
    	Image img = new Image( disp, imagesPath + "\\crash_analyser_16.png" );        	 //$NON-NLS-1$
        imgReg.put( ImageKeys.IMG_APP_ICON, img );

        img = new Image( disp, imagesPath + "\\crash_analyser_55_bannered.png" );        	 //$NON-NLS-1$
        imgReg.put( ImageKeys.WIZARD_BANNER, img );
        
        img = new Image( disp, imagesPath + "\\decode_files.png"); //$NON-NLS-1$
        imgReg.put( ImageKeys.DECODE_FILES, img);
        
        img = new Image( disp, imagesPath + "\\emulator.png");
        imgReg.put( ImageKeys.EMULATOR_PANIC, img);
        
        img = new Image( disp, imagesPath + "\\error_library.png");
        imgReg.put( ImageKeys.ERROR_LIBRARY, img);

        img = new Image( disp, imagesPath + "\\file_de_coded.png");
        imgReg.put( ImageKeys.DECODED_FILE, img);

        img = new Image( disp, imagesPath + "\\file_part_coded.png");
        imgReg.put( ImageKeys.PARTIALLY_DECODED_FILE, img);

        img = new Image( disp, imagesPath + "\\file_coded.png");
        imgReg.put( ImageKeys.CODED_FILE, img);
        
        img = new Image( disp, imagesPath + "\\selected.png");
        imgReg.put( ImageKeys.SELECTED_SDK, img);

	}
	
	/**
	 * Returns an image descriptor for given image key
	 * @param key image key
	 * @return image descriptor
	 */
	public static ImageDescriptor getImageDescriptor( String key ){
    	ImageRegistry imgReg = JFaceResources.getImageRegistry();
    	return  imgReg.getDescriptor( key );		
	}	

	/**
	 * Returns an image for given image key
	 * @param key image key
	 * @return image
	 */
	public static Image getImage( String key ){
    	ImageRegistry imgReg = JFaceResources.getImageRegistry();    	
    	return  imgReg.get(key);		
	}	
}
