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
	public static final String TAG_SEG_HW_INFO = "seg_hw_info";
	public static final String TAG_VERSION = "version_text";
	public static final String TAG_VERSIONS = "version_text_list";
	public static final String TAG_PRODUCT_TYPE = "product_type";
	public static final String TAG_PRODUCT_CODE = "product_code";
	public static final String TAG_SERIAL_NUMBER = "serial_number";
	public static final String TAG_SEG_SW_INFO = "seg_sw_info";
	public static final String TAG_LANGUAGE = "language";
	public static final String TAG_CHECKSUM = "checksum";
	public static final String TAG_IMEI = "imei";
	public static final String TAG_SEG_MEMORY_INFO = "seg_memory_info";
	public static final String TAG_RAM = "ram";
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
	
	private Summary(String crashTime, String crashDate, String upTime, String[] swVersion, String language,
					String romId, String[] hwVersion, String productType, String productCode, 
					String serialNumber, String imei, String freeRam) {
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
	}
	
	/**
	 * Writes summary data into given buffer (i.e. text file)
	 * @param out
	 * @throws IOException
	 */
	public void writeTo(BufferedWriter out) throws IOException {
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
	}
	
	void writeLine(BufferedWriter out, String header, String value) throws IOException {
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
	public static Summary read(Element rootElement) {
		try {
			String crashTime = "";
			String crashDate = "";
			String upTime = "";
			
			// read data under seg_header node
			NodeList segHeader = rootElement.getElementsByTagName(TAG_SEG_HEADER);
			if (segHeader != null && segHeader.getLength() > 0) {
				// read crash time
				crashTime = XmlUtils.getTextValue((Element)segHeader.item(0), TAG_TIME);
				if (crashTime == null) {
					crashTime = "";
				} else if (!crashTime.contains(":") && crashTime.length() == 6){
					String time = crashTime.substring(0, 2) + ":" +
								  crashTime.substring(2, 4) + ":" +
								  crashTime.substring(4, 6);
					crashTime = time;
				}
				
				// read crashdate
				crashDate = XmlUtils.getTextValue((Element)segHeader.item(0), TAG_DATE);
				if (crashDate == null) {
					crashDate = "";
				} else if (crashDate.length() == 8) {
					String date = crashDate.substring(0,4) + "-" + 
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
			}
			
			List<String> hwVersions = new ArrayList<String>();
			String productType = "";
			String productCode = "";
			String serialNumber = "";
			
			// read data under seg_hw_info node
			NodeList segHwInfo = rootElement.getElementsByTagName(TAG_SEG_HW_INFO);
			if (segHwInfo != null && segHwInfo.getLength() > 0) {

				// read all versions
				NodeList versions = ((Element)segHwInfo.item(0)).getElementsByTagName(TAG_VERSION);
				if (versions != null && versions.getLength() > 0) {
					for (int i = 0; i < versions.getLength(); i++) {
						// read sw version
						String version = XmlUtils.getNodeValue(versions.item(i));
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
			}
			
			List<String> swVersions = new ArrayList<String>();
			String language = "";
			String romId = "";
			
			// read data under seg_sw_info node
			NodeList segSwInfo = rootElement.getElementsByTagName(TAG_SEG_SW_INFO);
			if (segSwInfo != null && segSwInfo.getLength() > 0) {

				// read all versions
				NodeList versions = ((Element)segSwInfo.item(0)).getElementsByTagName(TAG_VERSION);
				if (versions != null && versions.getLength() > 0) {
					for (int i = 0; i < versions.getLength(); i++) {
						// read sw version
						String version = XmlUtils.getNodeValue(versions.item(i));
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
			NodeList imeiNode = rootElement.getElementsByTagName(TAG_IMEI);
			if (imeiNode != null && imeiNode.getLength() > 0) {
				imei = imeiNode.item(0).getFirstChild().getNodeValue();
			}
			
			String freeRamAmount = "";
			// read free ram amount
			NodeList segMemoryInfo = rootElement.getElementsByTagName(TAG_SEG_MEMORY_INFO);
			if (segMemoryInfo != null && segMemoryInfo.getLength() > 0) {
				NodeList ram = ((Element)segMemoryInfo.item(0)).getElementsByTagName(TAG_RAM);
				if (ram != null && ram.getLength() > 0) {
					freeRamAmount = XmlUtils.getTextValue((Element)ram.item(0), TAG_FREE);
					if (freeRamAmount == null)
						freeRamAmount = "";
				}
			}
			
			return new Summary(crashTime, crashDate, upTime, swVersions.toArray(new String[swVersions.size()]), 
								language, romId, hwVersions.toArray(new String[hwVersions.size()]), productType, productCode, serialNumber, imei, freeRamAmount);
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
	
	static String convertUpTime(String uptime) {
		try {
			int secs =Integer.valueOf(uptime); 
			int hours =  secs / 3600,
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
