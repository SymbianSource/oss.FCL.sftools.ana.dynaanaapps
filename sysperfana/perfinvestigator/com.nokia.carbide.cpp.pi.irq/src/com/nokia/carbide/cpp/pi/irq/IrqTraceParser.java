/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.pi.irq;

import java.io.*;
import java.util.*;

import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;


/**
 * Class irq trace parser. 
 */
public class IrqTraceParser extends Parser
{
//  private String profilerVersion;
  private Vector completeIrqTrace;
  private int firstSample,lastSample;
  private long old1 = 0;
  private long old2 = 0;
  private long old3 = 0;
  private int sampleNum = 0;
  private int oldIrqLev1 = 0;
  private int oldIrqLev2 = 0;
    
  public IrqTraceParser(/*File irqFile, ProgressBar progressBar*/) throws Exception
  {
    this.completeIrqTrace = new Vector();
  }
  
  public ParsedTraceData parse(File f) throws Exception 
  {
    
    if(!f.exists()) //throw new IOException("GFC file does not exist");
	{
		setStateOk(false);
		return null;
	}
	else
    {
		/*if(progressBar != null) 
		{
			setProgressBarString("Analysing IRQ/SWI...");
		}*/
		
		FileInputStream fis = new FileInputStream(f);
		byte[] data = new byte[(int)f.length()];
		fis.read(data);
		//String version = getVersion(data);
		//System.out.println("IRQ trace version "+version);
		//profilerVersion = version;
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bais);
		
		this.traceVersion = this.getVersion(dis);
		if (this.traceVersion.indexOf("V1.20") != -1)
		{
			// start parsing
			try
			{
				int total = 0;
				while(true)
				{
					total += readEntries(dis);
					this.sampleNum++;
					//System.out.println("Sample #"+sampleNum+" "+this.completeIrqTrace.size());
					//System.out.println("******* Total now "+total+" "+Integer.toHexString(total));
			  	  	//System.in.read();
			  	  	//System.in.read();
				}
			}
			catch (EOFException eof)
			{
				//eof.printStackTrace();
			    ParsedTraceData ptd = new ParsedTraceData();
				ptd.staticData = null;
				ptd.traceData = this.getTrace();
				return ptd;
			}
			catch (Exception e)
			{
				System.out.println("Error in reading IRQ trace file");
				throw e;				
			}
	    }
		else
		{
			System.out.println("Unsupported IRQ version: "+this.traceVersion);
		    return null;
		}
    }
  	
  }
  
  public String getProfilerVersion()
  {
      return this.traceVersion;
  }
  
  private String getVersion(DataInputStream dis) throws IOException
  {
  	int length = dis.readUnsignedByte();
  	byte[] verArray = new byte[length];
  	dis.read(verArray);

  	String verString = new String(verArray);
//  	System.out.println("Version string: "+verString);
	if(verString.indexOf("Bappea") != -1)
		if(verString.indexOf("IRQ") != -1)
		{
  			int index = verString.indexOf("_");
  			String ver = verString.substring(index,length);
  			return ver;
  		}
	return("Unidentified");
  }
  
  private int readEntries(DataInputStream dis) throws Exception
  {
	int b0=0,b1=0,b2=0,b3=0;
	boolean swi = false;
  	int read = 0;
  	//System.out.println("-------------");
  	b0 = dis.readUnsignedByte();read++;
  	int length = b0; 
  	if(length == 0xff)
  	{
  		// length encoded in 3 bytes
  		b1 = dis.readUnsignedByte();read++;
  		b2 = dis.readUnsignedByte();read++;
  		b3 = dis.readUnsignedByte();read++;
  		length = b1;
  		length |= b2<<8;
  		length |= b3<<16;
  		//System.out.println("LONG length "+length);
  	}
//  	
//	System.out.println("SWI len "+Integer.toHexString(b0)+
//				 " "+Integer.toHexString(b1)+
//				 " "+Integer.toHexString(b2)+
//				 " "+Integer.toHexString(b3)+" "+length+" = 0x"+Integer.toHexString(length));
  	
  	while(length > 0)
  	{
  		int read_val = dis.readUnsignedByte();read++;length--;
		
  		//System.out.println("Length "+length);
  		
  		int repeat_val = 0;
  		int header = (read_val & (int)0xff);
  		int repeat = header>>>6;
  		
  		//System.out.println("Header "+Long.toHexString(header)+" "+Long.toBinaryString(header)+" repeat "+repeat);
  		
  		if(repeat == 0)
  		{
  			repeat_val = 0;
  			//System.out.println("no repeat "+Long.toHexString(repeat_val));
  		}
  		else if(repeat == 1)
  		{
  			repeat_val = (header & ((int)0x3F));
  			
  			//System.out.println("short repeat "+Long.toHexString(repeat_val));
  		}
  		else if(repeat == 2)
  		{
  			repeat_val = ((header & ((int)0x3F))<<8) | 
			(dis.readUnsignedByte());length--;read++;
  			
  			//System.out.println("medium repeat"+Long.toHexString(repeat_val));  		
  		}
  		else if(repeat == 3)
  		{
  			repeat_val = ((header & ((int)0x3F))<<24) | 
			(dis.readUnsignedByte() << 16) |
			(dis.readUnsignedByte() << 8) |
			(dis.readUnsignedByte() );length-=3;read+=3;
			/*
			for(int x=0;x<10;x++)
			{
				for(int y=0;y<8;y++)
				{
					System.out.println(Integer.toHexString(dis.readUnsignedByte()));
				}
				System.out.print("\n");
			}
			
			System.in.read();
			System.in.read();
			*/
  		}
  		
  		if(repeat_val > 0) 
  		{
  			addSwiRepeat(repeat_val);
  		}
  		else
  		{
  			int bytes_1 = (header & ((int)0x30))>>>4;
  			int bytes_2 = (header & ((int)0x0C))>>>2;
  			int bytes_3 = (header & ((int)0x03));
  			if(bytes_1 == 3) bytes_1 = 4;
  			if(bytes_2 == 3) bytes_2 = 4;
  			if(bytes_3 == 3) bytes_3 = 4;
  			
  			readSwiEntry(dis,bytes_1,bytes_2,bytes_3);
  			length-=(bytes_1+bytes_2+bytes_3);
  			read+=(bytes_1+bytes_2+bytes_3);		
  		}
  	}
  	
  	if(length == 0)
  	{
  		//Sytem.out.println("End of SWI, reading IRQ");
  	  	b0 = dis.readUnsignedByte();read++;
  	  	length = b0; 
  	  	if(length == 0xff)
  	  	{
  	  		// length encoded in 3 bytes
  	  		b1 = dis.readUnsignedByte();read++;
  	  		b2 = dis.readUnsignedByte();read++;
  	  		b3 = dis.readUnsignedByte();read++;
  	  		length = b1;
  	  		length |= b2<<8;
  	  		length |= b3<<16;
  	  		//System.out.println("LONG length "+length);
  	  	}
//  		System.out.println("IRQ len "+Integer.toHexString(b0)+
//				 " "+Integer.toHexString(b1)+
//				 " "+Integer.toHexString(b2)+
//				 " "+Integer.toHexString(b3)+" "+length+" = 0x"+Integer.toHexString(length));
  	  	
  	  	swi = true;
  	  	while(length > 0)
  	  	{
  	  		//System.out.println("Length "+length);
  	  		int firstByte = dis.readUnsignedByte();length--;read++;
  	  		
  	  		if(firstByte == 0xff)
  	  		{
  	  			// this is a repeat of the previous sample
  	  			int repeat = 	dis.readUnsignedByte() |
								(dis.readUnsignedByte()<<8) |
								(dis.readUnsignedByte()<<16);
  	  			//if(sampleNum == 5918 || sampleNum == 6137 || sampleNum == 7958)
  	  			//System.out.println(this.sampleNum+" IRQ Repeat of "+repeat+" L1:"+this.old_irq_lev1+" L2:"+this.old_irq_lev2);
  	  			this.addIrqRepeat(repeat);
  	  			
  	  			length-=3;read+=3;
  	  			
  	  			if(length > 0)
  	  			{
  	  				this.oldIrqLev1 = dis.readUnsignedByte();
  	  				this.oldIrqLev2 = dis.readUnsignedByte();
  	  				length-=2; read-=2;

  	  				this.addIrqSample();
  	  			}
  	  		}
  	  		else
  	  		{
  	  			this.oldIrqLev1 = firstByte;
  	  			this.oldIrqLev2 = dis.readUnsignedByte();
  	  			length--;read++;

  	  			this.addIrqSample();
  	  		}
  	  		
  			//System.out.println(this.sampleNum+" IRQ L1:"+this.old_irq_lev1+" L2:"+this.old_irq_lev2);
  	  	}
  	}
  	else
  	{
//  		System.out.println("Parse error "+Integer.toHexString(b0)+
//  										 " "+Integer.toHexString(b1)+
//  										 " "+Integer.toHexString(b2)+
//  										 " "+Integer.toHexString(b3)+" "+swi+" "+read);
  		
  		throw new Exception("Parse error "+Integer.toHexString(b0)+
  										 " "+Integer.toHexString(b1)+
  										 " "+Integer.toHexString(b2)+
  										 " "+Integer.toHexString(b3));
  	}

  	if(length == 0)
  	{
  		return read;
  	}
  	else
  	{
  		throw new Exception("Parse error");
  	}
  	
  }
     
  private void readSwiEntry(DataInputStream dis,int b1,int b2,int b3) throws Exception
  {
  	//System.out.println("b1: "+b1+" b2:"+b2+" b3:"+b3);
  
  	int value = 0;
  	int fValue = 0;
  	boolean neg = false;
	for(int i=0;i<4;i++) 
	{
		if(i<b1)
		{
			value = dis.readUnsignedByte();
			if((value & 0x80) > 0) neg = true;
			else neg = false;
			value = (value<<(8*i));
			fValue |= value;
			//System.out.println("v"+i+":"+Integer.toHexString(value));
		}
		else
		{
			if(neg == true) value = 0xff;
			else value = 0x00;
			value = (value<<(8*i));
			fValue |= value;
			//System.out.println("v"+i+":"+Integer.toHexString(value));
		}
	}
	//if(fValue == -1) System.out.println("1:"+fValue); 	
	this.old1 += fValue;

	value = 0;
	fValue = 0;
	neg = false;
	for(int i=0;i<4;i++)
	{
		if(i<b2)
		{
			value = dis.readUnsignedByte();
			if((value & 0x80) > 0) neg = true;
			else neg = false;
			value = (value<<(8*i));
			fValue |= value;
			//System.out.println("v"+i+":"+Integer.toHexString(value));
		}
		else
		{
			if(neg == true) value = 0xff;
			else value = 0x00;
			value = (value<<(8*i));
			fValue |= value;
			//System.out.println("v"+i+":"+Integer.toHexString(value));
		}
	}
	//if(fValue == -1) System.out.println("2:"+fValue); 	
	this.old2 += fValue;
	
	value = 0;
	fValue = 0;
	neg = false;
	for(int i=0;i<4;i++) 
	{
		if(i<b3)
		{
			value = dis.readUnsignedByte();
			if((value & 0x80) > 0) neg = true;
			else neg = false;
			value = (value<<(8*i));
			fValue |= value;
			//System.out.println("v"+i+":"+Integer.toHexString(value));
		}
		else
		{
			if(neg == true) value = 0xff;
			else value = 0x00;
			value = (value<<(8*i));
			fValue |= value;
			//System.out.println("v"+i+":"+Integer.toHexString(value));
		}
	}
	//if(fValue == -1) System.out.println("3:"+fValue); 	
	this.old3 += fValue;
	
	this.addSwiSample();
	/*
	System.out.println( "#"+this.sampleNum+" "+Integer.toHexString((int)old_1)+
						" "+Integer.toHexString((int)old_2)+
						" "+Integer.toHexString((int)old_3)+" "+sfp.getFunctionNameForAddress(((old_3<<32)>>>32)));
	*/
 	return;
  }
  
  private void addSwiSample()
  {
	  long temp_1 = ((this.old1 << 32) >>> 32);
	  long temp_2 = ((this.old2 << 32) >>> 32);
	  long temp_3 = ((this.old3 << 32) >>> 32)-4;
	  
	  IrqSample sample = new IrqSample(this.sampleNum, temp_1, temp_2, temp_3);
	  /*
	  String n1 = sfp.getFunctionNameForAddress(temp_3);
	  
	  if(n1.indexOf("WaitForAnyRequest") != -1)
	  {
		  for(int i=-256;i<256;i++)
		  {
			  String n2 = sfp.getFunctionNameForAddress(temp_1+i);
			  if(n2.indexOf("WaitForAnyRequest") != -1)
			  {
				  System.out.print("\n"+i);
				  
				  System.out.println("EXEC CALL : "+sfp.getFunctionNameForAddress(temp_3));
				  System.out.println("Made in : "+sfp.getFunctionNameForAddress(temp_1+i));
				  break;
			  }
		  }
	  } 
	  */
	  this.completeIrqTrace.add(sample);
  }
  
  private void addSwiRepeat(int amount)
  {
	  long temp_1 = ((this.old1 << 32) >>> 32);
	  long temp_2 = ((this.old2 << 32) >>> 32);
	  long temp_3 = ((this.old3 << 32) >>> 32)-4;
	  
	  /*
	  String n1 = sfp.getFunctionNameForAddress(temp_3);
	  
	  if(n1.indexOf("WaitForAnyRequest") != -1)
	  {
		  for(int i=-256;i<256;i++)
		  {
			  String n2 = sfp.getFunctionNameForAddress(temp_1+i);
			  if(n2.indexOf("WaitForAnyRequest") != -1)
			  {
				  System.out.print("\n"+i);
				  
				  System.out.println("EXEC CALL : "+sfp.getFunctionNameForAddress(temp_3));
				  System.out.println("Made in : "+sfp.getFunctionNameForAddress(temp_1+i));
				  break;
			  }
		  }
	  } 
	  */
	  
	  IrqSample sample = new IrqSample(this.sampleNum,temp_1,temp_2,temp_3);
	  sample.repeatCount = amount;
	  this.completeIrqTrace.add(sample);
  }

  private void addIrqSample()
  {
  	IrqSample sample = new IrqSample(this.sampleNum,this.oldIrqLev1,this.oldIrqLev2);
  	this.completeIrqTrace.add(sample);
  }
  
  private void addIrqRepeat(int amount)
  {
  	  	IrqSample sample = new IrqSample(this.sampleNum,this.oldIrqLev1,this.oldIrqLev2);
  	  	sample.repeatCount = amount; 
 	  	this.completeIrqTrace.add(sample);
  }
  
  private GenericTrace getTrace()
  {
  	Enumeration completeEnum = this.completeIrqTrace.elements();
  	IrqTrace trace = new IrqTrace();
  	
  	while(completeEnum.hasMoreElements())
  	{
  		IrqSample sample = (IrqSample)completeEnum.nextElement();
  		trace.addSample(sample);
  	}
  	return trace;
  }
}