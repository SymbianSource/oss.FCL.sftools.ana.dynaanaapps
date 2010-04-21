/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class AnalyzeFactory
 *
 */
package com.nokia.s60tools.analyzetool.engine;

import java.util.AbstractList;
import java.util.ArrayList;

import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;
import com.nokia.s60tools.analyzetool.internal.engine.MemoryActivityModel;

/**
 * Factory for creating the memory model of AnalyzeTool
 *
 */
public class AnalyzeFactory {
	private static final IMemoryActivityModel EMPTY_MODEL= new EmptyAnalysisGraphModel();
	
	/**
	 * Creates an instance of IMemoryActivityModel
	 * @param emptyModel if true return a model that cannot take processes
	 * @return the newly created model
	 */
	public IMemoryActivityModel createModel(final boolean emptyModel){
		return emptyModel ? EMPTY_MODEL : new MemoryActivityModel();
	}
	
	/**
	 * Returns an empty model
	 * @return Empty memory model
	 */
	public static IMemoryActivityModel getEmptyModel(){
		return EMPTY_MODEL;
	}

	private static class EmptyAnalysisGraphModel implements IMemoryActivityModel {
		

		/* (non-Javadoc)
		 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getFirstMemOpTime()
		 */
		public Long getFirstMemOpTime() {
			return 0L;
		}

		/* (non-Javadoc)
		 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getFirstProcessTime()
		 */
		public Long getFirstProcessTime() {
			return 0L;
		}

		/* (non-Javadoc)
		 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getHighestCumulatedMemoryAlloc()
		 */
		public int getHighestCumulatedMemoryAlloc() {
			return 10*1024;
		}

		/* (non-Javadoc)
		 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getLastMemOpTime()
		 */
		public Long getLastMemOpTime() {
			return 0L;
		}

		/* (non-Javadoc)
		 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getLastProcessTime()
		 */
		public Long getLastProcessTime() {
			return 0L;
		}

		/* (non-Javadoc)
		 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#getProcesses()
		 */
		public AbstractList<ProcessInfo> getProcesses() {
			return new ArrayList<ProcessInfo>();
		}

		public void addListener(IMemoryActivityModelChangeListener listener) {
			//immediately call this listener to let it know there is no data
			//there is no need to add this listener
			listener.onProcessesAdded();
		}

		/* (non-Javadoc)
		 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#addProcesses(java.util.AbstractList)
		 */
		public void addProcesses(AbstractList<ProcessInfo> processes) {
			// nothing to do
		}

		/* (non-Javadoc)
		 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#removeListener(com.nokia.s60tools.analyzetool.engine.IMemoryActivityModelChangeListener)
		 */
		public void removeListener(IMemoryActivityModelChangeListener listener) {
			// nothing to do
		}

		public ProcessInfo getSelectedProcess() {
			return null;
		}

		public void setSelectedProcess(ProcessInfo processInfo) {
			//cannot occur in empty model
		}

		/* (non-Javadoc)
		 * @see com.nokia.s60tools.analyzetool.engine.IMemoryActivityModel#isDeferredCallstackReading()
		 */
		public boolean isDeferredCallstackReading() {
			return false;
		}

		public void setDeferredCallstackReading(boolean value) {
			//nothing to do
		}

		public ICallstackManager getCallstackManager() {
			return null;
		}

		public void setCallstackManager(ICallstackManager callstackManager) {
			//nothing to do
		}
	}
}
