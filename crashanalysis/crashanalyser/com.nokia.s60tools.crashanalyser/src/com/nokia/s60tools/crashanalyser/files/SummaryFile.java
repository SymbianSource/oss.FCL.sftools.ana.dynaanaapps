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

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.w3c.dom.*;

import com.nokia.s60tools.crashanalyser.containers.OstTrace;
import com.nokia.s60tools.crashanalyser.containers.RegisterDetails;
import com.nokia.s60tools.crashanalyser.containers.CodeSegment;
import com.nokia.s60tools.crashanalyser.containers.Message;
import com.nokia.s60tools.crashanalyser.containers.EventLog;
import com.nokia.s60tools.crashanalyser.containers.Process;
import com.nokia.s60tools.crashanalyser.containers.RegisterSet;
import com.nokia.s60tools.crashanalyser.containers.Register;
import com.nokia.s60tools.crashanalyser.containers.Summary;
import com.nokia.s60tools.crashanalyser.containers.Symbol;
import com.nokia.s60tools.crashanalyser.containers.Thread;
import com.nokia.s60tools.crashanalyser.containers.Stack;
import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.model.*;
import com.nokia.s60tools.crashanalyser.plugin.CrashAnalyserPlugin;

/**
 * A summary file is a crash file which has not been fully decoded.
 * A summary file has been decoded without symbol information and it therefore
 * does not contain stack information. A summary file is a base class for CrashFile. 
 *
 */
public class SummaryFile extends CrashAnalyserFile implements IEditorInput, Cloneable {

	public enum ContentType {CRASH, REGMSG, REPORT};

	// XML tags
	public static final String TAG_SYMBOL_SET = "symbol_set";
	public static final String TAG_SYMBOL = "symbol";
	public static final String TAG_SOURCE = "source";
	public static final String TAG_PROCESS = "process";
	public static final String TAG_THREAD = "thread";
	public static final String TAG_STACK = "stack";
	public static final String TAG_SEG_STACKS = "seg_stacks";
	public static final String TAG_ID = "id";
	public static final String TAG_STACK_ENTRY = "stack_entry";
	public static final String TAG_CODESEG = "codeseg";
	public static final String TAG_REGISTER_SET = "register_set";
	public static final String TAG_SEG_EVENT_LOG = "seg_event_log";
	public static final String TAG_SEG_TRACES = "seg_traces";
	public static final String TAG_MESSAGE = "message";
	public static final String TAG_VI_ENTRY = "vi_entry";
	public static final String TAG_SOURCE_INFO = "source_info";
	public static final String TAG_TYPE = "type";
	public static final String TAG_SEGMENT = "segment";
	public static final String TAG_MAJOR = "major";
	public static final String TAG_MINOR = "minor";

	// data from xml file
	protected Map<Integer, Process> processes = null;
	protected Map<Integer, Message> messages = null;
	protected Map<Integer, Stack> stacks = null;
	protected Map<Integer, RegisterSet> registerSets = null;
	protected List<RegisterDetails> registerDetails = null;
	protected EventLog eventLog = null;
	protected OstTrace ostTrace = null;
	protected Summary crashSummary = null;
	protected String sourceFileType = "";
	protected String sourceFileName = "";
	protected String sourceFilePath = "";

	
	/**
	 * Constructor
	 * @param filePath file path to this crash file
	 * @param library error library
	 */
	protected SummaryFile(String filePath, ErrorLibrary library) {
		super(filePath, library);
	}
	
	/**
	 * Constructor
	 * @param filePath file path to this crash file
	 * @param library error library
	 */
	protected SummaryFile(String filePath, ErrorLibrary library, Thread thread) {
		super(filePath, library);
		threadInfo = thread;
	}

	public Object clone() {
		SummaryFile newSummaryFile = null;
		try {
			// Just shallow copy (i.e. no need to read information from file)
			newSummaryFile = (SummaryFile) super.clone();
		} catch (CloneNotSupportedException ex) {
			// ignore
		}
		return newSummaryFile;
	}
	
	

	/**
	 * Returns the file type of this crash file.
	 * @return "Decoded File"
	 */
	public String getFileType() {
		return "Partially Decoded File";
	}
	
	public String getSourceFileType() {
		return sourceFileType;
	}
	
	public String getSourceFileName() {
		return sourceFileName;
	}
	
	public String getSourceFilePath() {
		return sourceFilePath;
	}
	
	
	public void setThread(Thread thread) {
		threadInfo = thread;
		panicCategory = threadInfo.getExitCategory();
		panicCode = threadInfo.getExitReason();
		threadName = threadInfo.getFullName();
	}
	
	public List<Stack> getStandAloneStacks() {
		if (stacks != null && !stacks.isEmpty()) {
			return new ArrayList<Stack>(stacks.values());
		}
		
		return null;
	}
	
	public List<RegisterSet> getStandAloneRegisterSets() {
		if (registerSets != null && !registerSets.isEmpty()) {
			return new ArrayList<RegisterSet>(registerSets.values());
		}
		
		return null;
	}
	
	/**
	 * Checks whether this file contains any error or warning messages.
	 * @return true if file contains errors or warning, otherwise false is returned
	 */
	public boolean containsErrorsOrWarnings() {
		List<Message> msgs = getMessages();
		if (msgs != null && !msgs.isEmpty()) {
			for (int i = 0; i < msgs.size(); i++) {
				if (Message.MessageTypes.ERROR.equals(msgs.get(i).getMessageType()) ||
					Message.MessageTypes.WARNING.equals(msgs.get(i).getMessageType()))
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the content type of the file.
	 * @return Returns crash/registration/report. If not found returns crash. 
	 */
	public ContentType getContentType() {
		List<Message> msgs = getMessages();
		if (msgs != null && !msgs.isEmpty()) {
			for (int i = 0; i < msgs.size(); i++) {
				if (Message.MessageTypes.MESSAGE.equals(msgs.get(i).getMessageType()) &&
						msgs.get(i).getTitle().equals("MobileCrash content type")) {
					if(msgs.get(i).getMessage().equals("registration")) 
						return ContentType.REGMSG;
					else if (msgs.get(i).getMessage().equals("report")) 
						return ContentType.REPORT;
				}
			}
		}
		// Returns crash by default.
		return ContentType.CRASH; 
	}

	/**
	 * Returns all messages
	 * @return all messages
	 */
	public List<Message> getMessages() {
		List<Message> msgs = new ArrayList<Message>();
		msgs.addAll(messages.values());
		return msgs;
	}

	/**
	 * Reads crash file
	 * @param file crash file
	 * @param library error library
	 * @return read crash file or null
	 */
	public static SummaryFile read(File file, ErrorLibrary library) {
		if (file == null || !file.exists() || !file.isFile())
			return null;
		
		SummaryFile summaryFile = new SummaryFile(file.getAbsolutePath(), library);
		summaryFile.doRead();
		return summaryFile;
	}
	
	/**
	 * Reads crash file
	 * @param folder where xml file is
	 * @param library error library
	 * @return read crash file or null
	 */
	public static SummaryFile read(String folder, ErrorLibrary library) {
		String summaryFile = findFile(folder,"xml");
		
		// summary file doesn't exist
		if (summaryFile == null)
			return null;
		
		SummaryFile file = new SummaryFile(summaryFile, library);
		file.doRead();
		return file;
	}

	/**
	 * Reads crash file
	 * @param folder where xml file is
	 * @param library error library
	 * @param thread thread
	 * @return read crash file or null
	 */
	public static SummaryFile read(String folder, ErrorLibrary library, Thread thread) {
		String summaryFile = findFile(folder,"xml");
		
		// summary file doesn't exist
		if (summaryFile == null)
			return null;
		
		SummaryFile file = new SummaryFile(summaryFile, library, thread);
		file.doRead();
		return file;
	}
	
		/**
	 * Writes crash file into text or html file
	 * @param filePathWithoutFileName file path where file is to be written to
	 * @param html if false a .txt file is created. if true a .html file is created
	 */
	public void writeTo(String filePathWithoutFileName, boolean html) {
		File file = new File(FileOperations.addSlashToEnd(filePathWithoutFileName) + 
								FileOperations.getFileNameWithoutExtension(fileName) + ".txt");
		if (html)
			file = new File(FileOperations.addSlashToEnd(filePathWithoutFileName) + 
								FileOperations.getFileNameWithoutExtension(fileName) + ".html");
		writeTo(file);
	}
	
	/**
	 * Writes crash file into a file
	 * @param file file to be written to (.xml, .crashxml, .html, . txt, etc)
	 */
	public void writeTo(File file) {
		try {
			// file is saved as .xml or .crashxml
			if (file.getName().endsWith(CrashAnalyserFile.OUTPUT_FILE_EXTENSION) ||
					file.getName().endsWith("."+CrashAnalyserFile.SUMMARY_FILE_EXTENSION)) {
				FileOperations.copyFile(new File(filePath), file, true);
			// file is saved as .txt or .html
			} else {
				BufferedWriter out = null;
				try {
					// Create file 
					FileWriter fstream = new FileWriter(file, false);
					out = new BufferedWriter(fstream);
					
					// check whether we are writing an html file
					boolean html = false;
					if (file.getName().toLowerCase().endsWith(".htm") ||
							file.getName().toLowerCase().endsWith(".html"))
						html = true;
					
					// if html file, write html start tags
					if (html)
						writeHtmlStart(out);

					// write crash analyser file version data
					writeVersion(out);

					String panicDescription = "";

					// write process & thread summary
					Process process = getCrashedProcess();
					if (process != null) {
						writeLine(out, "Process", process.getName());

						Thread thread = process.getFirstThread();
						if (thread != null) {
							writeLine(out, "Thread", thread.getFullName());
							writeLine(out, "Stack Pointer", thread.getStackPointer());
							writeLine(out, "Link Register", thread.getLinkRegister());
							writeLine(out, "Program Counter", thread.getProgramCounter());
							panicDescription = thread.getPanicDescription();
						}
					} else if (threadInfo != null) {
						Process threadProcess = getProcessByThread(threadInfo.getId());
						
						if (threadProcess != null)
							writeLine(out, "Process", threadProcess.getName());
						
						writeLine(out, "Thread", threadInfo.getFullName());
						writeLine(out, "Stack Pointer", threadInfo.getStackPointer());
						writeLine(out, "Link Register", threadInfo.getLinkRegister());
						writeLine(out, "Program Counter", threadInfo.getProgramCounter());
						panicDescription = threadInfo.getPanicDescription();						
					}
					
					// write crash summary to file
					if (crashSummary != null) {
						crashSummary.writeTo(out);
						out.newLine();
					}

					if (html)
						writeLine(out, "</pre><A HREF=\"#STACKPOINTER\">Current Stack Pointer</A><pre>");
					
					// write panic description if panic description exists. Panic description
					// is written only to html file (not to .txt)
					if (!"".equals(panicDescription) && html) {
						writeLine(out, "</pre><h2>Panic Description</h2>");
						writeLine(out, panicDescription);
						writeLine(out, "<pre>");
					}
					
					List<Message> msgs = getMessages();
					// write errors and warnings to file
					if (msgs != null && !msgs.isEmpty()) {
						for (int i = 0; i < msgs.size(); i++) {
							msgs.get(i).writeTo(out);
						}
						out.newLine();
					}
					
					// write event log to file
					if (eventLog != null)
						eventLog.writeTo(out);					
						
					// write OST trace to file
					if (ostTrace != null)
						ostTrace.writeTo(out);					

					// write process data to file
					if (process != null) {
						process.writeTo(out, Process.StackItems.ALL, html);
						out.newLine();
					}

					// if html file, write html end tags
					if (html)
						writeHtmlEnd(out);
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (out != null)
						out.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes CrashAnalyser version information.
	 * @param out where to write
	 * @throws IOException
	 */
	void writeVersion(BufferedWriter out) throws IOException {
		String version = (String) CrashAnalyserPlugin.getDefault().getBundle().getHeaders().get("Bundle-Version"); //$NON-NLS-1$
		out.write("Crash Report Created with Crash Analyser Carbide Extension " + version);
		out.newLine();
		out.write("----------------------------------------------------------------");
		out.newLine();
		out.newLine();
	}
	
	/**
	 * Writes html start tags
	 * @param out where to write
	 * @throws IOException
	 */
	void writeHtmlStart(BufferedWriter out) throws IOException {
		out.write("<html><head><title>Crash File</title></head><body><pre>");
		out.newLine();
	}
	
	/**
	 * Writes html end tags
	 * @param out where to write
	 * @throws IOException
	 */
	void writeHtmlEnd(BufferedWriter out) throws IOException {
		out.newLine();
		out.write("</pre></body></html>");
		out.newLine();
	}
	
	void writeLine(BufferedWriter out, String header, String value) throws IOException {
		if (!"".equals(value)) {
			out.write(String.format(Summary.FORMAT, header, value));
			out.newLine();
		}
	}

	void writeLine(BufferedWriter out, String line) throws IOException {
		out.write(line);
		out.newLine();
	}

	/**
	 * Reads the file location of the given xml file
	 * @param xmlFilePath path to xml file
	 * @return file location or "".
	 */
	public static String getSourceFilePath(String xmlFilePath) {

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			Document dom = db.parse(xmlFilePath);
			 
			// get the root element
			Element docEle = dom.getDocumentElement();		
			
			// read source file name
			NodeList nl = docEle.getElementsByTagName(TAG_SOURCE);
			if (nl != null && nl.getLength() > 0) {
				String sourcePath = XmlUtils.getNodeValue(nl.item(0));
				if (sourcePath != null && !"".equals(sourcePath)) {
					File f = new File(sourcePath);
					if (f.exists() && f.isFile()) {
						return sourcePath;
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	@Override
	protected void doRead() {
		super.doRead();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			Document dom = db.parse(filePath);
			 
			// get the root element
			Element docEle = dom.getDocumentElement();		
			
			// read source file type
			NodeList nl = docEle.getElementsByTagName(TAG_SOURCE_INFO);
			if (nl != null && nl.getLength() > 0) {
				sourceFileType = XmlUtils.getTextValue((Element)nl.item(0), TAG_TYPE);
			}
			
			// read source file name
			nl = docEle.getElementsByTagName(TAG_SOURCE);
			if (nl != null && nl.getLength() > 0) {
				String sourcePath = XmlUtils.getNodeValue(nl.item(0));
				if (sourcePath != null && !"".equals(sourcePath)) {
					File f = new File(sourcePath);
					if (f.exists() && f.isFile()) {
						sourceFileName = f.getName();
						sourceFilePath = sourcePath;
					}
				}
			}
			
			messages = new HashMap<Integer, Message>();
			
			// check that is the xml file newer than what we support
			nl = docEle.getElementsByTagName(TAG_SEGMENT);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					// if major number is larger than what we support, add an error to messages
					String major = XmlUtils.getTextValue((Element)nl.item(i), TAG_MAJOR);
					if (!"1".equals(major)) {
						messages.put(500, Message.newMessage(500, "Version Error", "XML file is newer than what is supported by this version of Crash Analyser.", Message.MessageTypes.ERROR));
						break;
					}
					// if minor number is larger than what we support, add a warning to messages
					String minor = XmlUtils.getTextValue((Element)nl.item(i), TAG_MINOR);
					if (!"00".equals(minor)) {
						messages.put(500, Message.newMessage(500, "Version Problem", "XML file has some new data which is not supported by this version of Crash Analyser.", Message.MessageTypes.WARNING));
						break;
					}
				}
			}
			
			// read summary data
			crashSummary = Summary.read(docEle);

			// read ROM id for this file
			if (crashSummary != null) {
				romId = crashSummary.getRomId();
			}
			
			// read event log
			nl = docEle.getElementsByTagName(TAG_SEG_EVENT_LOG);
			if (nl != null && nl.getLength() > 0) {
				eventLog = EventLog.read((Element)nl.item(0));
			}
			
			// read OST traces
			nl = docEle.getElementsByTagName(TAG_SEG_TRACES);
			if (nl != null && nl.getLength() > 0) {
				ostTrace = OstTrace.read((Element)nl.item(0));
			}

			// read messages from xml
			nl = docEle.getElementsByTagName(TAG_MESSAGE);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element)nl.item(i);
					Message message = Message.read(el);
					if (message != null)
						messages.put(message.getId(), message);
				}
			}			
			
			Map<Integer, CodeSegment> codeSegments = new HashMap<Integer, CodeSegment>();
			
			// read code segments
			nl = docEle.getElementsByTagName(TAG_CODESEG);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element)nl.item(i);
					CodeSegment codeSeg = CodeSegment.read(el, messages);
					if (codeSeg != null)
						codeSegments.put(codeSeg.getId(), codeSeg);
				}
			}

			Map<Integer, Symbol> symbols = new HashMap<Integer, Symbol>();

			// read symbols (Summary file won't have symbols, but this same method
			// is used by CrashFile)
			nl = docEle.getElementsByTagName(TAG_SYMBOL_SET);
			if(nl != null && nl.getLength() > 0) {
				// go throug all symbol_set tags
				for(int i = 0 ; i < nl.getLength(); i++) {
					// get the symbol_set element
					Element el = (Element)nl.item(i);
					NodeList childs = el.getChildNodes();
					// if symbol set has child nodes
					if (childs != null && childs.getLength() > 0) {
						String source = "";
						// go through all symbol_set child nodes
						for(int k = 0; k < childs.getLength(); k++) {
							Node child = childs.item(k);
							// if node is source node
							if (child.getNodeName() == TAG_SOURCE) {
								source = child.getFirstChild().getNodeValue();
							// if node is symbol node
							} else if (child.getNodeName() == TAG_SYMBOL) {
								Symbol symbol = Symbol.read((Element)child, source, codeSegments);
								if (symbol != null)
									symbols.put(symbol.getId(), symbol);
							}
						}
					}
				}
			}
					
			registerSets = new HashMap<Integer, RegisterSet>();
			Map<Integer, Register> allRegisters = new HashMap<Integer, Register>();
			
			// read register sets
			nl = docEle.getElementsByTagName(TAG_REGISTER_SET);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element)nl.item(i);
					RegisterSet registerSet = RegisterSet.read(el, symbols, messages);
					if (registerSet != null) {
						registerSets.put(registerSet.getId(),registerSet);
						
						// read all individual registers from sets
						List<Register> registers = registerSet.getRegisters();
						for (int j = 0; j < registers.size(); j++) {
							Register register = registers.get(j);
							if (!allRegisters.containsKey(register.getId())) {
								allRegisters.put(register.getId(), register);
							}
						}						
					}
				}
			}
			
			stacks = new HashMap<Integer, Stack>();
			
			// read stacks
			nl = docEle.getElementsByTagName(TAG_STACK);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element)nl.item(i);
					Stack stack = Stack.read(el, registerSets, allRegisters, symbols);
					if (stack != null)
						stacks.put(stack.getId(), stack);
				}
			}
			
			Map<Integer, Thread> threads = new HashMap<Integer, Thread>();
			
			// read threads
			nl = docEle.getElementsByTagName(TAG_THREAD);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element)nl.item(i);
					Thread thread = Thread.read(el, registerSets, symbols, stacks, errorLibrary);
					if (thread != null) {
						threads.put(thread.getId(), thread);
						stacks = thread.removeOwnStacks(stacks);
						registerSets = thread.removeOwnRegisterSets(registerSets);
					}
				}
			}
			
			processes = new HashMap<Integer, Process>();
			
			// read processes
			nl = docEle.getElementsByTagName(TAG_PROCESS);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element)nl.item(i);
					Process process = Process.read(el, threads, codeSegments);
					if (process != null)
						processes.put(process.getId(), process);
				}
			}
			
			registerDetails = new ArrayList<RegisterDetails>();
			
			// read register details
			nl = docEle.getElementsByTagName(TAG_VI_ENTRY);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element)nl.item(i);
					RegisterDetails details = RegisterDetails.read(el);
					if (details != null)
						registerDetails.add(details);
				}
			}

			// if xml contained crash date and time, parse them into this.time
			if (!"".equals(crashSummary.getCrashDate()) && !"".equals(crashSummary.getCrashTime()))
				time = crashSummary.getCrashDate() + " " + crashSummary.getCrashTime();
			
			// set panic data
			Process process = getCrashedProcess();
			processCount = getProcessCount();
			if (process != null) {
				Thread firstThread = process.getFirstThread();
				if (firstThread != null) {
					panicCategory = firstThread.getExitCategory();
					panicCode = firstThread.getExitReason();
					threadName = firstThread.getFullName();
				}
			} else if (threadInfo != null) {
				panicCategory = threadInfo.getExitCategory();
				panicCode = threadInfo.getExitReason();
				threadName = threadInfo.getFullName();
			}

			formatDescription();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Formats a description for this crash. 
	 */
	public void formatDescription() {
		Thread thread = null;
		
		if (threadInfo != null) {
			thread = threadInfo;
		}
		// get the first thread of the first process
		else if (processes != null && !processes.isEmpty()) {
			thread = getCrashedThread();
		}

		if (thread != null) {
			description = HtmlFormatter.formatCrashFileDescription(crashSummary, getMessages(), thread);
			shortDescription = HtmlFormatter.formatCrashFileDescription(crashSummary, null, thread);
		}
	}
	
	public Summary getSummary() {
		return crashSummary;
	}
	
	/**
	 * Get crashed process
	 * 
	 * @return first process or null
	 */
	public Process getCrashedProcess() {
		if (threadInfo != null) {
			return null;
		}
		
		Process crashedProcess = null;
		if (processes != null && !processes.isEmpty()) {
			Process[] processesArray = processes.values().toArray(new Process[processes.values().size()]);
			for (Process process : processesArray) {
				for (Thread thread : process.getThreads()) {
					String exitType = thread.getExitType();
					if (exitType.equalsIgnoreCase("Panic") || exitType.equalsIgnoreCase("Exception")) {
						crashedProcess = process;
						break;
					}					
				}
			}
		}
		return crashedProcess;
	}
	
	/**
	 * Get crashed process
	 * 
	 * @return first crashed thread
	 */
	public Thread getCrashedThread() {
		if (threadInfo != null) {
			return null;
		}
		
		Thread crashedThread = null;
		if (processes != null && !processes.isEmpty()) {
			Process[] processesArray = processes.values().toArray(new Process[processes.values().size()]);
			for (Process process : processesArray) {
				for (Thread thread : process.getThreads()) {
					String exitType = thread.getExitType();
					if (exitType.equalsIgnoreCase("Panic") || exitType.equalsIgnoreCase("Exception")) {
						crashedThread = thread;
						break;
					}					
				}
			}
		}
		return crashedThread;
	}

	/**
	 * Get process by thread id
	 * 
	 * @return Found process or null
	 */
	public Process getProcessByThread(int threadId) {
		
		Process threadProcess = null;
		if (processes != null && !processes.isEmpty()) {
			Process[] processesArray = processes.values().toArray(new Process[processes.values().size()]);
			for (Process process : processesArray) {
				for (Thread thread : process.getThreads()) {
					//String exitType = thread.getExitType();
					if (thread.getId() == threadId) {
						threadProcess = process;
						break;
					}					
				}
			}
		}
		return threadProcess;
	}
	
	/**
	 * Get process count
	 * 
	 * @return the number of processes or -1 if not available
	 */
	public int getProcessCount()
	{
		if (processes == null) {
			processCount = -1;
		}
		else {
			processCount = processes.size();
		}
		return processCount;
	}
	
	/**
	 * Get process count
	 * 
	 * @return the number of threads or -1 if not available
	 */
	public int getTotalThreadCount()
	{
		totalThreadCount = 0;
		if (processes == null) {
			return  -1;
		}
		for(Process process : processes.values()) {
			totalThreadCount += process.getThreads().size();
		}
		return totalThreadCount;
	}
	
	/**
	 * Returns all threads
	 * 
	 * @return all threads in all processes
	 */
	public List<Thread> getThreads() {
		List<Thread> allThreads = new ArrayList<Thread>();
		for(Process process : processes.values()) {
			allThreads.addAll(process.getThreads());
		}
		return allThreads;
	}

	public EventLog getEventLog() {
		return eventLog;
	}
	
	public OstTrace getOstTrace() {
		return ostTrace;
	}

	public List<RegisterDetails> getRegisterDetails() {
		return registerDetails;
	}
	
	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return fileName;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "Partial Crash Analyser File";
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}	
}
