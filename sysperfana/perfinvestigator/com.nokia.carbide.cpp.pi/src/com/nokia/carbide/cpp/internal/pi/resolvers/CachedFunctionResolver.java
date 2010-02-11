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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;


// re-implement SymbolFileParser for supporting 
// cached resolver with ROFS symbols

public abstract class CachedFunctionResolver implements FunctionResolver {
	
	protected SymbolFileDllItem[] dllList = null;
	
	// cache
	private int cacheSize = 0;
	private long cacheAddressTable[] = new long[cacheSize];
	private long mostPopularCache[] = new long[cacheSize];
	private SymbolFileFunctionItem[] cacheOrderTable = new SymbolFileFunctionItem[cacheSize];
	private SymbolFileFunctionItem[] mostPopularCacheTable = new SymbolFileFunctionItem[cacheSize];
	//private int mostPopularHitCount[] = new int[cacheSize];
	private int mostPopularTableIndex = 0;
	private int cacheIndex = 0;
	//private int cacheFoundPopular = 0;
	//private int cacheFound = 0;
	//private int cacheNotFound = 0;
	
	// whether the resolver has enough information to resolve anything
	protected boolean ableToResolve = false;
	
	private Hashtable<String,Function> knownFunctions = new Hashtable<String,Function>();
	private Hashtable<String,Binary> knownBinaries = new Hashtable<String,Binary>();
	
	protected static class SymbolFileFunctionItem
	{
		public String name;
		public long address;
	    public long length;
	    public SymbolFileDllItem dll;

	    public SymbolFileFunctionItem(String name, long address, long length, SymbolFileDllItem dll)
	    {
	    	this.name = name;
	    	this.length = length;
	    	this.address = address;
	    	this.dll = dll;
	    }
	}
	
	protected static class SymbolFileDllItem
	{
		public String name;
		public long start;
	    public long end;
	    public ArrayList<SymbolFileFunctionItem> data;
	    public boolean uninitialised;

	    public SymbolFileDllItem()
	    {
	      start = 0;
	      end = 0;
	      name = null;
	      data = new ArrayList<SymbolFileFunctionItem>();
	      uninitialised = true;
	    }
	}
	
	// must implement some way to form a DLL list e.g. read rom/rofs symbol file, read obyfile
	public abstract void parseAndProcessSymbolFile(java.io.File symbolFile);
	
	public void addAllToDllList(Collection<SymbolFileDllItem> list) {
		
		// if the dll array is empty, create an array based on the input list
		if (dllList == null) {
			dllList = new SymbolFileDllItem[list.size()];
			dllList = list.toArray(dllList);
		} else {
			// add the input list to the existing dll array
			SymbolFileDllItem[] oldList = dllList;
			Object[] objects = list.toArray();
			
			dllList = new SymbolFileDllItem[dllList.length + list.size()];
			
			int i = 0;
			for (int j = 0; j < oldList.length; j++)
				dllList[i++] = oldList[j];
			
			for (int j = 0; j < list.size(); j++)
				dllList[i++] = (SymbolFileDllItem) objects[j];
		}
		
		// sort the dll list
		Arrays.sort(dllList, new Comparator<Object>() {
			
			public int compare(Object arg0, Object arg1)
			{
				return (int) (((SymbolFileDllItem)arg0).start - ((SymbolFileDllItem)arg1).start);
			}
		});	
	}
	
	public void clearDllList()
	{
		dllList = null;
	}
	
	public CachedFunctionResolver()
	{
		this.clearDllList();
		this.initializeCache(250);
	}

	public Binary findBinaryForAddress(long address) {
	  	SymbolFileDllItem item = this.getDllItemForAddress(address);
	  	if (item != null)
	  	{
	  		Binary b = this.knownBinaries.get(item.name);
	  		
	  		if (b == null) {
		  		b = new Binary(item.name);
		  		b.length = (int)(item.end-item.start);
		  		b.offsetToCodeStart = 0;
		  		b.startAddress = item.start;
				this.knownBinaries.put(item.name, b);
	  		}
	  		return b;
	  	}
	  	else
	  		return null;
//	  	{
//	  		String bName = "Binary at 0x"+Long.toHexString(address)+" not found;
//
//	  		Binary b = this.knownBinaries.get(bName);
//	  		
//	  		if (b == null) {
//		  		b = new Binary(bName);
//		  		b.length = 0;
//		  		b.offsetToCodeStart = 0;
//		  		b.startAddress = address;
//				this.knownBinaries.put(bName, b);
//			}
//	  		return b;
//	  	}
	}

	public String findBinaryNameForAddress(long address) {
	    SymbolFileDllItem foundDllItem = this.getDllItemForAddress(address);
	    if (foundDllItem != null)
	    	return foundDllItem.name;
	    else
	    	return Messages.getString("CachedFunctionResolver.dllForAddress")+Long.toHexString(address)+Messages.getString("CachedFunctionResolver.notFound");   //$NON-NLS-1$ //$NON-NLS-2$
	}

	public Function findFunctionForAddress(long address) {
	  	SymbolFileFunctionItem item = this.getFunctionItemForAddress(address);

	  	if (item != null)
	  	{
	  		String search = item.name + item.dll.name;
	 		Function f = this.knownFunctions.get(search);

	  		if (f == null) {
		  		f = new Function(item.name,new Long(item.address),item.dll.name);
		  		f.offsetFromBinaryStart = item.address-item.dll.start;
		  		f.length = item.length;
		  		this.knownFunctions.put(search, f);
	  		}
	  		return f;
	  	}
	  	else
	  	{
	//System.out.println(Long.toHexString(address));
	  		SymbolFileDllItem dllItem = this.getDllItemForAddress(address);
	  	  	  		
	  		if (dllItem != null)
	  		{
	  			String fName = Messages.getString("CachedFunctionResolver.functionNotFound1")+Long.toHexString(address)+Messages.getString("CachedFunctionResolver.functionNotFound2"); //$NON-NLS-1$ //$NON-NLS-2$
	  			
	  			Function f = this.knownFunctions.get(fName);

	  	  		if (f == null ) {
	  	  			f = new Function(fName, new Long(dllItem.start), dllItem.name);
		  			f.offsetFromBinaryStart = address-dllItem.start;
		  			f.length = 0;
		  			this.knownFunctions.put(fName, f);
	  	  		}

	  	  		return f;
	  		}
	  		else
	  			return null;
//	  		{
//	  			String fName = Messages.getString("CachedFunctionResolver.functionNotFound1")+Long.toHexString(address)+Messages.getString("CachedFunctionResolver.functionNotFound2"); //$NON-NLS-1$ //$NON-NLS-2$
//	  			
//	  			Function f = this.knownFunctions.get(fName);
//
//	  	  		if (f == null ) {
//	  	  			f = new Function(fName, new Long(address),
//	  	  							 "Binary at 0x" + 
//									 Long.toHexString(address)+" not found");
//	  	  			f.offsetFromBinaryStart = 0;
//	  	  			this.knownFunctions.put(fName, f);
//	  	  		}
//
//	  			return f;
//	  		}
	  	}
	}

	public String findFunctionNameForAddress(long address) {
	  	SymbolFileFunctionItem item = this.getFunctionItemForAddress(address);
	  	if (item != null)
	  	{
	  		return item.name;
	  	}
	  	else
	  	{
	  		return Messages.getString("CachedFunctionResolver.functionForAddress")+Long.toHexString(address)+Messages.getString("CachedFunctionResolver.notFound");   //$NON-NLS-1$ //$NON-NLS-2$
	  	}
	}

	public String getResolverName() {
		return Messages.getString("CachedFunctionResolver.symbol");  //$NON-NLS-1$
	}

	public String getResolverString() {
	  	return Messages.getString("CachedFunctionResolver.symbolFile");  //$NON-NLS-1$
	}

	public SymbolFileDllItem getDllItemForAddress(long address)
	{
		if (dllList == null)
			return null;
		
		for (int i=0; i < this.dllList.length; i++)
		{
			SymbolFileDllItem dllItem = this.dllList[i];
			if (address >= dllItem.start && address < dllItem.end)
			{
				return dllItem;
			}
		}
		return null;
	}

	private void initializeCache(int size)
	{
	    cacheSize = size;

	    cacheAddressTable = new long[cacheSize];
	    mostPopularCache = new long[cacheSize];
	    cacheOrderTable = new SymbolFileFunctionItem[cacheSize];
	    mostPopularCacheTable = new SymbolFileFunctionItem[cacheSize];
//	    mostPopularHitCount = new int[cacheSize];
	    mostPopularTableIndex = 0;

	    for (int i=0;i<cacheSize;i++)
	    {
	      cacheAddressTable[i] = 0;
	      mostPopularCache[i] = 0;

	      cacheOrderTable[i] = null;
	      mostPopularCacheTable[i] = null;

//	      mostPopularHitCount[i] = 0;
	    }
	}
	  
	private SymbolFileFunctionItem getFunctionItemForAddress(long address)
	{  	
		SymbolFileFunctionItem cached = findFromCache(address);
	    if (cached != null) return cached;

	    SymbolFileDllItem foundDllItem = null;

	    if (this.dllList != null)
	    	for (int i=0; i < this.dllList.length; i++)
		    {
		    	SymbolFileDllItem dllItem = this.dllList[i];
		    	if (address >= dllItem.start && address < dllItem.end)
		    	{
		    		foundDllItem = dllItem;
		    		break;
		    	}
		    }

	    if (foundDllItem != null)
	    {
	      int listSize = foundDllItem.data.size();
	      SymbolFileFunctionItem functionItem;
	      
	      for (int i = 0; i + 1 < listSize; i++)
	      {
	        functionItem = (SymbolFileFunctionItem)foundDllItem.data.get(i);
	       
	        if (address >= functionItem.address && address < functionItem.address+functionItem.length)
	        {
	            addToCache(functionItem,address);
	            return functionItem;
	        }
	      }
	      
	      // if last item is an entry for a static function area, ignore it
	      functionItem = (SymbolFileFunctionItem)foundDllItem.data.get(listSize - 1);
	      
	      if (!functionItem.name.startsWith(Messages.getString("CachedFunctionResolver.possibleStaticFunction1"))) {	//$NON-NLS-1$
	          addToCache(functionItem,address);
	          return functionItem;
	      }
	    }
	    
	    return null;
	}

	  private SymbolFileFunctionItem findFromCache(long address)
	  {
	    SymbolFileFunctionItem item = null;

	    item = findFromMostPopular(address);
	    if (item!=null) return item;

	    for (int i=0;i<cacheSize;i++)
	    {
	      if (cacheAddressTable[i] == address)
	      {
	        item = cacheOrderTable[i];
	        //System.out.println("Found from cache,adding to most popular");
//	        cacheFound++;
	        addToMostPopular(item,address);
	        break;
	      }
	    }
//	    cacheNotFound++;
	    //if (item == null) System.out.println("Not found from the cache!!");
	    return item;
	  }

	  private void addToCache(SymbolFileFunctionItem item,long address)
	  {
	    if (cacheSize == 0) return;
	    cacheOrderTable[cacheIndex] = item;
	    cacheAddressTable[cacheIndex] = address;

	    cacheIndex++;
	    if (cacheIndex == cacheSize) cacheIndex = 0;
	  }

		/* internal test function : comment out so code coverage looks
		 * good quantitatively
	  public void printMostPopularFromCache(int amountToPrint)
	  {
	    if (amountToPrint > cacheSize) amountToPrint = cacheSize;
	    System.out.println(Messages.getString("CachedFunctionResolver.mostPopularFromCache"));  //$NON-NLS-1$

	    for (int i=0;i<amountToPrint;i++)
	    {
	      SymbolFileFunctionItem item = mostPopularCacheTable[i];
	      if (item!=null) System.out.println(item.name+Messages.getString("CachedFunctionResolver.hits")+mostPopularHitCount[i]);  //$NON-NLS-1$
	      else break;
	    }
	    System.out.println(Messages.getString("CachedFunctionResolver.stats"));  //$NON-NLS-1$
	    int all = cacheFound+cacheFoundPopular+cacheNotFound;
	    System.out.println(Messages.getString("CachedFunctionResolver.foundFromMostPopularCache")+(cacheFoundPopular*100f/all)+Messages.getString("CachedFunctionResolver.percentageSign"));   //$NON-NLS-1$ //$NON-NLS-2$
	    System.out.println(Messages.getString("CachedFunctionResolver.foundFromCache")+(cacheFound*100f/all)+Messages.getString("CachedFunctionResolver.percentageSign"));   //$NON-NLS-1$ //$NON-NLS-2$
	    System.out.println(Messages.getString("CachedFunctionResolver.notFoundFromCache")+(cacheNotFound*100f/all)+Messages.getString("CachedFunctionResolver.percentageSign"));   //$NON-NLS-1$ //$NON-NLS-2$

	    System.out.println(Messages.getString("CachedFunctionResolver.end"));  //$NON-NLS-1$
	  }
	  *
	  */

	  private SymbolFileFunctionItem findFromMostPopular(long address)
	  {
	    SymbolFileFunctionItem item = null;

	    int i = 0;
	    
	    // until there are a few entries in mostPopularCache[], it's faster not using a binary search
	    if (mostPopularCache[4] == 0) {
		    for (;i<cacheSize && mostPopularCache[i] > address;i++)
		    	;
	    } else {
			// use a binary search to find the item
			int lowerBound = 0;
			int upperBound = cacheSize - 1;
		    while (lowerBound <= upperBound) {
		    	i = (lowerBound + upperBound)/2;
				if (mostPopularCache[i] == address) {
					item = mostPopularCacheTable[i];
		            break;
				} else if (address > mostPopularCache[i])
		            upperBound = i - 1;
		        else
		        	lowerBound = i + 1;
		    }

//			// check binary search		    
//		    int j = 0;
//		    for (;j<cacheSize && mostPopularCache[j] > address;j++)
//		    	;
//		    if (j != i && mostPopularCache[j] > address)
//		    	System.out.println("binary search failed");
	    }
	    
        if (mostPopularCache[i] == address)
        {
          item = mostPopularCacheTable[i];
//		  mostPopularHitCount[i]++;
//		  cacheFoundPopular++;
        }

	    return item;
	  }

	  private void addToMostPopular(SymbolFileFunctionItem item, long address)
	  {
	    // most popular cache is full, delete 20%
	    if (mostPopularTableIndex == cacheSize)
	    {
	       for (int i=(8*(cacheSize/10));i<cacheSize;i++)
	       {
	         mostPopularCache[i] = 0;
	         mostPopularCacheTable[i] = null;
//	         mostPopularHitCount[i] = 0;
	       }
	       mostPopularTableIndex = 8*(cacheSize/10);
	       //System.out.println("Clearing most popular ");
	    }
	    
	    // add the new item in descending address order, so that we can bail out during search
	    int i = 0;
	    
	    for ( ; i < mostPopularTableIndex && mostPopularCache[i] > address; i++)
	    	;
	    
	    for (int j = mostPopularTableIndex; j > i; j--)
	    {
	        mostPopularCacheTable[j] = mostPopularCacheTable[j-1];
	        mostPopularCache[j] = mostPopularCache[j-1];
//	        mostPopularHitCount[j] = mostPopularHitCount[j-1];
	    }

    	mostPopularCache[i] = address;
    	mostPopularCacheTable[i] = item;
//    	mostPopularHitCount[i] = 2;
    	mostPopularTableIndex++;
	  }
	  
	  public boolean canResolve() {
		  return this.ableToResolve;
	  }
}
