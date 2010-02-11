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

package com.nokia.s60tools.crashanalyser.model;

import java.util.List;
import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.containers.Message;
import com.nokia.s60tools.crashanalyser.containers.Summary;
import com.nokia.s60tools.crashanalyser.containers.Thread;
import com.nokia.s60tools.crashanalyser.data.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.TableItem;

/**
 * Formats different texts to shown in UI to be html format. 
 *
 */
public final class HtmlFormatter {
	private HtmlFormatter() {
	}
	
	final static String HEADER = "<h3>";
	final static String HEADER_END = "</h3>";
	final static String BOLD = "<b>";
	final static String BOLD_END = "</b>";
	final static String BREAK = "<br>";
	final static String ITALIC = "<i>";
	final static String ITALIC_END = "</i>";
	final static String RED_FONT = "<font color=#FF0000>";
	final static String ORANGE_FONT = "<font color=#FF8C00>";
	final static String FONT_END = "</font>";

	/**
	 * Formats a description for a panic category in html format
	 * @param categoryName category name
	 * @param categoryDescription category description
	 * @return category description in html format
	 */
	public static String formatCategoryDescription(String categoryName, String categoryDescription) {
		return BOLD + categoryName + BOLD_END + BREAK + BREAK + categoryDescription;
	}

	/**
	 * Formats a description for an error in html format
	 * @param errorName error name
	 * @param errorDescription error description 
	 * @return error description in html format
	 */
	public static String formatErrorDescription(String errorName, String errorDescription) {
		return BOLD + errorName + "  " + errorDescription + BOLD_END + BREAK + BREAK;
	}

	/**
	 * Formats a description for a panic in html format
	 * @param panicName panic name
	 * @param panicDescription panic description
	 * @return panic description in html format
	 */
	public static String formatPanicDescription(String panicName, String panicDescription) {
		return BOLD + panicName + BOLD_END + BREAK + panicDescription + BREAK + BREAK;
	}
	
	/**
	 * Formats a panic name to html format
	 * @param categoryName category name
	 * @param panicId panic id
	 * @return panic name in html format
	 */
	public static String formatPanicName(String categoryName, String panicId) {
		return BREAK + BREAK + BOLD + categoryName + " - " + panicId + BOLD_END + BREAK;
	}
	
	/**
	 * 
	 * @param component
	 * @return
	 */
	public static String formatErrorComponent(String component) {
		if (component.trim().length() < 1)
			return "";
		return BREAK + BREAK + BOLD + "Error Component:" + BOLD_END + BREAK + component;
	}
	
	/**
	 * Formats emulator panic into html format
	 * @param file emulator panic file
	 * @param errorLibrary error library
	 * @return emulator panic description in html format
	 */
	public static String formatEmulatorPanicDescription(PanicFile file, ErrorLibrary errorLibrary) {
		String panicCategory = file.getPanicCategory();
		String panicCode = file.getPanicCode();
		String description = BOLD + "Panic time : " + BOLD_END + file.getTime() + BREAK +
		  					 BOLD + "Paniced thread : " + BOLD_END + file.getThreadName() + BREAK +
		  					 BOLD + "Panic category : " + BOLD_END + panicCategory + BREAK +
		  					 BOLD + "Panic code : " + BOLD_END + panicCode + BREAK;

		try {
			String panicDescription = errorLibrary.getPanicDescription(panicCategory, panicCode);
			if (!"".equals(panicDescription))
				description += BOLD + "Panic description : " + BOLD_END + BREAK + panicDescription;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return description;
	}
	
	/**
	 * Formats a crash description in html format. 
	 * @param crashSummary crash summary data
	 * @param messages messages for the crash
	 * @param thread crashed thread
	 * @return crash description
	 */
	public static String formatCrashFileDescription(Summary crashSummary, 
													List<Message> messages,
													Thread thread) {
		String description = "";
		String panicDescription = "";
		
		description = HEADER + "Crash Summary" + HEADER_END;
		
		// format exit information
		if (thread != null) {
			if (!"".equals(thread.getFullName())) {
				description += BOLD + "Crashed Thread: " + BOLD_END + thread.getFullName() + BREAK;
			}
			if (!"".equals(thread.getExitType())) {
				description += BOLD + "Exit Type: " + BOLD_END + thread.getExitType() + BREAK;
			}
			
			if (!"".equals(thread.getExitCategory()) && !"".equals(thread.getExitReason())) {
				description += BOLD + "Exit Category: " + BOLD_END + thread.getExitCategory() + BREAK;
				description += BOLD + "Exit Reason: " + BOLD_END + thread.getExitReason() + BREAK;
				panicDescription = thread.getPanicDescription();
			}
		}

		// format summary information
		if (crashSummary != null) {
			if (!"".equals(crashSummary.getRomId()))
				description += BOLD + "ROM ID: " + BOLD_END + crashSummary.getRomId() + BREAK;
			String[] versions = crashSummary.getSwVersion();
			if (versions != null && versions.length > 0) {
				for (int i = 0; i < versions.length; i++) {
					description += BOLD + "SW Version: " + BOLD_END + versions[i] + BREAK;
				}
			}
			if (!"".equals(crashSummary.getProductType()))
				description += BOLD + "Product type: " + BOLD_END + crashSummary.getProductType() + BREAK;
			if (!"".equals(crashSummary.getProductCode()))
				description += BOLD + "Product code: " + BOLD_END + crashSummary.getProductCode() + BREAK;
			if (!"".equals(crashSummary.getLanguage()))
				description += BOLD + "Language: " + BOLD_END + crashSummary.getLanguage() + BREAK;
		}
		
		if (!"".equals(panicDescription)) {
			description += BREAK + BOLD + "Exit Description:" + BOLD_END + BREAK + panicDescription + BREAK;
		}

		return description;
	}
	
	/**
	 * Formats messages into errors and warnings into html format
	 * @param messages messages to be formatted
	 * @return messages in html format
	 */
	public static String formatErrosAndWarnings(List<Message> messages) {
		String description = "";
		String lineBreak = System.getProperty("line.separator");
		// format errors and warnings
		if (messages != null && !messages.isEmpty()) {
			for (int i = 0; i < messages.size(); i++) {
				Message message = messages.get(i);
				if (Message.MessageTypes.WARNING.equals(message.getMessageType())) {
					description += BOLD + ORANGE_FONT + "Warning: " + FONT_END + BREAK + ITALIC + message.getTitle() + ITALIC_END + BOLD_END + BREAK;
					description += message.getMessage().replace(lineBreak, BREAK) + BREAK + BREAK;
				} else if (Message.MessageTypes.ERROR.equals(message.getMessageType())){
					description += BOLD + RED_FONT + "Error: " + FONT_END + BREAK + ITALIC + message.getTitle() + ITALIC_END + BOLD_END + BREAK;
					description += message.getMessage().replace(lineBreak, BREAK) + BREAK + BREAK;
				}
			}
		}
		return description;
	}
	
	/**
	 * Formats css style parameters into html body text
	 * @param font font to be used
	 * @param bodyText html text
	 * @return html text with css style data
	 */
	public static String formatHtmlStyle(Font font, String bodyText) {
		String format = "";
		FontData[] fd = font.getFontData();
		if (fd != null && fd.length > 0) {
			format = "<style type=\"text/css\">";
			format += "body {font-family: " + fd[0].getName() + "} ";
			format += "body {font-size: 12} ";
			format += "p {font-family: " + fd[0].getName() + "} ";
			format += "p {font-size: 12} ";
			format += "b {font-family: " + fd[0].getName() + "} ";
			format += "b {font-size: 12} ";
			format += "</style>";
			format += "<body>";
			format += bodyText;
			format += "</body>";
		} else {
			format = bodyText;
		}
		return format;
	}
	
	/**
	 * Formats given table item's data into html format. Sets background colors correctly
	 * @param items table items
	 * @param columns how many columns a table item contains
	 * @return given table item's data in html format
	 */
	public static String formatStackForClipboard(TableItem[] items, int columns) {
		String retval = "";
		if (items != null && items.length > 0) {
			String separator = System.getProperty("line.separator"); 
			retval = "<table>" + separator;
			// go through all table items (i.e. table rows)
			for (int i = 0; i < items.length; i++) {
				TableItem item = items[i];
				String backGroundColor = getHexColor(item.getBackground());
				String textColor = getHexColor(item.getForeground());
				retval += "<tr bgcolor=#"+backGroundColor+"><font color=#"+textColor+">" + separator;
				// go through all columns in current row
				for (int j = 0; j < columns; j++) {
					retval += "<td>" + separator;
					retval += item.getText(j) + separator;
					retval += "</td>" + separator;
				}
				retval += "</font></tr>" + separator;
			}
			retval += "</table>" + separator;
		}
		return retval;
	}
	
	/**
	 * Returns color as hex value
	 * @param color
	 * @return color as hex value
	 */
	static String getHexColor(Color color) {
		RGB rgb = color.getRGB();
		String retval = Integer.toHexString( rgb.red);
		if (rgb.red < 1)
			retval += "0";
		retval += Integer.toHexString( rgb.green);
		if (rgb.green < 1)
			retval += "0";
		retval += Integer.toHexString( rgb.blue);
		if (rgb.blue < 1)
			retval += "0";
		return retval;
	}
	
	/**
	 * Formats a message about missing error description
	 * @param panic 
	 * @return
	 */
	public static String formatUnknownPanicMessage(String panic) {
		return "Could not find a description for this error. Please <a href=\"mailto:S60RnDtools@nokia.com?subject=Crash Analyser - Missing description for " +
				panic + 
				"\">report this missing error description</a>";		
	}
}
