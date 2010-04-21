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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.nokia.s60tools.swmtanalyser.data.ChunksData;
import com.nokia.s60tools.swmtanalyser.data.CycleData;
import com.nokia.s60tools.swmtanalyser.data.DiskOverview;
import com.nokia.s60tools.swmtanalyser.data.GlobalDataChunks;
import com.nokia.s60tools.swmtanalyser.data.OverviewData;
import com.nokia.s60tools.swmtanalyser.data.ParsedData;
import com.nokia.s60tools.swmtanalyser.data.SystemData;
import com.nokia.s60tools.swmtanalyser.data.ThreadData;
import com.nokia.s60tools.swmtanalyser.data.WindowGroupEventData;
import com.nokia.s60tools.util.debug.DbgUtility;

/**
 * Creates Excel when log files exported. 
 *
 */
public class ExcelCreator {

	private String fileName;
	private FileOutputStream out;
	
	private HSSFWorkbook wb;
	private OverviewData ovData;
	private ArrayList<Integer> constants = new ArrayList<Integer>();
	
	private Map<String, HSSFCellStyle> styles;
	private ParsedData logData; 
	private Map<String, ArrayList<ThreadData>> heapData = new HashMap<String, ArrayList<ThreadData>> ();
	private Map<String, ArrayList<GlobalDataChunks>> glodData = new HashMap<String, ArrayList<GlobalDataChunks>> ();
	private Map<String, ArrayList<ChunksData>> chunkData = new HashMap<String, ArrayList<ChunksData>> ();
	private Map<String, ThreadData> deltaData = new HashMap<String, ThreadData> ();
	private Map<String, String> glodDeltaData = new HashMap<String, String> ();
	private Map<String, String> chunkDeltaData = new HashMap<String, String> ();
	private ArrayList<String> heapThreads;
	private ArrayList<String> glodChunks;
	private ArrayList<String> nonHeapChunks;
	
	private long totalHeapSizeChange =0;
	private long totalFreeCellChange =0;
	private long totalAllocCellChange =0;
	private long totalFreeSpaceChange =0;
	private long totalAllocSpaceChange =0;
	private long totalSlackSpaceChange =0;
	private long totalFilesChange =0;
	private long totalHandlesChange =0;
	private int j = 0;
	
	private static final String NOT_AVAILABLE = "N/A";
	
 	/**
 	 * Construction
 	 * @param fileName
 	 */
 	public ExcelCreator(String fileName) {
 		//create a new workbook
 		wb = new HSSFWorkbook();
 		this.fileName = fileName;
 	}
 	
 	/**
 	 * Set overview data
 	 * @param ovdata
 	 */
 	public void setOverviewPageInput(OverviewData ovdata)
 	{
 		this.ovData = ovdata;
 	}
 	
 	/**
 	 * Set all the parsed data of log files.
 	 * @param cyclesData
 	 */
 	public void setInputCyclesData(ParsedData cyclesData)
 	{
 		this.logData = cyclesData;
 	}
 	
 	/**
 	 * The cycle numbers to be skipped when there are more than 254 cycles.
 	 * @param constants
 	 */
 	public void setSkipFileConstant(ArrayList<Integer> constants)
 	{
 		this.constants = constants;
 	}
 	
 	private boolean updateMonitor(IProgressMonitor monitor, String message)
 	{
 		if(monitor!=null)
 			monitor.subTask(message);
 		if(monitor.isCanceled())
 		{
 			monitor.done();
 			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Create a excel document
 	 * @param monitor
 	 * @return <code>true</code> if document was created successfully <code>false</code> otherwise.
 	 */
 	public boolean createExcel(IProgressMonitor monitor)
 	{

 		File file = new File(fileName);
 		
 		if(file.exists() && !file.delete()){
 			Runnable p = new Runnable(){
				public void run() {
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "SWMT Analyser", fileName + "file already exists and could not be deleted.\nPlease close it if open and try again.");
				}					
			};
			Display.getDefault().asyncExec(p);
			return false;
 		}
 		
 		try {
			out = new FileOutputStream(fileName); 
		} catch (FileNotFoundException e) {
			Runnable p = new Runnable(){
				public void run() {
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "SWMT Analyser", "Error in creating file " + fileName);
				}
			};
			Display.getDefault().asyncExec(p);
			return false;

		}
 	
		styles = createStyles(wb);
		
		SWMTLogReaderUtils utilsObj = new SWMTLogReaderUtils();
		
		readGlobalDataFromAllCycles(glodData, logData);
		getFieldDifferencesForAllGlodChunks(glodDeltaData, logData);
 		
		readChunkDataFromAllCycles(chunkData, logData);
		getFieldDifferencesForAllChunks(chunkDeltaData, logData);
 				
		readHeapDataFromAllCycles(heapData, logData);
 		getFieldDifferencesForAllThreads(deltaData, logData);
		
		heapThreads = utilsObj.getAllHeapThreads(logData);
		Collections.sort(heapThreads);

 		if(!updateMonitor(monitor, "Creating overview sheet..."))
 			return false;
 		createOverViewTab();
 		
		if(!updateMonitor(monitor, "Creating disk memory sheet..."))
			return false;			
 		createDiskVariationSheet();
 		
		if(!updateMonitor(monitor, "Creating global chunks sheet..."))
 			return false;
 		createGlobalChunksSheet();
 		
		if(!updateMonitor(monitor, "Creating non heap chunks sheet..."))
			return false;
		createNonHeapChunkSheet();
		
		if(!updateMonitor(monitor, "Creating heap size sheet..."))
			return false;
		createHeapSizeTab();
		
		if(!updateMonitor(monitor, "Creating heap allocated space sheet..."))
			return false;
		createHeapAllocSpaceTab();
		
		if(!updateMonitor(monitor, "Creating heap free space sheet..."))
			return false;
		createHeapFreeSpaceTab();
		
		if(!updateMonitor(monitor, "Creating allocated cells sheet..."))
			return false;
		createAllocatedCellsTab();
		
		if(!updateMonitor(monitor, "Creating free cells sheet..."))
			return false;
		createFreeCellsTab();
		
		if(!updateMonitor(monitor, "Creating free slack sheet..."))
			return false;
		createFreeSlackTab();
		
		if(!updateMonitor(monitor, "Creating largest allocated cells sheet..."))
			return false;
		createLargestAllocSizeTab();
		
		if(!updateMonitor(monitor, "Creating largest free size sheet..."))
			return false;
		createLargestFreeSizeTab();
 		
		if(!updateMonitor(monitor, "Creating window groups sheet..."))
			return false;
		createWindowGroupSheet();
		
 		// write the workbook to the output stream
		// close our file (don't blow out our file handles
		try {
			wb.write(out);
			out.close();
		} catch (IOException err) {
			err.printStackTrace();
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
 			return false;
		}

 		return true;
 	}
	
	private boolean createOverViewTab()
	{
		//create a new sheet
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		// declare a cell object reference
		HSSFCell cell = null;
	
		//set the sheet name in Unicode
		wb.setSheetName(0, "Overview");
		
		row=sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell= row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Overview"));
		
		row=sheet.createRow(1);
		cell=row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Number of Cycles"));
		cell=row.createCell(1);
		cell.setCellStyle(styles.get("cell_number"));
		
		CycleData firstCycle = logData.getLogData()[0];
		
		if(ovData.noOfcycles == 1)
			cell.setCellValue(new HSSFRichTextString(ovData.noOfcycles + " (" + firstCycle.getCycleNumber() + ")"));
		else
			cell.setCellValue(ovData.noOfcycles);
		
		row=sheet.createRow(2);
		cell=row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Number of Cycles Truncated"));
		cell=row.createCell(1);
		cell.setCellValue(constants.size());
		
		row=sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Time Period"));
		HSSFCell timePeriod = row.createCell(1);
		
		row=sheet.createRow(4);
		cell=row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Time Duration"));
		cell=row.createCell(1);
		cell.setCellValue(new HSSFRichTextString(ovData.duration + " sec(" + ovData.durationString + ")"));
		
		row=sheet.createRow(5);
		cell=row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("ROM Checksum"));
		cell=row.createCell(1);
		cell.setCellValue(new HSSFRichTextString(firstCycle.getRomCheckSum()));
		
		row=sheet.createRow(6);
		cell=row.createCell(0);
		cell.setCellValue(new HSSFRichTextString("ROM Version"));
		cell=row.createCell(1);
		cell.setCellValue(new HSSFRichTextString(firstCycle.getRomVersion()));
		
		sheet.createRow(7);
		sheet.createRow(8);
		sheet.createRow(9);
		
		row = sheet.createRow(10);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString(""));
		
		cell = row.createCell(1);
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		HSSFRow totalHeapSizeRow = sheet.createRow(11);
		cell = totalHeapSizeRow.createCell(0);
		cell.setCellStyle(styles.get("cell_bold"));
		cell.setCellValue(new HSSFRichTextString("Heap Size"));
		
		HSSFRow freeCellRow = sheet.createRow(12);
		cell = freeCellRow.createCell(0);
		cell.setCellStyle(styles.get("cell_bold"));
		cell.setCellValue(new HSSFRichTextString("Free Cell count"));
		
		HSSFRow alloCellRow = sheet.createRow(13);
		cell = alloCellRow.createCell(0);
		cell.setCellStyle(styles.get("cell_bold"));
		cell.setCellValue(new HSSFRichTextString("Allocated Cell count"));
		
		HSSFRow freeSpaceRow = sheet.createRow(14);
		cell = freeSpaceRow.createCell(0);
		cell.setCellStyle(styles.get("cell_bold"));
		cell.setCellValue(new HSSFRichTextString("Free space"));
		
		HSSFRow allocSpaceRow = sheet.createRow(15);
		cell = allocSpaceRow.createCell(0);
		cell.setCellStyle(styles.get("cell_bold"));
		cell.setCellValue(new HSSFRichTextString("Allocated space"));
		
		HSSFRow slackSpaceRow = sheet.createRow(16);
		cell = slackSpaceRow.createCell(0);
		cell.setCellStyle(styles.get("cell_bold"));
		cell.setCellValue(new HSSFRichTextString("Slack space"));
		
		HSSFRow filesRow = sheet.createRow(17);
		cell = filesRow.createCell(0);
		cell.setCellStyle(styles.get("cell_bold"));
		cell.setCellValue(new HSSFRichTextString("Total files"));
		
		HSSFRow psHandlesRow = sheet.createRow(18);
		cell = psHandlesRow.createCell(0);
		cell.setCellStyle(styles.get("cell_bold"));
		cell.setCellValue(new HSSFRichTextString("Total P&S Handles"));
		
		sheet.createRow(19);
		sheet.createRow(20);
		sheet.createRow(21);
		sheet.createRow(22);
		
		row = sheet.createRow(23);
		createOverviewFields(row);
		
		createDataInOverView(sheet, 24);
		
		cell = totalHeapSizeRow.createCell(1);
		cell.setCellStyle(styles.get("blue_font"));
		cell.setCellValue(totalHeapSizeChange);
		
		cell = freeCellRow.createCell(1);
		cell.setCellStyle(styles.get("blue_font"));
		cell.setCellValue(totalFreeCellChange);
		
		cell = alloCellRow.createCell(1);
		cell.setCellStyle(styles.get("blue_font"));
		cell.setCellValue(totalAllocCellChange);
		
		cell = freeSpaceRow.createCell(1);
		cell.setCellStyle(styles.get("blue_font"));
		cell.setCellValue(totalFreeSpaceChange);
		
		cell = allocSpaceRow.createCell(1);
		cell.setCellStyle(styles.get("blue_font"));
		cell.setCellValue(totalAllocSpaceChange);
		
		cell = slackSpaceRow.createCell(1);
		cell.setCellStyle(styles.get("blue_font"));
		cell.setCellValue(totalSlackSpaceChange);
		
		cell = filesRow.createCell(1);
		cell.setCellStyle(styles.get("blue_font"));
		cell.setCellValue(totalFilesChange);
		
		cell = psHandlesRow.createCell(1);
		cell.setCellStyle(styles.get("blue_font"));
		cell.setCellValue(totalHandlesChange);
		
		sheet.autoSizeColumn((short)0);
		sheet.autoSizeColumn((short)1);
		sheet.autoSizeColumn((short)2);
		sheet.autoSizeColumn((short)3);
		sheet.autoSizeColumn((short)4);
		sheet.autoSizeColumn((short)5);
		sheet.autoSizeColumn((short)6);
		sheet.autoSizeColumn((short)7);
		sheet.autoSizeColumn((short)8);
		sheet.autoSizeColumn((short)9);
		sheet.autoSizeColumn((short)10);
		sheet.autoSizeColumn((short)11);
	
		
		if(ovData.noOfcycles > 1)
			timePeriod.setCellValue(new HSSFRichTextString(ovData.fromTime + " to " + ovData.toTime));
		else
			timePeriod.setCellValue(new HSSFRichTextString(ovData.fromTime));
		
		return true;
	}

	/**
	 * This method creates the sheet for RAM and DISK Memory data.
	 * It shows the variation of RAM and Disk Memory in each cycle.
	 *
	 */
	private void createDiskVariationSheet() {
		//create a new sheet
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(1, "RAM and Disk Memory");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("RAM and Disk Memory"));
		
		description = sheet.createRow(1);
				
		//creates an empty row
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);

    	cell = row.createCell(0);
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString(""));
		
		addCycleIntervals(row);
		
		cell = row.createCell((int)(row.getLastCellNum()));
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		ArrayList<String> diskNames = utils.getAllDiskNames(logData);
				
		int rowNo = 4;
		
		row = sheet.createRow(rowNo);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("cell_normal"));
		cell.setCellValue(new HSSFRichTextString("RAM (Used)"));
		
		ArrayList<SystemData> systemData = utils.getSystemDataFromAllCycles(logData);
		
		long [] usedMemValues = new long[logData.getNumberOfCycles()];

		j=1;
		for(int i=0; i<logData.getNumberOfCycles(); i++)
		{
			long totalMem = systemData.get(i).getTotalMemory();
			long freeMem = systemData.get(i).getFreeMemory();
			
			if(totalMem == -1 || freeMem == -1){
				//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
				usedMemValues[i] = -1;
			}
			else{
				long usedMemory = totalMem - freeMem; 
				usedMemValues[i] = usedMemory;
			}
			
			if(constants.contains(i+1))
				continue;
			
			cell = row.createCell(j++);
			cell.setCellStyle(styles.get("cell_number"));
			cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			
			if(usedMemValues[i] == -1)
				cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
			else
				cell.setCellValue(usedMemValues[i]);
			
			//cell.setCellValue(logData.get(i).getFreeMemory());
		}
		
		cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
		cell.setCellStyle(styles.get("cell_number"));
		long usedMemChange = utils.calculateDeltaForGivenSet(usedMemValues);
			
		cell.setCellValue(usedMemChange);
		
		/*cell = row.createCell(logData.size()+1);
		cell.setCellStyle(styles.get("cell_number"));
		
		long firstCycleValue = logData.get(0).getFreeMemory();
		long lastCycleValue = logData.get(logData.size()-1).getFreeMemory();
		
		if(firstCycleValue!= -1 &&  lastCycleValue!= -1)
			cell.setCellValue(lastCycleValue - firstCycleValue);
		else{
			//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
			cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
		}*/
		
		rowNo++;
		
		row = sheet.createRow(rowNo);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("cell_normal"));
		cell.setCellValue(new HSSFRichTextString("RAM (Size)"));
		
		long [] totalMemValues = new long[logData.getNumberOfCycles()];
		
		j=1;
		for(int i=0; i<logData.getNumberOfCycles(); i++)
		{
			long totalMem = systemData.get(i).getTotalMemory();
			
			if(totalMem == -1){
				totalMemValues[i] = -1;
 			}
			else{
				totalMemValues[i] = totalMem;
			}
			//cell.setCellValue(logData.get(i).getFreeMemory());
			
			if(constants.contains(i+1))
				continue;
			
			cell = row.createCell(j++);
			cell.setCellStyle(styles.get("cell_number"));
			cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			
			if(totalMemValues[i] == -1)
				cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
			else
				cell.setCellValue(totalMemValues[i]);
		}
		
		cell = row.createCell(logData.getNumberOfCycles() + 1 -constants.size());
		cell.setCellStyle(styles.get("cell_number"));
		long totalMemChange = utils.calculateDeltaForGivenSet(totalMemValues);
		
		cell.setCellValue(totalMemChange);
		
		Collections.sort(diskNames);
		
		for(String name:diskNames)
		{
			rowNo++;
			
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(name + " (Used)"));
			
			ArrayList<DiskOverview> values = utils.getUsedMemoryAndSizesForDisk(name, logData);
			long [] usedSizes = new long[values.size()];
			
			j=1;
			for(int i=0; i<values.size(); i++)
			{
				if(constants.contains(i+1))
					continue;
				
				cell = row.createCell(j++);
				cell.setCellStyle(styles.get("cell_number"));
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				
				long usedSize = values.get(i).getUsedSize();
				
				if(usedSize == -1){
					//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
					cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
				}
				else
					cell.setCellValue(usedSize);
				
				usedSizes[i] = usedSize;
				
			}
			cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			long deltaValue = utils.calculateDeltaForGivenSet(usedSizes);
			
			cell.setCellValue(deltaValue);
			rowNo++;
			
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(name + " (Size)"));
			
			long [] sizeValues = new long[values.size()];
			
			j=1;
			for(int i=0; i<values.size(); i++)
			{
				if(constants.contains(i+1))
					continue;
				
				cell = row.createCell(j++);
				cell.setCellStyle(styles.get("cell_number"));
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				
				long size = values.get(i).getSize();
				
				if(size == -1){
					//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
					cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
				}
				else
					cell.setCellValue(size);
				
				sizeValues[i] = size;
			}
			
			cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			long sizeDelta = utils.calculateDeltaForGivenSet(sizeValues);
			
			cell.setCellValue(sizeDelta);
			
		}
		
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the amount of used RAM and disk memories of all drives in bytes, for each cycle in seconds"));
	}
	
	private void createHeapSizeTab()
	{
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(4, "Total Heap Size");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Total Heap Size"));
		
		description = sheet.createRow(1);
						
		//creates an empty row.
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header1"));
		cell.setCellValue(new HSSFRichTextString("Threads"));
		addCycleIntervals(row);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		int rowNo = 4;
		
		for(String heap:heapThreads)
		{
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(heap));

			ArrayList<ThreadData> heapValues = heapData.get(heap.toLowerCase());
			
			if(heapValues != null)
			{
				int j=1;
				for(int i=0; i<heapValues.size(); i++)
				{
					if(constants.contains(i+1))
					{
						if(heap.equalsIgnoreCase("!SensorServer[1020507e]0001::OrientationThread"))
							DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Skipping data from Cycle.." + i);
						continue;
					}
					
					cell = row.createCell(j++);
					cell.setCellStyle(styles.get("cell_number"));
					ThreadData thData = heapValues.get(i);
				
					if(thData.getStatus() == 0){
						//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
						cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
					}
					else
						cell.setCellValue(thData.getHeapChunkSize());
				}
			}
			
			cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			
			ThreadData delta = deltaData.get(heap.toLowerCase());
			
			//DbgUtility.println(DbgUtility.PRIORITY_OPERATION, "Delta for the thread " + heap + " is " + delta);
			
			if(delta != null){
				long heapSizeDelta = delta.getHeapChunkSize();
				cell.setCellValue(heapSizeDelta);
			}
			else
				cell.setCellValue(0);
			
			rowNo++;
		}
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the total heap size for each thread in bytes, for each cycle in seconds"));
	}
	
	private void createHeapAllocSpaceTab()
	{
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(5, "Total heap alloc space");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Total Heap Allocated Space"));
		
		description = sheet.createRow(1);
						
		//creates an empty row.
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header1"));
		cell.setCellValue(new HSSFRichTextString("Threads"));
		addCycleIntervals(row);
				
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		int rowNo = 4;
		
		for(String heap:heapThreads)
		{
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(heap));
			
			ArrayList<ThreadData> heapValues = heapData.get(heap.toLowerCase());
			
			if(heapValues != null)
			{
				int j=1;
				for(int i=0; i<heapValues.size(); i++)
				{
					if(constants.contains(i+1))
						continue;
					
					cell = row.createCell(j++);
					cell.setCellStyle(styles.get("cell_number"));
					ThreadData thData = heapValues.get(i);
				
					if(thData.getStatus() == 0){
						//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
						cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
					}
					else
						cell.setCellValue(thData.getHeapAllocatedSpace());
				
				}
			}
			cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			
			if(deltaData.get(heap.toLowerCase()) != null){
				long heapAllocSpaceDelta = deltaData.get(heap.toLowerCase()).getHeapAllocatedSpace();
				cell.setCellValue(heapAllocSpaceDelta);
			}
			else
				cell.setCellValue(0);
			
			rowNo++;
		}
				
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
		
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the total heap allocated space for each thread in bytes, for each cycle in seconds"));
	}
	
	private void createHeapFreeSpaceTab()
	{
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(6, "Total heap free space");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Total Heap Free Space"));
	
		description = sheet.createRow(1);
						
		//creates an empty row.
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header1"));
		cell.setCellValue(new HSSFRichTextString("Threads"));
		addCycleIntervals(row);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		int rowNo = 4;
		
		for(String heap:heapThreads)
		{
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(heap));
			
			ArrayList<ThreadData> heapValues = heapData.get(heap.toLowerCase());
			
			if(heapValues != null)
			{
				int j=1;
				for(int i=0; i<heapValues.size(); i++)
				{
					if(constants.contains(i+1))
						continue;
					
					cell = row.createCell(j++);
					cell.setCellStyle(styles.get("cell_number"));
					ThreadData thData = heapValues.get(i);
				
					if(thData.getStatus() == 0){
						//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
						cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
					}
					else
						cell.setCellValue(thData.getHeapFreeSpace());
				}
			}
			
			cell = row.createCell(logData.getNumberOfCycles() + 1 -constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			
			if(deltaData.get(heap.toLowerCase()) != null){
				long heapFreeeDelta = deltaData.get(heap.toLowerCase()).getHeapFreeSpace();
				cell.setCellValue(heapFreeeDelta);
			}
			else
				cell.setCellValue(0);
			
			rowNo++;
		}
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
		
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the total heap free space for each thread in bytes, for each cycle in seconds"));
	}
	
	private void createAllocatedCellsTab()
	{
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(7, "Allocated heap cell count");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Allocated heap cell count"));
				
		description = sheet.createRow(1);
						
		//creates an empty row.
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header1"));
		cell.setCellValue(new HSSFRichTextString("Threads"));
		addCycleIntervals(row);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		int rowNo = 4;
		
		for(String heap:heapThreads)
		{
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(heap));
			
			ArrayList<ThreadData> heapValues = heapData.get(heap.toLowerCase());
			
			if(heapValues != null)
			{
				int j=1;
				for(int i=0; i<heapValues.size(); i++)
				{
					if(constants.contains(i+1))
						continue;
					
					cell = row.createCell(j++);
					cell.setCellStyle(styles.get("cell_number"));
					ThreadData thData = heapValues.get(i);
				
					if(thData.getStatus() == 0){
						//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
						cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
					}
					else
						cell.setCellValue(thData.getAllocatedCells());
				}
			}
			cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			
			if(deltaData.get(heap.toLowerCase()) != null){
				long allocCellsDelta = deltaData.get(heap.toLowerCase()).getAllocatedCells();
				cell.setCellValue(allocCellsDelta);
			}
			else
				cell.setCellValue(0);
			
			rowNo++;
		}
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
		
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the allocated heap cell count for each thread in bytes, for each cycle in seconds"));
	}
	
	private void createFreeCellsTab()
	{
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(8, "Free heap cell count");
		
		row = sheet.createRow(0);
	
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Free heap cell count"));
		
		description = sheet.createRow(1);
						
		//creates an empty row.
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header1"));
		cell.setCellValue(new HSSFRichTextString("Threads"));
		addCycleIntervals(row);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		int rowNo = 4;
		
		for(String heap:heapThreads)
		{
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(heap));
			
			ArrayList<ThreadData> heapValues = heapData.get(heap.toLowerCase());
			
			if(heapValues != null)
			{
				int j=1;
				for(int i=0; i<heapValues.size(); i++)
				{
					if(constants.contains(i+1))
						continue;
					
					cell = row.createCell(j++);
					cell.setCellStyle(styles.get("cell_number"));
					ThreadData thData = heapValues.get(i);
				
					if(thData.getStatus() == 0){
						//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
						cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
					}
					else
						cell.setCellValue(thData.getFreeCells());
				}
			}
			
			cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			
			if(deltaData.get(heap.toLowerCase()) != null){
				long heapFreeCellsDelta = deltaData.get(heap.toLowerCase()).getFreeCells();
				cell.setCellValue(heapFreeCellsDelta);
			}
			else
				cell.setCellValue(0);
			
			rowNo++;
		}
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
		
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the free heap cell count for each thread in bytes, for each cycle in seconds"));
	}
	
	private void createFreeSlackTab()
	{
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(9, "Free Slack");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Free slack"));
		
		description = sheet.createRow(1);
						
		//creates an empty row.
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header1"));
		cell.setCellValue(new HSSFRichTextString("Threads"));
		addCycleIntervals(row);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		int rowNo = 4;
		
		for(String heap:heapThreads)
		{
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(heap));
			
			ArrayList<ThreadData> heapValues = heapData.get(heap.toLowerCase());
			
			if(heapValues != null)
			{
				int j=1;
				for(int i=0; i<heapValues.size(); i++)
				{
					if(constants.contains(i+1))
						continue;
					
					cell = row.createCell(j++);
					cell.setCellStyle(styles.get("cell_number"));
					ThreadData thData = heapValues.get(i);
				
					if(thData.getStatus() == 0){
						//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
						cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
					}
					else
						cell.setCellValue(thData.getFreeSlackSize());
				}
			}
			cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			
			if(deltaData.get(heap.toLowerCase()) != null){
				long heapSlackDelta = deltaData.get(heap.toLowerCase()).getFreeSlackSize();
				cell.setCellValue(heapSlackDelta);
			}
			else
				cell.setCellValue(0);
			
			rowNo++;
		}
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
		
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the free slack size for each thread in bytes, for each cycle in seconds"));
	}
	
	private void createLargestAllocSizeTab()
	{
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(10, "Largest allocated cell size in heap");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Largest allocated cell size in heap"));
		
		sheet.autoSizeColumn((short)0);
		
		description = sheet.createRow(1);
						
		//creates an empty row.
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header1"));
		cell.setCellValue(new HSSFRichTextString("Threads"));
		addCycleIntervals(row);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		int rowNo = 4;
		
		for(String heap:heapThreads)
		{
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(heap));
			
			ArrayList<ThreadData> heapValues = heapData.get(heap.toLowerCase());
			
			if(heapValues != null)
			{
				int j=1;
				for(int i=0; i<heapValues.size(); i++)
				{
					if(constants.contains(i+1))
						continue;
					
					cell = row.createCell(j++);
					cell.setCellStyle(styles.get("cell_number"));
					ThreadData thData = heapValues.get(i);
				
					if(thData.getStatus() == 0){
						//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
						cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
					}
					else
						cell.setCellValue(thData.getLargestAllocCellSize());
				}
			}
			cell = row.createCell(logData.getNumberOfCycles() + 1 -constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			
			if(deltaData.get(heap.toLowerCase()) != null){
				long largetAllocCellDelta = deltaData.get(heap.toLowerCase()).getLargestAllocCellSize();
				cell.setCellValue(largetAllocCellDelta);
			}
			else
				cell.setCellValue(0);
			
			rowNo++;
		}
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
				
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the largest allocated cell size in heap for each thread in bytes, for each cycle in seconds"));
	}
	
	private void createLargestFreeSizeTab()
	{
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(11, "Largest free cell size in heap");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Largest free cell size in heap"));
		
		description = sheet.createRow(1);
						
		//creates an empty row.
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header1"));
		cell.setCellValue(new HSSFRichTextString("Threads"));
		addCycleIntervals(row);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		int rowNo = 4;
		
		for(String heap:heapThreads)
		{
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(heap));
			
			ArrayList<ThreadData> heapValues = heapData.get(heap.toLowerCase());
			
			if(heapValues != null)
			{
				int j=1;
				for(int i=0; i<heapValues.size(); i++)
				{
					if(constants.contains(i+1))
						continue;
					
					cell = row.createCell(j++);
					cell.setCellStyle(styles.get("cell_number"));
					ThreadData thData = heapValues.get(i);
				
					if(thData.getStatus() == 0){
						//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
						cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
					}
					else
						cell.setCellValue(thData.getLargestFreeCellSize());
				}
			}
			
			cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			
			if(deltaData.get(heap.toLowerCase()) != null){
				long largestFreeCellDelta = deltaData.get(heap.toLowerCase()).getLargestFreeCellSize();
				cell.setCellValue(largestFreeCellDelta);
			}
			else
				cell.setCellValue(0);
			
			rowNo++;
		}
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
		
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the largest free cell size in heap for each thread in bytes, for each cycle in seconds"));
	}

	/**
	 * This method creates the sheet for Global Data.
	 * 
	 *
	 */
	private void createGlobalChunksSheet()
	{
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(2, "Global Data");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Global Data"));
		
		description = sheet.createRow(1);
						
		//creates an empty row.
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header1"));
		cell.setCellValue(new HSSFRichTextString("Chunk Names"));
		addCycleIntervals(row);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		int rowNo = 4;
		
		for(String heap:glodChunks)
		{
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(heap));
			
			ArrayList<GlobalDataChunks> heapValues = glodData.get(heap);
			
			int j=1;
			for(int i=0; i<heapValues.size(); i++)
			{
				if(constants.contains(i+1))
					continue;
				
				cell = row.createCell(j++);
				cell.setCellStyle(styles.get("cell_number"));
				GlobalDataChunks thData = heapValues.get(i);
				
				if(thData.getAttrib() == 0){
					//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
					cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
				}
				else
					cell.setCellValue(thData.getSize());
			}
			
			cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			
			if(glodDeltaData.get(heap) != null){
				long largestFreeCellDelta = Long.parseLong(glodDeltaData.get(heap));
				cell.setCellValue(largestFreeCellDelta);
			}
			rowNo++;
		}
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
		
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the chunk sizes in bytes that caontains global data, for each cycle in seconds"));

	}
	
	private void createNonHeapChunkSheet()
	{
		//create a new sheet
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(3, "Non-heap chunks");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Non-Heap Chunk Size"));
		
		description = sheet.createRow(1);
		
		//creates an empty row
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Chunk Names"));
		addCycleIntervals(row);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Delta"));
		
		int rowNo = 4;

		for(String heap:nonHeapChunks)
		{
			row = sheet.createRow(rowNo);
			cell = row.createCell(0);
			cell.setCellStyle(styles.get("cell_normal"));
			cell.setCellValue(new HSSFRichTextString(heap));
			
			ArrayList<ChunksData> chunkValues = chunkData.get(heap);

			int j=1;
			for(int i=0; i<chunkValues.size(); i++)
			{
				if(constants.contains(i+1))
					continue;
				
				cell = row.createCell(j++);
				cell.setCellStyle(styles.get("cell_number"));
				ChunksData chData = chunkValues.get(i);
				
				if(chData.getAttrib() == 0){
					//cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
					cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
				}
				else
					cell.setCellValue(chData.getSize());
			}
			
			cell = row.createCell(logData.getNumberOfCycles()+1-constants.size());
			cell.setCellStyle(styles.get("cell_number"));
			
			if(chunkDeltaData.get(heap) != null){
				long largestFreeCellDelta = Long.parseLong(chunkDeltaData.get(heap));
				cell.setCellValue(largestFreeCellDelta);
			}
			rowNo++;
		}
		
		for(int i=0; i <= logData.getNumberOfCycles(); i++)
		{
			sheet.autoSizeColumn((short)i);
		}
		
		cell = description.createCell(0);
		cell.setCellValue(new HSSFRichTextString("Specifies the non heap chunk sizes in bytes, for each cycle in seconds"));
	}
	
	/**
	 * This method creates sheet with Window Group events.
	 *
	 */
	private void createWindowGroupSheet()
	{
	    //create a new sheet
		HSSFSheet sheet = wb.createSheet();
		// declare a row object reference
		HSSFRow row = null;
		HSSFRow description = null;
		// declare a cell object reference
		HSSFCell cell = null;
		
		//set the sheet name in Unicode
		wb.setSheetName(12, "Window Groups");
		
		row = sheet.createRow(0);
		//r.setHeight((short)500);
		
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(new HSSFRichTextString("Window Group Events"));
		
		description = sheet.createRow(1);
		
		//creates an empty row
		row = sheet.createRow(2);
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(new HSSFRichTextString("Window Group Names"));
		addCycleIntervals(row);
	
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
		ArrayList<String> wndg_names = utils.getWindowGroupNames(logData);
		
		int rowNo = 4;
		
		if(wndg_names == null)
			return;
		else
		{
			for(String name:wndg_names)
			{
				row = sheet.createRow(rowNo++);
				cell = row.createCell(0);
				cell.setCellStyle(styles.get("cell_normal"));
				cell.setCellValue(new HSSFRichTextString(name));
					
				ArrayList<WindowGroupEventData> events_list = utils.getAllWindowGroupEvents(name, logData);
				
				int j = 1;
				
				for(int i=0; i<events_list.size(); i++)
				{
					if(constants.contains(i+1))
						continue;
					
					cell = row.createCell(j++);
					cell.setCellStyle(styles.get("cell_normal"));
						
					WindowGroupEventData eventSet = events_list.get(i);
						
					if(eventSet == null){
						cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
					}
					else
					{
						StringBuffer tmp = new StringBuffer();
						
						if(eventSet.getEvent_0_count() > 0)
							tmp.append(" NoEvent(" + eventSet.getEvent_0_count() + ")");
						if(eventSet.getEvent_1_count() > 0)
							tmp.append(" NameChanged(" + eventSet.getEvent_1_count() + ")");
						if(eventSet.getEvent_2_count() > 0)
							tmp.append(" FocusGained(" + eventSet.getEvent_2_count() + ")");
						if(eventSet.getEvent_3_count() > 0)
							tmp.append(" FocusLost(" + eventSet.getEvent_3_count() + ")");
						
						cell.setCellValue(new HSSFRichTextString(tmp.toString()));
					}
					
				}
			}
			
			for(int i=0; i <= logData.getNumberOfCycles(); i++)
				sheet.autoSizeColumn((short)i);
			
			cell = description.createCell(0);
			cell.setCellValue(new HSSFRichTextString("Specifies the type and number of events for each window group, in each cycle."));
		}
	}
	/**
     * create set of cell styles
     */
    private Map<String, HSSFCellStyle> createStyles(HSSFWorkbook wb){
        Map<String, HSSFCellStyle> styles = new HashMap<String, HSSFCellStyle>();
       
        HSSFCellStyle style;
        HSSFFont headerFont = wb.createFont();
        headerFont.setColor(HSSFColor.WHITE.index);
		headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        headerFont.setFontHeightInPoints((short)18);
        style = createBorderedStyle(wb);
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        style.setFillForegroundColor(HSSFColor.BLUE.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
         style.setFont(headerFont);
        styles.put("header", style);

        HSSFFont font1 = wb.createFont();
        font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font1.setFontHeightInPoints((short)12);
        font1.setFontName(HSSFFont.FONT_ARIAL);
        font1.setColor(HSSFColor.WHITE.index);
        style = createBorderedStyle(wb);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(HSSFColor.BLUE.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFont(font1);
        style.setWrapText(true);
        styles.put("header2", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        style.setFillForegroundColor(HSSFColor.BLUE.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFont(font1);
        style.setWrapText(true);
        styles.put("header1", style);
               
        HSSFFont font3 = wb.createFont();
        font3.setColor(HSSFColor.BLACK.index);
        font3.setFontHeightInPoints((short)10);
        font3.setFontName(HSSFFont.FONT_ARIAL);
        font3.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style = createBorderedStyle(wb);
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        style.setFont(font3);
        styles.put("cell_bold", style);

        HSSFFont font5 = wb.createFont();
        font5.setColor(HSSFColor.BLACK.index);
        font5.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        style = createBorderedStyle(wb);
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        style.setFont(font5);
        styles.put("cell_normal", style);
        
        HSSFFont font4 = wb.createFont();
        font4.setFontHeightInPoints((short)10);
        font4.setColor(HSSFColor.WHITE.index);
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(HSSFColor.BLUE.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFont(font4);
        styles.put("cell_blue_font_white", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        styles.put("cell_number", style);
        
        HSSFFont blue_font = wb.createFont();
        blue_font.setFontHeightInPoints((short)10);
        blue_font.setColor(HSSFColor.BLUE.index);
        style = createBorderedStyle(wb);
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        style.setFont(blue_font);
        styles.put("blue_font", style);
        
        return styles;
    }

    private HSSFCellStyle createBorderedStyle(HSSFWorkbook wb){
    	HSSFCellStyle style = wb.createCellStyle();
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        return style;
    }
    
    /**
     * This method adds cells with values of time intervals between 
     * consecutive cycles to given row.
     * @param row 
     */
    private void addCycleIntervals(HSSFRow row)
    {
    	HSSFCell cell = null;
    			
		SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
				
		cell = row.createCell(1);
		cell.setCellStyle(styles.get("header2"));
		cell.setCellValue(0);
		
		if(logData == null)
			return;
		
		long prevDuration = 0;
		
		int i;
		String currentTime = "";
		String prevTime = "";
		int j=1;
		int filesSkipped = 1;
		
		CycleData [] cycles = logData.getLogData();
		
		for(i=1; i<logData.getNumberOfCycles();i++)
		{
			if(constants.contains(i+1))
			{
				filesSkipped++ ;
				continue;
			}
			j++;
			currentTime = cycles[i].getTime();
			//if(constants.contains(i-1))
			//	prevTime = logData.get(i-2).getTime();
			//else
			//	prevTime = logData.get(i-1).getTime();
			prevTime = cycles[i-filesSkipped].getTime();
			
			long timeDiff = utils.getDurationInSeconds(prevTime, currentTime);
			cell = row.createCell(j);
			cell.setCellStyle(styles.get("header2"));
			cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			
			if(timeDiff < 0)
			{
				cell.setCellValue(new HSSFRichTextString("Cycle " + (i+1)));
				return;
			}
			else
			{
				timeDiff += prevDuration;
				prevDuration = timeDiff;
				
				cell.setCellValue(timeDiff);
			}
			filesSkipped = 1;
		}
	
    }
    
    private void createOverviewFields(HSSFRow row)
    {
    	HSSFCell cell = row.createCell(0);
    	cell.setCellStyle(styles.get("header1"));
    	cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("Threads"));
		
		cell = row.createCell(1);
		cell.setCellStyle(styles.get("header1"));
		cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("Status"));
		
		cell = row.createCell(2);
		cell.setCellStyle(styles.get("header1"));
		cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("Max Heap Size"));
		
		cell = row.createCell(3);
		cell.setCellStyle(styles.get("header1"));
		cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("Change in Heap \n size (in Bytes)"));
		
		cell = row.createCell(4);
		cell.setCellStyle(styles.get("header1"));
		cell.setCellValue(new HSSFRichTextString("Change in  Heap Allocated  \n space (in Bytes) "));
		
		cell = row.createCell(5);
		cell.setCellStyle(styles.get("header1"));
		cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("Change in Heap \n Free space (in Bytes)"));
		
		cell = row.createCell(6);
		cell.setCellStyle(styles.get("header1"));
		cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("Change in Allocated \n Cell Count"));
		
		cell = row.createCell(7);
		cell.setCellStyle(styles.get("header1"));
		cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("Change in Free \n Cell Count"));
			
		cell = row.createCell(8);
		cell.setCellStyle(styles.get("header1"));
		cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("Change in Slack \n space size (in Bytes) "));
		
		cell = row.createCell(9);
		cell.setCellStyle(styles.get("header1"));
		cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("Stack size"));
		
		cell = row.createCell(10);
		cell.setCellStyle(styles.get("header1"));
		cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("No. of Files \n opened"));
		
		cell = row.createCell(11);
		cell.setCellStyle(styles.get("header1"));
		cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cell.setCellValue(new HSSFRichTextString("No. of \n P&S Handles"));
		
	}
    
    private void createDataInOverView(HSSFSheet sheet, int rowNo)
    {
    	SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
    	
    	ArrayList<String> allThreads = utils.getAllThreadNames(logData);
    	Collections.sort(allThreads);
    	
    	long [] maxHeapSizes = new long[allThreads.size()];
    	long [] heapSizes = new long[allThreads.size()];
    	long [] freeCells = new long[allThreads.size()];
    	long [] allocCells = new long[allThreads.size()];
    	long [] freeSpace = new long[allThreads.size()];
    	long [] allocSpace = new long[allThreads.size()];
    	long [] slackSpace = new long[allThreads.size()];
    	long [] totalFiles = new long[allThreads.size()];
    	long [] totalHandles = new long[allThreads.size()];
    	long [] totalStacks = new long[allThreads.size()];
    	
    	int i =0;
    	
    	for(String thName:allThreads)
    	{
    		if(thName.startsWith("MemSpy") && thName.endsWith("::MemSpy"))
    			continue;
    		
    		HSSFRow row = sheet.createRow(rowNo);
    		HSSFCell cell = row.createCell(0);
    		    		    		
    		cell.setCellStyle(styles.get("cell_normal"));
    		cell.setCellValue(new HSSFRichTextString(thName));
    		
    		cell = row.createCell(1);
			cell.setCellStyle(styles.get("cell_normal"));
    		
			if(logData.getNumberOfCycles() == 1)
    		{
    			cell.setCellValue(new HSSFRichTextString("Alive"));
    		}
    		else
    		{
    			int status = utils.getThreadStatusFromAllCycles(thName, logData);
    			    		
    			if(status == 0)
    				cell.setCellValue(new HSSFRichTextString("Dead"));
    			else if(status == 1 || status == 2)
    				cell.setCellValue(new HSSFRichTextString("Alive"));
    			else
    				cell.setCellValue(new HSSFRichTextString(NOT_AVAILABLE));
    		}
    		
    		ThreadData threadDelta = deltaData.get(thName.toLowerCase());
    		
    		cell = row.createCell(2);
    		cell.setCellStyle(styles.get("cell_number"));
    		cell.setCellValue(threadDelta.getMaxHeapSize());
    		
    		maxHeapSizes[i] = threadDelta.getMaxHeapSize();
    		/*ArrayList<String> heapThreads = utils.getAllHeapThreads(logData);
    		long delta = 0;
    		long lastValue = 0;
    		
    		ArrayList<ThreadData> heapData = utils.getHeapDataFromAllCycles(thName, logData);
    		
    		if(utils.getHeapStatusFromAllCycles(thName, logData) == 0)
    		{
    			//Display zeros for all heap fields
    			maxHeapSize = 0;
    			delta = 0;
    		}
    		else{
    			lastValue = heapData.get(logData.size()-1).getHeapChunkSize();
        		maxHeapSize = heapData.get(logData.size()-1).getMaxHeapSize();
    		    			
        		long firstValue = 0;
    		  		
        		for(int i=heapData.size()-2; i>=0; i--)
        		{
        			ThreadData data = heapData.get(i);
        			if(data.getStatus() != CycleData.Deleted){
        				firstValue = data.getHeapChunkSize();
        			}
        			else
        				break;
        		}
    			
        		if(firstValue != -1)
        			delta = lastValue - firstValue;
    		} */
    		
    		//cell.setCellValue(maxHeapSize);
    		
    		cell = row.createCell(3);
    		cell.setCellStyle(styles.get("cell_number"));
    		cell.setCellValue(threadDelta.getHeapChunkSize());
    		heapSizes[i] = threadDelta.getHeapChunkSize();
    		
    		cell = row.createCell(4);
    		cell.setCellStyle(styles.get("cell_number"));
    		cell.setCellValue(threadDelta.getHeapAllocatedSpace());
    		allocSpace[i] = threadDelta.getHeapAllocatedSpace();
    		
    		cell = row.createCell(5);
    		cell.setCellStyle(styles.get("cell_number"));
    		cell.setCellValue(threadDelta.getHeapFreeSpace());
    		freeSpace[i] = threadDelta.getHeapFreeSpace();
    		
    		cell = row.createCell(6);
    		cell.setCellStyle(styles.get("cell_number"));
    		cell.setCellValue(threadDelta.getAllocatedCells());
    		allocCells[i] = threadDelta.getAllocatedCells();
    		
    		cell = row.createCell(7);
    		cell.setCellStyle(styles.get("cell_number"));
    		cell.setCellValue(threadDelta.getFreeCells());
    		freeCells[i] = threadDelta.getFreeCells();
    		
    		cell = row.createCell(8);
    		cell.setCellStyle(styles.get("cell_number"));
    		cell.setCellValue(threadDelta.getFreeSlackSize());
    		slackSpace[i] = threadDelta.getFreeSlackSize();
    		
    		cell = row.createCell(9);
    		cell.setCellStyle(styles.get("cell_number"));
    		cell.setCellValue(threadDelta.getStackSize());
    		totalStacks[i] = threadDelta.getStackSize();
    		
    		cell = row.createCell(10);
    		cell.setCellStyle(styles.get("cell_number"));
    		cell.setCellValue(threadDelta.getOpenFiles());
    		totalFiles[i] = threadDelta.getOpenFiles();
    		
    		cell = row.createCell(11);
    		cell.setCellStyle(styles.get("cell_number"));
    		cell.setCellValue(threadDelta.getPsHandles());
    		totalHandles[i] = threadDelta.getPsHandles();
    		
    		i++;
    		
    		rowNo++;
    	}
    	
    	if(allThreads.size() > 0)
    	{
    	
    		HSSFRow totalRow = sheet.createRow(rowNo);
    	
    		long totalMaxHeapSize = utils.calculateAndGetTotal(maxHeapSizes);
    		long totalStackSize = utils.calculateAndGetTotal(totalStacks);
    		totalHeapSizeChange = utils.calculateAndGetTotal(heapSizes);
    		totalFreeCellChange = utils.calculateAndGetTotal(freeCells);
    		totalAllocCellChange = utils.calculateAndGetTotal(allocCells);
    		totalFreeSpaceChange = utils.calculateAndGetTotal(freeSpace);
    		totalAllocSpaceChange = utils.calculateAndGetTotal(allocSpace);
    		totalSlackSpaceChange = utils.calculateAndGetTotal(slackSpace);
    		totalFilesChange = utils.calculateAndGetTotal(totalFiles);
    		totalHandlesChange = utils.calculateAndGetTotal(totalHandles);
    	
    		HSSFCell cell = totalRow.createCell(2);
    		cell.setCellStyle(styles.get("cell_blue_font_white"));
    		cell.setCellValue(totalMaxHeapSize);
    	
    		cell = totalRow.createCell(3);
    		cell.setCellStyle(styles.get("cell_blue_font_white"));
    		cell.setCellValue(totalHeapSizeChange);
    	
    		cell = totalRow.createCell(4);
    		cell.setCellStyle(styles.get("cell_blue_font_white"));
    		cell.setCellValue(totalAllocSpaceChange);
    	
    		cell = totalRow.createCell(5);
    		cell.setCellStyle(styles.get("cell_blue_font_white"));
    		cell.setCellValue(totalFreeSpaceChange);
    	
    		cell = totalRow.createCell(6);
    		cell.setCellStyle(styles.get("cell_blue_font_white"));
    		cell.setCellValue(totalAllocCellChange);
    	
    		cell = totalRow.createCell(7);
    		cell.setCellStyle(styles.get("cell_blue_font_white"));
    		cell.setCellValue(totalFreeCellChange);
    	
    		cell = totalRow.createCell(8);
    		cell.setCellStyle(styles.get("cell_blue_font_white"));
    		cell.setCellValue(totalSlackSpaceChange);
    	
    		cell = totalRow.createCell(9);
    		cell.setCellStyle(styles.get("cell_blue_font_white"));
    		cell.setCellValue(totalStackSize);
    	
    		cell = totalRow.createCell(10);
    		cell.setCellStyle(styles.get("cell_blue_font_white"));
    		cell.setCellValue(totalFilesChange);
    	
    		cell = totalRow.createCell(11);
    		cell.setCellStyle(styles.get("cell_blue_font_white"));
    		cell.setCellValue(totalHandlesChange);
    	}
    	
    }
    private void readHeapDataFromAllCycles(Map<String, ArrayList<ThreadData>> map, ParsedData logData)
    {
    	SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
    	//heapThreads = utils.getAllHeapThreads(logData);
    	  
    	ArrayList<String> allThreads = utils.getAllThreadNames(logData);   	
    	for(String heap:allThreads)
		{
    		//ignoring the data related to MemSpy threads
    		if(heap.startsWith("MemSpy") && heap.endsWith("::MemSpy"))
    			continue;
    		
			ArrayList<ThreadData> heapData = utils.getHeapDataFromAllCycles(heap, logData);
			
			map.put(heap.toLowerCase(), heapData);
		}
    }
    
    private void readGlobalDataFromAllCycles(Map<String, ArrayList<GlobalDataChunks>> map, ParsedData logData)
    {
    	SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
    	glodChunks = utils.getAllGlobalChunkNames(logData);
    	   
    	Collections.sort(glodChunks);
    	for(String glod:glodChunks)
		{
			ArrayList<GlobalDataChunks> globData = utils.getGLOBDataFromAllCycles(glod, logData);
			map.put(glod, globData);
		}
    }
    
    private void readChunkDataFromAllCycles(Map<String, ArrayList<ChunksData>> map, ParsedData logData)
    {
    	SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
    	nonHeapChunks = utils.getAllNonHeapChunkNames(logData);
    	   
    	Collections.sort(nonHeapChunks);
    	for(String chunk:nonHeapChunks)
		{
			ArrayList<ChunksData> globData = utils.getChunkDataFromAllCycles(chunk, logData);
			map.put(chunk, globData);
		}
    }
    
    private void getFieldDifferencesForAllThreads(Map<String, ThreadData> map, ParsedData logData)
    {	
    	SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
    	
    	ArrayList<String> allThreads = utils.getAllThreadNames(logData);
    	
    	for(String s:allThreads)
    	{
    		//ignoring the data related to MemSpy threads
    		if(s.startsWith("MemSpy") && s.endsWith("::MemSpy"))
    			continue;
    		
    		ThreadData delta = utils.getChangeinHeapData(s, heapData.get(s.toLowerCase()), logData);
    		
    		map.put(s.toLowerCase(), delta);
    	}
    }
    
    private void getFieldDifferencesForAllGlodChunks(Map<String, String> map, ParsedData logData)
    {	
    	SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
    	
    	ArrayList<String> allGlodChunks = utils.getAllGlobalChunkNames(logData);
    	
    	for(String s:allGlodChunks)
    	{
    		long delta = utils.getChangeinGlodChunksData(s, logData);
    		
    		map.put(s, delta+"");
    	}
    }
    
    private void getFieldDifferencesForAllChunks(Map<String, String> map, ParsedData logData)
    {	
    	SWMTLogReaderUtils utils = new SWMTLogReaderUtils();
    	
    	ArrayList<String> allChunks = utils.getAllNonHeapChunkNames(logData);
    	
    	for(String s:allChunks)
    	{
    		long delta = utils.getChangeinChunksData(s, logData);
    		
    		map.put(s, delta+"");
    	}
    }
}
