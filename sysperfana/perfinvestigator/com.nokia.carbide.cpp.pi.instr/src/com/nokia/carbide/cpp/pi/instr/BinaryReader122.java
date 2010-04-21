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
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Path;

import com.nokia.carbide.cpp.internal.pi.model.Binary;
import com.nokia.carbide.cpp.pi.importer.SampleImporter;
import com.nokia.carbide.cpp.pi.importer.SampleImporter.PkgObyFile;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;
import com.nokia.carbide.cpp.pi.util.GuessAndFixPath;


/*
 * This class is a reader of OBY/PKG file for building two hashes
 * with the support of IttTrace22(the real trace reader)
 * 1. binary(host side) => map files hash for using map file
 * 2. string(host side) => binary hash for using ROFS symbol file
 * 
 * it reads OBY/PKG line by line and for host/target reference and 
 * update records of "Binary" from dynamic binary trace.
 * 
 */

public class BinaryReader122
{
	private boolean debug = false;
	private IttTrace122 ittTrace122;
	
	private Hashtable<Binary, MapFile> binaryToMapFile;
	private Hashtable<String, Binary> hostNameToBinary;	// so we can adjust ROFS symbol
	
	private Pattern stopPattern     = Pattern.compile("(?:\\p{Blank}*)(?:[S|s][T|t][O|o][P|p])(?:\\p{Blank}+(?!\\p{Blank}).*|\\p{Blank}*)"); //$NON-NLS-1$
	private Pattern commentPattern  = Pattern.compile("(?:\\p{Blank}*)(?:[R|r][E|e][M|m])(?:\\p{Blank}+(?!\\p{Blank}).*|\\p{Blank}*)"); //$NON-NLS-1$
	private Pattern fileSpecPattern = Pattern.compile("(?:\\p{Blank}*)(?:data|file|primary|secondary|variant|device|extension|dll|filecompress|fileuncompress)(?:(?:\\[.*?\\])?)(?:\\p{Blank}*=\\p{Blank}*)((?:\\S|[ ])+(?:\\S))(?:\\p{Blank}+)(?:\")((?:\\S|[ ])+(?:\\S))(?:\")(?:.*)"); //$NON-NLS-1$
	
	public BinaryReader122(IttTrace122 ittTrace122)
	{
		binaryToMapFile = new Hashtable<Binary, MapFile>();
		setHostNameToBinary(new Hashtable<String,Binary>());
		
		this.ittTrace122 = ittTrace122;
		
		
		for(PkgObyFile currentFile : SampleImporter.getInstance().getPkgObyFilesList()) {
			String fileName = currentFile.fileAbsolutePath;
			
			if (fileName.toLowerCase().endsWith(".oby") || fileName.toLowerCase().endsWith(".iby")) //$NON-NLS-1$ //$NON-NLS-2$
				parseAndProcessObyFile(fileName);
			else if (fileName.toLowerCase().endsWith(".pkg")) //$NON-NLS-1$
			{
				addFilesInPkg(fileName);
			}
			else
			{
		    	GeneralMessages.showErrorMessage(Messages.getString("BinaryReader122.wrongEKA2FileType1")+fileName+Messages.getString("BinaryReader122.wrongEKA2FileType2")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}		
	}
	
	public MapFile getMapFileForBinary(Binary b)
	{	
		MapFile mf = binaryToMapFile.get(b);
		if(mf == null){
			String binaryName = b.getBinaryName();
			try{
				binaryName = new Path(b.getBinaryName()).lastSegment();
			}catch (Exception e) {
				// use defaults
			}		
			Iterator<Entry<Binary, MapFile>> iterator = binaryToMapFile.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<Binary, MapFile> entry = iterator.next();			
				String name = entry.getKey().getBinaryName();
				try{
					name = new Path(name).lastSegment();
				}catch (Exception e) {
					// use defaults
				}
				if(name.equals(binaryName)){
					b.setBinaryName(entry.getKey().getBinaryName());
					mf = entry.getValue();
					break;
				}
			}
		}
		return mf;
	}
	
	public Binary getBinaryForHostName(String s)
	{
		return this.getHostNameToBinary().get(s);
	}
	
	  public void addFilesInPkg(String pkgName)
	  {
		  long lineNumber = 0;
		  File pkgFile = new File(pkgName);
		  if (pkgFile.exists() && !pkgFile.isDirectory())
		  {
			  String localFile = ""; //$NON-NLS-1$
			  String mapName = ""; //$NON-NLS-1$
			  try
			  {
				  BufferedReader br = new BufferedReader(new FileReader(pkgFile));
				  String line;
				  
				  while((line = br.readLine()) != null)
				  {
					  ++lineNumber;
					  
					  // find comment lines, which are started with a ';'
					  int first = line.indexOf(";"); //$NON-NLS-1$
					  
					  if (first != -1) {
						  int i = 0;
						  for (; i < first; i++)
						  {
							  if (line.charAt(i) != ' ' && line.charAt(i) != '\t')
								  break;
						  }
						  if (i == first)
							  continue;
					  }
					  
					  //System.out.println(line);
					  first = line.indexOf("\""); //$NON-NLS-1$
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
						  Binary binary = null;
						  
						  localFile = line.substring(first+1,second);
						  String remoteFile = line.substring(third+3,fourth);
						  
						  // .PKG is referring everything as root relative,
						  // sometime there is reference to $(EPOCROOT) too
						  // let's try to guess what it was. I hate windows drive letter
						  if (localFile.charAt(0) == '\\') {
							  localFile = GuessAndFixPath.fixPath(localFile, "", pkgName);	//$NON-NLS-1$
						  } else if (localFile.charAt(1) != ':') {
							  // not absolute drive, resolve relative to PKG file
							  File relativeMapFile = new File(pkgFile.getParent(), localFile);
							  if (relativeMapFile.exists()) {
								  localFile = relativeMapFile.getCanonicalPath();
							  }
						  }
						  
						  String lowerLocal = localFile.toLowerCase();
						  if (BinaryReader.checkFileExtension(lowerLocal))
						  {
							  if (debug)System.out.println(Messages.getString("BinaryReader122.addingPkgFile")+lowerLocal); //$NON-NLS-1$
			        		  
				        	  binary = this.ittTrace122.getBinaryForFileName(remoteFile);
				        	  if (debug)System.out.println(Messages.getString("BinaryReader122.gotBinaryPkgFile")+lowerLocal); //$NON-NLS-1$
				        	  
					          if (binary != null)
					          {
					        	  if (debug)System.out.println(Messages.getString("BinaryReader122.foundPkgBinary1")+binary.getBinaryName()+Messages.getString("BinaryReader122.foundPkgBinary2")+localFile); //$NON-NLS-1$ //$NON-NLS-2$
				        		  binary.setBinaryName(localFile);
				        		  getHostNameToBinary().put(binary.getBinaryName(), binary);
					          }
							  
					          mapName = localFile+".map"; //$NON-NLS-1$
					          File mapFile = new File(mapName);
					          MapFile mf = new MapFile(mapFile, pkgFile.toString(), lineNumber);
							  if (mapFile.exists() && binary != null)
			        		  {
			        			  try
			        			  {
			        				  if (binary != null)
			        				  {
							        	  if (debug)System.out.println(Messages.getString("BinaryReader122.canGetBinaryFrom1")+binary.getBinaryName()+Messages.getString("BinaryReader122.canGetBinaryFrom2")+localFile); //$NON-NLS-1$ //$NON-NLS-2$
			        					  binaryToMapFile.put(binary,mf);
			        				  }
			        				  else
			        				  {
			        					  if (debug)System.out.println(Messages.getString("BinaryReader122.cannotCreatePkgFileFrom") + mapName); //$NON-NLS-1$
			        				  }
			        			  }
			        			  catch (Exception e)
			        			  {
			        				  if (debug)System.out.println(Messages.getString("BinaryReader122.pkgNotOK")+remoteFile); //$NON-NLS-1$
			        			  }
			        		  }
						  }
					  }
				  } // while((line = br.readLine()) != null)
			  } catch (Exception e) {
				  // either malformed PKG or we are not robust enough
				  GeneralMessages.showErrorMessage (Messages.getString("BinaryReader122.pkgMalform")); //$NON-NLS-1$
			  }
		  }
	  }
	
	public void parseAndProcessObyFile(String obyFile) {
		File f = new File (obyFile);
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String line = br.readLine();
			long lineNumber = 1;
			
			while (line != null) {
				Matcher stopMatcher = stopPattern.matcher(line);
				if (stopMatcher.matches()) {
					break;
				}
				Matcher commentMatcher = commentPattern.matcher(line); //$NON-NLS-1$
				if (!commentMatcher.matches()) {
					// case is sensitive in OBY keywords
					// only read file spec from OBY
					Matcher fileSpecMatcher = fileSpecPattern.matcher(line); //").matcher(line); //$NON-NLS-1$
					if (fileSpecMatcher.matches()) {
						Binary binary = ittTrace122.getBinaryForFileName(fileSpecMatcher.group(2).trim());
						if (binary != null) {
							binary.setBinaryName(fileSpecMatcher.group(1).trim());
							getHostNameToBinary().put(binary.getBinaryName(), binary);
							
							String pcFileName = fileSpecMatcher.group(1).trim();
							// .OBY is referring everything as root relative,
							// sometime there is reference to $(EPOCROOT) too
							// let's try to guess what it was. I hate windows drive letter
							if (pcFileName.charAt(0) == '\\') {
								pcFileName = GuessAndFixPath.fixPath(pcFileName, SampleImporter.getInstance().getRomEpocroot(), ""); //$NON-NLS-1$ //$NON-NLS-2$
							}
							if (pcFileName.endsWith("\"")) //$NON-NLS-1$
								pcFileName = pcFileName.substring(0, pcFileName.length() - 1);
							
							pcFileName += ".map"; //$NON-NLS-1$
							File pcFile = new File(pcFileName);
							if (pcFile.exists())
							{
								MapFile mf = new MapFile(pcFile, obyFile, lineNumber);
								binaryToMapFile.put(binary,mf);
							} else {
								  // .map not found
								  if(pcFileName.endsWith(".exe.map") || //$NON-NLS-1$
										  pcFileName.endsWith(".dll.map") || //$NON-NLS-1$
										  pcFileName.endsWith(".ldd.map") || //$NON-NLS-1$
										  pcFileName.endsWith(".pdd.map") || //$NON-NLS-1$
										  pcFileName.endsWith(".app.map"))  //$NON-NLS-1$
								  {
									  String myMessage = Messages.getString("BinaryReader122.map.file") +  pcFileName + Messages.getString("BinaryReader122.not.found"); //$NON-NLS-1$ //$NON-NLS-2$
									  if (obyFile != null && obyFile.length() > 0) {
										  myMessage += Messages.getString("BinaryReader122.referenced.by") + obyFile; //$NON-NLS-1$
									  }
									  if (lineNumber > 0) {
										  myMessage += Messages.getString("BinaryReader122.line.number") + lineNumber; //$NON-NLS-1$
									  }

									  // if it is code binary...
								      GeneralMessages.PiLog(myMessage, GeneralMessages.WARNING);
								  }
							}
						}
						else if (debug) {
							System.out.print(fileSpecMatcher.group(1));
							System.out.print("|"); //$NON-NLS-1$
							System.out.print(fileSpecMatcher.group(2));
							System.out.println(""); //$NON-NLS-1$
						}
					}
				}
				
				line = br.readLine();
				++lineNumber;
			}
		} catch (EOFException e) {
			// good, that's the end of file, bail out peacefully
		} catch (IOException e) {
			GeneralMessages.PiLog(Messages.getString("BinaryReader122.IOException.on") + obyFile, GeneralMessages.ERROR, e); //$NON-NLS-1$
		}
	}
	
	public void setHostNameToBinary(Hashtable<String, Binary> hostNameToBinary) {
		this.hostNameToBinary = hostNameToBinary;
	}

	public Hashtable<String, Binary> getHostNameToBinary() {
		return hostNameToBinary;
	}
	
	public int parsedMapFileCount() {
		return this.binaryToMapFile.size();
	}
}
