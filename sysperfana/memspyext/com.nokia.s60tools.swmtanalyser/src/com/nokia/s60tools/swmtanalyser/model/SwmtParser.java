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
package com.nokia.s60tools.swmtanalyser.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.nokia.s60tools.swmtanalyser.data.ChunksData;
import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.DiskData;
import com.nokia.s60tools.swmtanalyser.data.FilesData;
import com.nokia.s60tools.swmtanalyser.data.GlobalDataChunks;
import com.nokia.s60tools.swmtanalyser.data.HeapData;
import com.nokia.s60tools.swmtanalyser.data.KernelHandles;
import com.nokia.s60tools.swmtanalyser.data.PSHandlesData;
import com.nokia.s60tools.swmtanalyser.data.StackData;
import com.nokia.s60tools.swmtanalyser.data.SystemData;
import com.nokia.s60tools.swmtanalyser.data.WindowGroups;
import com.nokia.s60tools.swmtanalyser.exception.SwmtFormatException;

/**
 * Reads the text log files and stores all the data in CycleData objects.
 *
 */
public class SwmtParser {

	private static BufferedReader reader = null;

	/**
	 * @param path specifes the filesystem path of swmt log to be parsed
	 * @param cycleData is the data structure, wherein the parsed data gets stored.
	 * @return error messages during parsing. In case of successful parsing, null will be returned
	 */
	public static String parseSwmtLog(String path, CycleData cycleData) throws SwmtFormatException
	{
		FileReader inputStr;
		int lineNo = 0;
		
		try 
		{
			inputStr = new FileReader(path);
		
			reader = new BufferedReader(inputStr);
			String s = null;
		    		    
			while((s = reader.readLine()) != null)
			{
				lineNo++;
				
				if(s.contains("{SYSM}"))
				{
					SystemData sysData = readSystemMemory(s);
					
					if(sysData != null)
					{
						if(s.contains("Free"))
							cycleData.setFreeMemory(sysData.getFreeMemory());
						else if(s.contains("Total"))
							cycleData.setTotalMemory(sysData.getTotalMemory());
					}
					else
					{
						SwmtFormatException ex = new SwmtFormatException("Error in SYSM view. ");
						throw ex;
					}
					//String freeRam = s.substring(s.indexOf(FREE_MEM) + (FREE_MEM).length()).trim();
					
				}
				if(s.contains("{DISK}")){
					DiskData diskData = readDiskData(s);
									
					if(diskData != null){
						cycleData.addDiskData(diskData);
					}
					else{
						SwmtFormatException ex = new SwmtFormatException("Error in DISK view. ");
						throw ex;
					}
				}
				else if(s.contains("{HGEN}"))
				{
					KernelHandles kernelData = readKernelHandlesData(s);
					
					if(kernelData != null){
						cycleData.addKernelData(kernelData);
					}
					else{
						SwmtFormatException ex = new SwmtFormatException("Error in HGEN view. ");
						throw ex;
					}
				}
				else if(s.contains("{FILE}"))
				{
					//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Processing File info..");
					FilesData fileData = readFileData(s);
					
					if(fileData != null)
						cycleData.addFileData(fileData);
					
					else{
						SwmtFormatException ex = new SwmtFormatException("Error in FILE view. ");
						throw ex;
					}
				}
				else if(s.contains("{HEAP}"))
				{
					//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Processing Heap info..");
					HeapData heapData = readHeapData(s);
					
					if(heapData != null)
						cycleData.addHeapData(heapData);
					else{
						SwmtFormatException ex = new SwmtFormatException("Error in HEAP view. ");
						throw ex;
					}
				}
				else if(s.contains("{STAK}"))
				{
					//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Processing Stack info..");
					StackData stackData = readStackData(s);
					
					if(stackData != null)
						cycleData.addStackData(stackData);
					else{
						SwmtFormatException ex = new SwmtFormatException("Error in STAK view. ");
						throw ex;
					}
				}
				else if(s.contains("{HPAS}"))
				{
					PSHandlesData psHandlesData= readHPASHandlesData(s);
					
					if(psHandlesData != null){
						cycleData.addHPASHandlesData(psHandlesData);
					}
					else{
						SwmtFormatException ex = new SwmtFormatException("Error in HPAS view. ");
						throw ex;
					}
				}
				else if(s.contains("{CHNK}"))
				{
					ChunksData chunksData= readChunksData(s);
					
					if(chunksData != null){
						cycleData.addChunksData(chunksData);
					}
					else{
						SwmtFormatException ex = new SwmtFormatException("Error in CHNK view. ");
						throw ex;
					}
				}
				else if(s.contains("{GLOD}"))
				{
					GlobalDataChunks globalChunksData= readGlobalChunksData(s);
					
					if(globalChunksData != null){
						cycleData.addGlobalChunksData(globalChunksData);
					}
					else{
						SwmtFormatException ex = new SwmtFormatException("Error in GLOD view. ");
						throw ex;
					}
				}
				else if(s.contains("{WNDG}"))
				{
					WindowGroups wndgData = readWindowGroupsData(s);
					
					if(wndgData != null){
						cycleData.addWindowGroupsData(wndgData);
					}
					else
					{
						SwmtFormatException ex = new SwmtFormatException("Error in WNDG view. ");
						throw ex;
					}
				}
			}
		
		} 
		catch (FileNotFoundException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}catch(SwmtFormatException e)
		{
			throw new SwmtFormatException(e.getMessage() + " Line No: " + lineNo); 
		}
		
		finally {
			try{
				reader.close();
			}catch(IOException e){
				
			}
		}
		return null;
	}
	
	/**
	 * This method reads the DISK data from the given string 
	 * and returns a DiskData object. If the string does not contain
	 * "MemSpy" and "{DISK}" tags, it returns null.
	 */
	private static DiskData readDiskData(String str) throws SwmtFormatException
	{
		//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Reading Disk Data..");
		
		if(str.contains("MemSpy") && str.contains("{DISK}"))
		{
			DiskData data = new DiskData();
			String [] subStrings = str.split(",");
			
			if(subStrings.length !=8)
				throw new SwmtFormatException("Invalid number of fields in DISK view.");
			
			data.setName(subStrings[1].trim());
			data.setSize(subStrings[3].trim());
			data.setFreeSize(subStrings[4].trim());
			data.setStatus(subStrings[7].trim());
			
			return data;
		}
		return null;
	}
	
	/**
	 * This method reads the information under SYSM bucket to get the
	 * total memory and free memory of the system.  
	 */
	private static SystemData readSystemMemory(String str) throws SwmtFormatException
	{
		if(str.contains("MemSpy") && str.contains("{SYSM}"))
		{
			SystemData sysData = new SystemData();
			String [] subStrings = str.split(",");
			
			if(subStrings.length !=4)
				throw new SwmtFormatException("Invalid number of fields in SYSM view.");
			
			if(str.contains("Free"))
				sysData.setFreeMemory(Long.parseLong(subStrings[2]));
			if(str.contains("Total"))
				sysData.setTotalMemory(Long.parseLong(subStrings[2]));
			
			return sysData;
		}
		
		return null;
	}
	/**
	 * This method reads the Kerenel Handles data from the given string 
	 * and returns a KernelHandles object. If the string does not contain
	 * "MemSpy" and "{HGEN}" tags, it returns null.
	 */
	private static KernelHandles readKernelHandlesData(String str) throws SwmtFormatException
	{
		if(str.contains("MemSpy") && str.contains("{HGEN}"))
		{
			KernelHandles kernelData = new KernelHandles();
			//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Parsing Kernel Data " + str);
			
			String [] subStrings = str.split(",");
			
			if(subStrings.length != 5)
				throw new SwmtFormatException("Invalid number of fields in HGEN view.");
			
			kernelData.setHandleName(subStrings[1].trim());
			kernelData.setHandle(subStrings[2].trim());
			kernelData.setHandleType(subStrings[3].trim());
			kernelData.setStatus(subStrings[4].trim());
			
			return kernelData;
		}
		return null;
	}
	
	/**
	 * This method reads the FILE data from the given string 
	 * and returns a FilesData object. If the string does not contain
	 * "MemSpy" and "{FILE}" tags, it returns null.
	 * @throws SwmtFormatException if the string is having invalid number formats
	 */
	private static FilesData readFileData(String str) throws SwmtFormatException
	{
		if(str.contains("MemSpy") && str.contains("{FILE}"))
		{
			//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Parsing File Data " + str);
			FilesData fileData = new FilesData();
			
			String [] subStrings = str.split(",");
			
			if(subStrings.length != 6)
				throw new SwmtFormatException("Invalid number of fields in FILE view");
			
			fileData.setFileName(subStrings[1].trim());
			fileData.setThreadName(subStrings[2].trim());
			fileData.setFileSize(subStrings[3].trim());
			fileData.setStatus(subStrings[5].trim());
			
			return fileData;
		}
		return null;
	}
	
	/**
	 * This method reads the Heap data from the given string 
	 * and returns a HeapData object. If the string does not contain
	 * "MemSpy" and "{HEAP}" tags, it returns null.
	 * @throws SwmtFormatException
	 */
	private static HeapData readHeapData(String str) throws SwmtFormatException
	{
		if(str.contains("MemSpy") && str.contains("{HEAP}"))
		{
			//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Parsing Heap Data " + str);
			HeapData heapData = new HeapData();
			
			String [] subStrings = str.split(",");
			
			if(subStrings.length != 18)
				throw new SwmtFormatException("Invalid number of fields in HEAP view");
			
			if(!subStrings[1].contains("::"))
				throw new SwmtFormatException("Invalid thread name");
			
			heapData.setThreadAndProcessName(subStrings[1].trim());
			heapData.setBaseAddr(subStrings[4].trim());
			heapData.setSize(subStrings[5].trim());
			heapData.setMaxSize(subStrings[7].trim());
			heapData.setAllocatedCells(subStrings[10].trim());
			heapData.setAllocSpace(subStrings[11].trim());
			heapData.setFreeCells(subStrings[12].trim());
			heapData.setFreeSpace(subStrings[13].trim());
			heapData.setFreeSlack(subStrings[14].trim());
			heapData.setLargestFreeCell(subStrings[15].trim());
			heapData.setLargestAllocCell(subStrings[16].trim());
			heapData.setStatus(subStrings[17].trim());
			
			return heapData;
		}
		return null;
	}
	
	/**
	 * This method reads the STACK data from the given string 
	 * and returns a StackData object. If the string does not contain
	 * "MemSpy" and "{STAK}" tags, it returns null.
	 * @throws SwmtFormatException
	 */
	private static StackData readStackData(String str) throws SwmtFormatException
	{
		if(str.contains("MemSpy") && str.contains("{STAK}"))
		{
			//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Parsing Stack Data " + str);
			StackData stackData = new StackData();
			
			String [] subStrings = str.split(",");
			
			if(subStrings.length != 6)
				throw new SwmtFormatException("Invalid number of fields in STAK view");
			
			if(!subStrings[1].contains("::"))
				throw new SwmtFormatException("Invalid thread name");
			
			stackData.setThreadName(subStrings[1].trim());
			stackData.setChunkName(subStrings[2].trim());
			stackData.setSize(subStrings[4].trim());
			stackData.setStatus(subStrings[5].trim());
			
			return stackData;
		}
		return null;
	}
	
	/**
	 * This method reads the HPAS Handles data from the given string 
	 * and returns a HPAS Handles object. If the string does not contain
	 * "MemSpy" and "{HPAS}" tags, it returns null.
	 */
	private static PSHandlesData readHPASHandlesData(String str) throws SwmtFormatException
	{
		if(str.contains("MemSpy") && str.contains("{HPAS}"))
		{
			PSHandlesData psHandlesData = new PSHandlesData();
			
			String [] subStrings = str.split(",");
			
			if(subStrings.length != 11)
				throw new SwmtFormatException("Invalid number of fields in HPAS view");			
			
			psHandlesData.setHandleName(subStrings[1].trim());
			psHandlesData.setHandle(subStrings[2].trim());
			psHandlesData.setKeyType(Integer.parseInt(subStrings[3].trim()));
			psHandlesData.setThreadId(Long.parseLong(subStrings[8].trim()));
			psHandlesData.setThreadName(subStrings[9].trim());
			psHandlesData.setStatus(subStrings[10].trim());
					
			return psHandlesData;
		}
		return null;
	}
	
	/**
	 * This method reads the Chunks data from the given string 
	 * and returns a Chunk data object. If the string does not contain
	 * "MemSpy" and "{CHNK}" tags, it returns null.
	 */
	private static ChunksData readChunksData(String str) throws SwmtFormatException
	{
		if(str.contains("MemSpy") && str.contains("{CHNK}"))
		{
			ChunksData chunkData = new ChunksData();
			
			String [] subStrings = str.split(",");
			if(subStrings.length != 8)
				throw new SwmtFormatException("Invalid number of fields in CHUNK view");			
			
			chunkData.setProcessName(subStrings[1].trim());
			chunkData.setChunkName(subStrings[2].trim());
			chunkData.setHandle(subStrings[3].trim());
			chunkData.setBaseAddr(subStrings[4].trim());
			chunkData.setSize(Long.parseLong(subStrings[5].trim()));
			if(subStrings[7].equals("[N]+[A]"))
				chunkData.setAttrib(CycleData.New);
			else if(subStrings[7].equals("[D]"))
				chunkData.setAttrib(CycleData.Deleted);
			else if(subStrings[7].equals("[A]"))
				chunkData.setAttrib(CycleData.Alive);
			
			return chunkData;
		}
		return null;
	}
	
	/**
	 * This method reads the Global Chunks data from the given string 
	 * and returns a GlobalChunk data object. If the string does not contain
	 * "MemSpy" and "{GLOD}" tags, it returns null.
	 */
	private static GlobalDataChunks readGlobalChunksData(String str) throws SwmtFormatException
	{
		if(str.contains("MemSpy") && str.contains("{GLOD}"))
		{
			GlobalDataChunks chunkData = new GlobalDataChunks();
			
			String [] subStrings = str.split(",");
			if(subStrings.length != 8)
				throw new SwmtFormatException("Invalid number of fields in HPAS view");			
			
			chunkData.setProcessName(subStrings[1].trim());
			chunkData.setChunkName(subStrings[2].trim());
			chunkData.setBaseAddr(subStrings[4].trim());
			
			if(subStrings[5].trim().length()!=0)
				chunkData.setSize(Long.parseLong(subStrings[5].trim()));
			else
				chunkData.setSize(-1);
			
			if(subStrings[7].trim().equals("[N]+[A]"))
				chunkData.setAttrib(CycleData.New);
			else if(subStrings[7].trim().equals("[D]"))
				chunkData.setAttrib(CycleData.Deleted);
			else if(subStrings[7].trim().equals("[A]"))
				chunkData.setAttrib(CycleData.Alive);
			
			return chunkData;
		}
		return null;
	}

	/**
	 * 
	 * This method parses the data related to WNDG events.
	 */
	private static WindowGroups readWindowGroupsData(String start_line) throws SwmtFormatException
	{
		if(start_line.contains("MemSpy") && start_line.contains("{WNDG}"))
		{
			String [] subStrings = start_line.split(",");
			
			if(subStrings.length != 6)
				throw new SwmtFormatException("Invalid number of fields in WNDG view");			
			
			WindowGroups wndgData = new WindowGroups();
			
			String id_str = subStrings[1].trim();
			int id = Integer.parseInt(id_str);
			
			int event = Integer.parseInt(subStrings[4].trim());
			
			wndgData.setId(id);
			wndgData.setName(subStrings[2].trim());
			wndgData.setEvent(event);
			
			if(subStrings[5].trim().equals("[N]+[A]"))
				wndgData.setStatus(CycleData.New);
			else if(subStrings[5].trim().equals("[D]"))
				wndgData.setStatus(CycleData.Deleted);
			else if(subStrings[5].trim().equals("[A]"))
				wndgData.setStatus(CycleData.Alive);
			
			return wndgData;
		}
		return null;
	}
}
