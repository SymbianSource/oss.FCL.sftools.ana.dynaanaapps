/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Delegate to configuration of the view
*
*/
package com.nokia.tracebuilder.engine;

import java.util.ArrayList;

/**
 * Delegate to configuration of the view
 * 
 */
final class ConfigurationDelegate implements TraceBuilderConfiguration {

	/**
	 * Configuration from view
	 */
	private TraceBuilderConfiguration configuration;

	/**
	 * Temporary list of configuration listeners
	 */
	private ArrayList<TraceBuilderConfigurationListener> tempListeners;

	/**
	 * Sets the configuration delegate
	 * 
	 * @param configuration
	 *            the configuration
	 */
	void setConfiguration(TraceBuilderConfiguration configuration) {
		this.configuration = configuration;
		if (configuration != null && tempListeners != null) {
			for (int i = 0; i < tempListeners.size(); i++) {
				TraceBuilderConfigurationListener listener = tempListeners
						.get(i);
				configuration.addConfigurationListener(listener);
				listener.configurationCreated();
			}
			tempListeners.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#isAvailable()
	 */
	public boolean isAvailable() {
		boolean retval;
		if (configuration != null) {
			retval = configuration.isAvailable();
		} else {
			retval = false;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#
	 *      addConfigurationListener(com.nokia.tracebuilder.engine.TraceBuilderConfigurationListener)
	 */
	public void addConfigurationListener(
			TraceBuilderConfigurationListener configurationListener) {
		if (configuration != null) {
			configuration.addConfigurationListener(configurationListener);
		} else {
			if (tempListeners == null) {
				tempListeners = new ArrayList<TraceBuilderConfigurationListener>();
			}
			tempListeners.add(configurationListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#
	 *      removeConfigurationListener(com.nokia.tracebuilder.engine.TraceBuilderConfigurationListener)
	 */
	public void removeConfigurationListener(
			TraceBuilderConfigurationListener configurationListener) {
		if (configuration != null) {
			configuration.removeConfigurationListener(configurationListener);
		} else {
			if (tempListeners != null) {
				tempListeners.remove(configurationListener);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#getFlag(java.lang.String)
	 */
	public boolean getFlag(String flagName) {
		boolean retval;
		if (configuration != null) {
			retval = configuration.getFlag(flagName);
		} else {
			retval = false;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#getText(java.lang.String)
	 */
	public String getText(String textName) {
		String retval;
		if (configuration != null) {
			retval = configuration.getText(textName);
		} else {
			retval = ""; //$NON-NLS-1$
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#getValue(java.lang.String)
	 */
	public int getValue(String valueName) {
		int retval;
		if (configuration != null) {
			retval = configuration.getValue(valueName);
		} else {
			retval = 0;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#hasEntry(java.lang.String)
	 */
	public boolean hasEntry(String entryName) {
		boolean retval;
		if (configuration != null) {
			retval = configuration.hasEntry(entryName);
		} else {
			retval = false;
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#
	 *      setFlag(java.lang.String, boolean)
	 */
	public void setFlag(String flagName, boolean flag) {
		if (configuration != null) {
			configuration.setFlag(flagName, flag);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#
	 *      setText(java.lang.String, java.lang.String)
	 */
	public void setText(String textName, String text) {
		if (configuration != null) {
			configuration.setText(textName, text);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#setValue(java.lang.String,
	 *      int)
	 */
	public void setValue(String valueName, int value) {
		if (configuration != null) {
			configuration.setValue(valueName, value);
		}
	}

}
