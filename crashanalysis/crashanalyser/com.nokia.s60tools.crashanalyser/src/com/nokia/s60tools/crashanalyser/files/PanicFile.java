/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.s60tools.crashanalyser.files;

import java.io.*;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import com.nokia.s60tools.crashanalyser.model.*;
import com.nokia.s60tools.crashanalyser.containers.Thread;
import com.nokia.s60tools.crashanalyser.data.*;

/**
 * An emulator panic file. 
 *
 */
public class PanicFile extends CrashAnalyserFile {
	
	// XML tags
	final static String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	final static String TAG_ROOT = "panic";
	final static String TAG_TIME = "time";
	final static String TAG_THREAD = "thread";
	final static String TAG_CATEGORY = "category";
	final static String TAG_CODE = "code";
	
	String nodeText = "";
	
	/**
	 * Constructor
	 * @param filePath file path to this panic file
	 * @param library error library
	 */
	protected PanicFile(String filePath, ErrorLibrary library) {
		super(filePath, library);
	}
	
	/**
	 * Returns the file type of this crash file.
	 * @return "Emulator panic"
	 */
	public String getFileType() {
		return "Emulator panic";
	}
	
	// No threads
	public List<Thread> getThreads() {
		return null;
	}
	
	/**
	 * Reads panic file
	 * @param file panic file
	 * @param library error library
	 * @return read panic file
	 */
	public static PanicFile read(String folder, ErrorLibrary library) {
		String panicFile = findFile(folder, CrashAnalyserFile.EMULATOR_PANIC_EXTENSION);
		
		// panic file doesn't exists
		if (panicFile == null) 
			return null;

		PanicFile file = new PanicFile(panicFile, library);
		file.doRead();
		return file;
	}
	
	@Override
	protected void doRead() {
		super.doRead();
		
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.parse(filePath, this);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void startElement(String arg0, String arg1, String arg2,	Attributes arg3) 
			throws SAXException {
		nodeText = "";
		super.startElement(arg0, arg1, arg2, arg3);
	}
	
	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		String s = String.copyValueOf(arg0, arg1, arg2);
		nodeText += s;
		super.characters(arg0, arg1, arg2);
	}	

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		if (TAG_TIME.equals(arg2)) {
			time = nodeText;
		} else if (TAG_THREAD.equals(arg2)) {
			threadName = nodeText;
		} else if (TAG_CATEGORY.equals(arg2)) {
			panicCategory = nodeText;
		} else if (TAG_CODE.equals(arg2)) {
			panicCode = nodeText;
		} else if (TAG_ROOT.equals(arg2)) {
			description = HtmlFormatter.formatEmulatorPanicDescription(this, errorLibrary);
		}
	}	
	
	/**
	 * Writes emulator panic data into an xml file
	 * @param timeStamp panic's time
	 * @param threadId panic's thread
	 * @param category panic category
	 * @param code panic code
	 */
	public static void WritePanicFile(String timeStamp, String threadId, String category, String code) {
		String folder = FileOperations.addSlashToEnd(DecoderEngine.getCrashFilesFolder());
		String fileName = "EmulatorPanic_" + timeStamp.replace(".","");
		folder += FileOperations.addSlashToEnd(fileName);
		if (!FileOperations.createFolder(folder))
			return;
		
		String file = folder + fileName + "." + CrashAnalyserFile.EMULATOR_PANIC_EXTENSION;
		
		try{
			// Create file 
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			String lineBreak = System.getProperty("line.separator");
			out.write(XML_DECLARATION + lineBreak);
			writeTag(TAG_ROOT, out, lineBreak);
			writeValue(TAG_TIME, out, timeStamp, lineBreak);
			writeValue(TAG_THREAD, out, threadId, lineBreak);
			writeValue(TAG_CATEGORY, out, category, lineBreak);
			writeValue(TAG_CODE, out, code, lineBreak);
			writeClosingTag(TAG_ROOT, out, lineBreak);

			// Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			e.printStackTrace();
		}		
	}
	
	static void writeTag(String tag, BufferedWriter out, String lineBreak) throws IOException {
		out.write("<" + tag + ">" + lineBreak);
	}

	static void writeClosingTag(String tag, BufferedWriter out, String lineBreak) throws IOException {
		out.write("</" + tag + ">" + lineBreak);
	}
	
	static void writeValue(String tag, BufferedWriter out, String value, String lineBreak) throws IOException {
		out.write("<" + tag + ">" + value + "</"+ tag + ">" + lineBreak);
	}	
}
