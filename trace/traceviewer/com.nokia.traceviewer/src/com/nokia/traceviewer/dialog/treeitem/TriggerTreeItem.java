/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Trigger Tree item interface
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

/**
 * Trigger Tree item interface
 * 
 */
public interface TriggerTreeItem extends TreeItem {

	/**
	 * Rule type for trigger items
	 */
	public enum Rule {

		/**
		 * Text rule
		 */
		TEXT_RULE,

		/**
		 * Group rule
		 */
		GROUP;
	}

	/**
	 * Type of the trigger
	 */
	public enum Type {

		/**
		 * Start trigger
		 */
		STARTTRIGGER,

		/**
		 * Stop trigger
		 */
		STOPTRIGGER,

		/**
		 * Activation trigger
		 */
		ACTIVATIONTRIGGER;
	}

	/**
	 * Gets the type of the rule
	 * 
	 * @return the rule
	 */
	public Rule getRule();

	/**
	 * Gets the type of the trigger
	 * 
	 * @return Type of the trigger
	 */
	public Type getType();
}
