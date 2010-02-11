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

import org.w3c.dom.*;

/**
 * Utility class for XML handling 
 *
 */
public final class XmlUtils {
	private XmlUtils() {
		// not meant to be initialized
	}
	
	/**
	 * Returns the value of given tagName under given element.
	 * 
	 * E.g.
	 * <ele>
	 * 	<tagName>MyValue</tagName>
	 * </ele>
	 * 
	 * MyValue is returned in the above case.
	 * 
	 * @param ele xml element under which the tagName should be found
	 * @param tagName tag name of the tag which value is read
	 * @return the value of given tagName under given element.
	 */
	public static String getTextValue(Element ele, String tagName) {
		String textVal = null;

		try {
			NodeList nl = ele.getElementsByTagName(tagName);
			if(nl != null && nl.getLength() > 0) {
				Element el = (Element)nl.item(0);
				Node firstChild = el.getFirstChild();
				if (firstChild != null)
					textVal = firstChild.getNodeValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return textVal;
	}
	
	/**
	 * Returns the value of given node
	 * @param node node which value is to be returned
	 * @return the value of given node
	 */
	public static String getNodeValue(Node node) {
		String retval = "";
		try {
			Node firstChild = node.getFirstChild();
			if (firstChild != null)
				retval = firstChild.getNodeValue();
		} catch (Exception e) { 
			return "";
		}
		return retval;
	}	
	
	/**
	 * Checks whether given xml element contains a node of given name
	 * @param ele xml element
	 * @param tagName name of the node to be found
	 * @return true if node is found under xml element, false if not
	 */
	public static boolean containsNode(Element ele, String tagName) {
		try {
			NodeList nl = ele.getElementsByTagName(tagName);
			if(nl != null && nl.getLength() > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
