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
 * Constants for XML Configuration file
 *

 */
package com.nokia.traceviewer.engine.preferences;

/**
 * Constants for XML Configuration file
 * 
 */
interface XMLConfigurationFileConstants {

	/**
	 * XML Header
	 */
	final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"; //$NON-NLS-1$

	/**
	 * File start
	 */
	final String FILE_START = "<ConfigurationFile Version=\"1\">\n"; //$NON-NLS-1$

	/**
	 * File end
	 */
	final String FILE_END = "</ConfigurationFile>\n"; //$NON-NLS-1$

	/**
	 * Main configurations start
	 */
	final String MAINCONFIGURATIONS_START = "  <Configurations>\n"; //$NON-NLS-1$

	/**
	 * Main configurations end
	 */
	final String MAINCONFIGURATIONS_END = "  </Configurations>\n"; //$NON-NLS-1$

	/**
	 * General configuration start
	 */
	final String GENERAL_CONFIGURATION_START = "    <General>\n"; //$NON-NLS-1$

	/**
	 * General configuration end
	 */
	final String GENERAL_CONFIGURATION_END = "    </General>\n"; //$NON-NLS-1$

	/**
	 * Color configuration start
	 */
	final String COLOR_CONFIGURATION_START = "    <ColorCoding>\n"; //$NON-NLS-1$

	/**
	 * Color configuration end
	 */
	final String COLOR_CONFIGURATION_END = "    </ColorCoding>\n"; //$NON-NLS-1$

	/**
	 * Filter configuration start
	 */
	final String FILTER_CONFIGURATION_START = "    <Filter>\n"; //$NON-NLS-1$

	/**
	 * Filter configuration end
	 */
	final String FILTER_CONFIGURATION_END = "    </Filter>\n"; //$NON-NLS-1$

	/**
	 * Linecount configuration start
	 */
	final String LINECOUNT_CONFIGURATION_START = "    <LineCount>\n"; //$NON-NLS-1$

	/**
	 * Linecount configuration end
	 */
	final String LINECOUNT_CONFIGURATION_END = "    </LineCount>\n"; //$NON-NLS-1$

	/**
	 * Variabletracing configuration start
	 */
	final String VARIABLETRACING_CONFIGURATION_START = "    <VariableTracing>\n"; //$NON-NLS-1$

	/**
	 * Variabletracing configuration end
	 */
	final String VARIABLETRACING_CONFIGURATION_END = "    </VariableTracing>\n"; //$NON-NLS-1$

	/**
	 * Trigger configuration start
	 */
	final String TRIGGER_CONFIGURATION_START = "    <Trigger>\n"; //$NON-NLS-1$

	/**
	 * Trigger configuration end
	 */
	final String TRIGGER_CONFIGURATION_END = "    </Trigger>\n"; //$NON-NLS-1$

	/**
	 * Filter tag
	 */
	final String FILTER_TAG = "Filter"; //$NON-NLS-1$

	/**
	 * General tag
	 */
	final String GENERAL_TAG = "General"; //$NON-NLS-1$

	/**
	 * Color tag
	 */
	final String COLOR_TAG = "ColorCoding"; //$NON-NLS-1$

	/**
	 * Linecount tag
	 */
	final String LINECOUNT_TAG = "LineCount"; //$NON-NLS-1$

	/**
	 * Variabletracing tag
	 */
	final String VARIABLETRACING_TAG = "VariableTracing"; //$NON-NLS-1$

	/**
	 * Trigger tag
	 */
	final String TRIGGER_TAG = "Trigger"; //$NON-NLS-1$

	/**
	 * Group tag
	 */
	final String GROUP_TAG = "Group"; //$NON-NLS-1$

	/**
	 * Configurations tag
	 */
	final String CONFIGURATIONS_TAG = "Configurations"; //$NON-NLS-1$

	/**
	 * Configuration tag
	 */
	final String CONFIGURATION_TAG = "Configuration"; //$NON-NLS-1$

	/**
	 * Rule tag
	 */
	final String RULE_TAG = "Rule"; //$NON-NLS-1$

	/**
	 * Name tag
	 */
	final String NAME_TAG = "Name"; //$NON-NLS-1$

	/**
	 * Type tag
	 */
	final String TYPE_TAG = "Type"; //$NON-NLS-1$

	/**
	 * Action tag
	 */
	final String ACTION_TAG = "Action"; //$NON-NLS-1$

	/**
	 * Text tag
	 */
	final String TEXT_TAG = "Text"; //$NON-NLS-1$

	/**
	 * Text rule tag
	 */
	final String TEXTRULE_TAG = "Text"; //$NON-NLS-1$

	/**
	 * Component rule tag
	 */
	final String COMPONENTRULE_TAG = "Component"; //$NON-NLS-1$

	/**
	 * Match case tag
	 */
	final String MATCHCASE_TAG = "MatchCase"; //$NON-NLS-1$

	/**
	 * Enabled tag
	 */
	final String ENABLED_TAG = "Enabled"; //$NON-NLS-1$

	/**
	 * Trigger type tag
	 */
	final String TRIGGERTYPE_TAG = "TriggerType"; //$NON-NLS-1$

	/**
	 * Configuration file tag
	 */
	final String CONFIGURATION_FILE_TAG = "ConfigurationFile"; //$NON-NLS-1$

	/**
	 * Configuration name tag
	 */
	final String CONFIGURATION_NAME_TAG = "ConfigurationName"; //$NON-NLS-1$

	/**
	 * Foreground color tag
	 */
	final String FORECOLOR_TAG = "ForeColor"; //$NON-NLS-1$

	/**
	 * Background color tag
	 */
	final String BACKCOLOR_TAG = "BackColor"; //$NON-NLS-1$

	/**
	 * Yes tag
	 */
	final String YES_TAG = "Yes"; //$NON-NLS-1$

	/**
	 * No tag
	 */
	final String NO_TAG = "No"; //$NON-NLS-1$

	/**
	 * AND tag
	 */
	final String AND_TAG = "AND"; //$NON-NLS-1$

	/**
	 * OR tag
	 */
	final String OR_TAG = "OR"; //$NON-NLS-1$

	/**
	 * Show tag
	 */
	final String SHOW_TAG = "Show"; //$NON-NLS-1$

	/**
	 * Hide tag
	 */
	final String HIDE_TAG = "Hide"; //$NON-NLS-1$

	/**
	 * Start trigger tag
	 */
	final String STARTTRIGGER_TAG = "StartTrigger"; //$NON-NLS-1$

	/**
	 * Stop trigger tag
	 */
	final String STOPTRIGGER_TAG = "StopTrigger"; //$NON-NLS-1$

	/**
	 * Activation trigger tag
	 */
	final String ACTIVATIONTRIGGER_TAG = "ActivationTrigger"; //$NON-NLS-1$

	/**
	 * Decode file tag
	 */
	final String DECODEFILE_TAG = "DecodeFile"; //$NON-NLS-1$

	/**
	 * Save history tag
	 */
	final String SAVEHISTORY_TAG = "SaveHistory"; //$NON-NLS-1$

	/**
	 * Component ID tag
	 */
	final String COMPONENTID_TAG = "ComponentId"; //$NON-NLS-1$

	/**
	 * Group ID tag
	 */
	final String GROUPID_TAG = "GroupId"; //$NON-NLS-1$

	/**
	 * Advanced filter tag
	 */
	final String ADVANCEDFILTER_TAG = "AdvancedFilter"; //$NON-NLS-1$
}
