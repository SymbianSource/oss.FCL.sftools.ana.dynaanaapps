/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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

package com.nokia.carbide.cpp.pi.instr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


public class MapFile 
{
	private boolean parsedMapFile = false;
	
	private String name;
	private File mapFile;
	private String referencePath;
	private long referenceLineNumber;
	private LinkedList<Function> functionData;
	private ArrayList<Function> sortedFunctionData;
	private Long currentGccLibEndingOffset = new Long(0);
	private Function lastGccFunction = null;
	
	// RVCT/RVDS map file line
	private static final Pattern rvctLinePattern = Pattern.compile("\\p{Blank}*((?!\\d)\\S.+)\\p{Blank}+(0[x|X]\\p{XDigit}+|\\d+)\\p{Blank}+(?:ARM Code|Thumb Code)\\p{Blank}+(0x\\p{XDigit}+|\\d+)\\p{Blank}*.*");	//$NON-NLS-1$

	// a GCC map file line looks like this:
	// <%x|%d> <symbol name> for function symbols
	// symbol name cannot be number (e.g. first non space is not a digit)
	private static final Pattern gccFuncLinePattern  = Pattern.compile("\\p{Blank}*(0[x|X]\\p{XDigit}+|\\d+)\\p{Blank}+((?!\\d)\\S.+)");	//$NON-NLS-1$

	// <section> <%x|%d> <%x|%d> <symbol name>	for whole library
	// *fill* <%x|%d> <%x|%d> 00000000 for filler
	private static final Pattern gccLibOrFillerLinePattern = Pattern.compile("\\p{Blank}*(?:\\S*)\\p{Blank}*(0[x|X]\\p{XDigit}+|\\d+)\\p{Blank}+(0[x|X]\\p{XDigit}+|\\d+)\\p{Blank}+(\\S.+)"); //$NON-NLS-1$


	public MapFile(File file, String referencePath, long referenceLineNumber)
	{
		if (!file.exists())
		{
			  // .map not found
			  if(file.getName().endsWith(".exe.map") || //$NON-NLS-1$
					  file.getName().endsWith(".dll.map") || //$NON-NLS-1$
					  file.getName().endsWith(".ldd.map") || //$NON-NLS-1$
					  file.getName().endsWith(".pdd.map") || //$NON-NLS-1$
					  file.getName().endsWith(".app.map"))  //$NON-NLS-1$
			  {
				  flagFileNotFound(file, referencePath, referenceLineNumber);
			  }
		}
		//System.out.println("Creating MAP file "+file.getAbsolutePath());
		
		this.name = file.getName();
		this.functionData = new LinkedList<Function>();
		this.sortedFunctionData = new ArrayList<Function>();
		
		this.mapFile = file;
		this.referencePath = referencePath;
		this.referenceLineNumber = referenceLineNumber;
		
		/*
		if (file.getName().indexOf("wserv.exe") != -1)
		{
			this.printSortedFunctions();
			System.out.println("Map file "+file.getName()+" processed");
		}*/
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getFunctionNameForOffset(long offset)
	{
		if (!this.parsedMapFile) {
			this.parseMapFile();
			this.copyToSortedFunctionData();
		}
		
		for (Function f : sortedFunctionData)
		{
			// is the asked offset within the area of this function
			// test first is the offset equal or past the first
			// instruction of the function
			if (offset >= f.offsetFromBinaryStart)
			{
				// if so, make sure that the offset is less
				// or equal to the last instruction within 
				// this function
				if (offset < (f.offsetFromBinaryStart+f.length))
				{
					return f.functionName;
				}
			}
		}
		
		return Messages.getString("MapFile.functionWithOffsetNotFound1")+offset+Messages.getString("MapFile.functionWithOffsetNotFound2")+this.name+ //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("MapFile.functionWithOffsetNotFound3")+ //$NON-NLS-1$
				(this.sortedFunctionData.get(this.sortedFunctionData.size() - 1)).offsetFromBinaryStart; 		 //$NON-NLS-1$
	}
	
	public Function getFunctionForOffset(long offset)
	{
		if (!this.parsedMapFile) {
			this.parseMapFile();
			this.copyToSortedFunctionData();
		}

		for (Function f : sortedFunctionData)
		{
			// is the asked offset within the area of this function
			// test first is the offset equal or past the first
			// instruction of the function
			if (offset >= f.offsetFromBinaryStart)
			{
				// if so, make sure that the offset is less
				// or equal to the last instruction within 
				// this function
				if (offset < (f.offsetFromBinaryStart+f.length))
				{
					return f;
				}
			}
		}
		
		return null; 		
	}
	
	public long getFunctionLengthForOffset(long offset)
	{
		if (!this.parsedMapFile) {
			this.parseMapFile();
			this.copyToSortedFunctionData();
		}

		for (Function f : sortedFunctionData)
		{
			// is the asked offset within the area of this function
			// test first is the offset equal or past the first
			// instruction of the function
			if (offset >= f.offsetFromBinaryStart)
			{
				// if so, make sure that the offset is less
				// or equal to the last instruction within 
				// this function
				if (offset < (f.offsetFromBinaryStart+f.length))
				{
					return f.length;
				}
			}
		}
		return 0; 		
	}
	
	public long getOffsetFromBinaryStartForFunction(String functionName)
	{
		if (!this.parsedMapFile) {
			this.parseMapFile();
			this.copyToSortedFunctionData();
		}

		for (Function f : sortedFunctionData)
		{
			if (f.functionName.equals(functionName))
			{
				return f.offsetFromBinaryStart;
			}
		}
		return -1;
	}
	
	private Function FunctionFromTokens(String funcNameToken, String funcOffsetToken, String funcLengthToken)
	{	
		Function f = new Function(funcNameToken,new Long(0),null);
		// look for length, set it tentatively
		// we may adjust it later
		f.length = Long.decode(funcLengthToken);
		f.offsetFromBinaryStart = Long.decode(funcOffsetToken);

		return f;
	}
	
	private void parseMapFile()
	{
		boolean isRVCT = false;
		
		System.out.println(Messages.getString("MapFile.parsingMapFile") + this.name); //$NON-NLS-1$
		this.parsedMapFile = true;
		
		// read into the map and see if it's RVCT built
		FileInputStream fis;
		try {
			fis = new FileInputStream(mapFile);
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(fis));
			
			String line = bufReader.readLine();
			
			if (line != null && line.length() > 0) {
				if (line.toLowerCase().indexOf("arm linker,") != -1) //$NON-NLS-1$
					isRVCT = true;
				else
					isRVCT = false;
			} else {
				// empty file, do nothing
				return;
			}
			
			bufReader.close();
			fis.close();	
			
			fis = new FileInputStream(mapFile);		
			bufReader = new BufferedReader(new InputStreamReader(fis));

			if (isRVCT || mapFile.getAbsolutePath().toLowerCase().indexOf("armv5") != -1) //$NON-NLS-1$
			{
				parseRVCT(bufReader);
			}
			else
			{
				parseGCC(bufReader);
			}

			bufReader.close();
			fis.close();	
		} catch (FileNotFoundException e) {
			flagFileNotFound(mapFile, referencePath, referenceLineNumber);
		} catch (IOException e) {
			flagIOException(mapFile, referencePath, referenceLineNumber);
		}		
	}
	
	private void parseGCC(BufferedReader bufReader) throws IOException
	{
		String line = bufReader.readLine();

		while(line != null)
		{
			this.processLineGCC(line);
			line = bufReader.readLine();
		}
	}
	
	private void parseRVCT(BufferedReader bufReader) throws IOException
	{
		String line = bufReader.readLine();

		// find the global symbols section
		//while(line.indexOf("Global Symbols") == -1)
		// line = bufReader.readLine();		
		
		line = bufReader.readLine();
		
		while(line != null)
		{
			this.processLineRVCT(line);
			line = bufReader.readLine();
		}
		bufReader.close();
	}
	
	private void processLineRVCT(String line)
	{
		// a RVCT symbol line looks like this:
		// <symbol name> <%x|%d> <ARM Code|Thumb Code> <%x|%d> <object>
		// symbol name cannot be number (e.g. first non space is not a digit)
		Matcher rvctLineMatcher = rvctLinePattern.matcher(line);
		
		if (rvctLineMatcher.matches())
		{
			String funcNameToken = rvctLineMatcher.group(1).trim();
			// internal symbol, not a function
			if (funcNameToken.indexOf(Messages.getString("MapFile.dollarSign")) != -1) //$NON-NLS-1$
				return;
			
			String funcOffsetToken = rvctLineMatcher.group(2).trim();
			
			if (funcOffsetToken.equalsIgnoreCase("0x00000001") && line.contains("Thumb Code"))
				return;
			
			String funcLengthToken = rvctLineMatcher.group(3).trim();
			
			Function f = FunctionFromTokens(funcNameToken, funcOffsetToken, funcLengthToken);
			
			this.insertToFunctionData(f);
		}
	}
		
	// our current parser picked up too many trash, let's try our best here
	private boolean qualifyGCCSymbol(long address, String symbol) {
		if (symbol == null || symbol.length() <= 0)
			return false;
		
		// zero address on ARM, you must be kidding. This isn't a real program address
		if (address <= 0) {
			return false;
		}
		if (symbol.contains("(size before relaxing")) { //$NON-NLS-1$
			return false;
		}
		if (symbol.contains("PROVIDE (")) { //$NON-NLS-1$
			return false;
		}
		// you better be kidding if this is a C symbol, it's linker symbol
		if (symbol.charAt(0) == '.') {
			return false;
		}
		if (symbol.equals("_DYNAMIC")) { //$NON-NLS-1$
			return false;
		}
		if (symbol.contains("vtable ") || symbol.contains("typeinfo ")) { //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		if (symbol.contains("= .")) { //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	private void processLineGCC(String line)
	{	
		// a GCC symbol line looks like this:
		// <%x|%d> <symbol name> for function symbols
		// symbol name cannot be number (e.g. first non space is not a digit)
		Matcher gccFuncLineMatcher  = gccFuncLinePattern.matcher(line);	//$NON-NLS-1$
		// <section> <%x|%d> <%x|%d> <symbol name>	for whole library
		// *fill* <%x|%d> <%x|%d> 00000000 for filler
		Matcher gccLibOrFillerLineMatcher = gccLibOrFillerLinePattern.matcher(line); //$NON-NLS-1$
		
		Function f = null;
		Long currentLineOffset = currentGccLibEndingOffset;
		

		if (gccFuncLineMatcher.matches())
		{
			String funcNameToken = gccFuncLineMatcher.group(2).trim();
			String funcOffsetToken = gccFuncLineMatcher.group(1).trim();
			String funcLengthToken = Messages.getString("MapFile.zero"); //$NON-NLS-1$
			
			f = FunctionFromTokens(funcNameToken, funcOffsetToken, funcLengthToken);
			
			// Some GCC symbol may be bogus
			if (qualifyGCCSymbol(f.offsetFromBinaryStart, funcNameToken)) {
				this.insertToFunctionData(f);
			} 
			
			if (lastGccFunction != null){
				// calculate size of last function with offset from current line
				if (f.offsetFromBinaryStart > lastGccFunction.offsetFromBinaryStart &&
						f.offsetFromBinaryStart < currentGccLibEndingOffset) {
					currentLineOffset = f.offsetFromBinaryStart;
				}
			}
			
		} else if (gccLibOrFillerLineMatcher.matches()) {
			String libOffsetToken = gccLibOrFillerLineMatcher.group(1).trim();
			String libLengthToken = gccLibOrFillerLineMatcher.group(2).trim();
			// next time around we will use the new library offset
			currentGccLibEndingOffset = Long.decode(libLengthToken) + Long.decode(libOffsetToken);				
		} else {
			// next time around we will use the new library offset
			currentGccLibEndingOffset = new Long(0);
		}
		
		// update last function's size if needed
		if (lastGccFunction != null)
		{
			if (currentLineOffset > lastGccFunction.offsetFromBinaryStart) {
				lastGccFunction.length = currentLineOffset - lastGccFunction.offsetFromBinaryStart;
			}
		}
		
		// track function on this line as last function, or null if this line is not a function
		lastGccFunction = f;

	}
	
	private void insertToFunctionData(Function function)
	{
		if (functionData.size() == 0) 
		{
			functionData.addFirst(function);
		}
		else if ((functionData.getFirst()).offsetFromBinaryStart 
					< function.offsetFromBinaryStart)
		{
			functionData.addFirst(function);
		}
		else if ((functionData.getLast()).offsetFromBinaryStart 
					> function.offsetFromBinaryStart)
		{
			functionData.addLast(function);
		}
		else
		{
			for (int i=0;i<functionData.size();i++)
			{
				if ((functionData.get(i)).offsetFromBinaryStart 
						< function.offsetFromBinaryStart)
				{
					functionData.add(i,function);
					break;
				}
			}
		}
	}
	
	private void copyToSortedFunctionData()
	{
		if (this.functionData.size() <= 0) {
			return;
		}
		this.sortedFunctionData.clear();
		long start = (this.functionData.getLast()).offsetFromBinaryStart;
		Function previous = null;
		boolean reallyAdded = false;
		
		for (int i=this.functionData.size()-1;i>=0;i--)
		{
			//System.out.println(i);
			Function f = this.functionData.get(i);
			f.offsetFromBinaryStart = f.offsetFromBinaryStart - start;
			
			if (this.sortedFunctionData.size() == 0)
			{
				// add the function if the vector is empty
				this.sortedFunctionData.add(f);
				reallyAdded = true;
			}
			else if ( (this.sortedFunctionData.get(this.sortedFunctionData.size()-1)).offsetFromBinaryStart != f.offsetFromBinaryStart)
			{
				// add the function if the offset is not the same as with the previous line
				this.sortedFunctionData.add(f);
				reallyAdded = true;
			}	
			else if ( (this.sortedFunctionData.get(this.sortedFunctionData.size()-1)).functionName.startsWith("_")) //$NON-NLS-1$
			{	
				// if there is a key with this offset, discard the previous with prefix "_"
				this.sortedFunctionData.remove(this.sortedFunctionData.get(this.sortedFunctionData.size()-1));
				// add the new function with the same key
				this.sortedFunctionData.add(f);
				reallyAdded = true;
			}
			
			// do this only if we really added the function to the sorted list
			if (reallyAdded == true)
			{
				// store the length of the previous function
				if (previous != null)
					previous.length = f.offsetFromBinaryStart - previous.offsetFromBinaryStart;
				previous = f;	
				reallyAdded = false;
			}
			
		}
		this.functionData.clear();
		this.functionData = null;
	}
	
	/* internal test function: comment out so code coverage looks
	 * good quantitatively
	private void printSortedFunctions()
	{ 
		long totalLength = 0;
		for (Function f : sortedFunctionData)
		{	
			System.out.println( f.offsetFromBinaryStart+Messages.getString("MapFile.openParenthesis")+totalLength+Messages.getString("MapFile.closeParenthesis")+Long.toHexString(f.length)+Messages.getString("MapFile.dashDash")+f.functionName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			totalLength += f.length;
		}
	}
	*/
	
	private void flagFileNotFound(File file, String referencePath, long referenceLineNumber) {
		  String myMessage = Messages.getString("MapFile.map.file") +  file.getAbsoluteFile().getName() + Messages.getString("MapFile.not.found"); //$NON-NLS-1$ //$NON-NLS-2$
		  if (referencePath != null && referencePath.length() > 0) {
			  myMessage += Messages.getString("MapFile.referenced.by") + referencePath; //$NON-NLS-1$
		  }
		  if (referenceLineNumber > 0) {
			  myMessage += Messages.getString("MapFile.line.number") + referenceLineNumber; //$NON-NLS-1$
		  }

    	  GeneralMessages.PiLog(myMessage, GeneralMessages.ERROR);
	}

	private void flagIOException(File file, String referencePath, long referenceLineNumber) {
		  String myMessage = Messages.getString("MapFile.map.file") + file.getAbsoluteFile().getName() + Messages.getString("MapFile.ioexception"); //$NON-NLS-1$ //$NON-NLS-2$
		  if (referencePath != null && referencePath.length() > 0) {
			  myMessage += Messages.getString("MapFile.referenced.by") + referencePath; //$NON-NLS-1$
		  }
		  if (referenceLineNumber > 0) {
			  myMessage += Messages.getString("MapFile.line.number") + referenceLineNumber; //$NON-NLS-1$
		  }

		  GeneralMessages.showErrorMessage(myMessage);
		  GeneralMessages.PiLog(myMessage, GeneralMessages.ERROR);
	}
}
