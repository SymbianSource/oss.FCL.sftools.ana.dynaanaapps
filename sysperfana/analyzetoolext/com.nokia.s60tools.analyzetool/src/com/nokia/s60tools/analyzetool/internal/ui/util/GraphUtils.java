/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class GraphUtils
 *
 */
package com.nokia.s60tools.analyzetool.internal.ui.util;
import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Utilities class for the graph
 *
 */
public final class GraphUtils {
	private static final String MILLISECONDS = "ms"; //$NON-NLS-1$
	private static final String SECONDS = "s"; //$NON-NLS-1$
	private static final String MINUTES = "m"; //$NON-NLS-1$
	private static final String HOURS = "h"; //$NON-NLS-1$
	private static final String DAYS = "d"; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final int KILOBYTE = 1024;
	private static final int MEGABYTE = 1024*1024;
	private static final int GIGABYTE = 1024*1024*1024;
	private static final DecimalFormat MB_FORMAT = new DecimalFormat("#####.0");
	private static final DecimalFormat BYTES_FORMAT = new DecimalFormat("#####.##");
	
	
	//make constructor private so class doesn't get instantiated
	private GraphUtils(){
		//do nothing by design
	}
	
	/**
	 * 
	 * @param aBytes
	 * @return next rounded value in Bytes.
	 */
	public static int prettyMaxBytes(final int aBytes) {
		int bytes = aBytes;
		if (bytes <= KILOBYTE)
			bytes = 1024;
		else if (bytes <= 10 * KILOBYTE)
			bytes = 10 * KILOBYTE;
		else if (bytes <= 20 * KILOBYTE)
			bytes = 20 * KILOBYTE;
		else if (bytes <= 30 * KILOBYTE)
			bytes = 30 * KILOBYTE;
		else if (bytes <= 50 * KILOBYTE)
			bytes = 50 * KILOBYTE;
		else if (bytes <= 100 * KILOBYTE)
			bytes = 100 * KILOBYTE;
		else if (bytes <= 200 * KILOBYTE)
			bytes = 200 * KILOBYTE;
		else if (bytes <= 300 * KILOBYTE)
			bytes = 300 * KILOBYTE;
		else if (bytes <= 500 * KILOBYTE)
			bytes = 500 * KILOBYTE;
		else if (bytes <= MEGABYTE)
			bytes = MEGABYTE;
		else if (bytes <= 2 * MEGABYTE)
			bytes = 2 * MEGABYTE;
		else if (bytes <= 3 * MEGABYTE)
			bytes = 3 * MEGABYTE;
		else if (bytes <= 5 * MEGABYTE)
			bytes = 5 * MEGABYTE;
		else if (bytes <= 10 * MEGABYTE)
			bytes = 10 * MEGABYTE;
		else if (bytes <= 20 * MEGABYTE)
			bytes = 20 * MEGABYTE;
		else if (bytes <= 30 * MEGABYTE)
			bytes = 30 * MEGABYTE;
		else if (bytes <= 50 * MEGABYTE)
			bytes = 50 * MEGABYTE;
		else if (bytes <= 100 * MEGABYTE)
			bytes = 100 * MEGABYTE;
		else if (bytes <= 200 * MEGABYTE)
			bytes = 200 * MEGABYTE;
		else if (bytes <= 300 * MEGABYTE)
			bytes = 300 * MEGABYTE;
		else if (bytes <= 500 * MEGABYTE)
			bytes = 500 * MEGABYTE;
		else
			bytes = ((bytes + GIGABYTE - 1) / GIGABYTE) * GIGABYTE;

		return bytes;
	}
	/**
	 * Draws the given String and an arrow on an image and returns it.
	 * @param name The string to display
	 * @return the newly created image
	 */
	public static Image getVerticalLabel(final String name)
	{
		final Image image = new Image(Display.getDefault(), 90, 14);
	    final GC gc = new GC(image);
	    final Font font = new Font(Display.getDefault(), Display.getDefault().getSystemFont().getFontData()[0].getName(), 9, SWT.BOLD);
	    gc.setFont(font);
	    gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
	    gc.fillRectangle(0, 0, 90, 15);
	    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
	    gc.drawText(name + " ->", 0, 0, true);
	    font.dispose();
	    gc.dispose();
	    return image;
	}
	/**
	 * Calculates the next larger or smaller scale. Used typically when zooming in / out.
	 * @param scale the current scale
	 * @param bigger flag to indicate whether to increase or decrease the scale
	 * @return the new scale
	 */
	public static double nextScale(final double scale, final boolean bigger)
    {
    	double logScale = Math.log10(scale);
    	double floorLogScale = Math.floor(Math.log10(scale));
    	double mostSignificantDigit =  Math.rint(Math.pow(10, (logScale - floorLogScale)));
    	double powerOfTen = Math.pow(10, floorLogScale);
   	
    	if (bigger) {
    		if (mostSignificantDigit < 2) {
    			mostSignificantDigit = 2;
    		} else if (mostSignificantDigit < 5) {
    			mostSignificantDigit = 5;
    		} else {
    			mostSignificantDigit = 10;
    		}
    	} else {
    		if (mostSignificantDigit > 5) {
    			mostSignificantDigit = 5;
    		} else if (mostSignificantDigit > 2) {
    			mostSignificantDigit = 2;
    		} else if (mostSignificantDigit > 1) {
    			mostSignificantDigit = 1;
    		} else {
    			mostSignificantDigit = 0.5;
    		}
    	}

    	double result = mostSignificantDigit * powerOfTen;
 
    	if (result < 0.1)
        	result = 0.1;

    	return result;
    }
	/**
	 * Returns the given amount of microseconds as user-friendly formatted string,
	 * for example "3d 5h 20min 2d 3ms"
	 * The returned string can be quite long. Use {@link #renderTime(double)} for
	 * a short version
	 * @param aTotalMicroSeconds the given amount of microseconds to convert
	 * @return the formatted time string
	 */
	@SuppressWarnings("nls")
	public static String renderTime(final double aTotalMicroSeconds) { 
		double totalMicroSeconds = aTotalMicroSeconds;
		long days, hours, minutes, seconds, ms;
		days= (long) (totalMicroSeconds / 86400000000L);
		totalMicroSeconds -= days * 86400000000L;
		hours = (long) (totalMicroSeconds / 3600000000L);
		totalMicroSeconds -= hours * 3600000000L;
		minutes = (long) totalMicroSeconds / 60000000;
		totalMicroSeconds -= minutes * 60000000;
		seconds = (long) totalMicroSeconds / 1000000;
		totalMicroSeconds -= seconds * 1000000;
		ms = (long)totalMicroSeconds / 1000;
		
		StringBuilder result= new StringBuilder();
		if(days > 0) {
			result.append(days).append(DAYS);
		} 
		if(hours > 0) {
			if (result.length() > 0){
				result.append(SPACE);
			}
			result.append(hours).append(HOURS);
		} 
		if(minutes > 0) {
			if (result.length() > 0){
				result.append(SPACE);
			}
			result.append(minutes).append(MINUTES);
		} 
		if(seconds > 0) {
			if (result.length() > 0){
				result.append(SPACE);
			}
			result.append(seconds).append(SECONDS);
		}
		if (ms > 0){
			if (result.length() > 0){
				result.append(SPACE);
			}
			result.append(ms).append(MILLISECONDS);
		}
		
		if (result.length() == 0){
			result.append(0).append(MILLISECONDS);			
		}
		return result.toString();
	}
	
	/**
	 * Formats the given number of bytes into an easily readable format 
	 * @param bytes The number of bytes to format
	 * @return String containing a formatted version of the input data 
	 */
	public static String formatBytes(final double bytes) {
		String scaledY;

		if (bytes < 10000) {
			scaledY = BYTES_FORMAT.format((long)bytes) + " B"; 
		} else if (bytes <= 500 * 1024) {
			scaledY = BYTES_FORMAT.format(bytes / 1024) + " KB";
		} else {
			scaledY = MB_FORMAT.format(((float) bytes / (1024 * 1024))) + " MB";
		}
		return scaledY;
	}

	/**
	 * Formats given time value to String with units. The resulting String
	 * is formatted quite short, e.g.:
	 * <br>"30s" 
	 * <br>"1m 30s" 
	 * <br>"40m"
	 * <br>"1h 30m"
	 * @param aTime Time in microseconds
	 * @return time String with units
	 */
	public static String getTimeStringWithUnits(final double aTime) {
		long time = (long) aTime / 1000; //convert from microseconds to milliseconds
		String formatted ;
		
		//If we have only less than 10 seconds, show seconds and milliseconds
		if(time < 6000){
			//"s.S's'"
			int seconds = (int) time / 1000;
			long ms = time - (seconds * 1000);
			if (ms == 0 ){
				formatted = String.format("%ds", seconds);				
			} else {
				formatted = String.format("%ds%03d", seconds, ms);				
			}
		}
		//If we have 10s or more, but less than 1min, show seconds
		else if(time < 60000){
			//"s's'"
			formatted = String.format("%ds", (int) time / 1000);
		}
		//If we have more than one minute, but less than one 5min, showing the seconds as well
		else if(time >= 60000 && time < (60000*5)){
			//"m'm' s's'"
			long minutes = (int) time / 60000;
			long seconds = (time - minutes * 60000) / 1000;
			if (seconds == 0 ){
				formatted = String.format("%dm", minutes);
			} else {
				formatted = String.format("%dm %ds", minutes, seconds);
			}
		}
		//If we have more than five minute, but less than one hour, we show only minutes
		else if(time >= (60000*5) && time < (60*60*1000)){
			//"m'm'"
			formatted = String.format("%dm", (int) time / 60000);
		}		
		else{
			//"H'h' m'm'"
			long hours = (time / 3600000);
			long minutes = (time - hours * 3600000) / 60000;
			
			if (minutes == 0){
				formatted = String.format("%dh", hours);				
			} else {
				formatted = String.format("%dh %dm", hours, minutes);				
			}

		}		
		return formatted;
	}
}
