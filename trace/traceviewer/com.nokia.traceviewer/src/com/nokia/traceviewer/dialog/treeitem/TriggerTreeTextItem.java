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
 * Trigger tree Text Item class
 *
 */
package com.nokia.traceviewer.dialog.treeitem;

/**
 * Trigger tree Text Item class
 * 
 */
public class TriggerTreeTextItem extends TriggerTreeBaseItem {

	/**
	 * Text of the item
	 */
	private final String text;

	/**
	 * Text to be compared
	 */
	private final String textToCompare;

	/**
	 * Indicates if the case should match in the item
	 */
	private final boolean matchCase;

	/**
	 * Configuration file path
	 */
	private String configurationFilePath = ""; //$NON-NLS-1$

	/**
	 * Configuration name
	 */
	private String configurationName = ""; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param listener
	 *            TreeItemListener
	 * @param parent
	 *            parent object
	 * @param name
	 *            name of the item
	 * @param rule
	 *            rule of the item
	 * @param text
	 *            text of the item
	 * @param matchCase
	 *            match case indication of the item
	 * @param type
	 *            type of the trigger
	 * @param configurationFilePath
	 *            configuration file path
	 * @param configurationName
	 *            configuration name
	 */
	public TriggerTreeTextItem(TreeItemListener listener, Object parent,
			String name, Rule rule, String text, boolean matchCase, Type type,
			String configurationFilePath, String configurationName) {
		super(listener, parent, name, rule, type);
		this.text = text;
		this.matchCase = matchCase;
		this.configurationFilePath = configurationFilePath;
		this.configurationName = configurationName;

		// Save text to compare
		if (matchCase || text == null) {
			textToCompare = text;
		} else {
			textToCompare = text.toLowerCase();
		}
	}

	/**
	 * Gets match case status
	 * 
	 * @return the matchCase
	 */
	public boolean isMatchCase() {
		return matchCase;
	}

	/**
	 * Gets text
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Gets text to compare
	 * 
	 * @return the text to compare
	 */
	public String getTextToCompare() {
		return textToCompare;
	}

	/**
	 * Gets configuration file path
	 * 
	 * @return configuration file path
	 */
	public String getConfigurationFilePath() {
		return configurationFilePath;
	}

	/**
	 * Gets configuration name
	 * 
	 * @return configuration name
	 */
	public String getConfigurationName() {
		return configurationName;
	}
}
