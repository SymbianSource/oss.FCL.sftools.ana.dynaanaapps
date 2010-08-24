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

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.nokia.s60tools.crashanalyser.containers.Process.StackItems;
import com.nokia.s60tools.crashanalyser.model.XmlUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * Represents one entry in a stack. 
 *
 */
public final class StackEntry {
	
	// XML tags
	public static final String TAG_ADDRESS = "address";
	public static final String TAG_VALUE = "value";
	public static final String TAG_OFFSET = "offset";
	public static final String TAG_TEXT = "text";
	public static final String TAG_ACCURATE = "accurate";
	public static final String TAG_LINK = "link";
	public static final String TAG_OUTSIDE_STACK_BOUNDS = "outside_current_stack_pointer_range";
	public static final String TAG_CURRENT_STACK_POINTER = "current_stack_pointer";
	public static final String ATTRIBUTE_SEG = "seg";
	public static final String SEGMENT_SYMBOLS = "seg_symbols";
	public static final String SEGMENT_REGISTERS = "seg_registers";
	
	static final String FORMAT = "%-10s  %-10s  %-6s  %-6s  %s  %s";

	// Data
	private final String stackEntryAddress;
	private final String stackEntryValue;
	private final String stackEntrySymbol;
	private final String stackEntryObject;
	private final String stackEntryOffset;
	private final String stackEntryText;
	private final String stackEntrySource;
	private final String stackEntryCodeSegmentName; 
	private final boolean outsideStackBounds;
	private final boolean currentStackPointer;
	private final boolean accurate;
	private final boolean xipSymbol;
	private final boolean registerBased;
	
	private StackEntry(final String address, final String value, final String symbol, final String object, final String offset, final String text, final String source,
						final String codeSegmentName, final boolean outsideSB, final boolean currentSP, final boolean isAccurate, final boolean xip, final boolean register) {
		stackEntryAddress = address;
		stackEntryValue = value;
		stackEntrySymbol = symbol;
		stackEntryObject = object;
		stackEntryOffset = offset;
		stackEntryText = text;
		stackEntrySource = source;
		stackEntryCodeSegmentName = codeSegmentName;
		outsideStackBounds = outsideSB;
		currentStackPointer = currentSP;
		accurate = isAccurate;
		xipSymbol = xip;
		registerBased = register;
	}
	
	public String getAddress() {
		return stackEntryAddress;
	}
	
	public String getValue() {
		return stackEntryValue;
	}
	
	public String getSymbol() {
		return stackEntrySymbol;
	}
	
	public String getObject() {
		return stackEntryObject;
	}
	
	public String getOffset() {
		return stackEntryOffset;
	}
	
	public String getText() {
		return stackEntryText;
	}
	
	public String getSource() {
		return stackEntrySource;
	}
	
	public boolean outsideStackBounds() {
		return outsideStackBounds;
	}
	
	public boolean currentStackPointer() {
		return currentStackPointer;
	}
	
	public boolean accurate() {
		return accurate;
	}
	
	public boolean xip() {
		return xipSymbol;
	}
	
	public boolean registerBased() {
		return registerBased;
	}
	
	public String getCodeSegmentName() {
		return stackEntryCodeSegmentName;
	}
	
	@Override
	public String toString() {
		return stackEntrySource;
	}	
	
	/**
	 * Writes stack item into given buffer (i.e. text file)
	 * @param out
	 * @param stackItems
	 * @param stack
	 * @param html
	 * @throws IOException
	 */
	public void writeTo(final BufferedWriter out, final StackItems stackItems, final Stack stack, final boolean html) throws IOException {
		if (currentStackPointer) {
			writeLine(out, "");
			if (html)
				writeLine(out, "</pre><a name=\"STACKPOINTER\"><h3><<<<<<<<<< CURRENT STACK POINTER >>>>>>>>>></h3></a><pre>");
			else
				writeLine(out, "<<<<<<<<<< CURRENT STACK POINTER >>>>>>>>>>");
			writeLine(out, "");
			writeLine(out, String.format(FORMAT, stackEntryAddress, 
					 stackEntryValue,
					 stackEntryText,
					 stackEntryOffset,
					 stackEntryObject,
					 stackEntrySymbol));

			if (!"".equals(stack.getLinkRegister()) || 
				!"".equals(stack.getStackPointer()) || 
				!"".equals(stack.getProgramCounter())) {
				writeLine(out, "");
				if (!"".equals(stack.getLinkRegister())) {
					writeLine(out, "LR :" + stack.getLinkRegister());
				}
				if (!"".equals(stack.getProgramCounter())) {
					writeLine(out, "PC :" + stack.getProgramCounter());
				}
				if (!"".equals(stack.getStackPointer())) {
					writeLine(out, "SP :" + stack.getStackPointer());
				}
			}
			writeLine(out, "");
			if (html)
				writeLine(out, "</pre><h3><<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>></h3><pre>"); // just to make it look nice
			else
				writeLine(out, "<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>");
			writeLine(out, "");
		} else if (StackItems.ACCURATE.equals(stackItems) && !accurate) {
			return;
		} else if (StackItems.SYMBOLS.equals(stackItems) && "".equals(stackEntrySymbol)) {
			return;
		} else {
			writeLine(out, String.format(FORMAT, stackEntryAddress, 
												 stackEntryValue,
												 stackEntryText,
												 stackEntryOffset,
												 stackEntryObject,
												 stackEntrySymbol));
		}
	}	
	
	void writeLine(final BufferedWriter out, final String line) throws IOException {
		out.write(line);
		out.newLine();
	}

	/**
	 * Reads and creates stack entry from stack entry xml element
	 * @param elementStackEntry
	 * @param symbols
	 * @param registers
	 * @return created stack entry or null
	 */
	public static StackEntry read(final Element elementStackEntry, 
									final Map<Integer, Symbol> symbols,
									final Map<Integer, Register> registers) {
		try {
			// read address if exists
			String address = XmlUtils.getTextValue(elementStackEntry, TAG_ADDRESS);
			if (address == null)
				address = "";
			
			// read value if exists
			String value = XmlUtils.getTextValue(elementStackEntry, TAG_VALUE);
			if (value == null)
				value = "";
			
			boolean xip = false;
			boolean registerBased = false;
			String symbol = "";
			String object = "";
			String source = "";
			String codeSegment = "";
			
			// try to read symbol & register information for this stack entry
			final NodeList nl = elementStackEntry.getElementsByTagName(TAG_LINK);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					final Node linkNode = nl.item(i);
					final String nodeValue = XmlUtils.getNodeValue(linkNode);
					final NamedNodeMap attributes = linkNode.getAttributes();
					if (attributes != null && attributes.getLength() > 0) {
						final Node seg = attributes.getNamedItem(ATTRIBUTE_SEG);
						// symbol id
						if (SEGMENT_SYMBOLS.equals(XmlUtils.getNodeValue(seg))) {
							try {
								final int sId = Integer.parseInt(nodeValue);
								if (symbols.containsKey(sId)) {
									final Symbol sym = symbols.get(sId);
									symbol = sym.getName();
									object = sym.getObject();
									source = sym.getSource();
									xip = sym.xip();
									codeSegment = sym.getCodeSegmentName();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						// register id
						} else if (SEGMENT_REGISTERS.equals(XmlUtils.getNodeValue(seg))) {
							registerBased = true;
							final int rId = Integer.parseInt(nodeValue);
							if (registers.containsKey(rId)) {
								final Register reg = registers.get(rId);
								if ("R14".equals(reg.getName())) {
									address = "LR";
								} else if ("R15".equals(reg.getName())) {
									address = "PC";
								}
							}
						}
					}
				}
			}
			
			final boolean currentStackPointer = XmlUtils.containsNode(elementStackEntry, TAG_CURRENT_STACK_POINTER);
			final boolean accurate = XmlUtils.containsNode(elementStackEntry, TAG_ACCURATE);
			final boolean outsideStackBounds = XmlUtils.containsNode(elementStackEntry, TAG_OUTSIDE_STACK_BOUNDS);
	
			// read offset if exists
			String offset = XmlUtils.getTextValue(elementStackEntry, TAG_OFFSET);
			if (offset == null)
				offset = "";
			
			// read text if exists
			String text = XmlUtils.getTextValue(elementStackEntry, TAG_TEXT);
			if (text == null)
				text = "";
			
			return new StackEntry(address, value, symbol, object, offset, text, source, codeSegment,
									outsideStackBounds, currentStackPointer, accurate, xip, registerBased);
		} catch (Exception e) {
			return null;
		}
	}
}
