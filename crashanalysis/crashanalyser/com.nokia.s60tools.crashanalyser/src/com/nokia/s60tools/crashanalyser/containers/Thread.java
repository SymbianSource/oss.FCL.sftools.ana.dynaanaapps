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

import org.w3c.dom.*;

import com.nokia.s60tools.crashanalyser.model.XmlUtils;
import com.nokia.s60tools.crashanalyser.containers.Process.StackItems;
import com.nokia.s60tools.crashanalyser.data.ErrorLibrary;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * Contains thread data.
 *
 */
public final class Thread {

	// XML tags
	public static final String TAG_ID = "id";
	public static final String TAG_FULLNAME = "fullname";
	public static final String TAG_EXIT_INFO = "exit_info";
	public static final String TAG_EXIT_TYPE = "exit_type";
	public static final String TAG_EXIT_CATEGORY = "exit_category";
	public static final String TAG_EXIT_REASON = "exit_reason";
	public static final String TAG_EXIT_DESCRIPTION = "exit_description";
	public static final String TAG_LINK = "link";
	public static final String ATTRIBUTE_SEG = "seg";
	public static final String SEGMENT_STACKS = "seg_stacks";
	public static final String SEGMENT_REGISTERS = "seg_registers";
	
	static final String FORMAT = "%-13s: %s";
	
	private final int threadId;
	private final String threadFullName;
	private final String threadExitType;
	private final String threadExitCategory;
	private final String threadExitReason;
	private final String threadExitDescription;
	private final String threadPanicDescription;
	// PC, SP and LR do not really belong to thread,
	// but these are taken from this thread's stack which
	// contains CPSR. So these are the most "interesting" 
	// PC, SP and LR values for this thread, even though 
	// this thread can own multiple stacks and hence multiple
	// PC, SP and LR.
	private final String threadProgramCounter;
	private final String threadStackPointer;
	private final String threadLinkRegister;
	private final List<Stack> threadStacks;
	private final List<RegisterSet> threadRegisters;

	private Thread(final int id, final String fullName, final String exitType, final String exitCategory, 
					final String panicDescription, final String programCounter, final String stackPointer, final String linkRegister, 
					final String exitReason, final String exitDescription, final List<Stack> stacks, final List<RegisterSet> registers) {
		threadId = id;
		threadFullName = fullName;
		threadExitType = exitType;
		threadExitCategory = exitCategory;
		threadExitReason = exitReason;
		threadExitDescription = exitDescription;
		threadProgramCounter = programCounter;
		threadStackPointer = stackPointer;
		threadLinkRegister = linkRegister;
		threadStacks = stacks;
		threadPanicDescription = panicDescription;
		threadRegisters = registers;
	}
	
	public int getId() {
		return threadId;
	}
	
	public String getFullName() {
		return threadFullName;
	}
	
	public String getExitType() {
		return threadExitType;
	}
	
	public String getExitCategory() {
		return threadExitCategory;
	}
	
	public String getExitReason() {
		return threadExitReason;
	}
	
	public String getExitDescription() {
		return threadExitDescription;
	}

	public String getProgramCounter() {
		return threadProgramCounter;
	}
	
	public String getStackPointer() {
		return threadStackPointer;
	}
	
	public String getLinkRegister() {
		return threadLinkRegister;
	}
	
	public String getPanicDescription() {
		return threadPanicDescription;
	}
	
	public List<Stack> getStacks() {
		return threadStacks;
	}
	
	public List<RegisterSet> getRegisters() {
		return threadRegisters;
	}
	
	/**
	 * Writes thread data into given buffer (i.e. text file)
	 * @param out
	 * @param stackItems
	 * @param html
	 * @throws IOException
	 */
	public void writeTo(final BufferedWriter out, final StackItems stackItems, final boolean html) throws IOException {
		writeLine(out,"");
		writeLine(out, "THREAD:");
		writeLine(out, "--------");
		writeLine(out, "Thread Name", threadFullName);
		writeLine(out, "Exit Type", threadExitType);
		
		if ("Exception".equals(threadExitType)) {
			writeLine(out, "Exit Reason", threadExitReason);
		} else {
			writeLine(out, "Exit Reason", threadExitCategory + " - " +threadExitReason);
		}
		writeLine(out, "Exit Description", threadExitDescription);
		
		writeLine(out, "");
		if (threadRegisters != null && !threadRegisters.isEmpty()) {
			for (int i = 0; i < threadRegisters.size(); i++) {
				final RegisterSet registerSet = threadRegisters.get(i);
				registerSet.writeTo(out);
				writeLine(out, "");
			}
		}
		
		if (threadStacks != null && !threadStacks.isEmpty()) {
			for (int i = 0; i < threadStacks.size(); i++) {
				final Stack stack = threadStacks.get(i);
				stack.writeTo(out, stackItems, html);
			}
		}
	}
	
	void writeLine(final BufferedWriter out, final String line) throws IOException {
		out.write(line);
		out.newLine();
	}
	
	void writeLine(final BufferedWriter out, final String header, final String value) throws IOException {
		if (!"".equals(value)) {
			out.write(String.format(FORMAT, header, value));
			out.newLine();
		}
	}
	
	/**
	 * Reads and creates thread from thread xml element
	 * @param elementThread
	 * @param registers
	 * @param symbols
	 * @param stacks
	 * @param errorLibrary
	 * @return created thread or null
	 */
	public static Thread read(final Element elementThread,
								final Map<Integer, RegisterSet> registers,
								final Map<Integer, Symbol> symbols,
								final Map<Integer, Stack> stacks,
								final ErrorLibrary errorLibrary) {
		try {
			// read thread id
			final String threadId = XmlUtils.getTextValue(elementThread, TAG_ID);
			if (threadId == null)
				return null;
			
			// convert thread id to integer
			int id;
			try {
				id = Integer.parseInt(threadId);
			} catch (Exception e) {
				return null;
			}
			
			// read the threads full name
			final String fullName = XmlUtils.getTextValue(elementThread, TAG_FULLNAME);
			if (fullName == null)
				return null;
			
			String exitType = "";
			String exitCategory = "";
			String exitReason = "";
			String exitDescription = "";
			// get child nodes such as exit_info, stacks, registers
			final NodeList childNodes = elementThread.getChildNodes();
			if (childNodes == null || childNodes.getLength() < 1)
				return null;
			
			// read Exit info
			final NodeList exitInfo = elementThread.getElementsByTagName(TAG_EXIT_INFO);
			if (exitInfo != null && exitInfo.getLength() > 0) {
				final NodeList exitInfos = exitInfo.item(0).getChildNodes();
				if (exitInfos != null && exitInfos.getLength() > 0) {
					for (int i = 0; i < exitInfos.getLength(); i++) {
						final Node el = exitInfos.item(i);
						Node firstChild = null;
						if (TAG_EXIT_TYPE.equals(el.getNodeName())) {
							// read exit type (Exception, Panic, Kill, Terminate)
							firstChild = el.getFirstChild();
							if (firstChild != null) {
								exitType = firstChild.getNodeValue();
								if (exitType == null) {
									exitType = "";
								}
							} else {
								exitType = "";
							}
						} else if (TAG_EXIT_CATEGORY.equals(el.getNodeName())) {
							// read exit category (e.g W32)
							firstChild = el.getFirstChild();
							if (firstChild != null) {
								exitCategory = firstChild.getNodeValue();
								if (exitCategory == null)
									exitCategory = "";
							} else {
								exitCategory = "";
							}
						} else if (TAG_EXIT_REASON.equals(el.getNodeName())) {
							// read exit reason (e.g 3)
							firstChild = el.getFirstChild();
							if (firstChild != null) {							
								exitReason = firstChild.getNodeValue();
								if (exitReason == null)
									exitReason = "";
							} else {
								exitReason = "";
							}								
						} else if (TAG_EXIT_DESCRIPTION.equals(el.getNodeName())) {
							// read exit description
							firstChild = el.getFirstChild();
							if (firstChild != null) {							
								exitDescription = firstChild.getNodeValue();
								if (exitDescription == null)
									exitDescription = "";
							} else {
								exitDescription = "";
							}								
						}
					}
				}
			}

			if ("Exception".equals(exitType)) {
				exitReason = exitCategory;
				exitCategory = "Exceptions";
			}
			
			
			String panicDescription = "";
			if (!"".equals(exitCategory) && !"".equals(exitReason)) {
				panicDescription = errorLibrary.getPanicDescription(exitCategory, exitReason);
			}
			
			final List<Stack> threadStacks = new ArrayList<Stack>();
			final List<RegisterSet> threadRegisters = new ArrayList<RegisterSet>();
			String programCounter = "";
			String stackPointer = "";
			String linkRegister = "";
			
			// see if register has a symbol and/or message
			final NodeList nl = elementThread.getElementsByTagName(TAG_LINK);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					final Node linkNode = nl.item(i);
					final String nodeValue = XmlUtils.getNodeValue(linkNode);
					final NamedNodeMap attributes = linkNode.getAttributes();
					if (attributes != null && attributes.getLength() > 0) {
						final Node seg = attributes.getNamedItem(ATTRIBUTE_SEG);
						// stack id
						if (SEGMENT_STACKS.equals(XmlUtils.getNodeValue(seg))) {
							final int sId = Integer.parseInt(nodeValue);
							if (stacks.containsKey(sId)) {
								final Stack s = stacks.get(sId);
								threadStacks.add(s);
								// the most interesting PC, SP and LR comes from
								// that stack which contains CPSR.
								if (s.stackRegisterContainsCpsr()) {
									programCounter = s.getProgramCounter();
									stackPointer = s.getStackPointer();
									linkRegister = s.getLinkRegister();
								}
							}
						// register id
						} else if (SEGMENT_REGISTERS.equals(XmlUtils.getNodeValue(seg))) {
							final int rId = Integer.parseInt(nodeValue);
							// if passed registers list contains a register for this id
							if (registers.containsKey(rId)) {
								final RegisterSet registerSet = registers.get(rId);
								threadRegisters.add(registerSet);								
							}							
						}
					}
				}
			}

			return new Thread(id, fullName, exitType, exitCategory, 
								panicDescription, programCounter, stackPointer, linkRegister,
								exitReason, exitDescription, threadStacks, threadRegisters);

		} catch (Exception e) {
			return null;
		}
	}
	
	public Map<Integer, Stack> removeOwnStacks(final Map<Integer, Stack> stacks) {
		
		if (threadStacks != null && !threadStacks.isEmpty()) {
			for (int i = 0; i < threadStacks.size(); i++) {
				if (stacks.containsKey(threadStacks.get(i).getId())) {
					stacks.remove(threadStacks.get(i).getId());
				}
			}
		}
		
		return stacks;
	}
	
	public Map<Integer, RegisterSet> removeOwnRegisterSets(final Map<Integer, RegisterSet> registerSets) {
		
		if (threadRegisters != null && !threadRegisters.isEmpty()) {
			for (int i = 0; i < threadRegisters.size(); i++) {
				if (registerSets.containsKey(threadRegisters.get(i).getId())) {
					registerSets.remove(threadRegisters.get(i).getId());
				}
			}
		}
		
		return registerSets;
	}
	
}
