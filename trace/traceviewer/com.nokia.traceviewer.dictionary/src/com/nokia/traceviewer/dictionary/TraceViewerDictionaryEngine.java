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
 * TraceViewer Dictionary Plugin Engine
 *
 */
package com.nokia.traceviewer.dictionary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.nokia.trace.dictionary.TraceDictionaryEngine;
import com.nokia.trace.dictionary.model.Trace;
import com.nokia.trace.dictionary.model.TraceComponent;
import com.nokia.trace.dictionary.model.TraceGroup;
import com.nokia.traceviewer.engine.DecodeProvider;
import com.nokia.traceviewer.engine.TraceInformation;
import com.nokia.traceviewer.engine.TraceMetaData;
import com.nokia.traceviewer.engine.TraceProperties;
import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;

/**
 * TraceViewer Dictionary Plugin Engine
 * 
 */
public class TraceViewerDictionaryEngine implements DecodeProvider {

	/**
	 * Dictionary Engine from com.nokia.trace.dictionary
	 */
	private final TraceDictionaryEngine dictionaryEngine;

	/**
	 * TraceViewer Dictionary Activation handler
	 */
	private final ActivationHandler activationHandler;

	/**
	 * TraceViewer Dictionary Decode handler
	 */
	private final DecodeHandler decodeHandler;

	/**
	 * Building model
	 */
	private boolean buildingModel;

	/**
	 * Boolean indicating if class and method names should be added to the trace
	 * when decoded
	 */
	private boolean addClassMethodPrefix = true;

	/**
	 * Boolean indicating if component and group names should be added to the
	 * trace when decoded
	 */
	private boolean addComponentGroupPrefix = true;

	/**
	 * Constructor
	 */
	public TraceViewerDictionaryEngine() {
		// Create New com.nokia.trace.dictionary Engine
		dictionaryEngine = new TraceDictionaryEngine();
		activationHandler = new ActivationHandler(dictionaryEngine.getModel());
		decodeHandler = new DecodeHandler(dictionaryEngine);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DecodeProvider#decodeTrace(java.nio.ByteBuffer
	 * , com.nokia.traceviewer.engine.TraceProperties)
	 */
	public TraceProperties decodeTrace(ByteBuffer dataBuffer,
			TraceProperties properties) {
		properties = decodeHandler.decode(dataBuffer, properties,
				addClassMethodPrefix, addComponentGroupPrefix);
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DecodeProvider#getActivationInformation(
	 * boolean)
	 */
	public ArrayList<TraceActivationComponentItem> getActivationInformation(
			boolean getAlsoTraces) {
		ArrayList<TraceActivationComponentItem> activationComponents = activationHandler
				.getActivationInformation(getAlsoTraces);

		return activationComponents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DecodeProvider#getComponentGroupTraceName
	 * (int, int, int)
	 */
	public String[] getComponentGroupTraceName(int componentId, int groupId,
			int traceId) {
		String[] names = new String[3];

		// Component name
		TraceComponent component = dictionaryEngine.getModel().getComponent(
				componentId);
		if (component != null) {
			String componentName = component.getName();
			names[0] = componentName;

			// Group name
			TraceGroup group = component.getGroup(groupId);
			if (group != null) {
				String groupName = group.getName();
				names[1] = groupName;

				// Trace name
				Trace trace = group.getTrace(traceId);
				if (trace != null) {
					String traceName = trace.getName();
					names[2] = traceName;
				}
			}
		}

		return names;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DecodeProvider#getComponentName(int)
	 */
	public String getComponentName(int componentId) {
		String componentName = null;
		TraceComponent component = dictionaryEngine.getModel().getComponent(
				componentId);
		if (component != null) {
			componentName = component.getName();
		}

		return componentName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DecodeProvider#getGroupName(int, int)
	 */
	public String getGroupName(int componentId, int groupId) {
		String groupName = null;
		TraceComponent component = dictionaryEngine.getModel().getComponent(
				componentId);
		if (component != null) {
			TraceGroup group = component.getGroup(groupId);
			if (group != null) {
				groupName = group.getName();
			}
		}

		return groupName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DecodeProvider#getGroupId(int,
	 * java.lang.String)
	 */
	public int getGroupId(int componentId, String groupName) {
		int groupId = dictionaryEngine.getModel().getGroupIdWithName(
				componentId, groupName);
		return groupId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DecodeProvider#getTraceMetaData(com.nokia
	 * .traceviewer.engine.TraceInformation)
	 */
	public TraceMetaData getTraceMetaData(TraceInformation information) {
		TraceMetaData metaData = decodeHandler.getTraceMetaData(information);
		return metaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DecodeProvider#getTraceName(int, int,
	 * int)
	 */
	public String getTraceName(int componentId, int groupId, int traceId) {
		String traceName = null;
		TraceComponent component = dictionaryEngine.getModel().getComponent(
				componentId);
		if (component != null) {
			TraceGroup group = component.getGroup(groupId);
			if (group != null) {
				Trace trace = group.getTrace(traceId);
				if (trace != null) {
					traceName = trace.getName();
				}
			}
		}
		return traceName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.DecodeProvider#isModelLoadedAndValid()
	 */
	public boolean isModelLoadedAndValid() {
		boolean loadedAndValid = false;
		boolean valid = dictionaryEngine.getModel().isValid();
		int nrOfComponents = dictionaryEngine.getModel()
				.getActivationInformation().size();
		if (valid && nrOfComponents > 0 && !buildingModel) {
			loadedAndValid = true;
		}
		return loadedAndValid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DecodeProvider#openDecodeFile(java.lang.
	 * String, java.io.InputStream, boolean)
	 */
	public void openDecodeFile(String filePath, InputStream inputStream,
			boolean createNew) {
		buildingModel = true;
		File file = new File(filePath);

		// Check if the file is a ZIP file
		try {
			ZipFile zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			// Loop through elements
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				if (!entry.isDirectory()) {
					inputStream = zipFile.getInputStream(entry);

					// Open the decode file entry from the ZIP
					dictionaryEngine.getModelBuilder().openDecodeFile(
							entry.getName(), inputStream, createNew);

					createNew = false;
				}
			}

		} catch (IOException e) {

			// Not a ZIP file, open as normal decode file
			dictionaryEngine.getModelBuilder().openDecodeFile(filePath,
					inputStream, createNew);
		} finally {
			buildingModel = false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DecodeProvider#removeComponentFromModel(int)
	 */
	public boolean removeComponentFromModel(int componentId) {
		boolean removed = dictionaryEngine.getModel().removeComponent(
				componentId);
		return removed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.traceviewer.engine.DecodeProvider#setAddPrefixesToTrace(boolean
	 * , boolean)
	 */
	public void setAddPrefixesToTrace(boolean addClassAndFunctionName,
			boolean addComponentAndGroupName) {
		addClassMethodPrefix = addClassAndFunctionName;
		addComponentGroupPrefix = addComponentAndGroupName;
	}
}
