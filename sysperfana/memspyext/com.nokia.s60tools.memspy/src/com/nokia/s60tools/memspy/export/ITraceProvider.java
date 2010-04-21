/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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
package com.nokia.s60tools.memspy.export;

/**
 * Interface for binding external trace data providers for MemSpy.
 * Only a single trace data provider instance at a time is supported.
 * In case more that a single trace data providers are available 
 * the selection among them is undeterministic.
 */
public interface ITraceProvider {
	
	/**
	 * Connects to trace source.
	 * @param traceClient Client to send information about changes in connectivity status and errors etc.
	 * @return <code>true</code> on success, otherwise <code>false</code>
	 */
	public boolean connectTraceSource(ITraceClientNotificationsIf traceClient);
	
	/**
	 * Disconnects to trace source.
	 */
	public void disconnectTraceSource();
	
	/**
	 * Sends current usage context-specific integer data to launcher.
	 * Integer data can contain values that can be expressed with 10 bytes
	 * i.e. only 10 lower bytes are taken into account when setting data.
	 * @param integerData integer data to be sent
	 * @return <code>false</code> if failed to send integer data, otherwise <code>true</code>
	 */
	public boolean sendIntData(int integerData);
	
	/**
	 * Sends current usage context-specific string message to launcher.
	 * @param stringData string data to send
	 * @return <code>true</code> on success, otherwise <code>false</code>
	 */
	boolean sendStringData( String stringData);
	
	/**
	 * Activates trace with given group ID.
	 * @param traceGroupID trace group ID
	 * @return <code>true</code> on success, otherwise <code>false</code>
	 */
	boolean activateTrace(String traceGroupID);
	
	/**
	 * Starts trace data listening
	 * @param dataProcessor data processor that further handles trace data at MemSpy side.
	 * @return <code>true</code> in case data processor is registered properly, otherwise <code>false</code>
	 */
	public boolean startListenTraceData(ITraceDataProcessor dataProcessor);
	
	/**
	 * Stops trace data listening
	 */
	public void stopListenTraceData();
	
	/**
	 * Gets preference page ID for the user trace source.
	 * @return preference page ID for the user trace source
	 */
	public String getTraceSourcePreferencePageId();
	
	/**
	 * Gets current connections display name.
	 * @return current connections display name
	 */
	public String getDisplayNameForCurrentConnection();
	
}
