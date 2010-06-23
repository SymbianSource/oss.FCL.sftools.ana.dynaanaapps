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
 * DecodeProvider provides decoding to the TraceViewer engine
 *
 */
package com.nokia.traceviewer.engine;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.nokia.traceviewer.engine.activation.TraceActivationComponentItem;

/**
 * DecodeProvider interface
 */
public interface DecodeProvider {

	/**
	 * Decodes trace from dataBuffer and returns properties
	 * 
	 * @param dataBuffer
	 *            dataBuffer
	 * @param properties
	 *            trace properties
	 * @return trace properties
	 */
	public TraceProperties decodeTrace(ByteBuffer dataBuffer,
			TraceProperties properties);

	/**
	 * Gets trace activation information (list of components having groups)
	 * 
	 * @param getAlsoTraces
	 *            if true, also traces are added to the activation model. If
	 *            false, traces arrays in groups are empty.
	 * @return trace activation information
	 */
	public ArrayList<TraceActivationComponentItem> getActivationInformation(
			boolean getAlsoTraces);

	/**
	 * Gets component name with component ID
	 * 
	 * @param componentId
	 *            component ID
	 * @return component name or null if not found
	 */
	public String getComponentName(int componentId);

	/**
	 * Gets group ID with component ID and group name
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupName
	 *            group name
	 * @return group ID or -1 if not found
	 */
	public int getGroupId(int componentId, String groupName);

	/**
	 * Gets group name with component and group IDs
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupId
	 *            group ID
	 * @return group name or null if not found
	 */
	public String getGroupName(int componentId, int groupId);

	/**
	 * Gets group name with component and group IDs
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupId
	 *            group ID
	 * @param traceId
	 *            trace ID
	 * @return trace name or null if not found
	 */
	public String getTraceName(int componentId, int groupId, int traceId);

	/**
	 * Gets list containing component, group and trace names in this order
	 * 
	 * @param componentId
	 *            component ID
	 * @param groupId
	 *            group ID
	 * @param traceId
	 *            trace ID
	 * @return list containing component, group and trace name. If some name is
	 *         not found, it's null
	 */
	public String[] getComponentGroupTraceName(int componentId, int groupId,
			int traceId);

	/**
	 * Gets trace metadata
	 * 
	 * @param information
	 *            trace information
	 * @return trace metadata
	 */
	public TraceMetaData getTraceMetaData(TraceInformation information);

	/**
	 * Tells if the model if loaded and valid
	 * 
	 * @return true if model is loaded and valid
	 */
	public boolean isModelLoadedAndValid();

	/**
	 * Opens decode file
	 * 
	 * @param filePath
	 *            decode file path. If fileStream is not null, this is only used
	 *            in error situations to tell the user the name of the file
	 *            having problems.
	 * @param inputStream
	 *            input stream of the decode file. If null, filePath is used to
	 *            open the file.
	 * @param createNew
	 *            true if creating new model. False if appending to old model.
	 */
	public void openDecodeFile(String filePath, InputStream inputStream,
			boolean createNew);

	/**
	 * Removes component from model
	 * 
	 * @param componentId
	 *            component ID
	 * @return true if component was found and removed, false otherwise
	 */
	public boolean removeComponentFromModel(int componentId);

	/**
	 * Sets if Decoder should add certain prefixes to the traces
	 * 
	 * @param addClassAndFunctionName
	 *            if true, add class and function name to the traces
	 * @param addComponentAndGroupName
	 *            if true, add component and group name to the traces
	 */
	public void setAddPrefixesToTrace(boolean addClassAndFunctionName,
			boolean addComponentAndGroupName);

}
