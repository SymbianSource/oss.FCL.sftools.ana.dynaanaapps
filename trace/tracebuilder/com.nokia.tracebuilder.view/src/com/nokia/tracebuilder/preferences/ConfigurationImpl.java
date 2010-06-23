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
* Configuration interface implementation
*
*/
package com.nokia.tracebuilder.preferences;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderConfigurationListener;
import com.nokia.tracebuilder.view.TraceViewPlugin;

/**
 * Configuration interface implementation
 * 
 */
public final class ConfigurationImpl implements TraceBuilderConfiguration {

	/**
	 * UI preferences
	 */
	private IPreferenceStore uiPreferences;

	/**
	 * List of configuration listeners
	 */
	private ArrayList<TraceBuilderConfigurationListener> configurationListeners;

	/**
	 * Property change listener for preferences
	 */
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.util.IPropertyChangeListener#
		 *      propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (configurationListeners != null) {
				Iterator<TraceBuilderConfigurationListener> itr = configurationListeners
						.iterator();
				while (itr.hasNext()) {
					itr.next().configurationChanged(event.getProperty(),
							event.getNewValue());
				}
			}
		}
	};

	/**
	 * Constructor
	 */
	public ConfigurationImpl() {
		uiPreferences = TraceViewPlugin.getDefault().getPreferenceStore();
		setDefaults();
		uiPreferences.addPropertyChangeListener(propertyChangeListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#hasEntry(java.lang.String)
	 */
	public boolean hasEntry(String entryName) {
		return uiPreferences.contains(entryName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#setFlag(java.lang.String,
	 *      boolean)
	 */

	public void setFlag(String flagName, boolean flag) {
		uiPreferences.setValue(flagName, flag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#getFlag(java.lang.String)
	 */
	public boolean getFlag(String flagName) {
		return uiPreferences.getBoolean(flagName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#setValue(java.lang.String,
	 *      int)
	 */
	public void setValue(String valueName, int value) {
		uiPreferences.setValue(valueName, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#getValue(java.lang.String)
	 */
	public int getValue(String valueName) {
		return uiPreferences.getInt(valueName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#setText(java.lang.String,
	 *      java.lang.String)
	 */
	public void setText(String textName, String text) {
		uiPreferences.setValue(textName, text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#getText(java.lang.String)
	 */
	public String getText(String textName) {
		return uiPreferences.getString(textName);
	}

	/**
	 * Sets default values for configuration entries not found from
	 * configuration file
	 */
	private void setDefaults() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#
	 *      addConfigurationListener(com.nokia.tracebuilder.engine.TraceBuilderConfigurationListener)
	 */
	public void addConfigurationListener(
			TraceBuilderConfigurationListener configurationListener) {
		if (configurationListeners == null) {
			configurationListeners = new ArrayList<TraceBuilderConfigurationListener>();
		}
		configurationListeners.add(configurationListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#
	 *      removeConfigurationListener(com.nokia.tracebuilder.engine.TraceBuilderConfigurationListener)
	 */
	public void removeConfigurationListener(
			TraceBuilderConfigurationListener configurationListener) {
		if (configurationListeners != null) {
			configurationListeners.remove(configurationListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderConfiguration#isAvailable()
	 */
	public boolean isAvailable() {
		return true;
	}

}