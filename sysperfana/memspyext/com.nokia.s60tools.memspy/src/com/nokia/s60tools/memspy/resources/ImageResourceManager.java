/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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


package com.nokia.s60tools.memspy.resources;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


/**
 * Class for handling MemSpy icons.
 *
 */
public class ImageResourceManager {

	/**
	 * Load images
	 * @param imagesPath
	 */
	public static void loadImages(String imagesPath){
		
    	Display disp = Display.getCurrent();
    	
    	ImageRegistry imgReg = JFaceResources.getImageRegistry();
    	
    	//
    	// Storing images to image registry
    	//    	
    	Image img = new Image( disp, imagesPath + "\\memspy_16.png" );
        imgReg.put( ImageKeys.IMG_APP_ICON, img );

        img = new Image( disp, imagesPath + "\\Launch_SWMT.png" );
        imgReg.put( ImageKeys.IMG_LAUNCH_SWMT, img );

        img = new Image( disp, imagesPath + "\\Export_To_html.png" );
        imgReg.put( ImageKeys.IMG_EXPORT_TO_HTML, img );

        img = new Image( disp, imagesPath + "\\Compare_2_Heaps.png" ); 
        imgReg.put( ImageKeys.IMG_COMPARE_2_HEAP, img );

        img = new Image( disp, imagesPath + "\\Analyse_Heap.png" );
        imgReg.put( ImageKeys.IMG_ANALYZE_HEAP, img );

        img = new Image( disp, imagesPath + "\\Launch_SWMT.png" );
        imgReg.put( ImageKeys.IMG_SWMT_LOG, img );
        
        img = new Image( disp, imagesPath + "\\Analyse_Heap.png" );
        imgReg.put( ImageKeys.IMG_HEAP_DUMP, img );

        img = new Image( disp, imagesPath + "\\Memspy_45.png" );
        imgReg.put( ImageKeys.IMG_WIZARD, img );

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
