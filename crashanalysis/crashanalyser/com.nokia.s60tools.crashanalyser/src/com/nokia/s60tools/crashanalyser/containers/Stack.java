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
import com.nokia.s60tools.crashanalyser.containers.Process.StackItems;
import com.nokia.s60tools.crashanalyser.model.XmlUtils;
import org.w3c.dom.*;

import java.util.*;

/**
 * Contains stack information and all stack entries for 
 * this stack. 
 *
 */
public final class Stack {
	
	// XML tags
	public static final String TAG_ID = "id";
	public static final String TAG_RANGE = "range";
	public static final String TAG_HASH = "hash";
	public static final String TAG_DETAILED_HASH = "detailed_hash";
	public static final String TAG_LINK = "link";
	public static final String TAG_STACK_ENTRY = "stack_entry";
	public static final String ATTRIBUTE_SEG = "seg";
	public static final String SEGMENT_REGISTERS = "seg_registers";
	
	static final String FORMAT = "%-13s: %s";

	// Data
	private final int stackId;
	private final String stackType;
	private final String stackRange;
	private final String stackHash;
	private final String stackDetailedHash;
	private final String stackLinkRegister;
	private final String stackStackPointer;
	private final String stackProgramCounter;
	private final boolean stackContainsAccurateStackEntries;
	private final boolean stackRegisterContainsCpsr;
	private final List<StackEntry> stackData;
	
	/**
	 * Constructor
	 * @param id stack id
	 * @param type stack type
	 * @param range stack range
	 * @param linkRegister link register value
	 * @param stackPointer stack pointer value
	 * @param programCounter program counter value
	 * @param containsAccurateStackEntries defines if this stack was constructed with accurate (true)
	 * or heuristic (false) method
	 * @param data stack entries
	 * @param cpsrStack defines whether this stack contains cpsr register
	 */
	private Stack(int id, String type, String range, String hash, String detailedHash, String linkRegister, String stackPointer, 
					String programCounter, boolean containsAccurateStackEntries, List<StackEntry> data,
					boolean cpsrStack) {
		stackId = id;
		stackType = type;
		stackRange = range;
		stackHash = hash;
		stackDetailedHash = detailedHash;
		stackLinkRegister = linkRegister;
		stackStackPointer = stackPointer;
		stackProgramCounter = programCounter;
		stackContainsAccurateStackEntries = containsAccurateStackEntries;
		stackData = data;
		stackRegisterContainsCpsr = cpsrStack;
	}
	
	public int getId() {
		return stackId;
	}
	
	public String getStackType() {
		return stackType;
	}
	
	public List<StackEntry> getStackEntries() {
		return stackData;
	}
	
	public boolean containsAccurateStackEntries() {
		return stackContainsAccurateStackEntries;
	}
	
	public boolean stackRegisterContainsCpsr() {
		return stackRegisterContainsCpsr;
	}
	
	public String getProgramCounter() {
		return stackProgramCounter;
	}
	
	public String getStackPointer() {
		return stackStackPointer;
	}
	
	public String getLinkRegister() {
		return stackLinkRegister;
	}
	
	public String getHash() {
		return stackHash;
	}

	public String getDetailedHash() {
		return stackDetailedHash;
	}

	/**
	 * Writes stack data into given buffer (i.e. text file)
	 * @param out buffer to write to
	 * @param stackItems
	 * @param html write in html format
	 * @throws IOException
	 */
	public void writeTo(BufferedWriter out, StackItems stackItems, boolean html) throws IOException {
		writeLine(out,"");
		writeLine(out, "STACK:");
		writeLine(out, "--------");
		writeLine(out, "Stack Type", stackType);
		writeLine(out, "Stack Range", stackRange);
		writeLine(out, "Defect Hash", stackHash);
		writeLine(out, "Detailed Defect Hash", stackDetailedHash);
		
		if (stackData != null && !stackData.isEmpty()) {
			writeLine(out, "");
			writeLine(out, "**********************************************");
			for (int i = 0; i < stackData.size(); i++) {
				StackEntry entry = stackData.get(i);
				if (!entry.registerBased())
					entry.writeTo(out, stackItems, this, html);
			}
			writeLine(out, "**********************************************");
		}
	}

	void writeLine(BufferedWriter out, String header, String value) throws IOException {
		if (!"".equals(value)) {
			out.write(String.format(FORMAT, header, value));
			out.newLine();
		}
	}

	void writeLine(BufferedWriter out, String line) throws IOException {
		out.write(line);
		out.newLine();
	}

	/**
	 * Reads and creates stack from given stack xml element
	 * @param elementThreadStack
	 * @param registerSets
	 * @param allRegisters
	 * @param symbols
	 * @return created stack
	 */
	public static Stack read(Element elementThreadStack, 
								Map<Integer, RegisterSet> registerSets,
								Map<Integer, Register> allRegisters,
								Map<Integer, Symbol> symbols) {
		try {
			// read stack id
			String stackId = XmlUtils.getTextValue(elementThreadStack, TAG_ID);
			if (stackId == null)
				return null;
			
			// convert stack id to integer
			int id = Integer.parseInt(stackId);
			
			RegisterSet regSet = null;
			// 
			NodeList nl = elementThreadStack.getElementsByTagName(TAG_LINK);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Node linkNode = nl.item(i);
					String nodeValue = XmlUtils.getNodeValue(linkNode);
					NamedNodeMap attributes = linkNode.getAttributes();
					if (attributes != null && attributes.getLength() > 0) {
						Node seg = attributes.getNamedItem(ATTRIBUTE_SEG);
						// symbol id
						if (SEGMENT_REGISTERS.equals(XmlUtils.getNodeValue(seg))) {
							int regSetId = Integer.parseInt(nodeValue);
							if (!registerSets.containsKey(regSetId))
								return null;
							regSet = registerSets.get(regSetId);
							break;
						}
					}
				}
			}			
			
			if (regSet == null)
				return null;

			String type = regSet.getName();
			boolean cpsrStack = regSet.containsCPSR();
			String linkRegister = regSet.getLinkRegister();
			String stackPointer = regSet.getStackPointer();
			String programCounter = regSet.getProgramCounter();
			
			// read range if exists (e.g. 00402000-00407000)
			String range = XmlUtils.getTextValue(elementThreadStack, TAG_RANGE);
			if (range == null)
				range = "";
			
			// read defect hash if exists
			String hash = XmlUtils.getTextValue(elementThreadStack, TAG_HASH);
			if (hash == null)
				hash = "";

			// read detailed defect hash if exists
			String detailedHash = XmlUtils.getTextValue(elementThreadStack, TAG_DETAILED_HASH);
			if (detailedHash == null)
				detailedHash = "";

			// read stack entries
			List<StackEntry> entries = null;
			NodeList stackEntries = elementThreadStack.getElementsByTagName(TAG_STACK_ENTRY);
			if (stackEntries != null && stackEntries.getLength() > 0) {
				for (int i = 0; i < stackEntries.getLength(); i++) {
					StackEntry entry = StackEntry.read((Element)stackEntries.item(i), symbols, allRegisters);
					if (entry != null) {
						if (entries == null)
							entries = new ArrayList<StackEntry>();
						entries.add(entry);
					}
				}
			}
			
			// see if this stack contains any accurate stack entries
			boolean containsAccurateStackEntries = false;
			if (entries != null && !entries.isEmpty()) {
				for (int i = 0; i < entries.size(); i++) {
					StackEntry entry = entries.get(i);
					if (entry.accurate()) {
						containsAccurateStackEntries = true;
						break;
					}
				}
			}
			
			return new Stack(id, type, range, hash, detailedHash, linkRegister, stackPointer, programCounter,
								containsAccurateStackEntries, entries, cpsrStack);
		} catch (Exception e) {
			return null;
		}
	}
}
