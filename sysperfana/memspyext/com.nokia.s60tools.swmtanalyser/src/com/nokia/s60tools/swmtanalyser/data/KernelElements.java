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
/**
 * This class contains the information about various kernel elements.
 */
package com.nokia.s60tools.swmtanalyser.data;
/**
 * Stores kernel handles information
 *
 */
public class KernelElements {

	private int numberOfThreads = 0;
	private int numberOfProcesses = 0;
	private int numberOfTimers = 0;
	private int numberOfSessions = 0;
	private int numberOfSemaphores = 0;
	private int numberOfServers = 0;
	private int numberOfChunks = 0;
	private int numberOfMsgQueues = 0;
	
	public int getNumberOfProcesses() {
		return numberOfProcesses;
	}
	public void setNumberOfProcesses(int numberOfProcesses) {
		this.numberOfProcesses = numberOfProcesses;
	}
	public int getNumberOfSemaphores() {
		return numberOfSemaphores;
	}
	public void setNumberOfSemaphores(int numberOfSemaphores) {
		this.numberOfSemaphores = numberOfSemaphores;
	}
	public int getNumberOfServers() {
		return numberOfServers;
	}
	public void setNumberOfServers(int numberOfServers) {
		this.numberOfServers = numberOfServers;
	}
	public int getNumberOfSessions() {
		return numberOfSessions;
	}
	public void setNumberOfSessions(int numberOfSessions) {
		this.numberOfSessions = numberOfSessions;
	}
	public int getNumberOfThreads() {
		return numberOfThreads;
	}
	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}
	public int getNumberOfTimers() {
		return numberOfTimers;
	}
	public void setNumberOfTimers(int numberOfTimers) {
		this.numberOfTimers = numberOfTimers;
	}
	public int getNumberOfChunks() {
		return numberOfChunks;
	}
	public void setNumberOfChunks(int numberOfChunks) {
		this.numberOfChunks = numberOfChunks;
	}
	public int getNumberOfMsgQueues() {
		return numberOfMsgQueues;
	}
	public void setNumberOfMsgQueues(int numberOfMsgQueues) {
		this.numberOfMsgQueues = numberOfMsgQueues;
	}
	
}
