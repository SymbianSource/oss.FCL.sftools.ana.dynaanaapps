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
package com.nokia.s60tools.swmtanalyser.ui.graphs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Object for storing time to show in X-axis in UI. Handles time formatting.
 */
public class TimeObject {
	
	private String days = null;
	
	private String hourMinutesAndSeconds = null;
	
	
	/**
	 * Create a time object
	 * @param time
	 * @param scale
	 */
	public TimeObject(double time, double scale){
		init(time, scale);
	}

	/**
	 * Get the days with units, e.g. "1d" or "12d"
	 * @return the days <code>null</code> if not defined or less than 1 
	 */
	public String getDays() {
		return days;
	}

	/**
	 * Set day String with units, e.g. "1d"
	 * @param days the days to set
	 */
	public void setDays(String days) {
		this.days = days;
	}

	/**
	 * Get Hours, minutes and seconds as "h:m:s" -format
	 * @return the hourMinutesAndSeconds
	 */
	public String getHourMinutesAndSeconds() {
		return hourMinutesAndSeconds;
	}

	/**
	 * Set Hours, minutes and seconds as "h:m:s" -format
	 * @param hourMinutesAndSeconds the hourMinutesAndSeconds to set
	 */
	public void setHourMinutesAndSeconds(String hourMinutesAndSeconds) {
		this.hourMinutesAndSeconds = hourMinutesAndSeconds;
	}

	/**
	 * Check if days has been set for this time object
	 * @return <code>true</code> if there are days set, <code>false</code> otherwise.
	 */
	public boolean hasDays() {		
		return getDays() != null;
	}

	/**
	 * Scaling the time and decides how to show the time in UI.
	 * E.g. 
	 * <br>"30s" 
	 * <br>"1m 30s" 
	 * <br>"40m"
	 * <br>"1h 30m"
	 * @param time
	 * @param scale
	 * @return time with units to add in graph
	 */
	private void init(double time, double scale) {
	
		time = time * scale;
		long timeAsLong = (long)time * 1000;//Ms to s
		String hoursMinsAndSecs ;
				
		//To avoid TimeZone adding e.g. 2hours to time, using GMT
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));//$NON-NLS-1$ 				
		SimpleDateFormat hmsDf;//Date format for hours, minutes and seconds
		hmsDf = new SimpleDateFormat("H:m:s");//$NON-NLS-1$
		hmsDf.setCalendar(cal);
	
		//If it has been taken more than one day, also days must be calculated
		if(time > (60*60*24)){
			//Decrease one day for time, because SimpeDateFormat will show "0" days as day "1"
			timeAsLong = timeAsLong - (60*60*24*1000);
			SimpleDateFormat dDf = new SimpleDateFormat("D'd'");//$NON-NLS-1$
			dDf.setCalendar(cal);			
			Date date =  new Date (timeAsLong);				
			hoursMinsAndSecs = hmsDf.format(date );
			String days = dDf.format(date);
			setDays(days);
			setHourMinutesAndSeconds(hoursMinsAndSecs);
		}
		//If it has been taken less than day, we need only hours minutes and seconds
		else{
			Date date =  new Date (timeAsLong);				
			hoursMinsAndSecs = hmsDf.format(date );
			setHourMinutesAndSeconds(hoursMinsAndSecs);
		}
		
						
	}	
	
}
