/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;

import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.engine.statistic.AllocCallstack;
import com.nokia.s60tools.analyzetool.engine.statistic.AllocInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.DllLoad;
import com.nokia.s60tools.analyzetool.engine.statistic.FreeInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.ProcessInfo;
import com.nokia.s60tools.analyzetool.global.Constants;

/**
 * Parses trace messages which comes thru tracing utility.
 * If one message contains PCSS prefix the message will saved to the data file Data file will be saved
 * to the project [bld.inf location]\atool_temp folder.
 *
 * @author kihe
 *
 */
public class ParseAnalyzeData {

	private static final String KEYWORD_ABNORMAL = "ABNORMAL";//$NON-NLS-1$
	/** FileOutputStream . */
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
	 * Contains list of executed process. One process will begin in the
	 * PROCESS_START and ends with the PROCESS_END tag
	 */
	private final AbstractList<ProcessInfo> processList;

	/**
	 * Allocation cache
	 * Used when one free info is separated to multiple lines.
	 */
	private final Hashtable<Long, AllocInfo> allocCache;


	/**
	 * Deallocation cache.
	 * Used when one free info is separated to multiple lines.
	 */
	private final Hashtable<Long, FreeInfo> freeCache;


	/**
	 * Cache for dll loads.
	 * We must find dll load item for every allocation callstack item.
	 * This is heavy process and usually we must find more than thousand times.
	 * So when using cache for found items => it makes finding process more rapid than without it.
	 */
	private final Hashtable<Long, DllLoad> dllLoadCache;


	/**
	 * Flag to determinate need to save parsed data to file.
	 * If this flag is set to "true" all the lines which contains PCSS are saved to the file.
	 * Otherwise just parse the file/trace content.
	 */
	boolean saveDataToFile;

	boolean createGraphModel;
	/**
	 * Constructor.
	 * @param saveData Need to save data to file.
	 */
	public ParseAnalyzeData(boolean saveData, boolean createModel) {

		processStart = new Hashtable<String, Integer>();
		processEnd = new ArrayList<Integer>();
		processes = new Hashtable<Integer, ProcessInfo>();
		processList = new ArrayList<ProcessInfo>();
		allocCache = new Hashtable<Long, AllocInfo>();
		saveDataToFile = saveData;
		dllLoadCache = new Hashtable<Long, DllLoad>();
		freeCache = new Hashtable<Long, FreeInfo>();
		createGraphModel = createModel;
	}

	/**
	 * Add one dllLoad object to process related list
	 *
	 * @param dllLoad
	 *            One DllLoad
	 */
	private void addDllLoad(DllLoad dllLoad) {
		// if one of the started process contains same process id what dll load
		// has add dll load to list
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
		// allocation has add memory allocation info to list
		if (processes.containsKey(info.getProcessID())) {
			ProcessInfo tempProcessInfo = processes.get(info.getProcessID());
			tempProcessInfo.addOneAlloc(info);
		}
	}


	/**
	 * Removes memory allocation from process related
	 * memory allocations list.
	 *
	 * @param info Deallocation info
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
		allocCache.clear();
		freeCache.clear();
		dllLoadCache.clear();
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
			//the current number of memory allocations is traced as potential memory leaks
			allocCnt += p.getMemLeaksNumber();
		}
		
		//get memory allocations count from already ended processes
		Iterator<ProcessInfo> iterPro = processList.iterator();
		while( iterPro.hasNext() ) {
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
	 */
	public final boolean parse(final String data) {

		try{
			// if no data => leave
			if (data == null) {
				//although the data is null
				//return true because false is returned only when
				//some unexpected error occurs
				return true;
			}

			// if data must be saved to project group\atool_temp folder
			if(saveDataToFile) {
				// open needed streams
				openStreams(usedFilePath);
			}

			// if data contains desired prefix => write data to file
			boolean contains = data.contains(Constants.PREFIX);

			if (contains) {
				writeDataToFile(data);
			}
			return true;
		}catch(OutOfMemoryError oome) {
			return false;
		}catch(Exception e) {
			Activator.getDefault().log(IStatus.ERROR, "AnalyzeTool - parsing trace data", e);
			return false;
		}
	}

	/**
	 * Fills dll load item information. Check what data file version is used,
	 * because data file format is changed.
	 * 
	 * @param dllLoad
	 *            Dll load item
	 * @param splittedText
	 *            One line of trace data file.
	 */
	private void fillDllLoadInfo(DllLoad dllLoad, String[] splittedText) {
		int processID = dllLoad.getProcessID();
		if(processes.containsKey(processID)) {
			ProcessInfo processInfo = processes.get(processID);

			//if data file contains also time stamp for dll load
			//this information is added later than other information
			//thats why we must check which version is used.
			if( processInfo.getTraceDataVersion() > 1 && splittedText.length > 6 ) {
				dllLoad.setLoadTime(splittedText[4]);
				dllLoad.setStartAddress(splittedText[5]);
				dllLoad.setEndAddress(splittedText[6]);
			}
			else {
				dllLoad.setStartAddress(splittedText[4]);
				dllLoad.setEndAddress(splittedText[5]);
			}
		}
	}

	/**
	 * Parses "FREE" tag information from the trace data file.
	 *
	 * Note! This tag is not used in current version,
	 * this is support for older version of trace data files.
	 * @param splitted One line content of trace file.
	 */
	private void parseFree(String[] splitted) {
		
		// check that there is enough data
		// at least we need know to memory address 
		// which is forth item of trace data
		if( splitted.length > 3 ) {
			String processID = splitted[1];
			FreeInfo freeInfo = new FreeInfo();
			freeInfo.setProcessID(processID);
			freeInfo.setMemoryAddress(splitted[3]);
			removeMemAddress(freeInfo);
		}
	}


	/**
	 * Parse dealloction header from the line
	 * @param line Allocation header line
	 */
	private void parseFreeHeader(String[] splitted) {
		//get free line info
		String processID = splitted[1];
		FreeInfo freeInfo = new FreeInfo();
		freeInfo.setProcessID(processID);

		freeInfo.setMemoryAddress(splitted[3]);
		
		if( createGraphModel ) {
		
			int traceFileVersion = 1;
			//get trace file version
			if(processes.containsKey(freeInfo.getProcessID())) {
				ProcessInfo processInfo = processes.get(freeInfo.getProcessID());
				traceFileVersion = processInfo.getTraceDataVersion();
			}
			// how many callstakc items
			int callstackCount = 0;

			// index where the callstack addresses begins
			int startIndex = 5;

			//if using the new trace file format
			if( traceFileVersion > 1 ) {
				freeInfo.setTime(splitted[4]);
				callstackCount = Integer.parseInt(splitted[5], 16);
				startIndex = 6;
			}
			else {
				callstackCount = Integer.parseInt(splitted[5], 16);
			}

			AbstractList<AllocCallstack> callstack = new ArrayList<AllocCallstack>();
			createCallstack(splitted, freeInfo.getProcessID(), callstack, startIndex);
			freeInfo.addCallstack(callstack);
			
			//if this free item contains fragments
			//so we must store this info to cache
			//and rest of the callstack items later
			if( callstackCount > (splitted.length-startIndex) ) {
				freeCache.put(freeInfo.getMemoryAddress(), freeInfo);
			}
		}
		
		removeMemAddress(freeInfo);

		
	}


	/**
	 * Parse dealloction fragment from the line
	 * @param line Allocation fragment line
	 */
	private void parseFreeFragment(String[] splitted)
	{
		if( createGraphModel ) {
			String procId = splitted[1];
			int processId = Integer.parseInt(procId,16);
			if (processes.containsKey(processId)) {
				String memAddr = splitted[3];
				Long memoryAddress =Long.parseLong(memAddr, 16);
				Long time = Long.parseLong(splitted[4],16);
				String packetNumber = splitted[5];

				//if cache contains corresponding free info
				if (freeCache.containsKey(memoryAddress)) {
					FreeInfo info = freeCache.get(memoryAddress);
					if (info.getMemoryAddress() == memoryAddress && info.getTime() == time ) {
						AbstractList<AllocCallstack> callstack = new ArrayList<AllocCallstack>();
						createCallstack(splitted, processId, callstack, 6);
						info.updateFragment(callstack, packetNumber);
					}
				}
			}
		}
	}
	
	/**
	 * Remove dll load from the list.
	 * This provides functionality for dynamically loaded dll loads
	 * @param dllLoad DllLoad item to unload
	 */
	private void unloadDll( DllLoad dllLoad )
	{
		if (processes.containsKey(dllLoad.getProcessID())) {
			ProcessInfo tempProcessInfo = processes.get(dllLoad.getProcessID());
			tempProcessInfo.unloadOneDll(dllLoad);

			//remove found dll load item from cache
			for( java.util.Enumeration<Long> e = dllLoadCache.keys(); e.hasMoreElements();)
			{
				Long key = e.nextElement();
				DllLoad tempDllLoad = dllLoadCache.get(key);

				// if values equals remove it from the list
				if (tempDllLoad.getProcessID() == dllLoad.getProcessID() && tempDllLoad.getStartAddress() == dllLoad.getStartAddress()
						&& tempDllLoad.getEndAddress() == dllLoad.getEndAddress() && tempDllLoad.getName() == tempDllLoad.getName()) {
					dllLoadCache.remove(key);
				}
			}

		}
	}

	/**
	 * Parse allocation header info from the line
	 * @param line Allocation line
	 */
	private void parseAllocHeader(String[] splitted)
	{
		try {
			String procID = splitted[1];
			int processID = Integer.parseInt(procID, 16);
			if (processes.containsKey(processID)) {
				AllocInfo oneAlloc = new AllocInfo();
				oneAlloc.setProcessID(procID);
				oneAlloc.setMemoryAddress(splitted[3]);
				if( createGraphModel ) {
					oneAlloc.setTime(splitted[4]);
					
					oneAlloc.setSizeInt(Integer.parseInt(splitted[5], 16));
					// if one trace message contains callstack
					if (splitted.length > 6) {

						AbstractList<AllocCallstack> callstack = new ArrayList<AllocCallstack>();
						createCallstack(splitted, processID, callstack, 7);
						oneAlloc.addCallstack(callstack);
						allocCache.put(oneAlloc.getMemoryAddress(), oneAlloc);
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
	 * @param line
	 *            Allocation fragment line
	 */
	private void parseAllocFragment(String[] splitted) {
		if( createGraphModel ) {
			String procId = splitted[1];
			int processId = Integer.parseInt(procId, 16);
			if (processes.containsKey(processId)) {
				String memAddr = splitted[3];
				Long memoryAddress = Long.parseLong(memAddr,16);
				Long time = Long.parseLong(splitted[4],16);
				String packetNumber = splitted[5];

				if (allocCache.containsKey(memoryAddress)) {
					AllocInfo info = allocCache.get(memoryAddress);
					if (info.getMemoryAddress() == memoryAddress && info.getTime() == time) {
						AbstractList<AllocCallstack> callstack = new ArrayList<AllocCallstack>();
						createCallstack(splitted, processId, callstack, 6);
						info.updateFragment(callstack, packetNumber);
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
	private void createCallstack(String[] splitted, int processId, AbstractList<AllocCallstack> callstack, int startIndex) {
		// append whole callstack as a one memory address
		for (int i = startIndex; i < splitted.length; i++) {
			try{
				AllocCallstack allocCAll = new AllocCallstack();
				allocCAll.setMemoryAddress(splitted[i]);

				// define dll load for current alloc
				DllLoad dllLoad = getDllLoadName(processId, Long.parseLong(splitted[i],16));
				if( dllLoad != null ) {
					allocCAll.setDllLoad(dllLoad);
					callstack.add(allocCAll);	
				}
			} catch(NumberFormatException nfe) {
				//no nothing by design
				
			} catch( Exception e ) {
				//no nothing by design
			}
			
		}
	}

	/**
	 * Parses memory address from the one trace message line. Parses one trace
	 * message whole callstack to one memory address. This memory address is
	 * used to calculate most used memory allocation location.
	 * 
	 * @param line
	 *            One trace message
	 */
	private void parseMemAddressesFromLine(String[] splitted) {

		//TODO
		String processID = splitted[1];
		if (processes.containsKey(processID)) {
			AllocInfo oneAlloc = new AllocInfo();
			oneAlloc.setProcessID(processID);
			oneAlloc.setMemoryAddress(splitted[3]);
			oneAlloc.setTime(splitted[4]);
			oneAlloc.setSizeInt(Integer.parseInt(splitted[5], 16));

			// if one trace message contains callstack
			if (splitted.length > 5) {
				AbstractList<AllocCallstack> callstack = new ArrayList<AllocCallstack>();
				// append whole callstack as a one memory address
				for (int i = 6; i < splitted.length; i++) {
					AllocCallstack allocCAll = new AllocCallstack();
					allocCAll.setMemoryAddress(splitted[i]);

					// define dll load for current alloc
					DllLoad dllLoad = getDllLoadName(oneAlloc.getProcessID(), allocCAll.getMemoryAddress());
					if (dllLoad != null) {
						allocCAll.setDllLoad(dllLoad);
					}

					callstack.add(allocCAll);
				}
				oneAlloc.addCallstack(callstack);
				addMemAddress(oneAlloc);
			}
		}
	}

	/**
	 * Returns dll load item for the memory address. Checks that entered memory
	 * address is dll load memory area
	 * 
	 * @param processId
	 *            Process id
	 * @param memoryAddress
	 *            Memory address
	 * @return DllLoad item if found otherwise null
	 */
	private DllLoad getDllLoadName(int processId, Long memoryAddress) {
		if (processes.containsKey(processId)) {
			ProcessInfo tempProcessInfo = processes.get(processId);
			Hashtable<String, DllLoad> loads = tempProcessInfo.getDllLoads();

			// check does cache contains already corresponding item
			if (dllLoadCache.containsKey(memoryAddress)) {
				return dllLoadCache.get(memoryAddress);
			}

			// no item found in the cache loop thru the loaded dlls
			for (java.util.Enumeration<String> e = loads.keys(); e.hasMoreElements();) {
				try {
					String key = e.nextElement();
					DllLoad oneLoad = loads.get(key);
					Long start = oneLoad.getStartAddress();
					Long end =  oneLoad.getEndAddress();
					Long actual = memoryAddress;
					if (actual >= start && actual <= end) {
						// dll load found => save it to cache and return it
						dllLoadCache.put(memoryAddress, oneLoad);
						return oneLoad;
					}
				} catch (java.lang.NumberFormatException nfe) {
					// trace message corrupt?
				}
			}
		}
		return null;
	}

	/**
	 * process the line: switch on the event type and fill the model
	 * 
	 * @param aLine
	 *            One debug print
	 */
	private final void parseLine(final String aLine) {

		try {
			// parse switch
			int index = aLine.indexOf(Constants.PREFIX); // lines should be preceded
			// with PCSS
			String usedString = null;

			if (index == -1 || index == 0) {
				usedString = aLine;
			} else {
				usedString = aLine.substring(index, aLine.length());
			}

			String[] lineFragments = usedString.split(" ");

			// determine the memory operation/event
			String event = null;
			if (lineFragments.length >= 3) {
				event = lineFragments[2];
			}

			DllLoad dllLoad = null;

			switch (Constants.Operation.toOperation(event)) {
			case PROCESS_START:
				// process PROCESS_START
				parseProcessStarted(lineFragments);
				break;
			case PROCESS_END:
				// process PROCESS_END
				parseProcessEnded(lineFragments);
				break;
			case ALLOC:
				// process ALLOC
				parseMemAddressesFromLine(lineFragments);
				break;
			case FREE:
				parseFree(lineFragments);
				break;
			case ALLOCH:
				parseAllocHeader(lineFragments);
				break;
			case FREEH:
				parseFreeHeader(lineFragments);
				break;
			case ALLOCF:
				parseAllocFragment(lineFragments);
				break;
			case FREEF:
				parseFreeFragment(lineFragments);
				break;
			case DLL_LOAD:
				if(createGraphModel) {
					dllLoad = new DllLoad();
					dllLoad.setProcessID(lineFragments[1]);
					dllLoad.setName(lineFragments[3]);
					fillDllLoadInfo(dllLoad, lineFragments);
					addDllLoad(dllLoad);
				}
				break;
			case DLL_UNLOAD:
				if(createGraphModel) {
					dllLoad = new DllLoad();
					dllLoad.setProcessID(lineFragments[1]);
					dllLoad.setName(lineFragments[3]);
					fillDllLoadInfo(dllLoad, lineFragments);
					unloadDll(dllLoad);
				}
				break;
			default:
				// ignore this line
				break;
			}
		}catch(Exception e) {
			Activator.getDefault().log(IStatus.ERROR, "Error while parsing data", e);
		}
		
	}

	/**
	 * Processing PROCESS_END tag
	 * @param lineFragments String[] containing split PROCESS_END tag line
	 */
	private void parseProcessEnded(String[] lineFragments) {

		// process id
		String procId = null;

		// if data contains all the needed information
		if (lineFragments.length >= 2) {
			procId = lineFragments[1];
		}
		
		int processId = Integer.parseInt(procId, 16);
		if (processes.containsKey(processId)) {
			if( lineFragments.length > 4 ){
				//check is ABNORMAL process end and that there are enough data to parse
				if( lineFragments[4].equals(KEYWORD_ABNORMAL) && lineFragments.length > 5 ) {
					processes.get(processId).setEndTime(lineFragments[5]);	
				}
				else {
					processes.get(processId).setEndTime(lineFragments[4]);
				}	
			}
			

			// store process id and process name
			this.processEnd.add(processId);

			if (processStart.containsValue(processId)) {
				for (java.util.Enumeration<String> e = processStart.keys(); e.hasMoreElements();) {
					Object key = e.nextElement();
					Object value = processStart.get(key);
					if (value.equals(processId)) {
						processStart.remove(key);
					}

				}
			}

			// PROCESS_END tag reached
			// check that started processes contains this process id.
			ProcessInfo info = processes.get(processId);

			// process found from the started processes list
			// add it to list
			if (info != null) {
				addProcessInfoToList(processes.get(processId));
			}

			// remove process id from processes list
			processes.remove(processId);

			// clear the founded dll load items list
			// this prevent that results between runs do not mixed up
			dllLoadCache.clear();
		}
	}

	/**
	 * Processing PROCESS_START tag
	 * @param lineFragments String[] containing split PROCESS_START tag line
	 */
	private void parseProcessStarted(String[] lineFragments) {

		String procId = lineFragments[1];
		int processId = Integer.parseInt(procId, 16);

		ProcessInfo processInfo = new ProcessInfo();
		processInfo.setProcessID(processId);

		if (lineFragments.length >= 4) {
			processInfo.setProcessName(lineFragments[3]);
		}

		if (lineFragments.length > 5) {
			processInfo.setStartTime(lineFragments[5]);
		}

		// set trace data version number
		// this information is used later when parsing e.g. one allocation
		// information
		if (lineFragments.length > 7) {
			processInfo.setTraceDataVersion(lineFragments[7]);
		}

		// store process id and process name
		if( lineFragments.length > 3 ) {
			this.processStart.put(lineFragments[3], processId);
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
			if( info.getStartTime() != null && info.getStartTime() < tempInfo.getStartTime() ){
				processList.add(i, info);
				return;
			}
		}
		processList.add(info);
	}

	/**
	 * Writes data to file if line contains wanted data.
	 * 
	 * @param data
	 *            Data to write
	 */
	private void writeDataToFile(String data) {
		try {

			// check if data contains PROCESS_START or PROCESS_END tag
			parseLine(data);

			// if saveDataToFile flag is true => save data
			if (saveDataToFile) {

				if (fis == null) {

					// streams not open => try to open
					boolean returnValue = this.openStreams(usedFilePath);
					if (!returnValue) {
						return;
					}
				}

				// append line feed and write given data to file
				String dataAndLineFeed = data + "\n";

				// write data
				fis.write(dataAndLineFeed.getBytes("UTF-8"));
			}

		} catch (IOException ioe) {
			return;
		}
	}
}
