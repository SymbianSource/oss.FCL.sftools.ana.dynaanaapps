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
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.pi.importer.SampleImporter;
import com.nokia.carbide.cpp.pi.importer.SampleImporter.PkgObyFile;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;
import com.nokia.carbide.cpp.pi.util.GuessAndFixPath;


public class BinaryReader
{
  private boolean debug = false;
  private Vector<File> files;
  private Hashtable<String, ProcessedBinary> processedFiles;

  public BinaryReader() throws Exception
  {
    files = new Vector<File>();
    processedFiles = new Hashtable<String, ProcessedBinary>();

    for (PkgObyFile currentFile : SampleImporter.getInstance().getPkgObyFilesList())
    {    	
    	{
    		String epocRoot = null;
    	    File epoc32;
    		
    	    if (currentFile.epocRoot.endsWith("\\")) //$NON-NLS-1$
    	    {
    	    	epocRoot = currentFile.epocRoot.substring(0, currentFile.epocRoot.length() - 1);
    	    }
    	    else
    	    {
    	    	epocRoot = currentFile.epocRoot;
    	    }
    	    epoc32 = new File(epocRoot + "\\epoc32"); //$NON-NLS-1$

    	    if (!epoc32.exists() || !epoc32.isDirectory()) {
    	    	GeneralMessages.showErrorMessage(Messages.getString("BinaryReader.cannotFindEpoc32FromRoot")+epoc32.toString()); //$NON-NLS-1$
    	    	throw new Exception (Messages.getString("BinaryReader.cannotFindEpoc32FromRoot")+epoc32.toString()); //$NON-NLS-1$
    	    }
    	}
    	
    	if (currentFile.fileAbsolutePath.toLowerCase().endsWith(".oby") || currentFile.fileAbsolutePath.toLowerCase().endsWith(".iby")) //$NON-NLS-1$ //$NON-NLS-2$
    		addFilesInOby(currentFile.fileAbsolutePath, currentFile.epocRoot);
    	else if (currentFile.fileAbsolutePath.toLowerCase().endsWith(".pkg")) //$NON-NLS-1$
    		addFilesInPkg(currentFile.fileAbsolutePath, currentFile.epocRoot);
    	else
    		this.addSingleFile(currentFile.fileAbsolutePath);
    }
    refreshFileTable();
  }
  public BinaryReaderResult findSequence(IttSample sample)
  {
    return findSequence(sample,(ProcessedBinary)null);
  }

  public BinaryReaderResult findSequence(IttSample sample, Binary b)
  {
    if (this.processedFiles.containsKey(b.getBinaryName()))
    {
      ProcessedBinary pf = (ProcessedBinary)this.processedFiles.get(b.getBinaryName());
      return this.findSequence(sample,pf);
    }
    return null;
  }
  
  public ProcessedBinary getProcessedBinaryForName(String name)
  {
  	ProcessedBinary pb = (ProcessedBinary)this.processedFiles.get(name);
  	
  	if (pb != null)
  	{
  		return pb;
  	}
  	return null;
  }
  
  public boolean checkSampleInBinary(IttSample sample,Binary binary,int differencesAllowed)
  {
  	ProcessedBinary pb = this.getProcessedBinaryForName(binary.getBinaryName());
  	if (pb == null)
  	{
  		System.out.println(Messages.getString("BinaryReader.cannotFindBinary")+binary.getBinaryName()); //$NON-NLS-1$
		return false;
  	}
  	
  	if (binary.getStartAddress() <= sample.programCounter &&
  	   binary.getStartAddress()+binary.getLength() >= sample.programCounter)
  	{
  		// the program counter value indicates that the sample
  		// is within the binary
  		long[] reversedInstructions = sample.reversedInstructions();
  		int matches = 0;
  		for (int i=0;i<reversedInstructions.length;i++)
  		{
  			
  			long instruction = reversedInstructions[i];
  			long offset = (long)(sample.programCounter-binary.getStartAddress());

  			if (offset < binary.getLength())
  			{
  				// add the amount of bytes that match in the next 4 bytes
  				matches += this.testNextFourBytes((int)offset+(i*4),instruction,pb);
  			}
  		}	
  		
  		if (matches >= (reversedInstructions.length*4 - differencesAllowed))
  		{
  			// binary matches with the sample
  			// within the binary
  			return true;
  		}
  	}
  	
  	// the sample is not within the binary memory area
  	return false;
  }

  public BinaryReaderResult findSequence(IttSample sample,ProcessedBinary processedFile)
  {
    long[] sequence = sample.reversedInstructions();

    Vector possibleBinaries  = new Vector();
    Vector checksumValues = new Vector();

    if (sequence.length != 0)
    {
      int first = (int) ((sequence[0] & (long)0xff000000) >> 24);
      int second = (int) ((sequence[0] & (long)0x00ff0000) >> 16);
      int third = (int) ((sequence[0] & (long)0x0000ff00) >> 8);
      int fourth = (int) ((sequence[0] & (long)0x000000ff));

      // this is for searching only one binary
      Vector tempVec = null;
      Enumeration enumer = null;

      if (processedFile != null)
      {
        tempVec = new Vector();
        tempVec.add(processedFile);
        enumer = tempVec.elements();
      }
      else
      {
        enumer = this.processedFiles.elements();
      }

      while (enumer.hasMoreElements())
      {
        ProcessedBinary pf = (ProcessedBinary) enumer.nextElement();

        int[] indicesToCheck = pf.getIndicesForSequence(first);

        if (indicesToCheck != null)
        {
          for ( int k = 0;k<indicesToCheck.length;k++)
          {
            int i = indicesToCheck[k];

            int match = 0;
            
            if (i < pf.getLength() - 5)
            {
              if (pf.data[i] == (byte) first) match++;
              
              if (pf.data[i + 1] == (byte) second) match++;
              
              if (pf.data[i + 2] == (byte) third) match++;
              
              if (pf.data[i + 3] == (byte) fourth) match++;
                    
              // if at least three of the previous four bytes did match
              if (match >= 3)
                    {
                      for (int s = 1; s < sequence.length; s++)
                      {

                      	// add the match value with one if three of the four bytes
                      	// in the next 4 bytes match
                      	match += testNextFourBytes(i + s * 4, sequence[s], pf);
                      }
                      
                      if (match >= (sequence.length*4)-2)
                      {
                        // check the checksum
                        // go back from the current position another 16 bytes
                        // and calculate the xor checksum for 11 instructions
                        long checksum = pf.calculateXorChecksum(i+12,8);

                        // this value should match with the last value in the
                        // search pattern
                        if ( (checksum & 0xffffffff) == (sample.checksum & 0xffffffff) )
                        {

                          // add this location to possible binaries
                          Binary b = new Binary(pf.getBinaryName());
                          b.setLength(pf.getLength());
                          b.setOffsetToCodeStart(pf.getOffsetToCodeStart());
                          b.setType(pf.getType());

                          // binary start address is the this address - this offset
                          b.setStartAddress(sample.programCounter-i);
                          possibleBinaries.add(b);

                          //System.out.println (	"CHECKSUM MATCH at "+pf.binaryName+"@0x"+Long.toHexString(b.startAddress-b.offsetToCodeStart)+
                          //						" pos:"+i+"!! Checksum:"+checksum+" "+pf.file.getAbsolutePath());

                          
                          // this is a checksum match
                          checksumValues.add(Boolean.TRUE);
                        }
                        else
                        {

                          // add this location to possible binaries

                          Binary b = new Binary(pf.getBinaryName());
                          b.setLength(pf.getLength());
                          b.setOffsetToCodeStart(pf.getOffsetToCodeStart());
                          b.setType(pf.getType());

                          // binary start address is this address - this offset
                          b.setStartAddress(sample.programCounter-i);
                          possibleBinaries.add(b);

                          //System.out.println(	"MATCH at "+pf.binaryName+"@0x"+Long.toHexString(b.startAddress-b.offsetToCodeStart)+
                          //						" pos:"+i+"!! "+pf.file.getAbsolutePath());
                          
                          // this one is not a checksum match
                          checksumValues.add(Boolean.FALSE);
                        }
                      }
                    }
            }
          }
        }
      }
    }
    BinaryReaderResult brr = new BinaryReaderResult();

    brr.checksumValues = (Boolean[])checksumValues.toArray(new Boolean[checksumValues.size()]);
    brr.possibleBinaries = (Binary[])possibleBinaries.toArray(new Binary[possibleBinaries.size()]);

    return brr;
    //System.out.println("Finished!");
  }


  public void printBinaryFromOffset(ProcessedBinary pf, int offset, int length)
  {
    if (pf.getLength() < offset+length) return;
    int c = 0;

    for (int i=offset;i<offset+length;i++)
    {
      String hex = Integer.toHexString((int)pf.data[i] & 0xff);
      if (hex.length() == 1) hex = "0"+hex; //$NON-NLS-1$

      if (c%4 == 0) System.out.print(" "); //$NON-NLS-1$
      if (c%40 == 0) System.out.print("\n"); //$NON-NLS-1$

      System.out.print(hex);
      c++;
    }
  }
  
  private int testNextFourBytes(int offset,long data, ProcessedBinary pf)
  {
      if (data == -1) return 4;

      int first = (int) ((data & (long)0xff000000) >> 24);
      int second = (int) ((data & (long)0x00ff0000) >> 16);
      int third = (int) ((data & (long)0x0000ff00) >> 8);
      int fourth = (int) ((data & (long)0x000000ff));

      int matches = 0;
      
      if (pf.getLength() > offset+4)
      {
        if (pf.data[offset] == (byte)first) matches++; 
        
        if (pf.data[offset+1] == (byte)second) matches++;
        
        if (pf.data[offset+2] == (byte)third) matches++;
        
        if (pf.data[offset+3] == (byte)fourth) matches++;
      }
      
      return matches;
  }

  public void printBinary(String fileName)
  {
    this.printBinary(fileName,0,-1);
  }

  public void printBinary(String fileName,int startOffset,int length)
  {
  	System.out.println(binaryToString(fileName,startOffset,length));
  }

  public String binaryToString(String fileName,int startOffset,int length)
  {
  	String resultString = ""; //$NON-NLS-1$
  	
    int counter = 1;
    if (this.processedFiles.containsKey(fileName))
    {
      ProcessedBinary pf = (ProcessedBinary)this.processedFiles.get(fileName);
      if (length == -1)
        {
          startOffset = 0;
          length = pf.getLength();
        }

      for (int i=startOffset;i<startOffset+length;i++)
      {
        String hex = Integer.toHexString((int)pf.data[i] & 0xff);
        if (hex.length() == 1) hex = "0"+hex; //$NON-NLS-1$

        resultString+=hex;

        if (counter%4 == 0) resultString+=" "; //$NON-NLS-1$
        if (counter%40 == 0) resultString+="\n"; //$NON-NLS-1$

        counter++;
      }
    }
    
    return resultString;
  }

  
  public String getFunctionName(String binaryName, long offset)
  {
  	ProcessedBinary pb = (ProcessedBinary)this.processedFiles.get(binaryName);
  	if (pb != null)
  	{
  		return pb.getFunctionNameForOffset(offset);
  	}
  	else
  	{
  		return Messages.getString("BinaryReader.binaryNotFound1")+binaryName+Messages.getString("BinaryReader.binaryNotFound2"); //$NON-NLS-1$ //$NON-NLS-2$
  	}
  }
  
  public Function getFunction(String binaryName,long offset)
  {
  	ProcessedBinary pb = (ProcessedBinary)this.processedFiles.get(binaryName);
  	if (pb != null)
  	{
  		return pb.getFunctionForOffset(offset);
   	}
  	else
  	{
  		return null;
  	}
  }
  
  public long getFunctionStartOffsetFromBinaryStart(String binaryName,String functionName)
  {
  	ProcessedBinary pb = (ProcessedBinary)this.processedFiles.get(binaryName);
  	if (pb != null)
  	{
  		return pb.getOffsetFromBinaryStartForFunction(functionName);
  	}
  	else
  	{
  		return -1;
  	}
  }

  public void refreshFileTable()
  {
    long length = 0;
    int addedFiles = 0;

    if (debug) System.out.println(this.files.size()+Messages.getString("BinaryReader.filesFound")); //$NON-NLS-1$
    if (this.files.size() > 0)
    {
      Enumeration enumer = files.elements();
      while (enumer.hasMoreElements())
      {
        File f = (File) enumer.nextElement();
        String name = f.getAbsolutePath();
        name = name.substring(name.indexOf(File.separator),name.length());

        if (!(this.processedFiles.containsKey(name) &&
              (((ProcessedBinary)this.processedFiles.get(name)).getLength() == f.length())))
        {
          try
          {
            ProcessedBinary pf = processFile(f);
            //System.out.println(f.getName());
            this.processedFiles.put(pf.getBinaryName(), pf);
            length += pf.getLength();
            addedFiles++;
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
        else
        {
          System.out.println(Messages.getString("BinaryReader.fileAlreadyPresent1")+f.getName()+Messages.getString("BinaryReader.fileAlreadyPresent2")); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }

    this.files.clear();

    if (debug) System.out.println(Messages.getString("BinaryReader.bytesProcessed1")+length+Messages.getString("BinaryReader.bytesProcessed2")+addedFiles+Messages.getString("BinaryReader.bytesProcessed3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  private ProcessedBinary processFile(File f) throws Exception
  {
    return new ProcessedBinary(f);
  }

  public void addFilesInOby(String obyFile, String epocroot) throws Exception
  {
    File file = new File(obyFile);
    if (!file.exists())
    {
      throw new Exception(Messages.getString("BinaryReader.cannotFindObyFile")); //$NON-NLS-1$
    }

    BufferedReader br = new BufferedReader(new FileReader(file));

    String line = br.readLine();
    
    while(line != null)
    {
        line = line.trim(); 
        
        if (line.endsWith("\""))  //$NON-NLS-1$
        {        
        	line = line.replaceAll("\"",""); //$NON-NLS-1$ //$NON-NLS-2$
        	line.trim();
        }
    	
        int start = line.indexOf("="); //$NON-NLS-1$
//        start = line.indexOf("\\", start);
        start = line.indexOf(System.getProperty(Messages.getString("BinaryReader.fileSeparatorProperty")), start); //$NON-NLS-1$
//        System.getProperty("file.separator")

        int space = line.indexOf(" ",start+2); //$NON-NLS-1$
        int tab = line.indexOf("\t",start+2); //$NON-NLS-1$
        int end = 0;

        if (space < 0 && tab < 0)
          end = line.length();
        else if (space > 0 && tab > 0 && space > tab)
          end = tab;
        else if (space > 0 && tab > 0 && tab > space)
          end = space;
        else if (space < 0 && tab > 0)
          end = tab;
        else if (space > 0)
          end = space;
        else
          start = -1;

        if (start >= 0)
        {
          String lowCase = line.toLowerCase();
		  
          if (checkFileExtension(lowCase) == false) start = -1;
		                    	
          if (line.indexOf("rem") >= 0) //$NON-NLS-1$
          {
            if (line.indexOf("rem") <= start) //$NON-NLS-1$
              start = -1;
          }
          else if (line.indexOf("romname") >= 0) //$NON-NLS-1$
          {
            if (line.indexOf("romname") <= start) //$NON-NLS-1$
              start = -1;
          }
        }

        if (start >= 0 && end > start)
        {
          String fileNameLine = line.substring(start, end);
          if (fileNameLine.endsWith("\"")) //$NON-NLS-1$
            fileNameLine = fileNameLine.substring(0, fileNameLine.length() - 1);

          addFile(fileNameLine, epocroot);
          //System.out.println("ADDED "+fileNameLine);
        }
        else
        {
        	//if (line.indexOf(".rsc") == -1 && !line.startsWith("rem") && line.length() > 3)
        	//System.out.println("SKIPPED "+line);
        }

        line = br.readLine();
        //System.out.println("READ "+line);
      }
    br.close();

  }
  
  public static boolean checkFileExtension(String lowCase)
  {
      if (lowCase.endsWith(".rsc")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".aif")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".dll55l")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".dof")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".mid")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".rng")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".ini")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".awb")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".dat")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".txt")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".mbm_rom")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".xml")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".dtd")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".snm")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".smil")) return false; //$NON-NLS-1$
      else if (lowCase.endsWith(".sis")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".skn")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".lnk")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".gdr")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".esk")) return false;	   //$NON-NLS-1$
      else if (lowCase.endsWith(".ota")) return false;	  //$NON-NLS-1$
	  else if (lowCase.endsWith(".cfg")) return false;	  //$NON-NLS-1$
	  else if (lowCase.endsWith(".wav")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".mp3")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".amr")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".pcm")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".mbm")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".bmp")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".aac")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".png")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".hlp")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".jpg")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".dic")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".mask")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".db")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".mxmf")) return false; //$NON-NLS-1$
	  else if (lowCase.endsWith(".gif")) return false;          //$NON-NLS-1$

	  else return true;
  }
  
  public void addFilesInPkg(String pkgName, String epocroot) throws IOException
  {
	  File pkgFile = new File(pkgName);
	  if (pkgFile.exists() && !pkgFile.isDirectory())
	  {
		  try
		  	{
			  BufferedReader br = new BufferedReader(new FileReader(pkgFile));
			  
			  while(true)
			  {
				  String line = br.readLine();
				  //System.out.println(line);
				  int first = line.indexOf("\""); //$NON-NLS-1$
				  int second = -1;
				  int third = -1;
				  int fourth = -1;
				  
				  if (first != -1)
					  second = line.indexOf("\"",first+1); //$NON-NLS-1$
				  
				  if (second != -1)
					  third = line.indexOf("\"!:",second+1); //$NON-NLS-1$
				  
				  if (third != -1)
					  fourth = line.indexOf("\"",third+1); //$NON-NLS-1$
				  
				  //System.out.println(first+" "+second+" "+third+" "+fourth);
				  if (fourth != -1)
				  {
					  String localFile = line.substring(first+1,second);
//					  String remoteFile = line.substring(third+3,fourth);
					  
					  // .PKG is referring everything as root relative,
					  // sometime there is reference to $(EPOCROOT) too
					  // let's try to guess what it was. I hate windows drive letter
					  if (localFile.charAt(0) == '\\') {
						  localFile = GuessAndFixPath.fixPath(localFile, epocroot, pkgName);
					  }
					  
					  String lowerLocal = localFile.toLowerCase();
					  if (checkFileExtension(lowerLocal))
					  {
						  System.out.println(Messages.getString("BinaryReader.addedPkgFile")+lowerLocal); //$NON-NLS-1$
						  this.addFile(lowerLocal, epocroot);
					  }
				  }
			  }
		  	}
		  catch (Exception e)
		  {
			  
		  }
	  }
  }
  
  public void addSingleFile(String fileName)
  {
  	File file = new File(fileName);
  	this.addFile(file); 
  	this.refreshFileTable();
  }
  
  private void addFile(String fileName, String epocroot)
  {
	  File f = new File(epocroot + fileName);
    if (!f.exists()) System.out.println(Messages.getString("BinaryReader.epocRootFileNonexistent1")+fileName+Messages.getString("BinaryReader.epocRootFileNonexistent2")); //$NON-NLS-1$ //$NON-NLS-2$
    else
      {
        addFile(f);
        //System.out.println("Added file "+fileName+" length "+f.length());
      }

  }

  private void addFile(File file)
  {
    if (file.exists() && !file.isDirectory())
    {
      //System.out.println("ADDED"+file.getName());
      this.files.add(file);
    }
    else
    {
      System.out.println(Messages.getString("BinaryReader.fileNonexistent1")+file.getAbsolutePath()+Messages.getString("BinaryReader.fileNonexistent2")); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public static void main(String a[]) throws Exception
  {
	  System.out.println("Starting"); //$NON-NLS-1$
	  try
	  {
	    	GeneralMessages.showErrorMessage("BinaryReader Internal Error"); //$NON-NLS-1$
	    	throw new Exception ("BinaryReader Internal Error"); //$NON-NLS-1$
	  }
	  catch (Exception e)
	  {
		  e.printStackTrace();
	  }
  }
  
}
