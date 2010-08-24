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

package com.nokia.s60tools.crashanalyser.containers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.nokia.s60tools.crashanalyser.model.XmlUtils;

/**
 * This class contains basic information about a crash such as
 * crash date and time, product version etc. 
 *
 */
public final class Summary {
	
	// XML tags
	public static final String TAG_SEG_HEADER = "seg_header";
	public static final String TAG_DATE = "date";
	public static final String TAG_TIME = "time";
	public static final String TAG_UPTIME = "uptime";
	public static final String TAG_CRASH_SOURCE = "crash_source";
	public static final String TAG_SEG_HW_INFO = "seg_hw_info";
	public static final String TAG_VERSION = "version_text";
	public static final String TAG_VERSIONS = "version_text_list";
	public static final String TAG_PRODUCT_TYPE = "product_type";
	public static final String TAG_PRODUCT_CODE = "product_code";
	public static final String TAG_SERIAL_NUMBER = "serial_number";
	public static final String TAG_PRODUCTION_MODE = "production_mode";
	public static final String TAG_SEG_SW_INFO = "seg_sw_info";
	public static final String TAG_LANGUAGE = "language";
	public static final String TAG_CHECKSUM = "checksum";
	public static final String TAG_IMEI = "imei";
	public static final String TAG_SEG_MEMORY_INFO = "seg_memory_info";
	public static final String TAG_RAM = "ram";
	public static final String TAG_DRIVE = "drive";
	public static final String TAG_FREE = "free";
	
	public static final String FORMAT = "%-15s: %s";

	// Data
	private final String summaryCrashTime;
	private final String summaryCrashDate;
	private final String summaryUpTime;
	private final String[] summarySwVersion;
	private final String summaryLanguage;
	private final String summaryRomId;
	private final String[] summaryHwVersion;
	private final String summaryProductType;
	private final String summaryProductCode;
	private final String summarySerialNumber;
	private final String summaryImei;
	private final String summaryFreeRam;
	private final String summaryFreeDisk;
	private final String summaryProductionMode;
	private final String summaryCrashSource;
	
	private Summary(final String crashTime, final String crashDate, final String upTime, final String[] swVersion, final String language,
					final String romId, final String[] hwVersion, final String productType, final String productCode, 
					final String serialNumber, final String productionMode, final String crashSource, final String imei, final String freeRam, final String freeDisk) {
		summaryCrashTime = crashTime;
		summaryCrashDate = crashDate;
		summaryUpTime = upTime;
		summarySwVersion = swVersion.clone();
		summaryLanguage = language;
		summaryRomId = romId;
		summaryHwVersion = hwVersion.clone();
		summaryProductType = productType;
		summaryProductCode = productCode;
		summarySerialNumber = serialNumber;
		summaryImei = imei;
		summaryFreeRam = freeRam;
		summaryFreeDisk = freeDisk;
		summaryProductionMode = productionMode;
		summaryCrashSource = crashSource;
	}
	
	/**
	 * Writes summary data into given buffer (i.e. text file)
	 * @param out
	 * @throws IOException
	 */
	public void writeTo(final BufferedWriter out) throws IOException {
		writeLine(out, "Crash Time", summaryCrashTime);
		writeLine(out, "Crash Date", summaryCrashDate);
		writeLine(out, "Up Time", summaryUpTime);
		if (summarySwVersion != null && summarySwVersion.length > 0) {
			for (int i = 0; i < summarySwVersion.length; i++) {
				writeLine(out, "SW Version", summarySwVersion[i]);
			}
		}
		writeLine(out, "Language", summaryLanguage);
		writeLine(out, "Rom Id", summaryRomId);
		if (summaryHwVersion != null && summaryHwVersion.length > 0) {
			for (int i = 0; i < summaryHwVersion.length; i++) {
				writeLine(out, "HW Version", summaryHwVersion[i]);
			}
		}
		writeLine(out, "Product Type", summaryProductType);
		writeLine(out, "Product Code", summaryProductCode);
		writeLine(out, "Serial Number", summarySerialNumber);
		writeLine(out, "Imei", summaryImei);
		writeLine(out, "Free Ram", summaryFreeRam);
		writeLine(out, "Free Disk Space", summaryFreeDisk);
		writeLine(out, "Production Mode", summaryProductionMode);
		writeLine(out, "Crash Source", summaryCrashSource);
	}
	
	void writeLine(final BufferedWriter out, final String header, final String value) throws IOException {
		if (!"".equals(value)) {
			out.write(String.format(FORMAT, header, value));
			out.newLine();
		}
	}
	
	/**
	 * Reads and creates summary data from given root xml element
	 * @param rootElement
	 * @return created summary data or null
	 */
	public static Summary read(final Element rootElement) {
		try {
			String crashTime = "";
			String crashDate = "";
			String upTime = "";
			String crashSource = "";
			
			// read data under seg_header node
			final NodeList segHeader = rootElement.getElementsByTagName(TAG_SEG_HEADER);
			if (segHeader != null && segHeader.getLength() > 0) {
				// read crash time
				crashTime = XmlUtils.getTextValue((Element)segHeader.item(0), TAG_TIME);
				if (crashTime == null) {
					crashTime = "";
				} else if (!crashTime.contains(":") && crashTime.length() == 6){
					final String time = crashTime.substring(0, 2) + ":" +
								  crashTime.substring(2, 4) + ":" +
								  crashTime.substring(4, 6);
					crashTime = time;
				}
				
				// read crashdate
				crashDate = XmlUtils.getTextValue((Element)segHeader.item(0), TAG_DATE);
				if (crashDate == null) {
					crashDate = "";
				} else if (crashDate.length() == 8) {
					final String date = crashDate.substring(0,4) + "-" + 
									crashDate.substring(4,6) + "-" +
									crashDate.substring(6,8);
					crashDate = date;
				}
				
				// read uptime
				upTime = XmlUtils.getTextValue((Element)segHeader.item(0), TAG_UPTIME);
				if (upTime == null) {
					upTime = "";
				} else {
					upTime = convertUpTime(upTime);
				}
				
				// read crash source
				crashSource = XmlUtils.getTextValue((Element)segHeader.item(0), TAG_CRASH_SOURCE);
				if(crashSource == null)
					crashSource = "";
			}
			
			final List<String> hwVersions = new ArrayList<String>();
			String productType = "";
			String productCode = "";
			String serialNumber = "";
			String productionMode = "";
			
			// read data under seg_hw_info node
			final NodeList segHwInfo = rootElement.getElementsByTagName(TAG_SEG_HW_INFO);
			if (segHwInfo != null && segHwInfo.getLength() > 0) {

				// read all versions
				final NodeList versions = ((Element)segHwInfo.item(0)).getElementsByTagName(TAG_VERSION);
				if (versions != null && versions.getLength() > 0) {
					for (int i = 0; i < versions.getLength(); i++) {
						// read sw version
						final String version = XmlUtils.getNodeValue(versions.item(i));
						if (version != null)
							hwVersions.add(version);
					}
				}
				
				// read product type
				productType = XmlUtils.getTextValue((Element)segHwInfo.item(0), TAG_PRODUCT_TYPE);
				if (productType == null)
					productType = "";
				
				// read product code
				productCode = XmlUtils.getTextValue((Element)segHwInfo.item(0), TAG_PRODUCT_CODE);
				if (productCode == null)
					productCode = "";
				
				// read serial number
				serialNumber = XmlUtils.getTextValue((Element)segHwInfo.item(0), TAG_SERIAL_NUMBER);
				if (serialNumber == null)
					serialNumber = "";

				// read production mode
				productionMode = XmlUtils.getTextValue((Element)segHwInfo.item(0), TAG_PRODUCTION_MODE);
				if (productionMode == null)
					productionMode = "";
			}
			
			final List<String> swVersions = new ArrayList<String>();
			String language = "";
			String romId = "";
			
			// read data under seg_sw_info node
			final NodeList segSwInfo = rootElement.getElementsByTagName(TAG_SEG_SW_INFO);
			if (segSwInfo != null && segSwInfo.getLength() > 0) {

				// read all versions
				final NodeList versions = ((Element)segSwInfo.item(0)).getElementsByTagName(TAG_VERSION);
				if (versions != null && versions.getLength() > 0) {
					for (int i = 0; i < versions.getLength(); i++) {
						// read sw version
						final String version = XmlUtils.getNodeValue(versions.item(i));
						if (version != null)
							swVersions.add(version);
					}
				}
				
				// read language
				language = XmlUtils.getTextValue((Element)segSwInfo.item(0), TAG_LANGUAGE);
				if (language == null)
					language = "";
				
				// read rom id
				romId = XmlUtils.getTextValue((Element)segSwInfo.item(0), TAG_CHECKSUM);
				if (romId == null)
					romId = "";
			}
			
			String imei = "";
			// read imei value
			final NodeList imeiNode = rootElement.getElementsByTagName(TAG_IMEI);
			if (imeiNode != null && imeiNode.getLength() > 0) {
				imei = imeiNode.item(0).getFirstChild().getNodeValue();
			}
			
			String freeRamAmount = "";
			String freeDiskSpace = "";
			
			// read free ram amount
			final NodeList segMemoryInfo = rootElement.getElementsByTagName(TAG_SEG_MEMORY_INFO);
			if (segMemoryInfo != null && segMemoryInfo.getLength() > 0) {
				final NodeList ram = ((Element)segMemoryInfo.item(0)).getElementsByTagName(TAG_RAM);
				if (ram != null && ram.getLength() > 0) {
					freeRamAmount = XmlUtils.getTextValue((Element)ram.item(0), TAG_FREE);
					if (freeRamAmount == null)
						freeRamAmount = "";
				}
				final NodeList drive = ((Element)segMemoryInfo.item(0)).getElementsByTagName(TAG_DRIVE);
				if (drive != null && drive.getLength() > 0) {
					freeDiskSpace = XmlUtils.getTextValue((Element)drive.item(0), TAG_FREE);
					if (freeDiskSpace == null)
						freeDiskSpace = "";
				}
			}
			
			return new Summary(crashTime, crashDate, upTime, swVersions.toArray(new String[swVersions.size()]), 
								language, romId, hwVersions.toArray(new String[hwVersions.size()]), productType, 
								productCode, serialNumber, productionMode, crashSource, imei, freeRamAmount, freeDiskSpace);
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getCrashTime() {
		return summaryCrashTime;
	}
	
	public String getCrashDate() {
		return summaryCrashDate;
	}
	
	public String getUpTime() {
		return summaryUpTime;
	}
	
	public String getRomId() {
		return summaryRomId;
	}
	
	public String[] getSwVersion() {
		return summarySwVersion.clone();
	}
	
	public String[] getHwVersion() {
		return summaryHwVersion.clone();
	}
	
	public String getProductType() {
		return summaryProductType;
	}
	
	public String getProductCode() {
		return summaryProductCode;
	}
	
	public String getLanguage() {
		return summaryLanguage;
	}
	
	public String getSerialNumber() {
		return summarySerialNumber;
	}
	
	public String getImei() {
		return summaryImei;
	}
	
	public String getFreeRam() {
		return summaryFreeRam;
	}

	public String getFreeDisk() {
		return summaryFreeDisk;
	}

	public String getProductionMode() {
		return summaryProductionMode;
	}
	
	public String getCrashSource() {
		return summaryCrashSource;
	}

	
	static String convertUpTime(final String uptime) {
		try {
			final int secs =Integer.valueOf(uptime); 
			final int hours =  secs / 3600,
			remainder = secs % 3600,
			minutes = remainder / 60,
			seconds = remainder % 60;
	
			return ( (hours < 10 ? "0" : "") + hours
			+ ":" + (minutes < 10 ? "0" : "") + minutes
			+ ":" + (seconds< 10 ? "0" : "") + seconds );
		} catch (Exception e) {
			return uptime;
		}
	}
}
