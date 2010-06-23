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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericEvent;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.utils.QuickSortImpl;
import com.nokia.carbide.cpp.internal.pi.utils.Sortable;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


public class IttTraceParser extends Parser
{
  private boolean debug = false;
  private Vector<IttSample> samples;
  private IttTrace122 trace122;

  public IttTraceParser()
  {
  }
  
  public ParsedTraceData parseNoProgress(File f) throws Exception
  {
	  return this.parse(f/*,null*/);
  }
  
  public ParsedTraceData parse(File f /*, ProgressBar progressBar*/) throws Exception 
  {
  		if (!f.exists() || f.isDirectory())
  	    {
  	      throw new Exception(Messages.getString("IttTraceParser.cannotOpenTraceFile")); //$NON-NLS-1$
  	    }

  		parseIttTrace(f);
  		ParsedTraceData ptd = new ParsedTraceData();
		ptd.traceData = this.getTrace();
		
		if (ptd.traceData instanceof IttTrace) 
		{
			BinaryReader br = new BinaryReader();
			AdvancedMemoryMap amm = new AdvancedMemoryMap(br);
			
	  		int analysisId = NpiInstanceRepository.getInstance().activeUid();
        	Enumeration resEnum = TraceDataRepository.getInstance().getResolvers(analysisId);
        	
        	FunctionResolver symbolFileParser = null;
        	
        	while(resEnum.hasMoreElements())
        	{
        		FunctionResolver fr = (FunctionResolver)resEnum.nextElement();
        		if (fr.getResolverName().equals("Symbol"))  //$NON-NLS-1$
        		{
        			symbolFileParser = fr;
        			break;
        		}
        	}
			
      		if (symbolFileParser == null)
      	    {
      			// unfortunately, GPP need to be read first in order to make Symbol function resolver available
      	      throw new Exception(Messages.getString("IttTraceParser.symbolResolverNotFound")); //$NON-NLS-1$
      	    }
			
            Enumeration sEnum = ((IttTrace)this.getTrace()).getSamples();
            int i = 0;
            int len = ((IttTrace)this.getTrace()).samples.size();
            String progString = Messages.getString("IttTraceParser.traceProgress0Percent"); //$NON-NLS-1$
            
            while (sEnum.hasMoreElements())
            {
        		IttSample itts = (IttSample) sEnum.nextElement();

            	if (symbolFileParser != null)
            	{
            		if (symbolFileParser.findBinaryNameForAddress(
                				itts.programCounter).endsWith(Messages.getString("IttTraceParser.binaryNotFound"))) //$NON-NLS-1$
            		{
            			amm.insertSample(itts);
            		}
            	}
            	else
            	{
            		amm.insertSample(itts);
            	}
                i++;
                
                String newProgString = Messages.getString("IttTraceParser.traceProgress1")+(i*100)/len+Messages.getString("IttTraceParser.traceProgress2"); //$NON-NLS-1$ //$NON-NLS-2$
                if (!progString.equals(newProgString))
                {
                	progString = newProgString;
                	
                	if (symbolFileParser == null)
                		progString +=Messages.getString("IttTraceParser.traceProgress3"); //$NON-NLS-1$
                		
                 }
            }

             amm.postProcess();
			
			ptd.functionResolvers = new FunctionResolver[]{amm};
		}
		else if (ptd.traceData instanceof IttTrace122)
		{
			BinaryReader122 br122 = new BinaryReader122((IttTrace122)ptd.traceData);
			PiInstrFunctionResolver pifr = new PiInstrFunctionResolver(br122,(IttTrace122)ptd.traceData,br122.parsedMapFileCount());
			ptd.functionResolvers = new FunctionResolver[]{pifr};
		}
		
		return ptd;
	}

  private void parseIttTrace(File f) throws Exception
  {
    int instrPerSample = 4;

    DataInputStream dis = new DataInputStream(new FileInputStream(f));
    //checks if the itt trace is valid
    
    byte[] traceArray = new byte[(int)f.length()];
    
    dis.readFully(traceArray);
    
    String traceStart = new String(traceArray,0,30);
    int offset = 0;
    if (traceStart.indexOf("Bappea_ITT") != -1 ) //trace version 1.10 or later //$NON-NLS-1$
    {
        offset = traceArray[0];
        traceVersion = new String(traceArray,1,offset);
        traceVersion = traceVersion.substring(traceVersion.indexOf("_")+1); //$NON-NLS-1$
        if (debug) System.out.println(Messages.getString("IttTraceParser.debugTraceVersion")+traceVersion);  //$NON-NLS-1$
        offset++;
    }
    else
    {
        traceVersion = "ITT pre 1.0"; //$NON-NLS-1$
        if (debug) System.out.println(Messages.getString("IttTraceParser.debugTraceVersion1.0")); //$NON-NLS-1$
    }

    //opens up to 1.10 version ITT traces.
    if (traceVersion.indexOf("ITT_V1.10") != -1 || traceVersion.equals("ITT pre 1.0")) //$NON-NLS-1$ //$NON-NLS-2$
    {
	   	try
		{
	   		for (int i=offset;i<traceArray.length-16;)
	   		{
	   			long magic = this.getInt32From(traceArray,i);
	   			long repeat = this.getInt32From(traceArray,i+4);
	   			
	   			if (magic == 0xbabbeaaa)
	   			{
	   				int valueHi = (int)(((repeat & 0xffff0000) >>> 16) & 0xffff);
	   				int valueLo = (int)(repeat & 0xffff);
	   				if (valueHi + valueLo == 0xffff)
	   				{
	   					//System.out.println("Performing a repeat of "+valueLo);
	   					for (int m=0;m<valueLo;m++)
	   					{
	   						IttSample s = (IttSample)samples.lastElement();
	   						IttSample newS = new IttSample(instrPerSample);
	   						
	   						// copy the fields, increase the synch time for each sample
	   						newS.checksum = s.checksum;
	   						newS.programCounter = s.programCounter;
	   						newS.sampleSynchTime = s.sampleSynchTime+1;
	   						//System.out.println("Sample: "+newS.sampleSynchTime);
	   						for (int k=0;k<s.instructions.length;k++)
	   							newS.instructions[k] = s.instructions[k];
	   						
	   						this.printError(traceArray,i,newS);
	   						
	   						samples.add(newS);
	   					}
	   					i+=8;
	   				}
	   				else
	   				{
	   					parseNormalSample(magic,repeat,instrPerSample,traceArray,i+8);
	   					i+=(instrPerSample+3)*4;
	   				}
	   			}
	   			else
	   			{
	   				parseNormalSample(magic,repeat,instrPerSample,traceArray,i+8);
	   				i+=(instrPerSample+3)*4;
	   			}
	   		}
		}
	   	catch (Exception e)
		{
	   		// end of trace
	   	}
    }
    else if (traceVersion.indexOf("ITT_V1.22") != -1 ) //$NON-NLS-1$ 
    {
    	this.trace122 = parse122IttTrace(traceArray, false);
    }
    else if (traceVersion.indexOf("ITT_V2.00") != -1 || traceVersion.indexOf("ITT_V2.01") != -1) //$NON-NLS-1$ //$NON-NLS-2$
    {
    	this.trace122 = parse122IttTrace(traceArray, true);
    }
    else
    {
        GeneralMessages.showErrorMessage(Messages.getString("IttTraceParser.unsupportedTrace")+traceVersion); //$NON-NLS-1$
    }
  }
   
  private IttTrace122 parse122IttTrace(byte[] traceArray, boolean isVersion2x)
  {
	  IttTrace122 trace = new IttTrace122();
	  
	  int ptr = 0;

	  // read the first header
	  byte length = traceArray[ptr++];
	  String txt = new String(traceArray,ptr,length);ptr+=length;
	
	  class SortableString implements Sortable
	  {
		  String string;
		  long value;
		  long startAddress;
		  long endAddress;
		  double sampleStartTime;
		  double sampleRemoveTime;
		  
		  public long valueOf()
		  {
			  return this.value;
		  }
	  }
	  Vector<SortableString> sortables = new Vector<SortableString>();
	  

	  
	  int adjust = isVersion2x ? 12 : 8;
	  
	  while(ptr < traceArray.length)
	  {
		  IttEvent122 event = new IttEvent122();
		  try
		  {
		  length = traceArray[ptr++];
		  txt = new String(traceArray,ptr,(length-adjust));
		  ptr+=(length-adjust);
		  event.binaryName = txt;
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
			  break;
		  }
		  
		  long adr = getUnsignedByte(traceArray[ptr++]);
		  adr |= (getUnsignedByte(traceArray[ptr++])<<8);
		  adr |= (getUnsignedByte(traceArray[ptr++])<<16);
		  adr |= (getUnsignedByte(traceArray[ptr++])<<24);
		  adr = (adr<<32)>>>32;
		  event.binaryLocation = adr;
		  
		  long len = getUnsignedByte(traceArray[ptr++]);
		  len |= (getUnsignedByte(traceArray[ptr++])<<8);
		  len |= (getUnsignedByte(traceArray[ptr++])<<16);
		  len |= (getUnsignedByte(traceArray[ptr++])<<24);
		  len = (len<<32)>>>32;
		  event.binaryLength = len;
		  
		  if (isVersion2x) {
			  long time = getUnsignedByte(traceArray[ptr++]);
			  time |= (getUnsignedByte(traceArray[ptr++])<<8);
			  time |= (getUnsignedByte(traceArray[ptr++])<<16);
			  time |= (getUnsignedByte(traceArray[ptr++])<<24);
			  time = (time<<32)>>>32;
			  event.eventTime = time / 1000.0;
		  } else {
			  event.eventTime = 0.0;
		  }

		  event.createBinary();
		  
		  trace.addEvent(event, isVersion2x);
	  }
	  
	  if (debug)
	  {
		  for(GenericEvent ge : trace.getEvents())
		  {
			  IttEvent122 ie = (IttEvent122)ge;
			  SortableString s = new SortableString();
			  if(isVersion2x){
				  s.string = Long.toHexString(ie.binaryLocation)+" - "+Long.toHexString(ie.binaryLocation+ie.binaryLength)+" \t "+ie.getBinary().getBinaryName()+" length:"+ie.binaryLength+"\tEvent Start Time: "+ie.eventTime+"\tEvent End Time: "+ie.eventEndTime; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  //$NON-NLS-4$ //$NON-NLS-5$  
			  }else{
				  s.string = Long.toHexString(ie.binaryLocation)+" - "+Long.toHexString(ie.binaryLocation+ie.binaryLength)+" \t "+ie.getBinary().getBinaryName()+" length:"+ie.binaryLength; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			  }
			  
			  s.value = ie.binaryLocation;
			  s.startAddress = ie.binaryLocation;
			  s.endAddress = ie.binaryLocation+ie.binaryLength;
			  s.sampleStartTime = ie.eventTime;
			  s.sampleRemoveTime = ie.eventEndTime;
			  sortables.add(s);
		  }
		  QuickSortImpl.sort(sortables);
		  Enumeration<SortableString> e = sortables.elements();
		  SortableString prev = null;
		  while (e.hasMoreElements())
		  {
			  SortableString s = (SortableString)e.nextElement();
			  if (prev != null)
			  {
				  if(isVersion2x){
					  if(prev.sampleRemoveTime <= 0){
						  prev.sampleRemoveTime = Double.MAX_VALUE;
					  }
					  if(s.sampleRemoveTime <= 0){
						  s.sampleRemoveTime = Double.MAX_VALUE;
					  }
					  if (s.startAddress < prev.endAddress && s.sampleStartTime < prev.sampleRemoveTime && s.sampleRemoveTime > prev.startAddress) {
						  System.out.println(Messages.getString("IttTraceParser.debugOverlapping")); //$NON-NLS-1$
						  System.out.println(Messages.getString("IttTraceParser.previous") + prev.string); //$NON-NLS-1$
						  System.out.println(Messages.getString("IttTraceParser.this") + s.string); //$NON-NLS-1$
					  }
				  }else{
					  if (s.startAddress < prev.endAddress) {
						  System.out.println(Messages.getString("IttTraceParser.debugOverlapping")); //$NON-NLS-1$
						  System.out.println(Messages.getString("IttTraceParser.previous") + prev.string); //$NON-NLS-1$
						  System.out.println(Messages.getString("IttTraceParser.this") + s.string); //$NON-NLS-1$
					  } 
				  }
			
			  }
			  System.out.println(s.string);
			  prev = s;
		  }
	  }
	  
	  return trace;
  }
  
  private int getUnsignedByte(byte b)
  {
	  return ((b<<24)>>>24);
  }
  
  private long getInt32From(byte[] traceArray,int i)
  {
  	long value = (traceArray[i]& 0xff) | ((traceArray[i+1] & 0xff) << 8) |
			((traceArray[i+2]& 0xff) << 16) | ((traceArray[i+3]&0xff) << 24);
  	return value;
  }
  
  private long getReversedInt32From(byte[] traceArray,int i)
  {
  	long value = ((traceArray[i]& 0xff) << 24) | ((traceArray[i+1] & 0xff) << 16) |
					((traceArray[i+2]& 0xff) << 8) | (traceArray[i+3]&0xff);
  	return value;
  }
  
  private void parseNormalSample(	long first,long second,
  										int instrPerSample,
										byte[] traceArray,int i) throws Exception
  {
    IttSample sample = new IttSample(instrPerSample);
    // change the byte order of the instructions
    sample.instructions[0] = ((first & 0xff) << 24) | 
							 ((first >> 8) & 0xff) << 16 |
							 ((first >> 16) & 0xff) << 8 |
							 ((first >> 24) & 0xff);

    sample.instructions[1]  = ((second & 0xff) << 24) | 
							  ((second >> 8) & 0xff) << 16 |
							  ((second >> 16) & 0xff) << 8 |
							  ((second >> 24) & 0xff);
    
    for (int k=2;k<instrPerSample;k++)
    {
    	// read in reverse byte order
    	sample.instructions[k] = this.getReversedInt32From(traceArray,i);
    	i+=4;
    }

    sample.sampleSynchTime = this.getInt32From(traceArray,i);
    i+=4;

    
    sample.checksum = this.getInt32From(traceArray,i);
    i+=4;


    sample.programCounter = (((this.getInt32From(traceArray,i)) << 32) >>> 32);
    i+= 4;

    this.printError(traceArray,i,sample);
    
    this.samples.add(sample);
  }

  private void printError(byte[] traceArray,int i,IttSample newSample)
  {
    if (this.samples.size() > 0)
    {
    	IttSample sample = (IttSample)this.samples.lastElement();
        if ( sample.sampleSynchTime != newSample.sampleSynchTime-1)
        {
        	System.out.println(Messages.getString("IttTraceParser.missingSample1")); //$NON-NLS-1$
            
            System.out.println(Messages.getString("IttTraceParser.missingSample2")+sample.sampleSynchTime); //$NON-NLS-1$
            System.out.println(Messages.getString("IttTraceParser.missingSample3")+Integer.toHexString((int)sample.programCounter)); //$NON-NLS-1$
            System.out.println(Messages.getString("IttTraceParser.missingSample4")+Integer.toHexString((int)sample.checksum)+Messages.getString("IttTraceParser.missingSample5")); //$NON-NLS-1$ //$NON-NLS-2$
            
            System.out.println(Messages.getString("IttTraceParser.missingSample6")+newSample.sampleSynchTime); //$NON-NLS-1$
            System.out.println(Messages.getString("IttTraceParser.missingSample7")+Integer.toHexString((int)newSample.programCounter)); //$NON-NLS-1$
            System.out.println(Messages.getString("IttTraceParser.missingSample8")+Integer.toHexString((int)newSample.checksum)+Messages.getString("IttTraceParser.missingSample9")); //$NON-NLS-1$ //$NON-NLS-2$

            int v = 0;
        	for (int k=i-48;k<i+48;k+=4)
        	{
        		v++;
        		String s = Long.toHexString(this.getInt32From(traceArray,k));
        		if (s.length() > 8) s = s.substring(s.length()-8,s.length());
        		else if (s.length()<8) {for (int g=0;g<8-s.length();g++){s="0"+s;}} //$NON-NLS-1$
        		
        		System.out.print("0x"+s+" "); //$NON-NLS-1$ //$NON-NLS-2$
        		if (v%4 == 0) System.out.print("\n"); //$NON-NLS-1$
        	}
        	
        }
    }
  }

  public Enumeration getSamples()
  {
    return this.samples.elements();
  }

  public GenericTrace getTrace()
  {
	  if (this.trace122 == null)
	  {
		  IttTrace trace = new IttTrace();
		  Enumeration sEnum = this.samples.elements();
		  while(sEnum.hasMoreElements())
		  {
			  IttSample s = (IttSample)sEnum.nextElement();
			  trace.addSample(s);
		  }	
		  return trace;
	  }
	  else
	  {
		  return this.trace122;
	  }
  }
}
