/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class CallstackDataParser
 *
 */
package com.nokia.s60tools.analyzetool.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nokia.s60tools.analyzetool.engine.statistic.AllocCallstack;
import com.nokia.s60tools.analyzetool.engine.statistic.AllocInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.DllLoad;
import com.nokia.s60tools.analyzetool.engine.statistic.FreeInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;
import com.nokia.s60tools.analyzetool.global.Constants.Operation;

/**
 * This class parses line of .dat file format and extracts callstack data. For
 * now this is a separate class but could eventually be moved into
 * ParseAnalyzeData after some re-factoring.
 */
public class CallstackDataParser {

	/** True, if callstack reading for this BaseInfo is now complete. */
	boolean complete = false;

	/** Resulting callstack. */
	private List<AllocCallstack> callstack;

	/** BaseInfo for the current alloc or free */
	private BaseInfo baseInfo;

	/** ProcessInfo for the current alloc or free */
	private ProcessInfo process;

	/**
	 * Number of remaining callstack elements expected to be processed with ALF
	 * or FRF.
	 */
	private int remainingSize;

	/**
	 * Cache for callstack items. Used when allocation fragment is parsed in the
	 * wrong order.
	 */
	private HashMap<Integer, List<AllocCallstack>> callstackCache = null;

	/**
	 * Constructor.
	 * 
	 * @param baseInfo
	 *            BaseInfo for the current alloc or free
	 * @param processInfo
	 *            ProcessInfo for the current alloc or free
	 */
	public CallstackDataParser(BaseInfo baseInfo, ProcessInfo processInfo) {
		if (processInfo == null || baseInfo == null
				|| processInfo.getProcessID() != baseInfo.getProcessID()) {
			throw new IllegalArgumentException(
					"BaseInfo and ProcessInfo are mandatory and must not be null; and the process ID of both must match."); //$NON-NLS-1$
		}
		this.baseInfo = baseInfo;
		this.process = processInfo;
		callstack = new ArrayList<AllocCallstack>();
	}

	/**
	 * Parses one line of &ltAT&gt statement.
	 * 
	 * @param aLine
	 * @return true, if this callstack is now complete (all parts are available)
	 */
	public final boolean parseLine(final String aLine) {

		if (aLine.indexOf(Constants.PREFIX) == -1) {
			// not a <AT> statement
			return false;
		}

		// String[] lineFragments = getLineFragments(aLine);

		int index = aLine.indexOf(Constants.PREFIX);

		String time = aLine.substring(0, index).trim();

		// // get logging time of line in microseconds
		// long logTime = Util.getLogTime(time);

		long logTime = Long.parseLong(time, 16);

		String usedString = aLine.substring(index, aLine.length());

		String[] lineFragments = usedString.split(" ");

		if (lineFragments.length < 5) {
			// not a valid <AT> statement for callstack processing
			return false;
		}
		int processID = Integer.parseInt(lineFragments[1], 16);
		if (baseInfo.getProcessID() != processID
				|| process.getProcessID() != processID) {
			// statement is not for current process id
			return false;
		}

		// the operation must match
		Constants.Operation op = Constants.Operation
				.toOperation(lineFragments[2]);
		if (!verifyOperation(op, baseInfo)) {
			return false;
		}

		// the memory address must match
		long memoryAddress = Long.parseLong(lineFragments[3], 16);
		if (baseInfo.getMemoryAddress() != memoryAddress) {
			return false;
		}

		boolean ret = false;

		switch (op) {
		case ALH:
			ret = parseHeader(lineFragments, 6);
			break;
		case FRH:
			ret = parseHeader(lineFragments, 5);
			break;
		case ALF:
			// fall through
		case FRF:
			ret = parseFragment(lineFragments, logTime);
			break;
		default:
			// ignore this line
			break;
		}
		return ret;
	}

	private static boolean verifyOperation(Operation op, BaseInfo aBaseInfo) {
		return ((aBaseInfo instanceof AllocInfo && (op == Constants.Operation.ALH || op == Constants.Operation.ALF)) || (aBaseInfo instanceof FreeInfo && (op == Constants.Operation.FRH || op == Constants.Operation.FRF)));
	}

	// /**
	// * Returns the line fragments of the AT statement
	// *
	// * @param aLine
	// * the AT statement
	// * @return the line fragments separated by space
	// */
	// private String[] getLineFragments(final String aLine) {
	// int index = aLine.indexOf(Constants.PREFIX); // lines should be preceded
	//
	// if (index == -1) {
	// return new String[0];
	// }
	//
	// String usedString = (index == 0) ? aLine : aLine.substring(index, aLine
	// .length());
	//		return usedString.split(" "); //$NON-NLS-1$
	// }

	// /**
	// * Parses an ALC statement. This statement contains a complete callstack.
	// *
	// * @param fragments
	// * the line fragments of the statement to process
	// * @return true if statement is complete (this method will always return
	// * true)
	// */
	// private boolean parseAlloc(String[] fragments) {
	//
	// if (fragments.length > 5) {
	// createCallstack(fragments, process, callstack, 6);
	// }
	//
	// complete = true;
	// return true; // we are done; there are no fragments for this alloc
	// }

	/**
	 * Parses an ALH or FRH statement
	 * 
	 * @param fragments
	 *            the line fragments of the statement to process
	 * @param startIndex
	 *            index at which the callstack size is to be found
	 * @return true if callstack is complete, false if callstack fragment is
	 *         expected
	 */
	private boolean parseHeader(String[] fragments, int startIndex) {

		if (callstack.size() > 0 || remainingSize > 0) {
			throw new IllegalStateException(
					"Callstack list should still be empty when starting to process ALH or FRH"); //$NON-NLS-1$
		}

		if (fragments.length > startIndex) {

			int callstackSize = Integer.parseInt(fragments[startIndex], 16);
			startIndex++;

			createCallstack(fragments, process, callstack, startIndex);

			callstackSize -= callstack.size();
			if (callstackSize > 0) {
				// expect fragments
				remainingSize = callstackSize;
			} else {
				complete = true;
			}
		} else {
			complete = true; // this header doesn't have callstacks - a bit
			// strange
		}
		return complete;
	}

	/**
	 * Parses an ALF or FRF callstack fragment
	 * 
	 * @param fragments
	 *            the line fragments of the statement to process
	 * @return true if callstack is now complete; false otherwise
	 */
	private boolean parseFragment(String[] fragments, long logTime) {
		long time = process.getStartTime() + (logTime - process.getLogTime());
		int packetNumber = Integer.parseInt(fragments[4], 16);

		if (baseInfo.getTime() == time) {
			List<AllocCallstack> tmpCallstack = new ArrayList<AllocCallstack>();
			createCallstack(fragments, process, tmpCallstack, 5);
			updateFragment(tmpCallstack, packetNumber);

			remainingSize -= tmpCallstack.size();
			if (remainingSize <= 0) {
				complete = true;
				finaliseCallstack();
			}
		}
		return complete;
	}

	private DllLoad getDllForAddress(ProcessInfo p, Long memoryAddress,
			long time) {
		for (DllLoad oneLoad : p.getDllLoads()) {
			if (memoryAddress >= oneLoad.getStartAddress()
					&& memoryAddress <= oneLoad.getEndAddress()
					&& time >= oneLoad.getLoadTime()
					&& time <= oneLoad.getUnloadTime()) {
				// dll load found
				return oneLoad;
			}
		}
		return null;
	}

	private void createCallstack(String[] fragments, ProcessInfo p,
			List<AllocCallstack> callstack, int startIndex) {
		for (int i = startIndex; i < fragments.length; i++) {
			AllocCallstack callstackElem = new AllocCallstack(fragments[i]);

			// find matching dll
			DllLoad dllLoad = getDllForAddress(process, callstackElem
					.getMemoryAddress(), baseInfo.getTime());
			if (dllLoad != null) {
				callstackElem.setDllLoad(dllLoad);
			}
			callstack.add(callstackElem);
		}
	}

	/**
	 * Updates allocation fragment. Means that given callstack is addition to
	 * previous added alloc
	 * 
	 * @param callstack
	 *            Addition tmpcallstack items
	 * @param packetNumber
	 *            ordinal of callstack fragment
	 */
	private void updateFragment(List<AllocCallstack> tmpcallstack,
			int packetNumber) {
		if (packetNumber == 1) {
			// special case; this can be added to the end of the list straight
			// away
			callstack.addAll(tmpcallstack);
		} else {
			// packages may come out of order; this is managed in the
			// callstackCache
			if (callstackCache == null) {
				callstackCache = new HashMap<Integer, List<AllocCallstack>>();
			}
			callstackCache.put(packetNumber, tmpcallstack);
		}
	}

	/**
	 * Optimises internal callstack data structures. Should only be called after
	 * all data for this memory operation has been loaded (i.e. all fragments)
	 */
	public void finaliseCallstack() {
		if (callstackCache == null) {
			// nothing to do
			return;
		}

		if (!complete) {
			throw new IllegalStateException(
					"callstack processing is not yet complete."); //$NON-NLS-1$
		}

		if (callstack == null && callstackCache != null) {
			throw new IllegalStateException(); // first set of callstacks should
			// always be in callstacks
		}

		if (callstackCache != null) {
			int size = callstackCache.size();
			int i = 2;
			while (size != 0) {
				List<AllocCallstack> nextCallStacks = callstackCache.get(i);
				if (nextCallStacks != null) {
					size--;
					callstack.addAll(nextCallStacks);
				} // TODO else: missing callstack: shall we report it or log it?
				i++;
			}
			callstackCache = null;
		}
	}

	/**
	 * @return the completed callstack.
	 */
	public List<AllocCallstack> getCallstack() {
		if (!complete) {
			throw new IllegalStateException(
					"Callstack has not been completely processed."); //$NON-NLS-1$
		}

		finaliseCallstack();
		return callstack;
	}

	/**
	 * Forces the callstack state to be set to complete. This should only be
	 * used when the end of file is encountered.
	 */
	public void forceComplete() {
		complete = true;
		finaliseCallstack();
	}
}
