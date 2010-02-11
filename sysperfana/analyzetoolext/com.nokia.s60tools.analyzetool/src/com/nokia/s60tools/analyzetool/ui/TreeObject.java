/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class TreeObject
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import org.eclipse.core.runtime.IAdaptable;

import com.nokia.s60tools.analyzetool.engine.CallstackItem;

/**
 * Class to implement TreeObject.
 *
 * @author kihe
 *
 */
public class TreeObject implements IAdaptable {

	/** Name. */
	private String name;

	/** Parent object. */
	private TreeParent parent;

	/** Analysis run id. */
	private int runID = 0;

	/** Memory leak id. */
	private int memLeakID = 0;

	/** Callstack item. */
	private CallstackItem callstackItem = null;

	/** Flag to inform is current treeobject subtest or not. */
	private boolean isSubtest = false;

	/** Module name. */
	private String moduleName = null;

	/** Subtest id. */
	private int subtestID = 0;

	/** Memory address. */
	private String memAddress = null;

	/** Is this item build. */
	private boolean isBuild = false;

	/** Does current module belongs to selected project. */
	private boolean belongsToProject = false;

	/**
	 * Constructor.
	 */
	public TreeObject() {
		// ConstructorDeclaration[@Private='false'][count(BlockStatement) = 0
		// and ($ignoreExplicitConstructorInvocation = 'true' or
		// not(ExplicitConstructorInvocation)) and @containsComment = 'false']
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(final Class key) {
		return null;
	}

	/**
	 * Gets CallstackItem for current object.
	 *
	 * @return CallstackItem
	 */
	public final CallstackItem getCallstackItem() {
		return this.callstackItem;
	}

	/**
	 * Gets memory address.
	 *
	 * @return Memory address
	 */
	public final String getMemAddress() {
		return memAddress;
	}

	/**
	 * Gets memory leak id.
	 *
	 * @return Memory leak id
	 */
	public final int getMemLeakID() {
		return memLeakID;
	}

	/**
	 * Gets module name.
	 *
	 * @return Module name
	 */
	public final String getModuleName() {
		return moduleName;
	}

	/**
	 * Gets current object name.
	 *
	 * @return Current object name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Gets current object parent.
	 *
	 * @return TreeParent
	 */
	public final TreeParent getParent() {
		return parent;
	}

	/**
	 * Gets Analysis run id.
	 *
	 * @return Analysis run id
	 */
	public final int getRunID() {
		return runID;
	}

	/**
	 * Gets subtest id.
	 *
	 * @return Subtest id
	 */
	public final int getSubtestID() {
		return subtestID;
	}

	/**
	 * Gets value that belongs module to selected projects.
	 *
	 * @return True if module belongs to project otherwise False
	 */
	public final boolean isBelongs() {
		return belongsToProject;
	}

	/**
	 * Is build.
	 *
	 * @return True if this item is build otherwise False
	 */
	public final boolean isBuild() {
		return isBuild;
	}

	/**
	 * Gets flag is current treeobject is subtest.
	 *
	 * @return True is treeobject subtest otherwise false
	 */
	public final boolean isSubTest() {
		return isSubtest;
	}

	/**
	 * Does module belongs to selected project.
	 *
	 * @param belongs
	 *            Info
	 */
	public final void setBelongs(final boolean belongs) {
		belongsToProject = belongs;
	}

	/**
	 * Set build info .
	 *
	 * @param build
	 *            Build info
	 */
	public final void setBuild(final boolean build) {
		isBuild = build;
	}

	/**
	 * Sets CallstackItem.
	 *
	 * @param newCallItem
	 *            CallstackItem
	 */
	public final void setCallstackItem(final CallstackItem newCallItem) {
		this.callstackItem = newCallItem;
	}

	/**
	 * Sets memory address.
	 *
	 * @param newMemAddress
	 *            Memory address
	 */
	public final void setMemAddress(final String newMemAddress) {
		memAddress = newMemAddress;
	}

	/**
	 * Sets memory leak id.
	 *
	 * @param newID
	 *            Memory leak id
	 */
	public final void setMemLeakID(final int newID) {
		this.memLeakID = newID;
	}

	/**
	 * Sets new module name.
	 *
	 * @param newModuleName
	 *            Module name
	 */
	public final void setModuleName(final String newModuleName) {
		moduleName = newModuleName;
	}

	/**
	 * Set current object name.
	 *
	 * @param newName
	 *            Current object name
	 */
	public final void setName(final String newName) {
		this.name = newName;
	}

	/**
	 * Set parent current object.
	 *
	 * @param newParent
	 *            Sets current object parent
	 */
	public final void setParent(final TreeParent newParent) {
		this.parent = newParent;
	}

	/**
	 * Sets Analysis run id.
	 *
	 * @param newID
	 *            Analysis run id
	 */
	public final void setRunID(final int newID) {
		this.runID = newID;
	}

	/**
	 * Set subtest flag.
	 *
	 * @param value
	 *            True or false
	 */
	public final void setSubtest(final boolean value) {
		isSubtest = value;
	}

	/**
	 * Sets subtest id.
	 *
	 * @param newID
	 *            Subtest id
	 */
	public final void setSubtestID(final int newID) {
		subtestID = newID;
	}

	/**
	 * Gets current object name.
	 *
	 * @return Current object name
	 */
	@Override
	public final String toString() {
		return getName();
	}
}
