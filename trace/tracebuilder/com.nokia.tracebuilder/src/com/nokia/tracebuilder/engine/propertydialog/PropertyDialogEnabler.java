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
* A singleton access point to functionality of Trace Builder
*
*/
package com.nokia.tracebuilder.engine.propertydialog;

import com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler;
import com.nokia.tracebuilder.model.TraceObject;

/**
 * Property dialog enabler interface implementation. This is used with the "Add"
 * methods of the property dialog manager, but is not added to the objects
 * created by them.
 * 
 */
public final class PropertyDialogEnabler implements
		TraceObjectPropertyDialogEnabler {

	/**
	 * ID enabled bit
	 */
	public static final int ENABLE_ID = 0x1; // CodForChk_Dis_Magic

	/**
	 * Name enabled bit
	 */
	public static final int ENABLE_NAME = 0x2; // CodForChk_Dis_Magic

	/**
	 * Value enabled bit
	 */
	public static final int ENABLE_VALUE = 0x4; // CodForChk_Dis_Magic

	/**
	 * Type enabled bit
	 */
	public static final int ENABLE_TYPE = 0x8; // CodForChk_Dis_Magic

	/**
	 * Template enabled bit
	 */
	public static final int ENABLE_TEMPLATE = 0x10; // CodForChk_Dis_Magic

	/**
	 * Flags enabled bit
	 */
	public static final int ENABLE_FLAGS = 0x20; // CodForChk_Dis_Magic
	
	/**
	 * Target enabled bit
	 */
	public static final int ENABLE_TARGET = 0x40; // CodForChk_Dis_Magic	

	/**
	 * Enable flags
	 */
	private int flags;

	/**
	 * Constructor
	 * 
	 * @param flags
	 *            enabler flags
	 */
	public PropertyDialogEnabler(int flags) {
		this.flags = flags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler#isTargetEnabled()
	 */
	public boolean isTargetEnabled() {
		return (flags & ENABLE_TARGET) != 0;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler#isIdEnabled()
	 */
	public boolean isIdEnabled() {
		return (flags & ENABLE_ID) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler#isNameEnabled()
	 */
	public boolean isNameEnabled() {
		return (flags & ENABLE_NAME) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler#isTypeEnabled()
	 */
	public boolean isTypeEnabled() {
		return (flags & ENABLE_TYPE) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceObjectPropertyDialogEnabler#isValueEnabled()
	 */
	public boolean isValueEnabled() {
		return (flags & ENABLE_VALUE) != 0;
	}

	/**
	 * Internal flag which tells the property dialog manager to set the
	 * templates used in "Add" dialog to null if this returns false
	 * 
	 * @return true if templates are enabled
	 */
	boolean isTemplateEnabled() {
		return (flags & ENABLE_TEMPLATE) != 0;
	}

	/**
	 * Internal flag which tells the property dialog manager to set the flags
	 * used in "Add" dialog to null if this returns false
	 * 
	 * @return true if flags are enabled
	 */
	boolean isFlagsEnabled() {
		return (flags & ENABLE_FLAGS) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#getOwner()
	 */
	public TraceObject getOwner() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#
	 *      setOwner(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void setOwner(TraceObject owner) {
	}

}