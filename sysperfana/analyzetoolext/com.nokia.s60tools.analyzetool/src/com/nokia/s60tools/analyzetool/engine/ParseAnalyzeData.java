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
 * Description:  Definitions for the class ParseAnalyzeData
 *
 */

package com.nokia.s60tools.analyzetool.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.engine.statistic.AllocCallstack;
import com.nokia.s60tools.analyzetool.engine.statistic.AllocInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.DllLoad;
import com.nokia.s60tools.analyzetool.engine.statistic.FreeInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;
import com.nokia.s60tools.analyzetool.ui.WarningDialogWithCheckbox;

/**
 * Parses trace messages that come thru TraceViewer. If one message contains
 * &ltAT&gt prefix, the message will be saved to the data file. Data file will
 * be saved to the project [bld.inf location]\atool_temp folder.
 * 
 * @author kihe
 * 
 */
public class ParseAnalyzeData {

	/** FileOutputStream. */
	private FileOutputStream fis;

	/** File. */
	private File file;

	/** Contains information which processes are started. */
	private final Hashtable<String, Integer> processStart;

	/** Contains information which processes are ended. */
	private final AbstractList<Integer> processEnd;

	/** Contains information of where to save trace data. */
	private String usedFilePath;

	/** Contains active process information. */
	public Hashtable<Integer, ProcessInfo> processes;

	/**
	 * Contains list of executed process. One process begins with the PCS and
	 * ends with the PCE tag.
	 */
	private final AbstractList<ProcessInfo> processList;

	/**
	 * Allocation cache used when one alloc info is separated to multiple lines.
	 */
	private Hashtable<Long, AllocInfo> allocCache = null;

	/**
	 * For each memory operation, keeps count of how many callstacks are
	 * outstanding. This is used to clean up after all fragments have been
	 * processed. Zero callstack counts should not be kept.
	 */
	private Map<BaseInfo, Integer> remainingCallstacksMap = null;

	/**
	 * Deallocation cache. Used when one free info is separated to multiple
	 * lines.
	 */
	private Hashtable<Long, FreeInfo> freeCache = null;

	/**
	 * Cache for dll loads. We must find dll load item for every allocation
	 * callstack item. This is heavy process and usually we must find more than
	 * thousand times. So when using cache for found items => it makes finding
	 * process more rapid than without it.
	 */
	private Hashtable<Long, DllLoad> dllLoadCache = null;

	/**
	 * Flag to determinate need to save parsed data to file. If this flag is set
	 * to "true" all the lines which contains &ltAT&gt are saved to the file.
	 * Otherwise just parse the file/trace content.
	 */
	boolean saveDataToFile;

	boolean createGraphModel;

	/**
	 * When set to true, ignore any callstack information. Can be used decrease
	 * memory consumption of the model
	 */
	boolean ignoreCallstacks;

	/** Callstacks will be read later on demand directly from file */
	private boolean deferCallstacks;

	/**
	 * File position showing current write position, this is saved in BaseInfo
	 * for deferred callstack reading
	 */
	private long filePos;

	private int lineBreakSize;

	private int traceVersion = 0;

	private boolean showUnknownTagDialog = true;
	private boolean showAtoolBinariesVersionMismatchWarning = true;

	private String deviceAtoolVersion = "";

	long logTime;

	/**
	 * Constructor.
	 * 
	 * @param saveData
	 *            if true, save &ltAT&gt statements to file. Typical use case is
	 *            for TraceWrapper
	 * @param createModel
	 *            boolean indicating whether to create a graph model. Used to
	 *            improve performance.
	 * @param deferCallstackReading
	 *            true, if callstack reading is to be done later. This requires
	 *            saving the file position during parse phase, so saveData must
	 *            be true or use constructor with FileChannel.
	 */
	public ParseAnalyzeData(boolean saveData, boolean createModel,
			boolean deferCallstackReading) {
		this(saveData, createModel, deferCallstackReading, 0);
	}

	/**
	 * Constructor. Use this constructor when working with deferred callstacks
	 * and not saving an output file.
	 * 
	 * @param saveData
	 *            if true, save &ltAT&gt statements to file. Typical use case is
	 *            for TraceWrapper
	 * @param createModel
	 *            boolean indicating whether to create a graph model. Used to
	 *            improve performance.
	 * @param deferCallstackReading
	 *            true, if callstack reading is to be done later. This requires
	 *            saving the file position during parse phase, so saveData must
	 *            be true or use constructor with FileChannel.
	 * @param lineBreakSize
	 *            Size of line break, usually 1 for device-side file, and 2 for
	 *            host-side file
	 */
	public ParseAnalyzeData(boolean saveData, boolean createModel,
			boolean deferCallstackReading, int lineBreakSize) {

		if (deferCallstackReading && !saveData && lineBreakSize == 0) {
			throw new IllegalArgumentException(
					"linebreak size must be specified when trying to use deferred callstack reading with a .dat input file ");
		}

		processStart = new Hashtable<String, Integer>();
		processEnd = new ArrayList<Integer>();
		processes = new Hashtable<Integer, ProcessInfo>();
		processList = new ArrayList<ProcessInfo>();
		saveDataToFile = saveData;
		deferCallstacks = deferCallstackReading; // original data input might be
		// streamed through
		// TraceWrapper
		createGraphModel = createModel;
		ignoreCallstacks = !createGraphModel;
		if (!ignoreCallstacks && !deferCallstacks) {
			allocCache = new Hashtable<Long, AllocInfo>();
			freeCache = new Hashtable<Long, FreeInfo>();
			remainingCallstacksMap = new HashMap<BaseInfo, Integer>();
			dllLoadCache = new Hashtable<Long, DllLoad>();
		}
		filePos = deferCallstacks ? 0 : -1;// set to beginning of file if
		// applicable
		this.lineBreakSize = lineBreakSize;
	}

	/**
	 * Add one dllLoad object to process related list
	 * 
	 * @param dllLoad
	 *            One DllLoad
	 */
	private void addDllLoad(DllLoad dllLoad) {
		// if one of the started process contains same process id what DLL load
		// has add DLL load to list
		if (processes.containsKey(dllLoad.getProcessID())) {
			ProcessInfo tempProcessInfo = processes.get(dllLoad.getProcessID());
			tempProcessInfo.addOneDllLoad(dllLoad);
		}
	}

	/**
	 * Add one memory allocation info to process related list
	 * 
	 * @param info
	 *            One memory allocation info
	 */
	private void addMemAddress(AllocInfo info) {
		// if one of the started process contains same process id what memory
		// allocation has, add memory allocation info to list
		if (processes.containsKey(info.getProcessID())) {
			ProcessInfo tempProcessInfo = processes.get(info.getProcessID());
			tempProcessInfo.addOneAlloc(info);
		}
	}

	/**
	 * Removes memory allocation from process related memory allocations list.
	 * 
	 * @param info
	 *            Deallocation info
	 */
	private void removeMemAddress(FreeInfo info) {
		if (processes.containsKey(info.getProcessID())) {
			ProcessInfo tempProcessInfo = processes.get(info.getProcessID());
			tempProcessInfo.free(info);
		}
	}

	/**
	 * Closes the input stream if it is open.
	 */
	private final void closeStreams() {
		try {
			// if fis exists => close fis
			if (fis != null) {
				fis.close();

				// clear file and fis
				file = null;
				fis = null;
				filePos = 0;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Finish the writing.
	 * 
	 */
	public final void finish() {
		try {
			// if some data is not write to file yet => do it now
			if (fis != null) {
				fis.flush();
			}
		} catch (IOException ioe) {
			return;
		} finally {
			// close needed streams
			closeStreams();
		}

		// clear stored data
		processList.clear();
		processes.clear();
		processStart.clear();
		processEnd.clear();
		if (allocCache != null) {
			allocCache.clear();
		}
		if (freeCache != null) {
			freeCache.clear();
		}
		if (dllLoadCache != null) {
			dllLoadCache.clear();
		}
		if (remainingCallstacksMap != null) {
			remainingCallstacksMap.clear();
		}
	}

	/**
	 * Return memory allocation size for all started processes.
	 * 
	 * @return Memory allocation size for all started processes
	 */
	public final int getAllocationsSize() {
		int allocCnt = 0;
		// get memory allocations count from ongoing processes
		for (ProcessInfo p : processes.values()) {
			// the current number of memory allocations is traced as potential
			// memory leaks
			allocCnt += p.getMemLeaksNumber();
		}

		// get memory allocations count from already ended processes
		Iterator<ProcessInfo> iterPro = processList.iterator();
		while (iterPro.hasNext()) {
			ProcessInfo oneInfo = iterPro.next();
			allocCnt += oneInfo.getMemLeaksNumber();
		}
		return allocCnt;
	}

	/**
	 * Gets used data file name.
	 * 
	 * @return Data file name
	 */
	public final String getDataFileName() {
		return usedFilePath;
	}

	/**
	 * Checks given path if it is null set used path to point java temp
	 * directory.
	 * 
	 * @param path
	 *            Given path
	 * @return Data file location
	 */
	private final String getFileName(final String path) {
		if (path == null || ("").equals(path)) {
			// null path given => use java temp dir
			usedFilePath = System.getProperty("java.io.tmpdir")
					+ Constants.FILENAME;
		} else if (!path.contains(Constants.FILENAME)) {
			usedFilePath = path + Constants.FILENAME;
		}
		return usedFilePath;
	}

	/**
	 * Gets started process info.
	 * 
	 * @return Hashtable<String, String> Started processes info
	 */
	public final Hashtable<String, Integer> getStartedProcesses() {
		if (processStart.isEmpty()) {
			return null;
		}
		return processStart;
	}

	/**
	 * Returns stored statistic
	 * 
	 * @return Stored statistic
	 */
	public AbstractList<ProcessInfo> getStatistic() {
		return processList;
	}

	/**
	 * Opens the needed streams.
	 * 
	 * @param filePath
	 *            File name and path where to save trace data
	 * 
	 * @return True if stream could be opened otherwise false
	 */
	public final boolean openStreams(final String filePath) {

		boolean returnValue = false;
		try {
			// store used data file name
			usedFilePath = getFileName(filePath);

			// if file did not exists => create it
			if (file == null) {
				file = new File(usedFilePath);
			}

			// if fis did not exists => open it
			if (fis == null) {
				fis = new FileOutputStream(file, false);
			}
			returnValue = true;
		} catch (FileNotFoundException fno) {

			// if error occurs => close open streams
			closeStreams();
			returnValue = false;
		}
		return returnValue;
	}

	/**
	 * Executes parser.
	 * 
	 * @param data
	 *            File name to be used
	 * @return true on success
	 */
	public final boolean parse(final String data) {

		try {
			// if no data => leave
			if (data == null) {
				// although the data is null
				// return true because false is returned only when
				// some error occurs
				return true;
			}

			if (data.contains(Constants.PREFIX_OLD)) {
				Util.showMessageDialog(Constants.UNSUPPORTED_FORMAT_TITLE,
						Constants.UNSUPPORTED_FORMAT_MESSAGE, SWT.ICON_ERROR);
				return false;
			}

			// if data contains desired prefix
			if (data.contains(Constants.PREFIX)) {

				// parse line
				parseLine(data);

				if (!processStart.isEmpty()) {
					if (traceVersion != 3) {
						Util.showMessageDialog(
								Constants.UNSUPPORTED_FORMAT_TITLE,
								Constants.TRACE_FORMAT_VERSION_IS_HIGHER,
								SWT.ICON_ERROR);
						return false;
					}
				}

				// if saveDataToFile flag is true => save data
				if (saveDataToFile) {
					if (fis == null) {

						// streams not open => try to open
						boolean returnValue = this.openStreams(usedFilePath);
						if (!returnValue) {
							return false;
						}
					}
					// append line feed and write given data to file
					String dataAndLineFeed = data + "\n";

					// write data
					fis.write(dataAndLineFeed.getBytes("UTF-8"));
					filePos = deferCallstacks ? fis.getChannel().position()
							: -1;
				}
			}

			if (deferCallstacks && !saveDataToFile) {
				filePos += (data.length() + lineBreakSize);
			}
			return true;
		} catch (OutOfMemoryError oome) {
			return false;
		} catch (Exception e) {
			Activator.getDefault().log(IStatus.ERROR,
					"AnalyzeTool - parsing trace data", e);
			return false;
		}
	}

	/**
	 * Fills DLL load item information. Check what data file version is used,
	 * because data file format is changed.
	 * 
	 * @param dllLoad
	 *            DLL load item
	 * @param splittedText
	 *            One line of trace data file.
	 */
	private void fillDllLoadInfo(DllLoad dllLoad, String[] splittedText) {
		int processID = dllLoad.getProcessID();
		if (processes.containsKey(processID)) {
			ProcessInfo processInfo = processes.get(processID);

			if (splittedText.length > 5) {
				dllLoad.setLoadTime(processInfo.getStartTime()
						+ (logTime - processInfo.getLogTime()));
				dllLoad.setStartAddress(splittedText[4]);
				dllLoad.setEndAddress(splittedText[5]);
			}
		}
	}

	/**
	 * Parse dealloction header from the line
	 * 
	 * @param splitted
	 *            Split trace message
	 */
	private void parseFreeHeader(String[] splitted) {
		// get free line info
		String processID = splitted[1];
		FreeInfo freeInfo = new FreeInfo(splitted[3]);
		freeInfo.setProcessID(processID);
		freeInfo.setThreadId(splitted[4]);
		freeInfo.setFilePos(splitted.length > 6 ? filePos : -1);

		if (createGraphModel) {
			ProcessInfo processInfo = processes.get(freeInfo.getProcessID());
			freeInfo.setTime(processInfo.getStartTime()
					+ (logTime - processInfo.getLogTime()));
			int callstackCount = Integer.parseInt(splitted[5], 16);

			if (!ignoreCallstacks && !deferCallstacks) {
				AbstractList<AllocCallstack> callstack = new ArrayList<AllocCallstack>();
				createCallstack(splitted, freeInfo.getProcessID(), callstack,
						6, freeInfo.getTime());
				freeInfo.addCallstack(callstack);

				// if this free item contains fragments
				// so we must store this info to cache
				// and rest of the callstack items later
				if (callstackCount > (splitted.length - 6)) {
					freeCache.put(freeInfo.getMemoryAddress(), freeInfo);
					// expect fragments
					remainingCallstacksMap.put(freeInfo, callstackCount
							- callstack.size());
				}
			}
		}
		removeMemAddress(freeInfo);
	}

	/**
	 * Parse dealloction fragment from the line
	 * 
	 * @param splitted
	 *            Split trace message
	 */
	private void parseFreeFragment(String[] splitted) {
		if (createGraphModel) {
			String procId = splitted[1];
			int processId = Integer.parseInt(procId, 16);
			if (processes.containsKey(processId)) {
				ProcessInfo processInfo = processes.get(processId);
				String memAddr = splitted[3];
				Long memoryAddress = Long.parseLong(memAddr, 16);
				Long time = processInfo.getStartTime()
						+ (logTime - processInfo.getLogTime());
				String packetNumber = splitted[4];

				// if cache contains corresponding free info
				if (freeCache.containsKey(memoryAddress)) {
					FreeInfo info = freeCache.get(memoryAddress);
					if (info.getMemoryAddress() == memoryAddress
							&& info.getTime() == time) {
						AbstractList<AllocCallstack> callstack = new ArrayList<AllocCallstack>();
						createCallstack(splitted, processId, callstack, 5, time);
						info.updateFragment(callstack, packetNumber);

						int callstackCount = callstack.size();
						int remaining = remainingCallstacksMap.get(info);
						remaining -= callstackCount;
						if (remaining <= 0) {
							remainingCallstacksMap.remove(info);
							freeCache.remove(info);
							info.finaliseCallstack();
						} else {
							remainingCallstacksMap.put(info, remaining);
						}
					}
				}
			}
		}
	}

	/**
	 * Mark DLL as unloaded This provides functionality for dynamically loaded
	 * DLL loads
	 * 
	 * @param lineFragments
	 *            elements of text to parse for unloading dll
	 */
	private void unloadDll(String[] lineFragments) {
		int processID = Integer.parseInt(lineFragments[1], 16);

		if (processes.containsKey(processID)) {
			ProcessInfo processInfo = processes.get(processID);
			String dllName = lineFragments[3];

			DllLoad dll = null;

			if (lineFragments.length > 5) {
				long dllUnloadTime = processInfo.getStartTime()
						+ (logTime - processInfo.getLogTime());
				long startAddr = Long.parseLong(lineFragments[4], 16);
				long endAddr = Long.parseLong(lineFragments[5], 16);

				dll = processInfo.unloadOneDll(dllName, startAddr, endAddr,
						dllUnloadTime);
			}

			// remove found DLL load item from cache
			if (!ignoreCallstacks && !deferCallstacks && dll != null) {
				for (Entry<Long, DllLoad> entry : dllLoadCache.entrySet()) {
					if (entry.getValue().equals(dll)) {
						dllLoadCache.remove(entry.getKey());
						System.out.println("dllLoadCache.removedEntry for"
								+ dll.getName());
					}
				}
			}
		}
	}

	/**
	 * Parse allocation header info from the line
	 * 
	 * @param splitted
	 *            Split trace message
	 */
	private void parseAllocHeader(String[] splitted) {
		try {
			String procID = splitted[1];
			int processID = Integer.parseInt(procID, 16);
			if (processes.containsKey(processID)) {
				AllocInfo oneAlloc = new AllocInfo(splitted[3]);
				oneAlloc.setProcessID(procID);
				oneAlloc.setFilePos(splitted.length > 6 ? filePos : -1);
				if (createGraphModel) {
					ProcessInfo processInfo = processes.get(processID);
					oneAlloc.setTime(processInfo.getStartTime()
							+ (logTime - processInfo.getLogTime()));
					oneAlloc.setSizeInt(Integer.parseInt(splitted[4], 16));
					oneAlloc.setThreadId(splitted[5]);

					// if one trace message contains callstack
					if (!ignoreCallstacks && !deferCallstacks
							&& splitted.length > 6) {

						int callstackSize = Integer.parseInt(splitted[6], 16);

						AbstractList<AllocCallstack> callstack = new ArrayList<AllocCallstack>();
						createCallstack(splitted, processID, callstack, 7,
								oneAlloc.getTime());
						oneAlloc.addCallstack(callstack);

						callstackSize -= callstack.size();
						if (callstackSize > 0) {
							// expect fragments
							remainingCallstacksMap.put(oneAlloc, callstackSize);
							allocCache.put(oneAlloc.getMemoryAddress(),
									oneAlloc);
						}
					}
				}
				addMemAddress(oneAlloc);
			}
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
	}

	/**
	 * Parse allocation fragment from the line
	 * 
	 * @param splitted
	 *            Split trace message
	 */
	private void parseAllocFragment(String[] splitted) {
		if (createGraphModel) {
			String procId = splitted[1];
			int processId = Integer.parseInt(procId, 16);
			if (processes.containsKey(processId)) {
				ProcessInfo processInfo = processes.get(processId);
				String memAddr = splitted[3];
				long memoryAddress = Long.parseLong(memAddr, 16);
				long time = processInfo.getStartTime()
						+ (logTime - processInfo.getLogTime());
				String packetNumber = splitted[4];

				if (allocCache.containsKey(memoryAddress)) {
					AllocInfo info = allocCache.get(memoryAddress);
					if (info.getMemoryAddress() == memoryAddress
							&& info.getTime() == time) {
						AbstractList<AllocCallstack> callstack = new ArrayList<AllocCallstack>();
						createCallstack(splitted, processId, callstack, 5, time);
						info.updateFragment(callstack, packetNumber);

						int callstackCount = callstack.size();
						int remaining = remainingCallstacksMap.get(info);
						remaining -= callstackCount;
						if (remaining <= 0) {
							remainingCallstacksMap.remove(info);
							info.finaliseCallstack();
							allocCache.remove(info);
						} else {
							remainingCallstacksMap.put(info, remaining);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates callstack values
	 * 
	 * @param splitted
	 *            Callstack values
	 * @param processId
	 *            Process id
	 * @param callstack
	 *            Callstack reference where the callstack values are added
	 * @param startIndex
	 *            Index where to start parse callstack values
	 */
	private void createCallstack(String[] splitted, int processId,
			AbstractList<AllocCallstack> callstack, int startIndex, long time) {
		// append whole callstack as a one memory address
		for (int i = startIndex; i < splitted.length; i++) {
			try {
				AllocCallstack allocCallstack = new AllocCallstack(splitted[i]);

				// define dll load for current alloc
				DllLoad dllLoad = getDllLoadName(processId, Long.parseLong(
						splitted[i], 16), time);
				if (dllLoad != null) {
					allocCallstack.setDllLoad(dllLoad);
					callstack.add(allocCallstack);
				}
			} catch (NumberFormatException nfe) {
				// nothing by design
			} catch (Exception e) {
				// nothing by design
			}
		}
	}

	/**
	 * Returns DLL load item for the memory address. Checks that entered memory
	 * address is DLL load memory area
	 * 
	 * @param processId
	 *            Process id
	 * @param memoryAddress
	 *            Memory address
	 * @return DllLoad item if found otherwise null
	 */
	private DllLoad getDllLoadName(int processId, Long memoryAddress, long time) {
		if (processes.containsKey(processId)) {

			// check does cache contains already corresponding item
			if (dllLoadCache.containsKey(memoryAddress)) {
				return dllLoadCache.get(memoryAddress);
			}

			// no item found in the cache loop thru the loaded DLLs
			ProcessInfo processInfo = processes.get(processId);

			for (DllLoad oneLoad : processInfo.getDllLoads()) {
				if (memoryAddress >= oneLoad.getStartAddress()
						&& memoryAddress <= oneLoad.getEndAddress()
						&& time >= oneLoad.getLoadTime()
						&& time <= oneLoad.getUnloadTime()) {
					// dll load found => save it to cache and return it
					dllLoadCache.put(memoryAddress, oneLoad);
					return oneLoad;
				}
			}
		}
		return null;
	}

	/**
	 * Process the line: switch on the event type and fill the model.
	 * 
	 * @param aLine
	 *            one debug print
	 */
	private final void parseLine(final String aLine) {

		try {
			// parse switch
			int index = aLine.indexOf(Constants.PREFIX); // lines should be
			// preceded by <AT>

			String time = aLine.substring(0, index).trim();

			if (createGraphModel) {
				logTime = Long.parseLong(time, 16);
			}

			String usedString = aLine.substring(index, aLine.length());

			String[] lineFragments = usedString.split(" ");

			// determine the memory operation/event
			String event = null;
			if (lineFragments.length >= 3) {
				if (lineFragments[1].equals(Constants.DEVINFO)) {
					event = lineFragments[1];
				} else {
					event = lineFragments[2];
				}
			}

			DllLoad dllLoad = null;

			switch (Constants.Operation.toOperation(event)) {
			case PCS:
				// process start
				parseProcessStart(lineFragments);
				break;
			case PCE:
				// process end
				parseProcessEnd(lineFragments);
				break;
			case ALH:
				// allocation header
				parseAllocHeader(lineFragments);
				break;
			case ALF:
				// allocation fragment
				if (!ignoreCallstacks && !deferCallstacks) {
					parseAllocFragment(lineFragments);
				}
				break;
			case FRH:
				// free header
				parseFreeHeader(lineFragments);
				break;
			case FRF:
				// free fragment
				if (!ignoreCallstacks && !deferCallstacks) {
					parseFreeFragment(lineFragments);
				}
				break;
			case RAH:
				// reallocation header (not yet implemented)
				break;
			case RAF:
				// reallocation fragment (not yet implemented)
				break;
			case DLL:
				// DLL load
				if (createGraphModel && !ignoreCallstacks) {
					dllLoad = new DllLoad();
					dllLoad.setProcessID(lineFragments[1]);
					dllLoad.setName(lineFragments[3]);
					fillDllLoadInfo(dllLoad, lineFragments);
					addDllLoad(dllLoad);
				}
				break;
			case DLU:
				// DLL unload
				if (createGraphModel && !ignoreCallstacks) {
					unloadDll(lineFragments);
				}
				break;
			case TDS:
				// thread start
				break;
			case TDE:
				// thread end
				break;
			case VER:
				// version
				break;
			case TSS:
				// subtest start
				break;
			case TSE:
				// subtest end
				break;
			case DEVINFO:
				// device info
				break;
			default:
				if (showUnknownTagDialog) {
					final String unknownTag = event;
					PlatformUI.getWorkbench().getDisplay().asyncExec(
							new Runnable() {
								public void run() {
									WarningDialogWithCheckbox dialog = new WarningDialogWithCheckbox(
											new Shell(), unknownTag);
									int ret = dialog.open();
									if (ret == Dialog.OK
											|| ret == Dialog.CANCEL) {
										if (dialog.dontShow())
											showUnknownTagDialog = false;

										synchronized (ParseAnalyzeData.this) {
											ParseAnalyzeData.this.notify();
										}
									}
								}
							});

					synchronized (this) {
						this.wait();
					}
				}
				break;
			}
		} catch (Exception e) {
			Activator.getDefault().log(IStatus.ERROR,
					"Error while parsing data", e);
		}
	}

	/**
	 * Processing PCE tag
	 * 
	 * @param lineFragments
	 *            String[] containing split PCE tag line
	 */
	private void parseProcessEnd(String[] lineFragments) {

		// process id
		String procId = null;

		// if data contains all the needed information
		if (lineFragments.length >= 2) {
			procId = lineFragments[1];
		}

		int processId = Integer.parseInt(procId, 16);

		if (processes.containsKey(processId)) {
			ProcessInfo processInfo = processes.get(processId);

			if (createGraphModel) {
				processInfo.setEndTime(processInfo.getStartTime()
						+ (logTime - processInfo.getLogTime()));
			}

			// store process id and process name
			this.processEnd.add(processId);

			if (processStart.containsValue(processId)) {
				for (Enumeration<String> e = processStart.keys(); e
						.hasMoreElements();) {
					Object key = e.nextElement();
					Object value = processStart.get(key);
					if (value.equals(processId)) {
						processStart.remove(key);
					}
				}
			}

			// process found from the started processes list
			// add it to list
			if (processInfo != null) {
				addProcessInfoToList(processes.get(processId));
			}

			// remove process id from processes list
			processes.remove(processId);

			// clear found DLL load items list, this prevents that results
			// between runs do not mixed up
			if (dllLoadCache != null) {
				dllLoadCache.clear();
			}
		}
	}

	/**
	 * Processing PCS tag
	 * 
	 * @param lineFragments
	 *            String[] containing split PCS tag line
	 */
	private void parseProcessStart(String[] lineFragments) {

		ProcessInfo processInfo = new ProcessInfo();

		if (lineFragments.length > 8) {
			// set trace data version number
			processInfo.setTraceDataVersion(lineFragments[7]);
			traceVersion = processInfo.getTraceDataVersion();

			int processId = Integer.parseInt(lineFragments[1], 16);
			processInfo.setProcessID(processId);

			processInfo.setProcessName(lineFragments[3]);
			processInfo.setStartTime(lineFragments[5]);
			deviceAtoolVersion = lineFragments[8];

			if (!createGraphModel) {
				String carbideAtoolVersion = Util.getAtoolVersionNumber(Util
						.getAtoolInstallFolder());
				if (showAtoolBinariesVersionMismatchWarning) {
					if (!deviceAtoolVersion.equals(carbideAtoolVersion)) {
						Util
								.showMessageDialog(
										Constants.CLE_VERSION_MISMATCH,
										MessageFormat
												.format(
														Constants.AT_BINARIES_VERSION_MISMATCH,
														deviceAtoolVersion,
														carbideAtoolVersion),
										SWT.ICON_WARNING);
						showAtoolBinariesVersionMismatchWarning = false;
					}
				}
			}

			if (createGraphModel) {
				processInfo.setLogTime(logTime);
			}

			// store process id and process name
			processStart.put(lineFragments[3], processId);
			processes.put(processId, processInfo);
		}
	}

	/**
	 * Adds process info to the list
	 * 
	 * @param info
	 *            ProcessInfo reference
	 */
	private void addProcessInfoToList(ProcessInfo info) {
		if (processList.isEmpty()) {
			processList.add(info);
			return;
		}
		for (int i = 0; i < processList.size(); i++) {
			ProcessInfo tempInfo = processList.get(i);
			if (info.getStartTime() != null
					&& info.getStartTime() < tempInfo.getStartTime()) {
				processList.add(i, info);
				return;
			}
		}
		processList.add(info);
	}

	/**
	 * Returns true if callstack reading from file is done on demand; false if
	 * callstacks are made available during parsing phase.
	 * 
	 * @return true for deferred callstack reading
	 */
	public boolean hasDeferredCallstacks() {
		return deferCallstacks;
	}
}
