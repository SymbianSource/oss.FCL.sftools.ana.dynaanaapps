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
import org.w3c.dom.NodeList;
import com.nokia.s60tools.crashanalyser.model.XmlUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * A register set contains all registers.
 *
 */
public final class RegisterSet {

	// XML tags
	public static final String TAG_ID = "id";
	public static final String TAG_TYPE = "type";
	public static final String TAG_NAME = "name";
	public static final String TAG_REGISTER = "register";

	// data
	private final int setId;
	private final String setType;
	private final String setName;
	private final boolean setContainsCPSR;
	private final String linkRegisterValue;
	private final String stackPointerValue;
	private final String programCounterValue;
	private final List<Register> setRegisters;
	
	/**
	 * Constructor
	 * @param id register set id
	 * @param type register set type
	 * @param name register set name
	 * @param registers list of registers for this register set
	 * @param containsCPSR does this register set contain CPSR register
	 * @param linkRegister link register value
	 * @param stackPointer stack pointer value
	 * @param programCounter program counter value
	 */
	private RegisterSet(int id, String type, String name, List<Register> registers, boolean containsCPSR,
						String linkRegister, String stackPointer, String programCounter) {
		setId = id;
		setType = type;
		setName = name;
		setRegisters = registers;
		setContainsCPSR = containsCPSR;
		linkRegisterValue = linkRegister;
		stackPointerValue = stackPointer;
		programCounterValue = programCounter;
	}
	
	public int getId() {
		return setId;
	}
	
	public String getType() {
		return setType;
	}
	
	public String getName() {
		return setName;
	}
	
	public List<Register> getRegisters() {
		return setRegisters;
	}
	
	public boolean containsCPSR() {
		return setContainsCPSR;
	}
	
	public String getLinkRegister() {
		return linkRegisterValue;
	}
	
	public String getStackPointer() {
		return stackPointerValue;
	}
	
	public String getProgramCounter() {
		return programCounterValue;
	}
	
	/**
	 * Writes registers into give buffer (i.e. text file)
	 * @param out buffer to write to
	 * @throws IOException
	 */
	public void writeTo(BufferedWriter out) throws IOException {
		if (!"".equals(setName)) {
			writeLine(out, "Register Name : " + setName);
			if (setRegisters != null && !setRegisters.isEmpty()) {
				for (int i = 0; i < setRegisters.size(); i++) {
					Register register = setRegisters.get(i);
					register.writeTo(out);
				}
			}
		}
	}
	
	void writeLine(BufferedWriter out, String line) throws IOException {
		out.write(line);
		out.newLine();
	}

	/**
	 * Reads and creates a register set from register set xml element
	 * @param elementRegisterSet
	 * @param symbols
	 * @param messages
	 * @return created register set
	 */
	public static RegisterSet read(Element elementRegisterSet, 
									Map<Integer, Symbol> symbols,
									Map<Integer, Message> messages) {
		try {
			// read register set id
			String setId = XmlUtils.getTextValue(elementRegisterSet, TAG_ID);
			if (setId == null)
				return null;
			
			// convert id to integer
			int id;
			try {
				id = Integer.parseInt(setId);
			} catch (Exception e) {
				return null;
			}
			
			// read register set type
			String type =  XmlUtils.getTextValue(elementRegisterSet, TAG_TYPE);
			if (type == null)
				type = "";
			
			// read register set name
			String name = XmlUtils.getTextValue(elementRegisterSet, TAG_NAME);
			if (name == null)
				return null;
			
			List<Register> registers = new ArrayList<Register>();
			
			boolean containsCPSR = false;
			
			String stackPointer = "";
			String linkRegister = "";
			String programCounter = "";
			
			// get register nodes
			NodeList registerNodes = elementRegisterSet.getElementsByTagName(TAG_REGISTER);
			if (registerNodes != null && registerNodes.getLength() > 0) {
				// go through all registers
				for (int i = 0; i < registerNodes.getLength(); i++) {
					Element registerElement = (Element)registerNodes.item(i);
					Register register = Register.read(registerElement, symbols, messages);
					if (register != null) {
						registers.add(register);
						containsCPSR = "CPSR".equals(register.getName());
						// try to read stack pointer value
						if (register.getName().contains("R13")) {
							stackPointer = register.getValue() + " " + register.getSymbol();
						// try to read link register value
						} else if (register.getName().contains("R14")) {
							linkRegister = register.getValue() + " " + register.getSymbol();
						// try to read program counter value
						} else if (register.getName().contains("R15")) {
							programCounter = register.getValue() + " " + register.getSymbol();
						}
					}
				}
			}
			
			return new RegisterSet(id, type, name, registers, containsCPSR, 
									linkRegister, stackPointer, programCounter);
		} catch (Exception e) {
			return null;
		}
	}
}
