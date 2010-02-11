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

package com.nokia.carbide.cpp.internal.pi.resolvers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nokia.carbide.cpp.pi.util.GeneralMessages;

public class SymbolFileFunctionResolver extends CachedFunctionResolver {
	private boolean debug = false;
	
	@Override
	public void parseAndProcessSymbolFile(File symbolFile) {
	{
		/*
		 * From    \\epoc32\\release\\armv5\\urel\\foobar.exe
		 * f80130b4    0000    __some_func                              k_entry_.o(.emb_text)
		 * f8013e5c    0014    NKern::SomeFunc(unsigned long&, unsigned long&)  _reka2_ekern.in(.emb_text)
		 * f9165ea8    0008    thunk{-4} to SomeClass::SomeFunc(CustType*, int(*)(const void*))  SVGEngine.in(.text)
		 */
		
		Pattern binaryLinePattern  = Pattern.compile("From\\p{Blank}+(\\S.+)\\p{Blank}*");	//$NON-NLS-1$
  		Pattern symbolLinePattern1 = Pattern.compile("(\\p{XDigit}+)\\p{Blank}+(\\p{XDigit}+)\\p{Blank}+((?!\\d)\\S.+)\\p{Blank}+(\\S.+)");	//$NON-NLS-1$
 
		ArrayList<SymbolFileDllItem> dllTable = new ArrayList<SymbolFileDllItem>();
		SymbolFileDllItem currentDll = null;

	    BufferedReader br;
		try {
		    SymbolFileFunctionItem pendingItem = null;
		    SymbolFileFunctionItem previousItem = null;
		    
		    Matcher symbolLineMatcher;
		    
			long lineNumber = 0;
			br = new BufferedReader(new FileReader(symbolFile));
			this.ableToResolve = true;

			while(br.ready())
		    {
		    	String line = br.readLine();
		    	++lineNumber;
		    	if (line == null)
		    		continue;
		      
		    	symbolLineMatcher = symbolLinePattern1.matcher(line);
				
				if (symbolLineMatcher.matches()) {
					
		            long address = Long.parseLong(symbolLineMatcher.group(1),16);
		            long length = Long.parseLong(symbolLineMatcher.group(2),16);
		            String name = symbolLineMatcher.group(3).trim();
		            String section = symbolLineMatcher.group(4);
		            
		            if (skipSymbolFromSection(section, length)) {
		            	continue;
		            }
		            
		            if (currentDll.uninitialised == true)
		            {
		              currentDll.uninitialised = false;
		              currentDll.start = address;
		            }

		            if ((address+length) <= (currentDll.start+(1024*1024*20)) 
		            		&& (address+length) >= currentDll.end 
		            		&& address != 0xffffffffL )
		            {
		            	if (pendingItem != null)
		            	{
		            		// the pending item has to be completed with the 
		            		// address known about this function
		            		long pendingLength = address-pendingItem.address;
		           			pendingItem.length = pendingLength;
		           			// the pending item could be completed, add it to the function list
		           			currentDll.data.add(pendingItem);
		            		
		            		pendingItem = null;
		            	}
		            	
		            	if (previousItem != null)
		            	{
		            		if (previousItem.address+previousItem.length < address)
		            		{
		            			long gapAddress = previousItem.address+previousItem.length;
		            			long gapLength = address-previousItem.address-previousItem.length;
		            			
		           				String gapName = Messages.getString("SymbolFileFunctionResolver.possibleStaticFunction1")+(gapLength)+Messages.getString("SymbolFileFunctionResolver.possibleStaticFunction2")+ //$NON-NLS-1$ //$NON-NLS-2$
											 	Long.toHexString(gapAddress)+Messages.getString("SymbolFileFunctionResolver.possibleStaticFunction3")+ //$NON-NLS-1$
											 	Long.toHexString(address);
		           				SymbolFileFunctionItem gapItem = new SymbolFileFunctionItem(gapName,gapAddress,gapLength,currentDll);
		           			
		           				currentDll.data.add(gapItem);
		            		}
		            		
		            		else if (previousItem.address+previousItem.length > address)
		            		{
		            			//These overlaps happen in symbol files from some device's ROM
		            			//and don't seems to be a problem in our code.
		            			if (debug) {
			            			String outString = Messages.getString("SymbolFileFunctionResolver.debugOverlap1")+ //$NON-NLS-1$
	            					Long.toHexString(previousItem.address+previousItem.length)+
									Messages.getString("SymbolFileFunctionResolver.debugOverlap2")+ //$NON-NLS-1$
									Long.toHexString(address)+Messages.getString("SymbolFileFunctionResolver.debugOverlap3")+previousItem.name+Messages.getString("SymbolFileFunctionResolver.debugOverlap4")+name; //$NON-NLS-1$ //$NON-NLS-2$
			            			//GeneralMessages.showWarningMessage(outString);
		            				System.out.println(outString);
		            			}
		            		}
		            	}
		            	
		        		if (length == 0)
		                {
		                	currentDll.end = address;
		                	
		        			// if the length of this function is not known,
		        			// add it to be completed when the next function is parsed
		        			// this assumes that the length of the function is the space
		        			// between this function and the next function
		                	pendingItem = new SymbolFileFunctionItem(name,address,length,currentDll);
		                	
		                	// update the previous item
		                	previousItem = pendingItem;
		                }
		        		else
		        		{
		                	currentDll.end = address+length;
		                    SymbolFileFunctionItem function = new SymbolFileFunctionItem(name,address,length,currentDll);
		                    currentDll.data.add(function);
		                    
		                    // update the previous item
		                    previousItem = function;
		                    
		                    // there is no pending item since the function was added here
		                    pendingItem = null;
		        		}
		            }

	            } else if (binaryLinePattern.matcher(line).matches()) {
					
		          	pendingItem = null;
		          	previousItem = null;

		            currentDll = new SymbolFileDllItem();
		            if (line.indexOf('\\') != -1)
		            {
		            	currentDll.name = line.substring(line.indexOf('\\'),line.length());
		            }
		            else
		            {
		            	currentDll.name = line.substring(0,line.length());
		            }
		            dllTable.add(currentDll);
		            
				} else {
						
	            	if (debug) System.out.println(Messages.getString("SymbolFileFunctionResolver.skippingLine")+line); //$NON-NLS-1$
				}
		    }

		    if (br != null)
		    	br.close();
		} catch (FileNotFoundException e) {
			GeneralMessages.PiLog(Messages.getString("SymbolFileFunctionResolver.symbol.file1") +  symbolFile + Messages.getString("SymbolFileFunctionResolver.not.found"), GeneralMessages.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
			String myMessage = Messages.getString("SymbolFileFunctionResolver.symbol.file2") +  symbolFile + Messages.getString("SymbolFileFunctionResolver.ioexception"); //$NON-NLS-1$ //$NON-NLS-2$
			GeneralMessages.showErrorMessage(myMessage);
			GeneralMessages.PiLog(myMessage, GeneralMessages.ERROR);
		}

	    // this breaks the symbol file parser, use for testing itt trace
	    //dllTable.clear();
	    
	    if (debug) System.out.println(Messages.getString("SymbolFileFunctionResolver.dllCount")+dllTable.size()); //$NON-NLS-1$
	    addAllToDllList(dllTable);
	  }
	}

	private boolean skipSymbolFromSection(String section, long length) {
        
        if (section.endsWith("(.data)") || section.endsWith("(.bss)") || section.endsWith("(linker$$defined$$symbols)")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        	return true;
        }
        
        if (length == 0 && section.endsWith("(.init_array)")) { //$NON-NLS-1$
        	return true;
        }
        
        if (section.contains("(.data_")) {	// (.data__ZZ33ifPowerTraceIdIsEnabledCallCbOncePFvPvES_mE9isPrinted) //$NON-NLS-1$
        	return true;
        }
        
		return false;
	}
}
