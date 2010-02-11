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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.GenericEvent;
import com.nokia.carbide.cpp.internal.pi.model.GenericEventTrace;


public class IttTrace122 extends GenericEventTrace
  {
	private static final long serialVersionUID = -3670942351731061113L;
	private boolean debug = false;
	private transient boolean sortedEvents = false;
	private transient Object[] sorted;
	private transient Hashtable<String,Function> knownFunctions = new Hashtable<String,Function>();
	private transient Hashtable<String,Binary> knownBinaries = new Hashtable<String,Binary>();
	
	public IttTrace122()
	{
	}
	
	public Binary getBinaryForAddress(long address)
	{
		Enumeration<GenericEvent> enr = this.getEvents().elements();
		while(enr.hasMoreElements())
		{
			IttEvent122 ev = (IttEvent122)enr.nextElement();
			if (   (address >= ev.binaryLocation)
				&& (address < (ev.binaryLocation + ev.binaryLength)) )
			{
				//System.out.println("Found "+Long.toHexString(address));
				return ev.binary;			
			}
		}
		
		return null;
	}

	public Binary getBinaryForAddressNew(long address)
	{
		if (!sortedEvents) {
			sorted = this.getEvents().toArray();
			Arrays.sort(sorted, new Comparator<Object>() {
				
				public int compare(Object arg0, Object arg1)
				{
					if (arg0 instanceof IttEvent122 && arg1 instanceof IttEvent122)
						return (int) (((IttEvent122)arg0).binaryLocation - ((IttEvent122)arg1).binaryLocation);
					else
						return 0;
				}
			});
			sortedEvents = true;
		}

		int high = sorted.length;
		int low = -1;
		int next;
		
		// find a match using binary search
		while (high - low > 1) {
			next = (low + high) >>> 1;
			IttEvent122 event = (IttEvent122)sorted[next];

			if (   (address >= event.binaryLocation)
				&& (address < (event.binaryLocation + event.binaryLength)) ) {
				return event.binary;
			}

			if (event.binaryLocation >= address) {
				high = next;
			} else {
				low = next;
			}
		}

		return null;
	}

	public Function getFunctionForAddress(long address,BinaryReader122 br)
	{
		Binary b = this.getBinaryForAddressNew(address);
		if (b != null)
		{	
			MapFile mf = br.getMapFileForBinary(b);
			Function f = null;
			
			if (mf != null)
			{
				f = mf.getFunctionForOffset(address-b.startAddress);
				if (f != null)
				{
					f.startAddress = new Long(b.startAddress+mf.getOffsetFromBinaryStartForFunction(f.functionName));
					if (f.startAddress != null)
					{
						if (f.functionBinary != null) 
						{
							f.functionBinary = b;
						}
						//System.out.println("Resolved function to "+f.functionName+" "+Long.toHexString(f.startAddress.longValue()));
						return f;
					}
				}
				
				if (debug)System.out.println(Messages.getString("IttTrace122.couldNotResolveFunction")); //$NON-NLS-1$
			}
			else
			{
				if (debug)System.out.println(Messages.getString("IttTrace122.mapfileNotFound1")+b.binaryName+Messages.getString("IttTrace122.mapfileNotFound2")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			if (f != null)
			{
				return f;
			}
			else
			{
				String fName = Messages.getString("IttTrace122.functionForAddressNotFound1")+Long.toHexString(address)+Messages.getString("IttTrace122.functionForAddressNotFound2"); //$NON-NLS-1$ //$NON-NLS-2$

				f = this.knownFunctions.get(fName);
				
				if (f == null) {
					f = new Function(fName, new Long(address), b.binaryName);
					f.functionBinary = b;
					f.length = 1;
					f.offsetFromBinaryStart = 0;
					
					this.knownFunctions.put(fName, f);
				}

				return f;
			}
		}
		else
			return null;
//		{
//			String bName = Messages.getString("IttTrace122.binaryForAddressNotFound1")+Long.toHexString(address)+Messages.getString("IttTrace122.binaryForAddressNotFound2"); //$NON-NLS-1$ //$NON-NLS-2$
//
//			b = this.knownBinaries.get(bName);
//			
//			if (b == null) {
//				b = new Binary(bName);
//				b.length = 1;
//				b.offsetToCodeStart = 0;
//				b.startAddress = address;
//				b.type = Messages.getString("IttTrace122.unknownBinaryType"); //$NON-NLS-1$
//				this.knownBinaries.put(bName, b);
//			}
//			
//			Function f = new Function(	Messages.getString("IttTrace122.functionForAddressNotFound1")+Long.toHexString(address)+Messages.getString("IttTrace122.functionForAddressNotFound2"), //$NON-NLS-1$ //$NON-NLS-2$
//										new Long(address),
//										bName);
//			
//			f.functionBinary = b;
//			f.length = 1;
//			f.offsetFromBinaryStart = 0;
//			
//			return f;			
//		}
	}
	
	  public Binary getBinaryForFileName(String fileName)
	  {
		  Enumeration<GenericEvent> en = this.getEvents().elements();
		  while (en.hasMoreElements())
		  {
			  IttEvent122 e = (IttEvent122)en.nextElement();
			  if (e.binaryName.toLowerCase().indexOf(fileName.toLowerCase()) != -1)
				  return e.binary;
		  }
		  return null;
	  }
  }
