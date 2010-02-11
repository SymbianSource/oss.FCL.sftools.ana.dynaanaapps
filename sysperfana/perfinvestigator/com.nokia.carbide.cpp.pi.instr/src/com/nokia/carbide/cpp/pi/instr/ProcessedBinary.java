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

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.Function;


public class ProcessedBinary extends Binary
{
  private static final long serialVersionUID = 8267855662123433263L;
  	
  public File file;
  public byte[] data;
  private MapFile mapFile;
  private int[][] finalLookupTable;
  private boolean debug;
  private Hashtable<String,Function> knownFunctions = new Hashtable<String,Function>();
  
  public ProcessedBinary(File file) throws Exception
  {
  	//super(file.getName());
  	super(file.getAbsolutePath().substring(
  			file.getAbsolutePath().indexOf(File.separator),
			file.getAbsolutePath().length()));
  	
  	//System.out.println("File name: "+file.getAbsolutePath().substring(
  	//		file.getAbsolutePath().indexOf("\\"),
	//		file.getAbsolutePath().length()));
  	
  	this.file = file;
    this.length = (int)file.length();
    this.resolveType(file.getAbsolutePath());
    
    FileInputStream fis = new FileInputStream(file);
    data = new byte[(int)file.length()];
    fis.read(data,0,(int)file.length());
    fis.close();
    //System.out.println("Read "+length+" bytes from "+name);
    createLookupTable();
    
	mapFile = new MapFile(new File(file.getAbsolutePath()+".map"), "", 0); //$NON-NLS-1$ //$NON-NLS-2$
    
    if (this.data.length > 0x7C)
    {
    	this.offsetToCodeStart = 	( (long)this.data[0x67] << 24)+
									( (long)this.data[0x66] << 16)+
									( (long)this.data[0x65] << 8)+
									( (long)this.data[0x64] );
	
    	this.offsetToCodeStart += 	( (long)this.data[0x4B] << 24)+
									( (long)this.data[0x4A] << 16)+
									( (long)this.data[0x49] << 8)+
									( (long)this.data[0x48] );
    	
    	if (this.type == "thumb") this.offsetToCodeStart -=1; //$NON-NLS-1$
    	
    	if (this.offsetToCodeStart >= 4) this.offsetToCodeStart -=4;
    }
    
    //System.out.println("Bin "+this.binaryName+" offset "+this.offsetToCodeStart);
  }
  
  public long getOffsetFromBinaryStartForFunction(String functionName)
  {
  	if (this.mapFile != null)
  		return this.mapFile.getOffsetFromBinaryStartForFunction(functionName);
  	else 
  		return -1;
  }
  
  private void resolveType(String filePath)
  {
  	filePath = filePath.toLowerCase();
  	filePath = filePath.replace('\\','/');
  	
    if ( filePath.indexOf("/thumb/") != -1 ) //$NON-NLS-1$
    {
    	this.type = "thumb"; //$NON-NLS-1$
    }
    else if ( filePath.indexOf("/armi/") != -1 ) //$NON-NLS-1$
			  
    {
    	this.type = "armi"; //$NON-NLS-1$
    }
    
    else if ( filePath.indexOf("/momap15xx/") != -1 ) //$NON-NLS-1$
    {
    	this.type = "momap15xx"; //$NON-NLS-1$
    }

    else if ( filePath.indexOf("/momap16xx/") != -1 ) //$NON-NLS-1$
    {
    	this.type = "momap16xx"; //$NON-NLS-1$
    }

    else if ( filePath.indexOf("/arm4/") != -1 ) //$NON-NLS-1$
    {
    	this.type = "arm4"; //$NON-NLS-1$
    }
    else this.type = Messages.getString("ProcessedBinary.unknownType"); //$NON-NLS-1$

  }
  
  public String getFunctionNameForOffset(long offset)
  {
  	if (this.mapFile == null) return Messages.getString("ProcessedBinary.mapFileForBinaryNotFound1")+this.binaryName+Messages.getString("ProcessedBinary.mapFileForBinaryNotFound2"); //$NON-NLS-1$ //$NON-NLS-2$
  	return this.mapFile.getFunctionNameForOffset(offset);
  }
  
  public Function getFunctionForOffset(long offset)
  {
  	if (this.mapFile == null) 
  	{
  		return null;
  	}
  	else
  	{
  		String name = this.mapFile.getFunctionNameForOffset(offset);
  		String dllName = this.binaryName;
  		
  		long offsetFromBinaryStart = this.mapFile.getOffsetFromBinaryStartForFunction(name); 
  		// this cannot be resolved here, since the address is not known
  		Long addr = new Long(0);
  		
  		String search = name + dllName;
  		Function f = this.knownFunctions.get(search);
  		
  		if (f == null) {
	  		f = new Function(name,addr,dllName);
	  		f.offsetFromBinaryStart = offsetFromBinaryStart;
	  		f.length = this.mapFile.getFunctionLengthForOffset(offset);
	  		this.knownFunctions.put(search, f);
  		}
  		
  		return f;
  	}
  	
  }
  
  public long getFunctionLengthForOffset(long offset)
  {
  	if (this.mapFile != null)
  		return this.mapFile.getFunctionLengthForOffset(offset);
  	else return 0;
  }

  private static class Value
  {
    int value;

    public Value(int value)
    {this.value = value;}
  }

  private void createLookupTable()
  {
    Vector[] lookupTable = new Vector[256];

    //System.out.println("Creating Lookup Table for "+this.name);
    for (int i=0;i<data.length-1;i++)
    {
      //System.out.println("Processing sequence "+Integer.toHexString(value.intValue())+"\n");
      int unsigned = ((int)data[i] & 0xff);

      if (lookupTable[unsigned] == null)
      {
        Vector v = new Vector();
        v.add(new Value(i));
        lookupTable[unsigned] = v;
        //System.out.println("New Value "+Integer.toHexString(unsigned));
      }
      else
      {
        Vector v = lookupTable[unsigned];
        v.add(new Value(i));
        //System.out.println("Old Value "+Integer.toHexString(unsigned));
        //System.out.print("e");
      }
      //if (i%40 == 0) System.out.print("\n");
    }

    this.finalLookupTable = new int[256][];
    for (int i=0;i<256;i++)
    {
      if (lookupTable[i] != null)
      {
        this.finalLookupTable[i] = new int[lookupTable[i].size()];

        Enumeration enumer = lookupTable[i].elements();

        int element = 0;

        while (enumer.hasMoreElements())
        {
          Value v = (Value) enumer.nextElement();
          this.finalLookupTable[i][element] = v.value;
          element++;
        }
      }
    }
  }

  public long getInstructionFromOffset(int offset)
  {
    long instruction = 0;

    instruction = ((long)(( (long)this.data[offset+3])   & 0xff));
    instruction += ((long)((((long)(this.data[offset+2])) & 0xff) << 8));
    instruction += ((long)((((long)(this.data[offset+1])) & 0xff) << 16));
    instruction += ((long)((((long)(this.data[offset])) & 0xff) << 24));

    //System.out.println("INSTRUCTION: "+Integer.toHexString((int)instruction));

    return instruction;
  }

  public long calculateXorChecksum(int startIndex, int lengthInInstructions)
  {
    long value = 0;
    for (int i=0;i<lengthInInstructions;i++)
    {
      long instruction = this.getInstructionFromOffset(startIndex+(i*4));

      // xor the value with the provious value
      value ^= instruction;
    }
    return value;
  }

  public int[] getIndicesForSequence(int sequence)
  {
    if (sequence < this.finalLookupTable.length)
    {
      return this.finalLookupTable[sequence];
    }
    else return null;
  }
}
